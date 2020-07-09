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

(defn- process-bool! [stream _ data]
  (let [value (if data 1 0)]
    (b/write-byte! stream value)))

(defn- process-padding! [stream fmeta]
  (let [padlen (:len fmeta)]
    (write-padding! stream padlen)))

(defn- dispatch-single! [stream ftype fmeta value]
  (cond
    (contains? #{::d/i8 ::d/u8} ftype) (b/write-byte! stream value)
    (contains? #{::d/i16 ::d/u16} ftype) (b/write-int16! stream value)
    (contains? #{::d/i32 ::d/u32} ftype) (b/write-int32! stream value)
    (= ftype ::d/i64) (b/write-int64! stream value)
    (= ftype ::d/r32) (b/write-real32! stream value)
    (= ftype ::d/r64) (b/write-real64! stream value)
    (= ftype ::d/str) (process-str! stream fmeta value)
    (= ftype ::d/bool) (process-bool! stream nil value)
    (= ftype ::d/padding) (process-padding! stream fmeta)))

(defn- dispatch-vector! [stream ftype fmeta value]
  (cond
    (contains? #{::d/i8 ::d/u8} ftype) (b/write-bytes! stream value 0 (count value))
    (contains? #{::d/i16 ::d/u16} ftype) (b/write-ints16! stream value)
    (contains? #{::d/i32 ::d/u32} ftype) (b/write-ints32! stream value)
    (= ftype ::d/i64) (b/write-ints64! stream value)
    (= ftype ::d/r32) (b/write-reals32! stream value)
    (= ftype ::d/r64) (b/write-reals64! stream)
    (= ftype ::d/str) (run! #(process-str! stream fmeta %) value)
    (= ftype ::d/bool) (run! #(process-bool! stream nil %) value)))

(defn- dispatch-item! [stream item data]
  (let [[fid ftype fmeta] item
        value (get data fid)
        {rank ::d/rank} fmeta]
    (if (nil? rank) (dispatch-single! stream ftype fmeta value) ;;data is a single value
        (let [datalen (count value) ;;data is an array
              pfx (if (keyword? rank) rank nil)
              speclen (if (integer? rank) rank datalen)]
          (write-prefix! stream pfx datalen)
          (if (not= datalen speclen) (throw (Exception. "Array len do not match spec"))
              (dispatch-vector! stream ftype fmeta value))))))

(defn write! [stream pseq data]
    (run! #(dispatch-item! stream % data) pseq))
