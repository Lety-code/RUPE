package mx.unadm.rupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "respaldos")
public class Respaldo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombreArchivo;

    @Column(nullable = false, length = 250)
    private String rutaArchivo;

    @Column(length = 120)
    private String usuario;

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    
    public String getFechaGeneracionFormateada() {
    if (fechaGeneracion == null) {
        return "";
    }

    DateTimeFormatter formato =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    return fechaGeneracion.format(formato);
}
}
