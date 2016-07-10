(ns invoice-spec.transitions.processor
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [result.core :as result]
            [invoice-spec.transitions.transition :as transition]
            [invoice-spec.transitions.create]
            [invoice-spec.transitions.delete]
            [invoice-spec.transitions.cancel]
            [invoice-spec.transitions.settle]
            [invoice-spec.transitions.finalize]))

(defn- operate [context transition]
  (let [result (transition/operate context transition)]
    (if (result/succeeded? result)
      (-> context
          (assoc :success true)
          (assoc :document (:document result)))
      (reduced
        (assoc result :transition transition)))))

(defn run [context]
  (reduce operate context (:transitions context)))
