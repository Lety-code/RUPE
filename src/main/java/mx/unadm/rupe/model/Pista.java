package mx.unadm.rupe.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "pistas")
public class Pista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reporte_id")
    private ReporteExtravio reporte;

    @Column(nullable = false, length = 250)
    private String lugar;

    private LocalDate fecha;
    private LocalTime hora;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 250)
    private String fotoRuta;

    @Column(nullable = false)
    private boolean resguardado = false;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ReporteExtravio getReporte() { return reporte; }
    public void setReporte(ReporteExtravio reporte) { this.reporte = reporte; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFotoRuta() { return fotoRuta; }
    public void setFotoRuta(String fotoRuta) { this.fotoRuta = fotoRuta; }
    public boolean isResguardado() { return resguardado; }
    public void setResguardado(boolean resguardado) { this.resguardado = resguardado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
