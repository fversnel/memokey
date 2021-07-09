# Memokey

Memoization for functions that use map destructuring for Clojure and Clojurescript.

## Usage

Add to deps.edn:

```clojure
{org.fversnel/memokey {:git/url "https://github.com/fversnel/memokey"
                       :sha "905a08df1a29f3e509a656e9ac8250d2b8a61309"}}
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
