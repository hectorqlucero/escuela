(ns sk.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [sk.table_ref :refer [table_ref-routes]]
            [sk.routes.home :as home]
            [sk.routes.registro :as registro]))

(defroutes sk-routes
  table_ref-routes
  (GET "/" request [] (registro/buscar request))
  (POST "/" request [] (registro/buscar! request))
  (GET "/registrar" request [] (registro/registrar request))
  (POST "/registrar" request [] (registro/registrar! request))
  (GET "/matricula/:matricula" [matricula] (registro/matricula matricula))
  (POST "/matricula" request [] (registro/matricula! request))
  (GET "/rpaswd" request [] (registro/reset-password request))
  (POST "/rpaswd" request [] (registro/reset-password! request))
  (GET "/reset_password" request [] (registro/reset-jwt! request))
  (POST "/reset_password" request [] (registro/reset-jwt! request))
  (GET "/r_alumnos" request [] (registro/r-alumnos request))
  (GET "/logoff" request [] (registro/logoff request)))
