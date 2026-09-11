package com.simuladorso.memory;

public class BloqueMemoria {
    private int id;
    private int direccionInicio;
    private int tamano;
    private EstadoBloque estado;
    private String procesoAsignado;

    public BloqueMemoria(int id, int direccionInicio, int tamano, EstadoBloque estado, String procesoAsignado) {
        this.id = id;
        this.direccionInicio = direccionInicio;
        this.tamano = tamano;
        this.estado = estado;
        this.procesoAsignado = procesoAsignado;
    }

    public int getId() { return id; }
    public int getDireccionInicio() { return direccionInicio; }
    public int getTamano() { return tamano; }
    public EstadoBloque getEstado() { return estado; }
    public String getProcesoAsignado() { return procesoAsignado; }

    public void setEstado(EstadoBloque estado) { this.estado = estado; }
    public void setProcesoAsignado(String procesoAsignado) { this.procesoAsignado = procesoAsignado; }
}