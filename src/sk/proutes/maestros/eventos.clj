(ns sk.proutes.maestros.eventos
  (:require [cheshire.core :refer [generate-string]]
            [clojure.java.io :as io]
            [hiccup.core :refer [html]]
            [noir.response :refer [redirect]]
            [selmer.parser :refer [render-file]]
            [sk.models.crud :refer [Delete Insert Query Save Update config db]]
            [sk.models.email :refer [host send-email]]
            [sk.models.grid :refer [convert-search-columns grid-offset grid-rows grid-search grid-sort]]
            [sk.models.util :refer [current_date current_time_internal format-date-internal get-matricula-id get-session-id parse-int today-internal]]))

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

(defn padron [_]
  (if-not (nil? (get-session-id))
    (let [title "Padron de Alumnos"
          rows (Query db padron-sql)]
      (render-file "sk/proutes/maestros/padron.html" {:title title
                                                      :rows rows
                                                      :ok (get-session-id)}))
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
    (let [title "Eventos"
          etime (:email_seconds config)
          matricula (get-matricula-id)
          row (first (Query db [eventos-sql eventos_id]))
          erows (Query db [registro_evento-sql matricula])]
      (render-file "sk/proutes/maestros/peventos.html" {:title title
                                                        :matricula matricula
                                                        :etime etime
                                                        :path (str (:path config) "eventos/")
                                                        :row row
                                                        :erows erows}))
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

(defn valid-matricula
  "Revisar si existe un alumno con la matricula especificiada"
  [matricula_id]
  (let [record (first (Query db ["SELECT matricula FROM alumnos WHERE matricula = ?" matricula_id]))
        result (if (seq record) 1 nil)]
    result))

(defn matricula-exists
  "Revisar si existen datos pertinentes en la tabla registro_evento"
  [matricula_id eventos_id]
  (if (seq (first (Query db [matricula-sql eventos_id matricula_id]))) 1 nil))

(defn evento-start-check
  "Revisar si ya hay un dato de hora_entrada en la tabla registro_evento"
  [row]
  (if-not (nil? (:hora_entrada row)) 1 nil))

(defn evento-end-check
  "Revisar si ya hay un dato de hora_salida en la tabla registro_evento"
  [row]
  (if-not (nil? (:hora_salida row)) 1 nil))

(defn crear
  "Crear un record en la tabla registro_evento"
  [matricula_id eventos_id]
  (let [postvars {:matricula_id (str matricula_id)
                  :eventos_id (str eventos_id)
                  :hora_entrada (current_time_internal)
                  :fecha (today-internal)}
        result (Insert db :registro_evento postvars)]
    result))

(defn actualizar
  "Actualizar un record en la tabla registro_evento cuando existen datos o crear un nuevo record"
  [matricula_id registro_evento_id eventos_id start-exists end-exists]
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

(defn processar
  "Processar datos de los eventos y crear/modificar records o regresar un error"
  [matricula_id eventos_id]
  (if-not (nil? (get-session-id))
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
                   (if (nil? data-exists)
                     (crear matricula_id eventos_id)
                     (actualizar matricula_id registro_evento_id eventos_id start-exists end-exists))
                   nil)]
      (if (seq result)
        (generate-string {:success (str alumno-nombre " - Registro de " status " processado!")})
        (generate-string {:error error})))
    (redirect "/")))
;; End processar

(defn crear-eventos [_]
  (if-not (nil? (get-session-id))
    (render-file "sk/proutes/maestros/eventos.html" {:title "Mantenimiento de Eventos"
                                                     :path (str (:path config) "eventos/")
                                                     :ok (get-session-id)})
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
    (try
      (let [table "eventos"
            scolumns (convert-search-columns search-columns)
            aliases aliases-columns
            join ""
            search (grid-search (:search params nil) scolumns)
            order (grid-sort (:sort params nil) (:order params nil))
            offset (grid-offset (parse-int (:rows params)) (parse-int (:page params)))
            rows (grid-rows table aliases join search order offset)]
        (generate-string rows))
      (catch Exception e (.getMessage e)))
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
    (try
      (let [row (first (Query db [eventos-form-sql id]))]
        (generate-string row))
      (catch Exception e (.getMessage e)))
    (redirect "/")))
;; End eventos form

;; Start Save form
(def UPLOADS (str (config :uploads) "/es/eventos/"))

