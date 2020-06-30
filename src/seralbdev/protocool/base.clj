(ns seralbdev.protocool.base
  (:import (io.netty.buffer Unpooled)))

(declare wrap-bytearrayLE)
(declare wrap-bytearray)

;; INPUT INTERFACE
;;---------------------------------------------------------------------------------

(defn rindex
  "Returns the current reading position
  of this stream"
  [{b :buffer}]
  (.readerIndex b))

(defn rseek!
  "Sets the reading position
  of this stream"
  [{b :buffer} position]
  (.readerIndex b position))

(defn rslice!
  "Returns a new stream which is a view
  of current one from the reading position
  until the end"
  [{b :buffer}]
  (.slice b))

(defn read-byte! [{b :buffer}]
  (.readByte b))

(defn get-byte! [{b :buffer} index]
  (.getByte b index))

(defn read-int16! [{m :mode b :buffer}]
  (cond
    (= m :LE) (.readShortLE b)
    :else (.readShort b)))

(defn read-int32! [{m :mode b :buffer}]
  (cond
    (= m :LE) (.readIntLE b)
    :else (.readInt b)))

(defn read-int64! [{m :mode b :buffer}]
  (cond
    (= m :LE) (.readLongLE b)
    :else (.readLong b)))

(defn read-real32! [{m :mode b :buffer}]
  (cond
    (= m :LE) (.readFloatLE b)
    :else (.readFloat b)))

(defn read-real64! [{m :mode b :buffer}]
  (cond
    (= m :LE) (.readDoubleLE b)
    :else (.readDouble b)))

(defn- read-array [array f]
  (let [cnt (alength array)]
    (doall
     (for [index (range cnt)]
      (aset array index (f))))))

(defn read-bytes! [{b :buffer} count]
  (let [ar (byte-array count)]
    (.readBytes b ar)
    ar))

(defn read-ints16! [{m :mode b :buffer} count]
  (let [ar (short-array count)]
    (cond
      (= m :LE) (read-array ar #(.readShortLE b))
      :else (read-array ar #(.readShort b)))
    ar))

(defn read-ints32! [{m :mode b :buffer} count]
  (let [ar (int-array count)]
    (cond
      (= m :LE) (read-array ar #(.readIntLE b))
      :else (read-array ar #(.readInt b)))
    ar))

(defn read-ints64! [{m :mode b :buffer} count]
  (let [ar (long-array count)]
    (cond
      (= m :LE) (read-array ar #(.readLongtLE b))
      :else (read-array ar #(.readLong b)))
    ar))

(defn read-reals32! [{m :mode b :buffer} count]
  (let [ar (float-array count)]
    (cond
      (= m :LE) (read-array ar #(.readFloatLE b))
      :else (read-array ar #(.readFloat b)))
    ar))

(defn read-reals64! [{m :mode b :buffer} count]
  (let [ar (double-array count)]
    (cond
      (= m :LE) (read-array ar #(.readDoubleLE b))
      :else (read-array ar #(.readDouble b)))
    ar))



;; OUTPUT INTERFACE
;;---------------------------------------------------------------------------------

(defn windex
  "Returns the current write index position
  of this stream"
  [{b :buffer}]
  (.writerIndex b))

(defn wseek!
  "Sets the current write index position
  for this stream"
  [{b :buffer} position]
  (.writerIndex b position))

(defn write-byte! [{b :buffer} value]
  (.writeByte b (byte value)))

(defn set-byte! [{b :buffer} index value]
  (.setByte b index (byte value)))

(defn write-bytes! [{b :buffer} values src-index src-count]
  (.writeBytes b (byte-array values) src-index src-count))

(defn write-int16! [{m :mode b :buffer} value]
  (cond
    (= m :LE) (.writeShortLE b (short value))
    :else (.writeShort b (short value))))

(defn set-int16! [{m :mode b :buffer} index value]
  (cond
    (= m :LE) (.setShortLE b index (int value))
    :else (.setShort b index (int value))))

(defn write-int32! [{m :mode b :buffer} value]
  (cond
    (= m :LE) (.writeIntLE b (int value))
    :else (.writeInt b (int value))))

(defn set-int32! [{m :mode b :buffer} index value]
  (cond
    (= m :LE) (.setIntLE b index (int value))
    :else (.setInt b index (int value))))

(defn write-int64! [{m :mode b :buffer} value]
  (cond
    (= m :LE) (.writeLongLE b (long value))
    :else (.writeLong b (long value))))

(defn set-int64! [{m :mode b :buffer} index value]
  (cond
    (= m :LE) (.setLongLE b index (int value))
    :else (.setLong b index (int value))))

(defn write-real32! [{m :mode b :buffer} value]
  (cond
    (= m :LE) (.writeFloatLE b (float value))
    :else (.writeFloat b (float value))))

(defn write-real64! [{m :mode b :buffer} value]
  (cond
    (= m :LE) (.writeDoubleLE b (double value))
    :else (.writeDouble b (double value))))

(defn- write-array [values f]
  (let [cnt (alength values)]
    (doall
     (for [index (range cnt)]
      (f (aget values index))))))

(defn write-ints16! [{m :mode b :buffer} values]
  (cond
    (= m :LE) (write-array values #(.writeShortLE b %))
    :else (write-array values #(.writeShort b %))))

(defn write-ints32! [{m :mode b :buffer} values]
  (cond
    (= m :LE) (write-array values #(.writeIntLE b %))
    :else (write-array values #(.writeInt b %))))

(defn write-ints64! [{m :mode b :buffer} values]
  (cond
    (= m :LE) (write-array values #(.writeLongLE b %))
    :else (write-array values #(.writeLong b %))))

(defn write-reals32! [{m :mode b :buffer} values]
  (cond
    (= m :LE) (write-array values #(.writeFloatLE b %))
    :else (write-array values #(.writeFloat b %))))

(defn write-reals64! [{m :mode b :buffer} values]
  (cond
    (= m :LE) (write-array values #(.writeDoubleLE b %))
    :else (write-array values #(.writeDouble b %))))


;; STREAM INTERFACE
;; -----------------------------------------

(defn wrap-bytearrayLE
  "Creates a stream that wraps an existing
  byte array in LE mode"
  [values]
  {:mode :LE :buffer (Unpooled/wrappedBuffer values)})

(defn wrap-bytearray
  "Creates a stream that wraps an existing
  byte array in BE mode"
  [values]
  {:mode :BE :buffer (Unpooled/wrappedBuffer values)})

(defn createLE
  "Creates an empty stream in Little Endian mode"
  []
  {:mode :LE :buffer (Unpooled/buffer)})

(defn create
  "Creates an empty stream in Big Endian mode"
  []
  {:mode :BE :buffer (Unpooled/buffer)})


(defn seal!
  "Seals this stream and
  returns a the backing byte array"
  [{b :buffer}]
  (.capacity b (.writerIndex b))
  (.array b))


