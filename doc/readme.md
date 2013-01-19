# clj-squash

Clojure client library for the [Squash](http://www.squash.io/) bug
killer by [Square](http://www.square.com).

## Artifacts

clj-squash is available on [Clojars](http://clojars.org) and the
current version is:

    [clj-squash "0.1.0"]

## Usage

Use the `notifier` function in `clj-squash.api` to create a function
that when applied to an `Exception` will send a notification to a
Squash instance:

    (require '[clj-squash.api :as squash])
    
    (let [notify (notifier {:api-host "http://localhost:8081"
                            :api-key "fa2818ce-5480-4aa0-87a9-43a342bf425a"
                            :environment "dev"})]
      (notify (ex-info "Invalid use of robot" {:robot-id 42})))

### Ring Middleware

A ring middleware for sending notifiactions about unhandled exceptions
is available in the `clj-squash.ring` namespace. 

    (require '[clj-squash.ring :as squash])
    
    (-> handler
        (squash/wrap-squash {:api-host "http://localhost:8081"
                             :api-key "fa2818ce-5480-4aa0-87a9-43a342bf425a"
                             :environment "dev"}))


## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
