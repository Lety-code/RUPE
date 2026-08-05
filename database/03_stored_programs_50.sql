USE rupe_db;

DELIMITER //

CREATE PROCEDURE sp_consultar_reporte_por_folio(
    IN p_folio VARCHAR(30)
)
BEGIN
    SELECT
        r.id,
        r.folio,
        r.fecha_extravio,
        r.lugar_extravio,
        r.descripcion,
        r.estado,
        r.fecha_registro,
        t.nombre AS nombre_tutor,
        t.telefono,
        t.correo,
        p.nombre AS nombre_perro,
        p.raza,
        p.color,
        p.tamano,
        p.sexo,
        p.senas_particulares
    FROM reportes_extravio r
    INNER JOIN tutores t ON r.tutor_id = t.id
    INNER JOIN perros p ON r.perro_id = p.id
    WHERE r.folio = p_folio;
END //

CREATE PROCEDURE sp_registrar_bitacora(
    IN p_usuario VARCHAR(120),
    IN p_modulo VARCHAR(80),
    IN p_accion VARCHAR(250),
    IN p_ip VARCHAR(50)
)
BEGIN
    INSERT INTO bitacora(usuario, modulo, accion, ip, fecha_hora)
    VALUES(p_usuario, p_modulo, p_accion, p_ip, NOW());
END //

DELIMITER ;
