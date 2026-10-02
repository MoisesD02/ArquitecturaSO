package com.simuladorso.memory.algoritmos;

import java.util.List;

public interface AlgoritmoReemplazo {

    ResultadoReemplazo simular(
            List<Integer> referencias,
            int cantidadMarcos
    );

}