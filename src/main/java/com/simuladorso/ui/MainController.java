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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

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
    @FXML private Pane processesPage;
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

        // =========================================================
        // Reloj del simulador
        // =========================================================
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (kernel.estaActivo()) {
                reloj.avanzar();
                actualizarReloj();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);


        // =========================================================
        // Reloj real de la computadora
        // =========================================================
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
        if (!kernel.estaActivo()) {
            registrarEvento("El sistema ya se encuentra detenido.");
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
        reloj.avanzar();
        actualizarReloj();
        registrarEvento("Reloj avanzado manualmente 1 segundo.");
    }

    @FXML private void showDashboard() { mostrarPagina(dashboardPage, navDashboard); }
    @FXML private void showProcesses() { mostrarPagina(processesPage, navProcesses); }
    @FXML private void showMemory() { mostrarPagina(memoryPage, navMemory); }
    @FXML private void showDevices() { mostrarPagina(devicesPage, navDevices); }
    @FXML private void showFiles() { mostrarPagina(filesPage, navFiles); }
    @FXML private void showLogs() { mostrarPagina(logsPage, navLogs); }

    private void mostrarPagina(Pane pagina, Button boton) {
        Pane[] paginas = {dashboardPage, processesPage, memoryPage, devicesPage, filesPage, logsPage};
        Button[] botones = {navDashboard, navProcesses, navMemory, navDevices, navFiles, navLogs};

        for (Pane p : paginas) {
            p.setVisible(p == pagina);
            p.setManaged(p == pagina);
        }
        for (Button b : botones) {
            b.getStyleClass().remove("nav-active");
        }
        if (!boton.getStyleClass().contains("nav-active")) {
            boton.getStyleClass().add("nav-active");
        }
    }

    private void actualizarInterfaz() {
        actualizarReloj();
        lblKernelStatus.setText(kernel.estaActivo() ? "Inicializado" : "Detenido");
        lblKernelStatus.getStyleClass().removeAll("status-on", "status-off");
        lblKernelStatus.getStyleClass().add(kernel.estaActivo() ? "status-on" : "status-off");

        lblCpuStatus.setText(cpu.getEstado() == CPU.EstadoCPU.LIBRE ? "Libre" : "Detenida");
        lblMemoryTotal.setText(memoria.getTotalMB() + " MB");
        lblMemoryUsed.setText(memoria.getUsadaMB() + " MB");
        lblMemoryAvailable.setText(memoria.getDisponibleMB() + " MB");
        memoryProgress.setProgress(memoria.getPorcentajeUso());
    }

    private void actualizarReloj() {
        lblClock.setText(reloj.formatear());
    }

    private void actualizarHoraReal() {
        lblRealTime.setText(
                LocalTime.now().format(realTimeFormatter)
        );
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
