package com.simuladorso.ui.controllers.memory.multiprogramming;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class BitMapController {

    @FXML private TextField txtTotalMemory;
    @FXML private TextField txtUnitSize;
    @FXML private VBox mapContainer;
    @FXML private Canvas mapCanvas;

    @FXML private Label lblTotal;
    @FXML private Label lblUsed;
    @FXML private Label lblFree;
    @FXML private Label lblWaste;
    @FXML private Label lblDetail;
    @FXML private Label lblMessage;
    @FXML private Region regLegendFree;

    @FXML private TextField txtProcessName;
    @FXML private TextField txtProcessSize;
    @FXML private TableView<ProcesoAsignado> tblProcesses;
    @FXML private TableColumn<ProcesoAsignado, String> colName;
    @FXML private TableColumn<ProcesoAsignado, String> colRequested;
    @FXML private TableColumn<ProcesoAsignado, String> colAssigned;
    @FXML private TableColumn<ProcesoAsignado, String> colCells;
    @FXML private TableColumn<ProcesoAsignado, String> colWaste;
    @FXML private TableColumn<ProcesoAsignado, Void> colAction;

    private int totalMemoryKB = 1024;
    private int unitKB = 4;
    private int totalCells = 256;
    private int[] owners;
    private final List<ProcesoAsignado> allocations = new ArrayList<>();

    private int nextId = 1;
    private int processCounter = 1;

    private final int columns = 32;
    private final double cellWidth = 24;

    public record ProcesoAsignado(int id, String name, int requestedKB, int start, int units) {}

    @FXML
    public void initialize() {
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().name()));
        colRequested.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().requestedKB() + " KB"));
        colAssigned.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty((data.getValue().units() * unitKB) + " KB"));
        colCells.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().start() + "–" + (data.getValue().start() + data.getValue().units() - 1)));
        colWaste.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(((data.getValue().units() * unitKB) - data.getValue().requestedKB()) + " KB"));

        // BOTÓN LIBERAR ANCHO (CUBRE CASI TODA LA CASILLA)
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("Liberar");
            {
                btnDelete.setMaxWidth(Double.MAX_VALUE);
                btnDelete.setStyle(
                        "-fx-background-color: #fee2e2; " +
                                "-fx-text-fill: #dc2626; " +
                                "-fx-font-weight: bold; " +
                                "-fx-border-color: #fca5a5; " +
                                "-fx-border-radius: 4; " +
                                "-fx-background-radius: 4; " +
                                "-fx-padding: 4 6; " +
                                "-fx-font-size: 11px; " +
                                "-fx-cursor: hand;"
                );

                btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(
                        "-fx-background-color: #fca5a5; " +
                                "-fx-text-fill: #991b1b; " +
                                "-fx-font-weight: bold; " +
                                "-fx-border-color: #f87171; " +
                                "-fx-border-radius: 4; " +
                                "-fx-background-radius: 4; " +
                                "-fx-padding: 4 6; " +
                                "-fx-font-size: 11px; " +
                                "-fx-cursor: hand;"
                ));

                btnDelete.setOnMouseExited(e -> btnDelete.setStyle(
                        "-fx-background-color: #fee2e2; " +
                                "-fx-text-fill: #dc2626; " +
                                "-fx-font-weight: bold; " +
                                "-fx-border-color: #fca5a5; " +
                                "-fx-border-radius: 4; " +
                                "-fx-background-radius: 4; " +
                                "-fx-padding: 4 6; " +
                                "-fx-font-size: 11px; " +
                                "-fx-cursor: hand;"
                ));

                btnDelete.setOnAction(event -> {
                    ProcesoAsignado proceso = getTableView().getItems().get(getIndex());
                    liberarProceso(proceso);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDelete);
                }
            }
        });

        // CONTROL DE TOGGLE DE SELECCIÓN SIN CONFLICTION CON JAVAFX
        tblProcesses.setRowFactory(tv -> {
            TableRow<ProcesoAsignado> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    ProcesoAsignado item = row.getItem();
                    ProcesoAsignado currentSelected = tblProcesses.getSelectionModel().getSelectedItem();

                    if (currentSelected != null && currentSelected.equals(item)) {
                        // Consumimos el evento y limpiamos la selección
                        tblProcesses.getSelectionModel().clearSelection();
                        event.consume();
                    }
                }
            });
            return row;
        });

        tblProcesses.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            actualizarLeyenda(newVal != null);
            drawMap();
        });

        mapCanvas.setOnMouseMoved(e -> inspectCell(indexFromCoords(e.getX(), e.getY())));
    }

    @FXML
    private void handleApplyConfig() {
        try {
            int rawTotal = Integer.parseInt(txtTotalMemory.getText().trim());
            int rawUnit = Integer.parseInt(txtUnitSize.getText().trim());

            if (rawTotal <= 0 || rawUnit <= 0) {
                showError("La memoria y la unidad deben ser mayores a cero.");
                return;
            }

            totalMemoryKB = aproximarPotenciaDe2(rawTotal);
            txtTotalMemory.setText(String.valueOf(totalMemoryKB));

            if (rawUnit > totalMemoryKB) {
                showError("El tamaño de unidad (" + rawUnit + " KB) no puede ser mayor a la memoria (" + totalMemoryKB + " KB).");
                return;
            }
            unitKB = rawUnit;

            totalCells = totalMemoryKB / unitKB;
            owners = new int[totalCells];
            allocations.clear();
            nextId = 1;
            processCounter = 1;

            mapContainer.setVisible(true);
            mapContainer.setManaged(true);

            actualizarSugerenciasFormulario();
            refreshUI();
            showMessage("Memoria inicializada: " + totalMemoryKB + " KB / " + unitKB + " KB = " + totalCells + " celdas.", false);

        } catch (NumberFormatException e) {
            showError("Ingrese números enteros válidos.");
        }
    }

    @FXML
    private void handleAddProcess() {
        try {
            String pName = txtProcessName.getText().trim();
            int pSize = Integer.parseInt(txtProcessSize.getText().trim());

            if (pName.isEmpty() || pSize <= 0) {
                showError("Complete el nombre y un tamaño mayor a 0.");
                return;
            }

            int neededUnits = (pSize + unitKB - 1) / unitKB;

            int run = 0;
            int startIndex = -1;
            for (int i = 0; i < totalCells; i++) {
                if (owners[i] == 0) {
                    run++;
                    if (run == neededUnits) {
                        startIndex = i - neededUnits + 1;
                        break;
                    }
                } else {
                    run = 0;
                }
            }

            if (startIndex == -1) {
                showError("Memoria insuficiente o fragmentada. Se requerían " + neededUnits + " celdas consecutivas.");
                return;
            }

            ProcesoAsignado alloc = new ProcesoAsignado(nextId++, pName, pSize, startIndex, neededUnits);
            for (int i = startIndex; i < startIndex + neededUnits; i++) {
                owners[i] = alloc.id();
            }
            allocations.add(alloc);

            processCounter++;
            actualizarSugerenciasFormulario();

            refreshUI();
            tblProcesses.getSelectionModel().select(alloc);
            showMessage("Proceso '" + pName + "' asignado (" + neededUnits + " celdas).", false);

        } catch (NumberFormatException e) {
            showError("Ingrese un tamaño de proceso válido.");
        }
    }

    private void liberarProceso(ProcesoAsignado proceso) {
        if (proceso != null) {
            for (int i = proceso.start(); i < proceso.start() + proceso.units(); i++) {
                owners[i] = 0;
            }
            allocations.remove(proceso);
            refreshUI();
            showMessage("Proceso '" + proceso.name() + "' liberado.", false);
        }
    }

    @FXML
    private void handleResetMemory() {
        if (owners != null) {
            for (int i = 0; i < owners.length; i++) owners[i] = 0;
        }
        allocations.clear();
        nextId = 1;
        processCounter = 1;
        actualizarSugerenciasFormulario();
        refreshUI();
        showMessage("Mapa reiniciado.", false);
    }

    private void actualizarSugerenciasFormulario() {
        txtProcessName.setText("DDD" + processCounter);
        int defaultSize = Math.max(1, (int) Math.round(totalMemoryKB * 0.05));
        txtProcessSize.setText(String.valueOf(defaultSize));
    }

    private void actualizarLeyenda(boolean haySeleccion) {
        if (regLegendFree != null) {
            if (haySeleccion) {
                regLegendFree.setStyle("-fx-background-color: rgba(255, 255, 255, 0.65); -fx-border-color: #93c5fd; -fx-background-radius: 3; -fx-border-radius: 3;");
            } else {
                regLegendFree.setStyle("-fx-background-color: #2563eb; -fx-border-color: #1d4ed8; -fx-background-radius: 3; -fx-border-radius: 3;");
            }
        }
    }

    private int aproximarPotenciaDe2(int valor) {
        if (valor <= 1) return 1;
        double exponente = Math.log(valor) / Math.log(2);
        long redondeado = Math.round(exponente);
        return (int) Math.pow(2, redondeado);
    }

    private void refreshUI() {
        tblProcesses.getItems().setAll(allocations);

        int usedCells = 0;
        long totalWasteKB = 0;
        for (ProcesoAsignado a : allocations) {
            usedCells += a.units();
            totalWasteKB += ((long) a.units() * unitKB) - a.requestedKB();
        }

        lblTotal.setText(totalMemoryKB + " KB");
        lblUsed.setText((usedCells * unitKB) + " KB");
        lblFree.setText(((totalCells - usedCells) * unitKB) + " KB");
        lblWaste.setText(totalWasteKB + " KB");

        drawMap();
    }

    private void drawMap() {
        if (totalCells == 0) return;

        int rows = (int) Math.ceil((double) totalCells / columns);
        mapCanvas.setWidth(columns * cellWidth);
        mapCanvas.setHeight(rows * cellWidth);

        GraphicsContext g = mapCanvas.getGraphicsContext2D();
        g.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        ProcesoAsignado selected = tblProcesses.getSelectionModel().getSelectedItem();

        for (int i = 0; i < totalCells; i++) {
            double x = (i % columns) * cellWidth;
            double y = (i / columns) * cellWidth;

            int ownerId = owners[i];
            boolean isOccupied = ownerId != 0;

            g.setFill(Color.web(isOccupied ? "#16a34a" : "#2563eb"));
            g.fillRect(x, y, cellWidth, cellWidth);

            g.setStroke(Color.web("#0f172a"));
            g.setLineWidth(1.2);
            g.strokeRect(x, y, cellWidth, cellWidth);

            if (selected != null) {
                if (ownerId != selected.id()) {
                    g.setFill(Color.rgb(255, 255, 255, 0.65));
                    g.fillRect(x, y, cellWidth, cellWidth);
                } else {
                    g.setStroke(Color.web("#3A5A40"));
                    g.setLineWidth(1.5);
                    g.strokeRect(x + 0.75, y + 0.75, cellWidth - 1.5, cellWidth - 1.5);
                }
            }
        }
    }

    private int indexFromCoords(double x, double y) {
        if (x < 0 || y < 0 || x >= columns * cellWidth) return -1;
        int col = (int) (x / cellWidth);
        int row = (int) (y / cellWidth);
        int idx = row * columns + col;
        return (idx >= 0 && idx < totalCells) ? idx : -1;
    }

    private void inspectCell(int idx) {
        if (idx < 0 || idx >= totalCells) {
            lblDetail.setText("Pasa el cursor por una celda para inspeccionarla.");
            return;
        }
        int ownerId = owners[idx];
        String ownerName = allocations.stream()
                .filter(a -> a.id() == ownerId)
                .map(ProcesoAsignado::name)
                .findFirst()
                .orElse("Libre");

        lblDetail.setText("Celda " + idx + " | Bit: " + (ownerId == 0 ? "0" : "1") + " | Estado: " + ownerName
                + " | Rango: " + (idx * unitKB) + "–" + (((idx + 1) * unitKB) - 1) + " KB");
    }

    private void showMessage(String text, boolean isError) {
        lblMessage.setStyle(isError ? "-fx-text-fill: #b42318; -fx-font-weight: bold;" : "-fx-text-fill: #166534; -fx-font-weight: bold;");
        lblMessage.setText(text);
    }

    private void showError(String text) {
        showMessage(text, true);
    }
}