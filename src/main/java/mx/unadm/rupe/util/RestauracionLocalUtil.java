package mx.unadm.rupe.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Utilidad de restauración LOCAL para RUPE.
 *
 * IMPORTANTE:
 * - Sólo trabaja con respaldos H2 locales.
 * - Debe ejecutarse con RUPE detenido.
 * - Crea una copia física de emergencia antes de modificar data o uploads.
 * - Nunca debe utilizarse contra MySQL/Railway.
 */
public class RestauracionLocalUtil {

    private static final Path CARPETA_RESPALDOS =
            Path.of("respaldos").toAbsolutePath().normalize();

    private static final Path CARPETA_DATA =
            Path.of("data").toAbsolutePath().normalize();

    private static final Path CARPETA_UPLOADS =
            Path.of("uploads").toAbsolutePath().normalize();

    private static final Path CARPETA_TEMP_RESTAURACION =
            Path.of("restauracion_local_tmp").toAbsolutePath().normalize();

    private static final String ARCHIVO_H2 = "rupe_db.mv.db";

    public static void main(String[] args) {

        System.out.println("RUPE - Restauración local segura");
        System.out.println("Esta utilidad sólo restaura H2 LOCAL. No utiliza MySQL/Railway.");
        System.out.println();

        try {
            Path respaldoSeleccionado = seleccionarRespaldoH2();

            if (respaldoSeleccionado == null) {
                System.out.println("Restauración cancelada por el usuario.");
                return;
            }

            verificarBaseLocalCerrada();

            limpiarTemporalSiExiste();

            Path copiaEmergencia = crearCopiaEmergencia();

            Path baseTemporal = crearBaseTemporalDesdeRespaldo(respaldoSeleccionado);
            Path uploadsTemporales = prepararUploadsTemporales(respaldoSeleccionado);

            validarBaseTemporal(baseTemporal);
            validarUploadsTemporales(respaldoSeleccionado, uploadsTemporales);

            System.out.println("Respaldo seleccionado: " + respaldoSeleccionado);
            System.out.println("Copia de emergencia: " + copiaEmergencia);
            System.out.println("Base temporal validada: " + baseTemporal);
            System.out.println("Fotografías temporales validadas: " + uploadsTemporales);
            System.out.println();

            int respuesta = JOptionPane.showConfirmDialog(
                    null,
                    "La copia de emergencia y la restauración temporal ya fueron validadas.\n\n"
                    + "Respaldo: " + respaldoSeleccionado.getFileName() + "\n"
                    + "Copia de emergencia: " + copiaEmergencia.getFileName() + "\n\n"
                    + "¿Desea reemplazar ahora la base H2 local y las fotografías?",
                    "RUPE - Confirmar restauración local",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                limpiarTemporalSiExiste();
                System.out.println("Restauración cancelada antes de modificar los datos actuales.");
                System.out.println("La copia de emergencia se conservó en: " + copiaEmergencia);
                return;
            }

            restaurarConRollback(baseTemporal, uploadsTemporales, copiaEmergencia);
            limpiarTemporalSiExiste();

            System.out.println();
            System.out.println("RESTAURACIÓN LOCAL COMPLETADA CORRECTAMENTE.");
            System.out.println("Copia de emergencia conservada en: " + copiaEmergencia);
            System.out.println("Ya puede iniciar RUPE nuevamente con el perfil local.");

            JOptionPane.showMessageDialog(
                    null,
                    "La restauración local de RUPE terminó correctamente.\n\n"
                    + "Se conservó una copia de emergencia en:\n"
                    + copiaEmergencia,
                    "RUPE - Restauración completada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException | IOException e) {
            System.out.println("No se puede continuar:");
            System.out.println(e.getMessage());

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "RUPE - Restauración cancelada",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static Path seleccionarRespaldoH2() throws IOException {
        Files.createDirectories(CARPETA_RESPALDOS);

        List<Path> respaldos = new ArrayList<>();

        try (DirectoryStream<Path> elementos = Files.newDirectoryStream(CARPETA_RESPALDOS)) {
            for (Path elemento : elementos) {
                if (esRespaldoH2Valido(elemento)) {
                    respaldos.add(elemento.toAbsolutePath().normalize());
                }
            }
        }

        respaldos.sort(Comparator.comparing(
                (Path p) -> p.getFileName().toString(),
                String.CASE_INSENSITIVE_ORDER
        ).reversed());

        if (respaldos.isEmpty()) {
            throw new IOException(
                    "No se encontró ningún respaldo H2 completo dentro de la carpeta respaldos."
            );
        }

        String[] opciones = respaldos.stream()
                .map(p -> p.getFileName().toString())
                .toArray(String[]::new);

        String seleccion = (String) JOptionPane.showInputDialog(
                null,
                "Seleccione el respaldo H2 que desea restaurar.\n"
                + "RUPE debe estar detenido antes de continuar.",
                "RUPE - Seleccionar respaldo local",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (seleccion == null || seleccion.isBlank()) {
            return null;
        }

        Path ruta = CARPETA_RESPALDOS.resolve(seleccion).normalize();

        if (!esRespaldoH2Valido(ruta)) {
            throw new IOException("El respaldo seleccionado ya no es válido o está incompleto.");
        }

        return ruta;
    }

    private static boolean esRespaldoH2Valido(Path carpetaRespaldo) {
        if (carpetaRespaldo == null) {
            return false;
        }

        Path ruta = carpetaRespaldo.toAbsolutePath().normalize();

        if (!ruta.startsWith(CARPETA_RESPALDOS)) {
            return false;
        }

        if (!Files.isDirectory(ruta)) {
            return false;
        }

        Path archivoSql = ruta.resolve("base_datos").resolve("rupe_h2.sql");
        Path carpetaUploads = ruta.resolve("uploads");

        return Files.isRegularFile(archivoSql)
                && Files.isDirectory(carpetaUploads);
    }

    private static void verificarBaseLocalCerrada() throws IOException {
        Path archivoBase = CARPETA_DATA.resolve(ARCHIVO_H2).normalize();

        if (!archivoBase.startsWith(CARPETA_DATA)) {
            throw new IOException("La ruta de la base de datos local no es válida.");
        }

        if (!Files.isRegularFile(archivoBase)) {
            throw new IOException("No se encontró la base local data/" + ARCHIVO_H2 + ".");
        }

        try (FileChannel canal = FileChannel.open(archivoBase, StandardOpenOption.WRITE)) {
            try (FileLock bloqueo = canal.tryLock()) {
                if (bloqueo == null) {
                    throw new IOException(
                            "La base de datos está en uso. Cierre RUPE antes de restaurar."
                    );
                }
            }
        } catch (OverlappingFileLockException e) {
            throw new IOException(
                    "La base de datos está en uso. Cierre RUPE antes de restaurar.",
                    e
            );
        }
    }

    private static Path crearCopiaEmergencia() throws IOException {
        if (!Files.isDirectory(CARPETA_DATA)) {
            throw new IOException("No se encontró la carpeta data.");
        }

        if (!Files.isDirectory(CARPETA_UPLOADS)) {
            throw new IOException("No se encontró la carpeta uploads.");
        }

        String marcaTiempo = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );

        Path carpetaSeguridad = CARPETA_RESPALDOS
                .resolve("seguridad_pre_restauracion_" + marcaTiempo)
                .normalize();

        if (!carpetaSeguridad.startsWith(CARPETA_RESPALDOS)) {
            throw new IOException("La ruta de seguridad no es válida.");
        }

        if (Files.exists(carpetaSeguridad)) {
            throw new IOException("Ya existe una copia de seguridad con el mismo nombre.");
        }

        Path dataDestino = carpetaSeguridad.resolve("data_original");
        Path uploadsDestino = carpetaSeguridad.resolve("uploads_original");

        copiarCarpeta(CARPETA_DATA, dataDestino);
        copiarCarpeta(CARPETA_UPLOADS, uploadsDestino);

        Path baseCopiada = dataDestino.resolve(ARCHIVO_H2);

        if (!Files.isRegularFile(baseCopiada)) {
            throw new IOException("La copia física de emergencia de H2 quedó incompleta.");
        }

        long uploadsOrigen = contarArchivos(CARPETA_UPLOADS);
        long uploadsCopia = contarArchivos(uploadsDestino);

        if (uploadsOrigen != uploadsCopia) {
            throw new IOException("La copia de emergencia de fotografías quedó incompleta.");
        }

        return carpetaSeguridad;
    }

    private static Path crearBaseTemporalDesdeRespaldo(Path respaldoSeleccionado)
            throws IOException {

        Path archivoSql = respaldoSeleccionado
                .resolve("base_datos")
                .resolve("rupe_h2.sql")
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(archivoSql)) {
            throw new IOException("No se encontró rupe_h2.sql en el respaldo seleccionado.");
        }

        Path carpetaDataTemporal = CARPETA_TEMP_RESTAURACION
                .resolve("data_restaurada")
                .normalize();

        if (!carpetaDataTemporal.startsWith(CARPETA_TEMP_RESTAURACION)) {
            throw new IOException("La ruta temporal de restauración no es válida.");
        }

        Files.createDirectories(carpetaDataTemporal);

        Path baseTemporal = carpetaDataTemporal
                .resolve("rupe_db")
                .toAbsolutePath()
                .normalize();

        String rutaBase = baseTemporal.toString().replace("\\", "/");
        String rutaSql = archivoSql.toString()
                .replace("\\", "/")
                .replace("'", "''");

        String url = "jdbc:h2:file:" + rutaBase
                + ";MODE=MySQL"
                + ";DATABASE_TO_LOWER=TRUE"
                + ";CASE_INSENSITIVE_IDENTIFIERS=TRUE";

        try (Connection conexion = DriverManager.getConnection(url, "sa", "");
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute("RUNSCRIPT FROM '" + rutaSql + "'");

        } catch (SQLException e) {
            throw new IOException("No se pudo crear la base H2 temporal restaurada.", e);
        }

        Path archivoBaseTemporal = Path.of(baseTemporal.toString() + ".mv.db")
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(archivoBaseTemporal)) {
            throw new IOException("La base H2 temporal no se creó correctamente.");
        }

        return archivoBaseTemporal;
    }

    private static Path prepararUploadsTemporales(Path respaldoSeleccionado)
            throws IOException {

        Path origen = respaldoSeleccionado.resolve("uploads").toAbsolutePath().normalize();
        Path destino = CARPETA_TEMP_RESTAURACION
                .resolve("uploads_restaurados")
                .toAbsolutePath()
                .normalize();

        if (!Files.isDirectory(origen)) {
            throw new IOException("El respaldo no contiene la carpeta uploads.");
        }

        if (!destino.startsWith(CARPETA_TEMP_RESTAURACION)) {
            throw new IOException("La ruta temporal de fotografías no es válida.");
        }

        copiarCarpeta(origen, destino);
        return destino;
    }

    private static void validarBaseTemporal(Path archivoBaseTemporal) throws IOException {
        Path baseSinExtension = quitarExtensionMvDb(archivoBaseTemporal);
        String rutaBase = baseSinExtension.toString().replace("\\", "/");

        String url = "jdbc:h2:file:" + rutaBase
                + ";MODE=MySQL"
                + ";DATABASE_TO_LOWER=TRUE"
                + ";CASE_INSENSITIVE_IDENTIFIERS=TRUE";

        int tablasUsuario = 0;

        try (Connection conexion = DriverManager.getConnection(url, "sa", "")) {
            DatabaseMetaData meta = conexion.getMetaData();

            try (ResultSet rs = meta.getTables(null, "public", "%", new String[]{"BASE TABLE", "TABLE"})) {
                while (rs.next()) {
                    tablasUsuario++;
                }
            }

            if (tablasUsuario == 0) {
                try (Statement st = conexion.createStatement();
                     ResultSet rs = st.executeQuery("SHOW TABLES")) {
                    while (rs.next()) {
                        tablasUsuario++;
                    }
                }
            }

        } catch (SQLException e) {
            throw new IOException("La base H2 temporal no pudo validarse.", e);
        }

        if (tablasUsuario == 0) {
            throw new IOException("La base H2 temporal no contiene tablas de RUPE.");
        }
    }

    private static Path quitarExtensionMvDb(Path archivoMvDb) throws IOException {
        String nombre = archivoMvDb.getFileName().toString();

        if (!nombre.endsWith(".mv.db")) {
            throw new IOException("El archivo temporal H2 no tiene la extensión esperada .mv.db.");
        }

        String base = nombre.substring(0, nombre.length() - ".mv.db".length());
        Path padre = archivoMvDb.getParent();

        if (padre == null) {
            throw new IOException("No se pudo determinar la carpeta de la base H2 temporal.");
        }

        return padre.resolve(base).toAbsolutePath().normalize();
    }

    private static void validarUploadsTemporales(Path respaldoSeleccionado,
                                                  Path uploadsTemporales)
            throws IOException {

        Path uploadsRespaldo = respaldoSeleccionado.resolve("uploads");

        long origen = contarArchivos(uploadsRespaldo);
        long temporal = contarArchivos(uploadsTemporales);

        if (origen != temporal) {
            throw new IOException(
                    "La copia temporal de fotografías está incompleta. "
                    + "Origen: " + origen + ", temporal: " + temporal + "."
            );
        }
    }

    private static void restaurarConRollback(Path baseTemporal,
                                              Path uploadsTemporales,
                                              Path copiaEmergencia)
            throws IOException {

        Path baseActual = CARPETA_DATA.resolve(ARCHIVO_H2).normalize();
        Path traceActual = CARPETA_DATA.resolve("rupe_db.trace.db").normalize();

        Path baseEmergencia = copiaEmergencia
                .resolve("data_original")
                .resolve(ARCHIVO_H2)
                .normalize();

        Path uploadsEmergencia = copiaEmergencia
                .resolve("uploads_original")
                .normalize();

        boolean modificacionIniciada = false;

        try {
            Files.createDirectories(CARPETA_DATA);

            modificacionIniciada = true;

            Files.copy(
                    baseTemporal,
                    baseActual,
                    StandardCopyOption.REPLACE_EXISTING
            );

            Files.deleteIfExists(traceActual);

            vaciarDirectorioUploads();
            copiarCarpeta(uploadsTemporales, CARPETA_UPLOADS);

        } catch (IOException e) {
            if (modificacionIniciada) {
                try {
                    Files.copy(
                            baseEmergencia,
                            baseActual,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    vaciarDirectorioUploads();
                    copiarCarpeta(uploadsEmergencia, CARPETA_UPLOADS);

                } catch (IOException rollbackError) {
                    throw new IOException(
                            "La restauración falló y también ocurrió un problema durante el rollback. "
                            + "No inicie RUPE hasta revisar la copia de emergencia: "
                            + copiaEmergencia,
                            rollbackError
                    );
                }
            }

            throw new IOException(
                    "La restauración no pudo completarse. "
                    + "Los datos anteriores fueron recuperados desde la copia de emergencia.",
                    e
            );
        }
    }

    private static void vaciarDirectorioUploads() throws IOException {
        Path uploadsSeguro = CARPETA_UPLOADS.toAbsolutePath().normalize();

        if (!uploadsSeguro.equals(CARPETA_UPLOADS)) {
            throw new IOException("La ruta uploads no es válida.");
        }

        Files.createDirectories(uploadsSeguro);

        try (DirectoryStream<Path> elementos = Files.newDirectoryStream(uploadsSeguro)) {
            for (Path elemento : elementos) {
                eliminarRecursivo(elemento, uploadsSeguro);
            }
        }
    }

    private static void limpiarTemporalSiExiste() throws IOException {
        if (!Files.exists(CARPETA_TEMP_RESTAURACION)) {
            return;
        }

        Path temporal = CARPETA_TEMP_RESTAURACION.toAbsolutePath().normalize();

        if (!temporal.endsWith("restauracion_local_tmp")) {
            throw new IOException("La ruta temporal no pasó la validación de seguridad.");
        }

        eliminarRecursivo(temporal, temporal);
    }

    private static void eliminarRecursivo(Path objetivo,
                                           Path basePermitida)
            throws IOException {

        Path objetivoSeguro = objetivo.toAbsolutePath().normalize();
        Path baseSegura = basePermitida.toAbsolutePath().normalize();

        if (!objetivoSeguro.startsWith(baseSegura)) {
            throw new IOException("Se intentó eliminar una ruta fuera del área permitida.");
        }

        if (!Files.exists(objetivoSeguro)) {
            return;
        }

        if (Files.isDirectory(objetivoSeguro)) {
            try (DirectoryStream<Path> elementos = Files.newDirectoryStream(objetivoSeguro)) {
                for (Path elemento : elementos) {
                    eliminarRecursivo(elemento, baseSegura);
                }
            }
        }

        Files.deleteIfExists(objetivoSeguro);
    }

    private static void copiarCarpeta(Path origen,
                                       Path destino)
            throws IOException {

        if (!Files.exists(origen)) {
            return;
        }

        Files.createDirectories(destino);

        try (DirectoryStream<Path> elementos = Files.newDirectoryStream(origen)) {
            for (Path elemento : elementos) {
                Path destinoElemento = destino.resolve(elemento.getFileName());

                if (Files.isDirectory(elemento)) {
                    copiarCarpeta(elemento, destinoElemento);
                } else if (Files.isRegularFile(elemento)) {
                    Files.copy(
                            elemento,
                            destinoElemento,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        }
    }

    private static long contarArchivos(Path carpeta) throws IOException {
        if (!Files.exists(carpeta)) {
            return 0;
        }

        long total = 0;

        try (DirectoryStream<Path> elementos = Files.newDirectoryStream(carpeta)) {
            for (Path elemento : elementos) {
                if (Files.isDirectory(elemento)) {
                    total += contarArchivos(elemento);
                } else if (Files.isRegularFile(elemento)) {
                    total++;
                }
            }
        }

        return total;
    }
}
