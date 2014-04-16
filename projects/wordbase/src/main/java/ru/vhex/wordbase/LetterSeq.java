package ru.vhex.wordbase;

import java.util.*;

/**
 * Represents the element of prefix-tree on which dictionary based.
 *
 * @author viktorhex
 */
public class LetterSeq implements Comparable {

    /** Unique part of a phrase */
    private String data;

    /** Positions of upper-cased character */
    private ArrayList<Byte> upperMask;

    /** Set of translations */
    private Set<String> translations;

    /** Reference to the parent element */
    private LetterSeq parent;

    /** List of children elements */
    private LinkedList<LetterSeq> list;

    public LetterSeq(String _data){
        if(_data == null || _data.isEmpty())
            throw new IllegalArgumentException();
        data=_data.intern();
    }

    public String getData() {
        return data;
    }

    void setData(String data) {
        this.data = data;
    }


    ArrayList<Byte> getUpperMask() {
        return upperMask;
    }

    void setUpperMask(ArrayList<Byte> upperMask) {
        this.upperMask = upperMask;
    }

    public Set<String> getTranslations() {
        return translations!=null?translations:Collections.EMPTY_SET;
    }

    void addTranslations(List<String> translations) {
        if(translations==null)
            return;
        if(this.translations == null)
            this.translations = new LinkedHashSet<>();
        this.translations.addAll(translations);
    }

    LetterSeq getParent() {
        return parent;
    }

    LinkedList<LetterSeq> getList() {
        return list;
    }

    void setList(LinkedList<LetterSeq> list) {
        this.list = list;
    }

    public LetterSeq(LetterSeq another){ //move data fro another
        if(another==null)
            throw new IllegalArgumentException();
        this.data=another.data;
        this.parent=another.parent;
        this.list =another.list;
        this.translations=another.translations;
        this.upperMask=another.upperMask;
    }
    LetterSeq setParent(LetterSeq _parent){
        parent=_parent;
        return this;
    }

    @Override
    public int compareTo(Object o) {
        return data.compareTo(((LetterSeq)o).data);
    }

    public String toString(){
        LetterSeq parent;
        StringBuilder stringBuilder = toWord0();
        if(upperMask!=null){
            for(Byte index : upperMask){
                stringBuilder.setCharAt(index,Character.toUpperCase(stringBuilder.charAt(index)));
            }
        }
        return stringBuilder.toString();
    }
    private StringBuilder toWord0(){
        if(parent==null){
            StringBuilder stringBuilder = new StringBuilder();
            return stringBuilder.append(data);
        }
        return parent.toWord0().append(data);

    }

    void findAllSubWords(Map<String,Set<String>>  result) {
        if(result==null)
            return;
        if(upperMask!=null){        //It means that it's a word
            result.put(toString(),translations);
        }
        if(list == null)
            return;
        for(LetterSeq child : list){
            child.findAllSubWords(result);
        }

    }
}
