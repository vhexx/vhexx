package extra;

import view.Engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by root on 4/26/14.
 */
public class GraphMaker {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int n;
        int[][] c = null,p = null;
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            if(line.equals("ok")){
                Engine.setMatrixes(c,p);
                Engine.main(args);
            }
            n = Integer.parseInt(line);
            c = new int[n][n];
            p = new int[n][n];
            for(int i=0;i<n;++i){
                for(int j=0;j<n;++j){
                    if(Math.random() < 0.4){
                        c[i][j] = (int)(Math.random()*9) + 1;
                        p[i][j] = (int)(Math.random()*9) + 1;
                    }
                }
            }
            for(int i=0;i<n;++i){
                System.out.println(Arrays.toString(c[i]));
            }
            System.out.println();
            for(int i=0;i<n;++i){
                System.out.println(Arrays.toString(p[i]));
            }
        }
    }
}
