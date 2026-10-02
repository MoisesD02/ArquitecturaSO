package com.simuladorso.memory.algoritmos;

import com.simuladorso.memory.algoritmos.clock.EstadoPaginaClock;
import com.simuladorso.memory.algoritmos.clock.PasoClock;
import com.simuladorso.memory.algoritmos.clock.ResultadoClock;

import java.util.ArrayList;
import java.util.List;

public class Clock {

    public ResultadoClock simular(
            List<Integer> referencias,
            int cantidadMarcos) {

        if (referencias == null
                || referencias.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe ingresar al menos una referencia."
            );
        }

        if (cantidadMarcos <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad de marcos debe ser mayor que cero."
            );
        }


        ResultadoClock resultado =
                new ResultadoClock();


        List<EstadoPaginaClock> marcos =
                new ArrayList<>();


        /*
         * Posición de la aguja.
         *
         * 0 = Marco 1
         * 1 = Marco 2
         * 2 = Marco 3
         * ...
         */
        int aguja =
                0;


        for (Integer pagina :
                referencias) {

            boolean falloPagina;

            Integer paginaReemplazada =
                    null;

            String detalleDecision =
                    null;


            EstadoPaginaClock existente =
                    buscarPagina(
                            marcos,
                            pagina
                    );


            // =====================================
            // ACIERTO
            // =====================================

            if (existente != null) {

                falloPagina =
                        false;

                /*
                 * Si vuelve a aparecer,
                 * R pasa a 1.
                 */
                existente.setBitR(1);

            } else {

                falloPagina =
                        true;


                // =====================================
                // HAY ESPACIO LIBRE
                // =====================================

                if (marcos.size()
                        < cantidadMarcos) {

                    /*
                     * Página nueva con R = 0.
                     */
                    marcos.add(
                            new EstadoPaginaClock(
                                    pagina,
                                    0
                            )
                    );


                    /*
                     * Mientras se están llenando
                     * los marcos, la aguja avanza.
                     */
                    aguja =
                            marcos.size()
                                    % cantidadMarcos;

                } else {

                    // =====================================
                    // MEMORIA LLENA
                    // =====================================

                    boolean reemplazado =
                            false;


                    while (!reemplazado) {

                        EstadoPaginaClock candidata =
                                marcos.get(
                                        aguja
                                );


                        /*
                         * R = 0
                         *
                         * Se reemplaza.
                         */
                        if (candidata.getBitR()
                                == 0) {

                            paginaReemplazada =
                                    candidata.getPagina();


                            detalleDecision =
                                    "La aguja apunta al Marco "
                                            + (aguja + 1)
                                            + " y la página "
                                            + paginaReemplazada
                                            + " tiene R=0.";


                            marcos.set(
                                    aguja,
                                    new EstadoPaginaClock(
                                            pagina,
                                            0
                                    )
                            );


                            /*
                             * Después del reemplazo,
                             * la aguja avanza.
                             */
                            aguja =
                                    (aguja + 1)
                                            % cantidadMarcos;


                            reemplazado =
                                    true;

                        } else {

                            /*
                             * R = 1
                             *
                             * Se pone R = 0
                             * y la aguja continúa.
                             */
                            candidata.setBitR(0);


                            aguja =
                                    (aguja + 1)
                                            % cantidadMarcos;
                        }
                    }
                }
            }


            if (falloPagina) {

                resultado.incrementarFallos();

            } else {

                resultado.incrementarAciertos();
            }


            PasoClock paso =
                    new PasoClock(
                            pagina,
                            copiarMarcos(
                                    marcos
                            ),
                            falloPagina,
                            paginaReemplazada,
                            detalleDecision,
                            aguja
                    );


            resultado.agregarPaso(
                    paso
            );
        }


        return resultado;
    }


    private EstadoPaginaClock buscarPagina(
            List<EstadoPaginaClock> marcos,
            int pagina) {

        for (EstadoPaginaClock estado :
                marcos) {

            if (estado.getPagina()
                    == pagina) {

                return estado;
            }
        }

        return null;
    }


    private List<EstadoPaginaClock> copiarMarcos(
            List<EstadoPaginaClock> marcos) {

        List<EstadoPaginaClock> copia =
                new ArrayList<>();


        for (EstadoPaginaClock estado :
                marcos) {

            copia.add(
                    estado.copiar()
            );
        }


        return copia;
    }
}