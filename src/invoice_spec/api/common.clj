(ns invoice-spec.api.common
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [request-utils.core :as request-utils]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [result.core :as result]
            [environ.core :refer [env]]
            [clojure.data.xml :as xml]))

(defn env-port []
  (or (env :ix-api-port) "3001"))

(defn url [{:keys [host port path api-key]}]
  (let [host (or host (env :ix-api-host))
        port (or port (env-port))
        api-key (or api-key (env :ix-api-key))]
    (assert host)
    (assert port)
    (assert path)
    (assert api-key)
    (str host ":" port path "?api_key=" api-key)))

(defn from-path [options path]
  (url (merge options {:path path})))

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

(defn load-coll-from-xml [raw-xml-str]
  (let [xml-data (xml/parse-str raw-xml-str)]
    (map (fn [xml-data]
           (xml->map (:content xml-data) {:type (:tag xml-data)}))
         (:content xml-data))))
