(ns sk.routes.maestros.home
  (:require [cheshire.core :refer [generate-string]]
            [noir.response :refer [redirect]]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [selmer.parser :refer [render-file]]
            [sk.models.crud :refer [Query db]]
            [sk.models.util :refer [get-session-id]]))

;; Start Main
(def main-sql
  "SELECT
   username
   FROM users
   WHERE id = ?")

(defn get-main-title []
  (let [id (get-session-id)
        title (if id
                (:username (first (Query db [main-sql id])))
                "Hacer clic en el menu \"Entrar\" para accessar el sitio")]
    title))

(defn main [_]
  (render-file "sk/routes/maestros/home/main.html" {:title (get-main-title)
                                                    :ok (get-session-id)}))
;; End main

(defn login [_]
  (if-not (nil? (get-session-id))
    (redirect "/")
    (render-file "sk/routes/maestros/home/login.html" {:title "Accesar al Sitio!"
                                                       :ok (get-session-id)})))

(defn login! [username password]
  (let [row    (first (Query db ["select * from users where username = ?" username]))
        active (:active row)]
    (if (= active "T")
      (if (crypt/compare password (:password row))
        (do
          (session/put! :user_id (:id row))
          (generate-string {:url "/maestros"}))
        (generate-string {:error "Hay problemas para accesar el sitio!"}))
      (generate-string {:error "El usuario esta inactivo!"}))))

(defn logoff []
  (session/remove! :user_id)
  (redirect "/maestros"))