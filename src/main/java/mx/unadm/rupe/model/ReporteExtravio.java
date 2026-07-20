package mx.unadm.rupe.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Reporte de extravío.
 * Une tutor + perro + lugar + fecha + folio.
 */
@Entity
@Table(name = "reportes_extravio")
public class ReporteExtravio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, length=30)
    private String folio;

    @ManyToOne(cascade = CascadeType.ALL)
    private Tutor tutor = new Tutor();

    @ManyToOne(cascade = CascadeType.ALL)
    private Perro perro = new Perro();

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate fechaExtravio;

    private String lugarExtravio;
    private String descripcion;
    private String estado = "Pendiente";
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Long getId() { return id; }
    public String getFolio() { return folio; }
    public Tutor getTutor() { return tutor; }
    public Perro getPerro() { return perro; }
    public LocalDate getFechaExtravio() { return fechaExtravio; }
    public String getLugarExtravio() { return lugarExtravio; }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    public void setId(Long id) { this.id = id; }
    public void setFolio(String folio) { this.folio = folio; }
    public void setTutor(Tutor tutor) { this.tutor = tutor; }
    public void setPerro(Perro perro) { this.perro = perro; }
    public void setFechaExtravio(LocalDate fechaExtravio) { this.fechaExtravio = fechaExtravio; }
    public void setLugarExtravio(String lugarExtravio) { this.lugarExtravio = lugarExtravio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
