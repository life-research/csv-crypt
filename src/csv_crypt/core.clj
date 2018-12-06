(ns csv-crypt.core
  (:require
    [buddy.core.bytes :as bytes]
    [buddy.core.codecs :as codecs]
    [buddy.core.crypto :as crypto]
    [buddy.core.nonce :as nonce]
    [cheshire.core :as json]
    [cognitect.anomalies :as anom]
    [clj-bom.core :as bom]
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]
    [clojure.tools.cli :as cli])
  (:import
    [java.io Reader]
    [org.apache.commons.codec.binary Base64])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn bom-reader
  [in & opts]
  (let [is (io/input-stream in)]
    (if-let [encoding (bom/detect-encoding is)]
      (doto (io/reader is :encoding encoding) (.skip 1))
      (apply io/reader is opts))))

(defn read-csv
  "Reads CSV-data from file into a vector by applying `xform` to each line."
  [xform filename encoding separator]
  (with-open [^Reader reader (bom-reader filename :encoding encoding)]
    (into [] xform (csv/read-csv reader :separator separator))))

(defn write-csv [data filename with-bom? encoding separator]
  (with-open [writer (if with-bom? (bom/bom-writer encoding filename)
                                   (io/writer filename :encoding encoding))]
    (csv/write-csv writer data :separator separator)))

(defn encrypt-line
  "Encrypts the data of line starting with the second column.

  Doesn't touch the first column. Uses AES128-CBC-HMAC-SHA256 with a 16-byte
  random initialization vector (IV). Returns a tuple of the first column and
  a Base64 encoded concatenation of the IV and the cipher text."
  {:arglists '([key line])}
  [key [first & rest]]
  (if rest
    (let [iv (nonce/random-bytes 16)
          clear-text (json/generate-cbor rest)
          cipher-text (crypto/encrypt clear-text key iv
                                      {:algorithm :aes128-cbc-hmac-sha256})]
      [first (Base64/encodeBase64String (bytes/concat iv cipher-text))])
    [first]))

(defn decrypt [input key iv]
  (try
    (crypto/decrypt input key iv {:algorithm :aes128-cbc-hmac-sha256})
    (catch Exception e
      #::anom
          {:category ::anom/incorrect
           :message (str "Problem while decrypting: " (.getMessage e))})))

(defn decrypt-line
  "Decrypts the data of line.

  Doesn't touch the first column and expects the encrypted data in the second
  column. Also doesn't care about other columns. Expects the encrypted data to
  be a Base64 encoded concatenation of a 16-byte initialization vector (IV) and
  the cipher text. Uses AES128-CBC-HMAC-SHA256."
  {:arglists '([key line])}
  [key [first second]]
  (if second
    (let [iv-and-cipher-text (Base64/decodeBase64 ^String second)]
      (if (< (count iv-and-cipher-text) 32)
        #::anom
            {:category ::anom/incorrect
             :message "Invalid cipher text."}
        (let [iv (bytes/slice iv-and-cipher-text 0 16)
              cipher-text (bytes/slice iv-and-cipher-text 16 (count iv-and-cipher-text))
              clear-text (decrypt cipher-text key iv)]
          (if (and (map? clear-text) (::anom/category clear-text))
            clear-text
            (cons first (json/parse-cbor clear-text))))))
    [first]))

(defn decrypt-line-indexed [key index line]
  (let [line (decrypt-line key line)]
    (if (::anom/category line)
      (assoc line ::line-index index)
      line)))

(defn encrypt-file
  [key in-filename out-filename
   {:keys [in-encoding in-separator out-with-bom? out-encoding out-separator]
    :or {in-encoding "UTF-8"
         in-separator \,
         out-with-bom? false
         out-encoding "UTF-8"
         out-separator \,}}]
  (-> (read-csv (map #(encrypt-line key %)) in-filename in-encoding in-separator)
      (write-csv out-filename out-with-bom? out-encoding out-separator)))

(defn decrypt-file
  [key in-filename out-filename
   {:keys [in-encoding in-separator out-with-bom? out-encoding out-separator]
    :or {in-encoding "UTF-8"
         in-separator \,
         out-with-bom? false
         out-encoding "UTF-8"
         out-separator \,}}]
  (let [decrypt (map-indexed #(decrypt-line-indexed key %1 %2))
        lines (read-csv decrypt in-filename in-encoding in-separator)]
    (if (some ::anom/category lines)
      (let [{::anom/keys [message] ::keys [line-index]} (first (filter map? lines))]
        (println "Problem in line" (str (inc line-index) ":"))
        (println "    " message))
      (-> lines
          (write-csv out-filename out-with-bom? out-encoding out-separator)))))

(def cli-options
  [["-k" "--key KEY" "32-byte hex encoded key"]
   ["-e" "--encrypt"]
   ["-d" "--decrypt"]
   ["-g" "--gen-key"]
   [nil "--in-encoding ENCODING" :default "UTF-8"]
   [nil "--in-separator SEPARATOR" :default \, :default-desc "(default \\,)"
    :parse-fn first]
   [nil "--in-tab-separated" "Input file is tab separated"]
   [nil "--out-separator SEPARATOR" :default \, :default-desc "(default \\,)"
    :parse-fn first]
   [nil "--out-tab-separated" "Output file should be tab separated"]
   [nil "--out-optimize-win" "Optimize the output for Office 2010+"]
   ["-v" "--version"]
   ["-h" "--help"]])

(defn print-version []
  (println "csv-crypt version 0.4")
  (System/exit 0))

(defn print-help [summary exit]
  (println "Usage: csv-crypt [-g]"
           "[-e -k key in-file out-file]"
           "[-d -k key in-file out-file]")
  (println summary)
  (System/exit exit))

(defn- out-encoding [{:keys [out-optimize-win]}]
  (if out-optimize-win "UTF-16LE" "UTF-8"))

(defn- out-separator
  [{:keys [out-separator out-tab-separated out-optimize-win]}]
  (if (or out-tab-separated out-optimize-win)
    \tab
    out-separator))

(defn -main [& args]
  (let [{{:keys [key encrypt decrypt gen-key
                 in-encoding in-separator in-tab-separated
                 out-optimize-win
                 version help]
          :as options} :options
         [in-filename out-filename] :arguments
         :keys [summary]}
        (cli/parse-opts args cli-options)
        in-separator (if in-tab-separated \tab in-separator)
        out-encoding (out-encoding options)
        out-separator (out-separator options)]
    (when version
      (print-version))
    (when help
      (print-help summary 0))
    (when (or encrypt decrypt)
      (when-not (and key in-filename out-filename)
        (print-help summary 1)))
    (cond
      encrypt
      (encrypt-file (codecs/hex->bytes key) in-filename out-filename
                    {:in-encoding in-encoding
                     :in-separator in-separator
                     :out-with-bom? out-optimize-win
                     :out-encoding out-encoding
                     :out-separator out-separator})
      decrypt
      (decrypt-file (codecs/hex->bytes key) in-filename out-filename
                    {:in-encoding in-encoding
                     :in-separator in-separator
                     :out-with-bom? out-optimize-win
                     :out-encoding out-encoding
                     :out-separator out-separator})
      gen-key
      (println (codecs/bytes->hex (nonce/random-bytes 32)))
      :else
      (print-help summary 1))))
