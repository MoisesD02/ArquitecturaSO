package com.simuladorso.memory.algoritmos.secondchance;

import java.util.ArrayList;
import java.util.List;

public class ResultadoSecondChance {

    private List<PasoSecondChance> pasos;

    private int totalFallos;

    private int totalAciertos;

    public ResultadoSecondChance() {

        this.pasos =
                new ArrayList<>();

        this.totalFallos =
                0;

        this.totalAciertos =
                0;
    }

    public void agregarPaso(
            PasoSecondChance paso) {

        pasos.add(paso);
    }

    public void incrementarFallos() {
        totalFallos++;
    }

    public void incrementarAciertos() {
        totalAciertos++;
    }

    public List<PasoSecondChance> getPasos() {
        return pasos;
    }

    public int getTotalFallos() {
        return totalFallos;
    }

    public int getTotalAciertos() {
        return totalAciertos;
    }
}