package mx.unadm.rupe.service;

import mx.unadm.rupe.model.Pista;
import mx.unadm.rupe.model.ReporteExtravio;
import mx.unadm.rupe.repository.PistaRepository;
import mx.unadm.rupe.repository.ReporteExtravioRepository;
import mx.unadm.rupe.util.ValidacionUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class PistaService {
    private final PistaRepository pistaRepository;
    private final ReporteExtravioRepository reporteRepository;
    private final ArchivoService archivoService;

    public PistaService(PistaRepository pistaRepository, ReporteExtravioRepository reporteRepository, ArchivoService archivoService) {
        this.pistaRepository = pistaRepository;
        this.reporteRepository = reporteRepository;
        this.archivoService = archivoService;
    }

    public Pista registrarPista(String folio, String lugar, String fecha, String hora, String descripcion, boolean resguardado, MultipartFile foto) throws IOException {
        ReporteExtravio reporte = reporteRepository.findByFolio(folio)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el reporte para registrar la pista."));
        if (ValidacionUtil.estaVacio(lugar)) throw new IllegalArgumentException("El lugar de la pista es obligatorio.");

        Pista pista = new Pista();
        pista.setReporte(reporte);
        pista.setLugar(lugar.trim());
        if (!ValidacionUtil.estaVacio(fecha)) pista.setFecha(LocalDate.parse(fecha));
        if (!ValidacionUtil.estaVacio(hora)) pista.setHora(LocalTime.parse(hora));
        pista.setDescripcion(descripcion == null ? null : descripcion.trim());
        pista.setResguardado(resguardado);
        pista.setFotoRuta(archivoService.guardarImagen(foto, "pistas"));
        return pistaRepository.save(pista);
    }

    public List<Pista> listarPorFolio(String folio) {
        return pistaRepository.findByReporteFolioOrderByFechaRegistroDesc(folio);
    }
}
