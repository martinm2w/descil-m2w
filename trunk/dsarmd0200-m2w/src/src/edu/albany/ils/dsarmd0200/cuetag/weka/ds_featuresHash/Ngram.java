/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import java.util.ArrayList;

/**
 *
 * @author Laura G.H. Jiao
 */
public class Ngram {

    public static String urlNormalize(String text){

        text = URLReplacer.urlReplacer(text, URLReplacer.urlPattern);
        return text;

    }

    public static String filterUtterance(String text){
        text = text.replace("?", " questionmark ");
        if(text.indexOf("{F ")>=0) {
            text = text.replaceAll("\\{F[\\s][^}]+[}]", "");
        }
        if(text.indexOf("{D ")>=0) {
            text = text.replaceAll("\\{D", "");
        }
        if(text.indexOf("{C")>=0) {
            text = text.replaceAll("\\{C ", "");
        }
        if(text.indexOf("{A")>=0) {
            text = text.replaceAll("\\{A ", "");
        }
        if(text.indexOf("{E")>=0) {
            text = text.replaceAll("\\{E ", "");
        }
        if(text.indexOf("-")>=0) {
            text = text.replaceAll("-", "");
        }
        if(text.indexOf("<")>=0 && text.indexOf(">")>=0) {
            text = text.replaceAll("[<][^>]+[>]", "");
        }
        text = text.replaceAll("$a $m", "am");
        text = text.replaceAll("$p $m", "pm");
        text = text.replaceAll("[^\\w']+", " ").toLowerCase().trim();
        text = text.replaceAll("'cause", "because");
        text = text.replaceAll("'bout", "about");
        // by Laura, Jan 02, 2011
        text = text.replaceAll("lets", "let's");
        text = text.replaceAll("dont", "don't");
        text = text.replaceAll("wont", "won't");
        text = text.replaceAll("doesnt", "doesn't");
        text = text.replaceAll("hasnt", "hasn't");
        text = text.replaceAll("cant", "can't");
        text = text.replaceAll("couldnt", "couldn't");
        text = text.replaceAll("didnt", "didn't");
        text = text.replaceAll("arent", "are not");
        text = text.replaceAll("aint", "not");
        text = text.replaceAll("wouldnt", "wouldn't");
        text = text.replaceAll("nope", "no");
        text = text.replaceAll("yep", "yes");
        text = text.replaceAll("yea", "yeah");

        text = text.replaceAll("\\s+", " ");
        text = text.replace("\'", "\\'");
        return text;
    }


    /**
     * Generate 1-4 n-gram terms from a sentence string
     * @param sentence Sentence string to generate n-gram terms from
     * @return ArrayList of all 1-4 gram terms
     */
    public static ArrayList<String> generateNgramList(String sentence){
        ArrayList<String> ngramList = new ArrayList<String>();
        sentence = "<start> " + sentence.toLowerCase() + " <finish>";
        String[] words = sentence.split("\\s+");
        /* generate 1-4gram */
        for(int i = 0; i < words.length; i++){ // 1-gram
            if(!words[i].equals("<start>") && !words[i].equals("<finish>"))
                ngramList.add(words[i].trim());
        }
        for(int i = 0; i < words.length-1; i++){ // 2-gram
            ngramList.add(words[i].trim() + " " + words[i+1].trim());
        }
        for(int i = 0; i < words.length-2; i++){ // 3-gram
            ngramList.add(words[i].trim() + " " + words[i+1].trim() + " " + words[i+2].trim());
        }
        for(int i = 0; i < words.length-3; i++){ // 4-gram
            ngramList.add(words[i].trim() + " " + words[i+1].trim() + " " + words[i+2].trim() +
                    " " + words[i+3].trim());
        }
        return ngramList;
    }
}
