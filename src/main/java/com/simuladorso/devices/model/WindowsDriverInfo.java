package com.simuladorso.devices.model;

public final class WindowsDriverInfo {
    private final String nombre, proveedor, version, fecha;

    public WindowsDriverInfo(String nombre, String proveedor, String version, String fecha) {
        this.nombre = nombre;
        this.proveedor = proveedor;
        this.version = version;
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public String getProveedor() {
        return proveedor;
    }

    public String getVersion() {
        return version;
    }

    public String getFecha() {
        return fecha;
    }
}
