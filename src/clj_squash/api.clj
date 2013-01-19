(ns clj-squash.api
  (:require [clj-http.client     :as http]
            [clj-stacktrace.core :as stacktrace]
            [clj-stacktrace.repl :as stacktrace-repl]
            [cheshire.core       :as json]
            [clojure.java.shell  :as shell]
            [clojure.string      :as s])
  (:import [java.lang.management ManagementFactory]
           [java.net InetAddress]))

(defn- stack-trace-data
  [exception]
  (let [data (stacktrace/parse-exception exception)]
    (mapv (fn [el]
            [(:file el)
             (:line el)
             (stacktrace-repl/method-str el)])
          (:trace-elems data))))

(defn- backtraces-data
  [exception]
  (when exception
    [[(.. java.lang.Thread currentThread getName)
      true
      (stack-trace-data exception)]]))

(defn- exception-data
  [exception]
  {:message    (.getMessage exception)
   :backtraces (backtraces-data exception)
   :class_name (.getName (class exception))
   :user_data  (ex-data exception)})

(defn- pid
  []
  (let [runtime-name (.. ManagementFactory getRuntimeMXBean getName)]
    (if-let [[[_ pid]] (re-seq #"^(\d+)@\w+" runtime-name)]
      pid)))

(defn- hostname
  []
  (.. InetAddress getLocalHost getHostName))

(defn- environment-data
  []
  {:pid       (pid)
   :hostname  (hostname)
   :env_vars  (System/getenv)
   ;; TODO: No reliable way to find out in java?
   :arguments {}}) 

(defn- notification-data
  [{:keys [api-key environment revision]} exception]
  (merge (exception-data exception)
         (environment-data)
         {:client "clojure"
          :api_key api-key
          :environment environment
          :revision revision
          :occurred_at (java.util.Date.)}))

(defn- git-version
  []
  (try
    (s/trim (:out (shell/sh "git" "rev-parse" "--short" "HEAD")))
    (catch Exception e
      (throw (ex-info "Unable to determine git version. You can specify a revision manually using the :revision key." {} e)))))

(defn notifier
  [{:keys [api-key
           api-host
           environment
           revision
           socket-timeout
           conn-timeout]
    :or   {:revision (git-version)
           :socket-timeout 1000
           :conn-timeout 1000}
    :as   options}]
  {:pre [(string? api-key)
         (string? api-host)
         (string? environment)]}
  (let [api-url (str api-host "/api/1.0/notify")]
    (fn [exception]
      (http/post api-url
                 {:body (json/generate-string (notification-data options exception))
                  :content-type :json
                  :socket-timeout socket-timeout
                  :conn-timeout conn-timeout
                  :accept :json}))))

(comment

  (let [notify (notifier {:api-host "http://localhost:8081"
                          :api-key "fa2818ce-5480-4aa0-87a9-43a342bf425a"
                          :environment "dev"
                          :revision "e30c54e"})]
    (notify (ex-info "Invalid use of robot" {:robot-id 42})))
  

  )