(ns seralbdev.protocool.core-test
  (:require [clojure.test :as t]
            [seralbdev.protocool.base :as b]
            [seralbdev.protocool.bseq :as d]
            [seralbdev.protocool.bseq_encoder :as enc]))

(t/deftest process-pstr1
  (let [bs (b/create)
        pseq [["F1" ::d/pstr {::d/pfx ::d/i16}]]
        data ["HiWorld!"]
        _ (enc/write! bs pseq data)
        data (b/seal! bs)]
    (t/is (= (seq data) [0 8 72 105 87 111 114 108 100 33]) true)))

