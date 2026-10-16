package com.simuladorso.ui.controllers.devices;

import com.simuladorso.devices.model.DispositivoEntradaSalida;
import com.simuladorso.devices.model.WindowsDriverInfo;
import com.simuladorso.devices.services.DeviceScannerService;
import com.simuladorso.devices.services.WindowsDriverInfoService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
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

    @FXML
    private Label lblNombreDetalle;

    @FXML
    private Label lblFabricante;

    @FXML
    private Label lblIdHardware;

    @FXML
    private Label lblEstado;

    @FXML
    private Label lblConexionDetalle;

    @FXML
    private Label lblCantidadInterfaces;

    @FXML
    private TextArea txtHistorial;

    private final DeviceScannerService scannerService =
            new DeviceScannerService();

    @FXML
    private Label lblDriverNombre;
    @FXML
    private Label lblDriverProveedor;
    @FXML
    private Label lblDriverVersion;
    @FXML
    private Label lblDriverFecha;

    private final WindowsDriverInfoService driverInfoService = new WindowsDriverInfoService();
    private final ExecutorService driverExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "WindowsDriverInfo-Thread");
        t.setDaemon(true);
        return t;
    });
    private long solicitudDriver = 0;
    private String idDriverConsultado = null;

    private ScheduledExecutorService executorService;

    private final AtomicBoolean monitoreoActivo =
            new AtomicBoolean(false);

    private final Map<String, DispositivoEntradaSalida>
            dispositivosAnteriores = new HashMap<>();

    private boolean primerEscaneo = true;

    private String ultimaFirma = "";

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm:ss"
            );

    @FXML
    public void initialize() {

        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getNombre()
                )
        );

        colConexion.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTipoConexion()
                )
        );

        colDriver.setText("Estado");

        colDriver.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getEstado()
                )
        );

        tblDevices.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, actual) -> {

                    if (actual == null) {
                        limpiarDetalles();
                    } else {
                        mostrarDetalles(actual);
                        solicitarDriver(actual);
                    }
                });

        limpiarDetalles();
        iniciarMonitoreoEnTiempoReal();
    }

    private void mostrarDetalles(
            DispositivoEntradaSalida dispositivo) {

        lblNombreDetalle.setText(
                valor(dispositivo.getNombre())
        );

        lblFabricante.setText(
                obtenerVidPid(dispositivo.getId())
        );

        lblIdHardware.setText(
                valor(dispositivo.getId())
        );

        lblEstado.setText(
                valor(dispositivo.getEstado())
        );

        lblConexionDetalle.setText(
                valor(dispositivo.getTipoConexion())
        );

        lblCantidadInterfaces.setText(
                descripcionInterfaces(
                        dispositivo.getCantidadInterfaces()
                )
        );
    }


    private String obtenerVidPid(String id) {

        if (id == null || !id.startsWith("USB:")) {
            return "No disponible";
        }

        String[] partes = id.split(":");

        if (partes.length < 3) {
            return "No disponible";
        }

        String vid = partes[1].replace("0x", "")
                .toUpperCase(java.util.Locale.ROOT);

        String pid = partes[2].replace("0x", "")
                .toUpperCase(java.util.Locale.ROOT);

        return "VID: " + vid + " | PID: " + pid;
    }


    private void limpiarDetalles() {

        lblNombreDetalle.setText("No seleccionado");
        lblFabricante.setText("-");
        lblIdHardware.setText("-");
        lblEstado.setText("-");
        lblConexionDetalle.setText("-");
        lblCantidadInterfaces.setText("-");
        solicitudDriver++;
        idDriverConsultado = null;
        limpiarDriver("-");
    }

    private void limpiarDriver(String mensaje) {
        lblDriverNombre.setText(mensaje);
        lblDriverProveedor.setText(mensaje);
        lblDriverVersion.setText(mensaje);
        lblDriverFecha.setText(mensaje);
    }

    private void solicitarDriver(DispositivoEntradaSalida dispositivo) {
        if (dispositivo.getId().equals(idDriverConsultado)) return;
        idDriverConsultado = dispositivo.getId();
        long solicitud = ++solicitudDriver;
        String id = dispositivo.getId();
        String nombre = dispositivo.getNombre();
        limpiarDriver("Consultando Windows...");
        driverExecutor.submit(() -> {
            Optional<WindowsDriverInfo> info = driverInfoService.consultar(id, nombre);
            Platform.runLater(() -> {
                if (solicitud != solicitudDriver || !id.equals(idDriverConsultado)) return;
                DispositivoEntradaSalida seleccionado =
                        tblDevices.getSelectionModel().getSelectedItem();
                if (seleccionado == null || !id.equals(seleccionado.getId())) return;
                if (info.isEmpty()) {
                    limpiarDriver("No disponible");
                    return;
                }
                WindowsDriverInfo d = info.get();
                lblDriverNombre.setText(d.getNombre());
                lblDriverProveedor.setText(d.getProveedor());
                lblDriverVersion.setText(d.getVersion());
                lblDriverFecha.setText(d.getFecha());
            });
        });
    }

    private String valor(String texto) {

        return texto == null || texto.isBlank()
                ? "No disponible"
                : texto;
    }

    private String descripcionInterfaces(int cantidad) {

        if (cantidad == 1) {
            return "1 registro USB";
        }

        return cantidad + " registros USB/HID asociados";
    }

    private void detectarCambios(
            List<DispositivoEntradaSalida> actuales) {

        Map<String, DispositivoEntradaSalida> nuevos =
                new HashMap<>();

        for (DispositivoEntradaSalida dispositivo : actuales) {

            nuevos.put(
                    dispositivo.getId(),
                    dispositivo
            );
        }

        if (primerEscaneo) {

            dispositivosAnteriores.clear();
            dispositivosAnteriores.putAll(nuevos);

            primerEscaneo = false;
            return;
        }

        for (DispositivoEntradaSalida dispositivo : actuales) {

            if (!dispositivosAnteriores.containsKey(
                    dispositivo.getId())) {

                registrarEvento(
                        "CONECTADO",
                        dispositivo
                );
            }
        }

        for (DispositivoEntradaSalida anterior :
                dispositivosAnteriores.values()) {

            if (!nuevos.containsKey(anterior.getId())) {

                registrarEvento(
                        "DESCONECTADO",
                        anterior
                );
            }
        }

        dispositivosAnteriores.clear();
        dispositivosAnteriores.putAll(nuevos);
    }


    private void registrarEvento(
            String tipo,
            DispositivoEntradaSalida dispositivo) {

        String hora = LocalDateTime.now()
                .format(formatoHora);

        String mensaje =
                "[" + hora + "] "
                        + tipo + ": "
                        + dispositivo.getNombre()
                        + "\n";

        txtHistorial.appendText(mensaje);
    }


    private void iniciarMonitoreoEnTiempoReal() {

        if (!monitoreoActivo.compareAndSet(
                false, true)) {
            return;
        }

        lblStatus.setText(
                "Buscando dispositivos USB..."
        );

        executorService =
                Executors.newSingleThreadScheduledExecutor(
                        runnable -> {

                            Thread hilo = new Thread(
                                    runnable,
                                    "DeviceScanner-Thread"
                            );

                            hilo.setDaemon(true);
                            return hilo;
                        }
                );

        executorService.scheduleWithFixedDelay(() -> {

            if (!monitoreoActivo.get()) {
                return;
            }

            try {

                List<DispositivoEntradaSalida> encontrados =
                        scannerService.escanearDispositivosReales();

                encontrados.sort(
                        Comparator.comparing(
                                DispositivoEntradaSalida::getNombre,
                                String.CASE_INSENSITIVE_ORDER
                        )
                );

                String firma = construirFirma(encontrados);

                if (firma.equals(ultimaFirma)) {
                    return;
                }

                List<DispositivoEntradaSalida> copia =
                        new ArrayList<>(encontrados);

                Platform.runLater(() -> {

                    if (!monitoreoActivo.get()) {
                        return;
                    }

                    if (firma.equals(ultimaFirma)) {
                        return;
                    }

                    ultimaFirma = firma;

                    detectarCambios(copia);

                    DispositivoEntradaSalida seleccionado =
                            tblDevices.getSelectionModel()
                                    .getSelectedItem();

                    String idSeleccionado =
                            seleccionado == null
                                    ? null
                                    : seleccionado.getId();

                    tblDevices.getItems().setAll(copia);

                    boolean seleccionadoEncontrado = false;

                    if (idSeleccionado != null) {

                        for (DispositivoEntradaSalida dispositivo
                                : copia) {

                            if (idSeleccionado.equals(
                                    dispositivo.getId())) {

                                tblDevices.getSelectionModel()
                                        .select(dispositivo);

                                mostrarDetalles(dispositivo);

                                seleccionadoEncontrado = true;
                                break;
                            }
                        }
                    }

                    if (!seleccionadoEncontrado) {

                        tblDevices.getSelectionModel()
                                .clearSelection();

                        limpiarDetalles();
                    }

                    lblStatus.setText(
                            "Dispositivos USB detectados: "
                                    + copia.size()
                                    + " | Monitoreo activo."
                    );
                });

            } catch (Exception e) {

                Platform.runLater(() -> {

                    if (monitoreoActivo.get()) {

                        lblStatus.setText(
                                "No se pudieron consultar los "
                                        + "dispositivos USB. "
                                        + "Se reintentará automáticamente."
                        );
                    }
                });
            }

        }, 0, 3, TimeUnit.SECONDS);
    }

    private String construirFirma(
            List<DispositivoEntradaSalida> dispositivos) {

        return dispositivos.stream()
                .map(d ->
                        textoSeguro(d.getId())
                                + "|"
                                + textoSeguro(d.getNombre())
                                + "|"
                                + textoSeguro(d.getTipoConexion())
                                + "|"
                                + textoSeguro(d.getFabricante())
                                + "|"
                                + textoSeguro(d.getEstado())
                                + "|"
                                + d.getCantidadInterfaces()
                )
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining("\n"));
    }

    private String textoSeguro(String texto) {

        return texto == null
                ? ""
                : texto.trim();
    }

    public void detenerMonitoreo() {

        monitoreoActivo.set(false);
        solicitudDriver++;
        driverExecutor.shutdownNow();

        if (executorService != null) {

            executorService.shutdownNow();
            executorService = null;
        }
    }
}
