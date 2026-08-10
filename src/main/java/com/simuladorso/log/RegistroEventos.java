package com.simuladorso.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegistroEventos {
    private final List<String> eventos = new ArrayList<>();

    public void registrar(String evento) { eventos.add(evento); }
    public List<String> getEventos() { return Collections.unmodifiableList(eventos); }
    public void limpiar() { eventos.clear(); }
}
