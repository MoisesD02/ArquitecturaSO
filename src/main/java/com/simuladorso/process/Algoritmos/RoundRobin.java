package com.simuladorso.process.Algoritmos;

import com.simuladorso.process.Proceso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobin {

    // =========================================
    // SEGMENTO DEL DIAGRAMA DE GANTT
    // =========================================

    public static class Segmento {

        private final Proceso proceso;
        private final int inicio;
        private final int fin;

        public Segmento(
                Proceso proceso,
                int inicio,
                int fin) {

            this.proceso = proceso;
            this.inicio = inicio;
            this.fin = fin;
        }

        public Proceso getProceso() {
            return proceso;
        }

        public int getInicio() {
            return inicio;
        }

        public int getFin() {
            return fin;
        }

        public boolean esCPUOciosa() {
            return proceso == null;
        }
    }


    // =========================================
    // RESULTADO
    // =========================================

    public static class Resultado {

        private final List<Segmento> segmentos;
        private final int tiempoTotal;
        private final double esperaPromedio;
        private final double respuestaPromedio;

        public Resultado(
                List<Segmento> segmentos,
                int tiempoTotal,
                double esperaPromedio,
                double respuestaPromedio) {

            this.segmentos = segmentos;
            this.tiempoTotal = tiempoTotal;
            this.esperaPromedio = esperaPromedio;
            this.respuestaPromedio = respuestaPromedio;
        }

        public List<Segmento> getSegmentos() {
            return segmentos;
        }

        public int getTiempoTotal() {
            return tiempoTotal;
        }

        public double getEsperaPromedio() {
            return esperaPromedio;
        }

        public double getRespuestaPromedio() {
            return respuestaPromedio;
        }
    }


    // =========================================
    // ALGORITMO ROUND ROBIN
    // =========================================

    public Resultado planificar(List<Proceso> procesosOriginales, int quantum) {
        if (procesosOriginales == null || procesosOriginales.isEmpty() || quantum <= 0)
            throw new IllegalArgumentException("Se requieren procesos y quantum positivo.");
        List<Proceso> lote = new ArrayList<>(procesosOriginales);
        for (Proceso p : lote) if (p == null || p.getRafagaCPU() <= 0)
            throw new IllegalArgumentException("La ráfaga debe ser positiva.");
        lote.sort(Comparator.comparingInt(Proceso::getPid));
        lote.forEach(Proceso::reiniciarSimulacion);
        Queue<Proceso> colaListos = new LinkedList<>(lote);
        List<Segmento> segmentos = new ArrayList<>();
        int tiempo = 0;
        double espera = 0, respuesta = 0;
        while (!colaListos.isEmpty()) {
            Proceso actual = colaListos.remove();
            if (actual.getTiempoInicio() == -1) {
                actual.setTiempoInicio(tiempo);
                actual.setTiempoRespuesta(tiempo);
            }
            int ejecutado = Math.min(quantum, actual.getTiempoRestante());
            int fin = Math.addExact(tiempo, ejecutado);
            segmentos.add(new Segmento(actual, tiempo, fin));
            tiempo = fin;
            actual.setTiempoRestante(actual.getTiempoRestante() - ejecutado);
            actual.setTiempoCPURecibido(actual.getTiempoCPURecibido() + ejecutado);
            if (actual.getTiempoRestante() == 0) {
                actual.setTiempoFinalizacion(tiempo);
                actual.setTiempoEspera(tiempo - actual.getRafagaCPU());
                espera += actual.getTiempoEspera();
                respuesta += actual.getTiempoRespuesta();
            } else colaListos.add(actual);
        }
        return new Resultado(segmentos, tiempo, espera / lote.size(), respuesta / lote.size());
    }
}
