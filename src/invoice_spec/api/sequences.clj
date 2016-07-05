(ns invoice-spec.api.sequences
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [request-utils.core :as request-utils]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout]]
            [result.core :as result]
            [environ.core :refer [env]]
            [clojure.data.xml :as xml]))
