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

(deftest create-invoice-test
  (testing "creating an invoice"
    (let [invoice (document/new-invoice)
          result (<!! (api/create invoice))]
      (is (result/succeeded? result))
      (is-valid-document? result)

      (testing "finalizing an invoice"
        (let [result (<!! (api/finalize result))]
          (prn result)
          (is (result/succeeded? result))
          (is-valid-document? result))))))
