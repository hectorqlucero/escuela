(ns sk.proutes
  (:require [compojure.core :refer [GET POST defroutes]]
            [sk.proutes.alumnos.matricula :as alumnos-matricula]
            [sk.proutes.maestros.eventos :as maestros-eventos]))

(defroutes proutes
  ;; Start alumnos-matricula
  (GET "/alumnos/matricula/:matricula" [matricula] (alumnos-matricula/matricula matricula))
  (POST "/alumnos/matricula" request [] (alumnos-matricula/matricula! request))
  ;; End alumnos-matricula
  ;; Start maestros-eventons
  (GET "/eventos/:eventos_id" [eventos_id] (maestros-eventos/eventos eventos_id))
  (GET "/eventos/processar/:matricula_id/:eventos_id" [matricula_id eventos_id] (maestros-eventos/processar matricula_id eventos_id))
  (GET "/maestros/padron" request [] (maestros-eventos/padron request))
  (GET "/maestros/ceventos" request [] (maestros-eventos/crear-eventos request))
  (POST "/maestros/ceventos/json/grid" request [] (maestros-eventos/grid-eventos request))
  (GET "/maestros/ceventos/json/form/:id" [id] (maestros-eventos/form-eventos id))
  (POST "/maestros/ceventos/save" request [] (maestros-eventos/form-eventos! request))
  (POST "/maestros/ceventos/delete" request [] (maestros-eventos/borrar-evento! request))
  (GET "/maestros/aeventos" request [] (maestros-eventos/activar-eventos request))
  (GET "/maestros/reventos/:eventos_id" [eventos_id] (maestros-eventos/registrados-eventos eventos_id))
  (GET "/maestros/aprovados/:eventos_id" [eventos_id] (maestros-eventos/aprovados-eventos eventos_id))
  (GET "/maestros/reprovados/:eventos_id" [eventos_id] (maestros-eventos/reprovados-eventos eventos_id))
  (GET "/maestros/resultados/:eventos_id" [eventos_id] (maestros-eventos/resultados-eventos eventos_id))
  ;; End maestros-eventos
  )
