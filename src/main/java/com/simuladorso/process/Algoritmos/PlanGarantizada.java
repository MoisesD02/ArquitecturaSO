package com.simuladorso.process.Algoritmos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlanGarantizada {

    public record ProcesoGarantizado(
            int pid,
            String nombre,
            int rafagaCpu) {
    }


    public record Paso(
            int inicio,
            int fin,
            ProcesoGarantizado proceso,
            int procesosActivos) {
    }


    public record Resumen(
            ProcesoGarantizado proceso,
            int cpuRecibida,
            double porcentajeUso) {
    }


    public record Resultado(
            List<Paso> pasos,
            List<Resumen> resumen,
            int tiempoTotal) {
    }


    public Resultado simular(
            List<ProcesoGarantizado> procesos) {


        Map<ProcesoGarantizado, Double> derecho = new LinkedHashMap<>();
        Map<ProcesoGarantizado, Integer> restante =
                new LinkedHashMap<>();


        Map<ProcesoGarantizado, Integer> cpuRecibida =
                new LinkedHashMap<>();


        for (ProcesoGarantizado proceso :
                procesos) {


            derecho.put(proceso,0.0);
            restante.put(
                    proceso,
                    proceso.rafagaCpu()
            );


            cpuRecibida.put(
                    proceso,
                    0
            );
        }


        List<Paso> pasos =
                new ArrayList<>();


        int tiempo = 0;


        while (restante.values()
                .stream()
                .anyMatch(valor -> valor > 0)) {


            List<ProcesoGarantizado> activos = restante.keySet().stream()
                    .filter(proceso -> restante.get(proceso) > 0).toList();
            int procesosActivos =
                    activos.size();


            ProcesoGarantizado elegido =
                    activos.stream()

                            .min(

                                    Comparator
                                            .<ProcesoGarantizado>comparingDouble(
                                                    proceso ->
                                                            derecho.get(proceso)==0 ? 0 : cpuRecibida.get(proceso)/derecho.get(proceso)
                                            )

                                            .thenComparingInt(
                                                    ProcesoGarantizado::pid
                                            )
                            )

                            .orElseThrow();


            for(var proceso:activos) derecho.put(proceso,derecho.get(proceso)+1.0/procesosActivos);
            pasos.add(

                    new Paso(
                            tiempo,
                            tiempo + 1,
                            elegido,
                            procesosActivos
                    )
            );


            restante.put(

                    elegido,

                    restante.get(elegido)
                            - 1
            );


            cpuRecibida.put(

                    elegido,

                    cpuRecibida.get(elegido)
                            + 1
            );


            tiempo++;
        }


        int tiempoTotal =
                tiempo;


        List<Resumen> resumen =
                procesos.stream()

                        .map(
                                proceso ->
                                        new Resumen(

                                                proceso,

                                                cpuRecibida.get(proceso),

                                                tiempoTotal == 0
                                                        ? 0
                                                        : cpuRecibida.get(proceso)
                                                        / (double) tiempoTotal
                                        )
                        )

                        .toList();


        return new Resultado(
                List.copyOf(pasos),
                resumen,
                tiempoTotal
        );
    }


}
