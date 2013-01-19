# clj-squash

Clojure client library for the [Squash](http://www.squash.io/) bug
killer by [Square](http://www.square.com).

**NOTE**: This is currently the result of a few hours of hacking with
the purpose of trying out Squash for Clojure applications. No
thourough testing has been performed. 

## Artifacts

clj-squash is available on [Clojars](http://clojars.org) and the
current version is:

    [clj-squash "0.1.1"]

## Usage

Use the `notifier` function in `com.github.ragnard.clj-squash.api` to
create a function that when applied to an `Exception` will send a
notification to a Squash instance:

```` clojure
(require '[com.github.ragnard.clj-squash.api :as squash])
    
(let [notify (squash/notifier {:api-host "http://localhost:8081"
                               :api-key "fa2818ce-5480-4aa0-87a9-43a342bf425a"
                               :environment "dev"})]
  (notify (ex-info "Invalid use of robot" {:robot-id 42})))
````

An optional update function can also be passed as second argument to a
notification function. This function will be applied to the map of
notification data before it is transmitted, and is expected to return
a new, potentially different map. This can be used to extend, inspect
or alter the update data in any way imaginable, like so:

```` clojure
(require '[com.github.ragnard.clj-squash.api :as squash])

(let [notify (squash/notifier {:api-host "http://localhost:8081"
                               :api-key "fa2818ce-5480-4aa0-87a9-43a342bf425a"
                               :environment "dev"})]
  (notify (ex-info "Invalid use of robot" {:robot-id 42})
          (fn [notification-data]
            (update-in notification-data [:user_data] assoc :robot-favourite-pet "Turtle"))))
````

See doc for `notifier` for info on available options.

Any data conveyed with `ex-info` will be submitted as `user_data` to
Squash and is therefore nicely available in the UI.

### Ring Middleware

A [Ring](http://github.com/ring-clojure) middleware for sending
notifications of unhandled exceptions is available in the
`com.ragnard.clj-squash.ring` namespace.

```` clojure
(require '[com.github.ragnard.clj-squash.ring :as squash])
    
(-> handler
    (squash/wrap-squash {:api-host "http://localhost:8081"
                         :api-key "fa2818ce-5480-4aa0-87a9-43a342bf425a"
                         :environment "dev"}))
````

## License

Copyright © 2013 Ragnar Dahlén

Distributed under the Eclipse Public License, the same as Clojure.
