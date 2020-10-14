(ns seralbdev.protocool.pseq_decoder
  (:require
    [seralbdev.protocool.base :as b]
    [seralbdev.protocool.pseq :as d]))

(declare read!)

(defn- read-padding! [stream padlen]
  (b/skip! stream padlen)
  nil)

(defn- read-prefix! [stream prefix]
  (cond
    (= prefix ::d/u8) (b/read-ubyte! stream)
    (= prefix ::d/u16) (b/read-uint16! stream)
    (= prefix ::d/u32) (b/read-uint32! stream)))

(defn- read-str! [stream len]
  (let [data (b/read-bytes! stream len)
        txt (String. data)]
    txt))

(defn- process-pfx-str! [stream prefix]
  (let [len (read-prefix! stream prefix)]
    (read-str! stream len)))

(defn- process-fixlen-str! [stream maxlen]
  (let [rawdata (b/read-bytes! stream maxlen)
        trimdata (byte-array (remove #(= % 0) rawdata))]
    (String. trimdata)))

(defn- process-varlen-str! [stream]
  (loop [txt '()]
    (let [ch (b/read-byte! stream)]
      (if (= ch 0) (String. (byte-array txt))
          (recur (conj txt ch))))))

(defn- process-str! [stream fmeta]
  (let [{pfx ::d/pfx len ::d/len} fmeta]
    (cond
      (some? pfx) (process-pfx-str! stream pfx)
      (some? len) (process-fixlen-str! stream len)
      :else (process-varlen-str! stream))))

(defn- process-padding! [stream fmeta]
  (let [padlen (::d/len fmeta)]
    (read-padding! stream padlen)))

(defn- process-bool! [stream _]
  (let [value (b/read-byte! stream)
        tf (if (= value 0) false true)]
    tf))

(defn- process-pseq! [stream resolver fmeta]
  (let [fieldseq (::d/fields fmeta)]
    (read! stream resolver fieldseq)))

(defn- process-psref!
  "resolver: f(pseqid)->pseq
   fmeta: {::pfx ::i8|::i16|::i32}"
  [stream resolver fmeta]
  (let [pseqid (process-str! stream fmeta)
        reffmeta {::d/fields (resolver pseqid)}]
    (process-pseq! stream resolver reffmeta)))

(defn- dispatch-single! [stream resolver ftype fmeta]
  (cond
    (= ftype ::d/i8) (b/read-byte! stream)
    (= ftype ::d/u8) (b/read-ubyte! stream)
    (= ftype ::d/i16) (b/read-int16! stream)
    (= ftype ::d/u16) (b/read-uint16! stream)
    (= ftype ::d/i32) (b/read-int32! stream)
    (= ftype ::d/u32) (b/read-uint32! stream)
    (= ftype ::d/i64) (b/read-int64! stream)
    (= ftype ::d/r32) (b/read-real32! stream)
    (= ftype ::d/r64) (b/read-real64! stream)
    (= ftype ::d/str) (process-str! stream fmeta)
    (= ftype ::d/bool) (process-bool! stream nil)
    (= ftype ::d/padding) (process-padding! stream fmeta)
    (= ftype ::d/pseq) (process-pseq! stream resolver fmeta)
    (= ftype ::d/psref) (process-psref! stream resolver fmeta)))

(defn- dispatch-psref-vector! [stream resolver count fmeta]
  (let [pseqid (process-str! stream fmeta)
         reffmeta {::d/fields (resolver pseqid)}]
     (take count (repeatedly #(process-pseq! stream resolver reffmeta)))))

(defn- dispatch-vector! [stream resolver count ftype fmeta]
  (cond
    (= ftype ::d/i8) (b/read-bytes! stream count)
    (= ftype ::d/u8) (b/read-ubytes! stream count)
    (= ftype ::d/i16) (b/read-ints16! stream count)
    (= ftype ::d/u16) (b/read-uints16! stream count)
    (= ftype ::d/i32) (b/read-ints32! stream count)
    (= ftype ::d/u32) (b/read-uints32! stream count)
    (= ftype ::d/i64) (b/read-ints64! stream count)
    (= ftype ::d/r32) (b/read-reals32! stream count)
    (= ftype ::d/r64) (b/read-reals64! stream count)
    (= ftype ::d/str) (take count (repeatedly #(process-str! stream fmeta)))
    (= ftype ::d/bool) (take count (repeatedly #(process-bool! stream fmeta)))
    (= ftype ::d/pseq) (take count (repeatedly #(process-pseq! stream resolver fmeta)))
    (= ftype ::d/psref) (dispatch-psref-vector! stream resolver count fmeta)))

(defn- dispatch-item! [stream resolver item]
  (let [[fid ftype fmeta] item
        rank (::d/rank fmeta)]
    (if (nil? rank) {fid (dispatch-single! stream resolver ftype fmeta)} ;;data is a single value
        (let [pfxlen (read-prefix! stream rank) ;;data is vector
              count (or pfxlen rank)]
          {fid (into [] (dispatch-vector! stream resolver count ftype fmeta))}))))

(defn read! [stream resolver pseq]
  (into {} (map #(dispatch-item! stream resolver %) pseq)))
