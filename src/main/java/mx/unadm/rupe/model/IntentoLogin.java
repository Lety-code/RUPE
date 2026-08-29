package mx.unadm.rupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "intentos_login", indexes = @Index(name = "idx_intentos_login_correo", columnList = "correo"))
public class IntentoLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String correo;

    @Column(length = 50)
    private String ip;

    @Column(nullable = false)
    private boolean exitoso = false;

    @Column(nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    private LocalDateTime bloqueadoHasta;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public boolean isExitoso() { return exitoso; }
    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }
}
