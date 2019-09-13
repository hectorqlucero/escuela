(ns sk.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [cheshire.core :refer [generate-string]]
            [sk.table_ref :as table_ref]
            [sk.routes.alumnos.home :as alumnos-home]
            [sk.routes.alumnos.registrar :as alumnos-registrar]
            [sk.routes.maestros.home :as maestros-home]
            [sk.routes.maestros.registrar :as maestros-registrar]))

(defroutes open-routes
  ;; Start table_ref
  (GET "/table_ref/alumnos/:matricula" [matricula] (generate-string (table_ref/get-alumno matricula)))
  (GET "/table_ref/get_users" [] (generate-string (table_ref/get-users)))
  (GET "/table_ref/validate_email/:email" [email] (generate-string (table_ref/get-users-email email)))
  (GET "/table_ref/months" [] (generate-string (table_ref/months)))
  (GET "/table_ref/years/:pyears/:nyears" [pyears nyears] (generate-string (table_ref/years pyears nyears)))
  (GET "/table_ref/get_categorias" [] (generate-string (table_ref/get-categorias)))
  (GET "/table_ref/get_imagen/:id" [id] (table_ref/imagen "eventos" "imagen" "id" id))
  (GET "/table_ref/clock" [] (table_ref/clock))
  ;; End table_ref
  ;; Start alumnos-registrar
  (GET "/" request [] (alumnos-home/page-error request))
  (GET "/alumnos" request [] (alumnos-home/main request))
  (GET "/alumnos/login" request [] (alumnos-registrar/buscar request))
  (POST "/alumnos/login" request [] (alumnos-registrar/buscar! request))
  (GET "/alumnos/registrar" request [] (alumnos-registrar/registrar request))
  (POST "/alumnos/registrar" request [] (alumnos-registrar/registrar! request))
  (GET "/alumnos/rpaswd" request [] (alumnos-registrar/reset-password request))
  (POST "/alumnos/rpaswd" request [] (alumnos-registrar/reset-password! request))
  (GET "/alumnos/reset_password/:token" [token] (alumnos-registrar/reset-jwt token))
  (POST "/alumnos/reset_password" request [] (alumnos-registrar/reset-jwt! request))
  (GET "/alumnos/logoff" request [] (alumnos-registrar/logoff request))
  ;; End alumnos-registrar
  ;; Start maestros-home
  (GET "/maestros" request [] (maestros-home/main request))
  (GET "/maestros/login" request [] (maestros-home/login request))
  (POST "/maestros/login" [username password] (maestros-home/login! username password))
  (GET "/maestros/logoff" [] (maestros-home/logoff))
  ;; End maestros-home
  ;; Start maestros-registrar
  ;;(GET "/maestros/registrar" request [] (maestros-registrar/registrar request))
  ;;(POST "/maestros/registrar" request [] (maestros-registrar/registrar! request))
  (GET "/maestros/rpaswd" request [] (maestros-registrar/reset-password request))
  (POST "/maestros/rpaswd" request [] (maestros-registrar/reset-password! request))
  (GET "/maestros/reset_password/:token" [token] (maestros-registrar/reset-jwt token))
  (POST "/maestros/reset_password" request [] (maestros-registrar/reset-jwt! request))
  ;; End maestros-registrar
  )
