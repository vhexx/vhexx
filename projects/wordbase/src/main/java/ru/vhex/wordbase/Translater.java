package ru.vhex.wordbase;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Class contains static methods to translate words of texts by interacting with Yandex.Dictionary and
 * Yandex.Translate API.
 *
 * @author viktorhex
 */


public class Translater {
    /** HTTP client */
    private static HttpClient client = HttpClientBuilder.create().build();

    /** HTTP Response */
    private static HttpResponse resp;

    /** Direction of translation */
    private static String lang;

    /** URI of response */
    private static URI uri;

    /** Object for JSON response */
    private static JSONObject json_obj;

    /** String builder for getting response */
    private  static StringBuilder json_builder;

    /**
     * Sets the lang of translation.
     *
     * @param _lang
     */
    public static void setLang(String _lang){
        lang = _lang;
    }

    /**
     * Translates and handles exceptions throwing by using methods.
     *
     * @param text - Text given for translation
     * @return LinkedList of transations or null if it doesn't found.
     */
    public static List<String> translate(String text) throws IllegalAccessException {
        try{
            if(text == null)
                throw new IllegalArgumentException();
            return getFromDictionaryAPI(text);
        } catch (ParseException|URISyntaxException|IOException e){
            throw new IllegalArgumentException("Problem with data.");
        } catch (NullPointerException e){
        }
        return Collections.emptyList();
    }

    /**
     *  Tries to get the translation of text using oldStuff.Dictionary API.
     *  If repsonse is empty uses Translation API.
     *
     *  @param text - Text given for translation
     *  @return LinkedList of transations or empty list if it doesn't found.
     *
     */
    static List<String> getFromDictionaryAPI(String text) throws URISyntaxException, IOException, ParseException, IllegalAccessException {
        String key = "dict.1.1.20140202T144814Z.3d766cb5de2f1e4e.da17d7c6ad93c74ac3983829cb60a810887539bd";
        //oldStuff.Dictionary API key

        uri = new URIBuilder()
                .setScheme("https")
                .setHost("dictionary.yandex.net")
                .setPath("/api/v1/dicservice.json/lookup")
                .setParameter("key", key)
                .setParameter("lang", lang)
                .setParameter("text", text)
                .setParameter("ui", "ru")
                .build();
        HttpGet httpGet = new HttpGet(uri);
        resp = client.execute(httpGet); //Getting response

        Scanner reader = new Scanner(resp.getEntity().getContent());
        json_builder = new StringBuilder();
        while(reader.hasNextLine())
            json_builder.append(reader.nextLine()); //Reading response
        reader.close();
        json_obj = (JSONObject)(new JSONParser().parse(json_builder.toString())); //Parsing repsonse to the JSON Object
        if(json_obj.containsKey("code")){
            long code = (long)json_obj.get("code");
            if(code== 403){
                throw new IllegalAccessException("The daily limit is exceeded");
            }
            if(code == 413){
                throw new IOException("Allowable text size exceeded");
            }
            else if(code != 200)
                return Collections.emptyList();
        }
        List<String> translated_list = new LinkedList<String>();
        JSONArray json_def = (JSONArray)json_obj.get("def");

        if(json_def.size()!=0){ //If response isn't empty - get translated words
            JSONArray translations = (JSONArray)((JSONObject)json_def.get(0)).get("tr");
            Iterator<JSONObject> iter = translations.iterator();
            while(iter.hasNext()){
                JSONObject json_current = iter.next();
                translated_list.add((String)json_current.get("text"));
                try{
                    Iterator<JSONObject> syn_iter = ((JSONArray)json_current.get("syn")).iterator();
                    while(syn_iter.hasNext()){
                        String syn = "";
                        syn = (String)syn_iter.next().get("text");
                        if(syn!=null)
                            translated_list.add(syn);
                    }
                }catch (NullPointerException e){ }

            }
            return translated_list;
        }
        else    //If response is empty - get using Translation API
            return getFromTranslateAPI(text);
    }

    /**
     *  Tries to get the translation of text using Translation API.
     *
     *  @param text - Text given for translation
     *  @return List of transations or empty list if it doesn't found.
     *
     */
    static List<String> getFromTranslateAPI(String text) throws URISyntaxException, IOException, ParseException, IllegalAccessException {
        String key = "trnsl.1.1.20140202T080443Z.ad79b0d6da2cacb6.44f51c7b291e497fcaedf23695fe58f7585c3850";
        uri = new URIBuilder()
                .setScheme("https")
                .setHost("translate.yandex.net")
                .setPath("/api/v1.5/tr.json/translate")
                .setParameter("key", key)
                .setParameter("text", text)
                .setParameter("lang", lang)
                .setParameter("format", "plain")
                .build();
        resp = client.execute(new HttpGet(uri));
        Scanner reader = new Scanner(resp.getEntity().getContent());
        json_builder=new StringBuilder();
        while(reader.hasNextLine())
            json_builder.append(reader.nextLine());
        json_obj = (JSONObject)(new JSONParser().parse(json_builder.toString()));

        if(json_obj.containsKey("code")){
            long code = (long)json_obj.get("code");
            if(code== 403){
                throw new IllegalAccessException("The daily limit is exceeded");
            }
            if(code == 413){
                throw new IOException("Allowable text size exceeded");
            }
            else if(code != 200)
                return Collections.emptyList();
        }

        List<String> translated_list = new LinkedList<>();
        if(json_obj.containsKey("text")){
            JSONArray translatedWords = (JSONArray)json_obj.get("text");
            Iterator<String> iter = translatedWords.iterator();
            while(iter.hasNext())
                translated_list.add(iter.next());
        }

        return translated_list;
    }
}
