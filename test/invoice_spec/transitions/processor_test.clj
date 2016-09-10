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

(comment

(deftest create-test
  (let [document (->> (gen/generate (document/document-generator))
                      (api/set-random-sequence {}))
        result (processor/run {:document document
                               :transitions [[:create {:offset-days 10}]]})]
    (is (result/succeeded? result))))

(deftest finalize-test
  (let [document (->> (gen/generate (document/document-generator))
                      (api/set-random-sequence {}))
        result (processor/run {:document document
                               :transitions [[:create {:offset-days 0}]
                                             [:finalize]]})]
    (is (result/succeeded? result))
    (is (= "final" (get-in result [:document :status])))))

(deftest with-invalid-test
  (let [document (->> (gen/generate (document/document-generator))
                      (api/set-random-sequence {}))
        result (processor/run {:document document
                               :transitions [[:create {:offset-days 0}]
                                             [:finalize]
                                             [:settle]
                                             [:cancel-last-receipt]
                                             #_[:finalize]]})]

    (is (result/succeeded? result))
    (is (= "final" (get-in result [:document :status])))))

(defspec document-transitions
  0
  (prop/for-all [document-type (document/type-generator)
                 create-transition (transition/create-generator)
                 transitions (transition/other-generator)]
                (let [transitions (concat [create-transition] transitions)
                      document-template (assoc (gen/generate (document/document-generator)) :type document-type)
                      document (api/set-random-sequence {} document-template)
                      result (processor/run {:document document
                                             :transitions transitions})]

                  (prn document-type transitions (get-in result [:document :status]))
                  (when (result/failed? result)
                    (prn "---")
                    (prn result))
                  (result/succeeded? result))))

(defspec document-sequence-transitions
  0
  (prop/for-all [document-type (document/type-generator)
                 create-transition (transition/create-generator)
                 transitions (transition/other-generator)]
                (let [transitions (concat [create-transition] transitions)
                      document (-> (gen/generate (document/document-generator))
                                   (assoc :type document-type)
                                   (api/set-random-sequence))
                      result (processor/run {:document document
                                             :transitions transitions})]

                  (prn document-type transitions (get-in result [:document :status]))
                  (when (result/failed? result)
                    (prn "---")
                    (prn result))
                  (result/succeeded? result))))

)
