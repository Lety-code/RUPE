package mx.unadm.rupe.service;

import mx.unadm.rupe.model.Respaldo;
import mx.unadm.rupe.repository.RespaldoRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RespaldoService {
    private final RespaldoRepository respaldoRepository;

    public RespaldoService(RespaldoRepository respaldoRepository) {
        this.respaldoRepository = respaldoRepository;
    }

    public Respaldo crearRespaldo(String usuario) throws IOException {
        Files.createDirectories(Path.of("respaldos"));
        String nombre = "respaldo_rupe_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        Path ruta = Path.of("respaldos", nombre);
        String contenido = "Respaldo lógico informativo del sistema RUPE\n" +
                "Generado: " + LocalDateTime.now() + "\n" +
                "Usuario: " + usuario + "\n" +
                "Tablas: roles, usuarios, tutores, perros, reportes_extravio, pistas, fotos_reportes, fichas_pdf, configuracion_sistema, intentos_login, respaldos, bitacora\n";
        Files.writeString(ruta, contenido);

        Respaldo r = new Respaldo();
        r.setNombreArchivo(nombre);
        r.setRutaArchivo(ruta.toString());
        r.setUsuario(usuario);
        return respaldoRepository.save(r);
    }

    public List<Respaldo> listar() {
        return respaldoRepository.findAll();
    }
}
