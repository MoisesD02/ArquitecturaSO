package com.simuladorso.ui.controllers.memory.virtualmemory;

import com.simuladorso.memory.algoritmos.ResultadoReemplazo;
import com.simuladorso.ui.controllers.memory.virtualmemory.componentes.TablaReemplazoBuilder;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public abstract class ControladorReemplazoBase {

    @FXML
    protected TextField txtReferencias;

    @FXML
    protected TextField txtMarcos;

    @FXML
    protected Label lblMensaje;

    @FXML
    protected Label lblTotalReferencias;

    @FXML
    protected Label lblFallos;

    @FXML
    protected Label lblAciertos;

    @FXML
    protected VBox contenedorSimulacion;


    protected List<Integer> obtenerReferencias() {

        String texto =
                txtReferencias.getText().trim();

        if (texto.isEmpty()) {

            throw new IllegalArgumentException(
                    "Ingrese una cadena de referencias."
            );
        }

        String[] partes =
                texto.split(",");

        List<Integer> referencias =
                new ArrayList<>();

        for (String parte : partes) {

            String valor =
                    parte.trim();

            if (valor.isEmpty()) {
                continue;
            }

            referencias.add(
                    Integer.parseInt(valor)
            );
        }

        if (referencias.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe ingresar al menos una referencia."
            );
        }

        return referencias;
    }


    protected int obtenerCantidadMarcos() {

        String texto =
                txtMarcos.getText().trim();

        if (texto.isEmpty()) {

            throw new IllegalArgumentException(
                    "Ingrese la cantidad de marcos."
            );
        }

        int cantidadMarcos =
                Integer.parseInt(texto);

        if (cantidadMarcos <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad de marcos debe ser mayor que cero."
            );
        }

        return cantidadMarcos;
    }


    protected void mostrarResultado(
            List<Integer> referencias,
            int cantidadMarcos,
            ResultadoReemplazo resultado) {

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

        contenedorSimulacion
                .getChildren()
                .clear();

        GridPane tabla =
                TablaReemplazoBuilder
                        .crearTablaBasica(
                                referencias,
                                cantidadMarcos,
                                resultado
                        );

        contenedorSimulacion
                .getChildren()
                .add(tabla);
    }


    protected void limpiarResultados() {

        lblTotalReferencias.setText(
                "0"
        );

        lblFallos.setText(
                "0"
        );

        lblAciertos.setText(
                "0"
        );

        lblMensaje.setText(
                ""
        );

        contenedorSimulacion
                .getChildren()
                .clear();
    }


    protected void limpiarTodo() {

        txtReferencias.clear();
        txtMarcos.clear();

        limpiarResultados();
    }
}