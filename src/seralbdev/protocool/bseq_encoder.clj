(ns seralbdev.protocool.bseq_encoder
  (:require
   [seralbdev.protocool.base :as b]
   [seralbdev.protocool.bseq :as d]))

(defn- write-padding! [stream padlen]
  (b/write-bytes! stream (byte-array padlen) 0 padlen))

(defn- write-prefix! [stream prefix value]
  (cond
    (= prefix ::d/u8) (b/write-byte! stream value)
    (= prefix ::d/u16) (b/write-int16! stream value)
    (= prefix ::d/u32) (b/write-int32! stream value)))

(defn- write-str! [stream str]
  (let [bdata (.getBytes str)
        len (count str)]
    (b/write-bytes! stream bdata 0 len)))

(defn- process-pfx-str! [stream prefix str]
  (let [len (count str)]
    (write-prefix! stream prefix len)
    (write-str! stream str)))

(defn- process-fixlen-str! [stream maxlen str]
  (let [strlen (count str)
        padlen (if (> maxlen strlen) (- maxlen strlen) 0)]
    (write-str! stream str)
    (write-padding! stream padlen)))

(defn- process-varlen-str! [stream data]
  (write-str! stream data)
  (b/write-byte! stream 0))

(defn- process-str! [stream fmeta data]
  (let [{pfx ::d/pfx len ::d/len} fmeta]
    (cond
      (some? pfx) (process-pfx-str! stream pfx data)
      (some? len) (process-fixlen-str! stream len data)
      :else (process-varlen-str! stream data))))

(defn- process-item! [stream fmeta data f]
  (let [{rank ::d/rank} fmeta]
    (if (nil? rank) (f stream fmeta data) ;;data is a single value
      (let [datalen (count data) ;;data is an array
            pfx (if (keyword? rank) rank nil)
            speclen (if (integer? rank) rank datalen)]
        (write-prefix! stream pfx datalen)
        (if (not= datalen speclen) (throw (Exception. "Array len do not match spec")))
        (run! #(f stream fmeta %) data)))))

(defn- process-bool! [stream fmeta data]
  (let [value (if data 1 0)]
    (b/write-byte! stream value)))

(defn- dispatch-item! [stream item data]
  (let [[_ ftype fmeta] item]
    (cond
      (contains? #{::d/i8 ::d/u8} ftype) (process-item! stream fmeta data #(b/write-byte! %1 %3))
      (contains? #{::d/i16 ::d/u16} ftype) (process-item! stream fmeta data #(b/write-int16! %1 %3))
      (contains? #{::d/i32 ::d/u32} ftype) (process-item! stream fmeta data #(b/write-int32! %1 %3))
      (= ftype ::d/i64) (process-item! stream fmeta data #(b/write-int64! %1 %3))
      (= ftype ::d/r32) (process-item! stream fmeta data #(b/write-real32! %1 %3))
      (= ftype ::d/r64) (process-item! stream fmeta data #(b/write-real64! %1 %3))
      (= ftype ::d/str) (process-item! stream fmeta data process-str!)
      (= ftype ::d/bool) (process-item! stream fmeta data process-bool!))))


(defn get-specdata-pairs [pseq data]
  (loop [pairs '()
         pendingpseq pseq
         pendingdata data]
    (if (empty? pendingpseq) (reverse pairs)
        (let [pseqitem (first pendingpseq)
              [_ type] pseqitem
              dataitem (if (= type ::d/padding) nil (first pendingdata))
              restdata (if (= type ::d/padding) pendingdata (rest pendingdata))]
          (recur (conj pairs pseqitem dataitem) (rest pendingpseq) restdata)))))

(defn write! [stream pseq data]
  (let [pairs (partition 2 (get-specdata-pairs pseq data))]
    (run! #(dispatch-item! stream (first %) (last %)) pairs)))
