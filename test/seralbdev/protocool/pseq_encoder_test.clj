(ns seralbdev.protocool.pseq_encoder-test
  (:require [clojure.test :as t]
            [seralbdev.protocool.base :as b]
            [seralbdev.protocool.pseq :as d]
            [seralbdev.protocool.pseq_encoder :as enc]))

(t/deftest process-varlen-str
  (let [bs (b/create)
        pseq [["F1" ::d/str]]
        data {"F1" "HiWorld!"}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [72 105 87 111 114 108 100 33 0]) true)))

(t/deftest process-prefix-str
  (let [bs (b/create)
        pseq [["F1" ::d/str {::d/pfx ::d/u16}]]
        data {"F1" "HiWorld!"}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [0 8 72 105 87 111 114 108 100 33]) true)))

(t/deftest process-fixlen-str
  (let [bs (b/create)
        pseq [["F1" ::d/str {::d/len 10}]]
        data {"F1" "HiWorld!"}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [72 105 87 111 114 108 100 33 0 0]) true)))

(t/deftest process-i8
  (let [bs (b/create)
        pseq [["F1" ::d/i8]]
        data {"F1" 69}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [69]) true)))

(t/deftest process-i8s
  (let [bs (b/create)
        pseq [["F1" ::d/i8 {::d/rank 3}]]
        data {"F1" [69 70 71]}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [69 70 71]) true)))

(t/deftest process-i16
  (let [bs (b/create)
        pseq [["F1" ::d/i16]]
        data {"F1" 69}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [0 69]) true)))

(t/deftest process-i16-array
  (let [bs (b/create)
        pseq [["F1" ::d/i16 {::d/rank 3}]]
        data {"F1" [69 70 71]}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [0 69 0 70 0 71]) true)))

(t/deftest process-i32
  (let [bs (b/create)
        pseq [["F1" ::d/i32]]
        data {"F1" 69}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [0 0 0 69]) true)))

(t/deftest process-i64
  (let [bs (b/create)
        pseq [["F1" ::d/i64]]
        data {"F1" 69}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [0 0 0 0 0 0 0 69]) true)))

(t/deftest process-r32
  (let [bs (b/create)
        pseq [["F1" ::d/r32]]
        data {"F1" 69.71}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)
        rbs (b/wrap-bytearray data)
        value (b/read-real32! rbs)]
    (t/is (and (> value 69.70) (< value 69.72)))))

(t/deftest process-r64
  (let [bs (b/create)
        pseq [["F1" ::d/r64]]
        data {"F1" 69.71}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)
        rbs (b/wrap-bytearray data)
        value (b/read-real64! rbs)]
    (t/is (and (> value 69.70) (< value 69.72)))))

(t/deftest process-bool
  (let [bs (b/create)
        pseq [["F1" ::d/bool]]
        data {"F1" true}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)
        rbs (b/wrap-bytearray data)
        value (b/read-byte! rbs)]
    (t/is (= value 1))))

(t/deftest process-struct
  (let [bs (b/create)
        pseq [["F1" ::d/struct {::d/id "STRUCT1" ::d/fields [["F11" ::d/bool] ["F12" ::d/i16]]}]]
        data {"F1" {"F11" true "F12" 33}}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)
        rbs (b/wrap-bytearray data)
        f11 (b/read-byte! rbs)
        f12 (b/read-int16! rbs)]
    (t/is (and (= f11 1) (= f12 33)))))

(t/deftest process-struct-vector
  (let [bs (b/create)
        pseq [["F1" ::d/struct {::d/id "STRUCT1" ::d/rank 2 ::d/fields [["F11" ::d/bool] ["F12" ::d/i16]]}]]
        data {"F1" [{"F11" true "F12" 33}{"F11" false "F12" 34}]}
        _ (enc/write! bs pseq data)
        data (b/seal! bs)
        rbs (b/wrap-bytearray data)
        f11-1 (b/read-byte! rbs)
        f12-1 (b/read-int16! rbs)
        f11-2 (b/read-byte! rbs)
        f12-2 (b/read-int16! rbs)]
    (t/is (and (= f11-1 1) (= f12-1 33) (= f11-2 0) (= f12-2 34)))))
