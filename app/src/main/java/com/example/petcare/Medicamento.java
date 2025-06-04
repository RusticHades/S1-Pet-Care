package com.example.petcare;

public class Medicamento {
    private int id;
    private String nombre;
    private String dosis;
    private String frecuencia;
    private String motivo;
    private String fechaInicio;
    private String fechaFin;
    private String notas;

    public Medicamento(int id, String nombre, String dosis, String frecuencia, String motivo, String fechaInicio, String fechaFin, String notas) {
        this.id = id;
        this.nombre = nombre;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.motivo = motivo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.notas = notas;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDosis() { return dosis; }
    public String getFrecuencia() { return frecuencia; }
    public String getMotivo() { return motivo; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public String getNotas() { return notas; }
}