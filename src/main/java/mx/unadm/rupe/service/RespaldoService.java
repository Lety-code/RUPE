package mx.unadm.rupe.service;

import mx.unadm.rupe.model.Respaldo;
import mx.unadm.rupe.repository.RespaldoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RespaldoService {

    private final RespaldoRepository respaldoRepository;
    private final DataSource dataSource;

    @Value("${rupe.upload-dir:uploads}")
    private String uploadDir;

    @Value("${rupe.backup-dir:respaldos}")
    private String backupDir;

    public RespaldoService(RespaldoRepository respaldoRepository,
                           DataSource dataSource) {
        this.respaldoRepository = respaldoRepository;
        this.dataSource = dataSource;
    }

    public Respaldo crearRespaldo(String usuario) throws IOException {

        if (usuario == null || usuario.isBlank()) {
            throw new IOException("No se pudo identificar al usuario administrador.");
        }

        Path carpetaRespaldos = obtenerCarpetaRespaldos();
        Files.createDirectories(carpetaRespaldos);

        String marcaTiempo = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );

        String nombre = "respaldo_rupe_" + marcaTiempo;
        Path rutaRespaldo = carpetaRespaldos.resolve(nombre).normalize();

        if (!rutaRespaldo.startsWith(carpetaRespaldos)) {
            throw new IOException("La ruta del respaldo no es válida.");
        }

        if (Files.exists(rutaRespaldo)) {
            throw new IOException("Ya existe un respaldo con el mismo nombre.");
        }

        Path rutaBaseDatos = rutaRespaldo.resolve("base_datos");
        Path rutaUploads = rutaRespaldo.resolve("uploads");

        try {
            Files.createDirectories(rutaBaseDatos);
            Files.createDirectories(rutaUploads);

            String tipoBaseDatos;

            try (Connection conexion = dataSource.getConnection()) {
                tipoBaseDatos = obtenerTipoBaseDatos(conexion);
            } catch (SQLException e) {
                throw new IOException("No se pudo identificar la base de datos activa.", e);
            }

            if ("H2".equals(tipoBaseDatos)) {
                Path archivoSql = rutaBaseDatos.resolve("rupe_h2.sql");
                generarRespaldoH2(archivoSql);
            } else if ("MYSQL".equals(tipoBaseDatos)) {
                Path archivoSql = rutaBaseDatos.resolve("rupe_mysql.sql");
                generarRespaldoMySql(archivoSql);
            } else {
                throw new IOException(
                        "El tipo de base de datos activo no está soportado para respaldos: "
                        + tipoBaseDatos
                );
            }

            copiarCarpeta(
                    Path.of(uploadDir).toAbsolutePath().normalize(),
                    rutaUploads
            );

            crearArchivoInformacion(
                    rutaRespaldo,
                    usuario,
                    tipoBaseDatos,
                    rutaUploads
            );

        } catch (IOException e) {
            eliminarDirectorioParcial(rutaRespaldo);
            throw e;
        }

        Respaldo respaldo = new Respaldo();
        respaldo.setNombreArchivo(nombre);
        respaldo.setRutaArchivo(rutaRespaldo.toString());
        respaldo.setUsuario(usuario);

        return respaldoRepository.save(respaldo);
    }

    public boolean esRespaldoCompleto(Long id) {
        if (id == null) {
            return false;
        }

        return respaldoRepository.findById(id)
                .map(this::esRespaldoCompleto)
                .orElse(false);
    }

    public boolean esRespaldoRestaurableLocalH2(Long id) {
        if (id == null) {
            return false;
        }

        return respaldoRepository.findById(id)
                .map(r -> {
                    Path ruta = obtenerRutaRespaldoSegura(r);
                    if (ruta == null) {
                        return false;
                    }

                    Path archivoH2 = ruta.resolve("base_datos").resolve("rupe_h2.sql");
                    Path carpetaUploads = ruta.resolve("uploads");

                    return Files.isRegularFile(archivoH2)
                            && Files.isDirectory(carpetaUploads);
                })
                .orElse(false);
    }

    public String obtenerTipoBaseDatosActual() throws IOException {
        try (Connection conexion = dataSource.getConnection()) {
            return obtenerTipoBaseDatos(conexion);
        } catch (SQLException e) {
            throw new IOException("No se pudo identificar la base de datos activa.", e);
        }
    }

    public List<Respaldo> listar() {
        return respaldoRepository.findAll();
    }

    private Path obtenerCarpetaRespaldos() {
        return Path.of(backupDir)
                .toAbsolutePath()
                .normalize();
    }

    private Path obtenerRutaRespaldoSegura(Respaldo respaldo) {
        if (respaldo == null
                || respaldo.getRutaArchivo() == null
                || respaldo.getRutaArchivo().isBlank()) {
            return null;
        }

        Path carpetaRespaldos = obtenerCarpetaRespaldos();
        Path rutaRespaldo = Path.of(respaldo.getRutaArchivo())
                .toAbsolutePath()
                .normalize();

        if (!rutaRespaldo.startsWith(carpetaRespaldos)) {
            return null;
        }

        if (!Files.isDirectory(rutaRespaldo)) {
            return null;
        }

        return rutaRespaldo;
    }

    private boolean esRespaldoCompleto(Respaldo respaldo) {
        Path rutaRespaldo = obtenerRutaRespaldoSegura(respaldo);

        if (rutaRespaldo == null) {
            return false;
        }

        Path carpetaBaseDatos = rutaRespaldo.resolve("base_datos");
        Path carpetaUploads = rutaRespaldo.resolve("uploads");
        Path archivoH2 = carpetaBaseDatos.resolve("rupe_h2.sql");
        Path archivoMySql = carpetaBaseDatos.resolve("rupe_mysql.sql");

        boolean tieneBaseDatos = Files.isRegularFile(archivoH2)
                || Files.isRegularFile(archivoMySql);

        return Files.isDirectory(carpetaBaseDatos)
                && tieneBaseDatos
                && Files.isDirectory(carpetaUploads);
    }

    private String obtenerTipoBaseDatos(Connection conexion) throws SQLException {
        String producto = conexion.getMetaData().getDatabaseProductName();

        if (producto == null) {
            return "DESCONOCIDA";
        }

        String normalizado = producto.trim().toUpperCase();

        if (normalizado.contains("H2")) {
            return "H2";
        }

        if (normalizado.contains("MYSQL")) {
            return "MYSQL";
        }

        return normalizado;
    }

    private void generarRespaldoH2(Path rutaSql) throws IOException {

        String rutaSegura = rutaSql.toAbsolutePath()
                .normalize()
                .toString()
                .replace("\\", "/")
                .replace("'", "''");

        try (Connection conexion = dataSource.getConnection();
             Statement sentencia = conexion.createStatement()) {

            String tipoBaseDatos = obtenerTipoBaseDatos(conexion);

            if (!"H2".equals(tipoBaseDatos)) {
                throw new IOException("La base de datos activa no es H2.");
            }

            sentencia.execute("SCRIPT TO '" + rutaSegura + "'");

        } catch (SQLException e) {
            throw new IOException("No se pudo generar el respaldo de H2.", e);
        }

        if (!Files.isRegularFile(rutaSql)) {
            throw new IOException("H2 no generó el archivo de respaldo esperado.");
        }
    }

    private void generarRespaldoMySql(Path rutaSql) throws IOException {

        try (Connection conexion = dataSource.getConnection()) {

            String tipoBaseDatos = obtenerTipoBaseDatos(conexion);

            if (!"MYSQL".equals(tipoBaseDatos)) {
                throw new IOException("La base de datos activa no es MySQL.");
            }

            boolean autoCommitOriginal = conexion.getAutoCommit();
            int aislamientoOriginal = conexion.getTransactionIsolation();

            try {
                conexion.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                conexion.setAutoCommit(false);

                List<String> tablas = obtenerTablasMySql(conexion);

                try (BufferedWriter writer = Files.newBufferedWriter(
                        rutaSql,
                        StandardCharsets.UTF_8
                )) {
                    writer.write("-- RUPE - Respaldo lógico MySQL\n");
                    writer.write("-- Generado: " + LocalDateTime.now() + "\n");
                    writer.write("-- Restaurar únicamente con una base MySQL controlada.\n\n");
                    writer.write("SET NAMES utf8mb4;\n");
                    writer.write("SET FOREIGN_KEY_CHECKS=0;\n\n");

                    for (String tabla : tablas) {
                        writer.write("DROP TABLE IF EXISTS " + citarIdentificadorMySql(tabla) + ";\n");
                    }

                    writer.write("\n");

                    for (String tabla : tablas) {
                        String ddl = obtenerCreateTableMySql(conexion, tabla);
                        writer.write(ddl);
                        writer.write(";\n\n");
                    }

                    for (String tabla : tablas) {
                        escribirDatosMySql(conexion, writer, tabla);
                    }

                    writer.write("SET FOREIGN_KEY_CHECKS=1;\n");
                }

                conexion.rollback();

            } catch (SQLException e) {
                try {
                    conexion.rollback();
                } catch (SQLException ignored) {
                }
                throw new IOException("No se pudo generar el respaldo de MySQL.", e);
            } finally {
                try {
                    conexion.setAutoCommit(autoCommitOriginal);
                } catch (SQLException ignored) {
                }
                try {
                    conexion.setTransactionIsolation(aislamientoOriginal);
                } catch (SQLException ignored) {
                }
            }

        } catch (SQLException e) {
            throw new IOException("No se pudo conectar a MySQL para generar el respaldo.", e);
        }

        if (!Files.isRegularFile(rutaSql)) {
            throw new IOException("MySQL no generó el archivo de respaldo esperado.");
        }
    }

    private List<String> obtenerTablasMySql(Connection conexion) throws SQLException {
        List<String> tablas = new ArrayList<>();

        DatabaseMetaData metadata = conexion.getMetaData();
        String catalogo = conexion.getCatalog();

        try (ResultSet rs = metadata.getTables(
                catalogo,
                null,
                "%",
                new String[]{"TABLE"}
        )) {
            while (rs.next()) {
                String tabla = rs.getString("TABLE_NAME");
                if (tabla != null && !tabla.isBlank()) {
                    tablas.add(tabla);
                }
            }
        }

        Collections.sort(tablas, String.CASE_INSENSITIVE_ORDER);
        return tablas;
    }

    private String obtenerCreateTableMySql(Connection conexion,
                                            String tabla) throws SQLException {
        String sql = "SHOW CREATE TABLE " + citarIdentificadorMySql(tabla);

        try (Statement st = conexion.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (!rs.next()) {
                throw new SQLException("No se pudo obtener la estructura de la tabla " + tabla + ".");
            }

            return rs.getString(2);
        }
    }

    private void escribirDatosMySql(Connection conexion,
                                    BufferedWriter writer,
                                    String tabla) throws SQLException, IOException {

        String sql = "SELECT * FROM " + citarIdentificadorMySql(tabla);

        try (Statement st = conexion.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
        )) {
            st.setFetchSize(Integer.MIN_VALUE);

            try (ResultSet rs = st.executeQuery(sql)) {
                ResultSetMetaData md = rs.getMetaData();
                int columnas = md.getColumnCount();

                while (rs.next()) {
                    StringBuilder insert = new StringBuilder();
                    insert.append("INSERT INTO ")
                            .append(citarIdentificadorMySql(tabla))
                            .append(" (");

                    for (int i = 1; i <= columnas; i++) {
                        if (i > 1) {
                            insert.append(", ");
                        }
                        insert.append(citarIdentificadorMySql(md.getColumnLabel(i)));
                    }

                    insert.append(") VALUES (");

                    for (int i = 1; i <= columnas; i++) {
                        if (i > 1) {
                            insert.append(", ");
                        }
                        insert.append(valorSqlMySql(rs, i, md.getColumnType(i)));
                    }

                    insert.append(");\n");
                    writer.write(insert.toString());
                }
            }
        }

        writer.write("\n");
    }

    private String valorSqlMySql(ResultSet rs,
                                 int columna,
                                 int tipoSql) throws SQLException {

        Object valor = rs.getObject(columna);

        if (valor == null) {
            return "NULL";
        }

        switch (tipoSql) {
            case Types.BIT:
            case Types.BOOLEAN:
                if (valor instanceof Boolean) {
                    return ((Boolean) valor) ? "1" : "0";
                }
                return rs.getBoolean(columna) ? "1" : "0";

            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                return valor.toString();

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "0x" + bytesAHex(rs.getBytes(columna));

            case Types.BLOB:
                Blob blob = rs.getBlob(columna);
                if (blob == null) {
                    return "NULL";
                }
                byte[] bytes = blob.getBytes(1, (int) blob.length());
                return "0x" + bytesAHex(bytes);

            case Types.DATE:
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "'" + escaparTextoMySql(valor.toString()) + "'";

            default:
                String texto = rs.getString(columna);
                if (texto == null) {
                    return "NULL";
                }
                return "'" + escaparTextoMySql(texto) + "'";
        }
    }

    private String citarIdentificadorMySql(String identificador) {
        return "`" + identificador.replace("`", "``") + "`";
    }

    private String escaparTextoMySql(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("'", "''")
                .replace("\u0000", "");
    }

    private String bytesAHex(byte[] datos) {
        if (datos == null || datos.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(datos.length * 2);
        final char[] hex = "0123456789ABCDEF".toCharArray();

        for (byte dato : datos) {
            int valor = dato & 0xFF;
            sb.append(hex[valor >>> 4]);
            sb.append(hex[valor & 0x0F]);
        }

        return sb.toString();
    }

    private void copiarCarpeta(Path origen,
                               Path destino) throws IOException {

        Files.createDirectories(destino);

        if (!Files.exists(origen)) {
            return;
        }

        if (!Files.isDirectory(origen)) {
            throw new IOException("La ruta de uploads no es una carpeta válida: " + origen);
        }

        try (DirectoryStream<Path> elementos = Files.newDirectoryStream(origen)) {
            for (Path elemento : elementos) {
                Path destinoElemento = destino.resolve(elemento.getFileName());

                if (Files.isDirectory(elemento)) {
                    copiarCarpeta(elemento, destinoElemento);
                } else if (Files.isRegularFile(elemento)) {
                    Files.copy(
                            elemento,
                            destinoElemento,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                }
            }
        }
    }

    private void crearArchivoInformacion(Path rutaRespaldo,
                                         String usuario,
                                         String tipoBaseDatos,
                                         Path rutaUploads) throws IOException {

        long totalArchivos = contarArchivos(rutaUploads);

        String contenido =
                "RUPE - Respaldo completo\n"
                + "Fecha: " + LocalDateTime.now() + "\n"
                + "Usuario: " + usuario + "\n"
                + "Base de datos: " + tipoBaseDatos + "\n"
                + "Archivos copiados desde uploads: " + totalArchivos + "\n"
                + "\n"
                + "Este respaldo contiene una copia lógica de la base de datos "
                + "y una copia de la carpeta uploads.\n";

        Files.writeString(
                rutaRespaldo.resolve("informacion_respaldo.txt"),
                contenido,
                StandardCharsets.UTF_8
        );
    }

    private long contarArchivos(Path carpeta) throws IOException {
        if (!Files.exists(carpeta)) {
            return 0;
        }

        if (Files.isRegularFile(carpeta)) {
            return 1;
        }

        long total = 0;

        try (DirectoryStream<Path> elementos = Files.newDirectoryStream(carpeta)) {
            for (Path elemento : elementos) {
                total += contarArchivos(elemento);
            }
        }

        return total;
    }

    private void eliminarDirectorioParcial(Path carpeta) {
        if (carpeta == null || !Files.exists(carpeta)) {
            return;
        }

        try {
            if (Files.isDirectory(carpeta)) {
                try (DirectoryStream<Path> elementos = Files.newDirectoryStream(carpeta)) {
                    for (Path elemento : elementos) {
                        eliminarDirectorioParcial(elemento);
                    }
                }
            }
            Files.deleteIfExists(carpeta);
        } catch (IOException ignored) {
        }
    }
}
