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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
public class DevicesController {
    @FXML
    private TableView<DispositivoEntradaSalida> tblDevices;
    @FXML
    private TableColumn<DispositivoEntradaSalida, String> colNombre;
    @FXML
    private TableColumn<DispositivoEntradaSalida, String> colConexion;
    @FXML
    private TableColumn<DispositivoEntradaSalida, String> colDriver;
    @FXML
    private Label lblStatus;
    private final DeviceScannerService scannerService =
            new DeviceScannerService();
    private ScheduledExecutorService executorService;
    private String ultimaFirma = "";
    private final AtomicBoolean monitoreoActivo =
            new AtomicBoolean(false);
    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getNombre()));
        colConexion.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTipoConexion()));
        colDriver.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDriver()));
        iniciarMonitoreoEnTiempoReal();
    }
    private void iniciarMonitoreoEnTiempoReal() {
        if (!monitoreoActivo.compareAndSet(false, true)) {
            return;
        }
        lblStatus.setText(
                "Buscando teclados, ratones, monitores y dispositivos USB...");
        executorService =
                Executors.newSingleThreadScheduledExecutor(runnable -> {
                    Thread hilo = new Thread(
                            runnable, "DeviceScanner-Thread");
                    hilo.setDaemon(true);
                    return hilo;
                });
        executorService.scheduleWithFixedDelay(() -> {
            if (!monitoreoActivo.get()) {
                return;
            }
            try {
                List<DispositivoEntradaSalida> encontrados =
                        scannerService.escanearDispositivosReales();
                encontrados.sort(
                        Comparator.comparing(
                                d -> textoSeguro(d.getNombre()),
                                String.CASE_INSENSITIVE_ORDER));
                String firma = construirFirma(encontrados);
                if (!firma.equals(ultimaFirma)) {
                    ultimaFirma = firma;
                    List<DispositivoEntradaSalida> copia =
                            new ArrayList<>(encontrados);
                    Platform.runLater(() -> {
                        if (!monitoreoActivo.get()) {
                            return;
                        }
                        tblDevices.getItems().setAll(copia);
                        lblStatus.setText(
                                "Dispositivos relevantes detectados: "
                                        + copia.size()
                                        + " | Actualización automática activa.");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (monitoreoActivo.get()) {
                        lblStatus.setText(
                                "No se pudo consultar el hardware. "
                                        + "Se reintentará automáticamente.");
                    }
                });
                System.err.println(
                        "Error al detectar dispositivos: "
                                + e.getMessage());
            }
        }, 0, 3, TimeUnit.SECONDS);
    }
    private String construirFirma(
            List<DispositivoEntradaSalida> dispositivos) {
        return dispositivos.stream()
                .map(d -> textoSeguro(d.getNombre())
                        + "|" + textoSeguro(d.getTipoConexion())
                        + "|" + textoSeguro(d.getDriver()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining("\n"));
    }
    private String textoSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }
    /**
     * Llamar cuando el usuario salga de la vista de dispositivos.
     */
    public void detenerMonitoreo() {
        monitoreoActivo.set(false);
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }
}