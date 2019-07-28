(ns escuela.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [escuela.table_ref :refer [table_ref-routes]]
            [escuela.routes.home :as home]
            [escuela.routes.registro :as registro]
            [escuela.routes.eventos :as eventos]))

(defroutes escuela-routes
  table_ref-routes
  (GET "/" request [] (registro/buscar request))
  (POST "/" request [] (registro/buscar! request))
  (GET "/admin" request [] (home/main request))
  (GET "/admin/login" request [] (home/login request))
  (POST "/admin/login" [username password] (home/login! username password))
  (GET "/admin/logoff" request [] (home/logoff request))
  (GET "/registrar" request [] (registro/registrar request))
  (POST "/registrar" request [] (registro/registrar! request))
  (GET "/matricula/:matricula" [matricula] (registro/matricula matricula))
  (POST "/matricula" request [] (registro/matricula! request))
  (GET "/rpaswd" request [] (registro/reset-password request))
  (POST "/rpaswd" request [] (registro/reset-password! request))
  (GET "/reset_password" request [] (registro/reset-jwt! request))
  (POST "/reset_password" request [] (registro/reset-jwt! request))
  (GET "/r_alumnos" request [] (registro/r-alumnos request))
  (GET "/eventos" request [] (eventos/eventos request))
  (GET "/eventos/processar/:matricula_id/:eventos_id" [matricula_id eventos_id] (eventos/processar matricula_id eventos_id))
  (GET "/logoff" request [] (registro/logoff request)))
