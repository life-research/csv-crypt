(ns csv-crypt.core-test
  (:require
    [buddy.core.nonce :as nonce]
    [clojure.string :as str]
    [clojure.test :refer :all]
    [csv-crypt.core :refer :all])
  (:refer-clojure :exclude [key]))

(def key (nonce/random-bytes 32))

(defn file= [file & lines]
  (= (str/trim (slurp file)) (str/join "\n" lines)))

(deftest example-comma-utf8
  (encrypt-file key "examples/example-comma-utf8.csv" "target/out-cr.csv"
                "UTF-8" \, \,)
  (decrypt-file key "target/out-cr.csv" "target/out.csv" "UTF-8" \, \,)
  (is (file= "target/out.csv"
             "id,d1,d2"
             "1,a,23"
             "2,longer text with spaces,42"
             "3,text with german umlauts: äüöß,96"
             "4,\"text with"
             "newline\",2018"
             "5,\"comma ,\",semicolon ;")))

(deftest excel-2013-unicode-text
  (encrypt-file key "examples/excel-2013-unicode-text.txt" "target/out-cr.csv"
                "UTF-8" \tab \,)
  (decrypt-file key "target/out-cr.csv" "target/out.csv" "UTF-8" \, \,)
  (is (file= "target/out.csv"
             "id,d1,d2"
             "1,a,23"
             "2,longer text with spaces,42"
             "3,text with german umlauts: äüöß,96"
             "4,\"text with"
             "newline\",2018"
             "5,\"comma ,\",semicolon ;")))

(deftest excel-2013-csv-german
  (encrypt-file key "examples/excel-2013-csv-german.csv" "target/out-cr.csv"
                "ISO-8859-15" \; \,)
  (decrypt-file key "target/out-cr.csv" "target/out.csv" "UTF-8" \, \,)
  (is (file= "target/out.csv"
             "id,d1,d2"
             "1,a,23"
             "2,longer text with spaces,42"
             "3,text with german umlauts: äüöß,96"
             "4,\"text with"
             "newline\",2018"
             "5,\"comma ,\",semicolon ;")))

(comment
  (run-tests)
  )
