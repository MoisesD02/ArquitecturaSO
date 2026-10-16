package com.simuladorso.devices.model;

import java.util.Objects;

public class DispositivoEntradaSalida {

    private final String id;
    private final String nombre;
    private final String tipoConexion;
    private final String fabricante;
    private final String estado;
    private final int cantidadInterfaces;

    public DispositivoEntradaSalida(
            String id,
            String nombre,
            String tipoConexion,
            String fabricante,
            String estado) {

        this(id, nombre, tipoConexion, fabricante, estado, 1);
    }

    public DispositivoEntradaSalida(
            String id,
            String nombre,
            String tipoConexion,
            String fabricante,
            String estado,
            int cantidadInterfaces) {

        this.id = id;
        this.nombre = nombre;
        this.tipoConexion = tipoConexion;
        this.fabricante = fabricante;
        this.estado = estado;
        this.cantidadInterfaces = Math.max(1, cantidadInterfaces);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoConexion() {
        return tipoConexion;
    }

    public String getFabricante() {
        return fabricante;
    }

    public String getEstado() {
        return estado;
    }

    public int getCantidadInterfaces() {
        return cantidadInterfaces;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof DispositivoEntradaSalida)) {
            return false;
        }

        DispositivoEntradaSalida that =
                (DispositivoEntradaSalida) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
