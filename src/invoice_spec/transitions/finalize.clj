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
  (let [status (document/expected-final-status document)]
    (cond
      (not= status (:status document))
        (result/failure {:error "status-mismatch"
                         :expected status
                         :got (:status document)})

      :else (result/success))))

(defn process-success [final]
  (result/enforce-let [valid? (document/validate final)
                       valid-logic? (validate final)]
    (result/success {:document final})))

(defmethod transition/operate :finalize [context transition]
  (result/on-success [document (result/presence (:document context))]
    (let [final (<!! (api/finalize context document))]
      (if (result/succeeded? final)
        (process-success final)
        (transition/process-failure final document)))))
