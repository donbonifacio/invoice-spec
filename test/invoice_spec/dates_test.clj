(ns invoice-spec.dates-test
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test :refer :all]
            [result.core :as result]
            [invoice-spec.dates :as dates]))

(deftest today-test
  (is (dates/today)))

(deftest offset-test
  (is (dates/offset 10)))
