package mx.unadm.rupe.model;

import jakarta.persistence.*;

/*
 * Usuario del panel administrativo.
 * En este avance se usa correo, contraseña, activo y rol.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String nombre;

    @Column(nullable=false, unique=true, length=120)
    private String correo;

    @Column(nullable=false, length=120)
    private String password;

    @Column(nullable=false)
    private Boolean activo = true;

    @ManyToOne
    @JoinColumn(name="rol_id")
    private Rol rol;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getPassword() { return password; }
    public Boolean getActivo() { return activo; }
    public Rol getRol() { return rol; }

    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setPassword(String password) { this.password = password; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public void setRol(Rol rol) { this.rol = rol; }
}
