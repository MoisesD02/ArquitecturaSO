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

    public Resultado planificar(
            List<Proceso> procesosOriginales) {

        if (procesosOriginales == null
                || procesosOriginales.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe existir al menos un proceso."
            );
        }

        List<Proceso> pendientes =
                new ArrayList<>(procesosOriginales);

        for (Proceso proceso : pendientes) {
            proceso.reiniciarSimulacion();
        }

        List<Segmento> segmentos =
                new ArrayList<>();

        int tiempoActual = 0;

        double sumaEspera = 0;
        double sumaRespuesta = 0;


        while (!pendientes.isEmpty()) {

            final int tiempoReferencia =
                    tiempoActual;

            /*
             * Solo se consideran procesos
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
                                             * Primero se compara
                                             * la prioridad de la cola.
                                             */
                                            .<Proceso>comparingInt(
                                                    proceso ->
                                                            obtenerPrioridadCola(
                                                                    proceso.getCola()
                                                            )
                                            )

                                            /*
                                             * Dentro de una misma cola
                                             * se utiliza FCFS.
                                             */
                                            .thenComparingInt(
                                                    Proceso::getTiempoLlegada
                                            )

                                            /*
                                             * Si llegan al mismo tiempo,
                                             * menor PID primero.
                                             */
                                            .thenComparingInt(
                                                    Proceso::getPid
                                            )
                            )

                            .toList();


            /*
             * Si todavía no hay ningún proceso
             * disponible, la CPU permanece libre.
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
             * El primer proceso es el perteneciente
             * a la cola de mayor prioridad.
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


    // =========================================
    // PRIORIDAD DE LAS COLAS
    // =========================================

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