(ns invoice-spec.transitions.processor-test
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [invoice-spec.transitions.transition :as transition]
            [clojure.test.check.properties :as prop]
            [result.core :as result]
            [invoice-spec.api.documents :as api]
            [invoice-spec.api.sequences :as seq-api]
            [invoice-spec.models.document :as document]
            [invoice-spec.models.sequence :as sequences]
            [clojure.test.check.clojure-test :refer [defspec]]
            [invoice-spec.transitions.processor :as processor]))

(deftest create-test
  (let [document (-> (gen/generate (document/document-generator))
                     (api/set-random-sequence))
        result (processor/run {:document document
                               :transitions [[:create {:offset-days 10}]]})]
    (is (result/succeeded? result))))

(deftest finalize-test
  (let [document (-> (gen/generate (document/document-generator))
                     (api/set-random-sequence))
        result (processor/run {:document document
                               :transitions [[:create {:offset-days 0}]
                                             [:finalize]]})]
    (is (result/succeeded? result))
    (is (= "final" (get-in result [:document :status])))))

(deftest with-invalid-test
  (let [document (-> (gen/generate (document/document-generator))
                     (api/set-random-sequence))
        result (processor/run {:document document
                               :transitions [[:create {:offset-days 0}]
                                             [:finalize]
                                             [:delete]
                                             [:cancel]
                                             [:finalize]]})]

    (is (result/succeeded? result))
    (is (= "canceled" (get-in result [:document :status])))))

(defspec document-transitions
  1000
  (prop/for-all [document-type (document/type-generator)
                 transitions (transition/generator)]
                (let [document (-> (gen/generate (document/document-generator))
                                   (assoc :type document-type)
                                   (api/set-random-sequence))
                      result (processor/run {:document document
                                             :transitions transitions})]

                  (prn document-type transitions)
                  (when (result/failed? result)
                    (prn "---")
                    (prn result))
                  (result/succeeded? result)
                )))
