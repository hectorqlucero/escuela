(ns sk.models.uploads
  (:require [clojure.java.io :as io]
            [sk.models.crud :refer [config]]))

(def UPLOADS (str (config :uploads) "/es/"))

(defn upload-photo [file matricula]
  (let [tempfile  (:tempfile file)
        size      (:size file)
        type      (:content-type file)
        extension (peek (clojure.string/split type #"\/"))
        extension (if (= extension "jpeg") "jpg" "jpg")
        foto      (str matricula "." extension)
        result    (if-not (zero? size)
                    (do (io/copy tempfile (io/file (str UPLOADS foto)))))]
    foto))
