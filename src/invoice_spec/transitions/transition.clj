(ns invoice-spec.transitions.transition
  (:require [clojure.spec :as s]
            [invoice-spec.api.documents :as api]
            [result.core :as result]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
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
  #{[:finalize]
    [:delete]})

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
  #_(< 199 (:status result) 500)
  true)

(defn untouched [document]
  (let [previous document
        current (<!! (api/reload-document previous))]
    (if (= previous current)
      (result/success)
      (result/failure {:error "document-mismatch"
                       :previous previous
                       :current current}))))

(defn process-failure [result document]
  (if (valid-operation-status? result)
    (result/on-success [document-untouched? (untouched document)]
      (result/success {:document document}))
    result))

