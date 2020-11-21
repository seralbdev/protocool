![](doc/protologo.png)

# PROTOC00L

_A cool library to work with binary data in Clojure_

Protoc00l library allows you to work with binary data at the right abstract level Clojure offers\
Its development started by the need of interacting with industrial devices using Ethernet communications using arbitrary data sequences.

## Protoc00l low level functions

------
The **base** namespace defines the low level mutable byte stream abstraction. Functions here are "wrapping" a Java unpooled netty buffer
A protoc00l stream contains the raw byte sequence and its endianess
This namespace offers functions for creating streams and reading and writing data

## Protoc00l sequences

------
Two devices exchange information following a protocol. A protocol defines the possible set of tokens plus the sequence\
In this library a protocol is defined by a protoc00l sequece (pseq)\
The possible tokens are defined in the **pseq** namespace. Functions for encoding and decoding are available in **pseq-encoder** and **pseq-decoder** namespaces

A sequence is just a vector of fields\
A field is, as well, a vector defining the field-id, the field-type and optional metadata

```clj
[ ["field1-id" field-type metadata] ["field2-id" field-type metadata] ...]
```

This example shows a sequence of two fields

```clj
[ ["F1" ::u16] ["F2" ::str {::len 8}] ]
```

Reading data from a protc00l stream will return a map of field => value pairs

```clj
;; Binary stream data to read => |12|"HiWorld!\0\0"|
;; Protoc00l sequece
[["F1" ::u16]["F2" ::str {::len 10}]]
;; The returned data will be
{"F1" 12 "F2" "HiWorld!"}
```
Writing data to a protc00l stream will require a map of field => value pairs

```clj
;; Data to write to protoc00l stream
{"F1" 12 "F2" "HiWorld!"}
;; Protoc00l sequece
[["F1" ::u16]["F2" ::str {::len 10}]]
;; The written data will be
;; => |12|"HiWorld!\0\0"|
```

### Field id

------
Each field must have a unique ID represented by an arbitrary String

### Field type

------
Protoc00l supports a number of types. They are represented by fully qualified keywords from pseq namespace

|TYPE     |SIZE (bytes)|DESCRIPTION                              |
|---------|------------|-----------------------------------------|
|::bool   |1           |Boolean type (TRUE of FALSE)             |
|::i8     |1           |Signed 8 bit integer                     |
|::u8     |1           |Unsigned 8 bit integer                   |
|::i16    |2           |Signed 16 bit integer                    |
|::u16    |2           |Unsigned 16 bit integer                  |
|::i32    |4           |Signed 32 bit integer                    |
|::u32    |4           |Unsigned 32 bit integer                  |
|::i64    |8           |Signed 64 bit integer                    |
|::r32    |4           |32 bit real                              |
|::r64    |8           |64 bit real                              |
|::str    |variable    |String data                              |
|::padding|variable    |Empty data (used to align fields)        |
|::pseq   |variable    |Embedded/recursive pseq                  |
|::psref  |variable    |Reference (by id) to a external pseq     |

&nbsp;

### Metadata

------
This is an optional part of field and it is defined as map

Arrays

------
Arrays can be defined for every type except padding\
Single dimention array are supported\
Arrays can be defined with constant or variable lentgh\
The fully qualifed keyword ::rank is used to denote an array field


A constant length array is defined with this metadata attached to the field 

```clj
{::rank integer-value}
```

```clj
["F1" ::i16 {::rank 10}] ;; Example of a short integer array of 10 elements
```

A variable length array expects a previous value in the data stream defining the lenght. It is defined with this metadata attached to the field

```clj
{::rank ::u8}  ;;Variable length array. Size if defined by a preceeding 1 byte value
{::rank ::u16} ;;Variable length array. Size if defined by a preceeding 2 byte value
{::rank ::u32} ;;Variable length array. Size if defined by a preceeding 4 byte value
```

```clj
["F1" ::i16 {::rank ::U8}] ;; Example of a short integer array of a length defined by a 1 byte preceeding value
```


#### Strings

----
Single byte Strings are supported. There are three possible types\


Variable length string delimited by \0 at the end. In this case no metadata is needed (default case)\

> |H|I|W|O|R|L|D|!|\0|

```clj
["F1" ::str] ;; Example of a variable length string
```

Fixed length string padded with \0 until the length is completed

> |H|I|W|O|R|L|D|!|\0|\0|\0|\0|\0|\0|

```clj
{::len integer-value}
```

```clj
["F1" ::str {::len integer-value}] ;; Example of a Fixed length string field
```

Pre-fixed length string. The characters are preceeded by a value indicating the total lenght

> |8|H|I|W|O|R|L|D|!|

```clj
{::pfx ::u8}  ;;Pre-fixed length string defined by 1 byte value
{::pfx ::u16} ;;Pre-fixed length string defined by 2 byte value
{::pfx ::u32} ;;Pre-fixed length string defined by 4 byte value
```
```clj
["F1" ::str {::pfx ::u16}] ;; Example of a pre-fixed length string field
```

