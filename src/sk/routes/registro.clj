(ns sk.routes.registro
  (:require [cheshire.core :refer [generate-string]]
            [noir.response :refer [redirect]]
            [noir.session :as session]
            [selmer.parser :refer [render-file]]
            [sk.models.crud :refer [db Query Save]]
            [sk.models.util :refer [format-date-internal get-matricula-id capitalize-words]]))

(defn buscar [request]
  (render-file "routes/buscar.html" {:title "Busqueda de Registro"
                                     :matricula (get-matricula-id)}))

(defn buscar! [{params :params}]
  (let [matricula (or (:matricula params) "0")
        password  (:password params)
        result    (Query db ["SELECT matricula FROM alumnos WHERE matricula = ? AND password = ?" matricula password])]
    (if (seq result)
      (do
        (session/put! :matricula matricula)
        (session/put! :is_authenticated true)
        (generate-string {:url (str "/matricula/" matricula)}))
      (generate-string {:url "/"}))))

(defn registrar [request]
  (if (get-matricula-id)
    (redirect "/")
    (render-file "routes/registrar.html" {:title "Registro De Alumno"
                                          :matricula nil})))

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

(defn registrar! [{params :params}]
  (let [matricula (or (:matricula params) "0")
        postvars  (assoc (create-data params) :matricula matricula)
        result    (Save db :alumnos postvars ["matricula = ?" matricula])]
    (if (seq result)
      (do
        (session/put! :matricula matricula)
        (session/put! :is_authenticated true)
        (generate-string {:url "/"}))
      (generate-string {:error "No se pudo actualizar el Registro!"}))))

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
  (let [row (first (Query db [matricula-sql matricula]))]
    (render-file "routes/matricula.html" {:title "Registro De Alumno"
                                          :matricula matricula
                                          :row (generate-string row)})))
;; End matricula

(defn matricula! [{params :params}]
  (let [matricula (or (:matricula params) "0")
        postvars  (assoc (create-data params) :matricula matricula)
        result    (Save db :alumnos postvars ["matricula = ?" matricula])]
    (if (seq result)
      (do
        (session/put! :matricula matricula)
        (session/put! :is_authenticated true)
        (generate-string {:url "/"}))
      (generate-string {:error "No se pudo actualizar el Registro!"}))))
