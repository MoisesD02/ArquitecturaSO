package com.simuladorso.ui.controllers.files;

import com.simuladorso.filesystem.model.INodo;
import com.simuladorso.filesystem.services.FileSystemManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

public class FilesController {

    @FXML private Spinner<Integer> spnTamanoBloque; // Nuevo control para el peso del bloque
    @FXML private TextField txtNombre;
    @FXML private Spinner<Integer> spnBloques;
    @FXML private Label lblEspacioLibre;
    @FXML private FlowPane gridDisco;

    @FXML private TableView<INodo> tblINodos;
    @FXML private TableColumn<INodo, Number> colId;
    @FXML private TableColumn<INodo, String> colNombre;
    @FXML private TableColumn<INodo, Number> colTamano;

    @FXML private Label lblMetaId;
    @FXML private Label lblMetaNombre;
    @FXML private Label lblMetaFecha;
    @FXML private Label lblMetaTamanoBytes;
    @FXML private Label lblMetaBloques;
    @FXML private Label lblMetaPerm;
    @FXML private TextField txtMetaRutaVirtual;
    @FXML private TextArea txtMetaRuta;

    private final FileSystemManager fileSystem = new FileSystemManager();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreArchivo()));
        colTamano.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getTamanoBloques()));

        tblINodos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            mostrarMetadatos(newSelection);
        });

        actualizarVista();
    }

    @FXML
    private void handleCrearArchivo() {
        String nombre = txtNombre.getText().trim();
        int bloques = spnBloques.getValue();
        int tamanoBloqueKB = spnTamanoBloque.getValue();

        if (nombre.isEmpty()) {
            mostrarAlerta("Atención", "Ingrese un nombre válido para el archivo.");
            return;
        }

        if (!fileSystem.crearArchivo(nombre, bloques, tamanoBloqueKB)) {
            mostrarAlerta("Error", "Fallo al crear archivo. Espacio insuficiente o nombre duplicado.");
            return;
        }

        txtNombre.clear();
        actualizarVista();
    }

    @FXML
    private void handleEliminarArchivo() {
        INodo seleccionado = tblINodos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un archivo de la tabla para eliminarlo.");
            return;
        }

        if (fileSystem.eliminarArchivo(seleccionado.getNombreArchivo())) {
            mostrarMetadatos(null);
            actualizarVista();
        }
    }

    private void mostrarMetadatos(INodo inodo) {
        if (inodo != null) {
            lblMetaId.setText(String.valueOf(inodo.getId()));
            lblMetaNombre.setText(inodo.getNombreArchivo());
            lblMetaFecha.setText(inodo.getFechaCreacion());
            lblMetaTamanoBytes.setText(inodo.getTamanoBytes());
            lblMetaBloques.setText(inodo.getBloquesAsignados().toString());
            lblMetaPerm.setText(inodo.getPermisos() + " | Prop: " + inodo.getPropietario());
            txtMetaRutaVirtual.setText(inodo.getRutaVirtual());
            txtMetaRuta.setText(inodo.getRutaFisica());
        } else {
            lblMetaId.setText("-");
            lblMetaNombre.setText("-");
            lblMetaFecha.setText("-");
            lblMetaTamanoBytes.setText("-");
            lblMetaBloques.setText("-");
            lblMetaPerm.setText("-");
            txtMetaRutaVirtual.clear();
            txtMetaRuta.clear();
        }
    }

    private void actualizarVista() {
        tblINodos.getItems().setAll(fileSystem.getTodosLosArchivos());

        int libres = fileSystem.getBloquesLibres();
        lblEspacioLibre.setText(libres + " / " + fileSystem.getTotalBloques() + " Libres");

        if (libres == 0) {
            lblEspacioLibre.setStyle("-fx-background-color: rgba(248, 113, 113, 0.2); -fx-text-fill: #fca5a5; -fx-padding: 6px 14px; -fx-background-radius: 6px; -fx-font-weight: bold;");
        } else {
            lblEspacioLibre.setStyle("-fx-background-color: rgba(134, 239, 172, 0.2); -fx-text-fill: #86efac; -fx-padding: 6px 14px; -fx-background-radius: 6px; -fx-font-weight: bold;");
        }

        boolean hayArchivosCreados = !fileSystem.getTodosLosArchivos().isEmpty();
        spnTamanoBloque.setDisable(hayArchivosCreados);

        gridDisco.getChildren().clear();
        boolean[] disco = fileSystem.getEstadoDisco();

        for (int i = 0; i < disco.length; i++) {
            StackPane bloqueNode = new StackPane();
            bloqueNode.setPrefSize(48, 48);

            boolean ocupado = disco[i];

            // Colores suaves (Tailwind Red 400 y Green 400)
            String estilo = ocupado
                    ? "-fx-background-color: #f87171; -fx-background-radius: 6px; -fx-border-color: #ef4444; -fx-border-radius: 6px;"
                    : "-fx-background-color: #4ade80; -fx-background-radius: 6px; -fx-border-color: #22c55e; -fx-border-radius: 6px;";

            bloqueNode.setStyle(estilo);

            Label lblIndex = new Label("B-" + i);
            lblIndex.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
            bloqueNode.getChildren().add(lblIndex);

            gridDisco.getChildren().add(bloqueNode);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}