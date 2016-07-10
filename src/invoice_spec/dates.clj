(ns invoice-spec.dates
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clj-time.local :as l]
            [result.core :as result]))

(def custom-formatter (f/formatter "dd/MM/yyyy"))

(defn today []
  (f/unparse custom-formatter (l/local-now)))

(defn offset [days]
  (f/unparse custom-formatter (t/plus (l/local-now) (t/days days))))
