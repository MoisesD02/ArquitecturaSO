package com.simuladorso.ui.controllers.memory.multiprogramming;

import com.simuladorso.memory.LinkedMemory;
import com.simuladorso.memory.LinkedMemory.Block;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.beans.property.SimpleStringProperty;

public class LinkedListController {
    @FXML private VBox root;
    private final LinkedMemory memory=new LinkedMemory(1024);
    private final ComboBox<String> strategy=new ComboBox<>();
    private final TextField capacity=new TextField("1024"), name=new TextField(), size=new TextField();
    private final TableView<Block> table=new TableView<>();
    private final Canvas bar=new Canvas(800,90);
    private final HBox nodes=new HBox(8);
    private final Label total=new Label(),used=new Label(),free=new Label(),gap=new Label(),message=new Label(),rule=new Label(),cursor=new Label();
    private final TextArea history=new TextArea();
    private final Button release=new Button("Liberar proceso"),configure=new Button("Aplicar capacidad");
    @FXML public void initialize(){
        root.setPadding(new Insets(20));root.setMinHeight(Region.USE_PREF_SIZE);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        strategy.getItems().addAll("First Fit","Next Fit","Best Fit","Worst Fit");strategy.setValue("First Fit");
        strategy.setOnAction(e -> { updateRule(); draw(); });
        Label title=label("Listas ligadas",24);Label crumb=label("MEMORIA / ASIGNACIÓN DINÁMICA",11);
        HBox cards=new HBox(12,card("TOTAL",total),card("UTILIZADA",used),card("LIBRE",free),card("MAYOR HUECO",gap));
        capacity.setPrefColumnCount(8);name.setPromptText("Ej. Editor");size.setPromptText("Ej. 120");
        configure.setOnAction(e -> attempt(() -> {memory.reset(number(capacity));refresh();record();}));
        FlowPane settings=new FlowPane(10,10,field("Capacidad total (KB)",capacity),configure,field("Estrategia",strategy));
        settings.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        rule.setWrapText(true);rule.setStyle("-fx-text-fill: #475569;");
        StackPane map=new StackPane(bar);map.setMinWidth(0);
        map.widthProperty().addListener((o,a,b)->{bar.setWidth(Math.max(100,b.doubleValue()));draw();});
        bar.setOnMouseClicked(e -> {
            if(e.getY()<12 || e.getY()>56) { table.getSelectionModel().clearSelection(); return; }
            int address=(int)(e.getX()/bar.getWidth()*memory.capacity());
            table.getSelectionModel().clearSelection();
            memory.blocks().stream().filter(b->!b.free() && address>=b.start() && address<b.start()+b.size()).findFirst().ifPresent(b->table.getSelectionModel().select(b));
        });
        ScrollPane chain=new ScrollPane(nodes);chain.setFitToHeight(true);chain.setPrefHeight(112);chain.setMinHeight(112);
        chain.setStyle("-fx-background-color: transparent; -fx-background: white;");
        Button add=new Button("Asignar proceso");add.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 9 16;");
        add.setOnAction(e->attempt(()->{Block b=memory.allocate(name.getText(),number(size),strategy.getValue());refresh();table.getSelectionModel().select(b);record();name.clear();size.clear();}));
        release.setOnAction(e->attempt(()->{Block b=table.getSelectionModel().getSelectedItem();if(b!=null){memory.release(b.id());refresh();record();}}));
        Button example=new Button("Cargar ejemplo comparativo");example.setOnAction(e->{memory.example();capacity.setText("1024");refresh();record();});
        Button reset=new Button("Reiniciar memoria");reset.setStyle("-fx-text-fill: #b42318;");reset.setOnAction(e->{memory.reset(memory.capacity());refresh();history.clear();record();});
        FlowPane actions=new FlowPane(10,10,field("Nombre del proceso",name),field("Tamaño solicitado (KB)",size),add);actions.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        column("Proceso",Block::name);column("Tamaño",b->b.size()+" KB");column("Inicio",b->b.start()+" KB");column("Fin exclusivo",b->(b.start()+b.size())+" KB");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);table.setMinHeight(165);table.setPrefHeight(165);
        table.setPlaceholder(label("Agrega un proceso o carga el ejemplo.",13));
        table.getSelectionModel().selectedItemProperty().addListener((o,a,b)->{release.setDisable(b==null);draw();});
        message.setWrapText(true);message.setMaxWidth(Double.MAX_VALUE);
        history.setEditable(false);history.setWrapText(true);history.setPrefRowCount(4);
        TitledPane log=new TitledPane("Historial de operaciones",history);log.setExpanded(false);
        TitledPane structure=new TitledPane("Ver estructura de la lista ligada",chain);structure.setExpanded(false);
        Label note=label("Cada nodo guarda estado, inicio, tamaño y enlace. Liberar une huecos vecinos; no mueve los procesos. El ejemplo reemplaza la simulación actual.",12);note.setWrapText(true);
        root.getChildren().addAll(crumb,title,cards,settings,rule,label("Distribución de memoria · Gris: libre / Colores: procesos",13),map,cursor,
                structure,actions,message,label("Procesos en memoria",14),table,new FlowPane(10,10,release,example,reset),log,note);
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> dismissSelection = event -> {
            if (!(event.getTarget() instanceof javafx.scene.Node target)) return;
            for (javafx.scene.Node node=target; node!=null; node=node.getParent()) {
                if (node==release || node==bar) return;
                if (node instanceof TableRow<?> row && row.getTableView()==table && !row.isEmpty()) return;
                if (node.getUserData() instanceof Block block && !block.free()) return;
            }
            table.getSelectionModel().clearSelection();
        };
        root.sceneProperty().addListener((observable, previous, current) -> {
            if(previous!=null) previous.removeEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,dismissSelection);
            if(current!=null) current.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,dismissSelection);
        });
        updateRule();refresh();record();
    }
    public void setStrategy(String value){if(strategy.getItems().contains(value))strategy.setValue(value);}
    private void updateRule(){rule.setText(switch(strategy.getValue()){
        case "Next Fit" -> "Busca desde el cursor y vuelve al principio si hace falta. El cursor se actualiza después de cada asignación.";
        case "Best Fit" -> "Examina los huecos y elige el más pequeño donde quepa. En empate, el de menor dirección.";
        case "Worst Fit" -> "Examina los huecos y elige el más grande. En empate, el de menor dirección.";
        default -> "Recorre la lista desde el inicio y utiliza el primer hueco suficiente.";
    });}
    private Label label(String text,int font){Label l=new Label(text);l.setStyle("-fx-text-fill: #1e293b; -fx-font-size: "+font+"px;");return l;}
    private VBox field(String text,javafx.scene.Node input){return new VBox(5,label(text,12),input);}
    private VBox card(String text,Label value){value.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");VBox box=new VBox(7,label(text,10),value);box.setPadding(new Insets(12));box.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8;");box.setMaxWidth(Double.MAX_VALUE);HBox.setHgrow(box,Priority.ALWAYS);return box;}
    private Color blockColor(Block b){return b.free()?Color.web("#e2e8f0"):Color.hsb(((b.id()-1)*137.508+145)%360,.48,.94);}
    private String colorCss(Block b){Color c=blockColor(b);return String.format("#%02x%02x%02x",(int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255));}
    private void column(String title,java.util.function.Function<Block,String> f){TableColumn<Block,String> c=new TableColumn<>(title);c.setCellValueFactory(d->new SimpleStringProperty(f.apply(d.getValue())));
        if("Proceso".equals(title)) c.setCellFactory(col->new TableCell<>(){
            @Override protected void updateItem(String item,boolean empty){super.updateItem(item,empty);setText(empty?null:item);setGraphic(null);
                if(!empty && getTableRow()!=null && getTableRow().getItem()!=null){var dot=new javafx.scene.shape.Circle(5,blockColor(getTableRow().getItem()));dot.setStroke(Color.web("#64748b"));setGraphic(dot);}
            }
        });
        table.getColumns().add(c);}
    private int number(TextField input){try{return Integer.parseInt(input.getText().trim());}catch(NumberFormatException e){throw new IllegalArgumentException("Introduce un número entero válido en KB.");}}
    private void attempt(Runnable action){try{action.run();}catch(IllegalArgumentException e){message.setStyle("-fx-text-fill: #b42318;");message.setText(e.getMessage());history.appendText("No asignado: "+e.getMessage()+"\n");}}
    private void record(){message.setStyle("-fx-text-fill: #166534;");message.setText(memory.explanation());history.appendText(memory.explanation()+"\n");}
    private void refresh(){
        var blocks=memory.blocks();table.getItems().setAll(blocks.stream().filter(b->!b.free()).toList());
        total.setText(memory.capacity()+" KB");used.setText((memory.capacity()-memory.freeKB())+" KB");free.setText(memory.freeKB()+" KB");gap.setText(memory.largestGap()+" KB");
        boolean busy=memory.freeKB()!=memory.capacity();capacity.setDisable(busy);configure.setDisable(busy);release.setDisable(true);
        nodes.getChildren().clear();
        for(Block b:blocks){
            VBox node=new VBox(5,label(b.free()?"LIBRE":b.name(),13),label("Inicio: "+b.start()+" KB",11),label("Tamaño: "+b.size()+" KB",11));
            node.setPadding(new Insets(10));node.setMinWidth(125);node.setStyle("-fx-background-color: "+colorCss(b)+"; -fx-background-radius: 6;");
            node.setUserData(b);
            node.setOnMouseClicked(e->{table.getSelectionModel().clearSelection();if(!b.free())table.getSelectionModel().select(b);});
            nodes.getChildren().addAll(node,label("→",18));
        }
        nodes.getChildren().add(label("null",13));draw();
    }
    private void draw(){
        var g=bar.getGraphicsContext2D();g.clearRect(0,0,bar.getWidth(),90);Block selected=table.getSelectionModel().getSelectedItem();
        for(Block b:memory.blocks()){
            double x=b.start()/(double)memory.capacity()*bar.getWidth(),w=b.size()/(double)memory.capacity()*bar.getWidth();
            g.setFill(blockColor(b));g.fillRect(x,12,Math.max(1,w-1),44);
            if(selected!=null && selected.id()!=b.id()){g.setFill(Color.rgb(255,255,255,.55));g.fillRect(x,12,w,44);}
            if(w>60){g.setFill(Color.web("#1e293b"));g.fillText(b.free()?"Libre":b.name(),x+6,31);g.fillText(b.size()+" KB",x+6,48);}
        }
        g.setFill(Color.web("#475569"));g.fillText("0 KB",0,75);g.fillText(memory.capacity()+" KB",Math.max(0,bar.getWidth()-70),75);
        boolean nextFit="Next Fit".equals(strategy.getValue());cursor.setVisible(nextFit);cursor.setManaged(nextFit);
        cursor.setText("Próxima búsqueda desde: "+memory.cursor()+" KB · Cursor Next Fit");cursor.setStyle("-fx-text-fill: #475569;");
        if("Next Fit".equals(strategy.getValue())){double x=memory.cursor()/(double)memory.capacity()*bar.getWidth();g.setStroke(Color.web("#ea580c"));g.setLineWidth(3);g.strokeLine(x,0,x,60);}
    }
}
