(ns com.github.ragnard.clj-squash.ring
  (:require [com.github.ragnard.clj-squash.api :as squash]))

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
          (notify e)
          (throw e))))))
