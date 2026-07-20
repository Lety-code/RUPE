package mx.unadm.rupe.repository;

import mx.unadm.rupe.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

/* Repositorio para roles. */
public interface RolRepository extends JpaRepository<Rol, Long> {
}
