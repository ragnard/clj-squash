(ns clj-squash.ring
  (:require [clj-squash.api :as squash]))

(defn wrap-squash
  [handler options]
  (let [notify (squash/notifier options)] 
    (fn [req]
      (try
        (handler req)
        (catch Exception e (notify e))))))

