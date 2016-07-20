(ns invoice-spec.transitions.create
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [result.core :as result]
            [invoice-spec.dates :as dates]
            [invoice-spec.api.documents :as api]
            [invoice-spec.models.document :as document]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [invoice-spec.transitions.transition :as transition]))

(defn- offset-days [transition]
  (if-let [args (second transition)]
    (or (:offset-days args) 0)
    0))

(defn- validate [document transition]
  (cond
    (not= "draft" (:status document))
      (result/failure "status-mismatch")

    (not (integer? (:id document)))
      (result/failure "no-id")

    (not= (dates/offset (offset-days transition)) (:date document))
      (result/failure "date-mismatch")

    :else (result/success)))

(defn- set-dates [document transition]
  (let [date (dates/offset (offset-days transition))]
    (assoc document :date date
                    :due_date date)))

(defmethod transition/operate :create [context transition]
  (result/enforce-let [document (result/presence (:document context))
                       created (<!! (api/create context (set-dates document transition)))
                       valid? (document/validate created)
                       valid-logic? (validate created transition)]
  (result/success {:document created})))
