(ns parenskit.util)

(defn coerce
  "Coerce `x` to be of class `c` by applying `f` to it iff `x` isn't
already an instance of `c`."
  [c f x] (if (instance? c x) x (f x)))

(defn map-vals
  "Return a new map made by mapping `f` over the values of `m`."
  [f m] (into {} (map (fn [[k v]] [k (f v)]) m)))

(defn arg0
  "Accepts any number of arguments and returns the first."
  ([x] x)
  ([x y] x)
  ([x y z] x)
  ([x y z & more] x))

(defmacro returning
  "Evaluates the result of `expr`, then evaluates the forms in
`body` (presumably for side-effects), then returns the result of
`expr`.  AKA `prog1`."
  [expr & body] `(let [value# ~expr] ~@body value#))
