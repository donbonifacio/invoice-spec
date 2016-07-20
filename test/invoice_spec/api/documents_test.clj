(ns invoice-spec.api.documents-test
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test :refer :all]
            [request-utils.core :as request-utils]
            [invoice-spec.api.documents :as api]
            [invoice-spec.api.sequences :as seq-api]
            [invoice-spec.models.document :as document]
            [invoice-spec.models.sequence :as sequences]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [environ.core :refer [env]]
            [result.core :as result]
            [clojure.data.xml :as xml]))

(defn is-valid-document? [document]
  (is (s/valid? :invoice-spec.models.document/document document)
      (s/explain-str :invoice-spec.models.document/document document)))

(deftest create-invoice-cancel-test

  (testing "creating an invoice"
    (let [options {}
          invoice (->> (document/new-invoice)
                       (api/set-random-sequence options))
          result (<!! (api/create options invoice))]
      (is (result/succeeded? result))
      (is-valid-document? result)

      (testing "finalizing an invoice"
        (let [result (<!! (api/finalize options result))]
          (is (result/succeeded? result))
          (is-valid-document? result)
          (is (= "final" (:status result)))

          (testing "canceling an invoice"
            (let [result (<!! (api/cancel options result))]
              (is (result/succeeded? result))
              (is-valid-document? result)
              (is (= "canceled" (:status result))))))))))

(deftest create-invoice-pay-test
  (testing "creating an invoice"
    (let [options {}
          invoice (->> (document/new-invoice)
                       (api/set-random-sequence options))
          result (<!! (api/create options invoice))]
      (is (result/succeeded? result))
      (is-valid-document? result)

      (testing "finalizing an invoice"
        (let [result (<!! (api/finalize options result))]
          (is (result/succeeded? result))
          (is-valid-document? result)
          (is (= "final" (:status result)))

          (testing "paying an invoice"
            (let [result (<!! (api/settle options result))]
              (is (result/succeeded? result))
              (is-valid-document? result)
              (is (= "settled" (:status result))))))))))

(deftest change-state-body-test
  (is (= "<invoice><state>finalized</state></invoice>"
         (api/change-state-body {} {:state "finalized"}))))

(defspec all-documents-finalize
  0
  (prop/for-all [document-type (document/type-generator)
                 document (document/document-generator)]
                (let [options {}
                      document (->> (assoc document :type document-type)
                                    (api/set-random-sequence options))
                      created (<!! (api/create options document))
                      final (<!! (api/finalize options created))]
                  #_(prn (:id final) (:type final) (:sequence_number final))
                  (= (document/expected-final-status document)
                     (:status final)))))

(deftest related-documents-test
    (let [options {}
          invoice (->> (document/new-invoice)
                       (api/set-random-sequence options))
          result (<!! (api/create options invoice))]
      (is (result/succeeded? result))

      (let [result (<!! (api/finalize options result))]
        (is (result/succeeded? result))

        (let [result (<!! (api/settle options result))]
          (is (result/succeeded? result))
          (is (= "settled" (:status result)))

          (testing "related documents"
            (let [result (<!! (api/related-documents options result))]
              (is (result/succeeded? result))
              (is (= 1 (count (:documents result))))
              (is (= "Receipt" (-> result :documents first :type)))
              (is (= "final" (-> result :documents first :status)))))))))
