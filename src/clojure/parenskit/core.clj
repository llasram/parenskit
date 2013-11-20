(ns parenskit.core
  (:import [org.grouplens.lenskit.core LenskitConfiguration]
           [org.grouplens.lenskit.core LenskitRecommender]
           [parenskit ParenskitGlobalItemScorer ParenskitItemScorer]))

(defn item-scorer
  "Return LensKit ItemScorer implemented by function `f`.  Function `f` should
accept a user ID and a mutable vector, and should mutate the vector to contain
the item scores for the user."
  [f] (ParenskitItemScorer. f))

(defn global-item-scorer
  "Return LensKit GlobalItemScore implemented by function `f`.  Function `f`
should accept a collection of item IDs and a mutable output vector, and should
mutate the vector to contain the scores for the items in the domain of the
output vector."
  [f] (ParenskitGlobalItemScorer. f))

(defn config
  "Return new Lenskit configuration."
  {:tag `LenskitConfiguration}
  [] (LenskitConfiguration.))

(defn rec-build
  "Build Lenskit recommender from configuration `config`."
  {:tag `LenskitRecommender}
  [config] (LenskitRecommender/build config))

(defn ^:private rec-get*
  "Type-hinted form for LenskitRecommender#get invocation."
  ([rec cls]
     (let [rec (vary-meta rec assoc :tag `LenskitRecommender)
           cls (vary-meta cls assoc :tag `Class)]
       (vary-meta `(.get ~rec ~cls) assoc :tag cls)))
  ([rec ann cls]
     (let [rec (vary-meta rec assoc :tag `LenskitRecommender)
           ann (vary-meta ann assoc :tag `Class)
           cls (vary-meta cls assoc :tag `Class)]
       (vary-meta `(.get ~rec ~ann ~cls) assoc :tag cls))))

(defn rec-get
  "Get instance of injected implementation of class `cls` from configured
recommender `rec`, with optional qualifier annotation `ann`."
  {:inline (identity rec-get*), :inline-arities #{2 3}}
  ([^LenskitRecommender rec ^Class cls] (.get rec cls))
  ([^LenskitRecommender rec ^Class ann ^Class cls] (.get rec ann cls)))
