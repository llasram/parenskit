(ns parenskit.core
  (:import [parenskit ParenskitItemScorer]))

(defn item-scorer
  "Return LensKit ItemScore implemented by function `f`.  Function `f`
should accept a user ID and a mutable vector, and should mutate the
vector to contain the item scores for the user."
  [f] (ParenskitItemScorer. f))
