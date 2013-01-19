(ns com.github.ragnard.clj-squash.api
  (:require [clj-http.client     :as http]
            [clj-stacktrace.core :as stacktrace]
            [clj-stacktrace.repl :as stacktrace-repl]
            [cheshire.core       :as json]
            [clojure.java.shell  :as shell]
            [clojure.string      :as s])
  (:import [java.lang.management ManagementFactory]
           [java.net InetAddress]))

;; Utility functions

(defn- pid
  []
  ;; Really? No better way?
  (let [runtime-name (.. ManagementFactory getRuntimeMXBean getName)]
    (if-let [[[_ pid]] (re-seq #"^(\d+)@\w+" runtime-name)]
      pid)))

(defn- hostname []
  (.. InetAddress getLocalHost getHostName))

(defn- git-version
  []
  (try
    (s/trim (:out (shell/sh "git" "rev-parse" "--short" "HEAD")))
    (catch Exception _ nil)))

;; Functions for creating exception data in Squash format/structure

(defn- stack-trace-data
  [exception]
  (let [data (stacktrace/parse-exception exception)]
    (mapv (fn [el]
            [(or (:file el) "unknown")
             (or (:line el) 0)
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

;; Public api

(defn notifier
  "Return a function that when applied to an Exception will send a
  notification to a Squash instance.

  An update function can also be passed as an optional second
  argument. If given, it will be called with a map of the data that is
  to be sent to Squash, and and is expected to return a potentially
  updated version of this map. This can be used to extend, access or
  in any way alter the data sent in the notification.

  Options: 
    :api-key        - required
    :api-host       - required
    :environment    - required
    :revision       - optional, but requires current working directory
                      to be a Git repository if not specified explicitly
    :error-handler  - optional, function of two arguments called when
                      the submission of a notification fails. First
                      argument is the immediate exception, second
                      argument the original exception.
                      exception
    :socket-timeout - optional
    :conn-timeout   - optional"
  [{:keys [api-key
           api-host
           environment
           revision
           socket-timeout
           conn-timeout
           exception-handler]
    :or   {socket-timeout 1000
           conn-timeout 1000}
    :as   options}]
  {:pre [(string? api-key)
         (string? api-host)
         (string? environment)]}
  (let [api-url (str api-host "/api/1.0/notify")
        options (merge options
                       {:revision (or revision
                                      (git-version)
                                      (throw (Exception. "Unable to
                                      determine Git version. You can
                                      specify a revision manually
                                      using the :revision key.")))})]
    (fn notify
      ([exception]
         (notify exception identity))
      ([exception update-fn]
         (try
           (let [data-fn (comp (or update-fn identity) notification-data)
                 data (data-fn options exception)]
             (http/post api-url
                        {:body (json/generate-string data)
                         :content-type :json
                         :socket-timeout socket-timeout
                         :conn-timeout conn-timeout
                         :accept :json}))
           (catch Exception e
             (when exception-handler
               (exception-handler e exception))))))))

(comment

  (let [notify (notifier {:api-host "http://localhost:8081"
                          :api-key "fa2818ce-5480-4aa0-87a9-43a342bf425a"
                          :environment "dev"})]
    (notify (ex-info "Invalid use of robot" {:robot-id 42})))

 )
