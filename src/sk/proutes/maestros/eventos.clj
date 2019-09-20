(ns sk.proutes.maestros.eventos
  (:require [cheshire.core :refer [generate-string]]
            [sk.models.crud :refer [db Query Save Insert Update config]]
            [sk.models.grid :refer :all]
            [sk.models.util :refer [get-session-id
                                    get-matricula-id
                                    parse-int
                                    capitalize-words
                                    current_time_internal
                                    today-internal
                                    format-date-internal]]
            [clojure.java.io :as io]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.response :refer [redirect]]
            [selmer.parser :refer [render-file]]))

;; Start padron
(def padron-sql
  "SELECT
   matricula,
   apell_paterno,
   apell_materno,
   nombre,
   email
   FROM alumnos
   ORDER BY
   apell_paterno,
   apell_materno,
   nombre")

(defn padron [request]
  (if-not (nil? (get-session-id))
    (do
      (let [title "Padron de Alumnos"
            rows (Query db padron-sql)]
        (render-file "sk/proutes/maestros/padron.html" {:title title
                                                        :rows rows
                                                        :ok (get-session-id)})))
    (redirect "/")))
;; End padron

;; Start eventos
(def eventos-sql
  "SELECT
   eventos.id,
   categorias.categoria,
   eventos.descripcion,
   eventos.titulo,
   eventos.lugar,
   eventos.imagen,
   DATE_FORMAT(eventos.fecha_inicio,'%d/%m/%Y') as fecha_inicio,
   TIME_FORMAT(eventos.hora_inicio,'%r') as hora_inicio,
   DATE_FORMAT(eventos.fecha_terminacion,'%d/%m/%Y') as fecha_terminacion,
   TIME_FORMAT(eventos.hora_terminacion, '%r') as hora_terminacion
   FROM eventos
   JOIN categorias on categorias.id = eventos.categorias_id
   WHERE eventos.id = ?")

(def registro_evento-sql
  "SELECT
   registro_evento.id,
   registro_evento.matricula_id,
   registro_evento.eventos_id,
   TIME_FORMAT(registro_evento.hora_entrada, '%r') as hora_entrada,
   TIME_FORMAT(registro_evento.hora_salida, '%r') as hora_salida
   FROM registro_evento
   LEFT JOIN eventos on eventos.id = registro_evento.eventos_id
   WHERE registro_evento.matricula_id = ?")

(defn eventos [eventos_id]
  (if-not (nil? (get-session-id))
    (do
      (let [title "Eventos"
            matricula (get-matricula-id)
            row (first (Query db [eventos-sql eventos_id]))
            erows (Query db [registro_evento-sql matricula])]
        (render-file "sk/proutes/maestros/peventos.html" {:title title
                                                          :matricula matricula
                                                          :path (str (:path config) "eventos/")
                                                          :row row
                                                          :erows erows})))
    (redirect "/")))
;; End eventos

;; Start processar
(def matricula-sql
  "SELECT
   *
   FROM registro_evento
   WHERE
   eventos_id = ?
   AND
   matricula_id = ?
   ORDER BY id DESC
   LIMIT 1")

(defn valid-matricula [matricula_id]
  "Revisar si existe un alumno con la matricula especificiada"
  (let [record (first (Query db ["SELECT matricula FROM alumnos WHERE matricula = ?" matricula_id]))
        result (if (seq record) 1 nil)]
    result))

(defn matricula-exists [matricula_id eventos_id]
  "Revisar si existen datos pertinentes en la tabla registro_evento"
  (if (seq (first (Query db [matricula-sql eventos_id matricula_id]))) 1 nil))

(defn evento-start-check [row]
  "Revisar si ya hay un dato de hora_entrada en la tabla registro_evento"
  (if-not (nil? (:hora_entrada row)) 1 nil))

(defn evento-end-check [row]
  "Revisar si ya hay un dato de hora_salida en la tabla registro_evento"
  (if-not (nil? (:hora_salida row)) 1 nil))

(defn crear [matricula_id eventos_id]
  "Crear un record en la tabla registro_evento"
  (let [postvars {:matricula_id (str matricula_id)
                  :eventos_id (str eventos_id)
                  :hora_entrada (current_time_internal)
                  :fecha (today-internal)}
        result (Insert db :registro_evento postvars)]
    result))

(defn actualizar [matricula_id registro_evento_id eventos_id start-exists end-exists]
  "Actualizar un record en la tabla registro_evento cuando existen datos o crear un nuevo record"
  (let [postvars (if (nil? start-exists)
                   {:hora_entrada (current_time_internal)}
                   {:hora_salida (current_time_internal)})
        result (if (nil? end-exists)
                 (Update db :registro_evento postvars ["id = ?" (str registro_evento_id)])
                 (crear matricula_id eventos_id))]
    result))

(defn get-alumno-name [matricula_id]
  (let [row (first (Query db ["SELECT CONCAT(nombre,' ',apell_paterno,' ',apell_materno) AS alumno_nombre FROM alumnos WHERE matricula = ?" matricula_id]))
        alumno-nombre (:alumno_nombre row)]
    alumno-nombre))

(defn get-status [data-exists start-exists end-exists]
  (let [status (cond
                 (nil? data-exists) "Entrada"
                 (and (nil? start-exists)
                      (nil? end-exists)) "Entrada"
                 (and (= start-exists 1)
                      (nil? end-exists)) "Salida"
                 (and (= data-exists 1)
                      (= start-exists 1)
                      (= end-exists 1)) "Entrada")]
    status))

(defn processar [matricula_id eventos_id]
  "Processar datos de los eventos y crear/modificar records o regresar un error"
  (if-not (nil? (get-session-id))
    (do
      (let [row (first (Query db [matricula-sql eventos_id matricula_id]))
            registro_evento_id (:id row)
            alumno-nombre (get-alumno-name matricula_id)
            data-exists (matricula-exists matricula_id eventos_id)
            start-exists (evento-start-check row)
            end-exists (evento-end-check row)
            valid-alumno (valid-matricula matricula_id)
            status (get-status data-exists start-exists end-exists)
            error (if (nil? valid-alumno)
                    "Matricula no existe!"
                    "Fallo registro!")
            result (if-not (nil? valid-alumno)
                     (do
                       (if (nil? data-exists)
                         (crear matricula_id eventos_id)
                         (actualizar matricula_id registro_evento_id eventos_id start-exists end-exists)))
                     nil)]
        (if (seq result)
          (generate-string {:success (str alumno-nombre " - Registro de " status " processado!")})
          (generate-string {:error error}))))
    (redirect "/")))
;; End processar

(defn crear-eventos [request]
  (if-not (nil? (get-session-id))
    (do
      (render-file "sk/proutes/maestros/eventos.html" {:title "Mantenimiento de Eventos"
                                                       :path (str (:path config) "eventos/")
                                                       :ok (get-session-id)}))
    (redirect "/")))

;; Start eventos grid
(def search-columns
  ["id"
   "imagen"
   "titulo"
   "lugar"
   "DATE_FORMAT(fecha_inicio,'%d/%m/%Y')"
   "TIME_FORMAT(hora_inicio,'%h:%i %p')"
   "DATE_FORMAT(fecha_terminacion,'%d/%m/%Y')"
   "TIME_FORMAT(hora_terminacion,'%h:%i %p')"
   "total_horas"
   "total_porciento"])

(def aliases-columns
  ["id"
   "imagen"
   "titulo"
   "lugar"
   "DATE_FORMAT(fecha_inicio,'%d/%m/%Y') as fecha_inicio"
   "TIME_FORMAT(hora_inicio,'%h:%i %p') as hora_inicio"
   "DATE_FORMAT(fecha_terminacion,'%d/%m/%Y') as fecha_terminacion"
   "TIME_FORMAT(hora_terminacion,'%h:%i %p') as hora_terminacion"
   "total_horas"
   "total_porciento"])

(defn grid-eventos [{params :params}]
  (if-not (nil? (get-session-id))
    (do
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
    (redirect "/")))
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
   titulo,
   total_horas,
   total_porciento
   FROM eventos
   WHERE id = ?")

(defn form-eventos [id]
  (if-not (nil? (get-session-id))
    (do
      (try
        (let [row (first (Query db [eventos-form-sql id]))]
          (generate-string row))
        (catch Exception e (.getMessage e))))
    (redirect "/")))
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
   :fecha_terminacion (format-date-internal (:fecha_terminacion params))
   :hora_inicio (:hora_inicio params)
   :hora_terminacion (:hora_terminacion params)
   :lugar (:lugar params)
   :titulo (:titulo params)
   :total_horas (:total_horas params)
   :total_porciento (:total_porciento params)})

(defn form-eventos! [{params :params}]
  (if-not (nil? (get-session-id))
    (do
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
    (redirect "/")))
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
  (if-not (nil? (get-session-id))
    (do
      (render-file "sk/proutes/maestros/aeventos.html" {:title "Activar Eventos"
                                                        :rows (Query db activar-eventos-sql)
                                                        :path (str (:path config) "eventos/")
                                                        :epath "/eventos/"
                                                        :ok (get-session-id)}))
    (redirect "/")))
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
   registro_evento.eventos_id = ?
   ORDER BY 
   alumnos.apell_paterno,
   alumnos.apell_materno,
   alumnos.nombre,
   registro_evento.fecha,
   registro_evento.hora_entrada")

(defn registrados-eventos [eventos_id]
  (if-not (nil? (get-session-id))
    (do
      (render-file "sk/proutes/maestros/reventos.html" {:title "Registrados"
                                                        :rows (Query db [registrados-sql eventos_id])}))
    (redirect "/")))
;; End registrados eventos

;; Start resultados
(def resultados_evento-sql
  "SELECT
   alumnos.nombre,
   alumnos.apell_paterno,
   alumnos.apell_materno,
   eventos.titulo,
   DATE_FORMAT(registro_evento.fecha,'%d/%m/%Y') AS fecha,
   eventos.total_horas as horas,
   TIME_FORMAT(SEC_TO_TIME(SUM(TIME_TO_SEC(TIMEDIFF(registro_evento.hora_salida,registro_evento.hora_entrada)))),'%H:%i:%s') as asistencia,
   CONCAT(ROUND(((SUM(TIME_TO_SEC(TIMEDIFF(registro_evento.hora_salida,registro_evento.hora_entrada)))) * 100) / (total_horas * 60 * 60),0),' %') as porcentage,
   IF(ROUND(((SUM(TIME_TO_SEC(TIMEDIFF(registro_evento.hora_salida,registro_evento.hora_entrada)))) * 100) / (eventos.total_horas * 60 * 60),0) >= eventos.total_porciento,1,0) AS ok
   FROM registro_evento
   JOIN eventos on eventos.id = registro_evento.eventos_id
   JOIN alumnos on alumnos.matricula = registro_evento.matricula_id
   WHERE registro_evento.eventos_id = ?
   GROUP BY registro_evento.matricula_id
   ORDER BY 
   alumnos.apell_paterno,
   alumnos.apell_materno,
   alumnos.nombre")

(defn resultados-eventos [eventos_id]
  (if-not (nil? (get-session-id))
    (do
      (render-file "sk/proutes/maestros/resultados.html" {:title "Resultados"
                                                          :rows (Query db [resultados_evento-sql eventos_id])}))
    (redirect "/")))
;; End resultados

(defn aprovados-eventos [eventos_id]
  (if-not (nil? (get-session-id))
    (do
      (let [trows (Query db [resultados_evento-sql 1])
            rows (map #(if (= (:ok %) 1) %) trows)]
        (render-file "sk/proutes/maestros/resultados.html" {:title "Aprovados"
                                                            :rows (remove nil? rows)})))
    (redirect "/")))

(defn reprovados-eventos [eventos_id]
  (if-not (nil? (get-session-id))
    (do
      (let [trows (Query db [resultados_evento-sql 1])
            rows (map #(if (= (:ok %) 0) %) trows)]
        (render-file "sk/proutes/maestros/resultados.html" {:title "Reprovados"
                                                            :rows (remove nil? rows)})))
    (redirect "/")))
