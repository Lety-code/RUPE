package mx.unadm.rupe.service;

import mx.unadm.rupe.model.*;
import mx.unadm.rupe.repository.*;
import mx.unadm.rupe.util.ValidacionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReporteService {
    private final ReporteExtravioRepository reporteRepository;
    private final FotoReporteRepository fotoReporteRepository;
    private final CodigoQrRepository codigoQrRepository;
    private final ArchivoService archivoService;

    public ReporteService(ReporteExtravioRepository reporteRepository,
                          FotoReporteRepository fotoReporteRepository,
                          CodigoQrRepository codigoQrRepository,
                          ArchivoService archivoService) {
        this.reporteRepository = reporteRepository;
        this.fotoReporteRepository = fotoReporteRepository;
        this.codigoQrRepository = codigoQrRepository;
        this.archivoService = archivoService;
    }

    @Transactional
    public ReporteExtravio crearReporte(String tutorNombre,
                                         String tutorTelefono,
                                         String tutorCorreo,
                                         String tutorDireccion,
                                         String perroNombre,
                                         String raza,
                                         String color,
                                         String tamano,
                                         String sexo,
                                         String senas,
                                         String fechaExtravioTexto,
                                         String lugarExtravio,
                                         String descripcion,
                                         MultipartFile foto) throws IOException {

        ValidacionUtil.validarTextoObligatorio(tutorNombre, "nombre del tutor");
        ValidacionUtil.validarTelefono(tutorTelefono);
        ValidacionUtil.validarTextoObligatorio(perroNombre, "nombre del perro");
        ValidacionUtil.validarColor(color);
        ValidacionUtil.validarSenas(senas);
        LocalDate fechaExtravio = ValidacionUtil.validarFechaExtravio(fechaExtravioTexto);
        if (ValidacionUtil.estaVacio(lugarExtravio)) {
            throw new IllegalArgumentException("El lugar del extravío es obligatorio.");
        }

        Tutor tutor = new Tutor();
        tutor.setNombre(tutorNombre.trim());
        tutor.setTelefono(tutorTelefono.trim());
        tutor.setCorreo(tutorCorreo == null ? null : tutorCorreo.trim());
        tutor.setDireccion(tutorDireccion == null ? null : tutorDireccion.trim());

        Perro perro = new Perro();
        perro.setNombre(perroNombre.trim());
        perro.setRaza(raza == null ? null : raza.trim());
        perro.setColor(color.trim());
        perro.setTamano(tamano == null ? null : tamano.trim());
        perro.setSexo(sexo == null ? null : sexo.trim());
        perro.setSenasParticulares(senas.trim());

        String fotoRuta = archivoService.guardarImagen(foto, "reportes");
        perro.setFotoRuta(fotoRuta);

        ReporteExtravio reporte = new ReporteExtravio();
        reporte.setTutor(tutor);
        reporte.setPerro(perro);
        reporte.setFechaExtravio(fechaExtravio);
        reporte.setLugarExtravio(lugarExtravio.trim());
        reporte.setDescripcion(descripcion == null ? null : descripcion.trim());
        reporte.setEstado("ABIERTO");
        reporte.setFolio(generarFolioSeguro());

        ReporteExtravio guardado = reporteRepository.save(reporte);

        if (fotoRuta != null) {
            FotoReporte fr = new FotoReporte();
            fr.setReporte(guardado);
            fr.setRutaArchivo(fotoRuta);
            fr.setDescripcion("Fotografía principal del perro");
            fotoReporteRepository.save(fr);
        }

        CodigoQr qr = new CodigoQr();
        qr.setReporte(guardado);
        qr.setContenido("/reportes/" + guardado.getFolio());
        codigoQrRepository.save(qr);

        return guardado;
    }

    public String generarFolioSeguro() {
        int year = LocalDate.now().getYear();
        long consecutivo = reporteRepository.count() + 1;
        String folio;
        do {
            folio = String.format("RUPE-%d-%04d", year, consecutivo++);
        } while (reporteRepository.findByFolio(folio).isPresent());
        return folio;
    }

    public Optional<ReporteExtravio> buscarPorFolio(String folio) {
        if (folio == null || folio.trim().isEmpty()) return Optional.empty();
        return reporteRepository.findByFolio(folio.trim().toUpperCase());
    }

    public List<ReporteExtravio> listarReportes() {
        return reporteRepository.findAllByOrderByFechaRegistroDesc();
    }

    public long total() { return reporteRepository.count(); }
    public long abiertos() { return reporteRepository.countByEstado("ABIERTO"); }
    public long localizados() { return reporteRepository.countByEstado("LOCALIZADO"); }
    public long cerrados() { return reporteRepository.countByEstado("CERRADO"); }

    public ReporteExtravio cambiarEstado(Long id, String nuevoEstado) {
        ReporteExtravio reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado."));
        reporte.setEstado(nuevoEstado);
        return reporteRepository.save(reporte);
    }
}
