(ns seralbdev.protocool.pseq_decoder-test
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

(t/deftest process-struct
  (let [bs (b/create)
        _ (b/write-byte! bs 1)
        _ (b/write-int16! bs 66)
        pseq [["F1" ::d/struct {::d/id "STRUCT1" ::d/fields [["F11" ::d/bool] ["F12" ::d/i16]]}]]
        data (dc/read! bs pseq)
        f11 (get-in data ["F1" "F11"])
        f12 (get-in data ["F1" "F12"])]
    (t/is (and (= f11 true) (= f12 66)))))

(t/deftest process-struct2
  (let [bs (b/create)
        _ (b/write-byte! bs 1)
        _ (b/write-bytes! bs [0 0 0 0 0 0] 0 6)
        _ (b/write-int16! bs 66)
        pseq [["F1" ::d/struct {::d/id "STRUCT1" ::d/fields [["F11" ::d/bool] ["" ::d/padding {::d/len 6}] ["F12" ::d/i16]]}]]
        data (dc/read! bs pseq)
        f11 (get-in data ["F1" "F11"])
        f12 (get-in data ["F1" "F12"])]
    (t/is (and (= f11 true) (= f12 66)))))


(t/deftest process-struct-vector
  (let [bs (b/create)
        _ (b/write-byte! bs 1)
        _ (b/write-int16! bs 66)
        _ (b/write-byte! bs 0)
        _ (b/write-int16! bs 33)
        pseq [["F1" ::d/struct {::d/rank 2 ::d/id "STRUCT1" ::d/fields [["F11" ::d/bool] ["F12" ::d/i16]]}]]
        data (dc/read! bs pseq)
        vdata (get data "F1")
        f11 (get (nth vdata 0) "F11")
        f12 (get (nth vdata 0) "F12")
        f21 (get (nth vdata 1) "F11")
        f22 (get (nth vdata 1) "F12")]
    (t/is (and (= f11 true) (= f12 66) (= f21 false) (= f22 33)))))
