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

    // Metadatos extendidos
    private final String fechaCreacion;
    private final String rutaFisica;
    private final String permisos;
    private final String propietario;

    public INodo(int id, String nombreArchivo, int tamanoBloques, List<Integer> bloquesAsignados, String rutaFisica) {
        this.id = id;
        this.nombreArchivo = nombreArchivo;
        this.tamanoBloques = tamanoBloques;
        this.bloquesAsignados = new ArrayList<>(bloquesAsignados);

        this.fechaCreacion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        this.rutaFisica = rutaFisica;
        this.permisos = "-rw-r--r--"; // Lectura/Escritura base
        this.propietario = "simuladorSO_User";
    }

    public int getId() { return id; }
    public String getNombreArchivo() { return nombreArchivo; }
    public int getTamanoBloques() { return tamanoBloques; }
    public List<Integer> getBloquesAsignados() { return bloquesAsignados; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getRutaFisica() { return rutaFisica; }
    public String getPermisos() { return permisos; }
    public String getPropietario() { return propietario; }
}