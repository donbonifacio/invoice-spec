(ns invoice-spec.transitions.settle
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [result.core :as result]
            [invoice-spec.dates :as dates]
            [invoice-spec.api.documents :as api]
            [invoice-spec.models.document :as document]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [invoice-spec.transitions.transition :as transition]))

(defn- validate [document]
  (let [status "settled"]
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

(defmethod transition/operate :settle [context transition]
  (result/on-success [document (result/presence (:document context))]
    (let [settled (<!! (api/settle document))]
      (if (result/succeeded? settled)
        (process-success settled)
        (transition/process-failure settled document)))))
