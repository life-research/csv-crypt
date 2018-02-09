(defproject csv-crypt "0.1"
  :description "Line-by-line CSV Encryption Tool"
  :url "https://github.com/life-research/csv-crypt"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"
  :pedantic? :abort

  :dependencies
  [[buddy/buddy-core "1.4.0"]
   [cheshire "5.8.0"]
   [clj-bom "0.1.2"]
   [org.clojure/clojure "1.9.0"]
   [org.clojure/data.csv "0.1.4"]
   [org.clojure/tools.cli "0.3.5"]]

  :profiles
  {:uberjar
   {:main csv-crypt.core
    :aot :all}})
