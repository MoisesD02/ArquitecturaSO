package com.simuladorso.memory.algoritmos;

import java.util.List;

public class PasoReemplazo {

    private int paginaActual;
    private List<Integer> marcos;
    private boolean falloPagina;
    private Integer paginaReemplazada;
    private String detalleDecision;

    /*
     * Constructor general.
     * Se usa cuando no hace falta explicar una decisión.
     */
    public PasoReemplazo(
            int paginaActual,
            List<Integer> marcos,
            boolean falloPagina,
            Integer paginaReemplazada) {

        this(
                paginaActual,
                marcos,
                falloPagina,
                paginaReemplazada,
                null
        );
    }

    /*
     * Constructor con detalle de decisión.
     * Se usa, por ejemplo, cuando un algoritmo
     * necesita explicar por qué salió una página.
     */
    public PasoReemplazo(
            int paginaActual,
            List<Integer> marcos,
            boolean falloPagina,
            Integer paginaReemplazada,
            String detalleDecision) {

        this.paginaActual = paginaActual;
        this.marcos = marcos;
        this.falloPagina = falloPagina;
        this.paginaReemplazada = paginaReemplazada;
        this.detalleDecision = detalleDecision;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public List<Integer> getMarcos() {
        return marcos;
    }

    public boolean isFalloPagina() {
        return falloPagina;
    }

    public Integer getPaginaReemplazada() {
        return paginaReemplazada;
    }

    public String getDetalleDecision() {
        return detalleDecision;
    }
}