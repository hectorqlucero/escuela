(ns sk.models.cdb
  (:require [sk.models.crud :refer [Query! db Insert-multi]]
            [noir.util.crypt :as crypt]))

;; Start users table
(def users-sql
  "CREATE TABLE users (
  id int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  lastname varchar(45) DEFAULT NULL,
  firstname varchar(45) DEFAULT NULL,
  username varchar(45) DEFAULT NULL,
  password TEXT DEFAULT NULL,
  dob varchar(45) DEFAULT NULL,
  cell varchar(45) DEFAULT NULL,
  phone varchar(45) DEFAULT NULL,fax varchar(45) DEFAULT NULL,
  email varchar(100) DEFAULT NULL,
  level char(1) DEFAULT NULL COMMENT 'A=Administrador,U=Usuario,S=Sistema',
  active char(1) DEFAULT NULL COMMENT 'T=Active,F=Not active'
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
   matricula int(11) unsigned NOT NULL PRIMARY KEY,
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
   foto varchar(100) DEFAULT NULL
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")
;; End alumnos table

;; Start categorias table
(def categorias-sql
  "CREATE TABLE categorias (
   id int(11) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
   categoria varchar(100) DEFAULT NULL
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
   id int(11) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
   categorias_id int(11) unsigned NOT NULL,
   descripcion text DEFAULT NULL,
   fecha_inicio date DEFAULT NULL,
   hora_inicio time DEFAULT NULL,
   imagen varchar(100) DEFAULT NULL,
   lugar varchar(100) DEFAULT NULL,
   titulo varchar(250) DEFAULT NULL,
   fecha_terminacion date DEFAULT NULL,
   hora_terminacion time DEFAULT NULL,
   total_horas int(11) DEFAULT NULL,
   total_porciento int(11) DEFAULT NULL,
   CONSTRAINT `fk_eventos_categorias`
   FOREIGN KEY (categorias_id) REFERENCES categorias (id)
   ON DELETE CASCADE
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")
;; End eventos table

;; Start registro_evento table
(def registro_evento-sql
  "CREATE TABLE registro_evento (
   id int(11) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
   matricula_id int(11) unsigned NOT NULL,
   eventos_id int(11) unsigned NOT NULL,
   hora_entrada time DEFAULT NULL,
   hora_salida time DEFAULT NULL,
   fecha date DEFAULT NULL,
   CONSTRAINT `fk_registro_evento_alumnos`
   FOREIGN KEY (matricula_id) REFERENCES alumnos (matricula)
   ON DELETE CASCADE,
   CONSTRAINT `fk_registro_evento_eventos`
   FOREIGN KEY (eventos_id) REFERENCES eventos (id)
   ON DELETE CASCADE
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")
;; End registro_evento table

;; Start registro_correos table
(def registro_correos-sql
  "CREATE TABLE `registro_correos` (
   `id` int unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
   `matricula_id` int unsigned NOT NULL,
   `eventos_id` int unsigned NOT NULL,
   `hora_mandar` time DEFAULT NULL,
   `hora_recibir` time DEFAULT NULL,
   `fecha` date DEFAULT NULL,
   KEY `fk_registro_correos_alumnos` (`matricula_id`),
   KEY `fk_registro_correos_eventos` (`eventos_id`),
   CONSTRAINT `fk_registro_correos_alumnos` FOREIGN KEY (`matricula_id`) REFERENCES `alumnos` (`matricula`) ON DELETE CASCADE,
   CONSTRAINT `fk_registro_correos_eventos` FOREIGN KEY (`eventos_id`) REFERENCES `eventos` (`id`) ON DELETE CASCADE
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8")

(def registro_correos-rows
  [
   {:matricula_id 3333
    :eventos_id 2
    :hora_mandar "8:00:00"
    :hora_recibir "08:10:00"
    :fecha "2021-09-28"}
   {:matricula_id 3333
    :eventos_id 2
    :hora_mandar "8:30:00"
    :hora_recibir "8:32:00"
    :fecha "2021-09-28"}
   {:matricula_id 3333
    :eventos_id 2
    :hora_mandar "9:00:00"
    :hora_recibir "9:40:00"
    :fecha "2021-09-28"}
   {:matricula_id 3333
    :eventos_id 2
    :hora_mandar "9:30:00"
    :hora_recibir "9:42:00"
    :fecha "2021-9-28"}
   {:matricula_id 3333
    :eventos_id 2
    :hora_mandar "10:00:00"
    :hora_recibir "10:03:00"
    :fecha "2021-09-28"}
   {:matricula_id 3333
    :eventos_id 2
    :hora_mandar "10:30:00"
    :hora_recibir "10:34:00"
    :fecha "2021-09-28"}
   {:matricula_id 3333
    :eventos_id 2
    :hora_mandar "11:00:00"
    :hora_recibir "11:15:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 2
    :hora_mandar "8:00:00"
    :hora_recibir "08:10:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 2
    :hora_mandar "8:30:00"
    :hora_recibir "8:32:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 2
    :hora_mandar "9:00:00"
    :hora_recibir "9:40:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 2
    :hora_mandar "9:30:00"
    :hora_recibir "9:52:00"
    :fecha "2021-9-28"}
   {:matricula_id 10615
    :eventos_id 2
    :hora_mandar "10:00:00"
    :hora_recibir "10:05:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 2
    :hora_mandar "10:30:00"
    :hora_recibir "10:34:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 2
    :hora_mandar "11:00:00"
    :hora_recibir "11:25:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 1
    :hora_mandar "8:00:00"
    :hora_recibir "08:30:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 1
    :hora_mandar "8:30:00"
    :hora_recibir "8:32:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 1
    :hora_mandar "9:00:00"
    :hora_recibir "9:40:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 1
    :hora_mandar "9:30:00"
    :hora_recibir "9:42:00"
    :fecha "2021-9-28"}
   {:matricula_id 10615
    :eventos_id 1
    :hora_mandar "10:00:00"
    :hora_recibir "10:03:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 1
    :hora_mandar "10:30:00"
    :hora_recibir "10:34:00"
    :fecha "2021-09-28"}
   {:matricula_id 10615
    :eventos_id 1
    :hora_mandar "11:00:00"
    :hora_recibir "11:15:00"
    :fecha "2021-09-28"}
   {:matricula_id 1163580
    :eventos_id 2
    :hora_mandar "8:00:00"
    :hora_recibir "08:10:00"
    :fecha "2021-09-28"}
   {:matricula_id 1163580
    :eventos_id 2
    :hora_mandar "8:30:00"
    :hora_recibir "8:32:00"
    :fecha "2021-09-28"}
   {:matricula_id 1163580
    :eventos_id 2
    :hora_mandar "9:00:00"
    :hora_recibir "9:05:00"
    :fecha "2021-09-28"}
   {:matricula_id 1163580
    :eventos_id 2
    :hora_mandar "9:30:00"
    :hora_recibir "9:32:00"
    :fecha "2021-9-28"}
   {:matricula_id 1163580
    :eventos_id 2
    :hora_mandar "10:00:00"
    :hora_recibir "10:05:00"
    :fecha "2021-09-28"}
   {:matricula_id 1163580
    :eventos_id 2
    :hora_mandar "10:30:00"
    :hora_recibir "10:34:00"
    :fecha "2021-09-28"}
   {:matricula_id 1163580
    :eventos_id 2
    :hora_mandar "11:00:00"
    :hora_recibir "11:05:00"
    :fecha "2021-09-28"}
   ])
;; End registro_correos table

(defn drop-tables
  "Drops tables if they exist"
  []
  (Query! db "DROP table IF EXISTS registro_evento")
  (Query! db "DROP table if EXISTS registro_correos")
  (Query! db "DROP table if EXISTS eventos")
  (Query! db "DROP table if EXISTS alumnos")
  (Query! db "DROP table if EXISTS categorias")
  (Query! db "DROP table if EXISTS users"))

(defn create-tables
  "Creates tables"
  []
  (Query! db users-sql)
  (Query! db categorias-sql)
  (Query! db alumnos-sql)
  (Query! db eventos-sql)
  (Query! db registro_evento-sql)
  (Query! db registro_correos-sql))

(defn populate-tables
  "Populates tables with default data"
  []
  (Insert-multi db :users user-rows)
  (Insert-multi db :categorias categorias-rows))

(defn reset-database
  "Re-create database"
  []
  (drop-tables)
  (create-tables)
  (populate-tables))

(defn testor []
  (Insert-multi db :registro_correos registro_correos-rows))

(comment
  (testor)
  (reset-database))
