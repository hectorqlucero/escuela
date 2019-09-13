(ns sk.models.cdb
  (:require [sk.models.crud :refer :all]
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
   ON UPDATE RESTRICT
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
   ON DELETE CASCADE
   ON UPDATE RESTRICT,
   CONSTRAINT `fk_registro_evento_eventos`
   FOREIGN KEY (eventos_id) REFERENCES eventos (id)
   ON DELETE CASCADE
   ON UPDATE RESTRICT
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")
;; End registro_evento table

(defn create-database []
  "Creates database tables and default admin users
   Note: First create the database on MySQL with any client"
  (Query! db users-sql)
  (Insert-multi db :users user-rows)
  (Query! db categorias-sql)
  (Insert-multi db :categorias categorias-rows)
  (Query! db alumnos-sql)
  (Query! db eventos-sql)
  (Query! db registro_evento-sql))

(defn reset-database []
  "Removes existing tables and re-creates them"
  (Query! db "DROP table IF EXISTS registro_evento")
  (Query! db "DROP table IF EXISTS eventos")
  (Query! db "DROP table IF EXISTS alumnos")
  (Query! db "DROP table IF EXISTS categorias")
  (Query! db "DROP table IF EXISTS users")
  (Query! db users-sql)
  (Insert-multi db :users user-rows)
  (Query! db categorias-sql)
  (Insert-multi db :categorias categorias-rows)
  (Query! db alumnos-sql)
  (Query! db eventos-sql)
  (Query! db registro_evento-sql))

(defn migrate []
  "Migrate by the seat of my pants"
  (Query! db "DROP table IF EXISTS registro_evento")
  (Query! db "DROP table IF EXISTS eventos")
  (Query! db "DROP table IF EXISTS alumnos")
  (Query! db alumnos-sql)
  (Query! db eventos-sql)
  (Query! db registro_evento-sql)
  (Insert db "alumnos" {:matricula "28787"
                        :password "elmo1200"
                        :apell_paterno "Pacas"
                        :apell_materno "Verdes"
                        :nombre "Pedro"
                        :escuela "CSULB"
                        :carrera "Programacion"
                        :semestre "8"
                        :status "A"
                        :email "pedropacas@servidor.com"})
  (Insert db "alumnos" {:matricula "28788"
                        :password "101117"
                        :apell_paterno "Pacas"
                        :apell_materno "Azules"
                        :nombre "Maria"
                        :escuela "UABC"
                        :carrera "Programacion"
                        :semestre "8"
                        :status "A"
                        :email "mariapacas@servidor.com"})
  (Insert db "alumnos" {:matricula "28789"
                        :password "101117"
                        :apell_paterno "Paredes"
                        :apell_materno "Robles"
                        :nombre "Adrian"
                        :escuela "UABC"
                        :carrera "Programacion"
                        :semestre "8"
                        :status "A"
                        :email "adrianpardes@servidor.com"})
  (Insert db "alumnos" {:matricula "28790"
                        :password "101117"
                        :apell_paterno "Pacas"
                        :apell_materno "Azules"
                        :nombre "Maribel"
                        :escuela "UABC"
                        :carrera "Programacion"
                        :semestre "8"
                        :status "A"
                        :email "maribelpacas@servidor.com"})
  (Insert db "alumnos" {:matricula "28791"
                        :password "101117"
                        :apell_paterno "Paredes"
                        :apell_materno "Robles"
                        :nombre "Mario"
                        :escuela "UABC"
                        :carrera "Programacion"
                        :semestre "8"
                        :status "A"
                        :email "mariopardes@servidor.com"})
  (Insert db "eventos" {:categorias_id "6"
                        :descripcion "Tutorias de Odontologia, para el desarollo del alumno"
                        :lugar "Vicerectoria Aula #1"
                        :titulo "Tutorias de Odontologia"
                        :fecha_inicio "2019-09-13"
                        :fecha_terminacion "2019-09-16"
                        :hora_inicio "08:00:00"
                        :hora_terminacion "18:00:00"
                        :total_horas "10"
                        :total_porciento "80"})
  (Insert db "registro_evento" {:matricula_id "28787"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "07:45:00"
                                :hora_salida "09:50:00"})
  (Insert db "registro_evento" {:matricula_id "28787"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "10:10:00"
                                :hora_salida "12:50:00"})
  (Insert db "registro_evento" {:matricula_id "28787"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "13:00:00"
                                :hora_salida "14:00:00"})
  (Insert db "registro_evento" {:matricula_id "28787"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "14:20:00"
                                :hora_salida "18:06:00"})

  (Insert db "registro_evento" {:matricula_id "28788"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "08:45:00"
                                :hora_salida "09:50:00"})
  (Insert db "registro_evento" {:matricula_id "28788"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "10:30:00"
                                :hora_salida "12:50:00"})
  (Insert db "registro_evento" {:matricula_id "28788"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "13:20:00"
                                :hora_salida "14:00:00"})
  (Insert db "registro_evento" {:matricula_id "28788"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "15:20:00"
                                :hora_salida "18:06:00"})

  (Insert db "registro_evento" {:matricula_id "28789"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "08:40:00"
                                :hora_salida "09:50:00"})
  (Insert db "registro_evento" {:matricula_id "28789"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "10:30:00"
                                :hora_salida "12:50:00"})
  (Insert db "registro_evento" {:matricula_id "28789"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "13:30:00"
                                :hora_salida "14:00:00"})
  (Insert db "registro_evento" {:matricula_id "28789"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "15:30:00"
                                :hora_salida "18:06:00"})

  (Insert db "registro_evento" {:matricula_id "28790"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "07:45:00"
                                :hora_salida "09:50:00"})
  (Insert db "registro_evento" {:matricula_id "28790"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "10:10:00"
                                :hora_salida "12:50:00"})
  (Insert db "registro_evento" {:matricula_id "28790"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "13:00:00"
                                :hora_salida "14:00:00"})
  (Insert db "registro_evento" {:matricula_id "28790"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "14:20:00"
                                :hora_salida "18:06:00"})

  (Insert db "registro_evento" {:matricula_id "28791"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "08:45:00"
                                :hora_salida "09:50:00"})
  (Insert db "registro_evento" {:matricula_id "28791"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "10:30:00"
                                :hora_salida "12:50:00"})
  (Insert db "registro_evento" {:matricula_id "28791"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "13:20:00"
                                :hora_salida "14:00:00"})
  (Insert db "registro_evento" {:matricula_id "28791"
                                :eventos_id "1"
                                :fecha "2019-09-13"
                                :hora_entrada "15:20:00"
                                :hora_salida "18:06:00"})
  )
;;(create-database)
