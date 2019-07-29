(ns escuela.admin.home
  (:require [cheshire.core :refer [generate-string]]
            [escuela.models.crud :refer [db
                                         Query
                                         Save]]
            [escuela.models.util :refer [get-session-id
                                         capitalize-words
                                         check-token
                                         create-token]]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [noir.response :refer [redirect]]
            [selmer.parser :refer [render-file]]))

(defn main [request]
  (render-file "admin/main.html" {:title "Bienvenido!"}))

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

(defn logoff []
  (session/clear!)
  (redirect "/admin"))
