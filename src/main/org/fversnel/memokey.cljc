(ns ^{:author "Frank Versnel"
      :doc "Memoization for functions that use map destructuring"} 
 org.fversnel.memokey)

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
   
   Optionally a :org.fversnel.memokey/memoize-bindings directive can be provided.
   This will override the memoization to use only the bindings provided in the directive."
  [map-destructuring-arg & body]
  (let [memoize-bindings (or (::memoize-bindings map-destructuring-arg)
                             (map-destructuring-arg->bindings map-destructuring-arg))
        map-destructuring-arg (dissoc map-destructuring-arg ::memoize-bindings)]
    `(let [mem# (atom {})]
       (fn [~map-destructuring-arg]
         (let [cache-key# ~memoize-bindings]
           (if-let [e# (find (deref mem#) cache-key#)]
             (val e#)
             (let [ret# (do ~@body)]
               (swap! mem# assoc cache-key# ret#)
               ret#)))))))

(comment

  (require :reload '[org.fversnel.memokey :as m])

  (m/map-destructuring-arg->bindings '{:a/keys [b] :keys [:b/c] e :d/e :strs [some-string] :syms [some-symbol]})

  (macroexpand
   '(m/memo-fn
     {:a/keys [b c]}
     (println "sleeping...")
     (Thread/sleep 5000)
     (identity b)))

  (def memo-example (m/memo-fn
                     {:a/keys [b]}
                     (println "sleeping...")
                     (Thread/sleep 5000)
                     (identity b)))

  (def memo-example2 (m/memo-fn
                     {:a/keys [b c]
                      :org.fversnel.memokey/memoize-bindings [c]}
                     (println "sleeping...")
                     (Thread/sleep 5000)
                     [b c]))

  ;; end
  )