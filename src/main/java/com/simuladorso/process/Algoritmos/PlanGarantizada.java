package com.simuladorso.process.Algoritmos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlanGarantizada {

    public record ProcesoGarantizado(int pid, String nombre, int rafagaCpu) {
    }

    public record Paso(int inicio, int fin, ProcesoGarantizado proceso, int procesosActivos) {
    }

    public record Resumen(ProcesoGarantizado proceso, int cpuRecibida, double porcentajeUso) {
    }

    public record Resultado(List<Paso> pasos, List<Resumen> resumen, int tiempoTotal) {
    }

    public Resultado simular(List<ProcesoGarantizado> procesos) {
        Map<ProcesoGarantizado, Integer> restante = new LinkedHashMap<>();
        Map<ProcesoGarantizado, Integer> cpuRecibida = new LinkedHashMap<>();

        for (ProcesoGarantizado proceso : procesos) {
            restante.put(proceso, proceso.rafagaCpu());
            cpuRecibida.put(proceso, 0);
        }

        List<Paso> pasos = new ArrayList<>();
        int tiempo = 0;

        while (restante.values().stream().anyMatch(valor -> valor > 0)) {
            int procesosActivos = (int) restante.values().stream()
                    .filter(valor -> valor > 0)
                    .count();

            int tiempoActual = tiempo;
            ProcesoGarantizado elegido = restante.keySet().stream()
                    .filter(proceso -> restante.get(proceso) > 0)
                    .min(Comparator.<ProcesoGarantizado>comparingDouble(proceso -> calcularRelacion(
                                    cpuRecibida.get(proceso),
                                    tiempoActual,
                                    procesosActivos))
                            .thenComparingInt(proceso -> proceso.pid()))
                    .orElseThrow();

            pasos.add(new Paso(tiempo, tiempo + 1, elegido, procesosActivos));
            restante.put(elegido, restante.get(elegido) - 1);
            cpuRecibida.put(elegido, cpuRecibida.get(elegido) + 1);
            tiempo++;
        }

        int tiempoTotal = tiempo;
        List<Resumen> resumen = procesos.stream()
                .map(proceso -> new Resumen(
                        proceso,
                        cpuRecibida.get(proceso),
                        cpuRecibida.get(proceso) / (double) tiempoTotal))
                .toList();

        return new Resultado(List.copyOf(pasos), resumen, tiempoTotal);
    }

    private double calcularRelacion(int cpuRecibida, int tiempoTranscurrido, int procesosActivos) {
        if (tiempoTranscurrido == 0 || procesosActivos == 0) {
            return 0;
        }

        double cpuEsperada = tiempoTranscurrido / (double) procesosActivos;
        return cpuRecibida / cpuEsperada;
    }
}
