(defproject csv-crypt "0.6-SNAPSHOT"
  :description "Line-by-line CSV Encryption Tool"
  :url "https://github.com/life-research/csv-crypt"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"
  :pedantic? :abort

  :dependencies
  [[buddy/buddy-core "1.5.0"]
   [cheshire "5.8.1"]
   [clj-bom "0.1.2"]
   [com.cognitect/anomalies "0.1.12"]
   [org.bouncycastle/bcpkix-jdk15on "1.61"]
   [org.bouncycastle/bcprov-jdk15on "1.61"]
   [org.clojure/clojure "1.10.0"]
   [org.clojure/data.csv "0.1.4"]
   [org.clojure/tools.cli "0.4.2"]]

  :profiles
  {:dev
   {:dependencies
    [[org.clojars.akiel/iota "0.1"]]}
   :uberjar
   {:main csv-crypt.core
    :aot :all}})
