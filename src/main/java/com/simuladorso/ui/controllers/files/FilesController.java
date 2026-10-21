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

    @FXML private TextField txtNombre;
    @FXML private Spinner<Integer> spnBloques;
    @FXML private Label lblEspacioLibre;
    @FXML private FlowPane gridDisco;

    // Tabla
    @FXML private TableView<INodo> tblINodos;
    @FXML private TableColumn<INodo, Number> colId;
    @FXML private TableColumn<INodo, String> colNombre;
    @FXML private TableColumn<INodo, Number> colTamano;

    // Metadatos (Mitad derecha)
    @FXML private Label lblMetaId;
    @FXML private Label lblMetaNombre;
    @FXML private Label lblMetaFecha;
    @FXML private Label lblMetaBloques;
    @FXML private Label lblMetaProp;
    @FXML private Label lblMetaPerm;
    @FXML private TextArea txtMetaRuta;

    private final FileSystemManager fileSystem = new FileSystemManager();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreArchivo()));
        colTamano.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getTamanoBloques()));

        // Escuchador de selección para mostrar metadatos al hacer clic en un archivo
        tblINodos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            mostrarMetadatos(newSelection);
        });

        actualizarVista();
    }

    @FXML
    private void handleCrearArchivo() {
        String nombre = txtNombre.getText().trim();
        int bloques = spnBloques.getValue();

        if (nombre.isEmpty()) {
            mostrarAlerta("Atención", "Ingrese un nombre válido para el archivo.");
            return;
        }

        if (!fileSystem.crearArchivo(nombre, bloques)) {
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
            mostrarMetadatos(null); // Limpiar panel derecho
            actualizarVista();
        }
    }

    private void mostrarMetadatos(INodo inodo) {
        if (inodo != null) {
            lblMetaId.setText(String.valueOf(inodo.getId()));
            lblMetaNombre.setText(inodo.getNombreArchivo());
            lblMetaFecha.setText(inodo.getFechaCreacion());
            lblMetaBloques.setText(inodo.getBloquesAsignados().toString());
            lblMetaProp.setText(inodo.getPropietario());
            lblMetaPerm.setText(inodo.getPermisos());
            txtMetaRuta.setText(inodo.getRutaFisica());
        } else {
            lblMetaId.setText("-");
            lblMetaNombre.setText("-");
            lblMetaFecha.setText("-");
            lblMetaBloques.setText("-");
            lblMetaProp.setText("-");
            lblMetaPerm.setText("-");
            txtMetaRuta.clear();
        }
    }

    private void actualizarVista() {
        tblINodos.getItems().setAll(fileSystem.getTodosLosArchivos());
        lblEspacioLibre.setText("Bloques Libres: " + fileSystem.getBloquesLibres() + " / " + fileSystem.getTotalBloques());

        gridDisco.getChildren().clear();
        boolean[] disco = fileSystem.getEstadoDisco();

        for (int i = 0; i < disco.length; i++) {
            StackPane bloqueNode = new StackPane();
            bloqueNode.setPrefSize(48, 48);

            boolean ocupado = disco[i];
            String estilo = ocupado
                    ? "-fx-background-color: #ef4444; -fx-background-radius: 4px;"
                    : "-fx-background-color: #22c55e; -fx-background-radius: 4px;";

            bloqueNode.setStyle(estilo);

            Label lblIndex = new Label("B-" + i);
            lblIndex.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
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