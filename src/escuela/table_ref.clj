(ns escuela.table_ref
  (:require [cheshire.core :refer [generate-string]]
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

(defroutes table_ref-routes
  (GET "/table_ref/get_users" [] (generate-string (Query db [get_users-sql])))
  (GET "/table_ref/get_categorias" [] (generate-string (Query db [get-categorias-sql])))
  (GET "/table_ref/get_imagen/:id" [id] (get-imagen "eventos" "imagen" "id" id))
  (GET "/table_ref/alumnos/:matricula" [matricula] (generate-string (get-alumno matricula)))
  (GET "/table_ref/months" [] (generate-string (months)))
  (GET "/table_ref/clock" [] (current_time))
  (GET "/table_ref/years/:pyears/:nyears" [pyears nyears] (generate-string (years pyears nyears))))