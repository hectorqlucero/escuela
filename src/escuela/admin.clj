(ns escuela.admin
  (:require [compojure.core :refer [defroutes GET POST]]
            [escuela.admin.eventos :as eventos]
            [escuela.routes.eventos :refer [eventos]]))

(defroutes admin-routes
  (GET "/eventos/:eventos_id" [eventos_id] (eventos eventos_id))
  (GET "/admin/ceventos" request [] (eventos/crear-eventos request))
  (POST "/admin/ceventos/json/grid" request [] (eventos/grid-eventos request))
  (GET "/admin/ceventos/json/form/:id" [id] (eventos/form-eventos id))
  (POST "/admin/ceventos/save" request [] (eventos/form-eventos! request))
  (GET "/admin/aeventos" request [] (eventos/activar-eventos request))
  (GET "/admin/reventos/:eventos_id" [eventos_id] (eventos/registrados-eventos eventos_id)))
