package com.example.petcare;

public class Citas {
    private int id;
    private String fechaHora;
    private String motivo;
    private String notas;
    private String estado;

    public Citas(int id, String fechaHora, String motivo, String notas, String estado) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.notas = notas;
        this.estado = estado;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getNotas() {
        return notas;
    }

    public String getEstado() {
        return estado;
    }
}