(defn upload-imagen [file _]
  (let [tempfile  (:tempfile file)
        size      (:size file)
        foto      (:filename file)
        result    (when-not (zero? size)
                    (io/copy tempfile (io/file (str UPLOADS foto))))]
    (if result foto result)))

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
    (let [id (or (:id params) "0")
          file (:file params)
          imagen (if-not (zero? (:size file))
                   (upload-imagen file id)
                   (:imagen params))
          postvars (assoc (create-data params) :id id :imagen imagen)
          result (Save db :eventos postvars ["id = ?" id])]
      (if (seq result)
        (generate-string {:success "Record processado correctamente!"})
        (generate-string {:error "Error, no se pudo processar el record!"})))
    (redirect "/")))
;; End Save form

(defn borrar-evento! [{params :params}]
  (let [id (:id params)]
    (Delete db :eventos ["id = ?" id])
    (generate-string {:success "Removido appropiadamente"})))

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
    (render-file "sk/proutes/maestros/aeventos.html" {:title "Activar Eventos"
                                                      :rows (Query db activar-eventos-sql)
                                                      :path (str (:path config) "eventos/")
                                                      :epath "/eventos/"
                                                      :ok (get-session-id)})
    (redirect "/")))
;; End activar eventos

;; Start registrados eventos
(def registrados-sql
  "SELECT
   alumnos.matricula,
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
    (render-file "sk/proutes/maestros/reventos.html" {:title "Registrados"
                                                      :rows (Query db [registrados-sql eventos_id])})
    (redirect "/")))
;; End registrados eventos

;; Start resultados
(def resultados_evento-sql
  "SELECT
   alumnos.matricula,
   alumnos.nombre,
   alumnos.apell_paterno,
   alumnos.apell_materno,
   eventos.titulo,
   DATE_FORMAT(registro_evento.fecha,'%d/%m/%Y') AS fecha,
   DATE_FORMAT(eventos.fecha_inicio,'%d/%m/%Y') AS inicia,
   DATE_FORMAT(eventos.fecha_terminacion,'%d/%m%Y') AS termina,
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
    (render-file "sk/proutes/maestros/resultados.html" {:title "Resultados"
                                                        :rows (Query db [resultados_evento-sql eventos_id])})
    (redirect "/")))
;; End resultados

