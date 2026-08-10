package com.simuladorso.core;

public class Kernel {
    public enum EstadoKernel {
        DETENIDO,
        INICIALIZADO
    }

    private EstadoKernel estado = EstadoKernel.DETENIDO;

    public void iniciar() {
        estado = EstadoKernel.INICIALIZADO;
    }

    public void detener() {
        estado = EstadoKernel.DETENIDO;
    }

    public void reiniciar() {
        detener();
        iniciar();
    }

    public boolean estaActivo() {
        return estado == EstadoKernel.INICIALIZADO;
    }

    public EstadoKernel getEstado() {
        return estado;
    }
}
