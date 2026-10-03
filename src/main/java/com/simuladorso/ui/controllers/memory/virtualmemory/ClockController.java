package com.simuladorso.ui.controllers.memory.virtualmemory;

import com.simuladorso.memory.algoritmos.Clock;
import com.simuladorso.memory.algoritmos.clock.ResultadoClock;
import com.simuladorso.ui.controllers.memory.virtualmemory.componentes.TablaReemplazoBuilder;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

import java.util.List;

public class ClockController
        extends ControladorReemplazoBase {


    @FXML
    private void handleSimular() {

        try {

            limpiarResultados();


            List<Integer> referencias =
                    obtenerReferencias();


            int cantidadMarcos =
                    obtenerCantidadMarcos();


            Clock algoritmo =
                    new Clock();


            ResultadoClock resultado =
                    algoritmo.simular(
                            referencias,
                            cantidadMarcos
                    );


            lblTotalReferencias.setText(
                    String.valueOf(
                            referencias.size()
                    )
            );


            lblFallos.setText(
                    String.valueOf(
                            resultado.getTotalFallos()
                    )
            );


            lblAciertos.setText(
                    String.valueOf(
                            resultado.getTotalAciertos()
                    )
            );

            actualizarMetricasRendimiento(
                    referencias.size(),
                    resultado.getTotalFallos()
            );


            contenedorSimulacion
                    .getChildren()
                    .clear();


            GridPane tabla =
                    TablaReemplazoBuilder
                            .crearTablaClock(
                                    referencias,
                                    cantidadMarcos,
                                    resultado
                            );


            contenedorSimulacion
                    .getChildren()
                    .add(tabla);


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