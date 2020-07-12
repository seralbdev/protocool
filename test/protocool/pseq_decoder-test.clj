(ns protocool.pseq-decoder-test
  (:require [clojure.test :as t]
            [clojure.data :as cd]
            [seralbdev.protocool.base :as b]
            [seralbdev.protocool.pseq :as d]
            [seralbdev.protocool.pseq_decoder :as dc]))

(t/deftest process-i32
  (let [bs (b/create)
        _ (b/write-int32! bs 69)
        pseq [["F1" ::d/i32]]
        data (dc/read! bs pseq)]
    (t/is (= data {"F1" 69}) true)))

(t/deftest process-i32-vector
  (let [bs (b/create)
        _ (b/write-ints32! bs [69 70 71])
        pseq [["F1" ::d/i32 {::d/rank 3}]]
        data (dc/read! bs pseq)]
    (t/is (cd/diff data {"F1" [69 70 71]}) nil)))
