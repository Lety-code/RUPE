package mx.unadm.rupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fichas_pdf")
public class FichaPdf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reporte_id")
    private ReporteExtravio reporte;

    @Column(nullable = false, length = 250)
    private String rutaPdf;

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ReporteExtravio getReporte() { return reporte; }
    public void setReporte(ReporteExtravio reporte) { this.reporte = reporte; }
    public String getRutaPdf() { return rutaPdf; }
    public void setRutaPdf(String rutaPdf) { this.rutaPdf = rutaPdf; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
}
