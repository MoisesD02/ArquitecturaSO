package com.simuladorso.memory.algoritmos;

import java.util.ArrayList;
import java.util.List;

public class ResultadoReemplazo {

    private List<PasoReemplazo> pasos;
    private int totalFallos;
    private int totalAciertos;

    public ResultadoReemplazo() {
        this.pasos = new ArrayList<>();
        this.totalFallos = 0;
        this.totalAciertos = 0;
    }

    public void agregarPaso(PasoReemplazo paso) {
        pasos.add(paso);
    }

    public void incrementarFallos() {
        totalFallos++;
    }

    public void incrementarAciertos() {
        totalAciertos++;
    }

    public List<PasoReemplazo> getPasos() {
        return pasos;
    }

    public int getTotalFallos() {
        return totalFallos;
    }

    public int getTotalAciertos() {
        return totalAciertos;
    }
}