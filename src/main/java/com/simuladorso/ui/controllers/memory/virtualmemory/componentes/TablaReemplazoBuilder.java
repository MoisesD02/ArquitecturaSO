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
        TITULO, PASO, PAGINA, MARCO, FALLO, ACIERTO, SALE
    }

    private TablaReemplazoBuilder() {}

    public static GridPane crearTablaBasica(
            List<Integer> referencias,
            int cantidadMarcos,
            ResultadoReemplazo resultado) {

        GridPane grid = new GridPane();

        grid.setHgap(2);
        grid.setVgap(2);
        grid.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(grid, Priority.ALWAYS); // Ocupar todo el ancho del contenedor padre

        grid.setStyle("-fx-background-color: #cfd8e3; -fx-padding: 2;");

        List<PasoReemplazo> pasos = resultado.getPasos();
        int totalPasos = referencias.size();

        // COLUMNA 0: Títulos laterales
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setMinWidth(85);
        col0.setPrefWidth(95);
        col0.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().add(col0);

        // COLUMNAS 1 a N: Pasos (Se expanden en partes iguales)
        for (int i = 0; i < totalPasos; i++) {
            ColumnConstraints colStep = new ColumnConstraints();
            colStep.setMinWidth(35);
            colStep.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(colStep);
        }

        // FILA DE PASOS
        agregarCelda(grid, "Paso", 0, 0, TipoCelda.TITULO);
        for (int columna = 0; columna < totalPasos; columna++) {
            agregarCelda(grid, String.valueOf(columna + 1), columna + 1, 0, TipoCelda.PASO);
        }

        // FILA DE PÁGINAS
        agregarCelda(grid, "Página", 0, 1, TipoCelda.TITULO);
        for (int columna = 0; columna < totalPasos; columna++) {
            agregarCelda(grid, String.valueOf(referencias.get(columna)), columna + 1, 1, TipoCelda.PAGINA);
        }

        // FILAS DE MARCOS
        for (int marco = 0; marco < cantidadMarcos; marco++) {
            int fila = marco + 2;
            agregarCelda(grid, "Marco " + (marco + 1), 0, fila, TipoCelda.TITULO);

            for (int columna = 0; columna < pasos.size(); columna++) {
                PasoReemplazo paso = pasos.get(columna);
                String valor = "-";
                if (marco < paso.getMarcos().size()) {
                    valor = String.valueOf(paso.getMarcos().get(marco));
                }
                agregarCelda(grid, valor, columna + 1, fila, TipoCelda.MARCO);
            }
        }

        // FILA RESULTADO
        int filaResultado = cantidadMarcos + 2;
        agregarCelda(grid, "Resultado", 0, filaResultado, TipoCelda.TITULO);
        for (int columna = 0; columna < pasos.size(); columna++) {
            PasoReemplazo paso = pasos.get(columna);
            agregarCelda(grid, paso.isFalloPagina() ? "Fallo" : "Acierto", columna + 1, filaResultado,
                    paso.isFalloPagina() ? TipoCelda.FALLO : TipoCelda.ACIERTO);
        }

        // FILA PÁGINA REEMPLAZADA
        int filaSale = cantidadMarcos + 3;
        agregarCelda(grid, "Sale", 0, filaSale, TipoCelda.TITULO);
        for (int columna = 0; columna < pasos.size(); columna++) {
            PasoReemplazo paso = pasos.get(columna);
            String valor = (paso.getPaginaReemplazada() != null) ? String.valueOf(paso.getPaginaReemplazada()) : "-";
            agregarCelda(grid, valor, columna + 1, filaSale, TipoCelda.SALE);
        }

        return grid;
    }

    private static void agregarCelda(GridPane grid, String texto, int columna, int fila, TipoCelda tipo) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(32);

        String estilo = switch (tipo) {
            case TITULO, PASO -> "-fx-background-color: #dce6f2; -fx-text-fill: #142744; -fx-font-weight: bold;";
            case PAGINA -> "-fx-background-color: #9bd955; -fx-text-fill: #17320b; -fx-font-weight: bold;";
            case MARCO -> "-fx-background-color: white; -fx-text-fill: #142744; -fx-font-weight: bold;";
            case FALLO -> "-fx-background-color: #ef5350; -fx-text-fill: white; -fx-font-weight: bold;";
            case ACIERTO -> "-fx-background-color: #49b96f; -fx-text-fill: white; -fx-font-weight: bold;";
            case SALE -> "-fx-background-color: #f1f4f8; -fx-text-fill: #142744;";
        };

        label.setStyle(estilo + "-fx-border-color: #9daaba; -fx-border-width: 1; -fx-font-size: 12px;");
        grid.add(label, columna, fila);
    }
}