(defn aprovados-eventos [eventos_id]
  (if-not (nil? (get-session-id))
    (let [trows (Query db [resultados_evento-sql eventos_id])
          rows (map #(when (= (:ok %) 1) %) trows)]
      (render-file "sk/proutes/maestros/resultados.html" {:title "Aprovados"
                                                          :rows (remove nil? rows)}))
    (redirect "/")))

(defn reprovados-eventos [eventos_id]
  (if-not (nil? (get-session-id))
    (let [trows (Query db [resultados_evento-sql eventos_id])
          rows (map #(when (= (:ok %) 0) %) trows)]
      (render-file "sk/proutes/maestros/resultados.html" {:title "Reprovados"
                                                          :rows (remove nil? rows)}))
    (redirect "/")))

;; Start correos-mandar
(defn build-correos-email-body [id matricula_id]
  (let [row (first (Query db ["select * from alumnos where matricula = ?" matricula_id]))
        nombre (str (:nombre row) " " (:apell_paterno row) " " (:apell_materno row))
        alumno-email (:email row)
        subject (str "Hola " nombre ", por favor hacer click en el link del correo!")
        url0 (str (:base-url config) "maestros/correos/recibir/" id)
        url (str "<a href='" url0 "'>Clic aqui para confirmar asistencia...</a>")
        body {:from "escueladeodontologiamxli@fastmail.com"
              :to alumno-email
              :subject subject
              :body [{:type "text/html;charset=utf-8"
                      :content (str "<strong>Hola</strong> " nombre "</br>" url)}]}]
    body))

(defn get-hora-mandar []
  (->> (Query db (str "select TIME_TO_SEC('" (current_time_internal) "') as hora_mandar"))
       first
       :hora_mandar))


(defn check-evento-dates [eventos_id]
  (let [row (first (Query db [(str "select TO_DAYS(fecha_inicio) as fecha_inicio,
                                   TO_DAYS(fecha_terminacion) as fecha_terminacion,
                                   TO_DAYS(NOW()) as fecha_hoy
                                   FROM eventos WHERE id = ?") eventos_id]))
        fecha_inicio (:fecha_inicio row)
        fecha_terminacion (:fecha_terminacion row)
        fecha_hoy (:fecha_hoy row)]
    (if (and (>= fecha_hoy fecha_inicio)
             (<= fecha_hoy fecha_terminacion)) 1 0)))

(defn get-hora-tabla [eventos_id]
  (let [hora_tabla (->> (Query db ["select TIME_TO_SEC(hora_terminacion) as hora_terminacion from eventos where id = ?" eventos_id])
                        first
                        :hora_terminacion)]
    hora_tabla))

(defn check-correos-time [eventos_id]
  (let [hora_tabla (get-hora-tabla eventos_id)
        hora_mandar (get-hora-mandar)]
    [hora_mandar hora_tabla]
    (if (> hora_tabla hora_mandar) 1 0)))

(defn process-email [matricula_id eventos_id]
  (let [hora_mandar (current_time_internal)
        fecha (format-date-internal (current_date))
        rows {:matricula_id matricula_id
              :eventos_id eventos_id
              :hora_mandar hora_mandar
              :fecha fecha}
        result (Save db :registro_correos rows ["eventos_id = ? and matricula_id = ? and hora_mandar = ? and fecha = ?" eventos_id matricula_id hora_mandar fecha])
        r-correos-id (or (:generated_key (first result)) nil)
        email-body (when r-correos-id (build-correos-email-body r-correos-id matricula_id))]
    (when email-body (send-email host email-body))))

(defn correos-mandar [matricula_id eventos_id]
  (when (and (= (check-correos-time eventos_id) 1)
             (= (check-evento-dates eventos_id) 1))
    (process-email matricula_id eventos_id)))
;; End correos-mandar

(defn correos-recibir [id]
  (let [hora-recibir (current_time_internal)
        row {:hora_recibir hora-recibir}
        result (Update db :registro_correos row ["id = ? AND hora_recibir IS NULL" id])
        message (if (= result 1) 
                  "Se verifico su correo, puede cerrar la pagina!!!"
                  "No se actualizo su verificación. Posibles causas es que ya se actualizo anteriormente, puede cerrar la pagina!!!")]
    (render-file "sk/proutes/maestros/recibir.html" {:message message})))

;; Start correos-eventos
(def totals-sql
  "
  SELECT
  COUNT(hora_recibir) as mandados,
  SUM(TIMEDIFF(hora_recibir,hora_mandar)) / 100 as diferencia
  FROM registro_correos
  WHERE 
  matricula_id = ?
  and eventos_id = ?
  GROUP BY matricula_id
  ")

(def correos-sql
  "
  SELECT
  r.matricula_id,
  r.eventos_id,
  CONCAT(a.nombre,' ',a.apell_paterno,' ',a.apell_materno) as nombre,
  DATE_FORMAT(r.fecha,'%d/%c/%Y') as fecha
  FROM registro_evento r
  JOIN alumnos a on a.matricula = r.matricula_id
  WHERE 
  r.eventos_id = ?
  and r.matricula_id = ?
  limit 1
  ")

(defn process-totals [row]
  (let [matricula_id (:matricula_id row)
        eventos_id (:eventos_id row)
        trow (first (Query db [totals-sql matricula_id eventos_id]))
        mandados (:mandados trow)
        row (assoc row 
                   :mandados  mandados 
                   :diferencia (first (clojure.string/split (str (:diferencia trow)) #"\.")))]
    row))

(defn process-correos-eventos [row]
  (let [matricula_id  (:matricula_id row)
        eventos_id (:evento-id row)
        r-rows (Query db [correos-sql eventos_id matricula_id])
        rows (map process-totals r-rows)]
    rows))

(defn correos-eventos [eventos-id]
  (if-not (nil? (get-session-id))
    (let [r-rows (Query db ["select distinct matricula_id from registro_evento where eventos_id = ?" eventos-id])
          r-rows (map #(assoc % :evento-id eventos-id) r-rows)
          rows (flatten (map process-correos-eventos r-rows))]
      (render-file "sk/proutes/maestros/correos.html" {:title "Correos"
                                                       :rows rows
                                                       :ok (get-session-id)}))))
;; End correos-eventos


(comment
  (->> (Query db (str "select DAYOFYEAR('" (format-date-internal (current_date)) "') as doy"))
       first
       :doy)
  (check-evento-dates "9")
  (check-correos-time "9")
  (get-hora-tabla "9")
  (get-hora-mandar)
  (current_time_internal)
  (correos-eventos 2)
  (correos-mandar "111111" "9")
  (correos-recibir "586"))
