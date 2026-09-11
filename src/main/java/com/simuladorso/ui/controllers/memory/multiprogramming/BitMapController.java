package com.simuladorso.ui.controllers.memory.multiprogramming;

import com.simuladorso.memory.BitmapMemory;
import com.simuladorso.memory.BitmapMemory.Allocation;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class BitMapController {
    @FXML private VBox root;
    private final BitmapMemory memory = new BitmapMemory();
    private final Canvas canvas = new Canvas();
    private final TextField unit = new TextField("4");
    private final TextField name = new TextField();
    private final TextField size = new TextField();
    private final Label summary = new Label();
    private final Label detail = new Label("Pasa el cursor por una celda para inspeccionarla.");
    private final Label message = new Label();
    private final TableView<Allocation> list = new TableView<>();
    private final Label total = new Label(), used = new Label(), free = new Label(), waste = new Label();
    private final ProgressBar occupancy = new ProgressBar();
    private final TextArea log = new TextArea();
    private final Button apply = new Button("Aplicar unidad");
    private final Button release = new Button("Liberar seleccionado");
    private int columns = 64;
    private double cell;

    @FXML public void initialize() {
        root.setPadding(new Insets(20));
        root.setSpacing(18); root.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        Label title = new Label("Mapa de bits");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label subtitle = new Label("MEMORIA  /  MULTIPROGRAMACIÓN  /  ASIGNACIÓN CONTIGUA");
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        ComboBox<String> layout = new ComboBox<>();
        layout.getItems().addAll("64 columnas × 16 filas", "32 columnas × 32 filas");
        layout.getSelectionModel().selectFirst();
        layout.setOnAction(e -> { columns = layout.getSelectionModel().getSelectedIndex() == 0 ? 64 : 32; draw(); });
        unit.setPrefColumnCount(6);
        apply.setOnAction(e -> attempt(() -> {
            memory.configure(integer(unit)); refresh(); record("Unidad configurada: " + memory.unitKB() + " KB.");
        }));
        FlowPane settings = new FlowPane(10, 10, new Label("Unidad (KB):"), unit, apply, layout);
        summary.setWrapText(true);
        summary.setStyle("-fx-text-fill: #64748b;");
        occupancy.setMaxWidth(Double.MAX_VALUE);
        occupancy.setStyle("-fx-accent: #16a34a;");
        HBox metrics = new HBox(12, metric("MEMORIA TOTAL", total), metric("UTILIZADA", used), metric("DISPONIBLE", free), metric("SOBRANTE INTERNO", waste));
        StackPane map = new StackPane(canvas);
        map.setMinWidth(0);
        map.widthProperty().addListener((o, a, b) -> { canvas.setWidth(Math.max(100, b.doubleValue())); draw(); });
        canvas.setOnMouseMoved(e -> inspect(index(e.getX(), e.getY())));
        canvas.setOnMouseClicked(e -> {
            int index = index(e.getX(), e.getY());
            if (index < 0) return;
            int id = memory.owner(index);
            list.getSelectionModel().clearSelection();
            memory.allocations().stream().filter(a -> a.id() == id).findFirst()
                    .ifPresent(a -> list.getSelectionModel().select(a));
        });
        detail.setWrapText(true);
        name.setPromptText("Ej. Editor"); name.setPrefColumnCount(12);
        size.setPromptText("Tamaño en KB"); size.setPrefColumnCount(9);
        Button add = new Button("Asignar proceso");
        add.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 9 16;");
        add.setOnAction(e -> attempt(() -> {
            Allocation a = memory.allocate(name.getText(), integer(size));
            refresh(); list.getSelectionModel().select(a);
            record(a.name() + ": " + a.requestedKB() + " KB → " + a.units() + " unidades; celdas "
                    + a.start() + " a " + (a.start() + a.units() - 1) + ". Sobrante: "
                    + ((long) a.units() * memory.unitKB() - a.requestedKB()) + " KB.");
            name.clear(); size.clear();
        }));
        release.setOnAction(e -> {
            Allocation a = list.getSelectionModel().getSelectedItem();
            if (a != null) { memory.release(a.id()); refresh(); record(a.name() + " liberado. Sus bits vuelven a 0."); }
        });
        Button clearSelection = new Button("Quitar resaltado");
        clearSelection.setOnAction(e -> list.getSelectionModel().clearSelection());
        Button reset = new Button("Reiniciar mapa");
        reset.setOnAction(e -> { memory.reset(); refresh(); log.clear(); record("Mapa reiniciado: 1024 unidades libres."); });
        Button example = new Button("Ejemplo de clase");
        example.setOnAction(e -> {
            if (!memory.allocations().isEmpty()) { error("Reinicia el mapa antes de cargar el ejemplo."); return; }
            memory.configure(4); unit.setText("4");
            memory.allocate("A", 12); memory.allocate("B", 20); memory.allocate("C", 7);
            refresh(); record("Ejemplo: A=12 KB (3 unidades), B=20 KB (5), C=7 KB (2). Libera B y prueba otro proceso.");
        });
        FlowPane actions = new FlowPane(10, 10, field("Nombre", name), field("Memoria solicitada (KB)", size), add);
        actions.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        FlowPane tools = new FlowPane(10, 10, release, clearSelection, example, reset);
        reset.setStyle("-fx-text-fill: #b42318;");
        list.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        column("Proceso", a -> a.name());
        column("Solicitada", a -> a.requestedKB() + " KB");
        column("Asignada", a -> (long)a.units() * memory.unitKB() + " KB");
        column("Celdas", a -> a.start() + "–" + (a.start()+a.units()-1));
        column("Sobrante", a -> ((long)a.units()*memory.unitKB()-a.requestedKB()) + " KB");
        list.setPrefHeight(180); list.setMinHeight(180);
        list.setPlaceholder(new Label("No hay procesos asignados."));
        list.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            release.setDisable(b == null); draw();
        });
        log.setEditable(false); log.setWrapText(true); log.setPrefRowCount(4);
        message.setWrapText(true);
        Label note = new Label("Búsqueda desde el inicio: se toma el primer hueco suficiente. El final de una fila continúa en la siguiente. Selecciona un proceso para verlo bajo el resaltado.");
        note.setWrapText(true);
        Label legend = new Label("●  Azul · Libre (0)       ●  Verde · Ocupado (1)       Borde oscuro · Proceso seleccionado");
        legend.setWrapText(true);
        legend.setStyle("-fx-text-fill: #334155;");
        detail.setStyle("-fx-background-color: #eff6ff; -fx-padding: 10; -fx-background-radius: 6; -fx-text-fill: #1e3a8a;");
        detail.setMaxWidth(Double.MAX_VALUE);
        VBox config = new VBox(10, new Label("Configuración de memoria"), settings);
        TitledPane configPane = new TitledPane("Configuración · 1024 unidades de asignación", config);
        configPane.setExpanded(false);
        TitledPane history = new TitledPane("Historial de operaciones", log);
        history.setExpanded(false);
        root.getChildren().addAll(subtitle, title, metrics, occupancy, summary, configPane, legend, map, detail, actions,
                message, new Label("Procesos en memoria"), list, tools, history, note);
        refresh(); record("Mapa preparado. Configura la unidad y agrega un proceso.");
    }
    private VBox metric(String caption, Label value) {
        value.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label label = new Label(caption); label.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
        VBox box = new VBox(8, label, value); box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8;");
        box.setMinWidth(0); box.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(box, Priority.ALWAYS); return box;
    }
    private VBox field(String text, TextField input) { Label label = new Label(text); label.setStyle("-fx-text-fill: #334155;"); return new VBox(5, label, input); }
    private void column(String title, java.util.function.Function<Allocation, String> value) {
        TableColumn<Allocation, String> c = new TableColumn<>(title);
        c.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(value.apply(data.getValue())));
        list.getColumns().add(c);
    }
    private int integer(TextField field) {
        try { return Integer.parseInt(field.getText().trim()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Introduce un número entero válido."); }
    }
    private void attempt(Runnable action) { try { action.run(); } catch (IllegalArgumentException ex) { error(ex.getMessage()); } }
    private void error(String text) { message.setStyle("-fx-text-fill: #b42318;"); message.setText(text); log.appendText("No asignado / configuración: " + text + "\n"); }
    private void record(String text) { message.setStyle("-fx-text-fill: #166534;"); message.setText(text); log.appendText(text + "\n"); }
    private void refresh() {
        list.getItems().setAll(memory.allocations());
        boolean occupied = !memory.allocations().isEmpty();
        unit.setDisable(occupied); apply.setDisable(occupied);
        release.setDisable(list.getSelectionModel().getSelectedItem() == null);
        total.setText((long)BitmapMemory.CELLS * memory.unitKB() + " KB");
        used.setText((long)(BitmapMemory.CELLS-memory.freeUnits()) * memory.unitKB() + " KB");
        free.setText((long)memory.freeUnits() * memory.unitKB() + " KB");
        waste.setText(memory.wasteKB() + " KB");
        occupancy.setProgress((BitmapMemory.CELLS-memory.freeUnits())/(double)BitmapMemory.CELLS);
        summary.setText(String.format("Ocupación: %.1f %%   ·   Unidad: %d KB   ·   Mayor hueco: %d celdas   ·   %d procesos",
                occupancy.getProgress()*100, memory.unitKB(), memory.largestGap(), memory.allocations().size()));
        detail.setText("Celdas 0–1023. Selecciona un proceso o pasa el cursor por el mapa.");
        draw();
    }
    private int index(double x, double y) {
        x -= 38;
        if (cell <= 0 || x < 0 || y < 0 || x >= columns * cell) return -1;
        int i = (int) (y / cell) * columns + (int) (x / cell);
        return i >= 0 && i < BitmapMemory.CELLS ? i : -1;
    }
    private void inspect(int i) {
        if (i < 0) return;
        String owner = memory.allocations().stream().filter(a -> a.id() == memory.owner(i))
                .map(Allocation::name).findFirst().orElse("Libre");
        detail.setText("Celda " + i + " · Bit " + (memory.owner(i) == 0 ? 0 : 1) + " · " + owner
                + " · Rango: " + (long) i * memory.unitKB() + "–" + ((long) (i + 1) * memory.unitKB() - 1) + " KB");
    }
    private void draw() {
        cell = Math.max(1, (canvas.getWidth() - 38) / columns);
        canvas.setHeight(cell * (BitmapMemory.CELLS / columns));
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        Allocation selected = list.getSelectionModel().getSelectedItem();
        for (int i = 0; i < BitmapMemory.CELLS; i++) {
            double x = 38 + (i % columns) * cell, y = (i / columns) * cell;
            if (i % columns == 0) {
                g.setFill(Color.web("#64748b")); g.setFont(javafx.scene.text.Font.font(10));
                g.fillText(String.format("%04d", i), 0, y + cell * .73);
            }
            int owner = memory.owner(i);
            g.setFill(Color.web(owner == 0 ? "#2563eb" : "#16a34a"));
            g.fillRect(x + 1, y + 1, Math.max(1, cell - 2), Math.max(1, cell - 2));
            if (cell >= 12) {
                g.setFill(Color.WHITE);
                g.setFont(javafx.scene.text.Font.font(Math.min(12, cell * .65)));
                g.fillText(owner == 0 ? "0" : "1", x + cell * .3, y + cell * .73);
            }
            if (selected != null) {
                if (owner != selected.id()) {
                    g.setFill(Color.rgb(255, 255, 255, .70)); g.fillRect(x, y, cell, cell);
                } else {
                    g.setStroke(Color.web("#0f172a")); g.setLineWidth(2);
                    g.strokeRect(x + 1, y + 1, cell - 2, cell - 2);
                }
            }
        }
    }
}
