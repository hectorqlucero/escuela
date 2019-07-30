(ns escuela.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [escuela.table_ref :refer [table_ref-routes]]
            [escuela.routes.home :as home]
            [escuela.admin.home :as ahome]
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
  (GET "/reset_password/:token" [token] (registro/reset-jwt! token))
  (POST "/reset_password" request [] (registro/reset-jwt! request))
  (GET "/r_alumnos" request [] (registro/r-alumnos request))
  (GET "/eventos/:eventos_id" [eventos_id] (eventos/eventos eventos_id))
  (GET "/eventos/processar/:matricula_id/:eventos_id" [matricula_id eventos_id] (eventos/processar matricula_id eventos_id))
  (GET "/admin/registrar" request [] (ahome/registrar request))
  (POST "/admin/registrar" request [] (ahome/registrar! request))
  (GET "/admin/rpaswd" request [] (ahome/reset-password request))
  (POST "/admin/rpaswd" request [] (ahome/reset-password! request))
  (GET "/admin/reset_password/:token" [token] (ahome/reset-jwt token))
  (POST "/admin/reset_password" request [] (ahome/reset-jwt! request))
  (GET "/logoff" request [] (registro/logoff request)))
