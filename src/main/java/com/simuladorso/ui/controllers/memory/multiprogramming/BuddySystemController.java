package com.simuladorso.ui.controllers.memory.multiprogramming;

import com.simuladorso.memory.algoritmos.buddy.BuddyTree;
import com.simuladorso.memory.algoritmos.buddy.BuddyTree.EstadoBloque;
import com.simuladorso.memory.algoritmos.buddy.BuddyTree.NodoBloque;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class BuddySystemController {

    @FXML private TextField txtTotalMemory;
    @FXML private VBox mainContainer;
    @FXML private HBox memoryBarContainer; // Contenedor de la barra horizontal

    @FXML private Label lblTotal;
    @FXML private Label lblUsed;
    @FXML private Label lblFree;
    @FXML private Label lblWaste;
    @FXML private Label lblMessage;

    @FXML private TextField txtProcessName;
    @FXML private TextField txtProcessSize;
    @FXML private TableView<NodoBloque> tblProcesses;
    @FXML private TableColumn<NodoBloque, String> colName;
    @FXML private TableColumn<NodoBloque, String> colRequested;
    @FXML private TableColumn<NodoBloque, String> colBlockSize;
    @FXML private TableColumn<NodoBloque, String> colRange;
    @FXML private TableColumn<NodoBloque, String> colWaste;
    @FXML private TableColumn<NodoBloque, Void> colAction;

    private BuddyTree buddyTree;
    private int totalMemoryKB = 1024;
    private int processCounter = 1;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProcessName()));
        colRequested.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRequestedKB() + " KB"));
        colBlockSize.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSizeKB() + " KB"));
        colRange.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStartAddress() + "–" + (d.getValue().getStartAddress() + d.getValue().getSizeKB() - 1) + " KB"));
        colWaste.setCellValueFactory(d -> new SimpleStringProperty((d.getValue().getSizeKB() - d.getValue().getRequestedKB()) + " KB"));

        // Botón de Liberación en la Tabla
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("Liberar");
            {
                btnDelete.setMaxWidth(Double.MAX_VALUE);
                btnDelete.setStyle(
                        "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-weight: bold; " +
                                "-fx-border-color: #fca5a5; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 3 6; -fx-cursor: hand;"
                );
                btnDelete.setOnAction(e -> {
                    NodoBloque node = getTableView().getItems().get(getIndex());
                    liberarProceso(node.getProcessName());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });
    }

    @FXML
    private void handleApplyConfig() {
        try {
            int rawTotal = Integer.parseInt(txtTotalMemory.getText().trim());
            if (rawTotal < 1024) {
                showError("La memoria total mínima permitida es de 1024 KB.");
                return;
            }

            totalMemoryKB = BuddyTree.getNextPowerOfTwo(rawTotal);
            txtTotalMemory.setText(String.valueOf(totalMemoryKB));

            buddyTree = new BuddyTree(totalMemoryKB);
            processCounter = 1;

            mainContainer.setVisible(true);
            mainContainer.setManaged(true);

            actualizarSugerenciasFormulario();
            refreshUI();
            showMessage("Sistema de Asociados inicializado en " + totalMemoryKB + " KB.", false);

        } catch (NumberFormatException e) {
            showError("Ingrese un valor numérico entero válido.");
        }
    }

    @FXML
    private void handleAddProcess() {
        if (buddyTree == null) return;
        try {
            String name = txtProcessName.getText().trim();
            int size = Integer.parseInt(txtProcessSize.getText().trim());

            if (name.isEmpty() || size <= 0) {
                showError("Complete el nombre y un tamaño mayor a 0 KB.");
                return;
            }

            boolean success = buddyTree.allocate(name, size);
            if (!success) {
                showError("No se pudo asignar. Memoria insuficiente o espacio contiguo no disponible.");
                return;
            }

            processCounter++;
            actualizarSugerenciasFormulario();
            refreshUI();
            showMessage("Proceso '" + name + "' (" + size + " KB) asignado exitosamente.", false);

        } catch (NumberFormatException e) {
            showError("Ingrese un tamaño numérico válido.");
        }
    }

    private void liberarProceso(String processName) {
        if (buddyTree != null && processName != null) {
            boolean success = buddyTree.deallocate(processName);
            if (success) {
                refreshUI();
                showMessage("Proceso '" + processName + "' liberado y compañeros (buddies) fusionados.", false);
            }
        }
    }

    @FXML
    private void handleResetMemory() {
        if (totalMemoryKB >= 1024) {
            buddyTree = new BuddyTree(totalMemoryKB);
            processCounter = 1;
            actualizarSugerenciasFormulario();
            refreshUI();
            showMessage("Memoria reiniciada.", false);
        }
    }

    private void actualizarSugerenciasFormulario() {
        txtProcessName.setText("DDD" + processCounter);
        int defaultSize = Math.max(1, (int) Math.round(totalMemoryKB * 0.05));
        txtProcessSize.setText(String.valueOf(defaultSize));
    }

    private void refreshUI() {
        List<NodoBloque> leaves = buddyTree.getLeafBlocks();

        // 1. Filtrar procesos asignados para la tabla
        List<NodoBloque> allocated = leaves.stream()
                .filter(n -> n.getEstado() == EstadoBloque.OCUPADO)
                .toList();
        tblProcesses.getItems().setAll(allocated);

        // 2. Calcular métricas
        int usedKB = 0;
        int wasteKB = 0;
        for (NodoBloque n : allocated) {
            usedKB += n.getSizeKB();
            wasteKB += (n.getSizeKB() - n.getRequestedKB());
        }

        lblTotal.setText(totalMemoryKB + " KB");
        lblUsed.setText(usedKB + " KB");
        lblFree.setText((totalMemoryKB - usedKB) + " KB");
        lblWaste.setText(wasteKB + " KB");

        // 3. Renderizar la Barra Gráfica Horizontal
        renderMemoryBar(leaves);
    }

    /**
     * Renderiza la barra horizontal proporcional con los bloques divididos
     */
    private void renderMemoryBar(List<NodoBloque> leaves) {
        memoryBarContainer.getChildren().clear();

        for (NodoBloque leaf : leaves) {
            // Calcular porcentaje del ancho de la barra según el tamaño del bloque
            double widthPercentage = ((double) leaf.getSizeKB() / totalMemoryKB) * 100.0;

            VBox block = new VBox();
            block.setAlignment(Pos.CENTER);
            block.setMaxHeight(Double.MAX_VALUE);
            HBox.setHgrow(block, Priority.ALWAYS);

            // Vinculación proporcional del ancho en JavaFX
            block.prefWidthProperty().bind(memoryBarContainer.widthProperty().multiply(widthPercentage / 100.0));

            Label lblContent = new Label();
            lblContent.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

            if (leaf.getEstado() == EstadoBloque.OCUPADO) {
                // Bloque Ocupado (Verde)
                block.setStyle("-fx-background-color: #22c55e; -fx-border-color: #0f172a; -fx-border-width: 1.5; -fx-background-radius: 4; -fx-border-radius: 4;");
                lblContent.setText(leaf.getProcessName() + " (" + leaf.getRequestedKB() + " KB / " + leaf.getSizeKB() + " KB)");
                lblContent.setStyle(lblContent.getStyle() + " -fx-text-fill: white;");
            } else {
                // Bloque Libre (Azul)
                block.setStyle("-fx-background-color: #3b82f6; -fx-border-color: #0f172a; -fx-border-width: 1.5; -fx-background-radius: 4; -fx-border-radius: 4;");
                lblContent.setText(leaf.getSizeKB() + " KB Libre");
                lblContent.setStyle(lblContent.getStyle() + " -fx-text-fill: white;");
            }

            block.getChildren().add(lblContent);
            memoryBarContainer.getChildren().add(block);
        }
    }

    private void showMessage(String text, boolean isError) {
        lblMessage.setStyle(isError ? "-fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-text-fill: #166534; -fx-font-weight: bold;");
        lblMessage.setText(text);
    }

    private void showError(String text) {
        showMessage(text, true);
    }
}