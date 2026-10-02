package com.simuladorso.memory.algoritmos.secondchance;

public class EstadoPaginaSecondChance {

    private int pagina;
    private int bitR;

    public EstadoPaginaSecondChance(
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

    public EstadoPaginaSecondChance copiar() {
        return new EstadoPaginaSecondChance(
                pagina,
                bitR
        );
    }
}