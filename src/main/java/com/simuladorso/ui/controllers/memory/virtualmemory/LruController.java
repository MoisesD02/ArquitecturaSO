package com.simuladorso.ui.controllers.memory.virtualmemory;

import com.simuladorso.memory.algoritmos.LRU;
import com.simuladorso.memory.algoritmos.ResultadoReemplazo;
import javafx.fxml.FXML;

import java.util.List;

public class LruController
        extends ControladorReemplazoBase {


    @FXML
    private void handleSimular() {

        try {

            limpiarResultados();


            List<Integer> referencias =
                    obtenerReferencias();


            int cantidadMarcos =
                    obtenerCantidadMarcos();


            LRU lru =
                    new LRU();


            ResultadoReemplazo resultado =
                    lru.simular(
                            referencias,
                            cantidadMarcos
                    );


            /*
             * Reutilizamos exactamente la misma
             * tabla básica de FIFO y OPT.
             */
            mostrarResultado(
                    referencias,
                    cantidadMarcos,
                    resultado
            );


            lblMensaje.setText(
                    "Simulación completada."
            );


        } catch (NumberFormatException e) {

            lblMensaje.setText(
                    "Ingrese únicamente números válidos."
            );

        } catch (IllegalArgumentException e) {

            lblMensaje.setText(
                    e.getMessage()
            );
        }
    }


    @FXML
    private void handleEjemplo() {

        txtReferencias.setText(
                "2,3,2,1,5,2,4,5,3,2,5,2"
        );

        txtMarcos.setText(
                "3"
        );

        limpiarResultados();
    }


    @FXML
    private void handleLimpiar() {

        limpiarTodo();
    }
}