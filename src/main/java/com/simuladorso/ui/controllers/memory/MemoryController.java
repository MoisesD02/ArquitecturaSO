package com.simuladorso.ui.controllers.memory;

import com.simuladorso.ui.controllers.memory.multiprogramming.LinkedListController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MemoryController {

    @FXML private VBox optsMultiprogramming;
    @FXML private VBox optsVirtualMemory;
    @FXML private ComboBox<String> cmbLinkedListFit;
    @FXML private StackPane contentArea;
    @FXML private StackPane subOptionsArea;
    private final java.util.Map<String, Node> cachedViews = new java.util.HashMap<>();
    private void showOptions() { subOptionsArea.setVisible(true); subOptionsArea.setManaged(true); contentArea.getChildren().clear(); }

    @FXML
    public void initialize() {
        if (cmbLinkedListFit != null) {
            cmbLinkedListFit.setItems(FXCollections.observableArrayList(
                    "First Fit", "Next Fit", "Best Fit", "Worst Fit"
            ));
            cmbLinkedListFit.getSelectionModel().selectFirst();
        }
    }

    // --- Cambios de Menú Principal ---

    @FXML
    private void showMultiprogramming() {
        showOptions();
        optsVirtualMemory.setVisible(false);
        optsVirtualMemory.setManaged(false);

        optsMultiprogramming.setVisible(true);
        optsMultiprogramming.setManaged(true);
    }

    @FXML
    private void showVirtualMemory() {
        showOptions();
        optsMultiprogramming.setVisible(false);
        optsMultiprogramming.setManaged(false);

        optsVirtualMemory.setVisible(true);
        optsVirtualMemory.setManaged(true);
    }

    // --- Sub-navegación Multiprogramación ---

    @FXML
    private void handleOpenBitMap() {
        loadSubView("/fxml/Multiprogramming/bitmap_view.fxml");
    }

    @FXML
    private void handleOpenLinkedList() {
        String fitType = cmbLinkedListFit.getValue();
        if (fitType != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Multiprogramming/LinkedList-view.fxml"));
                Node node = loader.load();

                LinkedListController controller = loader.getController();
                controller.setStrategy(fitType);

                contentArea.getChildren().clear();
                contentArea.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleOpenBuddySystem() {
        loadSubView("/fxml/Multiprogramming/buddy_system_view.fxml");
    }

    // --- Sub-navegación Memoria Virtual ---

    @FXML private void handleOpenPageReplacement() { loadSubView("/fxml/MemoryVirtual/page_replacement_view.fxml"); }
    @FXML private void handleOpenOptimal() { loadSubView("/fxml/MemoryVirtual/opt_page_view.fxml"); }
    @FXML private void handleOpenNru() { loadSubView("/fxml/MemoryVirtual/nru_page_view.fxml"); }
    @FXML private void handleOpenFifo() { loadSubView("/fxml/MemoryVirtual/fifo_page_view.fxml"); }
    @FXML private void handleOpenSecondChance() { loadSubView("/fxml/MemoryVirtual/second_chance_view.fxml"); }
    @FXML private void handleOpenClock() { loadSubView("/fxml/MemoryVirtual/clock_page_view.fxml"); }
    @FXML private void handleOpenLru() { loadSubView("/fxml/MemoryVirtual/lru_page_view.fxml"); }

    private void loadSubView(String fxmlPath) {
        try {
            var resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("No se encontró la sub-vista en la ruta: " + fxmlPath);
                return;
            }
            Node node = cachedViews.get(fxmlPath);
            if (node == null) {
                FXMLLoader loader = new FXMLLoader(resource);
                node = loader.load();
                cachedViews.put(fxmlPath, node);
            }
            subOptionsArea.setVisible(false); subOptionsArea.setManaged(false);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}