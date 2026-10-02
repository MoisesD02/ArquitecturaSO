package com.simuladorso.ui.controllers.memory.virtualmemory.componentes;

import com.simuladorso.memory.algoritmos.PasoReemplazo;
import com.simuladorso.memory.algoritmos.ResultadoReemplazo;

import com.simuladorso.memory.algoritmos.nru.EstadoPaginaNRU;
import com.simuladorso.memory.algoritmos.nru.PasoNRU;
import com.simuladorso.memory.algoritmos.nru.ResultadoNRU;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import com.simuladorso.memory.algoritmos.secondchance.EstadoPaginaSecondChance;
import com.simuladorso.memory.algoritmos.secondchance.PasoSecondChance;
import com.simuladorso.memory.algoritmos.secondchance.ResultadoSecondChance;

import java.util.ArrayList;
import java.util.List;

public class TablaReemplazoBuilder {

    private enum TipoCelda {

        TITULO,
        PASO,
        PAGINA,
        BIT,
        MARCO,
        FALLO,
        ACIERTO,
        SALE,
        DECISION,
        LIMPIEZA
    }


    private TablaReemplazoBuilder() {
    }


    // =========================================================
    // TABLA BÁSICA
    // FIFO / OPT / LRU
    // =========================================================

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


        // COLUMNA DE TÍTULOS
        ColumnConstraints col0 =
                new ColumnConstraints();

        col0.setMinWidth(85);
        col0.setPrefWidth(95);
        col0.setHgrow(
                Priority.NEVER
        );

        grid.getColumnConstraints()
                .add(col0);


        // COLUMNAS DE PASOS
        for (int i = 0;
             i < totalPasos;
             i++) {

            ColumnConstraints columna =
                    new ColumnConstraints();

            columna.setMinWidth(35);

            columna.setHgrow(
                    Priority.ALWAYS
            );

            grid.getColumnConstraints()
                    .add(columna);
        }


        // PASO
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


        // PÁGINA
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


        // MARCOS
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


        // RESULTADO
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


        // SALE
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


        // DECISIONES
        boolean tieneDecisiones =
                pasos.stream()
                        .anyMatch(
                                paso ->
                                        paso.getPaginaReemplazada()
                                                != null
                                                &&
                                                paso.getDetalleDecision()
                                                        != null
                                                &&
                                                !paso.getDetalleDecision()
                                                        .isBlank()
                        );


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

                if (paso.getPaginaReemplazada()
                        != null
                        &&
                        paso.getDetalleDecision()
                                != null
                        &&
                        !paso.getDetalleDecision()
                                .isBlank()) {

                    detalle =
                            paso.getDetalleDecision();
                }


