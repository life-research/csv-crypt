(ns csv-crypt.core-test
  (:require
    [buddy.core.nonce :as nonce]
    [clojure.string :as str]
    [clojure.test :refer :all]
    [cognitect.anomalies :as anom]
    [csv-crypt.core :refer :all]
    [juxt.iota :refer [given]])
  (:import
    [org.apache.commons.codec.binary Base64])
  (:refer-clojure :exclude [key]))

(def key (nonce/random-bytes 32))
(def key1 (byte-array [-70 17 126 -77 -37 -36 127 -28 61 -70 124 -73 102 96 -24 -58 119 109 -1 94 100 31 0 32 99 -59 -73 -38 -63 -66 -103 71]))

(defmacro file= [file & lines]
  `(is (= (str/trim (slurp ~file)) (str/join "\n" [~@lines]))))

(defmacro utf-16le-file= [file & lines]
  `(is (= (str/trim (subs (slurp ~file :encoding "UTF-16LE") 1))
          (str/join "\n" [~@lines]))))

(deftest example-comma-utf8
  (encrypt-file key "examples/example-comma-utf8.csv" "target/out-cr-1.csv" {})
  (decrypt-file key "target/out-cr-1.csv" "target/out-1.csv" {})
  (file= "target/out-1.csv"
         "id,d1,d2"
         "1,a,23"
         "2,longer text with spaces,42"
         "3,text with german umlauts: äüöß,96"
         "4,\"text with"
         "newline\",2018"
         "5,\"comma ,\",semicolon ;"))

(deftest excel-2013-unicode-text
  (encrypt-file key "examples/excel-2013-unicode-text.txt" "target/out-cr-2.csv"
                {:in-separator \tab})
  (decrypt-file key "target/out-cr-2.csv" "target/out-2.csv"
                {:out-with-bom? true
                 :out-encoding "UTF-16LE"
                 :out-separator \tab})
  (utf-16le-file= "target/out-2.csv"
                  "id\td1\td2"
                  "1\ta\t23"
                  "2\tlonger text with spaces\t42"
                  "3\ttext with german umlauts: äüöß\t96"
                  "4\t\"text with"
                  "newline\"\t2018"
                  "5\tcomma ,\tsemicolon ;"))

(deftest excel-2013-csv-german
  (encrypt-file key "examples/excel-2013-csv-german.csv" "target/out-cr-2.csv"
                {:in-encoding "ISO-8859-15" :in-separator \;})
  (decrypt-file key "target/out-cr-2.csv" "target/out-2.csv" {})
  (file= "target/out-2.csv"
         "id,d1,d2"
         "1,a,23"
         "2,longer text with spaces,42"
         "3,text with german umlauts: äüöß,96"
         "4,\"text with"
         "newline\",2018"
         "5,\"comma ,\",semicolon ;"))

(deftest example-invalid-first-line
  (is (.startsWith
        (with-out-str
          (decrypt-file key "examples/invalid-first-line.csv" "target/out.csv" {}))
        "Problem in line 1:")))

(deftest example-invalid-second-line
  (is (.startsWith
        (with-out-str
          (decrypt-file key1 "examples/invalid-second-line.csv" "target/out.csv" {}))
        "Problem in line 2:")))

(deftest decrypt-with-wrong-key
  (encrypt-file key "examples/example-comma-utf8.csv" "target/out-cr-3.csv" {})
  (is (= (with-out-str
           (decrypt-file key1 "target/out-cr-3.csv" "target/out.csv" {}))
         "Problem in line 1:\n     Problem while decrypting: Message seems corrupt or manipulated.\n")))

(deftest decrypt-line-test
  (testing "An empty second column is incorrect."
    (given (decrypt-line key ["id" ""])
      ::anom/category := ::anom/incorrect))

  (testing "A too short second column is incorrect."
    (given (decrypt-line key ["id" (Base64/encodeBase64String (byte-array 31))])
      ::anom/category := ::anom/incorrect
      ::anom/message := "Invalid cipher text."))

  (testing "A short second column is incorrect."
    (given (decrypt-line key ["id" (Base64/encodeBase64String (byte-array 32))])
      ::anom/category := ::anom/incorrect
      ::anom/message := "Problem while decrypting: Message seems corrupt or manipulated.")))
