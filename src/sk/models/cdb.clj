(ns sk.models.cdb
  (:require [sk.models.crud :refer :all]
            [noir.util.crypt :as crypt]))

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
   status char(1) DEFAULT NULL COMMENT 'E=estudiante, G=Egresado',
   fecha_ingreso date DEFAULT NULL,
   fecha_egreso date DEFAULT NULL,
   email varchar(100) DEFAULT NULL,
   foto varchar(100) DEFAULT NULL,
   PRIMARY KEY (matricula)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")

(defn create-database []
  "Creates database and a default admin users"
  (Query! db users-sql)
  (Insert-multi db :users user-rows)
  (Query! db alumnos-sql))

(defn reset-database []
  "Removes existing tables and re-creates them"
  (Query! db "DROP table IF EXISTS users")
  (Query! db users-sql)
  (Insert-multi db :users user-rows)
  (Query! db "DROP table IF EXISTS alumnos")
  (Query! db alumnos-sql))

(defn migrate []
  "Migrate by the seat of my pants"
  (Query! db "DROP table IF EXISTS users")
  (Query! db users-sql)
  (Insert-multi db :users user-rows))

;;(create-database)
