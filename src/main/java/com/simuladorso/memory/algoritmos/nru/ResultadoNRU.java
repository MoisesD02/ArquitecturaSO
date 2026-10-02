package com.simuladorso.memory.algoritmos.nru;

import java.util.ArrayList;
import java.util.List;

public class ResultadoNRU {

    private List<PasoNRU> pasos;

    private int totalFallos;

    private int totalAciertos;

    public ResultadoNRU() {

        this.pasos =
                new ArrayList<>();

        this.totalFallos =
                0;

        this.totalAciertos =
                0;
    }

    public void agregarPaso(
            PasoNRU paso) {

        pasos.add(paso);
    }

    public void incrementarFallos() {
        totalFallos++;
    }

    public void incrementarAciertos() {
        totalAciertos++;
    }

    public List<PasoNRU> getPasos() {
        return pasos;
    }

    public int getTotalFallos() {
        return totalFallos;
    }

    public int getTotalAciertos() {
        return totalAciertos;
    }
}