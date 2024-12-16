package com.example.glem_laboratorio; // Asegúrate de usar el paquete de tu proyecto.

public class Solicitud {
    private String laboratorio;
    private String fecha;
    private String hora;
    private String estado;
    private String idprestamo;

    // Constructor
    public Solicitud(String laboratorio, String fecha, String hora, String estado, String idprestamo) {
        this.laboratorio = laboratorio;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.idprestamo = idprestamo;
    }

    // Getters
    public String getLaboratorio() {
        return laboratorio;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getEstado() {
        return estado;
    }

    public String getIdprestamo() {
        return idprestamo;
    }

    public void setIdprestamo(String idprestamo) {
        this.idprestamo = idprestamo;
    }

    // Setters (opcionales, si planeas modificar los datos en algún momento)
    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
