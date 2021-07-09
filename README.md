# Memokey

Memoization for functions that use map destructuring for Clojure and Clojurescript.

## Usage

Add to project.clj:

```clojure
{org.fversnel/memokey {:git/url "https://github.com/fversnel/memokey"
                       :sha "6f6e34602e5f014a0228ff7aea7e95bd129c5a39"}}
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
