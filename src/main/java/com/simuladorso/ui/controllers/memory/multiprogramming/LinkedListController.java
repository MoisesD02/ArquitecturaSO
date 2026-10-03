package com.simuladorso.ui.controllers.memory.multiprogramming;

import com.simuladorso.memory.LinkedMemory;
import com.simuladorso.memory.LinkedMemory.Block;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class LinkedListController {

    @FXML private VBox root;
    @FXML private TextField txtCapacity;
    @FXML private ComboBox<String> cmbStrategy;
    @FXML private Button btnConfigure;
    @FXML private Label lblRule;
    @FXML private Label lblMessage;

    @FXML private Label lblTotal;
    @FXML private Label lblUsed;
    @FXML private Label lblFree;
    @FXML private Label lblGap;

    @FXML private VBox canvasContainer;
    @FXML private Canvas mapCanvas;
    @FXML private Label lblCursor;
    @FXML private HBox nodesContainer;

    @FXML private TextField txtProcessName;
    @FXML private TextField txtProcessSize;
    @FXML private TableView<Block> tblProcesses;
    @FXML private TableColumn<Block, String> colName;
    @FXML private TableColumn<Block, String> colSize;
    @FXML private TableColumn<Block, String> colStart;
    @FXML private TableColumn<Block, String> colEnd;
    @FXML private TableColumn<Block, Void> colAction;

    @FXML private Button btnRelease;
    @FXML private TextArea txtHistory;

    private final LinkedMemory memory = new LinkedMemory(1024);
    private int processCounter = 1;

    @FXML
    public void initialize() {
        cmbStrategy.getItems().setAll("First Fit", "Next Fit", "Best Fit", "Worst Fit");
        cmbStrategy.setValue("First Fit");
        cmbStrategy.setOnAction(e -> { updateRule(); draw(); });

        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setGraphic(null);
                if (!empty && getTableRow() != null && getTableRow().getItem() != null) {
                    Circle dot = new Circle(4, blockColor(getTableRow().getItem()));
                    dot.setStroke(Color.web("#64748b"));
                    setGraphic(dot);
                }
            }
        });

        colSize.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().size() + " KB"));
        colStart.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().start() + " KB"));
        colEnd.setCellValueFactory(d -> new SimpleStringProperty((d.getValue().start() + d.getValue().size()) + " KB"));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("Liberar");
            {
                btnDelete.setMaxWidth(Double.MAX_VALUE);
                btnDelete.setStyle(
                        "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-weight: bold; " +
                                "-fx-border-color: #fca5a5; -fx-border-radius: 4; -fx-background-radius: 4; " +
                                "-fx-padding: 3 6; -fx-font-size: 11px; -fx-cursor: hand;"
                );
                btnDelete.setOnAction(e -> {
                    Block b = getTableView().getItems().get(getIndex());
                    attempt(() -> {
                        memory.release(b.id());
                        refresh();
                        record();
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });

        if (canvasContainer != null) {
            canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.doubleValue() > 0) {
                    mapCanvas.setWidth(Math.max(100, newVal.doubleValue() - 10));
                    draw();
                }
            });
        }

        mapCanvas.setOnMouseClicked(e -> {
            if (e.getY() < 10 || e.getY() > 55) {
                tblProcesses.getSelectionModel().clearSelection();
                return;
            }
            int address = (int) (e.getX() / mapCanvas.getWidth() * memory.capacity());
            tblProcesses.getSelectionModel().clearSelection();
            memory.blocks().stream()
                    .filter(b -> !b.free() && address >= b.start() && address < b.start() + b.size())
                    .findFirst()
                    .ifPresent(b -> tblProcesses.getSelectionModel().select(b));
        });

        tblProcesses.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (btnRelease != null) btnRelease.setDisable(b == null);
            draw();
        });

        EventHandler<MouseEvent> dismissSelection = event -> {
            if (!(event.getTarget() instanceof Node target)) return;
            for (Node node = target; node != null; node = node.getParent()) {
                if (node == btnRelease || node == mapCanvas) return;
                if (node instanceof TableRow<?> row && row.getTableView() == tblProcesses && !row.isEmpty()) return;
                if (node.getUserData() instanceof Block block && !block.free()) return;
            }
            tblProcesses.getSelectionModel().clearSelection();
        };

        root.sceneProperty().addListener((observable, previous, current) -> {
            if (previous != null) previous.removeEventFilter(MouseEvent.MOUSE_PRESSED, dismissSelection);
            if (current != null) current.addEventFilter(MouseEvent.MOUSE_PRESSED, dismissSelection);
        });

        updateRule();
        actualizarSugerenciasFormulario();
        refresh();
        record();
    }

    public void setStrategy(String value) {
        if (cmbStrategy != null && cmbStrategy.getItems().contains(value)) {
            cmbStrategy.setValue(value);
            updateRule();
            draw();
        }
    }

    @FXML
    private void handleApplyCapacity() {
        attempt(() -> {
            memory.reset(number(txtCapacity));
            processCounter = 1;
            actualizarSugerenciasFormulario();
            refresh();
            record();
        });
    }

    @FXML
    private void handleAddProcess() {
        attempt(() -> {
            String pName = txtProcessName.getText().trim();
            int pSize = number(txtProcessSize);
            Block b = memory.allocate(pName, pSize, cmbStrategy.getValue());

            processCounter++;
            actualizarSugerenciasFormulario();

            refresh();
            tblProcesses.getSelectionModel().select(b);
            record();
        });
    }

    @FXML
    private void handleReleaseSelected() {
        attempt(() -> {
            Block b = tblProcesses.getSelectionModel().getSelectedItem();
            if (b != null) {
                memory.release(b.id());
                refresh();
                record();
            }
        });
    }

    @FXML
    private void handleLoadExample() {
        memory.example();
        txtCapacity.setText("1024");
        processCounter = 1;
        actualizarSugerenciasFormulario();
        refresh();
        record();
    }

    @FXML
    private void handleResetMemory() {
        memory.reset(memory.capacity());
        txtHistory.clear();
        processCounter = 1;
        actualizarSugerenciasFormulario();
        refresh();
        record();
    }

    private void actualizarSugerenciasFormulario() {
        if (txtProcessName != null) txtProcessName.setText("DDD" + processCounter);
        if (txtProcessSize != null) {
            int defaultSize = Math.max(1, (int) Math.round(memory.capacity() * 0.05));
            txtProcessSize.setText(String.valueOf(defaultSize));
        }
    }

    private void updateRule() {
        if (lblRule == null || cmbStrategy == null) return;
        lblRule.setText(switch (cmbStrategy.getValue()) {
            case "Next Fit" -> "Busca desde el cursor y vuelve al principio si hace falta. El cursor se actualiza después de cada asignación.";
            case "Best Fit" -> "Examina los huecos y elige el más pequeño donde quepa. En empate, el de menor dirección.";
            case "Worst Fit" -> "Examina los huecos y elige el más grande. En empate, el de menor dirección.";
            default -> "Recorre la lista desde el inicio y utiliza el primer hueco suficiente.";
        });
    }

    private int number(TextField input) {
        try {
            return Integer.parseInt(input.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Introduce un número entero válido en KB.");
        }
    }

    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException e) {
            lblMessage.setStyle("-fx-text-fill: #b42318; -fx-font-weight: bold;");
            lblMessage.setText(e.getMessage());
            txtHistory.appendText("No asignado: " + e.getMessage() + "\n");
        }
    }

    private void record() {
        lblMessage.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
        lblMessage.setText(memory.explanation());
        txtHistory.appendText(memory.explanation() + "\n");
    }

    private void refresh() {
        var blocks = memory.blocks();
        tblProcesses.getItems().setAll(blocks.stream().filter(b -> !b.free()).toList());

        lblTotal.setText(memory.capacity() + " KB");
        lblUsed.setText((memory.capacity() - memory.freeKB()) + " KB");
        lblFree.setText(memory.freeKB() + " KB");
        lblGap.setText(memory.largestGap() + " KB");

        boolean busy = memory.freeKB() != memory.capacity();
        txtCapacity.setDisable(busy);
        btnConfigure.setDisable(busy);
        if (btnRelease != null) btnRelease.setDisable(true);

        // Renderizar la cadena de nodos ligados (MÁS GRANDES Y VISIBLES)
        nodesContainer.getChildren().clear();
        for (Block b : blocks) {
            VBox node = new VBox(5);
            node.setPadding(new Insets(10, 14, 10, 14));
            node.setMinWidth(135);
            node.setMinHeight(80);
            node.setStyle("-fx-background-color: " + colorCss(b) + "; -fx-background-radius: 8; -fx-border-color: #94a3b8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 4, 0, 0, 2);");
            node.setUserData(b);

            Label lblTitle = new Label(b.free() ? "NODO LIBRE" : b.name());
            lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");

            Label lblStart = new Label("Inicio: " + b.start() + " KB");
            lblStart.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");

            Label lblSize = new Label("Tamaño: " + b.size() + " KB");
            lblSize.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");

            node.getChildren().addAll(lblTitle, lblStart, lblSize);
            node.setOnMouseClicked(e -> {
                tblProcesses.getSelectionModel().clearSelection();
                if (!b.free()) tblProcesses.getSelectionModel().select(b);
            });

            Label lblArrow = new Label("➔");
            lblArrow.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #475569;");

            nodesContainer.getChildren().addAll(node, lblArrow);
        }
        Label lblNull = new Label("null");
        lblNull.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #94a3b8; -fx-padding: 0 10 0 0;");
        nodesContainer.getChildren().add(lblNull);

        draw();
    }

    private void draw() {
        if (mapCanvas == null) return;
        GraphicsContext g = mapCanvas.getGraphicsContext2D();
        double width = mapCanvas.getWidth();
        g.clearRect(0, 0, width, 75);

        Block selected = tblProcesses.getSelectionModel().getSelectedItem();

        for (Block b : memory.blocks()) {
            double x = b.start() / (double) memory.capacity() * width;
            double w = b.size() / (double) memory.capacity() * width;

            g.setFill(blockColor(b));
            g.fillRect(x, 10, Math.max(1, w - 1), 40);

            g.setStroke(Color.web("#0f172a"));
            g.setLineWidth(1.0);
            g.strokeRect(x, 10, Math.max(1, w - 1), 40);

            if (selected != null && selected.id() != b.id()) {
                g.setFill(Color.rgb(255, 255, 255, 0.60));
                g.fillRect(x, 10, w, 40);
            }

            if (w > 45) {
                g.setFill(Color.web("#0f172a"));
                g.fillText(b.free() ? "Libre" : b.name(), x + 6, 26);
                g.fillText(b.size() + " KB", x + 6, 42);
            }
        }

        g.setFill(Color.web("#475569"));
        g.fillText("0 KB", 0, 65);
        g.fillText(memory.capacity() + " KB", Math.max(0, width - 70), 65);

        boolean nextFit = "Next Fit".equals(cmbStrategy.getValue());
        if (lblCursor != null) {
            lblCursor.setVisible(nextFit);
            lblCursor.setManaged(nextFit);
            lblCursor.setText("Próxima búsqueda desde: " + memory.cursor() + " KB · Cursor Next Fit");
        }

        if (nextFit) {
            double x = memory.cursor() / (double) memory.capacity() * width;
            g.setStroke(Color.web("#ea580c"));
            g.setLineWidth(3);
            g.strokeLine(x, 0, x, 52);
        }
    }

    private Color blockColor(Block b) {
        return b.free() ? Color.web("#2563eb") : Color.hsb(((b.id() - 1) * 137.508 + 145) % 360, .68, .85);
    }

    private String colorCss(Block b) {
        Color c = blockColor(b);
        return String.format("#%02x%02x%02x", (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }
}