(ns csv-crypt.core
  (:require
    [buddy.core.bytes :as bytes]
    [buddy.core.codecs :as codecs]
    [buddy.core.codecs.base64 :as b64]
    [buddy.core.crypto :as crypto]
    [buddy.core.keys :as keys]
    [buddy.core.nonce :as nonce]
    [cheshire.core :as json]
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]
    [clojure.tools.cli :as cli])
  (:import
    [org.apache.commons.codec.binary Base64])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn read-csv
  "Reads CSV-data from file into a vector by applying `xform` to each line."
  [xform filename]
  (with-open [file (io/reader filename)]
    (into [] xform (csv/read-csv file))))

(defn write-csv [data filename]
  (with-open [file (io/writer filename)]
    (csv/write-csv file data)))

(defn encrypt-line
  "Encrypts the data of line starting with the second column.

  Doesn't touch the first column. Uses AES128-CBC-HMAC-SHA256 with a 16-byte
  random initialization vector (IV). Returns a tuple of the first column and
  a Base64 encoded concatenation of the IV and the cipher text."
  {:arglists '([key line])}
  [key [first & rest]]
  (let [iv (nonce/random-bytes 16)
        clear-text (json/generate-cbor rest)
        cipher-text (crypto/encrypt clear-text key iv
                                    {:algorithm :aes128-cbc-hmac-sha256})]
    [first (Base64/encodeBase64String (bytes/concat iv cipher-text))]))

(defn decrypt-line
  "Decrypts the data of line.

  Doesn't touch the first column and expects the encrypted data in the second
  column. Also doesn't care about other columns. Expects the encrypted data to
  be a Base64 encoded concatenation of a 16-byte initialization vector (IV) and
  the cipher text. Uses AES128-CBC-HMAC-SHA256."
  {:arglists '([key line])}
  [key [first second]]
  (let [iv-and-cipher-text (Base64/decodeBase64 ^String second)
        iv (bytes/slice iv-and-cipher-text 0 16)
        cipher-text (bytes/slice iv-and-cipher-text 16 (count iv-and-cipher-text))
        clear-text (crypto/decrypt cipher-text key iv
                                   {:algorithm :aes128-cbc-hmac-sha256})]
    (cons first (json/parse-cbor clear-text))))

(defn encrypt-file [key in-filename out-filename]
  (-> (read-csv (map #(encrypt-line key %)) in-filename)
      (write-csv out-filename)))

(defn decrypt-file [key in-filename out-filename]
  (-> (read-csv (map #(decrypt-line key %)) in-filename)
      (write-csv out-filename)))

(def cli-options
  [["-k" "--key KEY" "32-byte hex encoded key"]
   ["-e" "--encrypt"]
   ["-d" "--decrypt"]
   ["-g" "--gen-key"]
   ["-h" "--help"]])

(defn print-help [summary exit]
  (println "Usage: csv-crypt [-g] [-e -k key in-file out-file] [-d -k key in-file out-file]")
  (println summary)
  (System/exit exit))

(defn -main [& args]
  (let [{{:keys [key encrypt decrypt gen-key help]} :options
         [in-filename out-filename] :arguments
         :keys [summary]}
        (cli/parse-opts args cli-options)]
    (when help
      (print-help summary 0))
    (when (or encrypt decrypt)
      (when-not (and key in-filename out-filename)
        (print-help summary 1)))
    (cond
      encrypt
      (encrypt-file (codecs/hex->bytes key) in-filename out-filename)
      decrypt
      (decrypt-file (codecs/hex->bytes key) in-filename out-filename)
      gen-key
      (println (codecs/bytes->hex (nonce/random-bytes 32)))
      :else
      (print-help summary 1))))