#### Padding

----
Sometimes it is necessary to add padding to align data types. This type of field reads data from stream that is discarded\
The amount of padding is indicated by the fully qualified keyword ::len and a constant integer value\
In case of value the field id is not used and can be an empty string

```clj
{::len integer-value}
```

```clj
["" ::padding {::len 3}] ;; Example of a padding field of 3 bytes
```

#### Embedded/recursive protoc00l sequence

----
It is possible to define complex fields that are protoc00l sequences themselves. In this case the fully qualified ::fields keyword is used to define the embedded sequence

```clj
["F1" ::pseq {::fields [["F11" ::u8]["F12" ::str]]}] ;; Example of an embedded protoc00l sequence in a field
```

Reading/writing data using this sequence will return/require data in a map like this

```clj
{"F1" {"F11" 12 "F12" "HiWorld!"}}
```

#### Reference to an external protoc00l sequence

----
It is possible to define sequences that contain references to sequences not known in advance. Those sequences must be stored somewhere and identified by a String\
In this type of sequences the sequence id (String) is sent before the sequence data (dynamic sequence). The length of the string identifying the coming sequence is defined with the fully qualified keyword ::pfx

```clj
["F1" ::psref {::pfx ::u8}]  ;;dynamic sequence with 1 byte legth string id
["F1" ::psref {::pfx ::u16}] ;;dynamic sequence with 2 byte legth string id
["F1" ::psref {::pfx ::u32}] ;;dynamic sequence with 4 byte legth string id
```

This is an example of a protoc00l sequence in which token is well known but the following data is variable

```clj
[["token" ::str]["data" ::psref {::pfx ::u8}]]
```

The data on the wire would look somthing like

> |"token-001"|"seq123"|sequence data......|

User must provide a resolver function that accepts a sequence id and returns a protoc00l sequence

> f[id] => [[..][..]...]

&nbsp;
&nbsp;

### Usage examples

------
Let's see some usage examples

Reading a sequence from a stream

```clj
(:require [seralbdev.protocool.pseq :as d]
          [seralbdev.protocool.pseq_decoder :as dec])

(defn deserialize [stream])
  (let [pseq [["userid" ::d/u32]["addresses" ::d/str {::d/rank ::d/u8 ::d/pfx ::d/u16}]]
        resolver (fn [id] {})]
    (dec/read! stream resolver pseq)) ;; => {"userid" "user1" "addresses" ["home1" "home2"]}
```
Writing data matching a sequence into a stream

```clj
(:require [seralbdev.protocool.pseq :as d]
          [seralbdev.protocool.pseq_encoder :as enc])

(defn serialize [stream])
  (let [pseq [["userid" ::d/u32]["addresses" ::d/str {::d/rank ::d/u8 ::d/pfx ::d/u16}]]
        data {"userid" "user1" "addresses" ["home1" "home2"]}
        resolver (fn [id] {})]
    (dec/write! stream resolver pseq data))
```

Reading a sequence from a stream. The data that comes after the usedid is not know at design time. The type of sequence is prefixed to the data in the form of a prefix string with a length encoded in one byte. The resolver function will receive the id and will return the right sequence for the data to be decoded

```clj
(:require [seralbdev.protocool.pseq :as d]
          [seralbdev.protocool.pseq_decoder :as dec])

(defn resolver [id]
  (cond
    (= id "UDT1") [["F1" ::d/i16]["F2" ::d/bool]]
    (= id "UDT2") [["F3" ::d/str]["F4" ::d/r32]]))

(defn deserialize [stream])
  (let [pseq [["userid" ::d/u32]["data" ::d/psref {::d/pfx ::d/u8}]]
    (dec/read! stream resolver pseq)) ;; => {"userid" "user1" "addresses" ["home1" "home2"]}
```

### Sequence examples

------
Let's see some sequence examples

```clj
;; Sequence composed by a user id followed by a variable array of pre-fixed lenght strings
;; the number of address items comes in a byte
;; each address is a prefix length string of 2 bytes

[["userid" ::u32]["addresses" ::str {::rank ::u8 ::pfx ::u16}]]
```

```clj
;; Sequence composed by an user id followed by a variable array of embedded sequences
;; each embedded sequence is composed by an arecode (2 byte integer) and an address (variable string)
;; the number of items in an array is variable and comes as a prefix byte
 
[["userid" ::u32]["addresses" ::pseq {::rank ::u8 ::fields [["areacode" ::u16]["address" ::str]]}]]
```

# License

Copyright © 2020 Alberto Serrano Alonso

This program and the accompanying materials are made available under the
terms of the Creative Commons Attribution-ShareAlike 3.0 Unported License which is available at
https://creativecommons.org/licenses/by-sa/3.0/