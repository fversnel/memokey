# Memokey

Memoization for functions that use map destructuring for Clojure and Clojurescript.

## The map is Clojure's best datastructure

Maps provide us with unordered and associative data.
It's arguably the only data structure that we need to write
function parameters. They compose terrifically and are extensible (using `merge` and namespaced keys) Ordered parameters are (often) just unjustified added complexity:

```clojure
(defn wrong [a b])

(defn right [{:keys [a b]}])
```

Clojure's `memoize` only supports memoizing on the entire map being passed instead of just the keys that are being used.
Memokey fixes this. Now you can use the power of maps combined with the performance benefits of memoization.

## Usage

Add to deps.edn:

```clojure
{org.fversnel/memokey {:git/url "https://github.com/fversnel/memokey"
                       :sha "53081bc453b9950d5b00c55e3a27f5ea830263b3"}}
```

Require the namespace:

```clojure
(:require [org.fversnel.memokey :as m])
```

Then write a function using the `memo-fn` macro:

```clojure
(def example-fn
  (m/memo-fn
    ;; map destructuring argument
    {:a/keys [b]}
    ;; function body
    (println "executing slow function")
    (Thread/sleep 5000)
    (identity b)))
```

When we call the memoized function it will only look at the elements
of the map that are actually being destructured.
When we call the function twice with the same value
for `:a/b` regardless of the value of `:b/c` we get the memoized
result back:


```clojure
(example-fn {:a/b 42 :b/c 43}) ;; output: "executing slow function"; 42
(example-fn {:a/b 42 :b/c 44}) ;; output: 42

```

Optionally we can provide a directive to memoize on a subset of the bindings:


```clojure
(m/memo-fn
  {:a/keys [b c]
  ;; Memoizes only on binding c
  :org.fversnel.memokey/memoize-bindings [c]}
  (println "executing slow function")
  (Thread/sleep 5000)
  [b c])
```

And provide a custom implementation of the org.fversnel.memokey.Cache protocol:

*(by default memokey uses an `(atom {})` as its cache)*

```clojure
(m/memo-fn
  {:a/keys [b]
  ;; Custom cache:
  :org.fversnel.memokey/cache (create-custom-cache)}
  (println "executing slow function")
  (Thread/sleep 5000)
  (identity b))
```

It works on all kinds of map destructuring:

```clojure
{;; regular non-namespaced keys
 :keys [a]
 ;; namespaced keys
 :keys [:b/c]
 ;; namespaced :a/keys
 :a/keys [b]
 ;; regular destructuring
 e :d/e
 ;; string based destructuring
 :strs [some-string]
 ;; symbol based destructuring
 :syms [some-symbol]}
```

Each memoized function contains the following meta data:

```clojure
(m/memo-fn {:keys [a]} a)

=> #:org.fversnel.memokey{:memoize-bindings [a],
                          :cache #object[org.fversnel.memokey$atom_cache$reify__470 0x144ab54 "org.fversnel.memokey$atom_cache$reify__470@144ab54"]}
```

To turn off memoization all together (without any overhead):

```clojure
(m/memo-fn {:keys [a] :org.fversnel.memokey/memoize? false} a)
```

## TODO

- Provide different types of caches (supporting `clojure.core.cache`)