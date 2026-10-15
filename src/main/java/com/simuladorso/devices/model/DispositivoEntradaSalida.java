package com.simuladorso.devices.model;

import java.util.Objects;

public class DispositivoEntradaSalida {
    private final String nombre;
    private final String tipoConexion;
    private final String driver;

    public DispositivoEntradaSalida(String nombre, String tipoConexion, String driver) {
        this.nombre = nombre;
        this.tipoConexion = tipoConexion;
        this.driver = driver;
    }

    public String getNombre() { return nombre; }
    public String getTipoConexion() { return tipoConexion; }
    public String getDriver() { return driver; }

    // Necesario para comparar si un dispositivo fue desconectado/conectado
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispositivoEntradaSalida that = (DispositivoEntradaSalida) o;
        return Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}