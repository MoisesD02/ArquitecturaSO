package com.simuladorso.memory.algoritmos.nru;

public class EstadoPaginaNRU {

    private int pagina;
    private int bitR;
    private int bitM;

    public EstadoPaginaNRU(
            int pagina,
            int bitR,
            int bitM) {

        this.pagina = pagina;
        this.bitR = bitR;
        this.bitM = bitM;
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

    public int getBitM() {
        return bitM;
    }

    public void setBitM(int bitM) {
        this.bitM = bitM;
    }

    public int getClase() {

        if (bitR == 0 && bitM == 0) {
            return 0;
        }

        if (bitR == 0 && bitM == 1) {
            return 1;
        }

        if (bitR == 1 && bitM == 0) {
            return 2;
        }

        return 3;
    }

    public EstadoPaginaNRU copiar() {
        return new EstadoPaginaNRU(
                pagina,
                bitR,
                bitM
        );
    }
}