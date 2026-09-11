package com.simuladorso.ui.controllers.memory.multiprogramming;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LinkedListController {

    @FXML private Label lblFitTitle;

    public void setStrategy(String fitType) {
        if (lblFitTitle != null) {
            lblFitTitle.setText("Simulación: Listas Ligadas (" + fitType + ")");
        }
    }
}