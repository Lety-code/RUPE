package mx.unadm.rupe.service;

import mx.unadm.rupe.model.ReporteExtravio;
import mx.unadm.rupe.repository.ReporteExtravioRepository;
import org.springframework.stereotype.Service;
import java.time.Year;
import java.util.Optional;

/* Servicio para guardar y consultar reportes. */
@Service
public class ReporteService {
    private final ReporteExtravioRepository repo;
    private final BitacoraService bitacora;

    public ReporteService(ReporteExtravioRepository repo, BitacoraService bitacora) {
        this.repo = repo;
        this.bitacora = bitacora;
    }

    public ReporteExtravio guardar(ReporteExtravio reporte, String ip) {
        reporte.setFolio("RUPE-" + Year.now().getValue() + "-" + String.format("%04d", repo.count() + 1));
        ReporteExtravio guardado = repo.save(reporte);
        bitacora.registrar("Tutor", "Reportes", "Registró reporte " + guardado.getFolio(), ip);
        return guardado;
    }

    public Optional<ReporteExtravio> buscarFolio(String folio) {
        return repo.findByFolio(folio);
    }

    public long total() {
        return repo.count();
    }
}
