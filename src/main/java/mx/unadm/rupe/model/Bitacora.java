package mx.unadm.rupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora")
public class Bitacora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String usuario;

    @Column(length = 80)
    private String modulo;

    @Column(length = 250)
    private String accion;

    @Column(length = 50)
    private String ip;

    private LocalDateTime fechaHora = LocalDateTime.now();

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
