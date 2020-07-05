(ns seralbdev.protocool.bseq_encoder
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

(defn- process-pstr! [stream fmeta data]
  (let [{rank ::d/rank pfx ::d/pfx} fmeta]
      (if (nil? rank) (write-pstr! stream pfx data)
        (run! #(write-pstr! stream pfx %) (take rank data)))))

(defn- process-str! [stream fmeta data]
  (let [{rank ::d/rank len ::d/len} fmeta]
    (if (nil? rank) (write-str! stream (subs data 0 len))
      (run! #(write-str! stream (subs % 0 len)) (take rank data)))))

(defn- process-item! [stream item data]
  (let [[_ ftype fmeta] item]
    (cond
      (= ftype ::d/str) (process-str! stream fmeta data)
      (= ftype ::d/pstr) (process-pstr! stream fmeta data))))

(defn write! [stream pseq data]
  (let [pairs (partition 2 (interleave pseq data))]
    (run! #(process-item! stream (first %) (last %)) pairs)))
