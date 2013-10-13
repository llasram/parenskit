(defproject org.platypope/parenskit "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :global-vars {*warn-on-reflection* true}
  :source-paths ["src/clojure"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.grouplens.lenskit/lenskit-core "2.0.2"]
                 [org.platypope/esfj "0.2.0"]]
  :profiles {:java6
             {:dependencies
              [[org.codehaus.jsr166-mirror/jsr166y "1.7.0"]]}})
