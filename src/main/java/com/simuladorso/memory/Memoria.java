package com.simuladorso.memory;

public class Memoria {
    private final int totalMB;
    private int usadaMB;

    public Memoria(int totalMB) {
        this.totalMB = totalMB;
    }

    public int getTotalMB() { return totalMB; }
    public int getUsadaMB() { return usadaMB; }
    public int getDisponibleMB() { return totalMB - usadaMB; }
    public double getPorcentajeUso() { return totalMB == 0 ? 0 : (double) usadaMB / totalMB; }
    public void reiniciar() { usadaMB = 0; }
}
