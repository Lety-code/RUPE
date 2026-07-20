package mx.unadm.rupe.repository;

import mx.unadm.rupe.model.ReporteExtravio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/* Repositorio para reportes. */
public interface ReporteExtravioRepository extends JpaRepository<ReporteExtravio, Long> {
    Optional<ReporteExtravio> findByFolio(String folio);
}
