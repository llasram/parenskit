(ns parenskit.core
  (:import [org.grouplens.lenskit.core LenskitConfiguration]
           [org.grouplens.lenskit.core LenskitRecommender]
           [parenskit ParenskitItemScorer]))

(defn item-scorer
  "Return LensKit ItemScore implemented by function `f`.  Function `f` should
accept a user ID and a mutable vector, and should mutate the vector to contain
the item scores for the user."
  [f] (ParenskitItemScorer. f))

(defn config
  "Return new Lenskit configuration."
  {:tag `LenskitConfiguration}
  [] (LenskitConfiguration.))

(defn rec-build
  "Build Lenskit recommender from configuration `config`."
  {:tag `LenskitRecommender}
  [config] (LenskitRecommender/build config))

(defmacro rec-get
  "Get instance of injected implementation of class `cls` from configured
recommender `rec`, with optional qualifier annotation `ann`."
  ([rec cls]
     (let [rec (vary-meta rec assoc :tag `LenskitRecommender)
           cls (vary-meta cls assoc :tag `Class)]
       (vary-meta `(.get ~rec ~cls) assoc :tag cls)))
  ([rec ann cls]
     (let [rec (vary-meta rec assoc :tag `LenskitRecommender)
           ann (vary-meta ann assoc :tag `Class)
           cls (vary-meta cls assoc :tag `Class)]
       (vary-meta `(.get ~rec ~ann ~cls) assoc :tag cls))))
