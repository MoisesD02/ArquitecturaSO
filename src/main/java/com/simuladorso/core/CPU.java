package com.simuladorso.core;

public class CPU {
    public enum EstadoCPU { DETENIDA, LIBRE }

    private EstadoCPU estado = EstadoCPU.DETENIDA;

    public void iniciar() {
        estado = EstadoCPU.LIBRE;
    }

    public void detener() {
        estado = EstadoCPU.DETENIDA;
    }

    public EstadoCPU getEstado() {
        return estado;
    }
}
