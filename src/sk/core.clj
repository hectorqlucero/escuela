(ns sk.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as st]
            [compojure.core :refer [defroutes routes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.response :refer [redirect]]
            [noir.session :as session]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.reload :as reload]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [selmer.filters :refer [add-filter!]]
            [selmer.parser :refer [add-tag! set-resource-path!]]
            [sk.models.crud :refer [KEY Query config db]]
            [sk.models.util :refer [get-matricula-id get-session-id]]
            [sk.proutes :refer [proutes]]
            [sk.routes :refer [open-routes]]))

(set-resource-path! (io/resource "templates"))
(add-filter! :format-title (fn [x] [:safe (st/replace x #"'" "&#145;")]))
(add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(add-tag! :username
          (fn [_ _]
            (str (if (get-session-id) (:username (first (Query db ["select username from users where id=?" (get-session-id)]))) "Anonimo"))))
(add-tag! :site_name
          (fn [_ _]
            (str (:site-name config))))

(defn wrap-login [hdlr]
  (fn [req]
    (try
      (if (and
           (nil? (get-session-id))
           (nil? (get-matricula-id))) (redirect "/") (hdlr req))
      (catch Exception _
        {:status 400 :body "Unable to process your request!"}))))

(defn wrap-exception-handling [hdlr]
  (fn [req]
    (try
      (hdlr req)
      (catch Exception _
        {:status 400 :body "Invalid data"}))))

(defroutes public-routes
  open-routes)

(defroutes protected-routes
  proutes)

(defroutes app-routes
  (route/resources "/")
  (route/files "/uploads/" {:root (:uploads config)})
  (route/not-found "Not Found"))

(defn -main []
  (jetty/run-jetty
   (-> (routes
        public-routes
        (wrap-login protected-routes)
        (wrap-exception-handling protected-routes)
        app-routes)
       (handler/site)
       (wrap-session)
       (session/wrap-noir-session*)
       (wrap-multipart-params)
       (reload/wrap-reload)
       (wrap-defaults (-> site-defaults
                          (assoc-in [:security :anti-forgery] true)
                          (assoc-in [:session :store] (cookie-store {:key KEY}))
                          (assoc-in [:session :cookie-attrs] {:max-age 28800})
                          (assoc-in [:session :cookie-name] "LS"))))
   {:port (:port config)}))
