(ns sk.models.cdb
  (:require [sk.models.crud :refer :all]
            [noir.util.crypt :as crypt]))

;; Start users table
(def users-sql
  "CREATE TABLE users (
  id int(11) NOT NULL AUTO_INCREMENT,
  lastname varchar(45) DEFAULT NULL,
  firstname varchar(45) DEFAULT NULL,
  username varchar(45) DEFAULT NULL,
  password TEXT DEFAULT NULL,
  dob varchar(45) DEFAULT NULL,
  cell varchar(45) DEFAULT NULL,
  phone varchar(45) DEFAULT NULL,fax varchar(45) DEFAULT NULL,
  email varchar(100) DEFAULT NULL,
  level char(1) DEFAULT NULL COMMENT 'A=Administrador,U=Usuario,S=Sistema',
  active char(1) DEFAULT NULL COMMENT 'T=Active,F=Not active',
  PRIMARY KEY (id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8")

(def user-rows
  [{:lastname  "Lucero"
    :firstname "Hector"
    :username  "hectorqlucero@gmail.com"
    :password  (crypt/encrypt "elmo1200")
    :dob       "1957-02-07"
    :email     "hectorqlucero@gmail.com"
    :level     "S"
    :active    "T"}
   {:lastname  "Pescador"
    :firstname "Marco"
    :username  "marcopescador@hotmail.com"
    :dob       "1968-10-04"
    :email     "marcopescador@hotmail.com"
    :password  (crypt/encrypt "10201117")
    :level     "S"
    :active    "T"}])
;; End users table

;; Start alumnos table
(def alumnos-sql
  "CREATE TABLE alumnos (
   matricula int(11) unsigned NOT NULL,
   password varchar(50) DEFAULT NULL,
   apell_paterno varchar(100) DEFAULT NULL,
   apell_materno varchar(100) DEFAULT NULL,
   nombre varchar(100) DEFAULT NULL,
   escuela varchar(100) DEFAULT NULL,
   carrera varchar(100) DEFAULT NULL,
   semestre int(11) DEFAULT NULL,
   status char(1) DEFAULT NULL COMMENT 'A=Alumno, E=Egresado',
   fecha_ingreso date DEFAULT NULL,
   fecha_egreso date DEFAULT NULL,
   email varchar(100) DEFAULT NULL,
   foto varchar(100) DEFAULT NULL,
   PRIMARY KEY (matricula)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")
;; End alumnos table

;; Start categorias table
(def categorias-sql
  "CREATE TABLE categorias (
   id int(11) unsigned NOT NULL AUTO_INCREMENT,
   categoria varchar(100) DEFAULT NULL,
   PRIMARY KEY (id)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")

(def categorias-rows
  [{:id 1
    :categoria "Capacitación"}
   {:id 2
    :categoria "Conferencias"}
   {:id 3
    :categoria "Cursos"}
   {:id 4
    :categoria "Platicas"}
   {:id 5
    :categoria "Talleres"}
   {:id 6
    :categoria "Tutorias"}])
;; End categorias table

;; Start eventos table
(def eventos-sql
  "CREATE TABLE eventos (
   id int(11) unsigned NOT NULL AUTO_INCREMENT,
   categorias_id int(11) DEFAULT NULL,
   descripcion text DEFAULT NULL,
   fecha_inicio date DEFAULT NULL,
   hora_inicio time DEFAULT NULL,
   imagen varchar(100) DEFAULT NULL,
   lugar varchar(100) DEFAULT NULL,
   titulo varchar(250) DEFAULT NULL,
   fecha_terminacion date DEFAULT NULL,
   hora_terminacion time DEFAULT NULL,
   PRIMARY KEY(id)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")
;; End eventos table

;; Start registro_evento table
(def registro_evento-sql
  "CREATE TABLE registro_evento (
   id int(11) unsigned NOT NULL AUTO_INCREMENT,
   matricula_id int(11) NOT NULL,
   eventos_id int(11) NOT NULL,
   hora_entrada time DEFAULT NULL,
   hora_salida time DEFAULT NULL,
   fecha date DEFAULT NULL,
   PRIMARY KEY(id)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")
;; End registro_evento table

(defn create-database []
  "Creates database tables and default admin users
   Note: First create the database on MySQL with any client"
  (Query! db users-sql)
  (Insert-multi db :users user-rows)
  (Query! db alumnos-sql)
  (Query! db categorias-sql)
  (Insert-multi db :categorias categorias-rows)
  (Query! db eventos-sql)
  (Query! db registro_evento-sql))

(defn reset-database []
  "Removes existing tables and re-creates them"
  (Query! db "DROP table IF EXISTS users")
  (Query! db users-sql)
  (Insert-multi db :users user-rows)
  (Query! db "DROP table IF EXISTS alumnos")
  (Query! db alumnos-sql)
  (Query! db "DROP table IF EXISTS categorias")
  (Query! db categorias-sql)
  (Insert-multi db :categorias categorias-rows)
  (Query! db "DROP table IF EXISTS eventos")
  (Query! db eventos-sql)
  (Query! db "DROP table IF EXISTS registro_evento")
  (Query! db registro_evento-sql))

(defn migrate []
  "Migrate by the seat of my pants"
  (Query! db "DROP table IF EXISTS users")
  (Query! db users-sql)
  (Insert-multi db :users user-rows))

;;(create-database)
