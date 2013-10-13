(ns parenskit.vector
  (:refer-clojure :exclude [vec key keys val vals])
  (:require [clojure.core :as cc]
            [clojure.core.reducers :as r]
            [clojure.core.protocols :as ccp]
            [parenskit.util :refer [coerce map-vals arg0 returning]])
  (:import [java.io Writer]
           [java.util Collection Map]
           [clojure.lang Seqable]
           [org.grouplens.lenskit.vectors SparseVector]
           [org.grouplens.lenskit.vectors ImmutableSparseVector]
           [org.grouplens.lenskit.vectors MutableSparseVector]
           [org.grouplens.lenskit.vectors VectorEntry VectorEntry$State]))

(def ^:private entry-states
  {:either VectorEntry$State/EITHER
   :set VectorEntry$State/SET
   :unset VectorEntry$State/UNSET})

(defn ^:private ->EntryState
  {:tag `VectorEntry$State }
  [state] (coerce VectorEntry$State entry-states state))

(defn vec?
  "True iff `v` is a SparseVector instance."
  {:inline (fn [v] `(instance? SparseVector ~v))}
  [v] (instance? SparseVector v))

(defn key
  "Key for vector entry `ve`."
  ^long [^VectorEntry ve] (.getKey ve))

(defn keys
  "Reducible collection of just vector `v` keys, optionally only for entries in
`state`, which should be one of `#{:either :set :unset}` (default `:set`)."
  ([^SparseVector v] (keys :set v))
  ([state ^SparseVector v]
     (case state
       :either (.keyDomain v)
       :set (.keySet v)
       :unset (.unsetKeySet v))))

(defn val
  "Value for vector entry `ve`."
  ^double [^VectorEntry ve]
  (.getValue ve))

(defn vals
  "Reducible collection of just vector `v` values, optionally only for entries
in `state`, which should be one of `#{:either :set :unset}` (default `:set`)."
  ([^SparseVector v] (vals :set v))
  ([state ^SparseVector v]
     (let [state (->EntryState state)]
       (reify
         Seqable (seq [_] (map val v))
         ccp/CollReduce
         (coll-reduce [this f] (ccp/coll-reduce this f (f)))
         (coll-reduce [_ f init]
           (->> (.fast v state) (r/map val) (r/reduce f init)))))))

(defn keyval
  "Key/value pair for vector entry `ve`."
  [^VectorEntry ve] [(.getKey ve) (.getValue ve)])

(defn keyvals
  "Reducible collection of vector `v` key/value pair vectors, optionally only
for entries in `state`, which should be one of `#{:either :set :unset}` (default
`:set`)."
  ([^SparseVector v] (keyvals :set v))
  ([state ^SparseVector v]
     (let [state (->EntryState state)]
       (reify
         Seqable (seq [_] (map keyval v))
         ccp/CollReduce
         (coll-reduce [this f] (ccp/coll-reduce this f (f)))
         (coll-reduce [_ f init]
           (->> (.fast v state) (r/map keyval) (r/reduce f init)))))))

(defn entries
  "Reducible collection of vector `v` VectorEntries, optionally only for entries
in `state`, which should be one of `#{:either :set :unset}` (default `:set`)."
  ([^SparseVector v] (keyvals :set v))
  ([state ^SparseVector v]
     (let [state (->EntryState state)]
       (reify
         Seqable (seq [_] (map keyval v))
         ccp/CollReduce
         (coll-reduce [this f] (ccp/coll-reduce this f (f)))
         (coll-reduce [_ f init]
           (r/reduce f init (.fast v state)))))))

(defn freeze!
  "Return immutable version of mutable vector `mv`, invalidating the original
mutable vector in the process."
  {:tag `ImmutableSparseVector}
  [^MutableSparseVector mv] (.freeze mv))

(defn ^:private dispatch-*vec
  "Dispatch function for {i,m}vec multimethods."
  ([_] :content)
  ([kw _] kw)
  ([kw _ _] kw))

(def mvec nil)
(defmulti mvec
  "Return new mutable sparse vector with either provided content and implied key
domain, or provided explicit key domain and optional initial value."
  {:tag `MutableSparseVector
   :arglists '([content] [:content content]
                 [:domain domain] [:domain domain value])}
  dispatch-*vec)

