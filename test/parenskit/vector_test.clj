(ns parenskit.vector-test
  (:require [clojure.test :refer :all]
            [parenskit.vector :as lkv :refer [+! -!]]))

(deftest test-construction
  (testing "mutable vectors"
    (is (= #{}
           (->> (lkv/mvec :domain (range 10))
                (lkv/keys :set))))
    (is (= (set (range 10))
           (->> (lkv/mvec :domain (range 10))
                (lkv/keys :either))))
    (is (= '()
           (->> (lkv/mvec :domain (range 10))
                (lkv/vals :set)
                seq)))
    (is (= (repeat 10 1.0)
           (->> (lkv/mvec :domain (range 10) 1.0)
                (lkv/vals :set)
                seq)))
    (is (= (set (range 10))
           (->> (lkv/mvec (range 9 -1 -1))
                (lkv/keys :either))))
    (is (= (set (range 10))
           (->> (lkv/mvec :content (range 9 -1 -1))
                (lkv/keys :either))))
    (is (= (range 9.0 -1.0 -1)
           (->> (lkv/mvec :content (range 9 -1 -1))
                (lkv/vals :either)
                seq))))
  (testing "immutable vectors"
    (is (= #{}
           (->> (lkv/ivec :domain (range 10))
                (lkv/keys :set))))
    (is (= #{}
           (->> (lkv/ivec :domain (range 10))
                (lkv/keys :either)))
        "Immutable vectors' key domains do not include unset keys.")
    (is (= '()
           (->> (lkv/ivec :domain (range 10))
                (lkv/vals :set)
                seq)))
    (is (= (repeat 10 1.0)
           (->> (lkv/ivec :domain (range 10) 1.0)
                (lkv/vals :set)
                seq)))
    (is (= (set (range 10))
           (->> (lkv/ivec (range 9 -1 -1))
                (lkv/keys :either))))
    (is (= (range 9.0 -1.0 -1)
           (->> (lkv/ivec :content (range 9 -1 -1))
                (lkv/vals :either)
                seq)))))

(deftest test-reading
  (is (= (lkv/mvec :domain [1 2] 1.0)
         #parenskit/mvec {1 1.0, 2 1.0}))
  (is (= (lkv/ivec :domain [1 2] 1.0)
         #parenskit/ivec {1 1.0, 2 1.0})))

(deftest test-mutation
  (is (= (lkv/mvec {0 1.0, 1 2.0, 2 3.0}))
      (->> (lkv/mvec :domain (range 3))
           (lkv/map-kv! :either (fn [k _] (inc k)))))
  (is (= (lkv/mvec {0 1.0, 1 2.0, 2 3.0}))
      (->> (lkv/mvec (range 3))
           (lkv/map! inc))))

(deftest test-arithmetic
  (is (= (lkv/mvec [0 3 5])
         (+! (lkv/mvec [0 1 2]) (lkv/ivec [0 2 3]))))
  (is (= (lkv/mvec [0 3 5])
         (-! (lkv/mvec [0 5 8]) (lkv/ivec [0 2 3]))))
  (is (= (lkv/mvec [2 2 2])
         (+! (lkv/mvec [1 1 1]) 1)))
  (is (= (lkv/mvec [2 2 2])
         (-! (lkv/mvec [3 3 3]) 1))))
