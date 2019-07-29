(ns escuela.table_ref
  (:require [cheshire.core :refer [generate-string]]
            [selmer.parser :refer [render-file]]
            [escuela.models.crud :refer [db Query]]
            [escuela.models.util :refer [parse-int 
                                         current_year 
                                         current_time
                                         get-imagen]]
            [compojure.core :refer [defroutes GET]]))

(def get_users-sql
  "SELECT id AS value, concat(firstname,' ',lastname) AS text FROM users order by firstname,lastname")

(def get-categorias-sql
  "SELECT id AS value, categoria AS text FROM categorias order by categoria")

(defn get-alumno [matricula]
  (:matricula (first (Query db ["SELECT matricula FROM alumnos WHERE matricula = ?" matricula]))))

(defn get-users [email]
  (:email (first (Query db ["SELECT email FROM users WHERE email = ?" email]))))

(defn months []
  (list
   {:value 1 :text "Enero"}
   {:value 2 :text "Febrero"}
   {:value 3 :text "Marzo"}
   {:value 4 :text "Abril"}
   {:value 5 :text "Mayo"}
   {:value 6 :text "Junio"}
   {:value 7 :text "Julio"}
   {:value 8 :text "Agosto"}
   {:value 9 :text "Septiembre"}
   {:value 10 :text "Octubre"}
   {:value 11 :text "Noviembre"}
   {:value 12 :text "Diciembre"}))

(defn years [p n]
  (let [year   (parse-int (current_year))
        pyears (for [n (range (parse-int p) 0 -1)] {:value (- year n) :text (- year n)})
        nyears (for [n (range 0 (+ (parse-int n) 1))] {:value (+ year n) :text (+ year n)})
        years  (concat pyears nyears)]
    years))

;; Start get-registrados
(def get-registrados-sql
  "SELECT
   alumnos.nombre,
   alumnos.apell_paterno,
   alumnos.apell_materno,
   eventos.titulo,
   DATE_FORMAT(registro_evento.fecha, '%d/%m/%Y') as fecha,
   TIME_FORMAT(registro_evento.hora_entrada,'%H:%i %p') as hora_entrada,
   TIME_FORMAT(registro_evento.hora_salida,'%H:%i %p') as hora_salida
   FROM registro_evento
   JOIN alumnos on alumnos.matricula = registro_evento.matricula_id
   JOIN eventos on eventos.id = registro_evento.eventos_id
   WHERE
   registro_evento.eventos_id = ?")

(defn get-registrados [eventos_id]
  (render-file "admin/reventos.html" {:title "Registrados"
                                      :rows (Query db [get-registrados-sql eventos_id])}))
;; End get-registrados

(defroutes table_ref-routes
  (GET "/table_ref/get_users" [] (generate-string (Query db [get_users-sql])))
  (GET "/table_ref/get_categorias" [] (generate-string (Query db [get-categorias-sql])))
  (GET "/table_ref/get_imagen/:id" [id] (get-imagen "eventos" "imagen" "id" id))
  (GET "/table_ref/alumnos/:matricula" [matricula] (generate-string (get-alumno matricula)))
  (GET "/table_ref/users/:email" [email] (get-users email))
  (GET "/table_ref/months" [] (generate-string (months)))
  (GET "/table_ref/clock" [] (current_time))
  (GET "/table_ref/years/:pyears/:nyears" [pyears nyears] (generate-string (years pyears nyears)))
  (GET "/table_ref/get_registrados/:eventos_id" [eventos_id] (get-registrados eventos_id)))
