package mx.unadm.rupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fotos_reportes")
public class FotoReporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reporte_id")
    private ReporteExtravio reporte;

    @Column(nullable = false, length = 250)
    private String rutaArchivo;

    @Column(length = 150)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ReporteExtravio getReporte() { return reporte; }
    public void setReporte(ReporteExtravio reporte) { this.reporte = reporte; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }
}
