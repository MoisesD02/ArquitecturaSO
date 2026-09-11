package com.simuladorso.memory;

public class Memoria {
    private final int totalMB;
    private int usadaMB;

    public Memoria(int totalMB) {
        this.totalMB = totalMB;
        this.usadaMB = 0;
    }

    public int getTotalMB() { return totalMB; }
    public int getUsadaMB() { return usadaMB; }
    public int getDisponibleMB() { return totalMB - usadaMB; }
    public double getPorcentajeUso() { return totalMB == 0 ? 0 : (double) usadaMB / totalMB; }

    public void setUsadaMB(int usadaMB) {
        this.usadaMB = Math.min(usadaMB, totalMB);
    }

    public void reiniciar() {
        this.usadaMB = 0;
    }
}