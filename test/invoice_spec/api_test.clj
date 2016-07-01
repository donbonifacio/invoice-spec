(ns invoice-spec.api-test
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test :refer :all]
            [request-utils.core :as request-utils]
            [invoice-spec.api :as api]
            [invoice-spec.models.document :as document]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [environ.core :refer [env]]
            [result.core :as result]
            [clojure.data.xml :as xml]))

(defn is-valid-document? [document]
  (is (s/valid? :invoice-spec.models.document/document document)
      (s/explain-str :invoice-spec.models.document/document document)))

(deftest create-invoice-cancel-test
  (testing "creating an invoice"
    (let [invoice (document/new-invoice)
          result (<!! (api/create invoice))]
      (is (result/succeeded? result))
      (is-valid-document? result)

      (testing "finalizing an invoice"
        (let [result (<!! (api/finalize result))]
          (is (result/succeeded? result))
          (is-valid-document? result)
          (is (= "final" (:status result)))

          (testing "canceling an invoice"
            (let [result (<!! (api/cancel result))]
              (is (result/succeeded? result))
              (is-valid-document? result)
              (is (= "canceled" (:status result))))))))))

(deftest change-state-body-test
  (is (= "<invoice><state>finalized</state></invoice>"
         (api/change-state-body {} {:state "finalized"}))))
