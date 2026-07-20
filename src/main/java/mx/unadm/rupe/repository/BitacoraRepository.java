package mx.unadm.rupe.repository;

import mx.unadm.rupe.model.Bitacora;
import org.springframework.data.jpa.repository.JpaRepository;

/* Repositorio para bitácora. */
public interface BitacoraRepository extends JpaRepository<Bitacora, Long> {
}
