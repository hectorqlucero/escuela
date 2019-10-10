(ns sk.routes.maestros.registrar
  (:require [cheshire.core :refer [generate-string]]
            [selmer.parser :refer [render-file]]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.response :refer [redirect]]
            [sk.models.crud :refer [db Query Save Update]]
            [sk.models.email :refer [host send-email]]
            [sk.models.util :refer [get-session-id
                                    get-reset-url
                                    capitalize-words
                                    check-token
                                    create-token]]))

;; Start registrar
(defn registrar [secret]
  "Registrar un nuevo usuario"
  (if (get-session-id)
    (render-file "404.html" {:error "Existe una session, no se puede crear un nuevo Usuario."
                             :return-url "/"})
    (do
      (if (= secret "elmo1200")
        (render-file "sk/routes/maestros/registrar/registrar.html" {:title "Registro De usuario"
                                                                    :ok (get-session-id)})
        (render-file "404.html" {:error "No se pudo crear el usuario!"
                                 :return-url "/"})))))

(defn registrar! [{params :params}]
  "Postear los datos de registro de un nuevo cliente el la tabla usuarios"
  (let [email (or (:email params) "0")
        postvars {:email (clojure.string/lower-case email)
                  :username (clojure.string/lower-case email)
                  :firstname (capitalize-words (:firstname params))
                  :lastname (capitalize-words (:lastname params))
                  :password (crypt/encrypt (:password params))
                  :level "U"
                  :active "T"}
        result (Save db :users postvars ["username = ?" email])]
    (if (seq result)
      (generate-string {:url "/maestros/login"})
      (generate-string {:error "No se pudo registrar el usuario"}))))
;; End registrar

;; Start reset-password
(defn reset-password [request]
  (if (get-session-id)
    (render-file "404.html" {:error "Existe una session, no se puede cambiar la contraseña"
                             :ok (get-session-id)
                             :return-url "/maestros"})
    (render-file "sk/routes/maestros/registrar/rpaswd.html" {:title "Resetear Contraseña"})))

;; Start reset-password!
(defn get-username-row [username]
  (first (Query db ["SELECT * FROM users WHERE username = ?" username])))

(defn email-body [row url]
  "Crear el cuerpo del email"
  (let [nombre       (str (:firstname row) " " (:lastname row))
        email        (:email row)
        subject      "Resetear tu contraseña"
        content      (str "<strong>Hola</strong> " nombre ",</br></br>"
                          "Para resetear tu contraseña <strong>" "<a href='" url "'>Clic Aqui</a>" "</strong>.</br></br>"
                          "Alternativamente, puedes copiar y pegar el siguiente link en la barra de tu browser:</br></br>"
                          url "</br></br>"
                          "Este link solo sera bueno por 50 minutos.</br></br>"
                          "Si no solicito resetear su contraseña simplemente ignore este mensage.</br></br></br>"
                          "Sinceramente,</br></br>"
                          "La administracion")
        body         {:from    "escueladeodontologiamxli@fastmail.com"
                      :to      email
                      :subject subject
                      :body    [{:type    "text/html;charset=utf-8"
                                 :content content}]}]
    body))

(defn reset-password! [request]
  (let [params     (:params request)
        username   (:email params)
        token      (create-token username)
        url        (get-reset-url request "/maestros/reset_password/" token)
        row        (get-username-row username)
        email-body (email-body row url)]
    (if (future (send-email host email-body))
      (generate-string {:url "/maestros"})
      (generate-string {:error "No se pudo resetear su contraseña"}))))
;; End reset-password!

(defn reset-jwt [token]
  (let [username (check-token token)]
    (if-not (nil? username)
      (render-file "sk/routes/maestros/registrar/reset_password.html" {:title "Resetear Contraseña"
                                                              :row (generate-string {:username username})
                                                              :ok (get-session-id)})
      (render-file "404.html" {:title "Resetear Contraseña"
                               :return-url "/maestros"
                               :error "Su token es incorrecto o ya expiro!"}))))

(defn reset-jwt! [{params :params}]
  (let [username (or (:username params) "0")
        postvars  {:username username
                   :password  (crypt/encrypt (:password params))}
        result (Update db :users postvars ["username = ?" username])]
    (if (seq result)
      (generate-string {:url "/maestros"})
      (generate-string {:error "No se pudo cambiar su contraseña!"}))))
;; ENd reset-password

