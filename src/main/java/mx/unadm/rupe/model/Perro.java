package mx.unadm.rupe.model;

import jakarta.persistence.*;

@Entity
@Table(name = "perros")
public class Perro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 80)
    private String raza;

    @Column(nullable = false, length = 80)
    private String color;

    @Column(length = 30)
    private String tamano;

    @Column(length = 20)
    private String sexo;

    @Column(nullable = false, length = 300)
    private String senasParticulares;

    @Column(length = 250)
    private String fotoRuta;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getTamano() { return tamano; }
    public void setTamano(String tamano) { this.tamano = tamano; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public String getSenasParticulares() { return senasParticulares; }
    public void setSenasParticulares(String senasParticulares) { this.senasParticulares = senasParticulares; }
    public String getFotoRuta() { return fotoRuta; }
    public void setFotoRuta(String fotoRuta) { this.fotoRuta = fotoRuta; }
}
