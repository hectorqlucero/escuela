(ns sk.routes.alumnos.home
  (:require [cheshire.core :refer [generate-string]]
            [sk.models.crud :refer [db Query]]
            [sk.models.util :refer [get-matricula-id]]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.response :refer [redirect]]
            [selmer.parser :refer [render-file]]))

(defn main [request]
  (render-file "sk/routes/alumnos/home/main.html" {:title "Hacer click en el menu \"Entrar\" para accessar el sitio!"
                                  :matricula (get-matricula-id)}))

(defn page-error [request]
  (render-file "escuela.html" {:error "No se encuentra la pagina que buscas!"}))
