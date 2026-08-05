USE rupe_db;

CREATE INDEX idx_reporte_folio ON reportes_extravio(folio);
CREATE INDEX idx_reporte_estado ON reportes_extravio(estado);
CREATE INDEX idx_bitacora_fecha ON bitacora(fecha_hora);

CREATE USER IF NOT EXISTS 'rupe_user'@'localhost' IDENTIFIED BY 'rupe123';
GRANT SELECT, INSERT, UPDATE, DELETE ON rupe_db.* TO 'rupe_user'@'localhost';
FLUSH PRIVILEGES;

SHOW GRANTS FOR 'rupe_user'@'localhost';
