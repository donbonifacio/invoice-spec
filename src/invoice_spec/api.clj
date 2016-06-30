(ns invoice-spec.api
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
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


