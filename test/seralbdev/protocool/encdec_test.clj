(ns seralbdev.protocool.encdec-test
  (:require [clojure.test :as t]
            [seralbdev.protocool.base :as b]
            [seralbdev.protocool.pseq :as d]
            [seralbdev.protocool.pseq_encoder :as enc]
            [seralbdev.protocool.pseq_decoder :as dec]))


(comment
  (let [bs (b/create)
        resolver (fn [_] [["F1" :seralbdev.protocool.pseq/r32]
                          ["F2" :seralbdev.protocool.pseq/u8]
                          [""
                           :seralbdev.protocool.pseq/padding
                           #:seralbdev.protocool.pseq{:len 3}]])
        pseq [["vals" ::d/psref {::d/pfx ::d/u16}]]
        data {"vals" ["UDT6" {"F1" 2.3 "F2" 3}]}
        _ (enc/write! bs resolver pseq data)
        data (b/seal! bs)
        rbs (b/wrap-bytearray data)
        data2 (dec/read! rbs resolver pseq)]
    (def ed data)
    (def dd data2)
    ))
