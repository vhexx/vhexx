package ru.vhex.wordbase;

import java.io.BufferedReader;
import java.io.IOException;import java.io.InputStreamReader;

import java.util.*;

/**
 * Class contains static methods to represent text user interface.
 *
 * @author viktorhex
 */
public class UI{
    public static void out(Collection<String> list){
        int i=1;
        for(String str : list){
            System.out.println(String.format("  %d) %s",i,str));
            ++i;
        }
    }
    public static void out(String[] list){
        for(int i=0;i<list.length;++i){
            System.out.println(String.format("  %d) %s",i+1,list[i]));
        }
    }

    public static  int inputOneNumber(int max) throws IOException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        String tok = "";
        while(true){
            if((tok=keyboard.readLine().trim()).isEmpty())
                continue;
            try{
                int num = Integer.parseInt(tok);
                if(num>max || num<0)
                    throw new IllegalArgumentException("Incorrect number. Try again.");
                return num;
            } catch (NumberFormatException e){
                System.err.println("Syntax error. Try again.");
            } catch (IllegalArgumentException e){
                System.err.println(e.getMessage());
            }
        }

    }

    public static List<Integer> inputNumbers(int max) throws IOException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            String tok = keyboard.readLine().trim();
            if(tok.isEmpty())
                continue;
            List<Integer> numbers = new LinkedList<>();
            Integer num;
            do {
                try{
                    num = Integer.parseInt(tok);
                    if(num==0){
                        return numbers;
                    }
                    if(num>max || num<0)
                        throw new IllegalArgumentException("Incorrect number. Try again.");
                    if(!numbers.contains(num))
                        numbers.add(num);
                } catch (NumberFormatException e){
                    System.err.println("Syntax error. Try again.");
                } catch (IllegalArgumentException e){
                    System.err.println(e.getMessage());
                }
            } while (!(tok=keyboard.readLine().trim()).isEmpty());

            return numbers;
        }

    }
    public static int inputText(String message, String... answers) throws IOException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            System.out.println(message);
            String line = "";
            do{
                line = keyboard.readLine().trim();
            }while (line.isEmpty());

            for(int i=0;i<answers.length;++i){
                if(line.toLowerCase().equals(answers[i].toLowerCase()))
                    return i;
            }
        }
    }
}
