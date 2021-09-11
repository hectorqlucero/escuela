(ns sk.routes.alumnos.home
  (:require [selmer.parser :refer [render-file]]
            [sk.models.util :refer [get-matricula-id]]))

(defn main [_]
  (render-file "sk/routes/alumnos/home/main.html" {:title "Hacer click en el menu \"Entrar\" para accessar el sitio!"
                                                   :matricula (get-matricula-id)}))

(defn page-error [_]
  (render-file "escuela.html" {:error "No se encuentra la pagina que buscas!"}))
