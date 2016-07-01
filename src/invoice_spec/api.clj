(ns invoice-spec.api
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [request-utils.core :as request-utils]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [result.core :as result]
            [environ.core :refer [env]]
            [clojure.data.xml :as xml]))

(defn document-xml [document]
  (xml/element :invoice {}
               (xml/element :date {} (:date document))
               (if-let [sequence-number (:sequence_number document)]
                 (xml/element :sequence_number {} sequence-number))
               (xml/element :status {} (:status document))
               (xml/element :client {}
                            (xml/element :name {} (get-in document [:client :name]))
                            (if-let [language (get-in document [:client :language])]
                              (xml/element :language {} language))
                            (xml/element :code {} (get-in document [:client :code])))
               (xml/element :items {:type "array"}
                  (map (fn [item]
                         (xml/element :item {}
                           (xml/element :name {} (:name item))
                           (xml/element :unit_price {} (:unit_price item))
                           (xml/element :quantity {} (:quantity item))
                           (xml/element :description {} (:description item))))
                       (:items document)))))

(defn document-xml-str [document]
  (xml/emit-str (document-xml document)))

(defn url [path]
  (str (env :ix-api-host) path "?api_key=" (env :ix-api-key)))

(def boolean-fields #{:archived})
(def number-fields #{:taxes :total :id :before_taxes :discount :sum :value
                     :unit_price :quantity :tax_amount :subtotal :discount_amount})

(defn add-field [m xml-elem]
  (let [tag (:tag xml-elem)
        raw (first (:content xml-elem))]
    (assoc m tag (cond
                   (some boolean-fields [tag]) (= "true" raw)
                   (some number-fields [tag]) (read-string raw)
                   :else raw))))

(defn xml->map [xml-elements m]
  (reduce (fn [m xml-elem]
            (cond
              (= :client (:tag xml-elem))
                (assoc m :client (xml->map (:content xml-elem) {}))
              (= :tax (:tag xml-elem))
                (assoc m :tax (xml->map (:content xml-elem) {}))
              (= :items (:tag xml-elem))
                (assoc m :items (map #(xml->map (:content %) {}) (:content xml-elem)))
              (= :invoice_timeline (:tag xml-elem))
                m
              :else
                (add-field m xml-elem)))
          m
          xml-elements))

(defn load-from-xml [raw-xml-str]
  (let [xml-data (xml/parse-str raw-xml-str)]
    (xml->map (:content xml-data) {:type (:tag xml-data)})))

(defn create [document]
  (go
    (result/on-success [response (<! (request-utils/http-post
                                       {:host (url "/invoices.xml")
                                        :headers {"Content-type" "application/xml; charset=utf-8"}
                                        :plain-body? true
                                        :body (document-xml-str document)}))]
      (result/success (load-from-xml (:body response))))))

(defn reload-document [{:keys [id] :as document}]
  {:pre [(some? id)]}
  (go
    (result/on-success [response (<! (request-utils/http-get
                                       {:host (url (str "/invoices/" id ".xml"))
                                        :headers {"Content-type" "application/xml; charset=utf-8"}
                                        :plain-body? true}))]
      (result/success (load-from-xml (:body response))))))

(defn finalize [{:keys [id] :as document}]
  {:pre [(some? id)]}
  (go
    (result/enforce-let [response (<! (request-utils/http-put
                                         {:host (url (str "/invoice/" id "/change-state.xml"))
                                          :headers {"Content-type" "application/xml; charset=utf-8"}
                                          :plain-body? true
                                          :body (str "<invoice>"
                                                      "<state>finalized</state>"
                                                     "</invoice>")}))
                         document (<! (reload-document document))]
        document)))
