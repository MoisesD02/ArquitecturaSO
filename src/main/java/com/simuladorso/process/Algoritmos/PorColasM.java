package com.simuladorso.process.Algoritmos;

import com.simuladorso.process.Proceso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PorColasM {

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
    // PLANIFICACIÓN POR COLAS MÚLTIPLES
    // =========================================

    public Resultado planificar(List<Proceso> procesosOriginales) {
        if (procesosOriginales == null || procesosOriginales.isEmpty())
            throw new IllegalArgumentException("Debe existir al menos un proceso.");
        List<Proceso> procesos = new ArrayList<>(procesosOriginales);
        for (Proceso p : procesos) if (p == null || p.getRafagaCPU() <= 0)
            throw new IllegalArgumentException("La ráfaga debe ser positiva.");
        // Todos los procesos del lote están disponibles desde t=0.
        procesos.sort(Comparator.<Proceso>comparingInt(p -> obtenerPrioridadCola(p.getCola())).thenComparingInt(Proceso::getPid));
        List<Segmento> segmentos = new ArrayList<>();
        int tiempo = 0;
        double espera = 0;
        for (Proceso p : procesos) {
            p.reiniciarSimulacion();
            p.setTiempoInicio(tiempo);
            p.setTiempoEspera(tiempo);
            p.setTiempoRespuesta(tiempo);
            espera += tiempo;
            int fin = Math.addExact(tiempo, p.getRafagaCPU());
            p.setTiempoFinalizacion(fin);
            segmentos.add(new Segmento(p, tiempo, fin));
            tiempo = fin;
        }
        return new Resultado(segmentos, tiempo, espera / procesos.size(), espera / procesos.size());
    }
    private int obtenerPrioridadCola(
            String cola) {

        if (cola == null) {
            return 4;
        }

        return switch (cola) {

            case "Sistema" ->
                    1;

            case "Interactivo" ->
                    2;

            case "Segundo plano" ->
                    3;

            default ->
                    4;
        };
    }
}