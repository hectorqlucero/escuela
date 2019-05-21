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
  (GET "/logoff" request [] (home/logoff request)))