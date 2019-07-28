(ns escuela.admin
  (:require [compojure.core :refer [defroutes GET POST]]
            [escuela.admin.eventos :as eventos]))

(defroutes admin-routes
  (GET "/admin/ceventos" request [] (eventos/crear-eventos request))
  (POST "/admin/ceventos/json/grid" request [] (eventos/grid-eventos request))
  (GET "/admin/ceventos/json/form/:id" [id] (eventos/form-eventos id))
  (POST "/admin/ceventos/save" request [] (eventos/form-eventos! request)))
