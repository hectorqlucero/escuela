(ns sk.proutes.alumnos.matricula
  (:require [cheshire.core :refer (generate-string)]
            [clojure.java.io :as io]
            [noir.session :as session]
            [noir.response :refer [redirect]]
            [selmer.parser :refer [render-file]]
            [sk.models.uploads :refer [upload-photo]]
            [sk.models.crud :refer [db config Query Save]]
            [sk.models.util :refer [get-photo
                                    parse-int
                                    get-matricula-id
                                    format-date-internal
                                    capitalize-words]]))

;; Start matricula
(def matricula-sql
  "SELECT
   matricula,
   password,
   apell_paterno,
   apell_materno,
   nombre,
   escuela,
   carrera,
   semestre,
   status,
   DATE_FORMAT(fecha_ingreso, '%m/%d/%Y') as fecha_ingreso,
   DATE_FORMAT(fecha_egreso, '%m/%d/%Y') as fecha_egreso,
   email
   FROM alumnos
   WHERE matricula = ? ")

(defn matricula [matricula]
  (if (= (parse-int matricula) (parse-int (get-matricula-id)))
    (do
      (let [row (first (Query db [matricula-sql matricula]))]
        (render-file "sk/proutes/alumnos/matricula/matricula.html"
                     {:title "Registro De Alumno"
                      :matricula matricula
                      :foto (get-photo "alumnos" "foto" "matricula" matricula)
                      :row (generate-string row)})))
    (redirect "/")))
;; End matricula

;; Start matricula!
(defn create-data [params]
  {:matricula     (:matricula params)
   :password      (:password params)
   :apell_paterno (capitalize-words (:apell_paterno params))
   :apell_materno (capitalize-words (:apell_materno params))
   :nombre        (capitalize-words (:nombre params))
   :escuela       (clojure.string/upper-case (:escuela params))
   :carrera       (capitalize-words (:carrera params))
   :semestre      (:semestre params)
   :status        (:status params)
   :fecha_ingreso (format-date-internal (:fecha_ingreso params))
   :fecha_egreso  (format-date-internal (:fecha-egreso params))
   :email         (clojure.string/lower-case (:email params))})

(defn matricula! [{params :params}]
  (let [matricula (or (:matricula params) "0")
        file      (:file params)
        foto      (upload-photo file matricula)
        postvars  (assoc (create-data params) :matricula matricula :foto foto)
        result    (Save db :alumnos postvars ["matricula = ?" matricula])]
    (if (seq result)
      (do
        (session/put! :matricula matricula)
        (session/put! :is_authenticated true)
        (generate-string {:url (str "/alumnos/matricula/" matricula)}))
      (generate-string {:error "No se pudo actualizar el Registro!"}))))
;; End matricula!