                agregarCeldaDecision(
                        grid,
                        detalle,
                        columna + 1,
                        filaDecision,
                        paso.getPaginaReemplazada()
                                != null,
                        1
                );
            }
        }


        return grid;
    }


    // =========================================================
    // TABLA NRU
    // Página + R + M por cada paso
    // X / Limpiar R como columna independiente
    // * para modificación
    // =========================================================

    public static GridPane crearTablaNRU(
            List<Integer> referencias,
            int cantidadMarcos,
            ResultadoNRU resultado) {

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


        List<PasoNRU> pasos =
                resultado.getPasos();


        /*
         * Guardaremos la columna donde comienza
         * cada grupo:
         *
         * Página | R | M
         */
        List<Integer> columnasPaso =
                new ArrayList<>();

        /*
         * Si antes de un paso existe limpieza R,
         * aquí guardamos la columna X.
         *
         * -1 significa que no existe.
         */
        List<Integer> columnasLimpieza =
                new ArrayList<>();


        // ============================================
        // COLUMNA IZQUIERDA
        // ============================================

        ColumnConstraints titulo =
                new ColumnConstraints();

        titulo.setMinWidth(85);
        titulo.setPrefWidth(95);

        titulo.setHgrow(
                Priority.NEVER
        );

        grid.getColumnConstraints()
                .add(titulo);


        int columnaActual =
                1;


        // ============================================
        // CREAR COLUMNAS DINÁMICAS
        // ============================================

        for (int i = 0;
             i < pasos.size();
             i++) {

            PasoNRU paso =
                    pasos.get(i);


            /*
             * Si antes de este paso se limpia R,
             * insertamos una columna X.
             */
            if (paso.isLimpiarR()) {

                columnasLimpieza.add(
                        columnaActual
                );


                ColumnConstraints colX =
                        new ColumnConstraints();

                colX.setMinWidth(70);
                colX.setPrefWidth(80);

                colX.setHgrow(
                        Priority.NEVER
                );


                grid.getColumnConstraints()
                        .add(colX);


                columnaActual++;

            } else {

                columnasLimpieza.add(
                        -1
                );
            }


            /*
             * Inicio del grupo:
             *
             * Página | R | M
             */
            columnasPaso.add(
                    columnaActual
            );


            // Página
            ColumnConstraints colPagina =
                    new ColumnConstraints();

            colPagina.setMinWidth(42);

            colPagina.setHgrow(
                    Priority.ALWAYS
            );

            grid.getColumnConstraints()
                    .add(colPagina);


            // R
            ColumnConstraints colR =
                    new ColumnConstraints();

            colR.setMinWidth(30);

            colR.setHgrow(
                    Priority.ALWAYS
            );

            grid.getColumnConstraints()
                    .add(colR);


            // M
            ColumnConstraints colM =
                    new ColumnConstraints();

            colM.setMinWidth(30);

            colM.setHgrow(
                    Priority.ALWAYS
            );

            grid.getColumnConstraints()
                    .add(colM);


            columnaActual +=
                    3;
        }


        // ============================================
        // FILA 0 - PASOS
        // ============================================

        agregarCelda(
                grid,
                "Paso",
                0,
                0,
                TipoCelda.TITULO
        );


        for (int i = 0;
             i < pasos.size();
             i++) {

            PasoNRU paso =
                    pasos.get(i);


            int columnaX =
                    columnasLimpieza.get(i);


            if (columnaX != -1) {

                agregarCelda(
                        grid,
                        "X",
                        columnaX,
                        0,
                        TipoCelda.LIMPIEZA
                );
            }


            int inicio =
                    columnasPaso.get(i);


            /*
             * 4*
             *
             * si ese paso tiene modificación.
             */
            String numeroPaso =
                    String.valueOf(
                            i + 1
                    );


            if (paso.isModificarM()) {

                numeroPaso +=
                        "*";
            }


            agregarCeldaSpan(
                    grid,
                    numeroPaso,
                    inicio,
                    0,
                    3,
                    TipoCelda.PASO
            );
        }


        // ============================================
        // FILA 1 - PÁGINA / R / M
        // ============================================

        agregarCelda(
                grid,
                "Página",
                0,
                1,
                TipoCelda.TITULO
        );


        for (int i = 0;
             i < pasos.size();
             i++) {

            int columnaX =
                    columnasLimpieza.get(i);


            /*
             * Columna especial:
             *
             * X
             * Limpiar R
             */
            if (columnaX != -1) {

                agregarCelda(
                        grid,
                        "Limpiar R",
                        columnaX,
                        1,
                        TipoCelda.LIMPIEZA
                );
            }


            int inicio =
                    columnasPaso.get(i);


            agregarCelda(
                    grid,
                    String.valueOf(
                            referencias.get(i)
                    ),
                    inicio,
                    1,
                    TipoCelda.PAGINA
            );


            agregarCelda(
                    grid,
                    "R",
                    inicio + 1,
                    1,
                    TipoCelda.BIT
            );


            agregarCelda(
                    grid,
                    "M",
                    inicio + 2,
                    1,
                    TipoCelda.BIT
            );
        }


        // ============================================
        // FILAS DE MARCOS
        // ============================================

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


            for (int i = 0;
                 i < pasos.size();
                 i++) {

                PasoNRU paso =
                        pasos.get(i);


                int columnaX =
                        columnasLimpieza.get(i);


                /*
                 * Durante X simplemente indicamos
                 * que las referencias R pasan a 0.
                 */
                if (columnaX != -1) {

                    agregarCelda(
                            grid,
                            "R=0",
                            columnaX,
                            fila,
                            TipoCelda.LIMPIEZA
                    );
                }


                int inicio =
                        columnasPaso.get(i);


                String pagina =
                        "-";

                String bitR =
                        "-";

                String bitM =
                        "-";


                if (marco
                        < paso.getMarcos()
                        .size()) {

                    EstadoPaginaNRU estado =
                            paso.getMarcos()
                                    .get(marco);


                    pagina =
                            String.valueOf(
                                    estado.getPagina()
                            );


                    bitR =
                            String.valueOf(
                                    estado.getBitR()
                            );


                    bitM =
                            String.valueOf(
                                    estado.getBitM()
                            );
                }


                agregarCelda(
                        grid,
                        pagina,
                        inicio,
                        fila,
                        TipoCelda.MARCO
                );


                agregarCelda(
                        grid,
                        bitR,
                        inicio + 1,
                        fila,
                        TipoCelda.BIT
                );


                agregarCelda(
                        grid,
                        bitM,
                        inicio + 2,
                        fila,
                        TipoCelda.BIT
                );
            }
        }


        // ============================================
        // RESULTADO
        // ============================================

        int filaResultado =
                cantidadMarcos + 2;


        agregarCelda(
                grid,
                "Resultado",
                0,
                filaResultado,
                TipoCelda.TITULO
        );


        for (int i = 0;
             i < pasos.size();
             i++) {

            PasoNRU paso =
                    pasos.get(i);


            int columnaX =
                    columnasLimpieza.get(i);


            if (columnaX != -1) {

                agregarCelda(
                        grid,
                        "-",
                        columnaX,
                        filaResultado,
                        TipoCelda.LIMPIEZA
                );
            }


            int inicio =
                    columnasPaso.get(i);


            agregarCeldaSpan(
                    grid,
                    paso.isFalloPagina()
                            ? "Fallo"
                            : "Acierto",
                    inicio,
                    filaResultado,
                    3,
                    paso.isFalloPagina()
                            ? TipoCelda.FALLO
                            : TipoCelda.ACIERTO
            );
        }


        // ============================================
        // SALE
        // ============================================

        int filaSale =
                cantidadMarcos + 3;


        agregarCelda(
                grid,
                "Sale",
                0,
                filaSale,
                TipoCelda.TITULO
        );


        for (int i = 0;
             i < pasos.size();
             i++) {

            PasoNRU paso =
                    pasos.get(i);


            int columnaX =
                    columnasLimpieza.get(i);


            if (columnaX != -1) {

                agregarCelda(
                        grid,
                        "-",
                        columnaX,
                        filaSale,
                        TipoCelda.LIMPIEZA
                );
            }


            int inicio =
                    columnasPaso.get(i);


            String valor =
                    paso.getPaginaReemplazada()
                            != null

                            ? String.valueOf(
                            paso.getPaginaReemplazada()
                    )

                            : "-";


            agregarCeldaSpan(
                    grid,
                    valor,
                    inicio,
                    filaSale,
                    3,
                    paso.getPaginaReemplazada()
                            != null
                            ? TipoCelda.DECISION
                            : TipoCelda.SALE
            );
        }


        // ============================================
        // DECISIONES
        // ============================================

        boolean tieneDecisiones =
                pasos.stream()
                        .anyMatch(
                                paso ->
                                        paso.getPaginaReemplazada()
                                                != null
                                                &&
                                                paso.getDetalleDecision()
                                                        != null
                                                &&
                                                !paso.getDetalleDecision()
                                                        .isBlank()
                        );


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


            for (int i = 0;
                 i < pasos.size();
                 i++) {

                PasoNRU paso =
                        pasos.get(i);


                int columnaX =
                        columnasLimpieza.get(i);


                if (columnaX != -1) {

                    agregarCelda(
                            grid,
                            "-",
                            columnaX,
                            filaDecision,
                            TipoCelda.LIMPIEZA
                    );
                }


                int inicio =
                        columnasPaso.get(i);


                String detalle =
                        "-";


                if (paso.getPaginaReemplazada()
                        != null
                        &&
                        paso.getDetalleDecision()
                                != null
                        &&
                        !paso.getDetalleDecision()
                                .isBlank()) {

                    detalle =
                            paso.getDetalleDecision();
                }


                agregarCeldaDecision(
                        grid,
                        detalle,
                        inicio,
                        filaDecision,
                        paso.getPaginaReemplazada()
                                != null,
                        3
                );
            }
        }


        return grid;
    }


    // =========================================================
    // CELDA NORMAL
    // =========================================================

    private static void agregarCelda(
            GridPane grid,
            String texto,
            int columna,
            int fila,
            TipoCelda tipo) {

        Label label =
                crearLabel(
                        texto,
                        tipo
                );


        grid.add(
                label,
                columna,
                fila
        );
    }


    // =========================================================
    // CELDA QUE OCUPA VARIAS COLUMNAS
    // =========================================================

    private static void agregarCeldaSpan(
            GridPane grid,
            String texto,
            int columna,
            int fila,
            int columnas,
            TipoCelda tipo) {

        Label label =
                crearLabel(
                        texto,
                        tipo
                );


        grid.add(
                label,
                columna,
                fila,
                columnas,
                1
        );
    }


    // =========================================================
    // CREACIÓN DEL LABEL
    // =========================================================

    private static Label crearLabel(
            String texto,
            TipoCelda tipo) {

        Label label =
                new Label(texto);


        label.setAlignment(
                Pos.CENTER
        );

        label.setMaxWidth(
                Double.MAX_VALUE
        );

        label.setMinHeight(
                32
        );


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


                    case BIT ->

                            "-fx-background-color: #eef4ff;"
                                    + "-fx-text-fill: #315da8;"
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


                    case LIMPIEZA ->

                            "-fx-background-color: #ede9fe;"
                                    + "-fx-text-fill: #6d28d9;"
                                    + "-fx-font-weight: bold;";
                };


        label.setStyle(
                estilo
                        + "-fx-border-color: #9daaba;"
                        + "-fx-border-width: 1;"
                        + "-fx-font-size: 11px;"
                        + "-fx-padding: 3;"
        );


        return label;
    }


    // =========================================================
    // DECISIÓN
    // =========================================================

    private static void agregarCeldaDecision(
            GridPane grid,
            String texto,
            int columna,
            int fila,
            boolean esDecisionReal,
            int columnas) {

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

        label.setMinHeight(
                50
        );


        if (esDecisionReal) {

            label.setStyle(
                    "-fx-background-color: #fff7ed;"
                            + "-fx-text-fill: #9a3412;"
                            + "-fx-font-weight: bold;"
                            + "-fx-border-color: #f59e0b;"
                            + "-fx-border-width: 1;"
                            + "-fx-font-size: 10px;"
                            + "-fx-padding: 5;"
            );

        } else {

            label.setStyle(
                    "-fx-background-color: #f8fafc;"
                            + "-fx-text-fill: #94a3b8;"
                            + "-fx-border-color: #d5dce5;"
                            + "-fx-border-width: 1;"
                            + "-fx-font-size: 10px;"
            );
        }


        grid.add(
                label,
                columna,
                fila,
                columnas,
                1
        );
    }


    public static GridPane crearTablaSecondChance(
            List<Integer> referencias,
            int cantidadMarcos,
            ResultadoSecondChance resultado) {

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


        List<PasoSecondChance> pasos =
                resultado.getPasos();


        // ============================================
        // COLUMNA DE TÍTULOS
        // ============================================

        ColumnConstraints colTitulo =
                new ColumnConstraints();

        colTitulo.setMinWidth(85);
        colTitulo.setPrefWidth(95);

        colTitulo.setHgrow(
                Priority.NEVER
        );

        grid.getColumnConstraints()
                .add(colTitulo);


        /*
         * Cada referencia ocupa:
         *
         * Página | R
         */
        for (int i = 0;
             i < referencias.size();
             i++) {

            ColumnConstraints colPagina =
                    new ColumnConstraints();

            colPagina.setMinWidth(45);
            colPagina.setHgrow(
                    Priority.ALWAYS
            );


            ColumnConstraints colR =
                    new ColumnConstraints();

            colR.setMinWidth(35);
            colR.setHgrow(
                    Priority.ALWAYS
            );


            grid.getColumnConstraints()
                    .addAll(
                            colPagina,
                            colR
                    );
        }


        // ============================================
        // PASO
        // ============================================

        agregarCelda(
                grid,
                "Paso",
                0,
                0,
                TipoCelda.TITULO
        );


        for (int i = 0;
             i < pasos.size();
             i++) {

            int inicio =
                    1 + (i * 2);


            agregarCeldaSpan(
                    grid,
                    String.valueOf(
                            i + 1
                    ),
                    inicio,
                    0,
                    2,
                    TipoCelda.PASO
            );
        }


        // ============================================
        // PÁGINA / R
        // ============================================

        agregarCelda(
                grid,
                "Página",
                0,
                1,
                TipoCelda.TITULO
        );


        for (int i = 0;
             i < pasos.size();
             i++) {

            int inicio =
                    1 + (i * 2);


            agregarCelda(
                    grid,
                    String.valueOf(
                            referencias.get(i)
                    ),
                    inicio,
                    1,
                    TipoCelda.PAGINA
            );


            agregarCelda(
                    grid,
                    "R",
                    inicio + 1,
                    1,
                    TipoCelda.BIT
            );
        }


        // ============================================
        // MARCOS
        // ============================================

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

                PasoSecondChance paso =
                        pasos.get(columna);


                int inicio =
                        1 + (columna * 2);


                String pagina =
                        "-";

                String bitR =
                        "-";


                if (marco
                        < paso.getMarcos()
                        .size()) {

                    EstadoPaginaSecondChance estado =
                            paso.getMarcos()
                                    .get(marco);


                    pagina =
                            String.valueOf(
                                    estado.getPagina()
                            );


                    bitR =
                            String.valueOf(
                                    estado.getBitR()
                            );
                }


                agregarCelda(
                        grid,
                        pagina,
                        inicio,
                        fila,
                        TipoCelda.MARCO
                );


                agregarCelda(
                        grid,
                        bitR,
                        inicio + 1,
                        fila,
                        TipoCelda.BIT
                );
            }
        }


        // ============================================
        // RESULTADO
        // ============================================

        int filaResultado =
                cantidadMarcos + 2;


        agregarCelda(
                grid,
                "Resultado",
                0,
                filaResultado,
                TipoCelda.TITULO
        );


        for (int i = 0;
             i < pasos.size();
             i++) {

            PasoSecondChance paso =
                    pasos.get(i);


            int inicio =
                    1 + (i * 2);


            agregarCeldaSpan(
                    grid,
                    paso.isFalloPagina()
                            ? "Fallo"
                            : "Acierto",
                    inicio,
                    filaResultado,
                    2,
                    paso.isFalloPagina()
                            ? TipoCelda.FALLO
                            : TipoCelda.ACIERTO
            );
        }


        // ============================================
        // SALE
        // ============================================

        int filaSale =
                cantidadMarcos + 3;


        agregarCelda(
                grid,
                "Sale",
                0,
                filaSale,
                TipoCelda.TITULO
        );


        for (int i = 0;
             i < pasos.size();
             i++) {

            PasoSecondChance paso =
                    pasos.get(i);


            int inicio =
                    1 + (i * 2);


            String valor =
                    paso.getPaginaReemplazada()
                            != null

                            ? String.valueOf(
                            paso.getPaginaReemplazada()
                    )

                            : "-";


            agregarCeldaSpan(
                    grid,
                    valor,
                    inicio,
                    filaSale,
                    2,
                    paso.getPaginaReemplazada()
                            != null
                            ? TipoCelda.DECISION
                            : TipoCelda.SALE
            );
        }


        // ============================================
        // DECISIÓN
        // ============================================

        boolean tieneDecisiones =
                pasos.stream()
                        .anyMatch(
                                paso ->
                                        paso.getPaginaReemplazada()
                                                != null
                                                &&
                                                paso.getDetalleDecision()
                                                        != null
                                                &&
                                                !paso.getDetalleDecision()
                                                        .isBlank()
                        );


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


            for (int i = 0;
                 i < pasos.size();
                 i++) {

                PasoSecondChance paso =
                        pasos.get(i);


                int inicio =
                        1 + (i * 2);


                String detalle =
                        "-";


                if (paso.getPaginaReemplazada()
                        != null
                        &&
                        paso.getDetalleDecision()
                                != null
                        &&
                        !paso.getDetalleDecision()
                                .isBlank()) {

                    detalle =
                            paso.getDetalleDecision();
                }


                agregarCeldaDecision(
                        grid,
                        detalle,
                        inicio,
                        filaDecision,
                        paso.getPaginaReemplazada()
                                != null,
                        2
                );
            }
        }


        return grid;
    }
}