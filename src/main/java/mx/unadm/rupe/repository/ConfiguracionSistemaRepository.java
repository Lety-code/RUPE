        package mx.unadm.rupe.repository;

        import mx.unadm.rupe.model.ConfiguracionSistema;
        import org.springframework.data.jpa.repository.JpaRepository;
        import java.util.Optional;

        public interface ConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistema, Long> {
    Optional<ConfiguracionSistema> findByClave(String clave);
}
