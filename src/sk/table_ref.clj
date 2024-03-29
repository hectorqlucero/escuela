(ns sk.table_ref
  (:require [sk.models.crud :refer [Query db]]
            [sk.models.util :refer [current_time current_year get-imagen parse-int]]))

;; Start get_users
(def get_users-sql
  "SELECT id AS value, concat(firstname,' ',lastname) AS text FROM users order by firstname,lastname")

(defn get-users
  "Regresa todos los usuarios o vacio ex: (get-users)"
  []
  (Query db [get_users-sql]))
;; End get_users

;; Start get-users-email
(def get-users-email-sql
  "SELECT
   email
   FROM users
   WHERE email = ?")

(defn get-users-email
  "Regresa el correo del usuario o nulo"
  [email]
  (first (Query db [get-users-email-sql email])))
;; End get-users-email

(defn months
  "Regresa un arreglo de meses en español ex: (months)"
  []
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

(defn years
  "Genera listado para dropdown dependiendo de p=anterioriores de este año, n=despues de este año,
   ex: (years 5 4)"
  [p n]
  (let [year   (parse-int (current_year))
        pyears (for [n (range (parse-int p) 0 -1)] {:value (- year n) :text (- year n)})
        nyears (for [n (range 0 (+ (parse-int n) 1))] {:value (+ year n) :text (+ year n)})
        years  (concat pyears nyears)]
    years))

;; Start get-categorias
(def get-categorias-sql
  "SELECT id AS value, categoria AS text FROM categorias ORDER BY categoria")

(defn get-categorias []
  (Query db [get-categorias-sql]))
;; End get-categorias

(defn imagen [table field idname id]
  (get-imagen table field idname id))

(defn clock []
  (current_time))

(defn get-alumno [matricula]
  (:matricula (first (Query db ["SELECT matricula FROM alumnos WHERE matricula = ?" matricula]))))
