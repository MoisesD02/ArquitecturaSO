package com.simuladorso.memory.algoritmos.clock;

import java.util.ArrayList;
import java.util.List;

public class ResultadoClock {

    private List<PasoClock> pasos;

    private int totalFallos;

    private int totalAciertos;

    public ResultadoClock() {

        this.pasos =
                new ArrayList<>();

        this.totalFallos =
                0;

        this.totalAciertos =
                0;
    }

    public void agregarPaso(
            PasoClock paso) {

        pasos.add(paso);
    }

    public void incrementarFallos() {
        totalFallos++;
    }

    public void incrementarAciertos() {
        totalAciertos++;
    }

    public List<PasoClock> getPasos() {
        return pasos;
    }

    public int getTotalFallos() {
        return totalFallos;
    }

    public int getTotalAciertos() {
        return totalAciertos;
    }
}