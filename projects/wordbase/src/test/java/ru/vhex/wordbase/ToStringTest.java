package ru.vhex.wordbase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import ru.vhex.wordbase.DbConnecter;
import ru.vhex.wordbase.Dictionary;

import static org.junit.Assert.assertEquals;

/**
 * Created by viktor on 3/23/14.
 */
public class ToStringTest {
    @Test
    public void load() throws SQLException, IllegalAccessException {
        Connection connection = DbConnecter.connect();
        DbConnecter.setTableName("wop_test");
        connection.createStatement().execute("DROP TABLE IF EXISTS wop_test;");
        connection.createStatement().execute("CREATE TABLE wop_test(phrase TEXT NOT NULL,translations TEXT NOT NULL);");
        Dictionary dictionary = new Dictionary("ru-en");
        List<String> words = Arrays.asList("тест");
        for(String word : words){
            LetterSeq ls = dictionary.add(word,Translater.translate(word));
            assertEquals(word,ls.toString());
        }

    }
}
