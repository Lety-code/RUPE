package mx.unadm.rupe.repository;

import mx.unadm.rupe.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/* Repositorio para usuarios. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
}
