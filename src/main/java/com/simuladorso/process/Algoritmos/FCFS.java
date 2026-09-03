package com.simuladorso.process.Algoritmos;

import com.simuladorso.process.Proceso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FCFS {

    /*
     * Representa un bloque del diagrama de Gantt.
     *
     * Si proceso == null, significa que
     * la CPU estuvo libre.
     */
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


    /*
     * Resultado completo de la planificación.
     */
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

            this.esperaPromedio =
                    esperaPromedio;

            this.respuestaPromedio =
                    respuestaPromedio;
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
    // ALGORITMO FCFS
    // =========================================

    public Resultado planificar(
            List<Proceso> procesosOriginales) {


        if (procesosOriginales == null
                || procesosOriginales.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe existir al menos un proceso."
            );
        }


        /*
         * Creamos una lista nueva para no alterar
         * el orden visual de la tabla.
         */
        List<Proceso> procesos =
                new ArrayList<>(
                        procesosOriginales
                );


        /*
         * FCFS:
         *
         * 1. Menor tiempo de llegada.
         * 2. Si llegan al mismo tiempo,
         *    menor PID primero.
         */
        procesos.sort(

                Comparator
                        .comparingInt(
                                Proceso::getTiempoLlegada
                        )
                        .thenComparingInt(
                                Proceso::getPid
                        )
        );


        List<Segmento> segmentos =
                new ArrayList<>();


        int tiempoActual = 0;

        double sumaEspera = 0;

        double sumaRespuesta = 0;


        for (Proceso proceso : procesos) {


            proceso.reiniciarSimulacion();


            /*
             * Si todavía no ha llegado ningún proceso,
             * la CPU permanece libre.
             */
            if (tiempoActual
                    < proceso.getTiempoLlegada()) {


                segmentos.add(
                        new Segmento(
                                null,
                                tiempoActual,
                                proceso.getTiempoLlegada()
                        )
                );


                tiempoActual =
                        proceso.getTiempoLlegada();
            }


            /*
             * FCFS es NO APROPIATIVO.
             *
             * Una vez que el proceso obtiene la CPU,
             * la conserva hasta terminar.
             */
            int inicio =
                    tiempoActual;


            int fin =
                    inicio
                            + proceso.getRafagaCPU();


            int espera =
                    inicio
                            - proceso.getTiempoLlegada();


            int respuesta =
                    espera;


            proceso.setTiempoInicio(
                    inicio
            );


            proceso.setTiempoFinalizacion(
                    fin
            );


            proceso.setTiempoEspera(
                    espera
            );


            proceso.setTiempoRespuesta(
                    respuesta
            );


            segmentos.add(
                    new Segmento(
                            proceso,
                            inicio,
                            fin
                    )
            );


            sumaEspera += espera;

            sumaRespuesta += respuesta;


            /*
             * El siguiente proceso solo puede
             * comenzar cuando este termine.
             */
            tiempoActual = fin;
        }


        double esperaPromedio =
                sumaEspera
                        / procesos.size();


        double respuestaPromedio =
                sumaRespuesta
                        / procesos.size();


        return new Resultado(
                segmentos,
                tiempoActual,
                esperaPromedio,
                respuestaPromedio
        );
    }
}