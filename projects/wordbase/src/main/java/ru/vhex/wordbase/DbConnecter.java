package ru.vhex.wordbase;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Class contains static methods to interact with the SQLite3 database named 'wbase.db'
 * which is located in the main directory. It allows to load all texts and translation
 * from database to write in dictionary.
 *
 *
 * @author viktorhex
 */
public class DbConnecter {

    /** Flag makes enable or disable showing tables which contains word 'test' */
    public static boolean test = false;

    /** Name of currently used table */
    private static String table = "";

    /**
     * Connects to the SQLite3 database named 'wbase.db'.
     *
     * @return a new connection.
     *
     */
    public static Connection connect(){
        try{
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:wbase.db");
            return connection;
        } catch (ClassNotFoundException e1) {
            throw new IllegalArgumentException("Database-driver not found");
        } catch (SQLException e) {
            throw new IllegalArgumentException("Database not found");
        }
    }

    /**
     * Sets a name of a used table in database which is for saving dictionary data.
     *
     * @param name - name of a table.
     */
    public static void setTableName(String name){
        table = name;
    }

    /**
     * Returns a list of available tables in the database associated with dictionaries.
     *
     * @param c connection to the database.
     * @return List of table names.
     * @throws SQLException
     * @throws IOException
     */
    public static List<String> getTables(Connection c) throws SQLException, IOException {
        if(c==null)
            throw new IOException("Connection failed.");
        Statement stat = c.createStatement();
        ResultSet resultSet = stat.executeQuery("SELECT name FROM sqlite_master WHERE type='table';");
        List<String> list = new LinkedList<>();
        String result = "";
        while (resultSet.next()){
            result = resultSet.getString("name");
            if(result.indexOf("test")>=0 && !test)
                continue;
            list.add(result);
        }
        if(!list.contains("en_ru")){
            Statement statement = c.createStatement();
            statement.execute("CREATE TABLE en_ru(phrase TEXT PRIMARY KEY,translations TEXT NOT NULL);");
            list.add("en_ru");
        }
        if(!list.contains("ru_en")){
            Statement statement = c.createStatement();
            statement.execute("CREATE TABLE ru_en(phrase TEXT PRIMARY KEY,translations TEXT NOT NULL);");
            list.add("ru_en");
        }
        return list;
    }

    /**
     * Loads all data from some table associated with a dictionary in the application memory.
     *
     * @param dict dictionary.
     * @param c connection to the database.
     * @return count of load words (with duplicates).
     * @throws IOException
     */
    public static int loadFromDb(Dictionary dict, Connection c) throws IOException {
        try {
            if(c==null)
                throw new IOException("Connection failed.");
            if(dict==null)
                throw new IllegalArgumentException("Dictionary not found");
            String line, phrase = null, translations = null;
            List<String> transList;
            Statement stat = c.createStatement();
            ResultSet rs = stat.executeQuery(String.format("SELECT * FROM %s;",table)) ;
            int count = 0;
            while (rs.next()){
                transList = new LinkedList<>();
                phrase = rs.getString("phrase").replace("\\\\", "\\").replace("''","'");
                translations = rs.getString("translations");
                for(String target : translations.split("\\\\,")){
                    transList.add(target.replace("\\\\", "\\").replace("''", "'"));
                };
                dict.add(phrase,transList);
                ++count;
            } 
           return count;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Bad data");
        }
    }

    /**
     * Save the phrase with translation in a current table in the database.
     *
     * @param ls saving phase as element of the prefix-tree.
     * @param c connection to the database.
     * @throws IOException
     */
    public static void saveToDb(LetterSeq ls, Connection c) throws IOException {
        try {
            if(c==null)
                throw new IOException("Connection failed.");
            if(ls==null)
                throw new IllegalArgumentException("Phrase not found");
            String phrase = ls.toString()
                    .replace("'", "''")
                    .replace("\\", "\\\\");
            Set<String> translations = ls.getTranslations();
            StringBuilder builder = new StringBuilder();
            for(Iterator<String> iter = translations.iterator();iter.hasNext();){
                builder.append(iter.next()
                        .replace("'", "''")
                        .replace("\\", "\\\\"));
                if(iter.hasNext()){
                    builder.append("\\,");
                }
            }
            Statement stat = c.createStatement();
            stat.execute(String.format("INSERT OR REPLACE into %s (phrase, translations) VALUES ('%s','%s');",
                    table,phrase,builder.toString()));

        } catch (SQLException e) {
            throw new IllegalArgumentException("Bad data");
        }
    }
}
