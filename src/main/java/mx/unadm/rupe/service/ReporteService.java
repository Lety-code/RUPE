package mx.unadm.rupe.service;

import mx.unadm.rupe.model.*;
import mx.unadm.rupe.repository.ReporteExtravioRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReporteService {
    private final ReporteExtravioRepository reporteRepository;

    public ReporteService(ReporteExtravioRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

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
                                         LocalDate fechaExtravio,
                                         String lugarExtravio,
                                         String descripcion) {
        validarObligatorios(tutorNombre, tutorTelefono, perroNombre, lugarExtravio);

        Tutor tutor = new Tutor();
        tutor.setNombre(tutorNombre.trim());
        tutor.setTelefono(tutorTelefono.trim());
        tutor.setCorreo(tutorCorreo);
        tutor.setDireccion(tutorDireccion);

        Perro perro = new Perro();
        perro.setNombre(perroNombre.trim());
        perro.setRaza(raza);
        perro.setColor(color);
        perro.setTamano(tamano);
        perro.setSexo(sexo);
        perro.setSenasParticulares(senas);

        ReporteExtravio reporte = new ReporteExtravio();
        reporte.setTutor(tutor);
        reporte.setPerro(perro);
        reporte.setFechaExtravio(fechaExtravio);
        reporte.setLugarExtravio(lugarExtravio.trim());
        reporte.setDescripcion(descripcion);
        reporte.setEstado("ABIERTO");
        reporte.setFolio(generarFolio());

        return reporteRepository.save(reporte);
    }

    private void validarObligatorios(String tutorNombre, String tutorTelefono, String perroNombre, String lugarExtravio) {
        if (esVacio(tutorNombre)) {
            throw new IllegalArgumentException("El nombre del tutor es obligatorio.");
        }
        if (esVacio(tutorTelefono)) {
            throw new IllegalArgumentException("El teléfono del tutor es obligatorio.");
        }
        if (esVacio(perroNombre)) {
            throw new IllegalArgumentException("El nombre del perro es obligatorio.");
        }
        if (esVacio(lugarExtravio)) {
            throw new IllegalArgumentException("El lugar del extravío es obligatorio.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    public String generarFolio() {
        long consecutivo = reporteRepository.count() + 1;
        int year = java.time.LocalDate.now().getYear();
        return String.format("RUPE-%d-%04d", year, consecutivo);
    }

    public Optional<ReporteExtravio> buscarPorFolio(String folio) {
        if (folio == null || folio.trim().isEmpty()) {
            return Optional.empty();
        }
        return reporteRepository.findByFolio(folio.trim().toUpperCase());
    }

    public List<ReporteExtravio> listarReportes() {
        return reporteRepository.findAll();
    }

    public long total() {
        return reporteRepository.count();
    }

    public long abiertos() {
        return reporteRepository.countByEstado("ABIERTO");
    }

    public long localizados() {
        return reporteRepository.countByEstado("LOCALIZADO");
    }
}
