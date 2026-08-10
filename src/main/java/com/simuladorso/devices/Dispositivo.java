package com.simuladorso.devices;

public class Dispositivo {
    private final String nombre;
    private final String tipo;
    private final boolean disponible;

    public Dispositivo(String nombre, String tipo, boolean disponible) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.disponible = disponible;
    }

    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public boolean isDisponible() { return disponible; }
}
