(ns invoice-spec.api.common
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [request-utils.core :as request-utils]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [result.core :as result]
            [environ.core :refer [env]]
            [clojure.data.xml :as xml]))

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

