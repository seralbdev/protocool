(ns seralbdev.protocool.pseq
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::primitives #{::i8 ::u8 ::i16 ::u16 ::i32 ::u32 ::i64 ::r32 ::r64})

(s/def ::valid-tokens #{::bool ::i8 ::u8 ::i16 ::u16 ::i32 ::u32 ::i64 ::r32 ::r64 ::str ::pseq ::psref ::padding})
(s/def ::valid-fmeta #{::rank ::len ::pfx ::fields ::id})
