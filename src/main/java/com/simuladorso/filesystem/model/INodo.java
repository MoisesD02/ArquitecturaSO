package com.simuladorso.filesystem.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class INodo {
    private final int id;
    private final String nombreArchivo;
    private final int tamanoBloques;
    private final List<Integer> bloquesAsignados;

    private final String fechaCreacion;
    private final String rutaVirtual;
    private final String rutaFisica;
    private final String permisos;
    private final String propietario;
    private final String tamanoBytes;

    public INodo(int id, String nombreArchivo, int tamanoBloques, int tamanoBloqueKB, List<Integer> bloquesAsignados, String rutaFisica) {
        this.id = id;
        this.nombreArchivo = nombreArchivo;
        this.tamanoBloques = tamanoBloques;
        this.bloquesAsignados = new ArrayList<>(bloquesAsignados);

        this.fechaCreacion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        this.rutaFisica = rutaFisica;
        this.rutaVirtual = "/home/simuladorSO/" + nombreArchivo;

        // Cálculo matemático del peso: Bloques * KB por bloque
        int kilobytes = tamanoBloques * tamanoBloqueKB;
        this.tamanoBytes = kilobytes + " KB (" + (kilobytes * 1024) + " bytes)";

        this.permisos = "Lectura y Escritura (Modificable)";
        this.propietario = "Usuario_Local";
    }

    public int getId() { return id; }
    public String getNombreArchivo() { return nombreArchivo; }
    public int getTamanoBloques() { return tamanoBloques; }
    public List<Integer> getBloquesAsignados() { return bloquesAsignados; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getRutaVirtual() { return rutaVirtual; }
    public String getRutaFisica() { return rutaFisica; }
    public String getPermisos() { return permisos; }
    public String getPropietario() { return propietario; }
    public String getTamanoBytes() { return tamanoBytes; }
}