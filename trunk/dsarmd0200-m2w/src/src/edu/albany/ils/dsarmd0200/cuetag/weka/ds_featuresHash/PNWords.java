
package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

// author Laura G.H. Jiao

import edu.albany.ils.dsarmd0200.lu.Settings;
import java.io.*;
import java.util.*;

public class PNWords {

    public static String P_W = Settings.getValue("pWords");
    public static String N_W = Settings.getValue("nWords");

    public static String POSITIVEWORD = "positiveword";
    public static String NEGATIVEWORD = "negativeword";

    private static HashSet<String> P_WORDS = new HashSet<String>();
    private static HashSet<String> N_WORDS = new HashSet<String>();

    private static HashMap<String, Integer> P_WORDS_APPEARED = new HashMap<String, Integer>(); // positive words appeared and frequency
    private static HashMap<String, Integer> N_WORDS_APPEARED = new HashMap<String, Integer>(); // negative words appeared and frequency
    private static HashMap<String, Integer> P_WORDS_APPEARED_DR = new HashMap<String, Integer>(); // positive words appeared and DR frequency
    private static HashMap<String, Integer> N_WORDS_APPEARED_DR = new HashMap<String, Integer>(); // negative words appeared and DR frequency


    public PNWords(){
        P_WORDS.clear();
        N_WORDS.clear();
        P_WORDS_APPEARED.clear();
        N_WORDS_APPEARED.clear();
        P_WORDS_APPEARED_DR.clear();
        N_WORDS_APPEARED_DR.clear();
        loadPNWords();
    }

    /**
     * Load the positive and negative words
     */
    public static void loadPNWords(){
        BufferedReader br;
        /* load positive words */
        try {
            br = new BufferedReader(new FileReader(P_W));
            String str;
            while((str = br.readLine()) != null){
                String pWord = str.split("\\s+")[0].toLowerCase();
                if(pWord.contains("#")){
                    pWord = pWord.split("#")[0].trim();
                }
                P_WORDS.add(pWord);
            }
            br.close();
//            System.err.println("Number of positive words: " + P_WORDS.size());
//            System.err.println(Arrays.toString(P_WORDS.toArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* load negative words */
        try {
            br = new BufferedReader(new FileReader(N_W));
            String str;
            while((str = br.readLine()) != null){
                String nWord = str.split("\\s+")[0].toLowerCase();
                if(nWord.contains("#")){
                    nWord = nWord.split("#")[0].trim();
                }
                N_WORDS.add(nWord);
            }
            br.close();
//            System.err.println("Number of negative words: " + N_WORDS.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replace word to its polarity
     * @param word word to be replaced
     * @return polarity or itself
     */
    public String replace(String word){
        
        word = word.toLowerCase().trim();
        if(P_WORDS.contains(word)){
            return POSITIVEWORD;
        }
        else if(N_WORDS.contains(word)){
            return NEGATIVEWORD;
        }
        return word;
    }
    

    public String replace(String word, boolean countFreq, String daTag){
        if(countFreq == false){
            return replace(word);
        }
        word = word.toLowerCase().trim();
        if(P_WORDS.contains(word)){
            // total frequency
            if(P_WORDS_APPEARED.containsKey(word)){ 
                int currf = P_WORDS_APPEARED.get(word);
                P_WORDS_APPEARED.put(word, Integer.valueOf(currf+1));
            }
            else{
                P_WORDS_APPEARED.put(word, Integer.valueOf(1));
            }
            // DR frequency
            if(daTag.equals(DsarmdDATag.DR)){
                if(P_WORDS_APPEARED_DR.containsKey(word)){
                    int currf = P_WORDS_APPEARED_DR.get(word);
                    P_WORDS_APPEARED_DR.put(word, Integer.valueOf(currf+1));
                }
                else{
                    P_WORDS_APPEARED_DR.put(word, Integer.valueOf(1));
                }
            }
            return POSITIVEWORD;
        }
        else if(N_WORDS.contains(word)){
            // total frequency
            if(N_WORDS_APPEARED.containsKey(word)){
                int currf = N_WORDS_APPEARED.get(word);
                N_WORDS_APPEARED.put(word, Integer.valueOf(currf+1));
            }
            else{
                N_WORDS_APPEARED.put(word, Integer.valueOf(1));
            }
            // DR frequency
            if(daTag.equals(DsarmdDATag.DR)){
                if(N_WORDS_APPEARED_DR.containsKey(word)){
                    int currf = N_WORDS_APPEARED_DR.get(word);
                    N_WORDS_APPEARED_DR.put(word, Integer.valueOf(currf+1));
                }
                else{
                    N_WORDS_APPEARED_DR.put(word, Integer.valueOf(1));
                }
            }
            return NEGATIVEWORD;
        }
        return word;
    }

    public String replaceSentence(String sentence){
        
        String[] words = sentence.split("\\s+");
        sentence = "";
        for(String s : words){
            sentence += replace(s) + " ";
        }
        return sentence;
    }

    public String replaceSentence(String sentence, boolean countFreq, String daTag){

        String[] words = sentence.split("\\s+");
        sentence = "";
        for(String s : words){

            sentence += replace(s, countFreq, daTag) + " ";
        }
        return sentence;
    }

//    public static void main(String[] args){
//        String sentence = "hello abolish my name accord is laura";
//        loadPNWords();
//        System.out.println(replaceSentence(sentence));
//    }


    public String printPWordsFreq(){
        StringBuilder sb = new StringBuilder();
        Set<String> pwords = P_WORDS_APPEARED.keySet();
        for(String s : pwords){
            Integer totalFreq = P_WORDS_APPEARED.get(s);
            Integer DRFreq = P_WORDS_APPEARED_DR.get(s);
            if(DRFreq == null){
                DRFreq = 0;
            }
            sb.append(s).append(", total frequency: ").append(totalFreq).append(""
                    + "  DR tag frequency: ").append(DRFreq).append("\n");
        }
        return sb.toString();
    }
    
    public String printNWordsFreq(){
        StringBuilder sb = new StringBuilder();
        Set<String> nwords = N_WORDS_APPEARED.keySet();
        for(String s : nwords){
            Integer totalFreq = N_WORDS_APPEARED.get(s);
            Integer DRFreq = N_WORDS_APPEARED_DR.get(s);
            if(DRFreq == null){
                DRFreq = 0;
            }
            sb.append(s).append(", total frequency: ").append(totalFreq).append(""
                    + "  DR tag frequency: ").append(DRFreq).append("\n");
        }
        return sb.toString();
    }

}
