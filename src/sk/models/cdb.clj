(ns sk.models.cdb
  (:require [sk.models.crud :refer :all]
            [noir.util.crypt :as crypt]))

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

(def alumnos-rows
  [{:matricula 28787
    :password "10201117"
    :apell_paterno "Pescador"
    :apell_materno "Martinez"
    :nombre "Marco"
    :escuela "UABC"
    :carrera "Ciencias Computacionales"
    :semestre "0"
    :status "E"
    :fecha_ingreso "1990-01-01"
    :email "marcopescador@hotmail.com"
    }
   {:matricula 28788
    :password "elmo1200"
    :apell_paterno "Lucero"
    :apell_materno "Quihuis"
    :nombre "Hector"
    :escuela "LBSU"
    :carrera "Ciencias Computacionaels"
    :semestre "0"
    :status "E"
    :fecha_ingreso "1976-09-01"
    :email "hectorqlucero@gmail.com"}])

(def categorias-rows
  [{:id "1"
    :categoria "Capacitación"
    }
   {:id "2"
    :categoria "Conferencias"
    }
   {:id "3"
    :categoria "Cursos"
    }
   {:id "4"
    :categoria "Platicas"
    }
   {:id "5"
    :categoria "Talleres"}
   {:id "6"
    :categoria "Tutorias"}])

(def eventos-rows
  [{:id "1"
    :categorias_id "2"
    :descripcion "Conferencia Odontologia por el expositor Margarito Lopez, en el cual se hablara de los nuevos tratamientos odontologicos y las nuevas tecnologias que se aplicaran en estas modalidades."
    :fecha_inicio "2019-07-25"
    :hora_inicio "08:00"
    :imagen "1.jpg"
    :lugar "Edificio de Vi-serectoria aula magna # 1"
    :titulo "Nuevas tecnologias para la odontologia"
    :fecha_terminacion "2019-07-25"
    :hora_terminacion "12:00"
    }])

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

(def categorias-sql
  "CREATE TABLE categorias (
   id int(11) unsigned NOT NULL AUTO_INCREMENT,
   categoria varchar(100) DEFAULT NULL,
   PRIMARY KEY (id)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")

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
   PRIMARY KEY (id)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")

(def registro_evento-sql
  "CREATE TABLE registro_evento (
   id int(11) unsigned NOT NULL AUTO_INCREMENT,
   matricula_id int(11) NOT NULL,
   eventos_id int(11) NOT NULL,
   hora_entrada time DEFAULT NULL,
   hora_salida time DEFAULT NULL,
   fecha date DEFAULT NULL,
   PRIMARY KEY (id)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8")

(defn create-database []
  "Creates database and a default admin users"
  (Query! db users-sql)
  (Insert-multi db :users user-rows)
  (Query! db alumnos-sql)
  (Insert-multi db :alumnos alumnos-rows)
  (Query! db categorias-sql)
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
  (Insert-multi db :alumnos alumnos-rows)
  (Query! db categorias-sql)
  (Query! db "DROP table IF EXISTS eventos")
  (Query! db eventos-sql)
  (Query! db "DROP table IF EXISTS registro_evento")
  (Query! db registro_evento-sql))


(defn migrate []
  "Migrate by the seat of my pants"
  (Query! db "DROP table IF EXISTS eventos")
  (Query! db eventos-sql)
  (Insert-multi db :eventos eventos-rows)
  (Query! db "DROP table IF EXISTS registro_evento")
  (Query! db registro_evento-sql))

(migrate)