(def ivec nil)
(defmulti ivec
  "Return new immutable sparse vector with either provided content and implied
key domain, or provided explicit key domain and optional value."
  {:tag `ImmutableSparseVector
   :arglists '([content] [:content content]
               [:domain domain] [:domain domain value])}
  dispatch-*vec)

;; Basic reduce via fast iterator
(extend-type SparseVector
  ccp/CollReduce
  (coll-reduce
    ([v f] (ccp/coll-reduce v f (f)))
    ([v f init] (ccp/coll-reduce (.fast v) f init)))

  ccp/IKVReduce
  (kv-reduce [v f init]
    (reduce (fn [acc ve] (f acc (key ve) (val ve))) init v)))

(defprotocol MutableVectorConstruction
  (-mvec-domain [domain] [domain value])
  (-mvec-content [content]))

(defmethod mvec :domain
  ([_ domain] (-mvec-domain domain))
  ([_ domain value] (-mvec-domain domain value)))

(defmethod mvec :content
  ([content] (-mvec-content content))
  ([_ content] (-mvec-content content)))

(extend-protocol MutableVectorConstruction
  Collection
  (-mvec-domain
    ([domain] (MutableSparseVector/create domain))
    ([domain value] (MutableSparseVector/create domain value)))
  (-mvec-content [content]
    (doto (mvec :domain (-> content count range))
      (as-> v (loop [i (long 0), content (seq content)]
                (when content
                  (.set ^MutableSparseVector v i (-> content first double))
                  (recur (inc i) (next content)))))))

  Map
  (-mvec-domain
    ([domain] (mvec :domain (cc/keys domain)))
    ([domain value] (mvec :domain (cc/keys domain) value)))
  (-mvec-content [content] (MutableSparseVector/create content))

  SparseVector
  (-mvec-domain
    ([domain] (mvec :domain (keys domain)))
    ([domain value] (mvec :domain (keys domain) value)))
  (-mvec-content [content] (.mutableCopy content)))

(defprotocol ImmutableVectorConstruction
  (-ivec-content [content]))

(defmethod ivec :domain
  ([_ domain] (freeze! (mvec :domain domain)))
  ([_ domain value] (freeze! (mvec :domain value))))

(defmethod ivec :content
  ([content] (-ivec-content content))
  ([_ content] (-ivec-content content)))

(extend-protocol ImmutableVectorConstruction
  Collection (-ivec-content [content] (ivec (mvec content)))
  Map (-ivec-content [content] (ImmutableSparseVector/create content))
  SparseVector (-ivec-content [content] (.immutable content)))

(defn ^:private write-vector
  "Write `read`able form of vector `v` to writer `w`."
  [^String kind ^Writer w ^SparseVector v]
  (.write w "#parenskit/")
  (.write w kind)
  (.write w " {")
  (reduce (fn [first? ve]
            (when-not first?
              (.write w ", "))
            (.write w (-> ve key str))
            (.write w " ")
            (.write w (-> ve val str))
            false)
          true v)
  (.write w "}"))

(defmethod print-method MutableSparseVector [v w] (write-vector "mvec" w v))
(defmethod print-dup MutableSparseVector [v w] (write-vector "mvec" w v))
(defmethod print-method ImmutableSparseVector [v w] (write-vector "ivec" w v))
(defmethod print-dup ImmutableSparseVector [v w] (write-vector "ivec" w v))

(defn +!
  "Modify mutable vector `v` to add `o` (either a single value or vector with
overlapping domain) to all values in `v`."
  [^MutableSparseVector v o]
  (returning v
    (if (vec? o)
      (.add v ^SparseVector o)
      (.add v (double o)))))

(defn -!
  "Modify mutable vector `v` to subtract `o` (either a single value or vector
with overlapping domain) from all values in `v`."
  [^MutableSparseVector v o]
  (returning v
    (if (vec? o)
      (.subtract v ^SparseVector o)
      (.add v (-> o double -)))))

(defn fill!
  "Modify mutable vector `v` to set all values in the key domain to `x`."
  [^MutableSparseVector v x] (returning v (.fill v (double x))))

(defn map-ve!
  "Modify mutable vector `v` to contain result of applying `f` to each vector
entry for entries in `state` (`:set` by default).  Returns the modified vector."
  ([f ^MutableSparseVector v] (map-ve! :set f v))
  ([state f ^MutableSparseVector v]
     (returning v
       (r/reduce (fn [_ ^VectorEntry ve]
                   (.set v ve (double (f ve))))
                 nil (entries state v)))))

(defn map!
  "Modify mutable vector `v` to contain result of applying `f` to each value for
entries in `state` (`:set` by default).  Returns the modified vector."
  ([f v] (map! :set f v))
  ([state f v] (map-ve! state #(f (val %)) v)))

(defn map-kv!
  "Modify mutable vector `v` to contain result of applying `f` to each key and
value for entries in `state` (`:set` by default).  Returns the modified vector."
  ([f v] (map-kv! :set f v))
  ([state f v] (map-ve! state #(f (key %) (val %)) v)))
