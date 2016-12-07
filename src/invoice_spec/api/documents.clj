(ns invoice-spec.api.documents
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [request-utils.core :as request-utils]
            [clojure.core.async :refer [chan <!! >!! close! go <! timeout go-loop]]
            [invoice-spec.api.common :as common]
            [invoice-spec.models.sequence :as sequences]
            [invoice-spec.api.sequences :as seq-api]
            [result.core :as result]
            [environ.core :refer [env]]
            [clojure.data.xml :as xml]))

(def load-from-xml common/load-from-xml)
(def load-coll-from-xml common/load-coll-from-xml)

(defn doc-path [document]
  (cond
    (= "InvoiceReceipt" (:type document)) "invoice_receipt"
    (= "SimplifiedInvoice" (:type document)) "simplified_invoice"
    (= "Receipt" (:type document)) "receipt"
    (= "Quote" (:type document)) "quote"
    :else "invoice"))

(defn sequence-key [document]
  (let [path (doc-path document)]
    (keyword (str "current_" path "_sequence_id"))))

(defn optional-element [document prop-key]
  (if-let [prop-value (get document prop-key)]
    (xml/element prop-key {} prop-value)))

(defn document-xml [document]
  (let [client (:client document)]
    (xml/element (keyword (doc-path document)) {}
                 (xml/element :date {} (:date document))
                 (optional-element document :sequence_number)
                 (optional-element document :sequence_id)
                 (optional-element document :reference)
                 (optional-element document :observations)
                 (xml/element :status {} (:status document))
                 (xml/element :client {}
                              (xml/element :name {} (:name client))
                              (optional-element client :email)
                              (optional-element client :country)
                              (optional-element client :postal_code)
                              (optional-element client :address)
                              (optional-element client :city)
                              (optional-element client :send_options)
                              (optional-element client :website)
                              (optional-element client :phone)
                              (optional-element client :fax)
                              (optional-element client :language)
                              (xml/element :code {} (:code client)))
                 (xml/element :items {:type "array"}
                    (map (fn [item]
                           (xml/element :item {}
                             (xml/element :name {} (:name item))
                             (xml/element :unit_price {} (:unit_price item))
                             (xml/element :quantity {} (:quantity item))
                             (xml/element :description {} (:description item))))
                         (:items document))))))

(defn document-xml-str [document]
  (xml/emit-str (document-xml document)))

(defn create [options document]
  (go
    (result/on-success [response (<! (request-utils/http-post
                                       {:host (common/from-path options (str "/" (doc-path document) "s.xml"))
                                        :headers {"Content-type" "application/xml; charset=utf-8"}
                                        :plain-body? true
                                        :body (document-xml-str document)}))]
      (result/success (load-from-xml (:body response))))))

(defn reload-document [options {:keys [id] :as document}]
  {:pre [(some? id)]}
  (go
    (result/on-success [response (<! (request-utils/http-get
                                       {:host (common/from-path options (str "/" (doc-path document) "s/" id ".xml"))
                                        :headers {"Content-type" "application/xml; charset=utf-8"}
                                        :plain-body? true}))]
      (result/success (load-from-xml (:body response))))))

(defn related-documents [options {:keys [id] :as document}]
  {:pre [(some? id)]}
  (go
    (result/on-success [response (<! (request-utils/http-get
                                       {:host (common/from-path options (str "/document/" id "/related_documents.xml"))
                                        :headers {"Content-type" "application/xml; charset=utf-8"}
                                        :plain-body? true}))]
      (result/success {:documents (load-coll-from-xml (:body response))}))))

(defn change-state-body [document data]
  (let [path (doc-path document)]
    (str "<"path">"
           (reduce-kv (fn [all k v]
                     (str all "<"(name k)">"v"</"(name k)">"))
                   ""
                   data)
          "</"path">")))

(defn change-state [options {:keys [id] :as document} data]
  {:pre [(some? id)]}
  (go
    (result/enforce-let [response (<! (request-utils/http-put
                                         {:host (common/from-path options (str "/"(doc-path document)"s/" id "/change-state.xml"))
                                          :headers {"Content-type" "application/xml; charset=utf-8"}
                                          :plain-body? true
                                          :body (change-state-body document data)}))
                         document (<! (reload-document options document))]
        document)))

(defn finalize [options document]
  (change-state options document {:state "finalized"}))

(defn cancel [options document & cancel-message]
  (change-state options
                document
                {:state "canceled"
                 :message (or cancel-message "Canceled")}))

(defn settle [options document]
  (change-state options document {:state "settled"}))

(defn delete [options document]
  (change-state options document {:state "deleted"}))

(defn set-random-sequence [options document]
  (let[serie (gen/generate (sequences/serie-generator))
        ixseq (<!! (seq-api/create options {:serie serie}))
        seq-id (get ixseq (sequence-key document))
        document (assoc document :sequence_id seq-id)]
    document))

(defn download-pdf [options {:keys [id] :as document}]
  {:pre [(some? id)]}
  (go-loop [tries 0]
    (if (>= tries 50)
      (result/failure {:tries tries :data "no-response"})
      (result/on-success [response (<! (request-utils/http-get
                                         {:host (common/from-path options (str "/api/pdf/" id ".xml"))
                                          :headers {"Content-type" "application/xml; charset=utf-8"}
                                          :plain-body? true}))]
        (if (= 202 (:status response))
          (do
            (<! (timeout 1000))
            (recur (inc tries)))
          (result/success (load-from-xml (:body response))))))))
