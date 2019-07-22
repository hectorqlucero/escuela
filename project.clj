(defproject sk "0.1.0"
  :description "skeleton"
  :url "http://0.0.0.0"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [compojure "1.6.1" :exclusions [commons-code]]
                 [ring-server "0.5.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [clojure-csv/clojure-csv "2.0.2"]
                 [lib-noir "0.9.9"]
                 [com.draines/postal "2.0.3"]
                 [cheshire "5.8.1"]
                 [clj-pdf "2.3.5" exclusions [xalan commons-codec]]
                 [clj-jwt "0.1.1"]
                 [pdfkit-clj "0.1.7" :exclusions [commons-codec commons-logging]]
                 [clj-time "0.15.1"]
                 [date-clj "1.0.1"]
                 [clojurewerkz/money "1.10.0"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.clojure/data.codec "0.1.1"]
                 [mysql/mysql-connector-java "8.0.17"]
                 [selmer "1.12.12" :exclusions [commons-codec]]
                 [inflections "0.13.2" :exclusions [commons-codec]]]
  :main sk.handler
  :aot [sk.handler]
  :uberjar-name "sk.jar"
  :target-path "target/%s"
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler sk.handler/app
         :init sk.handler/init
         :destroy sk.handler/destroy}
  :resources-paths ["shared" "resources"]
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring/ring-mock "0.4.0"] [ring/ring-devel "1.7.1"]]}})
