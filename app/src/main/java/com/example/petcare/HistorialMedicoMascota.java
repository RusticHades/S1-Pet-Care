package com.example.petcare;

public class HistorialMedicoMascota {
    private int idHistorial;
    private String fechaConsulta;
    private String motivoConsulta;
    private String diagnostico;
    private String tratamiento;
    private String observaciones;
    private Double pesoActual;
    private Double temperatura;
    private String vacunasAplicadas;

    public HistorialMedicoMascota(int idHistorial, String fechaConsulta, String motivoConsulta,
                                  String diagnostico, String tratamiento, String observaciones,
                                  Double pesoActual, Double temperatura, String vacunasAplicadas) {
        this.idHistorial = idHistorial;
        this.fechaConsulta = fechaConsulta;
        this.motivoConsulta = motivoConsulta;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.observaciones = observaciones;
        this.pesoActual = pesoActual;
        this.temperatura = temperatura;
        this.vacunasAplicadas = vacunasAplicadas;
    }

    // Getters
    public int getIdHistorial() {
        return idHistorial;
    }

    public String getFechaConsulta() {
        return fechaConsulta;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public Double getPesoActual() {
        return pesoActual;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public String getVacunasAplicadas() {
        return vacunasAplicadas;
    }
}