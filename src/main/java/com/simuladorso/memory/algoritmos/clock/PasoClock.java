package com.simuladorso.memory.algoritmos.clock;

import java.util.List;

public class PasoClock {

    private int paginaActual;

    private List<EstadoPaginaClock> marcos;

    private boolean falloPagina;

    private Integer paginaReemplazada;

    private String detalleDecision;

    private int posicionAguja;

    public PasoClock(
            int paginaActual,
            List<EstadoPaginaClock> marcos,
            boolean falloPagina,
            Integer paginaReemplazada,
            String detalleDecision,
            int posicionAguja) {

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

        this.posicionAguja =
                posicionAguja;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public List<EstadoPaginaClock> getMarcos() {
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

    public int getPosicionAguja() {
        return posicionAguja;
    }
}