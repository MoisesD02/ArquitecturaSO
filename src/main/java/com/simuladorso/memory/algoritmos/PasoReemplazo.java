package com.simuladorso.memory.algoritmos;

import java.util.List;

public class PasoReemplazo {

    private int paginaActual;
    private List<Integer> marcos;
    private boolean falloPagina;
    private Integer paginaReemplazada;

    public PasoReemplazo(
            int paginaActual,
            List<Integer> marcos,
            boolean falloPagina,
            Integer paginaReemplazada) {

        this.paginaActual = paginaActual;
        this.marcos = marcos;
        this.falloPagina = falloPagina;
        this.paginaReemplazada = paginaReemplazada;
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
}