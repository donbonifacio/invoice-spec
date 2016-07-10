(ns invoice-spec.transitions.finalize
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [result.core :as result]
            [invoice-spec.dates :as dates]
            [invoice-spec.api.documents :as api]
            [invoice-spec.models.document :as document]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [invoice-spec.transitions.transition :as transition]))

(defn- validate [document]
  (cond
    (not= "final" (:status document))
      (result/failure "status-mismatch")

    :else (result/success)))

(defn process-success [final]
  (result/enforce-let [valid? (document/validate final)
                       valid-logic? (validate final)]
    (result/success {:document final})))

(defn untouched [document]
  (let [previous document
        current (<!! (api/reload-document previous))]
    (if (= previous current)
      (result/success)
      (result/failure {:error "document-mismatch"
                       :previous previous
                       :current current}))))

(defn process-failure [result document]
  (if (transition/valid-operation-status? result)
    (result/on-success [document-untouched? (untouched document)]
      (result/success {:document document}))
    result))

(defmethod transition/operate :finalize [context transition]
  (result/on-success [document (result/presence (:document context))]
    (let [final (<!! (api/finalize document))]
      (if (result/succeeded? final)
        (process-success final)
        (process-failure final document)))))
