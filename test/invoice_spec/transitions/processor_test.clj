(ns invoice-spec.transitions.processor-test
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test :refer :all]
            [invoice-spec.api.documents :as api]
            [invoice-spec.api.sequences :as seq-api]
            [invoice-spec.models.document :as document]
            [invoice-spec.models.sequence :as sequences]
            [clojure.test.check.clojure-test :refer [defspec]]
            [invoice-spec.transitions.processor :as processor]))

(deftest create-test
  (let [result (processor/run {:transitions [[:create {:offset-days 0}]]})]
    (is result)))
