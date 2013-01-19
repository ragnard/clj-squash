(ns com.github.ragnard.clj-squash.ring
  (:require [com.github.ragnard.clj-squash.api :as squash]))

(defn- ring-request-data
  [req notify-data]
  (-> notify-data
      (assoc-in [:headers] (:headers req))
      (assoc-in [:params]  (select-keys req [:server-port
                                             :server-name
                                             :remote-addr
                                             :uri
                                             :query-string
                                             :scheme
                                             :request-method 
                                             :content-type
                                             :content-length
                                             :character-encoding]))))

(defn wrap-squash
  "Send notifications of any unhandeled exceptions to a Squash
  instance specified by options. Exception will be rethrown.

  See com.github.ragnard.clj-squash.api/notifier for options"
  [handler options]
  (let [notify (squash/notifier options)] 
    (fn [req]
      (try
        (handler req)
        (catch Exception e
          (notify e (partial ring-request-data req))
          (throw e))))))
