(ns seralbdev.protocool.pseq_decoder-test
  (:require [clojure.test :as t]
            [clojure.data :as cd]
            [seralbdev.protocool.base :as b]
            [seralbdev.protocool.pseq :as d]
            [seralbdev.protocool.pseq_decoder :as dc]
            [seralbdev.protocool.pseq_encoder :as ec]))

(t/deftest process-i32
  (let [bs (b/create)
        _ (b/write-int32! bs 69)
        pseq [["F1" ::d/i32]]
        data (dc/read! bs nil pseq)]
    (t/is (= data {"F1" 69}) true)))

(t/deftest process-i32-vector
  (let [bs (b/create)
        _ (b/write-ints32! bs [69 70 71])
        pseq [["F1" ::d/i32 {::d/rank 3}]]
        data (dc/read! bs nil pseq)]
    (t/is (cd/diff data {"F1" [69 70 71]}) nil)))

(t/deftest process-pseq1
  (let [bs (b/create)
        _ (b/write-byte! bs 1)
        _ (b/write-int16! bs 66)
        pseq [["F1" ::d/pseq {::d/fields [["F11" ::d/bool] ["F12" ::d/i16]]}]]
        data (dc/read! bs nil pseq)
        f11 (get-in data ["F1" "F11"])
        f12 (get-in data ["F1" "F12"])]
    (t/is (and (= f11 true) (= f12 66)))))

(t/deftest process-pseq2
  (let [bs (b/create)
        _ (b/write-byte! bs 1)
        _ (b/write-bytes! bs [0 0 0 0 0 0] 0 6)
        _ (b/write-int16! bs 66)
        pseq [["F1" ::d/pseq {::d/fields [["F11" ::d/bool] ["" ::d/padding {::d/len 6}] ["F12" ::d/i16]]}]]
        data (dc/read! bs nil pseq)
        f11 (get-in data ["F1" "F11"])
        f12 (get-in data ["F1" "F12"])]
    (t/is (and (= f11 true) (= f12 66)))))


(t/deftest process-pseq-vector
  (let [bs (b/create)
        _ (b/write-byte! bs 1)
        _ (b/write-int16! bs 66)
        _ (b/write-byte! bs 0)
        _ (b/write-int16! bs 33)
        pseq [["F1" ::d/pseq {::d/rank 2 ::d/fields [["F11" ::d/bool] ["F12" ::d/i16]]}]]
        data (dc/read! bs nil pseq)
        vdata (get data "F1")
        f11 (get (nth vdata 0) "F11")
        f12 (get (nth vdata 0) "F12")
        f21 (get (nth vdata 1) "F11")
        f22 (get (nth vdata 1) "F12")]
    (t/is (and (= f11 true) (= f12 66) (= f21 false) (= f22 33)))))

(t/deftest process-pseq-vector2
  (let [bs (b/create)
        _ (b/write-byte! bs 2)
        _ (b/write-byte! bs 2)
        _ (b/write-int16! bs 0)

        _ (b/write-int16! bs 11)
        _ (b/write-bytes! bs (.getBytes "thetoken001") 0 11)

        _ (b/write-int16! bs 7)
        _ (b/write-bytes! bs (.getBytes "sensor1") 0 7)

        _ (b/write-int16! bs 2) ;;array count

        _ (b/write-int16! bs 2)
        _ (b/write-bytes! bs (.getBytes "s1") 0 2)
        _ (b/write-real32! bs 111)
        _ (b/write-int64! bs 222)

        _ (b/write-int16! bs 2)
        _ (b/write-bytes! bs (.getBytes "s2") 0 2)
        _ (b/write-real32! bs 333)
        _ (b/write-int64! bs 444)

        pseq [["fid" ::d/u8] 
              ["sid" ::d/u8] 
              ["res" ::d/u16] 
              ["token" ::d/str {::d/pfx ::d/u16}] 
              ["sensor" ::d/str {::d/pfx ::d/u16}] 
              ["samples" ::d/pseq {::d/rank ::d/u16, ::d/fields [["n" ::d/str {::d/pfx ::d/u16}]["v" ::d/r32]["t" ::d/i64]]}]]
        data (dc/read! bs nil pseq)
        samples (get data "samples")
        sample1 (nth samples 0)
        sample2 (nth samples 1)
        sample1-n (get sample1 "n")
        sample2-n (get sample2 "n")]
    (t/is (and (= sample1-n "s1") (= sample2-n "s2")))))


(t/deftest process-psref1
  (let [bs (b/create)
        resolver (fn [_] [["F11" ::d/bool] ["F12" ::d/i16]])
        tpseq [["F1" ::d/str {::d/pfx ::d/u16}] ["F2" ::d/pseq {::d/fields [["F11" ::d/bool] ["F12" ::d/i16]]}]]
        tdata {"F1" "ps1" "F2" {"F11" true "F12" 66}}
        _ (ec/write! bs nil tpseq tdata)
        pseq [["F1" ::d/psref {::d/pfx ::d/u16}]]
        data (dc/read! bs resolver pseq)
        f11 (get-in data ["F1" "F11"])
        f12 (get-in data ["F1" "F12"])]
    (t/is (and (= f11 true) (= f12 66))))) 

(t/deftest process-psref-vector1
  (let [bs (b/create)
        resolver (fn [_] [["F11" ::d/bool] ["F12" ::d/i16]])
        pseq [["F1" ::d/psref {::d/rank ::d/u16 ::d/pfx ::d/u16}]]
        tpseq [["RANK" ::d/u16] ["SEQID" ::d/str {::d/pfx ::d/u16}] ["F11" ::d/bool] ["F12" ::d/i16] ["F21" ::d/bool] ["F22" ::d/i16]]
        tdata {"RANK" 2 "SEQID" "_" "F11" true "F12" 33 "F21" false "F22" 66}
        _ (ec/write! bs nil tpseq tdata)
        wdata (b/seal! bs)
        rbs (b/wrap-bytearray wdata)
        rdata (dc/read! rbs resolver pseq)
        f1 (get rdata "F1")
        item1 (nth f1 0)
        item2 (nth f1 1)
        f11 (get item1 "F11")
        f12 (get item1 "F12")
        f21 (get item2 "F11")
        f22 (get item2 "F12")]
    (t/is (and (= f11 true) (= f12 33)))
    (t/is (and (= f21 false) (= f22 66)))))
