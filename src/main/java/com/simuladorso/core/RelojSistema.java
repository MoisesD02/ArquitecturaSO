package com.simuladorso.core;

public class RelojSistema {
    private long segundos;

    public void avanzar() {
        segundos++;
    }

    public void reiniciar() {
        segundos = 0;
    }

    public long getSegundos() {
        return segundos;
    }

    public String formatear() {
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long seg = segundos % 60;
        return String.format("%02d:%02d:%02d", horas, minutos, seg);
    }
}
