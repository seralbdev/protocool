(ns seralbdev.protocool.pseq_decoder
  (:require
    [seralbdev.protocool.base :as b]
    [seralbdev.protocool.pseq :as d]))

(defn- read-padding! [stream padlen]
  (b/read-bytes! stream padlen)
  nil)

(defn- read-prefix! [stream prefix]
  (cond
    (= prefix ::d/u8) (b/read-ubyte! stream)
    (= prefix ::d/u16) (b/read-uint16! stream)
    (= prefix ::d/u32) (b/read-uint32! stream)))

(defn- process-padding! [stream fmeta]
  (let [padlen (:len fmeta)]
    (read-padding! stream padlen)))

(defn- dispatch-single! [stream ftype fmeta]
  (cond
    (contains? #{::d/i8 ::d/u8} ftype) (b/read-byte! stream)
    (contains? #{::d/i16 ::d/u16} ftype) (b/read-int16! stream)
    (contains? #{::d/i32 ::d/u32} ftype) (b/read-int32! stream)
    (= ftype ::d/i64) (b/read-int64! stream)
    (= ftype ::d/r32) (b/read-real32! stream)
    (= ftype ::d/r64) (b/read-real64! stream)
    ;(= ftype ::d/str) (process-str! stream fmeta)
    ;(= ftype ::d/bool) (process-bool! stream nile)
    (= ftype ::d/padding) (process-padding! stream fmeta)))
    ;(= ftype ::d/struct) (process-struct! stream fmeta)))

(defn- dispatch-vector! [stream count ftype fmeta]
  (cond
    (contains? #{::d/i8 ::d/u8} ftype) (b/read-bytes! stream 0 count)
    (contains? #{::d/i16 ::d/u16} ftype) (b/read-ints16! stream count)
    (contains? #{::d/i32 ::d/u32} ftype) (b/read-ints32! stream count)
    (= ftype ::d/i64) (b/read-ints64! stream)
    (= ftype ::d/r32) (b/read-reals32! stream)
    (= ftype ::d/r64) (b/read-reals64! stream)))
    ;(= ftype ::d/str) (run! #(process-str! stream fmeta %) value)
    ;(= ftype ::d/bool) (run! #(process-bool! stream nil %) value)
    ;(= ftype ::d/struct) (run! #(process-struct! stream fmeta %) value)))

(defn- dispatch-item! [stream item]
  (let [[fid ftype fmeta] item
        rank (::d/rank fmeta)]
    (if (nil? rank) {fid (dispatch-single! stream ftype fmeta)} ;;data is a single value
        (let [pfx (::d/prefix fmeta)
              pfxlen (read-prefix! stream pfx)
              count (or pfxlen rank)];;data is a vector
          {fid (dispatch-vector! stream count ftype fmeta)}))))

(defn read! [stream pseq]
  (into {} (map #(dispatch-item! stream %) pseq)))
