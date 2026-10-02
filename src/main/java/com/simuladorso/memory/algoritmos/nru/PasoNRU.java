package com.simuladorso.memory.algoritmos.nru;

import java.util.List;

public class PasoNRU {

    private int paginaActual;

    private List<EstadoPaginaNRU> marcos;

    private boolean falloPagina;

    private Integer paginaReemplazada;

    private String detalleDecision;

    private boolean limpiarR;

    private boolean modificarM;

    public PasoNRU(
            int paginaActual,
            List<EstadoPaginaNRU> marcos,
            boolean falloPagina,
            Integer paginaReemplazada,
            String detalleDecision,
            boolean limpiarR,
            boolean modificarM) {

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

        this.limpiarR =
                limpiarR;

        this.modificarM =
                modificarM;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public List<EstadoPaginaNRU> getMarcos() {
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

    public boolean isLimpiarR() {
        return limpiarR;
    }

    public boolean isModificarM() {
        return modificarM;
    }
}