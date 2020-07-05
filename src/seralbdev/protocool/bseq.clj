(ns seralbdev.protocool.bseq
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::valid-tokens #{::bool ::i8 ::u8 ::i16 ::u16 ::i32 ::u32 ::i64 ::u64 ::r32 ::r64 ::str ::pstr ::struct ::padding})

(s/def ::valid-fmeta #{::rank ::len ::pfx})
