(ns invoice-spec.api.sequences
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [request-utils.core :as request-utils]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [result.core :as result]
            [environ.core :refer [env]]
            [invoice-spec.api.common :as common]
            [clojure.data.xml :as xml]))

(def url common/url)
(def load-from-xml common/load-from-xml)

(defn create [options data]
  (go
    (result/on-success [response (<! (request-utils/http-post
                                       {:host (common/from-path options (str "/sequences.xml"))
                                        :headers {"Content-type" "application/xml; charset=utf-8"}
                                        :plain-body? true
                                        :body (str "<sequence>"
                                                   "<serie>" (:serie data) "</serie>"
                                                   "</sequence>")}))]
      (result/success (load-from-xml (:body response))))))
