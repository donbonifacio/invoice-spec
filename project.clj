(defproject invoice-spec "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :aliases {"autotest" ["trampoline" "with-profile" "+test,+test-deps" "test-refresh"]}

  :dependencies [[org.clojure/clojure "1.9.0-alpha9"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/test.check "0.9.0"]
                 [aleph "0.4.1"]
                 [environ "1.0.3"]
                 [walmartlabs/datascope "0.1.0"]
                 [weareswat/request-utils "0.4.1"]]
  :plugins [[lein-environ "1.0.3"]]


  :profiles {:test-deps {:dependencies [[ring/ring-mock "0.3.0"]
                                        [org.clojure/tools.namespace "0.2.11"]]

                         :plugins [[com.jakemccrary/lein-test-refresh "0.15.0"]
                                   [lein-cloverage "1.0.2"]]}}

  :test-refresh {:quiet true
                 :changes-only true})
