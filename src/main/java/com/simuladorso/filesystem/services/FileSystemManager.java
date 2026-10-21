package com.simuladorso.filesystem.services;

import com.simuladorso.filesystem.model.INodo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileSystemManager {

    private static final int TOTAL_BLOQUES = 20;
    private final boolean[] disco = new boolean[TOTAL_BLOQUES];
    private final Map<String, INodo> tablaINodos = new HashMap<>();
    private int contadorINodos = 1;

    private static final String CARPETA_RAIZ = "disco_simulado";

    public FileSystemManager() {
        // Crear el directorio físico al inicializar si no existe
        File directorio = new File(CARPETA_RAIZ);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
    }

    public synchronized boolean crearArchivo(String nombreBruto, int bloquesRequeridos) {
        if (nombreBruto == null || nombreBruto.isBlank() || bloquesRequeridos <= 0) return false;

        // Forzar extensión .txt
        String nombre = nombreBruto.endsWith(".txt") ? nombreBruto : nombreBruto + ".txt";

        if (tablaINodos.containsKey(nombre)) return false;
        if (getBloquesLibres() < bloquesRequeridos) return false;

        List<Integer> bloquesAsignados = new ArrayList<>();
        for (int i = 0; i < TOTAL_BLOQUES && bloquesAsignados.size() < bloquesRequeridos; i++) {
            if (!disco[i]) {
                disco[i] = true;
                bloquesAsignados.add(i);
            }
        }

        // Crear el archivo físico real
        Path rutaArchivo = Paths.get(CARPETA_RAIZ, nombre);
        try {
            String contenidoSimulado = "Archivo generado por SimuladorSO.\nI-Nodo asignado.\nBloques ocupados: " + bloquesRequeridos;
            Files.writeString(rutaArchivo, contenidoSimulado);
        } catch (IOException e) {
            // Revertir bloques si falla la creación física
            for (int b : bloquesAsignados) disco[b] = false;
            return false;
        }

        INodo nuevoINodo = new INodo(contadorINodos++, nombre, bloquesRequeridos, bloquesAsignados, rutaArchivo.toAbsolutePath().toString());
        tablaINodos.put(nombre, nuevoINodo);
        return true;
    }

    public synchronized boolean eliminarArchivo(String nombre) {
        INodo inodo = tablaINodos.remove(nombre);
        if (inodo == null) return false;

        // Eliminar archivo físico
        try {
            Files.deleteIfExists(Paths.get(inodo.getRutaFisica()));
        } catch (IOException ignored) {}

        for (int bloque : inodo.getBloquesAsignados()) {
            disco[bloque] = false;
        }
        return true;
    }

    public INodo consultarArchivo(String nombre) { return tablaINodos.get(nombre); }
    public boolean[] getEstadoDisco() { return disco.clone(); }
    public int getBloquesLibres() {
        int libres = 0;
        for (boolean ocupado : disco) if (!ocupado) libres++;
        return libres;
    }
    public List<INodo> getTodosLosArchivos() { return new ArrayList<>(tablaINodos.values()); }
    public int getTotalBloques() { return TOTAL_BLOQUES; }
}