package com.simuladorso.memory.algoritmos;

import com.simuladorso.memory.algoritmos.nru.EstadoPaginaNRU;
import com.simuladorso.memory.algoritmos.nru.PasoNRU;
import com.simuladorso.memory.algoritmos.nru.ResultadoNRU;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NRU {

    public ResultadoNRU simular(
            List<Integer> referencias,
            int cantidadMarcos,
            Set<Integer> pasosModificarM,
            Set<Integer> pasosLimpiarR) {

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

        ResultadoNRU resultado =
                new ResultadoNRU();

        List<EstadoPaginaNRU> marcos =
                new ArrayList<>();


        for (int i = 0;
             i < referencias.size();
             i++) {

            int numeroPaso =
                    i + 1;

            int pagina =
                    referencias.get(i);

            boolean limpiarR =
                    pasosLimpiarR.contains(
                            numeroPaso
                    );

            boolean modificarM =
                    pasosModificarM.contains(
                            numeroPaso
                    );

            Integer paginaReemplazada =
                    null;

            String detalleDecision =
                    null;

            boolean falloPagina;


            /*
             * LIMPIEZA DE R
             */
            if (limpiarR) {

                for (EstadoPaginaNRU estado :
                        marcos) {

                    estado.setBitR(0);
                }
            }


            EstadoPaginaNRU paginaExistente =
                    buscarPagina(
                            marcos,
                            pagina
                    );


            /*
             * ACIERTO
             */
            if (paginaExistente != null) {

                falloPagina =
                        false;

                paginaExistente.setBitR(1);

                if (modificarM) {
                    paginaExistente.setBitM(1);
                }

            } else {

                falloPagina =
                        true;


                /*
                 * HAY MARCO LIBRE
                 */
                if (marcos.size()
                        < cantidadMarcos) {

                    int bitM =
                            modificarM
                                    ? 1
                                    : 0;

                    marcos.add(
                            new EstadoPaginaNRU(
                                    pagina,
                                    1,
                                    bitM
                            )
                    );

                } else {

                    /*
                     * MEMORIA LLENA.
                     * SE BUSCA LA CLASE MÁS BAJA.
                     */
                    int indiceVictima =
                            buscarVictima(
                                    marcos
                            );

                    EstadoPaginaNRU victima =
                            marcos.get(
                                    indiceVictima
                            );

                    paginaReemplazada =
                            victima.getPagina();

                    int claseVictima =
                            victima.getClase();

                    detalleDecision =
                            "La página "
                                    + paginaReemplazada
                                    + " pertenece a la clase "
                                    + claseVictima
                                    + ", la clase más baja disponible.";

                    int bitM =
                            modificarM
                                    ? 1
                                    : 0;

                    marcos.set(
                            indiceVictima,
                            new EstadoPaginaNRU(
                                    pagina,
                                    1,
                                    bitM
                            )
                    );
                }
            }


            if (falloPagina) {

                resultado.incrementarFallos();

            } else {

                resultado.incrementarAciertos();
            }


            PasoNRU paso =
                    new PasoNRU(
                            pagina,
                            copiarMarcos(marcos),
                            falloPagina,
                            paginaReemplazada,
                            detalleDecision,
                            limpiarR,
                            modificarM
                    );

            resultado.agregarPaso(
                    paso
            );
        }

        return resultado;
    }


    private EstadoPaginaNRU buscarPagina(
            List<EstadoPaginaNRU> marcos,
            int pagina) {

        for (EstadoPaginaNRU estado :
                marcos) {

            if (estado.getPagina()
                    == pagina) {

                return estado;
            }
        }

        return null;
    }


    private int buscarVictima(
            List<EstadoPaginaNRU> marcos) {

        int menorClase =
                Integer.MAX_VALUE;

        int indiceVictima =
                -1;

        for (int i = 0;
             i < marcos.size();
             i++) {

            int clase =
                    marcos
                            .get(i)
                            .getClase();

            if (clase
                    < menorClase) {

                menorClase =
                        clase;

                indiceVictima =
                        i;
            }
        }

        return indiceVictima;
    }


    private List<EstadoPaginaNRU> copiarMarcos(
            List<EstadoPaginaNRU> marcos) {

        List<EstadoPaginaNRU> copia =
                new ArrayList<>();

        for (EstadoPaginaNRU estado :
                marcos) {

            copia.add(
                    estado.copiar()
            );
        }

        return copia;
    }
}