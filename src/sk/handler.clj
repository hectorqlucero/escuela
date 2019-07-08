(ns sk.handler
  (:gen-class)
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.adapter.jetty :as jetty]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.session :as session]
            [noir.util.middleware :as noir-middleware]
            [noir.util.anti-forgery :refer [anti-forgery-field]]
            [noir.response :refer [redirect]]
            [selmer.filters :refer :all]
            [selmer.parser :refer :all]
            [sk.models.crud :refer [config db KEY Query]]
            [sk.routes :refer [sk-routes]]
            [sk.admin :refer [admin-routes]]))

(set-resource-path! (clojure.java.io/resource "templates"))
(add-filter! :format-title (fn [x] [:safe (clojure.string/replace x #"'" "&#145;")]))
(add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(add-tag! :username
          (fn [_ _]
            (str (if (session/get :user_id) (:username (first (Query db ["select username from users where id=?" (session/get :user_id)]))) "Anonimo"))))
(add-tag! :site_name
          (fn [_ _]
            (str (:site-name config))))

(defn wrap-login [hdlr]
  (fn [req]
    (try
      (if (nil? (session/get :user_id)) (redirect "/") (hdlr req))
      (catch Exception _
        {:status 400 :body "Unable to process your request!"}))))

(defn wrap-exception-handling [hdlr]
  (fn [req]
    (try
      (hdlr req)
      (catch Exception _
        {:status 400 :body "Invalid data"}))))

(defn init []
  (println "sk is starting"))

(defn destroy []
  (println "sk is shutting down"))

(defn user-page [_]
  (session/get :user_id))

(defroutes public-routes
  sk-routes)

(defroutes protected-routes
  admin-routes)

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (noir-middleware/app-handler
    [public-routes
     (wrap-login protected-routes)
     (wrap-exception-handling protected-routes)
     app-routes]
    :access-rules [user-page]))

(defn -main []
  (jetty/run-jetty app {:port (:port config)}))
