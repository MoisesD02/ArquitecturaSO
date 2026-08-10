package com.simuladorso.process;

public class Proceso {
    private final int pid;
    private final String nombre;
    private final int memoriaMB;
    private EstadoProceso estado;

    public Proceso(int pid, String nombre, int memoriaMB) {
        this.pid = pid;
        this.nombre = nombre;
        this.memoriaMB = memoriaMB;
        this.estado = EstadoProceso.NUEVO;
    }

    public int getPid() {
        return pid;
    }

    public String getNombre() {
        return nombre;
    }

    public int getMemoriaMB() {
        return memoriaMB;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return String.format("PID %03d   %-18s   %-10s   %d MB", pid, nombre, estado, memoriaMB);
    }
}
