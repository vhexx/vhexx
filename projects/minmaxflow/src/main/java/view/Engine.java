package view;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import controller.MaxFlow;
import controller.MinCostFlow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by viktor on 3/13/14.
 */
public class Engine extends Application{

    private static int width = 1000, height = 600;
    private static List<Vertex> vertexList;
    private static List<Edge> edgeList;
    private static TextField txt;
    private static Label err;
    private static Button show;
    private static boolean capcost = true;
    private static Group root;
    private static double r;
    private static int[][] capacity_matrix = null, cost_matrix = null;
    private static Vertex start = null, stock = null;

    public static void main(String[] args) {/*
        capacity_matrix = new int[][]{{0,2,5,1},{0,0,1,1},{0,0,0,2},{7,0,0,0}};
        cost_matrix = new int[][]{{0,3,7,2},{0,0,1,1},{0,0,0,3},{7,0,0,0}};*/
        MaxFlow.set(capacity_matrix,cost_matrix);

        launch(args);
    }

    public static void setMatrixes(int[][] _capacity_matrix, int[][] _cost_matrix){
        capacity_matrix = _capacity_matrix;
        cost_matrix = _cost_matrix;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        int N = capacity_matrix.length;
        r = 100/N;

        primaryStage.setTitle("Max Min cost flow");
        primaryStage.setWidth(width);
        primaryStage.setHeight(height + 50);
        root = new Group();

        vertexList = new ArrayList<>(capacity_matrix.length);
        edgeList = new ArrayList<>();

        if(capacity_matrix == null || cost_matrix == null){
            primaryStage.close();
            System.err.println("Error occurred");
        }
        vertexList = build(capacity_matrix, cost_matrix, edgeList);

        //Solving overheads
        boolean flag;
        Vertex center = null;
        do {
            flag = true;
            outer:
            for (Vertex v : vertexList) {
                for (Edge e : edgeList) {
                    while (e.v1 != v && e.v2 != v && isOverlapEV(v, e)) {
                        v.x = 2*r + Math.random() * (width-4*r);
                        v.y = 2*r + Math.random() * (height-4*r);
                        flag = false;
                    }
                }
                for(Vertex v_other : vertexList){
                    while (v!=v_other && isOverlapVV(v,v_other)){
                        center = getDirection(vertexList);
                        v.x = 2*r + Math.random() * (width-4*r);
                        v.y = 2*r + Math.random() * (height-4*r);
                        flag = false;
                    }
                }
            }

        }while (flag==false);

        double xmin = width, xmax = 0, ymin = height, ymax=0;
        double x=0, y=0, count=0;
        for(Vertex v : vertexList){
            x += v.x;
            y += v.y;
            count++;
            if(v.x < xmin)
                xmin = v.x;
            if(v.x > xmax)
                xmax = v.x;
            if(v.y < ymin)
                ymin = v.y;
            if(v.y > ymax)
                ymax = v.y;
        }

        double dx = width/2 - x/count;
        double dy = height/2 - y/count;
        if(xmin + dx < 2*r || xmax + dx > width-2*r) {
            dx=0;
        }
        if(ymin + dy < 2*r || ymax + dy > height-2*r) {
            dy=0;
        }
        for (Vertex v : vertexList) {
            v.x += dx;
            v.y += dy;
        }

        //Drawing
        for(Edge e : edgeList) {
            e.set();
            e.draw();
        }
        for(Vertex v : vertexList) {
            v.draw();
        }



        show = new Button("Show costs");
        show.setOnMousePressed(mouseEvent -> {
            if(capcost == true){
                show.setText("Show capacities");
            }else{
                show.setText("Show costs");
            }
            capcost = !capcost;
            for(Edge e : edgeList) {
                e.viewText();
            }
        });
        root.getChildren().add(show);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     *  Tests if Vertex stands on the edge between another two.
     */
    public static boolean isOverlapEV(Vertex v, Edge e){
        double x1 = e.v1.x, y1 = e.v1.y;
        double x2 = e.v2.x, y2 = e.v2.y;
        double x0 = v.x, y0 = v.y;
        double a = Math.sqrt((x0-x1)*(x0-x1) + (y0-y1)*(y0-y1));
        double b = Math.sqrt((x0-x2)*(x0-x2) + (y0-y2)*(y0-y2));
        double c = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
        double m = (a*a-b*b+c*c)/(2*c);
        double xp=(x2-x1)*m/c + x1;
        double yp=(y2-y1)*m/c + y1;
        double d = Math.sqrt((x0-xp)*(x0-xp) + (y0-yp)*(y0-yp));

        if(d<4*r)
            return true;
        return false;
    }

    public static boolean isOverlapVV(Vertex v1, Vertex v2){
        double d = distance(v1,v2);
        if(d<5*r){
            return true;
        }
        return false;
    }

    private static double distance(Vertex v1, Vertex v2){
        return Math.sqrt((v1.x-v2.x)*(v1.x-v2.x) + (v1.y-v2.y)*(v1.y-v2.y));
    }

    public static List<Vertex> build(int[][] capacity_matrix,int[][] cost_matrix,List<Edge> edges){
        double prev_x = width/2, prev_y=height/2, dx, dy;
        int space = 300;
        Vertex[] array = new Vertex[capacity_matrix.length];
        Edge tmpEdge;
        for(int i=0;i<capacity_matrix.length;++i){
            if(array[i] == null){
                do{
                    dx = (prev_x - space) + Math.random() * 2 * space;
                }while(dx < 2*r || dx > width - 3*r);
                do{
                    dy = (prev_y - space) + Math.random() * 2 * space;
                }while(dy < 2*r || dy > height - 3*r);
                array[i] = new Vertex(dx, dy, i+1);
            }
            Vertex center = getDirection(array);
            prev_x = center.x;
            prev_y = center.y;
            for(int j=0;j<capacity_matrix[i].length;++j){
                if(capacity_matrix[i][j] != 0){
                    if(array[j] == null) {
                        do{
                            dx = (prev_x - space ) + Math.random() * 2 * space;
                        }while(dx < 2*r || dx > width - 3*r);
                        do{
                            dy = (prev_y - space) + Math.random() * 2 * space;
                        }while(dy < 2*r || dy > height - 3*r);
                        array[j] = new Vertex(dx, dy, j+1);
                    }
                    tmpEdge = new Edge(array[i],array[j], capacity_matrix[i][j], cost_matrix[i][j]);
                    edges.add(tmpEdge);
                    array[i].edges.add(tmpEdge);

                }
            }
        }
        return Arrays.asList(array);
    }

    static Vertex getDirection(Vertex[] array){
        int x=0, y=0, count=0;
        for(Vertex v : array){
            if(v != null){
                x += v.x;
                y += v.y;
                count++;
            }
        }
        return new Vertex(x/(double)count, y/(double)count, 0);
    }

    static Vertex getDirection(List<Vertex> array){
        int x=0, y=0, count=0;
        for(Vertex v : array){
            if(v != null){
                x += v.x;
                y += v.y;
                count++;
            }
        }
        return new Vertex(x/(double)count, y/(double)count, 0);
    }
    static class Vertex{
        public List<Edge> edges;
        private Circle circle;
        private Text label;
        private int num;
        public double x,y;
        public Vertex(double x, double y, int _num){
            edges = new LinkedList<>();
            num = _num;
            circle = new Circle(this.x=x,this.y=y, r);
            circle.setOnMouseClicked(mouseEvent -> {
                int maxflow, kflow, cost;
                if(start == null){
                    start = Vertex.this;
                    circle.setFill(Color.YELLOW);
                }else{
                    if(stock == null){
                        stock = Vertex.this;
                        circle.setFill(Color.LIGHTGREEN);
                        maxflow = MaxFlow.maxflow(start.num, num);
                        kflow = (int)(maxflow*MinCostFlow.K);
                        int[][] flows = new int[capacity_matrix.length][capacity_matrix.length];
                        cost = MinCostFlow.mincost(capacity_matrix, cost_matrix,flows, start.num, num, kflow);


                        for(Vertex v : vertexList){
                            int i1 = v.getNum() - 1 , i2;
                            int flow;
                            for(Edge e : v.edges){
                                i2 = e.v2.getNum() - 1;
                                flow = flows[i1][i2];
                                e.initializeFlowAndCost();
                                e.setFlowAndCost(flow, flow*cost_matrix[i1][i2]);
                                e.viewText();
                            }
                        }
                        System.out.println(System.lineSeparator()+"---------");
                        System.out.printf("SOURCE: %d"+System.lineSeparator()+"STOCK: %d"+System.lineSeparator(),start.num, stock.num);
                        System.out.printf("MAX-FLOW: %d"+System.lineSeparator(),maxflow);
                        System.out.printf("[%.2f*MAX FLOW]: %d"+System.lineSeparator(), MinCostFlow.K, kflow);
                        System.out.printf("MIN-COST FOR %.2f*MAX-FLOW: %d"+System.lineSeparator(),MinCostFlow.K, cost);
                        System.out.println();

                    } else{
                        start.circle.setFill(Color.WHITE);
                        stock.circle.setFill(Color.WHITE);

                        start = Vertex.this;
                        circle.setFill(Color.YELLOW);
                        stock = null;
                    }
                }
            });
            label = new Text(Integer.toString(num));
        }
        public int getNum(){
            return num;
        }
        public Vertex draw(){
            circle.setFill(null);
            circle.setStroke(Color.BLACK);
            circle.setFill(Color.WHITE);
            circle.setCenterX(x);
            circle.setCenterY(y);
            circle.setRadius(r);

            label.setFont(Font.font("Arial",r));
            label.setX(x - (label.getText().length()>1?1.7 * r : r)/ 3);
            label.setY(y + r / 3);
            label.setDisable(true);
            root.getChildren().add(circle);
            root.getChildren().add(label);
            return this;
        }


    }
    static class Edge{
        private CubicCurve cubic;
        private Text label;
        private Line l1,l2;
        private int capacity, flow, maxcost, cost;
        public Vertex v1, v2;

        public Edge(Vertex v1, Vertex v2, int _capacity, int _maxcost){
            this.v1 =v1;
            this.v2 =v2;
            capacity = _capacity;
            maxcost = _maxcost;
            cubic = new CubicCurve();
            label = new Text();
            label.setFont(Font.font("Verdana",r*0.75));
            initializeFlowAndCost();
            viewText();
            set();

        }

        public Edge set(){

            cubic.setStartX(v1.x);
            cubic.setStartY(v1.y);

            cubic.setControlX1(v1.x/2 + v2.x/2);
            cubic.setControlY1(v1.y);

            cubic.setControlX2(v1.x/2 + v2.x/2);
            cubic.setControlY2(v2.y);

            cubic.setEndX(v2.x);
            cubic.setEndY(v2.y);
            label.setX(v1.x/2 + v2.x/2 - r);
            label.setY(v1.x < v2.x? v1.y/2 + v2.y/2 - r/3 : v1.y/2 + v2.y/2 + 2*r/3);


            return this;
        }
        public Edge setCX1(double val){
            cubic.setControlX1(val);
            return this;
        }
        public Edge setCY1(double val){
            cubic.setControlY1(val);
            return this;
        }
        public Edge setCX2(double val){
            cubic.setControlX2(val);
            return this;
        }
        public Edge setCY2(double val){
            cubic.setControlY2(val);
            return this;
        }

        public Edge initializeFlowAndCost(){
            flow = 0;
            cost = 0;
            cubic.setStroke(Color.GREY);
            if(l1 != null)
                l1.setStroke(Color.GREY);
            if(l2 != null)
                l2.setStroke(Color.GREY);
            if(label != null) {
                label.setFill(Color.RED);
                label.setStroke(Color.RED);
            }
            return this;
        }

        public Edge setFlowAndCost(int _flow, int _cost){
            flow = _flow;
            cost = _cost;
            if(flow != 0) {
                cubic.setStroke(Color.ORANGERED);
                l1.setStroke(Color.ORANGERED);
                l2.setStroke(Color.ORANGERED);
                label.setFill(Color.BLUEVIOLET);
                label.setStroke(Color.BLUEVIOLET);
            }
            return this;
        }

        public Edge viewText(){
            if(capcost == true) {
                label.setText(String.format("%d/%d", flow, capacity));
            }else{
                label.setText(String.format("%d/%d", cost, maxcost));
            }
            return this;
        }
        public Edge draw(){
            cubic.setStroke(Color.GREY);
            cubic.setFill(null);


            double lx = getCubicX(0.75);
            double ly = getCubicY(0.75);
            double cx = getCubicX(0.7);
            double cy = getCubicY(0.7);

            double label2x = getCubicX(0.55);
            double label2y = getCubicY(0.55);
            double label1x = getCubicX(0.45);
            double label1y = getCubicY(0.45);

            l1 = new Line(lx, ly, lx-r/2, ly - r/5);
            l2 = new Line(lx, ly, lx-r/2, ly + r/5);
            l1.setStroke(Color.GRAY);
            l2.setStroke(Color.GRAY);
            int k = lx>cx? 0 : 180;
            l1.getTransforms().add(new Rotate(Math.toDegrees(Math.atan((ly-cy)/(lx-cx))) + k,lx,ly));
            l2.getTransforms().add(new Rotate(Math.toDegrees(Math.atan((ly-cy)/(lx-cx))) + k,lx,ly));

            label.getTransforms().add(new Rotate(Math.toDegrees(Math.atan((label2y-label1y)/(label2x-label1x))),
                    (label1x+label2x)/2,(label1y+label2y)/2));

            root.getChildren().addAll(cubic, label, l1, l2);
            return this;
        }

        private double getCubicX(double t){
            return (cubic.getStartX())*(1-t)*(1-t)*(1-t) + (cubic.getControlX1())*3*(1-t)*(1-t)*t +
                    (cubic.getControlX2())*3*(1-t)*t*t + (cubic.getEndX())*t*t*t;

        }
        private double getCubicY(double t){
            return (cubic.getStartY())*(1-t)*(1-t)*(1-t) + (cubic.getControlY1())*3*(1-t)*(1-t)*t +
                    (cubic.getControlY2())*3*(1-t)*t*t + (cubic.getEndY())*t*t*t;

        }
    }



}
