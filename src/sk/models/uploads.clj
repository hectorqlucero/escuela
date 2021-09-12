(ns sk.models.uploads
  (:require [clojure.java.io :as io]
            [clojure.string :as st]
            [sk.models.crud :refer [config]]))

(def UPLOADS (str (config :uploads) "/es/"))

(defn copy-file [size tempfile foto]
  (when-not (zero? size)
    (io/copy tempfile (io/file (str UPLOADS foto)))))

(defn upload-photo [file matricula]
  (let [tempfile  (:tempfile file)
        size      (:size file)
        type      (:content-type file)
        extension (peek (st/split type #"\/"))
        extension (if (= extension "jpeg") "jpg" "jpg")
        foto      (str matricula "." extension)]
    (copy-file size tempfile foto)
    foto))
