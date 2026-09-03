package com.simuladorso.process.Algoritmos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlanDosNiveles {

    public record ProcesoPlanificado(int pid, String nombre, int memoriaMb, int rafagaCpu) {
    }

    public record Resultado(
            int memoriaTotalMb,
            List<ProcesoPlanificado> memoriaInicial,
            List<ProcesoPlanificado> secundariaInicial,
            List<ProcesoPlanificado> ordenEjecucion,
            List<String> eventos
    ) {
    }

    public Resultado simular(int memoriaTotalMb, List<ProcesoPlanificado> procesos) {
        List<ProcesoPlanificado> memoria = new ArrayList<>();
        List<ProcesoPlanificado> secundaria = new ArrayList<>();
        List<ProcesoPlanificado> memoriaInicial = new ArrayList<>();
        List<ProcesoPlanificado> secundariaInicial = new ArrayList<>();
        List<ProcesoPlanificado> ordenEjecucion = new ArrayList<>();
        List<String> eventos = new ArrayList<>();

        int memoriaDisponible = memoriaTotalMb;

        for (ProcesoPlanificado proceso : procesos) {
            if (proceso.memoriaMb() <= memoriaDisponible) {
                memoria.add(proceso);
                memoriaInicial.add(proceso);
                memoriaDisponible -= proceso.memoriaMb();
                eventos.add("Nivel 1: " + proceso.nombre() + " entra a memoria principal.");
            } else {
                secundaria.add(proceso);
                secundariaInicial.add(proceso);
                eventos.add("Nivel 2: " + proceso.nombre() + " espera en memoria secundaria.");
            }
        }

        int tiempo = 0;
        while (!memoria.isEmpty() || !secundaria.isEmpty()) {
            if (memoria.isEmpty()) {
                ProcesoPlanificado cargado = secundaria.removeFirst();
                memoria.add(cargado);
                memoriaDisponible -= cargado.memoriaMb();
                eventos.add("Swap-in: " + cargado.nombre() + " sube desde memoria secundaria.");
            }

            ProcesoPlanificado actual = memoria.removeFirst();
            ordenEjecucion.add(actual);
            tiempo += actual.rafagaCpu();
            memoriaDisponible += actual.memoriaMb();
            eventos.add("CPU: " + actual.nombre() + " termina en t=" + tiempo
                    + " y libera " + actual.memoriaMb() + " MB.");

            Iterator<ProcesoPlanificado> pendientes = secundaria.iterator();
            while (pendientes.hasNext()) {
                ProcesoPlanificado candidato = pendientes.next();
                if (candidato.memoriaMb() <= memoriaDisponible) {
                    memoria.add(candidato);
                    memoriaDisponible -= candidato.memoriaMb();
                    pendientes.remove();
                    eventos.add("Swap-in: " + candidato.nombre()
                            + " pasa al nivel 1 porque ya hay memoria.");
                }
            }
        }

        return new Resultado(
                memoriaTotalMb,
                List.copyOf(memoriaInicial),
                List.copyOf(secundariaInicial),
                List.copyOf(ordenEjecucion),
                List.copyOf(eventos)
        );
    }
}
