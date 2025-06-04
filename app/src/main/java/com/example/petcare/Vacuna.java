package com.example.petcare;

public class Vacuna {
    private int id;
    private String nombre;
    private String fechaAplicacion;
    private String fechaRefuerzo;
    private String veterinario;
    private String notas;

    public Vacuna(int id, String nombre, String fechaAplicacion, String fechaRefuerzo, String veterinario, String notas) {
        this.id = id;
        this.nombre = nombre;
        this.fechaAplicacion = fechaAplicacion;
        this.fechaRefuerzo = fechaRefuerzo;
        this.veterinario = veterinario;
        this.notas = notas;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getFechaAplicacion() { return fechaAplicacion; }
    public String getFechaRefuerzo() { return fechaRefuerzo; }
    public String getVeterinario() { return veterinario; }
    public String getNotas() { return notas; }
}