(ns invoice-spec.core
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [invoice-spec.models.document :as document]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [invoice-spec.api :as api]))

;; https://github.com/weareswat/invoicexpress/pull/2498
(prn 1)

(api/document-xml-str (gen/generate (s/gen :invoice-spec.models.document/document)))

(comment
  (let [document (gen/generate (s/gen :invoice-spec.models.document/document))]
    (<!! (api/create document))))
