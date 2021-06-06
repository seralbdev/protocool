(ns seralbdev.protocool.pseq_encoder
  (:require
   [seralbdev.protocool.base :as b]
   [seralbdev.protocool.pseq :as d]))

(declare write!)
(declare dispatch-vector!)

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

(defn- process-bool! [stream _ data]
  (let [value (if data 1 0)]
    (b/write-byte! stream value)))

(defn- process-padding! [stream fmeta]
  (let [padlen (::d/len fmeta)]
    (write-padding! stream padlen)))

(defn- process-pseq! [stream resolver fmeta data]
  (let [fields (::d/fields fmeta)]
    (write! stream resolver fields data)))

(defn- process-psref!
  "resolver: f(pseqid)->pseq
   value: ['refid' [{'f1' value1 'f2' value2 ...} {} ...]"
  [stream resolver value]
  (let [[pseqid data] value
        reffmeta {::d/fields (resolver pseqid)}]
    (process-str! stream {::d/pfx ::d/u16 } pseqid)
    (process-pseq! stream resolver reffmeta data)))

(defn- dispatch-single! [stream resolver ftype fmeta value]
  (cond
    (contains? #{::d/i8 ::d/u8} ftype) (b/write-byte! stream value)
    (contains? #{::d/i16 ::d/u16} ftype) (b/write-int16! stream value)
    (contains? #{::d/i32 ::d/u32} ftype) (b/write-int32! stream value)
    (= ftype ::d/i64) (b/write-int64! stream value)
    (= ftype ::d/r32) (b/write-real32! stream value)
    (= ftype ::d/r64) (b/write-real64! stream value)
    (= ftype ::d/str) (process-str! stream fmeta value)
    (= ftype ::d/bool) (process-bool! stream nil value)
    (= ftype ::d/padding) (process-padding! stream fmeta)
    (= ftype ::d/pseq) (process-pseq! stream resolver fmeta value)
    (= ftype ::d/psref) (process-psref! stream resolver value)))

(defn- dispatch-psref-vector!
  "fmeta => {::pfx ix ::rank n ::fields [...]]}
  value => ['REFID' [{...}...{...}]]"
  [stream resolver value]
  (let [[pseqid fieldata] value
        reffmeta {::d/fields (resolver pseqid)}]
    (process-str! stream {::d/pfx ::d/u16 } pseqid)
    (run! #(process-pseq! stream resolver reffmeta %) fieldata)))

(defn- dispatch-vector! [stream resolver ftype fmeta value]
  (cond
    (contains? #{::d/i8 ::d/u8} ftype) (b/write-bytes! stream value 0 (count value))
    (contains? #{::d/i16 ::d/u16} ftype) (b/write-ints16! stream value)
    (contains? #{::d/i32 ::d/u32} ftype) (b/write-ints32! stream value)
    (= ftype ::d/i64) (b/write-ints64! stream value)
    (= ftype ::d/r32) (b/write-reals32! stream value)
    (= ftype ::d/r64) (b/write-reals64! stream)
    (= ftype ::d/str) (run! #(process-str! stream fmeta %) value)
    (= ftype ::d/bool) (run! #(process-bool! stream nil %) value)
    (= ftype ::d/pseq) (run! #(process-pseq! stream resolver fmeta %) value)
    (= ftype ::d/psref) (dispatch-psref-vector! stream resolver value)))

(defn- dispatch-item! [stream resolver item data]
  (let [[fid ftype fmeta] item
        value (get data fid)
        {rank ::d/rank} fmeta]
    (if (nil? rank) (dispatch-single! stream resolver ftype fmeta value) ;;data is a single value
        (let [datalen (count value) ;;data is an array
              pfx (if (keyword? rank) rank nil)
              speclen (if (integer? rank) rank datalen)]
          (write-prefix! stream pfx datalen)
          (if (not= datalen speclen) (throw (Exception. "Array len do not match spec"))
              (dispatch-vector! stream resolver ftype fmeta value))))))

(defn write!
  "Writes data following the pseq pattern in this protocool stream
  This operation mutates the stream
  stream: protocool stream
  resolver: f[id]=>pseq (used for psrefs inside pseq)
  pseq: protocool sequence defining the shape of data
  data: data that conforms to this pseq"
  [stream resolver pseq data]
  (run! #(dispatch-item! stream resolver % data) pseq))
