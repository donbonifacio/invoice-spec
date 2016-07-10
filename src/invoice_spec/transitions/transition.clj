(ns invoice-spec.transitions.transition
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(defmulti operate #(first %2))

(def transition-keys #{:create})

(def create-examples
  #{[:create]
    [:create {:offset-days -20}]
    [:create {:offset-days -10}]
    [:create {:offset-days 10}]
    [:create {:offset-days 20}]})

(s/def ::create-examples create-examples)

(def transition-examples
  #{[:finalize]})

(s/def ::transition-examples transition-examples)

(s/def ::transition (fn [data]
                      (some transition-keys [(first data)])))

(s/def ::complete-transitions (s/cat :create-part ::create-examples
                                     :other-part (s/+ ::transition-examples)))

(defn generator []
  (s/gen ::complete-transitions))

#_(prn (gen/sample (s/gen ::transition)))
#_(prn (gen/generate (generator)))

(defn valid-operation-status? [result]
  (< 199 (:status result) 500)
  )
