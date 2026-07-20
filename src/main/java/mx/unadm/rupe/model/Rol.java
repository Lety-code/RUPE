package mx.unadm.rupe.model;

import jakarta.persistence.*;

/*
 * Rol del usuario.
 * Ejemplos: ADMINISTRADOR, CAPTURISTA.
 */
@Entity
@Table(name = "roles")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=50)
    private String nombre;

    public Rol() {}
    public Rol(String nombre) { this.nombre = nombre; }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
