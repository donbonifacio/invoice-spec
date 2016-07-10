(ns invoice-spec.transitions.processor
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [result.core :as result]
            [invoice-spec.transitions.transition :as transition]))

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
