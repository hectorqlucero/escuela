(ns sk.routes.alumnos.registrar
  (:require [cheshire.core :refer [generate-string]]
            [noir.response :refer [redirect]]
            [noir.session :as session]
            [selmer.parser :refer [render-file]]
            [sk.models.uploads :refer [upload-photo]]
            [sk.models.crud :refer [db Query Save Update config]]
            [sk.models.email :refer [host send-email]]
            [sk.models.util
             :refer
             [capitalize-words
              check-token
              create-token
              format-date-internal
              get-matricula-id
              get-photo
              get-reset-url]]))

(defn buscar [request]
  (render-file "sk/routes/alumnos/registrar/buscar.html" {:title "Busqueda de Registro"
                                                          :matricula (get-matricula-id)}))

(defn buscar! [{params :params}]
  (let [matricula (or (:matricula params) "0")
        password  (:password params)
        result    (Query db ["SELECT matricula FROM alumnos WHERE matricula = ? AND password = ?" matricula password])]
    (if (seq result)
      (do
        (session/put! :matricula matricula)
        (session/put! :is_authenticated true)
        (generate-string {:url (str "/alumnos/matricula/" matricula)}))
      (generate-string {:url "/alumnos"}))))

(defn registrar [request]
  (if (get-matricula-id)
    (render-file "404.html" {:error "Existe una session, no se puede crear una nueva Matricula."
                             :return-url "/alumnos"})
    (render-file "sk/routes/alumnos/registrar/registrar.html" {:title "Registro De Alumno"
                                                               :matricula nil
                                                               :foto (get-photo "alumnos" "foto" "matricula" nil)})))

(defn create-data [params]
  {:matricula     (:matricula params)
   :password      (:password params)
   :apell_paterno (capitalize-words (:apell_paterno params))
   :apell_materno (capitalize-words (:apell_materno params))
   :nombre        (capitalize-words (:nombre params))
   :escuela       (clojure.string/upper-case (:escuela params))
   :carrera       (capitalize-words (:carrera params))
   :semestre      (:semestre params)
   :status        (:status params)
   :fecha_ingreso (format-date-internal (:fecha_ingreso params))
   :fecha_egreso  (format-date-internal (:fecha-egreso params))
   :email         (clojure.string/lower-case (:email params))})

(defn registrar! [{params :params}]
  (let [matricula (or (:matricula params) "0")
        file      (:file params)
        foto      (upload-photo file matricula)
        postvars  (assoc (create-data params) :matricula matricula :foto foto)
        result    (Save db :alumnos postvars ["matricula = ?" matricula])]
    (if (seq result)
      (do
        (session/put! :matricula matricula)
        (session/put! :is_authenticated true)
        (generate-string {:url (str "/alumnos/matricula/" matricula)}))
      (generate-string {:error "No se pudo actualizar el Registro!"}))))

;; Start reset-password
(defn email-body [row url]
  (let [nombre       (str (:nombre row) " " (:apell_paterno row) " " (:apell_materno row))
        alumno-email (:email row)
        subject      "Resetear tu contraseña"
        content      (str "<strong>Hola</strong> " nombre ",</br></br>"
                          "Para resetear tu contraseña <strong>" "<a href='" url "'>Clic Aqui</a>" "</strong>.</br></br>"
                          "Alternativamente, puedes copiar y pegar el siguiente link en la barra de tu browser:</br></br>"
                          url "</br></br>"
                          "Este link solo sera bueno por 10 minutos.</br></br>"
                          "Si no solicito resetear su contraseña simplemente ignore este mensage.</br></br></br>"
                          "Sinceramente,</br></br>"
                          "La administracion")
        body         {:from    "hectorqlucero@gmail.com"
                      :to      alumno-email
                      :subject subject
                      :body    [{:type    "text/html;charset=utf-8"
                                 :content content}]}]
    body))

(defn reset-password [request]
  (if (get-matricula-id)
    (render-file "404.html" {:error "Existe una session, no se puede cambiar la contraseña"
                             :return-url "/alumnos"})
    (render-file "sk/routes/alumnos/registrar/rpaswd.html" {:title "Resetear Contraseña"})))

(defn reset-password! [request]
  (let [params     (:params request)
        matricula  (:matricula params)
        token      (create-token matricula)
        url        (get-reset-url request "/alumnos/reset_password/" token)
        row        (first (Query db ["SELECT * FROM alumnos WHERE matricula = ?" matricula]))
        email-body (email-body row url)]
    (if (future (send-email host email-body))
      (generate-string {:url "/alumnos"})
      (generate-string {:error "No se pudo resetear su contraseña"}))))

(defn reset-jwt [token]
  (let [matricula (check-token token)]
    (if-not (nil? matricula)
      (render-file "sk/routes/alumnos/registrar/reset_password.html" {:title "Resetear Contraseña"
                                                                      :row   (generate-string {:matricula matricula})})
      (render-file "404.html" {:title "Resetear Contraseña"
                               :return-url "/alumnos"
                               :error "Su token es incorrecto o ya expiro!"}))))

(defn reset-jwt! [{params :params}]
  (let [matricula (or (:matricula params) "0")
        postvars  {:matricula matricula
                   :password  (:password params)}
        result    (Update db :alumnos postvars ["matricula = ?" matricula])]
    (if (seq result)
      (generate-string {:url "/alumnos"})
      (generate-string {:error "No se pudo cambiar su contraseña!"}))))
;; End reset-password

(defn logoff [_]
  (session/remove! :matricula)
  (session/remove! :is_authenticated)
  (redirect "/alumnos"))
