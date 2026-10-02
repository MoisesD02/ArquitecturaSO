package com.simuladorso.memory.algoritmos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LRU implements AlgoritmoReemplazo {

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


        /*
         * Guarda la última posición en la que
         * fue utilizada cada página.
         *
         * Ejemplo:
         *
         * página 2 -> última aparición en paso 6
         */
        Map<Integer, Integer> ultimoUso =
                new HashMap<>();


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


            // =====================================
            // ACIERTO
            // =====================================

            if (marcos.contains(pagina)) {

                falloPagina =
                        false;

                /*
                 * Actualizamos la última vez
                 * que fue utilizada.
                 */
                ultimoUso.put(
                        pagina,
                        i
                );

            } else {

                falloPagina =
                        true;


                // =====================================
                // HAY MARCO DISPONIBLE
                // =====================================

                if (marcos.size()
                        < cantidadMarcos) {

                    marcos.add(
                            pagina
                    );

                    ultimoUso.put(
                            pagina,
                            i
                    );

                } else {

                    // =====================================
                    // MEMORIA LLENA
                    // =====================================

                    int posicionReemplazo =
                            buscarMarcoLRU(
                                    marcos,
                                    ultimoUso
                            );


                    paginaReemplazada =
                            marcos.get(
                                    posicionReemplazo
                            );


                    int ultimoPaso =
                            ultimoUso.get(
                                    paginaReemplazada
                            ) + 1;


                    /*
                     * La decisión solamente se muestra
                     * cuando realmente sale una página.
                     */
                    detalleDecision =
                            "La página "
                                    + paginaReemplazada
                                    + " es la menos recientemente utilizada. "
                                    + "Su último uso fue en el paso "
                                    + ultimoPaso
                                    + ".";


                    /*
                     * Quitamos del registro la página
                     * que abandonó la memoria.
                     */
                    ultimoUso.remove(
                            paginaReemplazada
                    );


                    /*
                     * Reemplazamos el marco.
                     */
                    marcos.set(
                            posicionReemplazo,
                            pagina
                    );


                    /*
                     * La página nueva acaba de utilizarse.
                     */
                    ultimoUso.put(
                            pagina,
                            i
                    );
                }
            }


            // =====================================
            // MÉTRICAS
            // =====================================

            if (falloPagina) {

                resultado.incrementarFallos();

            } else {

                resultado.incrementarAciertos();
            }


            // =====================================
            // GUARDAR PASO
            // =====================================

            PasoReemplazo paso =
                    new PasoReemplazo(
                            pagina,
                            new ArrayList<>(marcos),
                            falloPagina,
                            paginaReemplazada,
                            detalleDecision
                    );


            resultado.agregarPaso(
                    paso
            );
        }


        return resultado;
    }


    /*
     * Busca la página cuya última utilización
     * sea la más antigua.
     */
    private int buscarMarcoLRU(
            List<Integer> marcos,
            Map<Integer, Integer> ultimoUso) {

        int posicionReemplazo =
                -1;

        int usoMasAntiguo =
                Integer.MAX_VALUE;


        for (int i = 0;
             i < marcos.size();
             i++) {

            Integer pagina =
                    marcos.get(i);


            Integer ultimaPosicion =
                    ultimoUso.get(pagina);


            if (ultimaPosicion
                    < usoMasAntiguo) {

                usoMasAntiguo =
                        ultimaPosicion;

                posicionReemplazo =
                        i;
            }
        }


        return posicionReemplazo;
    }
}