package com.simuladorso.ui.controllers.memory;

import com.simuladorso.ui.controllers.memory.multiprogramming.LinkedListController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MemoryController {

    @FXML private HBox mainButtonsContainer;
    @FXML private Button btnCloseModule;
    @FXML private VBox optsMultiprogramming;
    @FXML private VBox optsVirtualMemory;
    @FXML private ComboBox<String> cmbLinkedListFit;
    @FXML private StackPane contentArea;
    @FXML private StackPane subOptionsArea;

    // Caché de vistas para optimizar la carga de FXML estáticos
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

        if (mainButtonsContainer != null) {
            mainButtonsContainer.setVisible(true);
            mainButtonsContainer.setManaged(true);
        }
        if (btnCloseModule != null) {
            btnCloseModule.setVisible(false);
            btnCloseModule.setManaged(false);
        }
        contentArea.getChildren().clear();
    }

    // --- Cierre de Módulo (Botón X) ---

    @FXML
    private void handleCloseModule() {
        showOptions();
    }

    // --- Sub-navegación Multiprogramación ---

    @FXML
    private void handleOpenBitMap() {
        loadSubView("/fxml/Multiprogramming/bitmap_view.fxml");
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

            // Para Listas Ligadas creamos/cargamos el FXML y SIEMPRE actualizamos la estrategia
            FXMLLoader loader = new FXMLLoader(resource);
            Node node = loader.load();

            LinkedListController controller = loader.getController();
            if (controller != null) {
                controller.setStrategy(fitType);
            }

            desplegarVista(node);

        } catch (IOException e) {
            System.err.println("Error al cargar Listas Ligadas: " + e.getMessage());
            e.printStackTrace();
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

    // --- Carga con Caché para Vistas Estándar ---

    private void loadSubView(String fxmlPath) {
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

            desplegarVista(node);

        } catch (IOException e) {
            System.err.println("Error al cargar la sub-vista (" + fxmlPath + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Despliegue de Interfaz de Módulo ---

    private void desplegarVista(Node node) {
        // Oculta el menú de selección de sub-opciones
        if (subOptionsArea != null) {
            subOptionsArea.setVisible(false);
            subOptionsArea.setManaged(false);
        }

        // Alterna entre la barra de botones principales y el botón X de cerrar
        if (mainButtonsContainer != null) {
            mainButtonsContainer.setVisible(false);
            mainButtonsContainer.setManaged(false);
        }
        if (btnCloseModule != null) {
            btnCloseModule.setVisible(true);
            btnCloseModule.setManaged(true);
        }

        // Inyecta el módulo en el área central
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }
}