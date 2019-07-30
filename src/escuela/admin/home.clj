(ns escuela.admin.home
  (:require [cheshire.core :refer [generate-string]]
            [escuela.models.crud :refer [db
                                         Query
                                         Update
                                         Save]]
            [escuela.models.email :refer [host
                                          send-email]]
            [escuela.models.util :refer [get-session-id
                                         get-reset-url-admin
                                         capitalize-words
                                         check-token
                                         create-token]]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.response :refer [redirect]]
            [selmer.parser :refer [render-file]]))

(defn main [request]
  (render-file "admin/main.html" {:title "Bienvenido!"}))

;; Start registrar
(defn registrar [request]
  (if (get-session-id)
    (render-file "404.html" {:error "Existe una session, no se puede crear un nuevo Usuario."
                             :return-url "/admin"})
    (render-file "admin/registrar.html" {:title "Registro De usuario"
                                         :ok (get-session-id)})))

(defn registrar! [{params :params}]
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
      (generate-string {:url "/admin/login"})
      (generate-string {:error "No se pudo registrar el usuario!"}))))
;; End registrar

;; Start reset-password
(defn email-body [row url]
  (let [nombre       (str (:firstname row) " " (:lastname row))
        email        (:email row)
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
                      :to      email
                      :subject subject
                      :body    [{:type    "text/html;charset=utf-8"
                                 :content content}]}]
    body))

(defn reset-password [request]
  (if (get-session-id)
    (render-file "404.html" {:error "Existe una session, no se puede cambiar la contraseña"
                             :ok (get-session-id)
                             :return-url "/admin"})
    (render-file "admin/rpaswd.html" {:title "Resetear Contraseña"})))

(defn reset-password! [request]
  (let [params     (:params request)
        username   (:email params)
        token      (create-token username)
        url        (get-reset-url-admin request token)
        row        (first (Query db ["SELECT * FROM users WHERE username = ?" username]))
        email-body (email-body row url)]
    (if (future (send-email host email-body))
      (generate-string {:url "/admin"})
      (generate-string {:error "No se pudo resetear su contraseña"}))))

(defn reset-jwt [token]
  (let [username (check-token token)]
    (if-not (nil? username)
      (render-file "admin/reset_password.html" {:title "Resetear Contraseña"
                                                :row (generate-string {:username username})})
      (render-file "404.html" {:title "Resetear Contraseña"
                               :return-url "/admin"
                               :error "Su token es incorrecto o ya expiro!"}))))

(defn reset-jwt! [{params :params}]
  (let [username (or (:username params) "0")
        postvars  {:username username
                   :password  (crypt/encrypt (:password params))}
        result    (Update db :users postvars ["username = ?" username])]
    (if (seq result)
      (generate-string {:url "/admin"})
      (generate-string {:error "No se pudo cambiar su contraseña!"}))))
;; End reset-password

;; Start login
(defn login [_]
  (if-not (nil? (get-session-id))
    (redirect "/admin")
    (render-file "routes/login.html" {:title "Accesar al Sitio!"})))

(defn login! [username password]
  (let [row    (first (Query db ["select * from users where username = ?" username]))
        active (:active row)]
    (if (= active "T")
      (do
        (if (crypt/compare password (:password row))
          (do
            (session/put! :user_id (:id row))
            (generate-string {:url "/admin"}))
          (generate-string {:error "Hay problemas para accesar el sitio!"})))
      (generate-string {:error "El usuario esta inactivo!"}))))
;; End login

(defn logoff []
  (session/clear!)
  (redirect "/admin"))
