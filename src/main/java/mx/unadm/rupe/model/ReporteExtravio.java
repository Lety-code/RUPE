package mx.unadm.rupe.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_extravio")
public class ReporteExtravio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String folio;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "perro_id")
    private Perro perro;

    private LocalDate fechaExtravio;

    @Column(length = 250)
    private String lugarExtravio;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 50)
    private String estado = "ABIERTO";

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
