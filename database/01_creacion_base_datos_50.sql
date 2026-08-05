DROP DATABASE IF EXISTS rupe_db;

CREATE DATABASE rupe_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE rupe_db;

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(150),
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    rol_id BIGINT NOT NULL,
    CONSTRAINT fk_usuarios_roles FOREIGN KEY (rol_id) REFERENCES roles(id)
);

CREATE TABLE tutores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(120),
    direccion VARCHAR(250),
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE perros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    raza VARCHAR(80),
    color VARCHAR(80),
    tamano VARCHAR(30),
    sexo VARCHAR(20),
    senas_particulares VARCHAR(300),
    foto_ruta VARCHAR(250)
);

CREATE TABLE reportes_extravio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folio VARCHAR(30) NOT NULL UNIQUE,
    tutor_id BIGINT NOT NULL,
    perro_id BIGINT NOT NULL,
    fecha_extravio DATE,
    lugar_extravio VARCHAR(250),
    descripcion TEXT,
    estado VARCHAR(50) DEFAULT 'ABIERTO',
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reportes_tutores FOREIGN KEY (tutor_id) REFERENCES tutores(id),
    CONSTRAINT fk_reportes_perros FOREIGN KEY (perro_id) REFERENCES perros(id)
);

CREATE TABLE bitacora (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(120),
    modulo VARCHAR(80),
    accion VARCHAR(250),
    ip VARCHAR(50),
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP
);
