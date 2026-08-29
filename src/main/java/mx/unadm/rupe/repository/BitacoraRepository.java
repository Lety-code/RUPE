        package mx.unadm.rupe.repository;

        import mx.unadm.rupe.model.Bitacora;
        import org.springframework.data.jpa.repository.JpaRepository;
        import java.util.List;

        public interface BitacoraRepository extends JpaRepository<Bitacora, Long> {
    List<Bitacora> findAllByOrderByFechaHoraDesc();
}
