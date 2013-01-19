(defproject clj-squash "0.1.2-SNAPSHOT"
  :description "Clojure client library for Square Squash"
  :url "http://github.com/ragnard/clj-squash"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-http "0.6.3"]
                 [clj-stacktrace "0.2.5"]
                 [cheshire "5.0.1"]]
  :profiles {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0-RC1"]]}}
  :aliases {"all" ["with-profile" "1.3:1.4:1.5"]})
