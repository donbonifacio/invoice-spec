(ns invoice-spec.core
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [invoice-spec.models.document :as document]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]))

;; https://github.com/weareswat/invoicexpress/pull/2498
