package controller;

import java.util.*;

/**
 * Created by viktor on 4/13/14.
 */
public class MinCostFlow {

    private static final int INF = (int)Double.POSITIVE_INFINITY;

    public static final float K = 2/3f;

    public static void main(String[] args) {/*
        int[][] c = {{0,2,5,1},{0,0,1,1},{0,0,0,2},{7,0,0,0}};
        int[][] p = {{0,3,7,2},{0,0,1,1},{0,0,0,3},{7,0,0,0}};
        int n = c.length;
        int[][] f = new int[n][n];
        int s = 0, t = 3, k = 3;
        */


    }
    public static int mincost(int [][] c, int[][] p,int[][]f, int start, int stock, int flowValue){
        /**
         * c - capacity matrix
         * p - cost matrix
         *
         */
        int s = start - 1;
        int t = stock - 1;
        int cost = 0;
        int n = c.length;
        int[] pred;
        int mincap;
        while(flowValue>0) {
            mincap = INF;
            pred = levita(c, p, f, s);
            for (int i = t; i != s; i = pred[i]) {
                if(pred[i] == -1){
                    return -1;
                }
                if (c[pred[i]][i] < mincap)
                    mincap = c[pred[i]][i];
            }
            //System.out.println(mincap);
            mincap = mincap < flowValue ? mincap : flowValue;
            for (int i = t; i != s; i = pred[i]) {
                f[pred[i]][i] += mincap;
                cost += mincap * p[pred[i]][i];
            }
            flowValue -= mincap;
            //System.out.println(Arrays.deepToString(f));
            //System.out.println(cost);
        }
        return cost;
    }

    public static int[] levita(int[][] c, int[][] p, int[][] f, int s){
        int n = p.length;
        int[] m = new int[n];
        int[] d = new int[n];
        int[] pred = new int[n];
        int[][] cf = new int[n][n];
        for(int i=0;i<n;++i){
            pred[i] = -1;
            m[i] = 2;
            d[i] = INF;
            for(int j=0;j<n;++j){
                cf[i][j] = c[i][j] - f[i][j];
            }
        }
        m[s] = 1;
        d[s] = 0;

        Deque<Integer> deque = new ArrayDeque<>();
        deque.addFirst(s);
        int u;
        while (!deque.isEmpty()){
            u = deque.pop();
            for(int v=0;v<n;++v){
                if(p[u][v]>=0 && cf[u][v] != 0){
                    if(m[v] == 2){
                        m[v] = 1;
                        deque.addLast(v);
                        if(d[v] > d[u]+p[u][v]) {
                            d[v] = d[u] + p[u][v];
                            pred[v] = u;
                        }
                    }
                    else if(m[v] == 1){
                        if(d[v] > d[u]+p[u][v]) {
                            d[v] = d[u] + p[u][v];
                            pred[v] = u;
                        }
                    }
                    if(m[v] == 0 && d[v] > d[u]+p[u][v]){
                        m[v] = 1;
                        deque.addFirst(v);
                        d[v] = d[u]+p[u][v];
                        pred[v] = u;
                    }

                }

            }
            m[u] = 0;
        }

        return pred;
    }


}
