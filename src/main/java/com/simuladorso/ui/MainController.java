package com.simuladorso.ui;

import com.simuladorso.core.CPU;
import com.simuladorso.core.Kernel;
import com.simuladorso.core.RelojSistema;
import com.simuladorso.devices.Dispositivo;
import com.simuladorso.filesystem.SistemaArchivos;
import com.simuladorso.log.RegistroEventos;
import com.simuladorso.memory.Memoria;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainController {

    @FXML private Label lblClock;
    @FXML private Label lblRealTime;
    @FXML private Label lblKernelStatus;
    @FXML private Label lblCpuStatus;
    @FXML private Label lblMemoryTotal;
    @FXML private Label lblMemoryUsed;
    @FXML private Label lblMemoryAvailable;
    @FXML private ProgressBar memoryProgress;
    @FXML private TextArea dashboardLog;
    @FXML private TextArea fullLog;

    @FXML private Pane dashboardPage;
//    @FXML private Pane processesPage;
    @FXML private StackPane contentArea;
    @FXML private StackPane centerStackPane; // Referencia al contenedor principal

    @FXML private Pane memoryPage;
    @FXML private Pane devicesPage;
    @FXML private Pane filesPage;
    @FXML private Pane logsPage;

    @FXML private Button navDashboard;
    @FXML private Button navProcesses;
    @FXML private Button navMemory;
    @FXML private Button navDevices;
    @FXML private Button navFiles;
    @FXML private Button navLogs;

    @FXML private Button btnAdvanceClock;
    @FXML private Button btnStart;
    @FXML private Button btnStop;
    @FXML private Button btnRestart;

    private final Kernel kernel = new Kernel();
    private final CPU cpu = new CPU();
    private final RelojSistema reloj = new RelojSistema();
    private final Memoria memoria = new Memoria(4096);
    private final RegistroEventos registro = new RegistroEventos();
    private final SistemaArchivos sistemaArchivos = new SistemaArchivos();
    private final List<Dispositivo> dispositivos = List.of(
            new Dispositivo("Teclado", "Entrada", true),
            new Dispositivo("Mouse", "Entrada", true),
            new Dispositivo("Disco", "Almacenamiento", true),
            new Dispositivo("Impresora", "Salida", false)
    );

    private Timeline timeline;
    private Timeline realTimeTimeline;

    private final DateTimeFormatter realTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {

        // Reloj del simulador
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (kernel.estaActivo()) {
                reloj.avanzar();
                actualizarReloj();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);

        // Reloj real del sistema
        realTimeTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            actualizarHoraReal();
        }));
        realTimeTimeline.setCycleCount(Timeline.INDEFINITE);
        realTimeTimeline.play();

        mostrarPagina(dashboardPage, navDashboard);
        actualizarInterfaz();
        actualizarHoraReal();

        registrarEvento("Simulador cargado. Núcleo base preparado.");
    }

    @FXML
    private void handleStart() {
        if (kernel.estaActivo()) {
            registrarEvento("El Kernel ya se encuentra inicializado.");
            return;
        }

        kernel.iniciar();
        cpu.iniciar();
        timeline.play();

        registrarEvento("Kernel inicializado.");
        registrarEvento("CPU disponible.");
        registrarEvento("Memoria detectada: " + memoria.getTotalMB() + " MB.");
        registrarEvento("Gestor de procesos preparado.");
        registrarEvento("Sistema de archivos base cargado.");
        registrarEvento("Dispositivos de E/S verificados.");
        actualizarInterfaz();
    }

    @FXML
    private void handleStop() {
        if (!validarKernelActivo("detener el sistema")) {
            return;
        }

        kernel.detener();
        cpu.detener();
        timeline.pause();
        registrarEvento("Sistema detenido.");
        actualizarInterfaz();
    }

    @FXML
    private void handleRestart() {

        if (!kernel.estaActivo()) {
            return;
        }

        timeline.stop();
        kernel.detener();
        cpu.detener();
        reloj.reiniciar();
        memoria.reiniciar();
        registro.limpiar();
        dashboardLog.clear();
        fullLog.clear();

        kernel.iniciar();
        cpu.iniciar();
        timeline.playFromStart();
        registrarEvento("Sistema reiniciado.");
        registrarEvento("Kernel inicializado.");
        actualizarInterfaz();
    }

    @FXML
    private void handleAdvanceClock() {
        // VALIDACIÓN: No se puede avanzar el reloj si el Kernel no está activo
        if (!kernel.estaActivo()) {
            registrarEvento("ADVERTENCIA: No se puede avanzar el reloj con el Kernel detenido.");
            return;
        }

        reloj.avanzar();
        actualizarReloj();
        registrarEvento("Reloj avanzado manualmente 1 segundo.");
    }

    @FXML private void showDashboard() { mostrarPagina(dashboardPage, navDashboard); }

