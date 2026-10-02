package com.simuladorso.ui.controllers.memory;

import com.simuladorso.ui.controllers.memory.multiprogramming.LinkedListController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MemoryController {

    @FXML private VBox headerContainer;
    @FXML private HBox mainButtonsContainer;
    @FXML private HBox moduleHeaderBar;
    @FXML private Label lblModuleTitle;
    @FXML private Button btnCloseModule;

    @FXML private VBox optsMultiprogramming;
    @FXML private VBox optsVirtualMemory;
    @FXML private ComboBox<String> cmbLinkedListFit;
    @FXML private StackPane contentArea;
    @FXML private StackPane subOptionsArea;

    private final Map<String, Node> cachedViews = new HashMap<>();

    @FXML
    public void initialize() {
        if (cmbLinkedListFit != null) {
            cmbLinkedListFit.setItems(FXCollections.observableArrayList(
                    "First Fit", "Next Fit", "Best Fit", "Worst Fit"
            ));
            cmbLinkedListFit.getSelectionModel().selectFirst();
        }
    }

    // --- Control de Pestañas Principales ---

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

    private void showOptions() {
        subOptionsArea.setVisible(true);
        subOptionsArea.setManaged(true);

        if (headerContainer != null) {
            headerContainer.setVisible(true);
            headerContainer.setManaged(true);
        }
        if (moduleHeaderBar != null) {
            moduleHeaderBar.setVisible(false);
            moduleHeaderBar.setManaged(false);
        }
        contentArea.getChildren().clear();
    }

    // --- Cierre de Módulo ---

    @FXML
    private void handleCloseModule() {
        showOptions();
    }

    // --- Sub-navegación Multiprogramación ---

    @FXML
    private void handleOpenBitMap() {
        loadSubView("/fxml/Multiprogramming/bitmap_view.fxml", "Mapa de Bits");
    }

    @FXML
    private void handleOpenLinkedList() {
        String fitType = (cmbLinkedListFit != null) ? cmbLinkedListFit.getValue() : "First Fit";
        String fxmlPath = "/fxml/Multiprogramming/LinkedList-view.fxml";

        try {
            var resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("No se encontró la vista en: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node node = loader.load();

            LinkedListController controller = loader.getController();
            if (controller != null) {
                controller.setStrategy(fitType);
            }

            desplegarVista(node, "Listas Ligadas");

        } catch (IOException e) {
            System.err.println("Error al cargar Listas Ligadas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenBuddySystem() {
        loadSubView("/fxml/Multiprogramming/buddy_system_view.fxml", "Sistemas Asociados (Buddy System)");
    }

    // --- Sub-navegación Memoria Virtual ---

    @FXML private void handleOpenPageReplacement() { loadSubView("/fxml/MemoryVirtual/page_replacement_view.fxml", "Algoritmo de Reemplazo Base"); }
    @FXML private void handleOpenOptimal() { loadSubView("/fxml/MemoryVirtual/opt_page_view.fxml", "Algoritmo Óptimo"); }
    @FXML private void handleOpenNru() { loadSubView("/fxml/MemoryVirtual/nru_page_view.fxml", "Uso No Tan Reciente (NRU)"); }
    @FXML private void handleOpenFifo() { loadSubView("/fxml/MemoryVirtual/fifo_page_view.fxml", "FIFO"); }
    @FXML private void handleOpenSecondChance() { loadSubView("/fxml/MemoryVirtual/second_chance_view.fxml", "Segunda Oportunidad"); }
    @FXML private void handleOpenClock() { loadSubView("/fxml/MemoryVirtual/clock_page_view.fxml", "Página de Reloj (Clock)"); }
    @FXML private void handleOpenLru() { loadSubView("/fxml/MemoryVirtual/lru_page_view.fxml", "Menor Uso Reciente (LRU)"); }

    // --- Carga con Caché ---

    private void loadSubView(String fxmlPath, String moduleTitle) {
        try {
            var resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("No se encontró la vista en la ruta: " + fxmlPath);
                return;
            }

            Node node = cachedViews.get(fxmlPath);
            if (node == null) {
                FXMLLoader loader = new FXMLLoader(resource);
                node = loader.load();
                cachedViews.put(fxmlPath, node);
            }

            desplegarVista(node, moduleTitle);

        } catch (IOException e) {
            System.err.println("Error al cargar la sub-vista (" + fxmlPath + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void desplegarVista(Node node, String moduleTitle) {
        // Ocultamos opciones principales
        if (subOptionsArea != null) {
            subOptionsArea.setVisible(false);
            subOptionsArea.setManaged(false);
        }
        if (headerContainer != null) {
            headerContainer.setVisible(false);
            headerContainer.setManaged(false);
        }

        // Mostramos la cabecera compacta del módulo
        if (lblModuleTitle != null) {
            lblModuleTitle.setText(moduleTitle);
        }
        if (moduleHeaderBar != null) {
            moduleHeaderBar.setVisible(true);
            moduleHeaderBar.setManaged(true);
        }

        // Cargar vista en el área central
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }
}