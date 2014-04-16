package ru.vhex.wordbase;

import java.util.*;

/**
 * Created by viktor on 3/16/14.
 *
 * @author viktorhex
 */
public class Dictionary {

    /** List of languages available. */
    private static List<String> languages = Arrays.asList("en", "ru");

    /** Regex for verification matching to the alphabet. */
    private String regex;

    /** From - source, to - target languages. */
    private String from,to;

    /** Map that contains words of the alphabet and the root of a prefix-tree associated with it. */
    private HashMap<Character, LetterSeq> alphabet;

    /**
     * Creates a new dictionary based on the direction of translation.
     * Dictionary contains alphabet represented as characters associated
     * with roots of prefix-trees - containers for text values with
     * translations (see class LetterSeq).
     *
     * @param direction in the format of "en-ru", "ru-en".
     */
    public Dictionary(String direction){
        if(direction==null)
            throw new IllegalArgumentException("Illegal argument.");
        int indexof;
        if((indexof = direction.indexOf("-"))>0 && indexof < direction.length()-1 ){
            from = direction.substring(0,indexof);
            to = direction.substring(indexof+1);
            if(!languages.contains(to))
                throw new IllegalArgumentException("Sorry, we don't have that language yet :(");
        }else
            throw new IllegalArgumentException("Illegal argument.");
        Translater.setLang(direction);
        alphabet = new HashMap<>();
        if(from.equals("en")){
            regex= "^[a-z0-9,.'\"]$";
        }
        else if(from.equals("ru")){
            regex= "^[а-я0-9,.'\"]$";
        }
        else
            throw new IllegalArgumentException("Sorry, we don't have that language yet :(");

    }

    /**
     * Matches string to the regex.
     *
     * @param first the string of first character.
     */
    public void match(String first){
        if(!first.matches(regex))
            throw new IllegalArgumentException(String.format("There is no letter '%s' in this language",first));
    }

    /**
     * Adds a word or phrase with translation in the prefix-tree for which current object is a root.
     *
     * @param phrase adding word or phrase.
     * @param translations list of translations.
     * @return added element.
     */
    public LetterSeq add(String phrase, List<String> translations){
        phrase = phrase.trim();
        if(phrase==null || phrase.equals("")){
            throw new IllegalArgumentException("Illegal argument.");
        }
        int wordLen = phrase.length();
        if(wordLen>120)
            throw new IllegalArgumentException("Too long phrase.");
        String lowerWord = phrase.toLowerCase();
        String first = lowerWord.substring(0,1);

        match(first);
        ArrayList<Byte> upperMask = new ArrayList<>();;//Make mask to restore upper-cases
        int masksize=0;
        for(int i=0;i<wordLen;++i){
            if(Character.isUpperCase(phrase.charAt(i)))
                upperMask.add((byte)i);
        }
        LetterSeq current = alphabet.get(first.charAt(0));
        if (current!=null){ //First case: There are words on this letter
            return addRec(current,lowerWord,wordLen,upperMask,translations);
        }
                                                             //Second case: No words for this letter
        LetterSeq newLS = new LetterSeq(lowerWord);
        newLS.setUpperMask(upperMask);
        newLS.addTranslations(translations);
        alphabet.put(first.charAt(0),newLS);
        return newLS;
    }

    private LetterSeq addRec(LetterSeq current, String word, int wordLen,
                             ArrayList<Byte> upperMask, List<String> translations){
            String data = current.getData();
            int dataLen = data.length();

            int index=0;
            if(data.equals(word)){  //Case 1
                current.addTranslations(translations);
                current.setUpperMask(upperMask);
                return current;
            }
                                //Case 2
            int min = Math.min(wordLen,dataLen);
            for(;index<min && data.charAt(index) == word.charAt(index);++index); //Searching for matching

            if(index==dataLen){         //Case 1.1 - word is longer than data
                LinkedList<LetterSeq> currentSet = current.getList();
                word = word.substring(index,wordLen);
                if(currentSet==null){
                    currentSet = new LinkedList<>();
                    LetterSeq newLS = new LetterSeq(word);
                    newLS.setUpperMask(upperMask);
                    newLS.addTranslations(translations);
                    newLS.setParent(current);
                    currentSet.add(newLS);
                    current.setList(currentSet);
                    return newLS;
                }
                //PASS
                for(LetterSeq child : currentSet){
                    if(child.getData().charAt(0)==word.charAt(0)){
                        return addRec(child,word,word.length(),upperMask,translations);
                    }
                }
                //If a suitable place not found
                LetterSeq newLS = new LetterSeq(word);
                newLS.setUpperMask(upperMask);
                newLS.addTranslations(translations);
                newLS.setParent(current);
                currentSet.add(newLS);
                current.setList(currentSet);
                return newLS;
            }
            current.setData(data.substring(index)); //Case 1.2 - word is shorter than data
            LetterSeq parent = current.getParent();
            LinkedList<LetterSeq> parentSet = null;
            if(parent!=null){
                parentSet = parent.getList();
                parentSet.remove(current);
            }
            LetterSeq firstChild= current;
            current = new LetterSeq(word.substring(0,index));
            LinkedList<LetterSeq> currentSet = new LinkedList<>();
                                             //Adding childs & parents
            currentSet.add(firstChild);
            firstChild.setParent(current);
            LetterSeq secondChild = null;
            if(index<wordLen){  //If word wasn't part of data
                secondChild = new LetterSeq(word.substring(index));
                currentSet.add(secondChild);
                secondChild.setParent(current);
                secondChild.setUpperMask(upperMask);
                secondChild.addTranslations(translations);
            }else{
                current.setParent(parent);
                current.setUpperMask(upperMask);
                current.addTranslations(translations);
            }
            current.setList(currentSet);
            if(parent!=null){
                parentSet.add(current);
                current.setParent(parent);
            }
            else
                alphabet.put(word.charAt(0), current);
            return secondChild==null?current:secondChild;
    }

    /**
     * Finds all words and phrases in the dictionary that starts with specified pattern and their translations.
     *
     * @param pattern string which finding words are started with.
     * @return map of words or phrases and sets of translations associated with them.
     */
    public Map<String,Set<String>> find(String pattern){
        pattern = pattern.toLowerCase();
        Map<String,Set<String>> result = new TreeMap<>();
        LetterSeq current;
        if(((current=alphabet.get(pattern.charAt(0))) == null))
            return Collections.emptyMap();
        return findRec(pattern, current, result);
    }

    private Map<String,Set<String>> findRec(String pattern, LetterSeq current,
                                             Map<String,Set<String>>  result){
        String data = current.getData();
        int dataLen = data.length(), patternLen = pattern.length();
        if(patternLen<=dataLen){
            if(data.indexOf(pattern)==0){
                current.findAllSubWords(result);
                return result;
            }
        }else{
            pattern = pattern.substring(dataLen, patternLen);
            if(current.getList()==null){
                return Collections.emptyMap();
            }
            for(LetterSeq child : current.getList()){
                if(pattern.charAt(0) == child.getData().charAt(0))
                    return findRec(pattern, child, result);
            }
        }
        return Collections.emptyMap();
    }
}