//    @FXML private void showProcesses() {
//        if (validarKernelActivo("acceder a la gestión de procesos")) {
//            mostrarPagina(processesPage, navProcesses);
//        }
//    }

    @FXML
    private void showProcesses() {
        if (!validarKernelActivo("acceder a la gestión de procesos")) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/processes-view.fxml"));
            VBox processesView = loader.load();

            // Ocultamos las páginas estáticas y cargamos la vista externa en el StackPane
            ocultarPaginasEstaticas();
            centerStackPane.getChildren().add(processesView);

            // Marcamos el botón en la barra lateral
            marcarBotonActivo(navProcesses);

        } catch (IOException e) {
            e.printStackTrace();
            registrarEvento("ERROR: No se pudo cargar el archivo processes-view.fxml.");
        }
    }


    @FXML private void showMemory() {
        if (validarKernelActivo("inspeccionar el estado de la memoria")) {
            mostrarPagina(memoryPage, navMemory);
        }
    }
    @FXML private void showDevices() {
        if (validarKernelActivo("consultar los dispositivos E/S")) {
            mostrarPagina(devicesPage, navDevices);
        }
    }
    @FXML private void showFiles() {
        if (validarKernelActivo("explorar el sistema de archivos")) {
            mostrarPagina(filesPage, navFiles);
        }
    }
    @FXML private void showLogs() { mostrarPagina(logsPage, navLogs); }

    /**
     * Valida si el kernel está encendido antes de realizar una acción de hardware o módulo.
     */
    private boolean validarKernelActivo(String accion) {
        if (!kernel.estaActivo()) {
            registrarEvento("DENEGADO: Debe iniciar el Kernel para " + accion + ".");
            return false;
        }
        return true;
    }



    private void mostrarPagina(Pane pagina, Button boton) {
        // Si traíamos una vista dinámica (como processesView) pegada en centerStackPane, la removemos
        centerStackPane.getChildren().removeIf(node -> node != dashboardPage
                && node != memoryPage
                && node != devicesPage
                && node != filesPage
                && node != logsPage);

        Pane[] paginas = {dashboardPage, memoryPage, devicesPage, filesPage, logsPage};

        for (Pane p : paginas) {
            boolean esLaPagina = (p == pagina);
            p.setVisible(esLaPagina);
            p.setManaged(esLaPagina);
        }

        marcarBotonActivo(boton);
    }

    private void ocultarPaginasEstaticas() {
        Pane[] paginas = {dashboardPage, memoryPage, devicesPage, filesPage, logsPage};
        for (Pane p : paginas) {
            p.setVisible(false);
            p.setManaged(false);
        }
    }

    private void marcarBotonActivo(Button boton) {
        Button[] botones = {navDashboard, navProcesses, navMemory, navDevices, navFiles, navLogs};
        for (Button b : botones) {
            b.getStyleClass().remove("nav-active");
        }
        if (boton != null && !boton.getStyleClass().contains("nav-active")) {
            boton.getStyleClass().add("nav-active");
        }
    }

//    private void mostrarPagina(Pane pagina, Button boton) {
//        Pane[] paginas = {dashboardPage, memoryPage, devicesPage, filesPage, logsPage};
//        Button[] botones = {navDashboard, navProcesses, navMemory, navDevices, navFiles, navLogs};
//
//        for (Pane p : paginas) {
//            p.setVisible(p == pagina);
//            p.setManaged(p == pagina);
//        }
//        for (Button b : botones) {
//            b.getStyleClass().remove("nav-active");
//        }
//        if (!boton.getStyleClass().contains("nav-active")) {
//            boton.getStyleClass().add("nav-active");
//        }
//    }

    private void actualizarInterfaz() {
        actualizarReloj();
        boolean activo = kernel.estaActivo();

        lblKernelStatus.setText(activo ? "Inicializado" : "Detenido");
        lblKernelStatus.getStyleClass().removeAll("status-on", "status-off");
        lblKernelStatus.getStyleClass().add(activo ? "status-on" : "status-off");

        lblCpuStatus.setText(cpu.getEstado() == CPU.EstadoCPU.LIBRE ? "Libre" : "Detenida");
        lblMemoryTotal.setText(memoria.getTotalMB() + " MB");
        lblMemoryUsed.setText(memoria.getUsadaMB() + " MB");
        lblMemoryAvailable.setText(memoria.getDisponibleMB() + " MB");
        memoryProgress.setProgress(memoria.getPorcentajeUso());

        actualizarEstadoBotones(activo);
    }

    /**
     * Habilita o deshabilita la interacción con ciertos controles según el estado del Kernel.
     */
    private void actualizarEstadoBotones(boolean activo) {
        if (btnStart != null) btnStart.setDisable(activo);     // Se deshabilita si YA está encendido
        if (btnStop != null) btnStop.setDisable(!activo);      // Se deshabilita si está apagado
        if (btnAdvanceClock != null) btnAdvanceClock.setDisable(!activo);

        // El Dashboard y los Logs se mantienen accesibles para ver el historial,
        // pero los módulos de hardware/procesos quedan restringidos si la simulación está apagada.
        navProcesses.setDisable(!activo);
        navMemory.setDisable(!activo);
        navDevices.setDisable(!activo);
        navFiles.setDisable(!activo);
    }

    private void actualizarReloj() {
        lblClock.setText(reloj.formatear());
    }

    private void actualizarHoraReal() {
        lblRealTime.setText(LocalTime.now().format(realTimeFormatter));
    }

    private void registrarEvento(String mensaje) {
        String linea = "[" + reloj.formatear() + "] " + mensaje;
        registro.registrar(linea);
        String texto = String.join("\n", registro.getEventos());
        dashboardLog.setText(texto);
        fullLog.setText(texto);
        dashboardLog.positionCaret(dashboardLog.getLength());
        fullLog.positionCaret(fullLog.getLength());
    }
}