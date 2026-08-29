package mx.unadm.rupe.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_extravio", indexes = {
        @Index(name = "idx_reporte_folio", columnList = "folio"),
        @Index(name = "idx_reporte_estado", columnList = "estado")
})
public class ReporteExtravio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String folio;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "perro_id")
    private Perro perro;

    @Column(nullable = false)
    private LocalDate fechaExtravio;

    @Column(nullable = false, length = 250)
    private String lugarExtravio;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String estado = "ABIERTO";

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public Tutor getTutor() { return tutor; }
    public void setTutor(Tutor tutor) { this.tutor = tutor; }
    public Perro getPerro() { return perro; }
    public void setPerro(Perro perro) { this.perro = perro; }
    public LocalDate getFechaExtravio() { return fechaExtravio; }
    public void setFechaExtravio(LocalDate fechaExtravio) { this.fechaExtravio = fechaExtravio; }
    public String getLugarExtravio() { return lugarExtravio; }
    public void setLugarExtravio(String lugarExtravio) { this.lugarExtravio = lugarExtravio; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
