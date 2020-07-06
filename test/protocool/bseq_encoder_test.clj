(ns seralbdev.protocool.core-test
  (:require [clojure.test :as t]
            [seralbdev.protocool.base :as b]
            [seralbdev.protocool.bseq :as d]
            [seralbdev.protocool.bseq_encoder :as enc]))

(t/deftest process-pstr
  (let [bs (b/create)
        pseq [["F1" ::d/pstr {::d/pfx ::d/i16}]]
        data ["HiWorld!"]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [0 8 72 105 87 111 114 108 100 33]) true)))

(t/deftest process-str
  (let [bs (b/create)
        pseq [["F1" ::d/str {::d/len 8}]]
        data ["HiWorld!xx"]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [72 105 87 111 114 108 100 33]) true)))

(t/deftest process-strs
  (let [bs (b/create)
        pseq [["F1" ::d/str {::d/rank 3  ::d/len 1}]]
        data [["a" "b" "c"]]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [97 98 99]) true)))

(t/deftest process-strs-dyn
  (let [bs (b/create)
        pseq [["F1" ::d/str {::d/rank ::d/i8  ::d/len 1}]]
        data [["a" "b" "c"]]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [3 97 98 99]) true)))


(t/deftest process-i8
  (let [bs (b/create)
        pseq [["F1" ::d/i8]]
        data [69]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [69]) true)))

(t/deftest process-i8s
  (let [bs (b/create)
        pseq [["F1" ::d/i8 {::d/rank 3}]]
        data [[69 70 71]]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [69 70 71]) true)))

(t/deftest process-i16
  (let [bs (b/create)
        pseq [["F1" ::d/i16]]
        data [69]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [0 69]) true)))

(t/deftest process-i32
  (let [bs (b/create)
        pseq [["F1" ::d/i32]]
        data [69]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [0 0 0 69]) true)))

(t/deftest process-i64
  (let [bs (b/create)
        pseq [["F1" ::d/i64]]
        data [69]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [0 0 0 0 0 0 0 69]) true)))

(t/deftest process-r32
  (let [bs (b/create)
        pseq [["F1" ::d/r32]]
        data [69.71]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)
        rbs (b/wrap-bytearray data)
        value (b/read-real32! rbs)]
    (t/is (and (> value 69.70) (< value 69.72)))))

(t/deftest process-r64
  (let [bs (b/create)
        pseq [["F1" ::d/r64]]
        data [69.71]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)
        rbs (b/wrap-bytearray data)
        value (b/read-real64! rbs)]
    (t/is (and (> value 69.70) (< value 69.72)))))
