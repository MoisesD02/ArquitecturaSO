package com.simuladorso.filesystem.services;

import com.simuladorso.filesystem.model.INodo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        formatearDiscoFisico();
    }

    private void formatearDiscoFisico() {
        File directorio = new File(CARPETA_RAIZ);
        if (!directorio.exists()) {
            directorio.mkdirs();
        } else {
            File[] archivos = directorio.listFiles();
            if (archivos != null) {
                for (File f : archivos) f.delete();
            }
        }
    }

    public synchronized boolean crearArchivo(String nombreBruto, int bloquesRequeridos, int tamanoBloqueKB) {
        if (nombreBruto == null || nombreBruto.isBlank() || bloquesRequeridos <= 0) return false;

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

        Path rutaArchivo = Paths.get(CARPETA_RAIZ, nombre);
        try {
            // 1. Calcular el peso exacto en Bytes (1 KB = 1024 Bytes)
            int targetBytes = bloquesRequeridos * tamanoBloqueKB * 1024;

            // 2. Encabezado del archivo
            String header = "SimuladorSO | Algoritmo: I-Nodos\nAsignado en los bloques: " + bloquesAsignados.toString() + "\n\n";
            byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

            // 3. Crear un arreglo de bytes del tamaño EXACTO requerido
            byte[] contenidoFinal = new byte[targetBytes];

            // 4. Escribir el encabezado al inicio del arreglo
            int lengthToCopy = Math.min(headerBytes.length, targetBytes);
            System.arraycopy(headerBytes, 0, contenidoFinal, 0, lengthToCopy);

            // 5. Rellenar el resto del archivo para alcanzar el peso físico
            byte[] padding = "/;-;".getBytes(StandardCharsets.UTF_8);
            for (int i = lengthToCopy; i < targetBytes; i++) {
                contenidoFinal[i] = padding[(i - lengthToCopy) % padding.length];
            }

            // 6. Escribir en el disco duro de la computadora
            Files.write(rutaArchivo, contenidoFinal);

        } catch (IOException e) {
            for (int b : bloquesAsignados) disco[b] = false;
            return false;
        }

        INodo nuevoINodo = new INodo(contadorINodos++, nombre, bloquesRequeridos, tamanoBloqueKB, bloquesAsignados, rutaArchivo.toAbsolutePath().toString());
        tablaINodos.put(nombre, nuevoINodo);
        return true;
    }

    public synchronized boolean eliminarArchivo(String nombre) {
        INodo inodo = tablaINodos.remove(nombre);
        if (inodo == null) return false;

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