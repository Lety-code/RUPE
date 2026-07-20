package mx.unadm.rupe.model;

import jakarta.persistence.*;

/*
 * Perro extraviado.
 * Guarda datos básicos para identificarlo.
 */
@Entity
@Table(name = "perros")
public class Perro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String raza;
    private String color;
    private String senasParticulares;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRaza() { return raza; }
    public String getColor() { return color; }
    public String getSenasParticulares() { return senasParticulares; }

    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setRaza(String raza) { this.raza = raza; }
    public void setColor(String color) { this.color = color; }
    public void setSenasParticulares(String senasParticulares) { this.senasParticulares = senasParticulares; }
}
