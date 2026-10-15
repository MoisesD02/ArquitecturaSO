package com.simuladorso.ui.controllers.devices;

import com.simuladorso.devices.model.DispositivoEntradaSalida;
import com.simuladorso.devices.services.DeviceScannerService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DevicesController {

    @FXML private TableView<DispositivoEntradaSalida> tblDevices;
    @FXML private TableColumn<DispositivoEntradaSalida, String> colNombre;
    @FXML private TableColumn<DispositivoEntradaSalida, String> colConexion;
    @FXML private TableColumn<DispositivoEntradaSalida, String> colDriver;
    @FXML private Label lblStatus;

    private final DeviceScannerService scannerService = new DeviceScannerService();
    private ScheduledExecutorService executorService;
    private List<DispositivoEntradaSalida> ultimaListaConocida = new ArrayList<>();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colConexion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoConexion()));
        colDriver.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDriver()));

        iniciarMonitoreoEnTiempoReal();
    }

    private void iniciarMonitoreoEnTiempoReal() {
        lblStatus.setText("Monitoreo de hardware activado. Conecta o desconecta una USB...");

        // Creamos un hilo en segundo plano
        executorService = Executors.newSingleThreadScheduledExecutor();

        // Ejecutamos el escaneo cada 2 segundos
        executorService.scheduleAtFixedRate(() -> {
            try {
                List<DispositivoEntradaSalida> dispositivosActuales = scannerService.escanearDispositivosReales();

                // Si hay un cambio (se conectó o desconectó algo)
                if (!dispositivosActuales.equals(ultimaListaConocida)) {
                    ultimaListaConocida = dispositivosActuales;

                    // Actualizamos la UI en el hilo de JavaFX
                    Platform.runLater(() -> {
                        tblDevices.getItems().setAll(dispositivosActuales);
                        lblStatus.setText("Detectados " + dispositivosActuales.size() + " dispositivos de E/S en tiempo real.");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    // Es importante apagar el hilo cuando se cierre la ventana o se cambie de vista
    public void detenerMonitoreo() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}