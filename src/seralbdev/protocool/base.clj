(ns seralbdev.protocool.base
  (:import (io.netty.buffer Unpooled)))


;; INPUT INTERFACE
;;---------------------------------------------------------------------------------

(defn rindex
  "Returns the current reading position
  of this stream"
  [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.readerIndex b))))

(defn rseek!
  "Sets the reading position
  of this stream"
  [stream position]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.readerIndex b position))))

(defn skip!
  "Skips the selected
  number of bytes"
  [stream numBytes]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.skipBytes b numBytes))))

(defn rslice!
  "Returns a new stream which is a view
  of current one from the reading position
  until the end"
  [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.slice b))))

(defn read-byte! [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.readByte b))))

(defn read-ubyte! [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.readUnsignedByte b))))

(defn get-byte! [stream index]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.getByte b index))))

(defn read-int16! [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.readShortLE b)
        :else (.readShort b)))))

(defn read-uint16! [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.readUnsignedShortLE b)
        :else (.readUnsignedShort b)))))

(defn read-int32! [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.readIntLE b)
        :else (.readInt b)))))

(defn read-uint32! [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.readUnsignedIntLE b)
        :else (.readUnsignedInt b)))))

(defn read-int64! [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.readLongLE b)
        :else (.readLong b)))))

(defn read-real32! [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.readFloatLE b)
        :else (.readFloat b)))))

(defn read-real64! [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.readDoubleLE b)
        :else (.readDouble b)))))

(defn- read-array [array f]
  (let [cnt (alength array)]
    (doall
     (for [index (range cnt)]
      (aset array index (f))))))

(defn read-bytes! [stream count]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          ar (byte-array count)]
      (.readBytes b ar)
      ar)))

(defn read-ubytes! [stream count]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          ar (short-array count)]
      (read-array ar #(.readUnsignedByte b))
      ar)))

(defn read-ints16! [stream count]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)
          ar (short-array count)]
      (cond
        (= m :little) (read-array ar #(.readShortLE b))
        :else (read-array ar #(.readShort b)))
      ar)))

(defn read-uints16! [stream count]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)
          ar (int-array count)]
      (cond
        (= m :little) (read-array ar #(.readUnsignedShortLE b))
        :else (read-array ar #(.readUnsignedShort b)))
      ar)))

(defn read-ints32! [stream count]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)
          ar (int-array count)]
      (cond
        (= m :little) (read-array ar #(.readIntLE b))
        :else (read-array ar #(.readInt b)))
      ar)))

(defn read-uints32! [stream count]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)
          ar (long-array count)]
      (cond
        (= m :little) (read-array ar #(.readUnsignedIntLE b))
        :else (read-array ar #(.readUnsignedInt b)))
      ar)))
 
(defn read-ints64! [stream count]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)
          ar (long-array count)]
      (cond
        (= m :little) (read-array ar #(.readLongtLE b))
        :else (read-array ar #(.readLong b)))
      ar)))

(defn read-reals32! [stream count]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)
          ar (float-array count)]
      (cond
        (= m :little) (read-array ar #(.readFloatLE b))
        :else (read-array ar #(.readFloat b)))
      ar)))

(defn read-reals64! [stream count]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)
          ar (double-array count)]
      (cond
        (= m :little) (read-array ar #(.readDoubleLE b))
        :else (read-array ar #(.readDouble b)))
      ar)))



;; OUTPUT INTERFACE
;;---------------------------------------------------------------------------------

(defn windex
  "Returns the current write index position
  of this stream"
  [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.writerIndex b))))

(defn wseek!
  "Sets the current write index position
  for this stream"
  [stream position]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.writerIndex b position))))

(defn write-byte! [stream value]
  (when (and stream value)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.writeByte b value))))

(defn set-byte! [stream index value]
  (when (and stream value)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.setByte b index value))))

(defn write-bytes! [stream values src-index src-count]
  (when (and stream values)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.writeBytes b (byte-array values) src-index src-count))))

(defn write-int16! [stream value]
  (when (and stream value)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.writeShortLE b value)
        :else (.writeShort b value)))))

(defn set-int16! [stream index value]
  (when (and stream value)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.setShortLE b index value)
        :else (.setShort b index value)))))

(defn write-int32! [stream value]
  (when (and stream value)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.writeIntLE b value)
        :else (.writeInt b value)))))

(defn set-int32! [stream index value]
  (when (and stream value)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.setIntLE b index value)
        :else (.setInt b index value)))))

(defn write-int64! [stream value]
  (when (and stream value)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.writeLongLE b value)
        :else (.writeLong b value)))))

(defn set-int64! [stream index value]
  (when (and stream value)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.setLongLE b index value)
        :else (.setLong b index value)))))

(defn write-real32! [stream value]
  (when (and stream value)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.writeFloatLE b (float value))
        :else (.writeFloat b (float value))))))

(defn write-real64! [stream value]
  (when (and stream value)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (.writeDoubleLE b (double value))
        :else (.writeDouble b (double value))))))

(defn- write-array [values f]
    (run! f values))

(defn write-ints16! [stream values]
  (when (and stream values)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (write-array values #(.writeShortLE b %))
        :else (write-array values #(.writeShort b %))))))

(defn write-ints32! [stream values]
  (when (and stream values)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (write-array values #(.writeIntLE b %))
        :else (write-array values #(.writeInt b %))))))

(defn write-ints64! [stream values]
  (when (and stream values)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (write-array values #(.writeLongLE b %))
        :else (write-array values #(.writeLong b %))))))

(defn write-reals32! [stream values]
  (when (and stream values)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (write-array values #(.writeFloatLE b %))
        :else (write-array values #(.writeFloat b %))))))

(defn write-reals64! [stream values]
  (when (and stream values)
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)
          m (:mode stream)]
      (cond
        (= m :little) (write-array values #(.writeDoubleLE b %))
        :else (write-array values #(.writeDouble b %))))))


;; STREAM INTERFACE
;; -----------------------------------------

(defn wrap-bytearray
  "Creates a stream that wraps an existing
  byte array in LE mode"
  [barray]
  (when barray
    {:mode :little :buffer (Unpooled/wrappedBuffer barray)}))

(defn wrap-bytearrayBE
  "Creates a stream that wraps an existing
  byte array in BE mode"
  [barray]
  (when barray
    {:mode :big :buffer (Unpooled/wrappedBuffer barray)}))

(defn create
  "Creates an empty stream in Little Endian mode"
  []
  {:mode :little :buffer (Unpooled/buffer)})

(defn createBE
  "Creates an empty stream in Big Endian mode"
  []
  {:mode :big :buffer (Unpooled/buffer)})

(defn seal!
  "Seals this stream and
  returns the backing byte array"
  [stream]
  (when stream
    (let [^io.netty.buffer.ByteBuf b (:buffer stream)]
      (.capacity b (.writerIndex b))
      (.array b))))

(defn clone
  "Returns a new (empty) stream with the same
  configuration as this one
  source: stream"
  [stream]
  (when stream
    (let [mode (:mode stream)]
      (if (= mode :little) (create)
        (createBE)))))

(defn endianess
  "Returns the endianess of this stream :litte|:big"
  [stream]
  (when stream
    (:mode stream)))
