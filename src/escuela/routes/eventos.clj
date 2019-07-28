(ns escuela.routes.eventos
  (:require [cheshire.core :refer [generate-string]]
            [noir.response :refer [redirect]]
            [noir.session :as session]
            [selmer.parser :refer [render-file]]
            [escuela.models.crud :refer [db Query Save Update]]
            [escuela.models.util :refer [get-matricula-id
                                    current_time_internal
                                    today-internal]]))

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
   WHERE CURDATE() BETWEEN eventos.fecha_inicio AND eventos.fecha_terminacion")

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

(defn eventos [request]
  (let [title "Eventos"
        matricula (get-matricula-id)
        row (first (Query db eventos-sql))
        erows (Query db [registro_evento-sql matricula])]
    (render-file "routes/eventos.html" {:title title
                                        :matricula matricula
                                        :row row
                                        :erows erows})))
;; End eventos

;; Start processar
(def matricula-sql
  "SELECT
   *
   FROM registro_evento
   WHERE
   eventos_id = ?
   AND
   matricula_id = ?")

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

(defn crear [matricula_id eventos_id]
  "Crear un record en la tabla registro_evento"
  (let [postvars {:matricula_id (str matricula_id)
                  :eventos_id (str eventos_id)
                  :hora_entrada (current_time_internal)
                  :fecha (today-internal)}
        result (Save db :registro_evento postvars ["matricula_id = ? AND eventos_id = ?" (str matricula_id) (str eventos_id)])]
    result))

(defn actualizar [matricula_id eventos_id start-exists]
  "Actualizar un record en la tabla registro_evento cuando existen datos"
  (let [postvars (if (nil? start-exists)
                   {:hora_entrada (current_time_internal)}
                   {:hora_salida (current_time_internal)})
        result (Update db :registro_evento postvars ["matricula_id = ? AND eventos_id = ?" (str matricula_id) (str eventos_id)])]
    result))

(defn processar [matricula_id eventos_id]
  "Processar datos de los eventos y crear/modificar records o regresar un error"
  (let [row (first (Query db [matricula-sql eventos_id matricula_id]))
        data-exists (matricula-exists matricula_id eventos_id)
        start-exists (evento-start-check row)
        valid-alumno (valid-matricula matricula_id)
        error (if (nil? valid-alumno)
                "Matricula no existe!"
                "Fallo registro!")
        result (if-not (nil? valid-alumno)
                 (do
                   (if (nil? data-exists)
                     (crear matricula_id eventos_id)
                     (actualizar matricula_id eventos_id start-exists)))
                 nil)]
    (if (seq result)
      (generate-string {:success "Registro processado!"})
      (generate-string {:error error}))))
;; End processar
