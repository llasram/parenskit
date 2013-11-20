(defproject org.platypope/parenskit "0.1.2-SNAPSHOT"
  :description "Clojure integration library for the LensKit framework."
  :url "http://github.com/llasram/parenskit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :global-vars {*warn-on-reflection* true}
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.6" "-source" "1.6"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.grouplens.lenskit/lenskit-core "2.0.4"]
                 [org.platypope/esfj "0.2.1"]]
  :profiles {:default [:base :system :user :provided :java6 :dev]
             :provided {:dependencies
                        [[com.google.code.findbugs/annotations "2.0.2"]
                         [org.grouplens.lenskit/lenskit-eval "2.0.4"]]}
             :java6 {:dependencies
                     [[org.codehaus.jsr166-mirror/jsr166y "1.7.0"]]}})
