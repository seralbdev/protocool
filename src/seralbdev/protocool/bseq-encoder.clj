(ns seralbdev.protocool.bseq-encoder
  (:require
   [seralbdev.protocool.base :as b]
   [seralbdev.protocool.bseq :as d]))

(defn- write-str! [stream value]
  (let [bdata (.getBytes value)
        len (count bdata)]
    (b/write-bytes! stream bdata 0 len)))


(defn- write-pstr! [stream pfx value]
  (let [len (count value)]
    (cond
      (= pfx ::d/i8) (b/write-byte! stream len)
      (= pfx ::d/i16) (b/write-int16! stream len)
      (= pfx ::d/i32) (b/write-int32! stream len))
    (write-str! stream value)))
   

(defn- process-pstr [stream rank pfx data]
  (if (nil? rank) (write-pstr! stream pfx data)
      (run! #(write-pstr! stream pfx %) (take rank data))))
