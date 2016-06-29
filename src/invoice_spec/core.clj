(ns invoice-spec.core
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [invoice-spec.models.document :as document]
            [invoice-spec.api :as api]))

;; https://github.com/weareswat/invoicexpress/pull/2498

(api/document-xml-str (gen/generate (s/gen :invoice-spec.models.document/document)))

