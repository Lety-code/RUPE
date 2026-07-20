package mx.unadm.rupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * Bitácora del sistema.
 * Registra acciones importantes.
 */
@Entity
@Table(name = "bitacora")
public class Bitacora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuario;
    private String modulo;
    private String accion;
    private String ip;
    private LocalDateTime fechaHora = LocalDateTime.now();

    public Bitacora() {}
    public Bitacora(String usuario, String modulo, String accion, String ip) {
        this.usuario = usuario;
        this.modulo = modulo;
        this.accion = accion;
        this.ip = ip;
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getModulo() { return modulo; }
    public String getAccion() { return accion; }
    public String getIp() { return ip; }
    public LocalDateTime getFechaHora() { return fechaHora; }

    public void setId(Long id) { this.id = id; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public void setModulo(String modulo) { this.modulo = modulo; }
    public void setAccion(String accion) { this.accion = accion; }
    public void setIp(String ip) { this.ip = ip; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
