DROP DATABASE IF EXISTS rupe_db;
CREATE DATABASE rupe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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
    color VARCHAR(80) NOT NULL,
    tamano VARCHAR(30),
    sexo VARCHAR(20),
    senas_particulares VARCHAR(300) NOT NULL,
    foto_ruta VARCHAR(250)
);

CREATE TABLE reportes_extravio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folio VARCHAR(30) NOT NULL UNIQUE,
    tutor_id BIGINT NOT NULL,
    perro_id BIGINT NOT NULL,
    fecha_extravio DATE NOT NULL,
    lugar_extravio VARCHAR(250) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(50) DEFAULT 'ABIERTO',
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reportes_tutores FOREIGN KEY (tutor_id) REFERENCES tutores(id),
    CONSTRAINT fk_reportes_perros FOREIGN KEY (perro_id) REFERENCES perros(id)
);

CREATE TABLE pistas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporte_id BIGINT NOT NULL,
    lugar VARCHAR(250) NOT NULL,
    fecha DATE,
    hora TIME,
    descripcion TEXT,
    foto_ruta VARCHAR(250),
    resguardado BOOLEAN DEFAULT FALSE,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pistas_reportes FOREIGN KEY (reporte_id) REFERENCES reportes_extravio(id)
);

CREATE TABLE fotos_reportes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporte_id BIGINT NOT NULL,
    ruta_archivo VARCHAR(250) NOT NULL,
    descripcion VARCHAR(150),
    fecha_subida DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fotos_reportes FOREIGN KEY (reporte_id) REFERENCES reportes_extravio(id)
);

CREATE TABLE fichas_pdf (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporte_id BIGINT NOT NULL,
    ruta_pdf VARCHAR(250) NOT NULL,
    fecha_generacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fichas_reportes FOREIGN KEY (reporte_id) REFERENCES reportes_extravio(id)
);

CREATE TABLE codigos_qr (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporte_id BIGINT NOT NULL,
    contenido VARCHAR(500) NOT NULL,
    fecha_generacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_qr_reportes FOREIGN KEY (reporte_id) REFERENCES reportes_extravio(id)
);

CREATE TABLE configuracion_sistema (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(80) NOT NULL UNIQUE,
    valor VARCHAR(250) NOT NULL,
    descripcion VARCHAR(250),
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE intentos_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(120) NOT NULL,
    ip VARCHAR(50),
    exitoso BOOLEAN DEFAULT FALSE,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    bloqueado_hasta DATETIME
);

CREATE TABLE respaldos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_archivo VARCHAR(150) NOT NULL,
    ruta_archivo VARCHAR(250) NOT NULL,
    usuario VARCHAR(120),
    fecha_generacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bitacora (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(120),
    modulo VARCHAR(80),
    accion VARCHAR(250),
    ip VARCHAR(50),
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reporte_folio ON reportes_extravio(folio);
CREATE INDEX idx_reporte_estado ON reportes_extravio(estado);
CREATE INDEX idx_bitacora_fecha ON bitacora(fecha_hora);
CREATE INDEX idx_intentos_login_correo ON intentos_login(correo);

INSERT INTO roles (nombre, descripcion, activo)
VALUES ('ADMINISTRADOR', 'Usuario con acceso al panel administrativo.', TRUE);

-- La contraseña se carga con hash automáticamente desde DataInitializer.
