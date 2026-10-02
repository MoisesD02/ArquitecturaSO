package com.simuladorso.memory.algoritmos;

import java.util.ArrayList;
import java.util.List;

public class Optimo implements AlgoritmoReemplazo {

    @Override
    public ResultadoReemplazo simular(
            List<Integer> referencias,
            int cantidadMarcos) {

        if (referencias == null || referencias.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe ingresar al menos una referencia."
            );
        }

        if (cantidadMarcos <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de marcos debe ser mayor que cero."
            );
        }

        ResultadoReemplazo resultado =
                new ResultadoReemplazo();

        List<Integer> marcos =
                new ArrayList<>();


        for (int i = 0;
             i < referencias.size();
             i++) {

            Integer pagina =
                    referencias.get(i);

            boolean falloPagina;

            Integer paginaReemplazada =
                    null;

            String detalleDecision =
                    null;


            /*
             * Si la página ya está en memoria,
             * es un acierto.
             */
            if (marcos.contains(pagina)) {

                falloPagina = false;

            } else {

                falloPagina = true;


                /*
                 * Si todavía existen marcos disponibles,
                 * simplemente se carga la página.
                 */
                if (marcos.size() < cantidadMarcos) {

                    marcos.add(pagina);

                } else {

                    /*
                     * Si la memoria está llena,
                     * se aplica la decisión del algoritmo Óptimo.
                     */
                    DecisionOptima decision =
                            buscarMarcoAReemplazar(
                                    referencias,
                                    marcos,
                                    i
                            );

                    int posicionReemplazo =
                            decision.getIndiceMarco();

                    paginaReemplazada =
                            marcos.get(
                                    posicionReemplazo
                            );

                    detalleDecision =
                            decision.getDetalle();

                    marcos.set(
                            posicionReemplazo,
                            pagina
                    );
                }
            }


            if (falloPagina) {

                resultado.incrementarFallos();

            } else {

                resultado.incrementarAciertos();
            }


            PasoReemplazo paso =
                    new PasoReemplazo(
                            pagina,
                            new ArrayList<>(marcos),
                            falloPagina,
                            paginaReemplazada,
                            detalleDecision
                    );

            resultado.agregarPaso(paso);
        }

        return resultado;
    }


    /*
     * Determina qué marco debe reemplazarse.
     */
    private DecisionOptima buscarMarcoAReemplazar(
            List<Integer> referencias,
            List<Integer> marcos,
            int posicionActual) {

        int indiceReemplazo =
                -1;

        int aparicionMasLejana =
                -1;


        for (int i = 0;
             i < marcos.size();
             i++) {

            Integer paginaEnMarco =
                    marcos.get(i);

            int siguienteAparicion =
                    buscarSiguienteAparicion(
                            referencias,
                            paginaEnMarco,
                            posicionActual + 1
                    );


            /*
             * Si la página no vuelve a aparecer,
             * se convierte en la mejor candidata
             * para ser reemplazada.
             */
            if (siguienteAparicion == -1) {

                return new DecisionOptima(
                        i,
                        "La página "
                                + paginaEnMarco
                                + " no vuelve a aparecer."
                );
            }


            /*
             * Si vuelve a aparecer, se conserva
             * la que aparezca más lejos.
             */
            if (siguienteAparicion
                    > aparicionMasLejana) {

                aparicionMasLejana =
                        siguienteAparicion;

                indiceReemplazo =
                        i;
            }
        }


        Integer paginaSeleccionada =
                marcos.get(
                        indiceReemplazo
                );


        return new DecisionOptima(
                indiceReemplazo,
                "La página "
                        + paginaSeleccionada
                        + " tiene la próxima aparición más lejana."
        );
    }


    /*
     * Busca la siguiente aparición
     * de una página.
     *
     * Retorna -1 si no vuelve a aparecer.
     */
    private int buscarSiguienteAparicion(
            List<Integer> referencias,
            Integer pagina,
            int desde) {

        for (int i = desde;
             i < referencias.size();
             i++) {

            if (referencias
                    .get(i)
                    .equals(pagina)) {

                return i;
            }
        }

        return -1;
    }


    /*
     * Clase auxiliar interna.
     */
    private static class DecisionOptima {

        private final int indiceMarco;
        private final String detalle;

        public DecisionOptima(
                int indiceMarco,
                String detalle) {

            this.indiceMarco =
                    indiceMarco;

            this.detalle =
                    detalle;
        }

        public int getIndiceMarco() {
            return indiceMarco;
        }

        public String getDetalle() {
            return detalle;
        }
    }
}