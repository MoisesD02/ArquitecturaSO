package com.simuladorso.process;

public class Proceso {

    private final int pid;
    private final String nombre;

    private int memoriaMB;

    private int tiempoLlegada;
    private int rafagaCPU;
    private int tiempoRestante;

    private int prioridad;
    private String cola;

    private int tiempoCPURecibido;

    // Métricas de planificación
    private int tiempoInicio = -1;
    private int tiempoFinalizacion = -1;
    private int tiempoEspera = 0;
    private int tiempoRespuesta = 0;

    private EstadoProceso estado;


    /*
     * Constructor original.
     * Se conserva para no afectar otras partes del proyecto.
     */
    public Proceso(
            int pid,
            String nombre,
            int memoriaMB) {

        this(
                pid,
                nombre,
                0,
                0,
                memoriaMB,
                0,
                ""
        );
    }


    /*
     * Constructor básico para FCFS y SJF.
     */
    public Proceso(
            int pid,
            String nombre,
            int tiempoLlegada,
            int rafagaCPU) {

        this(
                pid,
                nombre,
                tiempoLlegada,
                rafagaCPU,
                0,
                0,
                ""
        );
    }


    /*
     * Constructor general.
     */
    public Proceso(
            int pid,
            String nombre,
            int tiempoLlegada,
            int rafagaCPU,
            int memoriaMB,
            int prioridad,
            String cola) {

        this.pid = pid;
        this.nombre = nombre;

        this.tiempoLlegada = tiempoLlegada;
        this.rafagaCPU = rafagaCPU;
        this.tiempoRestante = rafagaCPU;

        this.memoriaMB = memoriaMB;
        this.prioridad = prioridad;

        this.cola = cola == null
                ? ""
                : cola;

        this.tiempoCPURecibido = 0;

        this.estado = EstadoProceso.NUEVO;
    }


    // ==========================
    // GETTERS Y SETTERS
    // ==========================

    public int getPid() {
        return pid;
    }


    public String getNombre() {
        return nombre;
    }


    public int getMemoriaMB() {
        return memoriaMB;
    }


    public void setMemoriaMB(int memoriaMB) {
        this.memoriaMB = memoriaMB;
    }


    public int getTiempoLlegada() {
        return tiempoLlegada;
    }


    public void setTiempoLlegada(int tiempoLlegada) {
        this.tiempoLlegada = tiempoLlegada;
    }


    public int getRafagaCPU() {
        return rafagaCPU;
    }


    public void setRafagaCPU(int rafagaCPU) {

        this.rafagaCPU = rafagaCPU;
        this.tiempoRestante = rafagaCPU;
    }


    public int getTiempoRestante() {
        return tiempoRestante;
    }


    public void setTiempoRestante(int tiempoRestante) {
        this.tiempoRestante = tiempoRestante;
    }


    public int getPrioridad() {
        return prioridad;
    }


    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }


    public String getCola() {
        return cola;
    }


    public void setCola(String cola) {
        this.cola = cola;
    }


    public int getTiempoCPURecibido() {
        return tiempoCPURecibido;
    }


    public void setTiempoCPURecibido(int tiempoCPURecibido) {
        this.tiempoCPURecibido = tiempoCPURecibido;
    }


    public EstadoProceso getEstado() {
        return estado;
    }


    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }


    // ==========================
    // MÉTRICAS
    // ==========================

    public int getTiempoInicio() {
        return tiempoInicio;
    }


    public void setTiempoInicio(int tiempoInicio) {
        this.tiempoInicio = tiempoInicio;
    }


    public int getTiempoFinalizacion() {
        return tiempoFinalizacion;
    }


    public void setTiempoFinalizacion(
            int tiempoFinalizacion) {

        this.tiempoFinalizacion =
                tiempoFinalizacion;
    }


    public int getTiempoEspera() {
        return tiempoEspera;
    }


    public void setTiempoEspera(int tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }


    public int getTiempoRespuesta() {
        return tiempoRespuesta;
    }


    public void setTiempoRespuesta(int tiempoRespuesta) {
        this.tiempoRespuesta = tiempoRespuesta;
    }


    /*
     * Restaura los datos variables antes de
     * iniciar una nueva simulación.
     */
    public void reiniciarSimulacion() {

        tiempoRestante = rafagaCPU;

        tiempoCPURecibido = 0;

        tiempoInicio = -1;

        tiempoFinalizacion = -1;

        tiempoEspera = 0;

        tiempoRespuesta = 0;

        estado = EstadoProceso.NUEVO;
    }


    @Override
    public String toString() {

        return String.format(
                "P%03d - %s",
                pid,
                nombre
        );
    }
}