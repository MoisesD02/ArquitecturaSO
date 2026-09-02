package com.simuladorso.ui.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ProcessesController {

    @FXML private VBox cardFcfs;
    @FXML private Button btnFcfs;
    @FXML private VBox algorithmWorkspace;

    @FXML
    public void initialize() {
        // Inicialización
    }

    /**
     * Evento al tocar cualquier parte de la tarjeta de FCFS.
     * Si es 1 clic -> Muestra la explicación/animación conceptual en el workspace.
     * Si es 2 clics -> Ejecuta la acción de cargar la sección del algoritmo.
     */
    @FXML
    private void handleFcfsCardClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            mostrarExplicacionFCFS();
        } else if (event.getClickCount() == 2) {
            abrirSimuladorFCFS();
        }
    }

    /**
     * Evento al presionar el botón azul directamente.
     */
    @FXML
    private void handleFcfsBtnClick() {
        // Si se presiona el botón directamente, mostramos la explicación
        // pero avisamos que requiere doble clic para ingresar.
        mostrarExplicacionFCFS();
    }

    /**
     * Renderiza la explicación y simulación gráfica conceptual de FCFS.
     */
    private void mostrarExplicacionFCFS() {
        algorithmWorkspace.getChildren().clear();

        Label titulo = new Label("Algoritmo FCFS (First-Come, First-Served)");
        titulo.getStyleClass().add("card-title");

        Label desc = new Label("• Tipo: No Apropiativo (Sin Desalojo).\n" +
                "• Funcionamiento: El primer proceso en llegar a la Cola de Listos es el primero en ser atendido por la CPU.\n" +
                "• Ventaja: Muy fácil de implementar y libre de inanición.\n" +
                "• Desventaja: Susceptible al 'Efecto Convoy' (un proceso largo retrasa a todos los demás).");
        desc.setWrapText(true);
        desc.getStyleClass().add("body-text");

        // Simulación visual simple (Barra de progreso de ejemplo de llegada)
        Label demoTitle = new Label("Demostración de flujo continuo:");
        demoTitle.getStyleClass().add("muted");

        HBox colaProcesos = new HBox(10);
        colaProcesos.setAlignment(Pos.CENTER_LEFT);

        Label p1 = new Label("P1 (Atendiendo...)");
        p1.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 6;");

        Label p2 = new Label("P2 (En espera)");
        p2.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 6;");

        Label p3 = new Label("P3 (En espera)");
        p3.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 6;");

        colaProcesos.getChildren().addAll(p1, p2, p3);

        Label indicacion = new Label("💡 Tip: Haz doble clic sobre el botón azul para ingresar a la mesa de trabajo de FCFS.");
        indicacion.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af; -fx-font-style: italic;");

        algorithmWorkspace.getChildren().addAll(titulo, desc, demoTitle, colaProcesos, indicacion);
    }

    /**
     * Lógica para entrar al simulador completo de FCFS.
     */
    private void abrirSimuladorFCFS() {
        algorithmWorkspace.getChildren().clear();

        Label titulo = new Label("🚀 CORTEX FCFS: Entorno de Ejecución Cargar...");
        titulo.getStyleClass().add("card-title");

        Label estado = new Label("Has ingresado al simulador interactivo de FCFS con doble clic. Aquí conectaremos la tabla de procesos y el diagrama de Gantt.");
        estado.getStyleClass().add("body-text");

        algorithmWorkspace.getChildren().addAll(titulo, estado);
    }
}