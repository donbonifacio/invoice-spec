(ns invoice-spec.transitions.processor
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [invoice-spec.transitions.transition :as transition]))

(defn run [context]
  (reduce transition/operate context (:transitions context)))
