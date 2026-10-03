package com.simuladorso.ui.controllers.memory.virtualmemory;

import com.simuladorso.memory.algoritmos.NRU;
import com.simuladorso.memory.algoritmos.nru.ResultadoNRU;
import com.simuladorso.ui.controllers.memory.virtualmemory.componentes.TablaReemplazoBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NruController
        extends ControladorReemplazoBase {

    @FXML
    private TextField txtModificarM;

    @FXML
    private TextField txtLimpiarR;


    @FXML
    private void handleSimular() {

        try {

            limpiarResultados();

            List<Integer> referencias =
                    obtenerReferencias();

            int cantidadMarcos =
                    obtenerCantidadMarcos();

            Set<Integer> pasosModificarM =
                    obtenerPasos(
                            txtModificarM.getText()
                    );

            Set<Integer> pasosLimpiarR =
                    obtenerPasos(
                            txtLimpiarR.getText()
                    );


            NRU nru =
                    new NRU();

            ResultadoNRU resultado =
                    nru.simular(
                            referencias,
                            cantidadMarcos,
                            pasosModificarM,
                            pasosLimpiarR
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
                            .crearTablaNRU(
                                    referencias,
                                    cantidadMarcos,
                                    resultado
                            );


            contenedorSimulacion
                    .getChildren()
                    .add(tabla);


            lblMensaje.setText(
                    "Simulación NRU completada."
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

        /*
         * Según la idea mostrada en el material:
         * 4* y 8* representan momentos
         * de modificación M.
         */
        txtModificarM.setText(
                "4,8"
        );

        /*
         * Puedes cambiar estos pasos
         * para probar la limpieza de R.
         */
        txtLimpiarR.setText(
                "5,9"
        );

        limpiarResultados();
    }


    @FXML
    private void handleLimpiar() {

        limpiarTodo();

        txtModificarM.clear();
        txtLimpiarR.clear();
    }


    private Set<Integer> obtenerPasos(
            String texto) {

        Set<Integer> pasos =
                new HashSet<>();

        if (texto == null
                || texto.trim().isEmpty()) {

            return pasos;
        }


        String[] partes =
                texto.split(",");


        for (String parte :
                partes) {

            String valor =
                    parte.trim();

            if (valor.isEmpty()) {
                continue;
            }

            int paso =
                    Integer.parseInt(valor);

            if (paso <= 0) {

                throw new IllegalArgumentException(
                        "Los pasos deben ser mayores que cero."
                );
            }

            pasos.add(
                    paso
            );
        }


        return pasos;
    }
}