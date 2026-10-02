package com.simuladorso.ui.controllers.memory.virtualmemory;

import com.simuladorso.memory.algoritmos.FIFO;
import com.simuladorso.memory.algoritmos.ResultadoReemplazo;

import javafx.fxml.FXML;

import java.util.List;

public class FifoController
        extends ControladorReemplazoBase {


    @FXML
    private void handleSimular() {

        try {

            limpiarResultados();

            List<Integer> referencias =
                    obtenerReferencias();

            int cantidadMarcos =
                    obtenerCantidadMarcos();

            FIFO fifo =
                    new FIFO();

            ResultadoReemplazo resultado =
                    fifo.simular(
                            referencias,
                            cantidadMarcos
                    );

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