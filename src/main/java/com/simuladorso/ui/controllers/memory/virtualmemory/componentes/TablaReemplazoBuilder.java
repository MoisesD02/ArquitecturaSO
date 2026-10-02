package com.simuladorso.ui.controllers.memory.virtualmemory.componentes;

import com.simuladorso.memory.algoritmos.PasoReemplazo;
import com.simuladorso.memory.algoritmos.ResultadoReemplazo;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;

public class TablaReemplazoBuilder {

    private enum TipoCelda {
        TITULO,
        PASO,
        PAGINA,
        MARCO,
        FALLO,
        ACIERTO,
        SALE,
        DECISION
    }

    private TablaReemplazoBuilder() {
    }


    public static GridPane crearTablaBasica(
            List<Integer> referencias,
            int cantidadMarcos,
            ResultadoReemplazo resultado) {

        GridPane grid =
                new GridPane();

        grid.setHgap(2);
        grid.setVgap(2);

        grid.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                grid,
                Priority.ALWAYS
        );

        grid.setStyle(
                "-fx-background-color: #cfd8e3;"
                        + "-fx-padding: 2;"
        );


        List<PasoReemplazo> pasos =
                resultado.getPasos();

        int totalPasos =
                referencias.size();


        // =========================
        // COLUMNAS
        // =========================

        ColumnConstraints col0 =
                new ColumnConstraints();

        col0.setMinWidth(85);
        col0.setPrefWidth(95);
        col0.setHgrow(
                Priority.NEVER
        );

        grid.getColumnConstraints()
                .add(col0);


        for (int i = 0;
             i < totalPasos;
             i++) {

            ColumnConstraints colStep =
                    new ColumnConstraints();

            colStep.setMinWidth(35);

            colStep.setHgrow(
                    Priority.ALWAYS
            );

            grid.getColumnConstraints()
                    .add(colStep);
        }


        // =========================
        // FILA PASO
        // =========================

        agregarCelda(
                grid,
                "Paso",
                0,
                0,
                TipoCelda.TITULO
        );

        for (int columna = 0;
             columna < totalPasos;
             columna++) {

            agregarCelda(
                    grid,
                    String.valueOf(
                            columna + 1
                    ),
                    columna + 1,
                    0,
                    TipoCelda.PASO
            );
        }


        // =========================
        // FILA PÁGINA
        // =========================

        agregarCelda(
                grid,
                "Página",
                0,
                1,
                TipoCelda.TITULO
        );

        for (int columna = 0;
             columna < totalPasos;
             columna++) {

            agregarCelda(
                    grid,
                    String.valueOf(
                            referencias.get(columna)
                    ),
                    columna + 1,
                    1,
                    TipoCelda.PAGINA
            );
        }


        // =========================
        // FILAS DE MARCOS
        // =========================

        for (int marco = 0;
             marco < cantidadMarcos;
             marco++) {

            int fila =
                    marco + 2;

            agregarCelda(
                    grid,
                    "Marco " + (marco + 1),
                    0,
                    fila,
                    TipoCelda.TITULO
            );


            for (int columna = 0;
                 columna < pasos.size();
                 columna++) {

                PasoReemplazo paso =
                        pasos.get(columna);

                String valor =
                        "-";

                if (marco
                        < paso.getMarcos().size()) {

                    valor =
                            String.valueOf(
                                    paso
                                            .getMarcos()
                                            .get(marco)
                            );
                }

                agregarCelda(
                        grid,
                        valor,
                        columna + 1,
                        fila,
                        TipoCelda.MARCO
                );
            }
        }


        // =========================
        // RESULTADO
        // =========================

        int filaResultado =
                cantidadMarcos + 2;

        agregarCelda(
                grid,
                "Resultado",
                0,
                filaResultado,
                TipoCelda.TITULO
        );


        for (int columna = 0;
             columna < pasos.size();
             columna++) {

            PasoReemplazo paso =
                    pasos.get(columna);

            agregarCelda(
                    grid,
                    paso.isFalloPagina()
                            ? "Fallo"
                            : "Acierto",
                    columna + 1,
                    filaResultado,
                    paso.isFalloPagina()
                            ? TipoCelda.FALLO
                            : TipoCelda.ACIERTO
            );
        }


        // =========================
        // SALE
        // =========================

        int filaSale =
                cantidadMarcos + 3;

        agregarCelda(
                grid,
                "Sale",
                0,
                filaSale,
                TipoCelda.TITULO
        );


