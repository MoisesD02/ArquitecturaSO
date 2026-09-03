package com.simuladorso.process.Algoritmos;

import com.simuladorso.process.Proceso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SJF {

    // =========================================
    // SEGMENTO DEL GANTT
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
    // ALGORITMO SJF NO APROPIATIVO
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
         * Trabajamos con una copia para no modificar
         * el orden general de la tabla.
         */
        List<Proceso> pendientes =
                new ArrayList<>(procesosOriginales);


        /*
         * Reiniciamos únicamente los procesos
         * que participan en esta simulación.
         */
        for (Proceso proceso : pendientes) {

            proceso.reiniciarSimulacion();
        }


        List<Segmento> segmentos =
                new ArrayList<>();


        int tiempoActual = 0;

        double sumaEspera = 0;
        double sumaRespuesta = 0;


        while (!pendientes.isEmpty()) {


            /*
             * Guardamos el tiempo actual en una
             * variable que no cambiará durante
             * esta búsqueda.
             */
            final int tiempoReferencia =
                    tiempoActual;


            /*
             * Buscamos todos los procesos
             * que ya llegaron.
             */
            List<Proceso> disponibles =
                    pendientes.stream()

                            .filter(
                                    proceso ->
                                            proceso.getTiempoLlegada()
                                                    <= tiempoReferencia
                            )

                            .sorted(

                                    Comparator
                                            /*
                                             * Menor ráfaga primero.
                                             */
                                            .comparingInt(
                                                    Proceso::getRafagaCPU
                                            )

                                            /*
                                             * Si empatan en ráfaga,
                                             * el que llegó primero.
                                             */
                                            .thenComparingInt(
                                                    Proceso::getTiempoLlegada
                                            )

                                            /*
                                             * Si también empatan
                                             * en llegada, menor PID.
                                             */
                                            .thenComparingInt(
                                                    Proceso::getPid
                                            )
                            )

                            .toList();


            /*
             * Si todavía no llegó ningún proceso,
             * la CPU permanece libre hasta la
             * próxima llegada.
             */
            if (disponibles.isEmpty()) {


                Proceso siguiente =
                        pendientes.stream()

                                .min(
                                        Comparator
                                                .comparingInt(
                                                        Proceso::getTiempoLlegada
                                                )

                                                .thenComparingInt(
                                                        Proceso::getPid
                                                )
                                )

                                .orElseThrow();


                int siguienteLlegada =
                        siguiente.getTiempoLlegada();


                segmentos.add(

                        new Segmento(
                                null,
                                tiempoActual,
                                siguienteLlegada
                        )
                );


                tiempoActual =
                        siguienteLlegada;


                continue;
            }


            /*
             * El primer elemento es el proceso
             * con la menor ráfaga disponible.
             */
            Proceso seleccionado =
                    disponibles.get(0);


            int inicio =
                    tiempoActual;


            int fin =
                    inicio
                            + seleccionado.getRafagaCPU();


            int espera =
                    inicio
                            - seleccionado.getTiempoLlegada();


            /*
             * En SJF no apropiativo,
             * respuesta = espera.
             */
            int respuesta =
                    espera;


            seleccionado.setTiempoInicio(
                    inicio
            );


            seleccionado.setTiempoFinalizacion(
                    fin
            );


            seleccionado.setTiempoEspera(
                    espera
            );


            seleccionado.setTiempoRespuesta(
                    respuesta
            );


            segmentos.add(

                    new Segmento(
                            seleccionado,
                            inicio,
                            fin
                    )
            );


            sumaEspera += espera;

            sumaRespuesta += respuesta;


            /*
             * SJF no apropiativo:
             *
             * El proceso conserva la CPU
             * hasta terminar.
             */
            tiempoActual = fin;


            pendientes.remove(
                    seleccionado
            );
        }


        double esperaPromedio =
                sumaEspera
                        / procesosOriginales.size();


        double respuestaPromedio =
                sumaRespuesta
                        / procesosOriginales.size();


        return new Resultado(
                segmentos,
                tiempoActual,
                esperaPromedio,
                respuestaPromedio
        );
    }
}