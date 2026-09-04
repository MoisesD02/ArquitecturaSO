package com.simuladorso.ui.controllers;

import com.simuladorso.process.Algoritmos.FCFS;
import com.simuladorso.process.Algoritmos.PlanDosNiveles;
import com.simuladorso.process.Algoritmos.PlanGarantizada;
import com.simuladorso.process.Algoritmos.SJF;
import com.simuladorso.process.EstadoProceso;
import com.simuladorso.process.Proceso;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ProcessesController {


    // ==============================================
    // NOMBRES DE ALGORITMOS
    // ==============================================

    private static final String FCFS_NOMBRE =
            "FCFS";

    private static final String SJF =
            "SJF";

    private static final String ROUND_ROBIN =
            "Round Robin";

    private static final String PRIORIDAD =
            "Por Prioridad";

    private static final String COLAS_MULTIPLES =
            "Colas Múltiples";

    private static final String GARANTIZADA =
            "Planificación Garantizada";

    private static final String DOS_NIVELES =
            "Planificación a Dos Niveles";


    // ==============================================
    // DATOS
    // ==============================================

    private final ObservableList<Proceso> procesos =
            FXCollections.observableArrayList();


    private final List<Proceso> procesosSimulacionActual =
            new ArrayList<>();

    private int siguientePid = 1;


    // ==============================================
    // FCFS
    // ==============================================

    private final FCFS fcfs =
            new FCFS();


    private FCFS.Resultado resultadoFCFS;



    private final SJF sjf =
            new SJF();



    private SJF.Resultado resultadoSJF;


    private final PlanDosNiveles planDosNiveles =
            new PlanDosNiveles();


    private final PlanGarantizada planGarantizada =
            new PlanGarantizada();


    private int tiempoSimulacion = 0;


    private Timeline timeline;


    private boolean simulacionIniciada = false;


    // ==============================================
    // SELECTOR
    // ==============================================

    @FXML
    private ComboBox<String> cmbAlgorithm;


    @FXML
    private HBox quantumContainer;


    @FXML
    private TextField txtQuantum;


    // ==============================================
    // FORMULARIO
    // ==============================================

    @FXML
    private TextField txtProcessName;


    @FXML
    private TextField txtArrivalTime;


    @FXML
    private TextField txtBurstTime;


    @FXML
    private VBox priorityContainer;


    @FXML
    private ComboBox<Integer> cmbPriority;


    @FXML
    private VBox queueContainer;


    @FXML
    private ComboBox<String> cmbQueue;


    @FXML
    private VBox memoryContainer;


    @FXML
    private TextField txtMemory;


    @FXML
    private Label lblFormMessage;


    // ==============================================
    // TABLA
    // ==============================================

    @FXML
    private TableView<Proceso> tblProcesses;


    @FXML
    private TableColumn<Proceso, String> colPid;


    @FXML
    private TableColumn<Proceso, String> colName;


    @FXML
    private TableColumn<Proceso, Integer> colArrival;


    @FXML
    private TableColumn<Proceso, Integer> colBurst;


    @FXML
    private TableColumn<Proceso, Integer> colRemaining;


    @FXML
    private TableColumn<Proceso, Integer> colPriority;


    @FXML
    private TableColumn<Proceso, String> colQueue;


    @FXML
    private TableColumn<Proceso, String> colMemory;


    @FXML
    private TableColumn<Proceso, String> colState;


    // ==============================================
    // COLA
    // ==============================================

    @FXML
    private HBox readyQueueContainer;


    // ==============================================
    // BOTONES
    // ==============================================

    @FXML
    private Button btnExecute;


    @FXML
    private Button btnStep;


    // ==============================================
    // CPU
    // ==============================================

    @FXML
    private Label lblSimulationTime;


    @FXML
    private Label lblCpuState;


    @FXML
    private Label lblCurrentProcess;


    @FXML
    private Label lblRemainingTime;


    @FXML
    private ProgressBar cpuProgress;


    // ==============================================
    // GANTT
    // ==============================================

    @FXML
    private HBox ganttContainer;


    @FXML
    private Label lblGanttEmpty;


    // ==============================================
    // RESULTADOS
    // ==============================================

    @FXML
    private Label lblAverageWait;


    @FXML
    private Label lblAverageResponse;


    @FXML
    private Label lblExecutionTime;


    // ==============================================
    // INITIALIZE
    // ==============================================

    @FXML
    public void initialize() {

        configurarSelectorAlgoritmos();

        configurarCamposDinamicos();

        configurarTabla();


        tblProcesses.setItems(
                procesos
        );


        cmbAlgorithm
                .getSelectionModel()
                .select(FCFS_NOMBRE);


        actualizarInterfazSegunAlgoritmo();

        actualizarColaListos();

        actualizarControles();
    }


    // ==============================================
    // SELECTOR
    // ==============================================

    private void configurarSelectorAlgoritmos() {

        cmbAlgorithm.setItems(

                FXCollections.observableArrayList(

                        FCFS_NOMBRE,
                        SJF,
                        ROUND_ROBIN,
                        PRIORIDAD,
                        COLAS_MULTIPLES,
                        GARANTIZADA,
                        DOS_NIVELES
                )
        );


        cmbAlgorithm
                .valueProperty()
                .addListener(
                        (observable,
                         anterior,
                         actual) -> {


                            if (actual != null
                                    && !actual.equals(anterior)) {

                                reiniciarDatosSimulacion();

                                actualizarInterfazSegunAlgoritmo();
                            }
                        }
                );
    }


    private void configurarCamposDinamicos() {

        cmbPriority.setItems(

                FXCollections.observableArrayList(
                        1,
                        2,
                        3
                )
        );


        cmbPriority
                .getSelectionModel()
                .select(Integer.valueOf(1));


        cmbQueue.setItems(

                FXCollections.observableArrayList(

                        "Sistema",
                        "Interactivo",
                        "Segundo plano"
                )
        );


        cmbQueue
                .getSelectionModel()
                .selectFirst();
    }


    // ==============================================
    // TABLA
    // ==============================================

    private void configurarTabla() {

        tblProcesses.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );


        colPid.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                String.format(
                                        "P%03d",
                                        data.getValue()
                                                .getPid()
                                )
                        )
        );


        colName.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()
                                        .getNombre()
                        )
        );


        colArrival.setCellValueFactory(
                data ->
                        new SimpleIntegerProperty(
                                data.getValue()
                                        .getTiempoLlegada()
                        ).asObject()
        );


        colBurst.setCellValueFactory(
                data ->
                        new SimpleIntegerProperty(
                                data.getValue()
                                        .getRafagaCPU()
                        ).asObject()
        );


        colRemaining.setCellValueFactory(
                data ->
                        new SimpleIntegerProperty(
                                data.getValue()
                                        .getTiempoRestante()
                        ).asObject()
        );


        colPriority.setCellValueFactory(
                data ->
                        new SimpleIntegerProperty(
                                data.getValue()
                                        .getPrioridad()
                        ).asObject()
        );


        colQueue.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()
                                        .getCola()
                        )
        );


        colMemory.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()
                                        .getMemoriaMB()
                                        + " MB"
                        )
        );


        colState.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()
                                        .getEstado()
                                        .name()
                        )
        );
    }


    // ==============================================
    // INTERFAZ DINÁMICA
    // ==============================================

    private void actualizarInterfazSegunAlgoritmo() {

        String algoritmo =
                cmbAlgorithm.getValue();


        mostrarNodo(
                priorityContainer,
                PRIORIDAD.equals(algoritmo)
        );


        mostrarNodo(
                queueContainer,
                COLAS_MULTIPLES.equals(algoritmo)
        );


        mostrarNodo(
                memoryContainer,
                DOS_NIVELES.equals(algoritmo)
        );


        mostrarNodo(
                quantumContainer,
                ROUND_ROBIN.equals(algoritmo)
        );


        colRemaining.setVisible(
                ROUND_ROBIN.equals(algoritmo)
        );


        colPriority.setVisible(
                PRIORIDAD.equals(algoritmo)
        );


        colQueue.setVisible(
                COLAS_MULTIPLES.equals(algoritmo)
        );


        colMemory.setVisible(
                DOS_NIVELES.equals(algoritmo)
        );


        limpiarFormulario();
    }


    private void mostrarNodo(
            Node nodo,
            boolean mostrar) {

        nodo.setVisible(mostrar);

        nodo.setManaged(mostrar);
    }


    // ==============================================
    // AGREGAR PROCESO
    // ==============================================

    @FXML
    private void handleAddProcess() {

        lblFormMessage.setText("");


        if (simulacionIniciada) {

            mostrarError(
                    "Reinicie la simulación para agregar nuevos procesos."
            );

            return;
        }


        try {

            String nombre =
                    txtProcessName
                            .getText()
                            .trim();


            if (nombre.isEmpty()) {

                mostrarError(
                        "Ingrese el nombre del proceso."
                );

                return;
            }


            int llegada =
                    leerEnteroNoNegativo(
                            txtArrivalTime,
                            "tiempo de llegada"
                    );


            int rafaga =
                    leerEnteroPositivo(
                            txtBurstTime,
                            "ráfaga CPU"
                    );


            String algoritmo =
                    cmbAlgorithm.getValue();


            int prioridad = 0;

            String cola = "";

            int memoria = 0;


            if (PRIORIDAD.equals(algoritmo)) {

                prioridad =
                        cmbPriority.getValue();
            }


            if (COLAS_MULTIPLES.equals(algoritmo)) {

                cola =
                        cmbQueue.getValue();
            }


            if (DOS_NIVELES.equals(algoritmo)) {

                memoria =
                        leerEnteroPositivo(
                                txtMemory,
                                "memoria requerida"
                        );
            }


            Proceso proceso =
                    new Proceso(

                            siguientePid++,

                            nombre,

                            llegada,

                            rafaga,

                            memoria,

                            prioridad,

                            cola
                    );


            if (llegada == 0) {

                proceso.setEstado(
                        EstadoProceso.LISTO
                );

            } else {

                proceso.setEstado(
                        EstadoProceso.NUEVO
                );
            }


            procesos.add(
                    proceso
            );


            ordenarTablaPorLlegada();


            actualizarColaListos();

            actualizarControles();

            limpiarFormulario();


        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    // ==============================================
    // CARGAR EJEMPLO
    // ==============================================

    @FXML
    private void handleLoadExample() {

        /*
         * Si la simulación ya comenzó,
         * no permitimos modificar la lista.
         */
        if (simulacionIniciada) {

            mostrarError(
                    "La simulación ya comenzó. "
                            + "Reiníciela para agregar nuevos procesos."
            );

            return;
        }


        String algoritmo =
                cmbAlgorithm.getValue();


        if (FCFS_NOMBRE.equals(algoritmo)) {

            agregarProcesoEjemplo(
                    0,
                    5
            );

            agregarProcesoEjemplo(
                    1,
                    3
            );

            agregarProcesoEjemplo(
                    2,
                    4
            );
        }

        if (SJF.equals(algoritmo)) {

            agregarProcesoEjemplo(
                    0,
                    5
            );

            agregarProcesoEjemplo(
                    1,
                    7
            );

            agregarProcesoEjemplo(
                    2,
                    2
            );
        }

        if (GARANTIZADA.equals(algoritmo)) {

            agregarProcesoEjemplo(
                    "Render",
                    0,
                    5,
                    0
            );

            agregarProcesoEjemplo(
                    "Audio",
                    0,
                    3,
                    0
            );

            agregarProcesoEjemplo(
                    "Red",
                    0,
                    4,
                    0
            );
        }

        if (DOS_NIVELES.equals(algoritmo)) {

            agregarProcesoEjemplo(
                    "Editor",
                    0,
                    3,
                    320
            );

            agregarProcesoEjemplo(
                    "Compilador",
                    1,
                    5,
                    520
            );

            agregarProcesoEjemplo(
                    "Navegador",
                    2,
                    4,
                    460
            );

            agregarProcesoEjemplo(
                    "BaseDatos",
                    3,
                    6,
                    700
            );

            agregarProcesoEjemplo(
                    "Backup",
                    4,
                    2,
                    300
            );
        }


        ordenarTablaPorLlegada();

        actualizarColaListos();

        actualizarControles();

        lblFormMessage.setText("");
    }


    private void agregarProcesoEjemplo(
            int llegada,
            int rafaga) {

        agregarProcesoEjemplo(
                "Proceso " + siguientePid,
                llegada,
                rafaga,
                0
        );
    }


    private void agregarProcesoEjemplo(
            String nombre,
            int llegada,
            int rafaga,
            int memoria) {

        int pid =
                siguientePid++;


        Proceso proceso =
                new Proceso(

                        pid,

                        nombre,

                        llegada,

                        rafaga,

                        memoria,

                        0,

                        ""
                );


        if (llegada == 0) {

            proceso.setEstado(
                    EstadoProceso.LISTO
            );

        } else {

            proceso.setEstado(
                    EstadoProceso.NUEVO
            );
        }


        procesos.add(
                proceso
        );
    }

    // ==============================================
    // EJECUTAR
    // ==============================================

    @FXML
    private void handleExecute() {

        String algoritmo =
                cmbAlgorithm.getValue();


        if (!FCFS_NOMBRE.equals(algoritmo)
                && !SJF.equals(algoritmo)
                && !GARANTIZADA.equals(algoritmo)
                && !DOS_NIVELES.equals(algoritmo)) {

            mostrarError(
                    "Este algoritmo todavía no está implementado."
            );

            return;
        }


        if (!hayProcesosPendientes()) {

            mostrarError(
                    "Agregue al menos un proceso pendiente."
            );

            return;
        }


        if (DOS_NIVELES.equals(algoritmo)) {

            ejecutarDosNiveles();

            return;
        }


        if (GARANTIZADA.equals(algoritmo)) {

            ejecutarGarantizada();

            return;
        }


        prepararPlanificacion();


        if (!simulacionIniciada) {
            return;
        }


        if (timeline != null) {

            timeline.stop();
        }


        btnExecute.setDisable(true);

        btnStep.setDisable(true);


        timeline =
                new Timeline(

                        new KeyFrame(

                                Duration.millis(700),

                                event -> {

                                    avanzarUnPasoPlanificacion();


                                    if (tiempoSimulacion
                                            >= obtenerTiempoTotal()) {

                                        timeline.stop();

                                        finalizarPlanificacion();
                                    }
                                }
                        )
                );


        timeline.setCycleCount(
                Timeline.INDEFINITE
        );


        timeline.play();
    }


    // ==============================================
    // PASO A PASO
    // ==============================================

    @FXML
    private void handleStep() {

        String algoritmo =
                cmbAlgorithm.getValue();


        if (!FCFS_NOMBRE.equals(algoritmo)
                && !SJF.equals(algoritmo)
                && !GARANTIZADA.equals(algoritmo)
                && !DOS_NIVELES.equals(algoritmo)) {

            mostrarError(
                    "Este algoritmo todavía no está implementado."
            );

            return;
        }


        if (!hayProcesosPendientes()) {

            mostrarError(
                    "Agregue al menos un proceso pendiente."
            );

            return;
        }


        if (DOS_NIVELES.equals(algoritmo)) {

            ejecutarDosNiveles();

            return;
        }


        if (GARANTIZADA.equals(algoritmo)) {

            ejecutarGarantizada();

            return;
        }


        prepararPlanificacion();


        if (!simulacionIniciada) {
            return;
        }


        if (tiempoSimulacion
                < obtenerTiempoTotal()) {

            avanzarUnPasoPlanificacion();
        }


        if (tiempoSimulacion
                >= obtenerTiempoTotal()) {

            finalizarPlanificacion();
        }
    }


    // ==============================================
    // PREPARAR FCFS
    // ==============================================

    private void prepararPlanificacion() {

        if (simulacionIniciada) {

            return;
        }


        procesosSimulacionActual.clear();


        /*
         * Solamente entran en la nueva tanda
         * los procesos que todavía no terminaron.
         */
        for (Proceso proceso : procesos) {

            if (proceso.getEstado()
                    != EstadoProceso.TERMINADO) {

                procesosSimulacionActual.add(
                        proceso
                );
            }
        }


        if (procesosSimulacionActual.isEmpty()) {

            mostrarError(
                    "No hay procesos pendientes por ejecutar."
            );

            return;
        }


        String algoritmo =
                cmbAlgorithm.getValue();


        /*
         * Ejecutamos el algoritmo seleccionado.
         */
        if (FCFS_NOMBRE.equals(algoritmo)) {

            resultadoFCFS =
                    fcfs.planificar(
                            procesosSimulacionActual
                    );

            resultadoSJF = null;


        } else if (SJF.equals(algoritmo)) {

            resultadoSJF =
                    sjf.planificar(
                            procesosSimulacionActual
                    );

            resultadoFCFS = null;


        } else {

            mostrarError(
                    "Este algoritmo todavía no está implementado."
            );

            return;
        }


        tiempoSimulacion = 0;

        simulacionIniciada = true;


        actualizarEstadosPlanificacion();

        actualizarVistaCPU();

        actualizarColaListos();

        actualizarGantt();

        tblProcesses.refresh();
    }


    // ==============================================
    // UN PASO DE FCFS
    // ==============================================

    private void avanzarUnPasoPlanificacion() {

        if (!simulacionIniciada) {

            return;
        }


        if (tiempoSimulacion
                >= obtenerTiempoTotal()) {

            return;
        }


        tiempoSimulacion++;


        actualizarEstadosPlanificacion();

        actualizarVistaCPU();

        actualizarColaListos();

        actualizarGantt();

        tblProcesses.refresh();
    }


    // ==============================================
    // ESTADOS
    // ==============================================

    private void actualizarEstadosPlanificacion() {


        for (Proceso proceso : procesos) {


            /*
             * Todavía no ha llegado.
             */
            if (tiempoSimulacion
                    < proceso.getTiempoLlegada()) {


                proceso.setEstado(
                        EstadoProceso.NUEVO
                );


                proceso.setTiempoRestante(
                        proceso.getRafagaCPU()
                );


                continue;
            }


            /*
             * Ya terminó.
             */
            if (proceso.getTiempoFinalizacion()
                    <= tiempoSimulacion) {


                proceso.setEstado(
                        EstadoProceso.TERMINADO
                );


                proceso.setTiempoRestante(
                        0
                );


                continue;
            }


            /*
             * Está utilizando la CPU.
             */
            if (proceso.getTiempoInicio()
                    <= tiempoSimulacion
                    && tiempoSimulacion
                    < proceso.getTiempoFinalizacion()) {


                proceso.setEstado(
                        EstadoProceso.EJECUCION
                );


                int restante =
                        proceso.getTiempoFinalizacion()
                                - tiempoSimulacion;


                proceso.setTiempoRestante(
                        restante
                );


                continue;
            }


            /*
             * Ya llegó pero todavía no le toca.
             */
            proceso.setEstado(
                    EstadoProceso.LISTO
            );


            proceso.setTiempoRestante(
                    proceso.getRafagaCPU()
            );
        }
    }



    private int obtenerTiempoTotal() {

        String algoritmo =
                cmbAlgorithm.getValue();


        if (FCFS_NOMBRE.equals(algoritmo)
                && resultadoFCFS != null) {

            return resultadoFCFS
                    .getTiempoTotal();
        }


        if (SJF.equals(algoritmo)
                && resultadoSJF != null) {

            return resultadoSJF
                    .getTiempoTotal();
        }


        return 0;
    }



    private double obtenerEsperaPromedio() {

        String algoritmo =
                cmbAlgorithm.getValue();


        if (FCFS_NOMBRE.equals(algoritmo)
                && resultadoFCFS != null) {

            return resultadoFCFS
                    .getEsperaPromedio();
        }


        if (SJF.equals(algoritmo)
                && resultadoSJF != null) {

            return resultadoSJF
                    .getEsperaPromedio();
        }


        return 0;
    }


    private double obtenerRespuestaPromedio() {

        String algoritmo =
                cmbAlgorithm.getValue();


        if (FCFS_NOMBRE.equals(algoritmo)
                && resultadoFCFS != null) {

            return resultadoFCFS
                    .getRespuestaPromedio();
        }


        if (SJF.equals(algoritmo)
                && resultadoSJF != null) {

            return resultadoSJF
                    .getRespuestaPromedio();
        }


        return 0;
    }


    // ==============================================
    // CPU
    // ==============================================

    private void actualizarVistaCPU() {


        lblSimulationTime.setText(
                String.valueOf(
                        tiempoSimulacion
                )
        );


        Proceso actual =
                obtenerProcesoEnCPU();


        if (actual == null) {


            lblCpuState.setText(
                    "LIBRE"
            );


            lblCurrentProcess.setText(
                    "---"
            );


            lblRemainingTime.setText(
                    "---"
            );


            cpuProgress.setProgress(
                    0
            );


            return;
        }


        lblCpuState.setText(
                "OCUPADA"
        );


        lblCurrentProcess.setText(

                String.format(
                        "P%03d - %s",
                        actual.getPid(),
                        actual.getNombre()
                )
        );


        lblRemainingTime.setText(

                String.valueOf(
                        actual.getTiempoRestante()
                )
        );


        double ejecutado =
                actual.getRafagaCPU()
                        - actual.getTiempoRestante();


        double progreso =
                ejecutado
                        / actual.getRafagaCPU();


        cpuProgress.setProgress(
                progreso
        );
    }


    private Proceso obtenerProcesoEnCPU() {


        for (Proceso proceso : procesos) {


            if (proceso.getEstado()
                    == EstadoProceso.EJECUCION) {

                return proceso;
            }
        }


        return null;
    }


    // ==============================================
    // COLA DE LISTOS
    // ==============================================

    private void actualizarColaListos() {


        readyQueueContainer
                .getChildren()
                .clear();


        procesos.stream()

                .filter(
                        proceso ->
                                proceso.getEstado()
                                        == EstadoProceso.LISTO
                )

                .sorted(

                        Comparator
                                .comparingInt(
                                        Proceso::getTiempoLlegada
                                )
                                .thenComparingInt(
                                        Proceso::getPid
                                )
                )

                .forEach(
                        proceso -> {


                            if (!readyQueueContainer
                                    .getChildren()
                                    .isEmpty()) {


                                Label flecha =
                                        new Label(
                                                "→"
                                        );


                                flecha
                                        .getStyleClass()
                                        .add(
                                                "queue-arrow"
                                        );


                                readyQueueContainer
                                        .getChildren()
                                        .add(
                                                flecha
                                        );
                            }


                            Label etiqueta =
                                    new Label(

                                            String.format(
                                                    "P%03d",
                                                    proceso.getPid()
                                            )
                                    );


                            etiqueta
                                    .getStyleClass()
                                    .add(
                                            "queue-process"
                                    );


                            readyQueueContainer
                                    .getChildren()
                                    .add(
                                            etiqueta
                                    );
                        }
                );


        if (readyQueueContainer
                .getChildren()
                .isEmpty()) {


            Label vacia =
                    new Label(
                            "Sin procesos en espera"
                    );


            vacia
                    .getStyleClass()
                    .add(
                            "muted"
                    );


            readyQueueContainer
                    .getChildren()
                    .add(
                            vacia
                    );
        }
    }


    // ==============================================
    // DIAGRAMA DE GANTT
    // ==============================================

    private void actualizarGantt() {

        ganttContainer
                .getChildren()
                .clear();


        String algoritmo =
                cmbAlgorithm.getValue();


        if (FCFS_NOMBRE.equals(algoritmo)) {

            actualizarGanttFCFS();

        } else if (SJF.equals(algoritmo)) {

            actualizarGanttSJF();
        }
    }



    private void actualizarGanttFCFS() {

        if (resultadoFCFS == null) {
            return;
        }


        for (FCFS.Segmento segmento :
                resultadoFCFS.getSegmentos()) {


            if (segmento.getInicio()
                    > tiempoSimulacion) {

                continue;
            }


            agregarBloqueGantt(

                    segmento.getProceso(),

                    segmento.getInicio(),

                    segmento.getFin(),

                    segmento.esCPUOciosa()
            );
        }
    }


    private void actualizarGanttSJF() {

        if (resultadoSJF == null) {
            return;
        }


        for (SJF.Segmento segmento :
                resultadoSJF.getSegmentos()) {


            if (segmento.getInicio()
                    > tiempoSimulacion) {

                continue;
            }


            agregarBloqueGantt(

                    segmento.getProceso(),

                    segmento.getInicio(),

                    segmento.getFin(),

                    segmento.esCPUOciosa()
            );
        }
    }



    private void agregarBloqueGantt(
            Proceso proceso,
            int inicio,
            int fin,
            boolean cpuOciosa) {


        VBox bloque =
                new VBox(3);


        bloque.setMinWidth(

                Math.max(
                        70,
                        (fin - inicio) * 35
                )
        );


        bloque.setAlignment(
                javafx.geometry.Pos.CENTER
        );


        Label nombre;


        if (cpuOciosa) {

            nombre =
                    new Label(
                            "LIBRE"
                    );


            bloque.setStyle(
                    "-fx-background-color: #e2e8f0;"
                            + "-fx-border-color: #94a3b8;"
                            + "-fx-padding: 8;"
            );


        } else {

            nombre =
                    new Label(

                            String.format(
                                    "P%03d",
                                    proceso.getPid()
                            )
                    );


            nombre.setStyle(
                    "-fx-text-fill: white;"
                            + "-fx-font-weight: bold;"
            );


            bloque.setStyle(
                    "-fx-background-color: #3462f5;"
                            + "-fx-border-color: white;"
                            + "-fx-padding: 8;"
            );
        }


        Label tiempos =
                new Label(
                        inicio
                                + " - "
                                + fin
                );


        if (!cpuOciosa) {

            tiempos.setStyle(
                    "-fx-text-fill: white;"
            );
        }


        bloque
                .getChildren()
                .addAll(
                        nombre,
                        tiempos
                );


        ganttContainer
                .getChildren()
                .add(
                        bloque
                );
    }


    // ==============================================
    // DOS NIVELES Y GARANTIZADA
    // ==============================================

    private void ejecutarDosNiveles() {

        procesosSimulacionActual.clear();


        for (Proceso proceso : procesos) {

            if (proceso.getEstado()
                    != EstadoProceso.TERMINADO) {

                proceso.reiniciarSimulacion();

                procesosSimulacionActual.add(
                        proceso
                );
            }
        }


        if (procesosSimulacionActual.stream()
                .anyMatch(proceso -> proceso.getMemoriaMB() <= 0)) {

            mostrarError(
                    "Todos los procesos de dos niveles deben tener memoria mayor que 0 MB."
            );

            return;
        }


        PlanDosNiveles.Resultado resultado =
                planDosNiveles.simular(
                        1200,
                        procesosSimulacionActual.stream()
                                .sorted(
                                        Comparator
                                                .comparingInt(Proceso::getTiempoLlegada)
                                                .thenComparingInt(Proceso::getPid)
                                )
                                .map(proceso ->
                                        new PlanDosNiveles.ProcesoPlanificado(
                                                proceso.getPid(),
                                                proceso.getNombre(),
                                                proceso.getMemoriaMB(),
                                                proceso.getRafagaCPU()
                                        )
                                )
                                .toList()
                );


        ganttContainer
                .getChildren()
                .clear();


        int tiempoActual = 0;

        double sumaEspera = 0;

        double sumaRespuesta = 0;


        for (PlanDosNiveles.ProcesoPlanificado planificado :
                resultado.ordenEjecucion()) {

            Proceso proceso =
                    buscarProcesoPorPid(
                            planificado.pid()
                    );


            if (proceso == null) {

                continue;
            }


            if (tiempoActual
                    < proceso.getTiempoLlegada()) {

                agregarBloqueGantt(
                        null,
                        tiempoActual,
                        proceso.getTiempoLlegada(),
                        true
                );

                tiempoActual =
                        proceso.getTiempoLlegada();
            }


            int inicio =
                    tiempoActual;

            int fin =
                    inicio
                            + proceso.getRafagaCPU();


            proceso.setTiempoInicio(
                    inicio
            );

            proceso.setTiempoFinalizacion(
                    fin
            );

            proceso.setTiempoEspera(
                    inicio
                            - proceso.getTiempoLlegada()
            );

            proceso.setTiempoRespuesta(
                    proceso.getTiempoEspera()
            );

            proceso.setTiempoRestante(
                    0
            );

            proceso.setEstado(
                    EstadoProceso.TERMINADO
            );


            agregarBloqueGantt(
                    proceso,
                    inicio,
                    fin,
                    false
            );


            sumaEspera += proceso.getTiempoEspera();

            sumaRespuesta += proceso.getTiempoRespuesta();

            tiempoActual =
                    fin;
        }


        finalizarPlanificacionDirecta(
                tiempoActual,
                sumaEspera,
                sumaRespuesta,
                "Dos niveles ejecutado: procesos cargados en memoria principal y secundaria."
        );
    }


    private void ejecutarGarantizada() {

        procesosSimulacionActual.clear();


        for (Proceso proceso : procesos) {

            if (proceso.getEstado()
                    != EstadoProceso.TERMINADO) {

                proceso.reiniciarSimulacion();

                procesosSimulacionActual.add(
                        proceso
                );
            }
        }


        PlanGarantizada.Resultado resultado =
                planGarantizada.simular(
                        procesosSimulacionActual.stream()
                                .sorted(
                                        Comparator
                                                .comparingInt(Proceso::getTiempoLlegada)
                                                .thenComparingInt(Proceso::getPid)
                                )
                                .map(proceso ->
                                        new PlanGarantizada.ProcesoGarantizado(
                                                proceso.getPid(),
                                                proceso.getNombre(),
                                                proceso.getRafagaCPU()
                                        )
                                )
                                .toList()
                );


        ganttContainer
                .getChildren()
                .clear();


        for (PlanGarantizada.Paso paso :
                resultado.pasos()) {

            Proceso proceso =
                    buscarProcesoPorPid(
                            paso.proceso().pid()
                    );


            if (proceso != null) {

                agregarBloqueGantt(
                        proceso,
                        paso.inicio(),
                        paso.fin(),
                        false
                );
            }
        }


        double sumaEspera = 0;

        double sumaRespuesta = 0;


        for (Proceso proceso :
                procesosSimulacionActual) {

            int inicio =
                    resultado.pasos()
                            .stream()
                            .filter(paso -> paso.proceso().pid() == proceso.getPid())
                            .mapToInt(PlanGarantizada.Paso::inicio)
                            .min()
                            .orElse(0);

            int fin =
                    resultado.pasos()
                            .stream()
                            .filter(paso -> paso.proceso().pid() == proceso.getPid())
                            .mapToInt(PlanGarantizada.Paso::fin)
                            .max()
                            .orElse(0);

            int espera =
                    Math.max(
                            0,
                            fin
                                    - proceso.getTiempoLlegada()
                                    - proceso.getRafagaCPU()
                    );

            int respuesta =
                    Math.max(
                            0,
                            inicio
                                    - proceso.getTiempoLlegada()
                    );


            proceso.setTiempoInicio(
                    inicio
            );

            proceso.setTiempoFinalizacion(
                    fin
            );

            proceso.setTiempoEspera(
                    espera
            );

            proceso.setTiempoRespuesta(
                    respuesta
            );

            proceso.setTiempoCPURecibido(
                    proceso.getRafagaCPU()
            );

            proceso.setTiempoRestante(
                    0
            );

            proceso.setEstado(
                    EstadoProceso.TERMINADO
            );


            sumaEspera += espera;

            sumaRespuesta += respuesta;
        }


        finalizarPlanificacionDirecta(
                resultado.tiempoTotal(),
                sumaEspera,
                sumaRespuesta,
                "Garantizada ejecutada: cada proceso activo recibe una cuota aproximada de 1/n de CPU."
        );
    }


    private void finalizarPlanificacionDirecta(
            int tiempoTotal,
            double sumaEspera,
            double sumaRespuesta,
            String mensaje) {

        tiempoSimulacion =
                tiempoTotal;

        simulacionIniciada =
                false;


        lblSimulationTime.setText(
                String.valueOf(
                        tiempoTotal
                )
        );

        lblCpuState.setText(
                "FINALIZADA"
        );

        lblCurrentProcess.setText(
                "---"
        );

        lblRemainingTime.setText(
                "0"
        );

        cpuProgress.setProgress(
                1
        );

        lblAverageWait.setText(
                String.format(
                        "%.2f",
                        sumaEspera / procesosSimulacionActual.size()
                )
        );

        lblAverageResponse.setText(
                String.format(
                        "%.2f",
                        sumaRespuesta / procesosSimulacionActual.size()
                )
        );

        lblExecutionTime.setText(
                tiempoTotal
                        + " u.t."
        );

        lblFormMessage.setText(
                mensaje
        );

        procesosSimulacionActual.clear();

        actualizarColaListos();

        tblProcesses.refresh();

        actualizarControles();
    }


    private Proceso buscarProcesoPorPid(
            int pid) {

        return procesos.stream()
                .filter(proceso -> proceso.getPid() == pid)
                .findFirst()
                .orElse(null);
    }





    // ==============================================
    // FINALIZAR
    // ==============================================

    private void finalizarPlanificacion() {

        /*
         * Guardamos los resultados ANTES
         * de modificar el estado de la simulación.
         */
        int tiempoTotal =
                obtenerTiempoTotal();

        double esperaPromedio =
                obtenerEsperaPromedio();

        double respuestaPromedio =
                obtenerRespuestaPromedio();


        tiempoSimulacion =
                tiempoTotal;


        actualizarEstadosPlanificacion();

        actualizarVistaCPU();

        actualizarColaListos();

        actualizarGantt();

        tblProcesses.refresh();


        lblAverageWait.setText(

                String.format(
                        "%.2f",
                        esperaPromedio
                )
        );


        lblAverageResponse.setText(

                String.format(
                        "%.2f",
                        respuestaPromedio
                )
        );


        lblExecutionTime.setText(

                tiempoTotal
                        + " u.t."
        );


        /*
         * La ejecución ya terminó.
         *
         * Ahora permitimos agregar procesos
         * nuevos sin borrar los anteriores.
         */
        simulacionIniciada = false;


        procesosSimulacionActual.clear();


        actualizarControles();
    }


    // ==============================================
    // REINICIAR
    // ==============================================

    @FXML
    private void handleReset() {

        reiniciarDatosSimulacion();

        limpiarFormulario();
    }


    private void reiniciarDatosSimulacion() {


        if (timeline != null) {

            timeline.stop();

            timeline = null;
        }


        procesos.clear();


        siguientePid = 1;


        resultadoFCFS = null;
        resultadoSJF = null;

        tiempoSimulacion = 0;

        simulacionIniciada = false;


        lblCpuState.setText(
                "LIBRE"
        );


        lblCurrentProcess.setText(
                "---"
        );


        lblRemainingTime.setText(
                "---"
        );


        lblSimulationTime.setText(
                "0"
        );


        cpuProgress.setProgress(
                0
        );


        ganttContainer
                .getChildren()
                .clear();


        ganttContainer
                .getChildren()
                .add(
                        lblGanttEmpty
                );


        lblAverageWait.setText(
                "--"
        );


        lblAverageResponse.setText(
                "--"
        );


        lblExecutionTime.setText(
                "--"
        );


        actualizarColaListos();

        actualizarControles();
    }


    private boolean hayProcesosPendientes() {

        for (Proceso proceso : procesos) {

            if (proceso.getEstado()
                    != EstadoProceso.TERMINADO) {

                return true;
            }
        }

        return false;
    }

    // ==============================================
    // CONTROLES
    // ==============================================

    private void actualizarControles() {

        boolean hayProcesosPendientes =
                hayProcesosPendientes();


        String algoritmo =
                cmbAlgorithm.getValue();


        boolean algoritmoImplementado =
                FCFS_NOMBRE.equals(algoritmo)
                        || SJF.equals(algoritmo)
                        || GARANTIZADA.equals(algoritmo)
                        || DOS_NIVELES.equals(algoritmo);


        boolean puedeEjecutar =
                hayProcesosPendientes
                        && algoritmoImplementado
                        && !simulacionIniciada;


        btnExecute.setDisable(
                !puedeEjecutar
        );


        btnStep.setDisable(
                !puedeEjecutar
        );
    }


    // ==============================================
    // ORDEN TABLA
    // ==============================================

    private void ordenarTablaPorLlegada() {


        FXCollections.sort(

                procesos,

                Comparator
                        .comparingInt(
                                Proceso::getTiempoLlegada
                        )
                        .thenComparingInt(
                                Proceso::getPid
                        )
        );
    }


    // ==============================================
    // VALIDACIONES
    // ==============================================

    private int leerEnteroNoNegativo(
            TextField campo,
            String nombreCampo) {


        String texto =
                campo
                        .getText()
                        .trim();


        try {


            int valor =
                    Integer.parseInt(
                            texto
                    );


            if (valor < 0) {

                throw new IllegalArgumentException(

                        "El "
                                + nombreCampo
                                + " no puede ser negativo."
                );
            }


            return valor;


        } catch (NumberFormatException e) {


            throw new IllegalArgumentException(

                    "Ingrese un valor entero válido para "
                            + nombreCampo
                            + "."
            );
        }
    }


    private int leerEnteroPositivo(
            TextField campo,
            String nombreCampo) {


        int valor =
                leerEnteroNoNegativo(
                        campo,
                        nombreCampo
                );


        if (valor == 0) {

            throw new IllegalArgumentException(

                    "El "
                            + nombreCampo
                            + " debe ser mayor que 0."
            );
        }


        return valor;
    }


    // ==============================================
    // FORMULARIO
    // ==============================================

    private void limpiarFormulario() {


        txtProcessName.clear();

        txtArrivalTime.clear();

        txtBurstTime.clear();

        txtMemory.clear();

        lblFormMessage.setText("");


        if (!cmbPriority
                .getItems()
                .isEmpty()) {


            cmbPriority
                    .getSelectionModel()
                    .select(
                            Integer.valueOf(1)
                    );
        }


        if (!cmbQueue
                .getItems()
                .isEmpty()) {


            cmbQueue
                    .getSelectionModel()
                    .selectFirst();
        }
    }


    private void mostrarError(
            String mensaje) {


        lblFormMessage
                .setText(
                        mensaje
                );
    }
}
