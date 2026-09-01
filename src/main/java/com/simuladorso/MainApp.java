package com.simuladorso;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/main-view.fxml"));

        Scene scene = new Scene(loader.load(), 1080, 640);
        scene.getStylesheets().add(MainApp.class.getResource("/css/minios.css").toExternalForm());

        stage.setTitle("simuladorSO - MiniOS Simulator");
        stage.setMinWidth(1050);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
