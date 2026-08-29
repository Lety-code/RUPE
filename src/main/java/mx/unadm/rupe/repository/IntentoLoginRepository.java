        package mx.unadm.rupe.repository;

        import mx.unadm.rupe.model.IntentoLogin;
        import org.springframework.data.jpa.repository.JpaRepository;
        import java.time.LocalDateTime;

        public interface IntentoLoginRepository extends JpaRepository<IntentoLogin, Long> {
    long countByCorreoIgnoreCaseAndExitosoFalseAndFechaHoraAfter(String correo, LocalDateTime fecha);
    void deleteByCorreoIgnoreCaseAndExitosoFalse(String correo);
}
