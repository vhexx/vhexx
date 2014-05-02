package controller;

import java.util.ArrayList;

/**
 * Created by viktor on 4/7/14.
 */
public class MaxFlow {
    private static int[][] c, p;

    public static void set(int[][] _capacity_matrix, int[][] _cost_matrix){
        c = _capacity_matrix;
        p = _cost_matrix;
        //System.out.println(Arrays.deepToString(c));
    }

    public static int maxflow(int start, int stock){

        int s = start - 1;
        int t = stock - 1;
        int n = c.length;
        int[][] f = new int[n][n];
        int[] h = new int[n];
        int[] e = new int[n];


        //initialize
        for(int i=0;i<n;++i){
            f[s][i] = c[s][i];
            f[i][s] = c[i][s]-c[s][i];
            e[i] = c[s][i];
        }

        h[s] = n;

        while(true){
            int i, j;
            for(i=0; i<n; ++i) {
                if (i != s && i != t && e[i] > 0)
                    break;
            }
            if (i == n)
                break;
            do {
                for (j = 0; j < n; ++j)
                    if (c[i][j] - f[i][j] > 0 && h[i] == h[j] + 1)
                        break;

                if (j < n) {
                    push(i, j, f, e);
                    break;
                } else {
                    if (!lift(i, h, f))
                        break;
                }
            }while (j >= n);
        }

        int flow = 0;
        for (int i=0; i<n; ++i) {
            if (c[s][i] > 0) {
                flow += f[s][i];
            }
        }
        flow = Math.max(0, flow);
        return flow;
    }

    private static void push(int u, int v, int[][] f, int[] e){
        double d = Math.min(e[u], c[u][v] - f[u][v]);
        f[u][v] += d;
        f[v][u] -= d;
        e[u] -= d;
        e[v] += d;

    }

    private static boolean lift (int u, int[] h, int[][] f)
    {
        int d = (int)Double.POSITIVE_INFINITY;
        for (int i = 0; i < f.length; ++i)
            if (c[u][i]-f[u][i] > 0)
                d = Math.min(d, h[i]);
        if (d == (int)Double.POSITIVE_INFINITY)
            return false;
        h[u] = d + 1;
            return true;
    }

    static class Rib{
        int b, u, c, f;
        int back;

        Rib(int b, int u, int c, int f, int back) {
            this.b = b;
            this.u = u;
            this.c = c;
            this.f = f;
            this.back = back;
        }
    }
    public static void add_rib (ArrayList<ArrayList<Rib>> g, int a, int b, int u, int c) {
        Rib r1 = new Rib( b, u, c, 0, g.get(a).size());
        Rib r2 = new Rib(a, 0, -c, 0, g.get(b).size());
        g.get(a).add(r1);
        g.get(b).add(r2);
    }
}
