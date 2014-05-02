package controller;

import view.Input;

/**
 * Created by viktor on 4/7/14.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        try {
            Input.main(args);
        }catch (Throwable e){
            System.err.println("Error occured.");
        }

    }
}
