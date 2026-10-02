package com.simuladorso.memory.algoritmos.clock;

public class EstadoPaginaClock {

    private int pagina;
    private int bitR;

    public EstadoPaginaClock(
            int pagina,
            int bitR) {

        this.pagina = pagina;
        this.bitR = bitR;
    }

    public int getPagina() {
        return pagina;
    }

    public int getBitR() {
        return bitR;
    }

    public void setBitR(int bitR) {
        this.bitR = bitR;
    }

    public EstadoPaginaClock copiar() {
        return new EstadoPaginaClock(
                pagina,
                bitR
        );
    }
}