(ns invoice-spec.api
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.data.xml :as xml]))

(defn document-xml [document]
  (xml/element :invoice {}
               (xml/element :date {} (:date document))
               (xml/element :status {} (:status document))))

(defn document-xml-str [document]
  (xml/emit-str (document-xml document)))


