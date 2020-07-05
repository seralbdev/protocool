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
  (let [{pfx ::d/pfx} fmeta]
    (write-pstr! stream pfx data)))

(defn- process-str! [stream fmeta data]
  (let [{len ::d/len} fmeta]
    (write-str! stream (subs data 0 len))))

(defn- process-item! [stream fmeta data f]
  (let [{rank ::d/rank} fmeta]
    (if (nil? rank) (f stream fmeta data)
      (run! #(f stream fmeta %) (take rank data)))))

(defn- dispatch-item! [stream item data]
  (let [[_ ftype fmeta] item]
    (cond
      (= ftype ::d/i8) (process-item! stream fmeta data #(b/write-byte! %1 %3))
      (= ftype ::d/i16) (process-item! stream fmeta data #(b/write-int16! %1 %3))
      (= ftype ::d/i32) (process-item! stream fmeta data #(b/write-int32! %1 %3))
      (= ftype ::d/i64) (process-item! stream fmeta data #(b/write-int64! %1 %3))
      (= ftype ::d/r32) (process-item! stream fmeta data #(b/write-real32! %1 %3))
      (= ftype ::d/r64) (process-item! stream fmeta data #(b/write-real64! %1 %3))
      (= ftype ::d/str) (process-item! stream fmeta data process-str!)
      (= ftype ::d/pstr) (process-item! stream fmeta data process-pstr!))))

(defn write! [stream pseq data]
  (let [pairs (partition 2 (interleave pseq data))]
    (run! #(dispatch-item! stream (first %) (last %)) pairs)))
