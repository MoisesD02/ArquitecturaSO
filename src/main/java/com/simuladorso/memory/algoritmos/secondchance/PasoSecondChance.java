package com.simuladorso.memory.algoritmos.secondchance;

import java.util.List;

public class PasoSecondChance {

    private int paginaActual;

    private List<EstadoPaginaSecondChance> marcos;

    private boolean falloPagina;

    private Integer paginaReemplazada;

    private String detalleDecision;

    public PasoSecondChance(
            int paginaActual,
            List<EstadoPaginaSecondChance> marcos,
            boolean falloPagina,
            Integer paginaReemplazada,
            String detalleDecision) {

        this.paginaActual =
                paginaActual;

        this.marcos =
                marcos;

        this.falloPagina =
                falloPagina;

        this.paginaReemplazada =
                paginaReemplazada;

        this.detalleDecision =
                detalleDecision;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public List<EstadoPaginaSecondChance> getMarcos() {
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