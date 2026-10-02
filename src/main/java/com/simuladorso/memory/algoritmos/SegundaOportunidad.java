package com.simuladorso.memory.algoritmos;

import com.simuladorso.memory.algoritmos.secondchance.EstadoPaginaSecondChance;
import com.simuladorso.memory.algoritmos.secondchance.PasoSecondChance;
import com.simuladorso.memory.algoritmos.secondchance.ResultadoSecondChance;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SegundaOportunidad {

    public ResultadoSecondChance simular(
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


        ResultadoSecondChance resultado =
                new ResultadoSecondChance();


        List<EstadoPaginaSecondChance> marcos =
                new ArrayList<>();


        /*
         * Mantiene el orden FIFO.
         *
         * La cola guarda los números de página.
         */
        Queue<Integer> cola =
                new LinkedList<>();


        for (Integer pagina :
                referencias) {

            boolean falloPagina;

            Integer paginaReemplazada =
                    null;

            String detalleDecision =
                    null;


            EstadoPaginaSecondChance existente =
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
                 * Según Segunda Oportunidad,
                 * una nueva referencia marca R = 1.
                 */
                existente.setBitR(1);

            } else {

                falloPagina =
                        true;


                // =====================================
                // HAY MARCO LIBRE
                // =====================================

                if (marcos.size()
                        < cantidadMarcos) {

                    /*
                     * Una página nueva entra con R = 0.
                     */
                    EstadoPaginaSecondChance nueva =
                            new EstadoPaginaSecondChance(
                                    pagina,
                                    0
                            );

                    marcos.add(
                            nueva
                    );

                    cola.add(
                            pagina
                    );

                } else {

                    // =====================================
                    // MEMORIA LLENA
                    // =====================================

                    boolean reemplazado =
                            false;


                    while (!reemplazado) {

                        Integer candidata =
                                cola.poll();


                        EstadoPaginaSecondChance estadoCandidata =
                                buscarPagina(
                                        marcos,
                                        candidata
                                );


                        /*
                         * Si R = 0:
                         * se reemplaza.
                         */
                        if (estadoCandidata.getBitR()
                                == 0) {

                            int posicion =
                                    marcos.indexOf(
                                            estadoCandidata
                                    );


                            paginaReemplazada =
                                    estadoCandidata
                                            .getPagina();


                            detalleDecision =
                                    "La página "
                                            + paginaReemplazada
                                            + " tiene R=0 y es la más antigua elegible.";


                            EstadoPaginaSecondChance nueva =
                                    new EstadoPaginaSecondChance(
                                            pagina,
                                            0
                                    );


                            marcos.set(
                                    posicion,
                                    nueva
                            );


                            cola.add(
                                    pagina
                            );


                            reemplazado =
                                    true;

                        } else {

                            /*
                             * Si R = 1:
                             *
                             * recibe segunda oportunidad,
                             * R pasa a 0
                             * y vuelve al final de la cola.
                             */
                            estadoCandidata
                                    .setBitR(0);


                            cola.add(
                                    candidata
                            );
                        }
                    }
                }
            }


            if (falloPagina) {

                resultado.incrementarFallos();

            } else {

                resultado.incrementarAciertos();
            }


            PasoSecondChance paso =
                    new PasoSecondChance(
                            pagina,
                            copiarMarcos(
                                    marcos
                            ),
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


    private EstadoPaginaSecondChance buscarPagina(
            List<EstadoPaginaSecondChance> marcos,
            int pagina) {

        for (EstadoPaginaSecondChance estado :
                marcos) {

            if (estado.getPagina()
                    == pagina) {

                return estado;
            }
        }

        return null;
    }


    private List<EstadoPaginaSecondChance> copiarMarcos(
            List<EstadoPaginaSecondChance> marcos) {

        List<EstadoPaginaSecondChance> copia =
                new ArrayList<>();


        for (EstadoPaginaSecondChance estado :
                marcos) {

            copia.add(
                    estado.copiar()
            );
        }


        return copia;
    }
}