        for (int columna = 0;
             columna < pasos.size();
             columna++) {

            PasoReemplazo paso =
                    pasos.get(columna);

            String valor =
                    paso.getPaginaReemplazada()
                            != null

                            ? String.valueOf(
                            paso.getPaginaReemplazada()
                    )

                            : "-";


            agregarCelda(
                    grid,
                    valor,
                    columna + 1,
                    filaSale,

                    paso.getPaginaReemplazada()
                            != null

                            ? TipoCelda.DECISION

                            : TipoCelda.SALE
            );
        }


        // =========================
        // VERIFICAR SI HAY DECISIONES
        // =========================

        boolean tieneDecisiones =
                pasos.stream()
                        .anyMatch(
                                paso ->
                                        paso.getPaginaReemplazada() != null
                                                &&
                                                paso.getDetalleDecision() != null
                                                &&
                                                !paso.getDetalleDecision().isBlank()
                        );


        // =========================
        // FILA DECISIÓN
        // =========================

        if (tieneDecisiones) {

            int filaDecision =
                    cantidadMarcos + 4;

            agregarCelda(
                    grid,
                    "Decisión",
                    0,
                    filaDecision,
                    TipoCelda.TITULO
            );


            for (int columna = 0;
                 columna < pasos.size();
                 columna++) {

                PasoReemplazo paso =
                        pasos.get(columna);

                String detalle =
                        "-";


                /*
                 * Solo mostramos explicación
                 * cuando realmente salió una página.
                 */
                if (paso.getPaginaReemplazada() != null
                        &&
                        paso.getDetalleDecision() != null
                        &&
                        !paso.getDetalleDecision().isBlank()) {

                    detalle =
                            paso.getDetalleDecision();
                }


                agregarCeldaDecision(
                        grid,
                        detalle,
                        columna + 1,
                        filaDecision,
                        paso.getPaginaReemplazada() != null
                );
            }
        }


        return grid;
    }


    private static void agregarCelda(
            GridPane grid,
            String texto,
            int columna,
            int fila,
            TipoCelda tipo) {

        Label label =
                new Label(texto);

        label.setAlignment(
                Pos.CENTER
        );

        label.setMaxWidth(
                Double.MAX_VALUE
        );

        label.setMinHeight(32);


        String estilo =
                switch (tipo) {

                    case TITULO, PASO ->
                            "-fx-background-color: #dce6f2;"
                                    + "-fx-text-fill: #142744;"
                                    + "-fx-font-weight: bold;";

                    case PAGINA ->
                            "-fx-background-color: #9bd955;"
                                    + "-fx-text-fill: #17320b;"
                                    + "-fx-font-weight: bold;";

                    case MARCO ->
                            "-fx-background-color: white;"
                                    + "-fx-text-fill: #142744;"
                                    + "-fx-font-weight: bold;";

                    case FALLO ->
                            "-fx-background-color: #ef5350;"
                                    + "-fx-text-fill: white;"
                                    + "-fx-font-weight: bold;";

                    case ACIERTO ->
                            "-fx-background-color: #49b96f;"
                                    + "-fx-text-fill: white;"
                                    + "-fx-font-weight: bold;";

                    case SALE ->
                            "-fx-background-color: #f1f4f8;"
                                    + "-fx-text-fill: #142744;";

                    case DECISION ->
                            "-fx-background-color: #f59e0b;"
                                    + "-fx-text-fill: white;"
                                    + "-fx-font-weight: bold;";
                };


        label.setStyle(
                estilo
                        + "-fx-border-color: #9daaba;"
                        + "-fx-border-width: 1;"
                        + "-fx-font-size: 12px;"
        );


        grid.add(
                label,
                columna,
                fila
        );
    }


    private static void agregarCeldaDecision(
            GridPane grid,
            String texto,
            int columna,
            int fila,
            boolean esDecisionReal) {

        Label label =
                new Label(texto);

        label.setAlignment(
                Pos.CENTER
        );

        label.setWrapText(
                true
        );

        label.setMaxWidth(
                Double.MAX_VALUE
        );

        label.setMinHeight(50);


        /*
         * Si no hubo reemplazo,
         * dejamos la celda muy neutra.
         */
        if (!esDecisionReal) {

            label.setStyle(
                    "-fx-background-color: #f8fafc;"
                            + "-fx-text-fill: #94a3b8;"
                            + "-fx-border-color: #d5dce5;"
                            + "-fx-border-width: 1;"
                            + "-fx-font-size: 11px;"
            );

        } else {

            label.setStyle(
                    "-fx-background-color: #fff7ed;"
                            + "-fx-text-fill: #9a3412;"
                            + "-fx-font-weight: bold;"
                            + "-fx-border-color: #f59e0b;"
                            + "-fx-border-width: 1;"
                            + "-fx-font-size: 11px;"
                            + "-fx-padding: 5;"
            );
        }


        grid.add(
                label,
                columna,
                fila
        );
    }
}