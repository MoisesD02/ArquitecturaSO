package com.simuladorso;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/main-view.fxml")
        );

        // Obtener límites visuales dinámicos de la pantalla actual
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width = bounds.getWidth() * 0.85;
        double height = bounds.getHeight() * 0.85;

        Scene scene = new Scene(loader.load(), width, height);

        scene.getStylesheets().add(
                MainApp.class.getResource("/css/minios.css").toExternalForm()
        );

        stage.setTitle("simuladorSO - MiniOS Simulator");

        // Límites mínimos adaptativos
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        stage.setScene(scene);
        stage.setMaximized(true); // Inicia maximizado de forma transparente
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}