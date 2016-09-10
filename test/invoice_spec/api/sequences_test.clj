(ns invoice-spec.api.sequences-test
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test :refer :all]
            [request-utils.core :as request-utils]
            [invoice-spec.api.sequences :as api]
            [invoice-spec.models.sequence :as sequences]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [environ.core :refer [env]]
            [result.core :as result]
            [clojure.data.xml :as xml]))

#_(deftest create-sequence-test
  (let [serie (gen/generate (sequences/serie-generator))
        ixseq (<!! (api/create {} {:serie serie}))]
    (is (result/succeeded? ixseq))))
