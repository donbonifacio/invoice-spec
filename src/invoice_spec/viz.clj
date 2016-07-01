(ns invoice-spec.viz
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [invoice-spec.models.document :as document]
            [com.walmartlabs.datascope :as ds]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]))

(defn show-document [document]
  (ds/view document))

#_(show-document (document/random))
