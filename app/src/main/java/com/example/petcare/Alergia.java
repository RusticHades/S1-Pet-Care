package com.example.petcare;

public class Alergia {
    private int id;
    private String nombre;
    private String severidad;
    private String notas;

    public Alergia(int id, String nombre, String severidad, String notas) {
        this.id = id;
        this.nombre = nombre;
        this.severidad = severidad;
        this.notas = notas;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getSeveridad() { return severidad; }
    public String getNotas() { return notas; }
}