package ru.vhex.wordbase;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.*;

/**
 * Main console application.
 *
 * @author viktorhex
 */
public class WordBaseMain {

    final static private String dirs = "{\"en_ru\":\"en-ru\",\"ru_en\":\"ru-en\",\"test_strings\":\"en-ru\"}";

    public static void main(String[] args) {
        try(Connection connection = DbConnecter.connect()) {
            Dictionary dictionary = chooseDictionary(connection);
            if(dictionary==null)
                return;
            DbConnecter.loadFromDb(dictionary,connection);
            while(true){
                int choice = UI.inputText("Add to dictionary, find or quit? [Enter 'a','f' or 'q']","a","f","q");
                if(choice==0)
                    addText(dictionary,connection);
                else if(choice==1)
                    findText(dictionary);
                else if(choice==2)
                    return;
            }

        } catch (SQLException |IllegalArgumentException|IllegalAccessException|IOException e) {
            System.err.println(e.getMessage());
        } catch (Exception|Error e){
            System.err.println("Critical error.");
        }
    }

    private static Dictionary chooseDictionary(Connection c) throws SQLException, IOException, IllegalAccessException {
        System.out.println("Choose the dictionary [or '0' to quit]:");
        List<String> tables = DbConnecter.getTables(c);
        UI.out(tables);
        int choice = UI.inputOneNumber(tables.size());
        if(choice==0)
            return null;
        String name = tables.get(choice-1);
        DbConnecter.setTableName(name);

        try{
            JSONObject dirObj = (JSONObject)(new JSONParser().parse(dirs.toString()));
            String direction = (String)dirObj.get(name);
            return new Dictionary(direction);
        } catch (ParseException e) {
            throw new IllegalArgumentException("'dirs' is incorrect");
        }

    }

    private static void addText(Dictionary dictionary, Connection c) throws IllegalAccessException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            System.out.println("Enter phrase [or 'b' to back]:");
            try {
                String line = "";
                do{
                    line = keyboard.readLine().trim();
                }while (line.isEmpty());
                String lowerPhrase = line.toLowerCase();
                if(lowerPhrase.equals("b")){
                    return;
                }
                dictionary.match(lowerPhrase.substring(0, 1));
                List<String> translations = Translater.translate(line);
                if(translations.isEmpty()){
                    System.out.println("No translations found.");
                    continue;
                }
                UI.out(translations);
                System.out.println("Input numbers of translations you with new line [or '0' to end input]:");
                List<Integer> chosen = UI.inputNumbers(translations.size());
                if(chosen.isEmpty() || chosen.get(0)==0)
                    continue;
                List<String> yourTranslations = new LinkedList<>();
                for(int i=0;i<chosen.size();++i){
                    yourTranslations.add(translations.get(chosen.get(i)-1));
                }
                System.out.println(String.format("%d translations added", chosen.size()));
                LetterSeq newPhrase = dictionary.add(line,yourTranslations);
                DbConnecter.saveToDb(newPhrase,c);
            } catch (IOException|IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static void findText(Dictionary dictionary) throws IOException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            System.out.println("Enter first letters and press 'Enter' [or 'b' to back]:");
            String line = "";
            do{
                line = keyboard.readLine().trim();
            }while (line.isEmpty());
            if(line.toLowerCase().equals("b")){
                return;
            }
            Map<String,Set<String>> results =  dictionary.find(line);
            if(results.isEmpty()){
                System.out.println("No results found.");
            }
            else{
                int i=0;
                for(Map.Entry<String,Set<String>> entry : results.entrySet()){
                    System.out.println(++i + ") "+ entry.getKey());
                    for(String translation : entry.getValue()){
                        System.out.println("   - " + translation);
                    }
                }
            }
            System.out.println();

        }

    }


}
