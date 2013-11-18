(ns parenskit.eval
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.util Properties]
           [clojure.lang RT]
           [org.grouplens.lenskit.eval.script EvalScriptEngine]))

(defn ^:private kv-split
  [arg]
  (let [arg (if-not (.startsWith ^String arg "-D") arg (subs arg 2))]
    (str/split arg #"=")))

(defn -main
  "Basic `lenskit-eval`-like command-line entry point.  The `args` should
consist of a Groovy evaluation script path followed by any number of properties
as `=`-separated key-value pairs, each optionally prefixed with `-D`."
  [& args]
  (let [[script & args] args
        props (doto (Properties.)
                (.putAll (System/getProperties))
                (as-> props (doseq [arg args, :let [[key val] (kv-split arg)]]
                              (.setProperty props key val))))
        ese (EvalScriptEngine. (RT/makeClassLoader) props)]
    (.loadProject ese (io/file script))))
