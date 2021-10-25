(ns ^{:author "Frank Versnel"
      :doc "Memoization for functions that use map destructuring"}
 org.fversnel.memokey)

(defprotocol Cache
  (get-value [this key])
  (put-value! [this key value])
  (underlying [this]))

(defrecord NoOpCache []
  Cache
  (get-value [_ _] nil)
  (put-value! [_ _ _])
  (underlying [this] this))

(def no-op-cache (NoOpCache.))

(defn atom-cache []
  (let [cache (atom {})]
    (reify Cache
      (get-value [_ key]
        (get @cache key))
      (put-value! [_ key value]
        (swap! cache assoc key value))
      (underlying [_]
        cache))))

(defn map-destructuring-arg->bindings [map-destructuring-arg]
  (into
   []
   (mapcat
    (fn [[left right]]
      (if (#{"keys" "strs" "syms"} (name left))
        (map (comp symbol name) right)

        [left])))
   map-destructuring-arg))

(defmacro memo-fn
  "Memoizes the given function on the keys in the destructured map.
   This is useful if you want to memoize a function based on a subset of a map.
   
   Optionally:
   - a :org.fversnel.memokey/memoize-bindings directive will override the memoization 
   to use only the bindings provided in the directive.
   - a :org.fversnel.memokey/memoize? directive with the value `false` will disable
   memoization alltogether
   - a :org.fversnel.memokey/cache directive will provide the function with a custom
   cache implementation. By default `atom-cache` is used."
  [map-destructuring-arg & body]
  (let [custom-memoize-bindings (::memoize-bindings map-destructuring-arg)
        cache (get map-destructuring-arg ::cache `(atom-cache))
        memoize? (get map-destructuring-arg ::memoize? true)

        map-destructuring-arg (dissoc map-destructuring-arg ::memoize? ::memoize-bindings ::cache)

        memoize-bindings (or custom-memoize-bindings
                             (map-destructuring-arg->bindings map-destructuring-arg))]
    (if memoize?
      `(let [cache# ~cache]
         (with-meta
           (fn [~map-destructuring-arg]
                            ;; optimize if there is only one binding
                            ;; we don't need to wrap the binding in a vector     
             (let [cache-key# ~(if (= (count memoize-bindings) 1)
                                 (first memoize-bindings)
                                 memoize-bindings)]
               (if-let [e# (get-value cache# cache-key#)]
                 e#
                 (let [ret# (do ~@body)]
                   (put-value! cache# cache-key# ret#)
                   ret#))))
           {::cache cache#
            ::memoize-bindings '~memoize-bindings}))

      ;; else
      `(fn [~map-destructuring-arg] ~@body))))

(comment

  (require :reload '[org.fversnel.memokey :as m])

  (m/map-destructuring-arg->bindings '{:a/keys [b] :keys [:b/c] e :d/e :strs [some-string] :syms [some-symbol]})

  (macroexpand
   '(m/memo-fn
     {:a/keys [b c]}
     (println "sleeping...")
     (Thread/sleep 5000)
     (identity b)))

  (m/memo-fn {:keys [a]} a)

  (def memo-example (m/memo-fn
                     {:a/keys [b]}
                     (println "sleeping...")
                     (Thread/sleep 5000)
                     (identity b)))
  (memo-example {:a/b 42 :a/c 43})
  (memo-example {:a/b 42 :a/c 44})

  (def memo-example2 (m/memo-fn
                      {:a/keys [b c]
                       :org.fversnel.memokey/memoize-bindings [c]}
                      (println "sleeping...")
                      (Thread/sleep 5000)
                      [b c]))
  (memo-example2 {:a/b 42 :a/c 44})
  (memo-example2 {:a/b 43 :a/c 44})

  (def memo-example3 (m/memo-fn
                      {:a/keys [b]
                       :org.fversnel.memokey/cache (m/atom-cache)}
                      (println "sleeping...")
                      (Thread/sleep 5000)
                      (identity b)))
  (memo-example3 {:a/b 42 :a/c 43})
  (memo-example3 {:a/b 42 :a/c 44})


  ;; end
  )