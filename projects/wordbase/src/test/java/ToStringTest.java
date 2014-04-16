import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


/**
 * Verify method LetterSeq.toString()
 * 
 * @author viktorhex
 */
public class ToStringTest {
    @Test
    public void load() throws SQLException, IllegalAccessException, IOException {
        Connection connection = DbConnecter.connect();
        DbConnecter.setTableName("test_strings");
        connection.createStatement().execute("DROP TABLE IF EXISTS test_strings;");
        connection.createStatement().execute("CREATE TABLE test_strings (phrase TEXT NOT NULL,translations TEXT NOT NULL);");
        Dictionary dictionary = new Dictionary("en-ru");
        Path test_strings = Paths.get("src/test/java/testStrings.txt");
        BufferedReader reader = Files.newBufferedReader(test_strings, Charset.defaultCharset());
        String phrase = null;
        while ((phrase=reader.readLine())!=null){
            LetterSeq ls = dictionary.add(phrase,Translater.translate(phrase));
            assertEquals(phrase.trim(),ls.toString());
        }

    }
}
