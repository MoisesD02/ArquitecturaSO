package com.simuladorso.process.Algoritmos;

import com.simuladorso.process.Proceso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PorPrioridad {

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
    // PLANIFICACIÓN POR PRIORIDAD
    // NO APROPIATIVA
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
         * Creamos una copia de la lista.
         *
         * De esta forma no alteramos el orden
         * original mostrado en la tabla.
         */
        List<Proceso> pendientes =
                new ArrayList<>(
                        procesosOriginales
                );


        /*
         * Reiniciamos las variables de simulación
         * de cada proceso.
         */
        for (Proceso proceso : pendientes) {

            proceso.reiniciarSimulacion();
        }


        List<Segmento> segmentos =
                new ArrayList<>();


        int tiempoActual = 0;

        double sumaEspera = 0;
        double sumaRespuesta = 0;


        /*
         * Continuamos mientras existan
         * procesos pendientes.
         */
        while (!pendientes.isEmpty()) {


            final int tiempoReferencia =
                    tiempoActual;


            /*
             * Obtenemos solamente los procesos
             * que ya llegaron al sistema.
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
                                             * En este simulador:
                                             *
                                             * 1 = prioridad más alta
                                             * 2 = prioridad media
                                             * 3 = prioridad más baja
                                             *
                                             * Por eso se ordena
                                             * de menor a mayor.
                                             */
                                            .comparingInt(
                                                    Proceso::getPrioridad
                                            )

                                            /*
                                             * En caso de empate
                                             * se selecciona primero
                                             * el que llegó antes.
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
             * Si no existe ningún proceso
             * disponible todavía, la CPU
             * permanece libre hasta la siguiente
             * llegada.
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
             * El primer elemento de la lista
             * es el proceso con mayor prioridad.
             */
            Proceso seleccionado =
                    disponibles.get(0);


            /*
             * Como este algoritmo es
             * NO APROPIATIVO, una vez que
             * obtiene la CPU no la abandona
             * hasta finalizar.
             */
            int inicio =
                    tiempoActual;


            int fin =
                    inicio
                            + seleccionado.getRafagaCPU();


            int espera =
                    inicio
                            - seleccionado.getTiempoLlegada();


            /*
             * Al ser no apropiativo,
             * el tiempo de respuesta coincide
             * con el tiempo de espera.
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
             * Avanzamos hasta el momento
             * en que termina el proceso.
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