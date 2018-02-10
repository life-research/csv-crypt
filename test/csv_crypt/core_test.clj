(ns csv-crypt.core-test
  (:require
    [buddy.core.nonce :as nonce]
    [clojure.string :as str]
    [clojure.test :refer :all]
    [csv-crypt.core :refer :all])
  (:refer-clojure :exclude [key]))

(def key (nonce/random-bytes 32))

(defmacro file= [file & lines]
  `(is (= (str/trim (slurp ~file)) (str/join "\n" [~@lines]))))

(defmacro utf-16le-file= [file & lines]
  `(is (= (str/trim (subs (slurp ~file :encoding "UTF-16LE") 1))
          (str/join "\n" [~@lines]))))

(deftest example-comma-utf8
  (encrypt-file key "examples/example-comma-utf8.csv" "target/out-cr.csv" {})
  (decrypt-file key "target/out-cr.csv" "target/out.csv" {})
  (file= "target/out.csv"
         "id,d1,d2"
         "1,a,23"
         "2,longer text with spaces,42"
         "3,text with german umlauts: äüöß,96"
         "4,\"text with"
         "newline\",2018"
         "5,\"comma ,\",semicolon ;"))

(deftest excel-2013-unicode-text
  (encrypt-file key "examples/excel-2013-unicode-text.txt" "target/out-cr.csv"
                {:in-separator \tab})
  (decrypt-file key "target/out-cr.csv" "target/out.csv"
                {:out-with-bom? true
                 :out-encoding "UTF-16LE"
                 :out-separator \tab})
  (utf-16le-file= "target/out.csv"
                  "id\td1\td2"
                  "1\ta\t23"
                  "2\tlonger text with spaces\t42"
                  "3\ttext with german umlauts: äüöß\t96"
                  "4\t\"text with"
                  "newline\"\t2018"
                  "5\tcomma ,\tsemicolon ;"))

(deftest excel-2013-csv-german
  (encrypt-file key "examples/excel-2013-csv-german.csv" "target/out-cr.csv"
                {:in-encoding "ISO-8859-15" :in-separator \;})
  (decrypt-file key "target/out-cr.csv" "target/out.csv" {})
  (file= "target/out.csv"
         "id,d1,d2"
         "1,a,23"
         "2,longer text with spaces,42"
         "3,text with german umlauts: äüöß,96"
         "4,\"text with"
         "newline\",2018"
         "5,\"comma ,\",semicolon ;"))
