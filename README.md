# Memokey

Memoization for functions that use map destructuring for Clojure and Clojurescript.

## Usage

Add to project.clj:

```clojure
{org.fversnel/memokey {:git/url "https://github.com/fversnel/memokey"
                       :sha "f87c37871d34e7a8e317b9dd0879a130772f1fa8"}
```

Require the namespace:

```clojure
(:require [org.fversnel.memokey :as m])
```

Then write a function using the `memo-fn` macro:

```clojure
(m/memo-fn
  ;; map destructuring argument
  {:a/keys [b]}
  ;; function body
  (Thread/sleep 5000)
  (identity b))
```

Optionally provide a directive to memoize on a subset of the bindings:


```clojure
(m/memo-fn
  {:a/keys [b c]
  ;; Memoizes only on binding c
  :org.fversnel.memokey/memoize-bindings [c]}
  (Thread/sleep 5000)
  [b c])
```
