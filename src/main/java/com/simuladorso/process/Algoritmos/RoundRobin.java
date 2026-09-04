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

    public Resultado planificar(
            List<Proceso> procesosOriginales,
            int quantum) {

        if (procesosOriginales == null
                || procesosOriginales.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe existir al menos un proceso."
            );
        }

        if (quantum <= 0) {

            throw new IllegalArgumentException(
                    "El quantum debe ser mayor que 0."
            );
        }


        /*
         * Copiamos y ordenamos los procesos
         * por llegada y PID.
         */
        List<Proceso> pendientes =
                new ArrayList<>(procesosOriginales);

        pendientes.sort(
                Comparator
                        .comparingInt(
                                Proceso::getTiempoLlegada
                        )
                        .thenComparingInt(
                                Proceso::getPid
                        )
        );


        /*
         * Reiniciamos los datos variables
         * antes de comenzar.
         */
        for (Proceso proceso : pendientes) {
            proceso.reiniciarSimulacion();
        }


        Queue<Proceso> colaListos =
                new LinkedList<>();


        List<Segmento> segmentos =
                new ArrayList<>();


        int tiempoActual = 0;
        int indiceLlegada = 0;

        double sumaEspera = 0;
        double sumaRespuesta = 0;


        while (indiceLlegada < pendientes.size()
                || !colaListos.isEmpty()) {


            /*
             * Si la cola está vacía y todavía
             * quedan procesos por llegar,
             * adelantamos el tiempo hasta la
             * próxima llegada.
             */
            if (colaListos.isEmpty()) {

                Proceso siguiente =
                        pendientes.get(indiceLlegada);

                if (tiempoActual
                        < siguiente.getTiempoLlegada()) {

                    segmentos.add(
                            new Segmento(
                                    null,
                                    tiempoActual,
                                    siguiente.getTiempoLlegada()
                            )
                    );

                    tiempoActual =
                            siguiente.getTiempoLlegada();
                }
            }


            /*
             * Agregamos todos los procesos
             * que ya llegaron.
             */
            while (indiceLlegada < pendientes.size()
                    && pendientes.get(indiceLlegada)
                    .getTiempoLlegada()
                    <= tiempoActual) {

                colaListos.add(
                        pendientes.get(indiceLlegada)
                );

                indiceLlegada++;
            }


            if (colaListos.isEmpty()) {
                continue;
            }


            Proceso actual =
                    colaListos.poll();


            /*
             * Primera vez que obtiene CPU.
             * Aquí calculamos el tiempo
             * de respuesta.
             */
            if (actual.getTiempoInicio() == -1) {

                actual.setTiempoInicio(
                        tiempoActual
                );

                actual.setTiempoRespuesta(
                        tiempoActual
                                - actual.getTiempoLlegada()
                );
            }


            int inicio =
                    tiempoActual;


            /*
             * Ejecuta como máximo un quantum.
             */
            int tiempoEjecutado =
                    Math.min(
                            quantum,
                            actual.getTiempoRestante()
                    );


            tiempoActual +=
                    tiempoEjecutado;


            actual.setTiempoRestante(
                    actual.getTiempoRestante()
                            - tiempoEjecutado
            );


            actual.setTiempoCPURecibido(
                    actual.getTiempoCPURecibido()
                            + tiempoEjecutado
            );


            segmentos.add(
                    new Segmento(
                            actual,
                            inicio,
                            tiempoActual
                    )
            );


            /*
             * Mientras el proceso estaba en CPU,
             * pudieron llegar nuevos procesos.
             *
             * Deben entrar a la cola antes de
             * reinsertar el proceso actual.
             */
            while (indiceLlegada < pendientes.size()
                    && pendientes.get(indiceLlegada)
                    .getTiempoLlegada()
                    <= tiempoActual) {

                colaListos.add(
                        pendientes.get(indiceLlegada)
                );

                indiceLlegada++;
            }


            /*
             * Si terminó, calculamos sus métricas.
             */
            if (actual.getTiempoRestante() == 0) {

                actual.setTiempoFinalizacion(
                        tiempoActual
                );


                int espera =
                        actual.getTiempoFinalizacion()
                                - actual.getTiempoLlegada()
                                - actual.getRafagaCPU();


                actual.setTiempoEspera(
                        espera
                );


                sumaEspera +=
                        espera;


                sumaRespuesta +=
                        actual.getTiempoRespuesta();

            } else {

                /*
                 * Si todavía tiene CPU pendiente,
                 * vuelve al final de la cola.
                 */
                colaListos.add(
                        actual
                );
            }
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