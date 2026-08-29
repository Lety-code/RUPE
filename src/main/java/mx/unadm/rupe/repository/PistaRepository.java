        package mx.unadm.rupe.repository;

        import mx.unadm.rupe.model.Pista;
        import org.springframework.data.jpa.repository.JpaRepository;
        import java.util.List;

        public interface PistaRepository extends JpaRepository<Pista, Long> {
    List<Pista> findByReporteFolioOrderByFechaRegistroDesc(String folio);
}
