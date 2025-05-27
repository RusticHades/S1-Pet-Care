package com.example.petcare;

public class Mascotas {
    public String idMascota;
    public String idUsuario;
    public String nombreMascota;
    public String especieMascota;
    public String razaMascota;
    public String fechaNacimientoMascota;
    public String edadMascota;
    public String sexoMascota;
    public String pesoMascota;
    public String esterilizadoMascota;

    public Mascotas(String idMascota, String nombreMascota, String especieMascota, String razaMascota, String edadMascota, String sexoMascota) {
        this.idMascota = idMascota;
        this.nombreMascota = nombreMascota;
        this.especieMascota = especieMascota;
        this.edadMascota = edadMascota;
        this.razaMascota = razaMascota;
        this.sexoMascota = sexoMascota;
    }

    public String getIdMascota() {
        return idMascota;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getNombreMascota() {
        return nombreMascota;
    }

    public String getEspecieMascota() {
        return especieMascota;
    }

    public String getRazaMascota() {
        return razaMascota;
    }

    public String getFechaNacimientoMascota() {
        return fechaNacimientoMascota;
    }

    public String getEdadMascota() {
        return edadMascota;
    }

    public String getSexoMascota() {
        return sexoMascota;
    }

    public String getPesoMascota() {
        return pesoMascota;
    }

    public String getEsterilizadoMascota() {
        return esterilizadoMascota;
    }
}
