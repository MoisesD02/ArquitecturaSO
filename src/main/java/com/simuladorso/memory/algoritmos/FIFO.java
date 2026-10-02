package com.simuladorso.memory.algoritmos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FIFO implements AlgoritmoReemplazo {

    @Override
    public ResultadoReemplazo simular(
            List<Integer> referencias,
            int cantidadMarcos) {

        ResultadoReemplazo resultado = new ResultadoReemplazo();

        List<Integer> marcos = new ArrayList<>();

        Queue<Integer> cola = new LinkedList<>();

        for (Integer pagina : referencias) {

            boolean falloPagina;
            Integer paginaReemplazada = null;
            String detalleDecision = null;

            if (marcos.contains(pagina)) {

                falloPagina = false;

            } else {

                falloPagina = true;

                if (marcos.size() < cantidadMarcos) {

                    marcos.add(pagina);
                    cola.add(pagina);

                } else {

                    Integer paginaVieja = cola.poll();

                    int posicion = marcos.indexOf(paginaVieja);

                    paginaReemplazada = paginaVieja;

                    detalleDecision = "La página " + paginaVieja
                            + " es la más antigua en memoria.";

                    marcos.set(
                            posicion, pagina
                    );

                    cola.add(pagina);
                }
            }

            if (falloPagina) {
                resultado.incrementarFallos();
            } else {
                resultado.incrementarAciertos();
            }

            PasoReemplazo paso = new PasoReemplazo(
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
}