package view;

import controller.MaxFlow;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.List;

public class Input extends Application {

    private static double width = 190, height = 120;

    static final private int NMAX = 15;

    private static  Group root;

    private static int  N=0;

    private static int part = 1;

    private static String[] _args;


    public static void main(String[] args) {
        _args = args;
        launch(args);
    }

    @Override
    public void start(final Stage stage) {

        final Scene scene = new Scene(new Group());
        stage.setTitle("Enter vertexes count");
        stage.setWidth(width);
        stage.setMaxWidth(width);
        stage.setHeight(height);
        stage.setMaxHeight(height);

        final VBox vbox = new VBox();

        final Text label = new Text("Enter vertexes count");
        final TextField input = new TextField();
        final Button button = new Button("Enter");
        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    N = Integer.parseInt(input.getText());
                    if(N <= 0){
                        label.setFill(Color.RED);
                        label.setText("Negative or zero value");
                    } else if(N > NMAX){
                        label.setFill(Color.RED);
                        label.setText("Too big integer");
                    } else{
                        stage.close();
                        matrixStage(stage);
                    }
                }catch (NumberFormatException e){
                    label.setFill(Color.RED);
                    label.setText("ooh!");
                    label.setText("Error occurred. Try again");
                }

            }
        });

        vbox.getChildren().addAll(label,input,button);
        vbox.setSpacing(5);
        vbox.setAlignment(Pos.CENTER);
        vbox.setLayoutX(10);

        root = (Group) scene.getRoot();
        root.getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();
    }

    public static void matrixStage(final Stage stage){
        stage.setTitle("Capacity capacity_matrix input");
        int n = 40;
        width = (N+1.5)*n;
        height = (N+3)*n;

        if(N < 3) {
            stage.setWidth(4 * n);
            stage.setMaxWidth(4 * n);

        }
        else {
            stage.setWidth(width);
            stage.setMaxWidth(width);
        }
        stage.setHeight(height);
        stage.setMaxHeight(height);
        stage.show();
        Canvas canvas = new Canvas(width,height);
        GraphicsContext gc =  canvas.getGraphicsContext2D();


        List<Text> textList = new LinkedList<>();
        Text t = null;
        final List<TextField> fieldList = new LinkedList<>();
        TextField tf = null;
        for(int i=0;i<N;++i){
            t = new Text(Integer.toString(i+1));
            t.setLayoutX(n*(i+1)+10);
            t.setLayoutY(30);
            textList.add(t);

            t = new Text(Integer.toString(i+1));
            t.setLayoutX(20);
            t.setLayoutY(n*(i+1)+15);
            textList.add(t);

            for(int j=0;j<N;++j){
                tf = new TextField();
                tf.setMaxWidth(35);
                tf.setMinWidth(35);
                tf.setMaxHeight(30);
                tf.setLayoutY(n * (i + 1));
                tf.setLayoutX(n * (j + 1));
                tf.setText("0");
                fieldList.add(tf);
            }
        }
        Button button = new Button("Enter");
        button.setLayoutX(n/2);
        button.setLayoutY(height - 1.7*n);

        final Text txt = new Text("Enter the capacities");
        txt.setLayoutX(n/2);
        txt.setLayoutY(height - 1.9*n);

        root.getChildren().clear();
        root.getChildren().addAll(canvas);
        root.getChildren().addAll(fieldList);
        root.getChildren().addAll(textList);
        root.getChildren().addAll(button , txt);


        final int[][] capacity_matrix = new int[N][N];
        final int[][] cost_matrix = new int[N][N];

        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(part == 1){
                    int i=0;
                    try {
                        for (int j = 0; j < N * N; ++j) {
                            i = j / N;
                            if(fieldList.get(j).getText().isEmpty())
                                fieldList.get(j).setText("0");
                            if(i != j)
                                capacity_matrix[i][j % N] = Integer.parseInt(fieldList.get(j).getText());
                            if(capacity_matrix[i][j % N] < 0){
                                throw new NumberFormatException();
                            }
                        }
                        for(TextField tf : fieldList){
                            tf.setText("0");
                        }
                        stage.setTitle("Cost capacity_matrix input");
                        txt.setFill(Color.BLACK);
                        txt.setText("Enter the costs");
                        part = 2;

                    }catch (NumberFormatException e){
                        txt.setFill(Color.RED);
                        txt.setText("Incorrect input");
                    }
                }
                else if(part == 2){
                    int i=0;
                    try {
                        for (int j = 0; j < N * N; ++j) {
                            i = j / N;
                            if(fieldList.get(j).getText().isEmpty())
                                fieldList.get(j).setText("0");
                            if(i != j)
                                cost_matrix[i][j % N] = Integer.parseInt(fieldList.get(j).getText());
                            if(cost_matrix[i][j % N] < 0){
                                throw new NumberFormatException();
                            }
                            if (capacity_matrix[i][j%N] == 0 && cost_matrix[i][j%N] != 0) {
                                throw new IllegalArgumentException();
                            }
                        }

                        stage.close();
                        try {
                            Engine.setMatrixes(capacity_matrix, cost_matrix);
                            new Engine().start(new Stage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        MaxFlow.set(capacity_matrix, capacity_matrix);

                    }catch (NumberFormatException e){
                        txt.setFill(Color.RED);
                        txt.setText("Incorrect input");
                    }catch (IllegalArgumentException e) {
                        txt.setFill(Color.RED);
                        txt.setText("Mismatch with the capacity matrix");
                    }

                }
            }
        });
    }
}
