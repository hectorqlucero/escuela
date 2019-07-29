(ns escuela.admin.eventos
  (:require [cheshire.core :refer [generate-string]]
            [escuela.models.crud :refer [db Query Save config]]
            [escuela.models.grid :refer :all]
            [escuela.models.util :refer [get-session-id
                                         parse-int
                                         get-imagen
                                         capitalize-words
                                         format-date-internal]]
            [clojure.java.io :as io]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.response :refer [redirect]]
            [selmer.parser :refer [render-file]]))

(defn crear-eventos [request]
  (render-file "admin/eventos.html" {:title "Mantenimiento de Eventos"
                                     :path (str (:path config) "eventos/")
                                     :ok (get-session-id)}))

;; Start eventos grid
(def search-columns
  ["id"
   "imagen"
   "titulo"
   "lugar"
   "DATE_FORMAT(fecha_inicio,'%d/%m/%Y')"
   "TIME_FORMAT(hora_inicio,'%h:%i %p')"
   "DATE_FORMAT(fecha_terminacion,'%d/%m/%Y')"
   "TIME_FORMAT(hora_terminacion,'%h:%i %p')"])

(def aliases-columns
  ["id"
   "imagen"
   "titulo"
   "lugar"
   "DATE_FORMAT(fecha_inicio,'%d/%m/%Y') as fecha_inicio"
   "TIME_FORMAT(hora_inicio,'%h:%i %p') as hora_inicio"
   "DATE_FORMAT(fecha_terminacion,'%d/%m/%Y') as fecha_terminacion"
   "TIME_FORMAT(hora_terminacion,'%h:%i %p') as hora_terminacion"])

(defn grid-eventos [{params :params}]
  (try
    (let [table "eventos"
          scolumns (convert-search-columns search-columns)
          aliases aliases-columns
          join ""
          search (grid-search (:search params nil) scolumns)
          order (grid-sort (:sort params nil) (:order params nil))
          offset (grid-offset (parse-int (:rows params)) (parse-int (:page params)))
          sql (grid-sql table aliases join search order offset)
          rows (grid-rows table aliases join search order offset)]
      (generate-string rows))
    (catch Exception e (.getMessage e))))
;; End eventos grid

;; Start eventos form
(def eventos-form-sql
  "SELECT id as id,
   imagen,
   categorias_id,
   descripcion,
   DATE_FORMAT(fecha_inicio,'%m/%d/%Y') as fecha_inicio,
   TIME_FORMAT(hora_inicio,'%H:%i') as hora_inicio,
   lugar,
   DATE_FORMAT(fecha_terminacion,'%m/%d/%Y') as fecha_terminacion,
   TIME_FORMAT(hora_terminacion,'%H:%i') as hora_terminacion,
   titulo
   FROM eventos
   WHERE id = ?")

(defn form-eventos [id]
  (try
    (let [row (first (Query db [eventos-form-sql id]))]
      (generate-string row))
    (catch Exception e (.getMessage e))))
;; End eventos form

;; Start Save form
(def UPLOADS (str (config :uploads) "/es/eventos/"))

(defn upload-imagen [file id]
  (let [tempfile  (:tempfile file)
        size      (:size file)
        type      (:content-type file)
        extension (peek (clojure.string/split type #"\/"))
        extension (if (= extension "jpeg") "jpg" "jpg")
        foto      (:filename file)
        result    (if-not (zero? size)
                    (do (io/copy tempfile (io/file (str UPLOADS foto)))))]
    foto))

(defn create-data [params]
  {:categorias_id (:categorias_id params)
   :descripcion (:descripcion params)
   :fecha_inicio (format-date-internal (:fecha_inicio params))
   :hora_inicio (:hora_inicio params)
   :lugar (:lugar params)
   :titulo (:titulo params)
   :fecha_terminacion (format-date-internal (:fecha_terminacion params))
   :hora_terminacion (:hora_terminacion params)})

(defn form-eventos! [{params :params}]
  (let [id (or (:id params) "0")
        file (:file params)
        imagen (if-not (zero? (:size file))
                 (upload-imagen file id)
                 (:imagen params))
        postvars (assoc (create-data params) :id id :imagen imagen)
        result (Save db :eventos postvars ["id = ?" id])]
    (if (seq result)
      (generate-string {:success "Record processado correctamente!"})
      (generate-string {:error "Error, no se pudo processar el record!"}))))
;; End Save form

;; Start activar eventos
(def activar-eventos-sql
  "SELECT
   id,
   titulo,
   descripcion,
   lugar,
   DATE_FORMAT(fecha_inicio,'%d/%m/%Y') as fecha_inicio,
   TIME_FORMAT(hora_inicio,'%h:%i %p') as hora_inicio,
   DATE_FORMAT(fecha_terminacion,'%d/%m/%Y') as fecha_terminacion,
   TIME_FORMAT(hora_terminacion,'%h:%i %p') as hora_terminacion
   FROM eventos
   ORDER BY fecha_inicio, hora_inicio")

(defn activar-eventos [_]
  (render-file "admin/aeventos.html" {:title "Activar Eventos"
                                      :rows (Query db activar-eventos-sql)
                                      :path (str (:path config) "eventos/")
                                      :epath "/eventos/"
                                      :ok (get-session-id)}))
;; End activar eventos

;; Start registrados eventos
(def registrados-sql
  "SELECT
   alumnos.nombre,
   alumnos.apell_paterno,
   alumnos.apell_materno,
   eventos.titulo,
   DATE_FORMAT(registro_evento.fecha, '%d/%m/%Y') as fecha,
   TIME_FORMAT(registro_evento.hora_entrada,'%h:%i %p') as hora_entrada,
   TIME_FORMAT(registro_evento.hora_salida,'%h:%i %p') as hora_salida
   FROM registro_evento
   JOIN alumnos on alumnos.matricula = registro_evento.matricula_id
   JOIN eventos on eventos.id = registro_evento.eventos_id
   WHERE
   registro_evento.eventos_id = ?")

(defn registrados-eventos [eventos_id]
  (render-file "admin/reventos.html" {:title "Registrados"
                                      :rows (Query db [registrados-sql eventos_id])}))
;; End registrados eventos
