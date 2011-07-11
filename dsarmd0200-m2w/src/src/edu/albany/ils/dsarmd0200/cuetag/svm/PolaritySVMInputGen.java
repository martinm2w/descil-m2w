/*
 * This class is to generate the input file formats for
 * SVM training and testing files.
 */

package edu.albany.ils.dsarmd0200.cuetag.svm;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.lu.Settings;
import edu.albany.ils.dsarmd0200.util.Util;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Laura G.H. Jiao
 */
public class PolaritySVMInputGen {

    public String folderName = "Polarity/";

    private int tag_num_ = Integer.parseInt(Settings.getValue("tagNum"));

    private ArrayList all_utts_ = new ArrayList(); // list of all the Utterance turn nodes
    private ArrayList tr_utts_ = new ArrayList(); // list of training Utterance turn nodes
    private ArrayList utts_ = new ArrayList(); // list of testing Utterance turn nodes
    private HashMap<String, Integer> term_alias_ = new HashMap<String, Integer>(); // key: ngram term; value: alias num for ngram term
    private HashMap<String, Integer> term_frequency_ = new HashMap<String, Integer>(); // key: ngram term in training set; value: frequency
    private HashMap<String, int[]> tag_frequency_ = new HashMap<String, int[]>(); // key: ngram term in training set; value: [tag1_frequency][tag2_frequency]...

    static int term_frequency_threshold_ = 3;
    static double fraction_threshold_ = 0.325;
    static int testing_term_frequency_min_ = 3;
//    static int testing_term_frequency_max_ = 20;


    /**
     * Constructor
     * @param all_utts_ list of all Utterance turn nodes in the training + testing files
     * @param tr_utts_ list of Utterance turn nodes in the training files
     * @param utts_ list of Utterance turn nodes in the testing files
     */
    public PolaritySVMInputGen(ArrayList all_utts_,
            ArrayList tr_utts_,
            ArrayList utts_) {

        init();
        this.all_utts_ = all_utts_;
        this.tr_utts_ = tr_utts_;
        this.utts_ = utts_;
//        System.err.println("Laura debug: size of all_utts_ = " + all_utts_.size());
//        System.err.println("Larua debug: size of tr_utts_ = " + tr_utts_.size());
//        System.err.println("Laura debug: size of utts_ = " + utts_.size());
    }

    private void init(){
        all_utts_.clear();
        tr_utts_.clear();
        utts_.clear();
        term_alias_.clear();
        term_frequency_.clear();
        tag_frequency_.clear();
    }

    public void generateSVMInputFiles(String trainingPath, String testingPath){

        generateTermAlias(all_utts_);
        calculateTraningFrequency(tr_utts_);
        calculateTrainingTagFrequency(tr_utts_);
        writeToTrainingDat(tr_utts_, trainingPath);
        writeToTestingDat(utts_, testingPath);
        
    }

    /**
     * Generate the alias number used in SVM, for all the ngram terms in the training + testing set data
     * @param all_utts list of all Utterance turn nodes 
     */
    private void generateTermAlias(ArrayList all_utts){

        int alias = 1;
        System.out.println("total number of utterances = " + all_utts.size());
        for(int i = 0; i < all_utts.size(); i++){
            boolean hastopic = false;
            Utterance utterance = (Utterance)all_utts.get(i);
            String topic = utterance.getTopic();
            if(!topic.equals("") && topic != null)
                hastopic = true;
            String utt = utterance.getContent(); // utterance
            utt = Util.filterIt(utt).toLowerCase();
            /* make it string safe */
            utt = utt.replace("\'", "\\\'");
            ArrayList utt_ngram = Ngram_gen(utt, hastopic);
//            ArrayList utt_ngram = Ngram_gen(utt);
            Object[] utt_ngram_array = utt_ngram.toArray();
            for(int j = 0; j < utt_ngram_array.length; j++){
                if(!term_alias_.containsKey(utt_ngram_array[j].toString())){
                    term_alias_.put(utt_ngram_array[j].toString(), alias++);
                }
            }
        }
        System.out.println("total number of ngram = " + term_alias_.size());
        Object[] keySet = term_alias_.keySet().toArray();
        for(int i = 0; i < term_alias_.size(); i++){
            System.err.println(keySet[i] + " -- " + term_alias_.get(keySet[i].toString()) + "&");
        }
    }
    

    /**
     * Generate 1-4 gram terms from a utterance String, append hastopic if there is a topic in this utterance
     * @param utterance a String to generate 1-4 gram terms from.
     * @param hastopic append hastopic if it is true
     * @return ArrayList of the 1-4 gram terms.
     */
    private ArrayList Ngram_gen(String utterance, boolean hastopic){

        String[] words = null;

        utterance = "<start> " + utterance + " <finish>";
        words = utterance.split("\\s+");
        ArrayList Ngram_list = new ArrayList();
        /* generate 1~4gram */
        for(int i = 0; i < words.length; i++){
            if (!words[i].equals("<start>") && !words[i].equals("<finish>")){ // omit the "start" and "end"
                Ngram_list.add(words[i].trim());
            }
        }
        for(int i = 0; i < words.length-1; i++){ // 2-gram
            Ngram_list.add(words[i].trim() + " " + words[i+1].trim());
        }
        for(int i = 0; i < words.length-2; i++){ // 3-gram
            Ngram_list.add(words[i].trim() + " " + words[i+1].trim() + " " + words[i+2].trim());
        }
        for(int i = 0; i < words.length-3; i++){ // 4-gram
            Ngram_list.add(words[i].trim() + " " + words[i+1].trim() + " " + words[i+2].trim() + " " + words[i+3].trim());
        }

        if(hastopic == true)
            Ngram_list.add("hastopic");
        else
            Ngram_list.add("notopic");
        
        return Ngram_list;
    }

    /**
     * Generate 1-4 gram terms from a utterance String.
     * @param utterance a String to generate 1-4 gram terms from.
     * @return ArrayList of the 1-4 gram terms.
     */
    private ArrayList Ngram_gen(String utterance){

        String[] words = null;

        utterance = "<start> " + utterance + " <finish>";
        words = utterance.split("\\s+");
        ArrayList Ngram_list = new ArrayList();
        /* generate 1~4gram */
        for(int i = 0; i < words.length; i++){
            if (!words[i].equals("<start>") && !words[i].equals("<finish>")){ // omit the "start" and "end"
                Ngram_list.add(words[i].trim());
            }
        }
        for(int i = 0; i < words.length-1; i++){ // 2-gram
            Ngram_list.add(words[i].trim() + " " + words[i+1].trim());
        }
        for(int i = 0; i < words.length-2; i++){ // 3-gram
            Ngram_list.add(words[i].trim() + " " + words[i+1].trim() + " " + words[i+2].trim());
        }
        for(int i = 0; i < words.length-3; i++){ // 4-gram
            Ngram_list.add(words[i].trim() + " " + words[i+1].trim() + " " + words[i+2].trim() + " " + words[i+3].trim());
        }

        return Ngram_list;
    }
    
    /**
     * Set up the term frequency with the training set data
     */
    private void calculateTraningFrequency(ArrayList tr_utts){
        
        for(int i = 0; i < tr_utts.size(); i++){
            boolean hastopic = false;
            Utterance utterance = (Utterance)tr_utts.get(i);
            String utt = utterance.getContent(); // utterance
            utt = Util.filterIt(utt).toLowerCase();
            /* make it string safe */
            utt = utt.replace("\'", "\\\'");
            String topic = utterance.getTopic();
            if(!topic.equals("") && topic != null)
                hastopic = true;
            ArrayList utt_ngram = Ngram_gen(utt, hastopic);
//            ArrayList utt_ngram = Ngram_gen(utt);
            Object[] utt_ngram_array = utt_ngram.toArray();
            for(int j = 0; j < utt_ngram_array.length; j++){
                if(term_frequency_.containsKey(utt_ngram_array[j].toString())){
                    int freq = term_frequency_.get(utt_ngram_array[j].toString());
                    term_frequency_.put(utt_ngram_array[j].toString(), freq+1);
                }
                else{
                    term_frequency_.put(utt_ngram_array[j].toString(), Integer.valueOf(1));
                }
            }
        }                
        System.out.println("Size of the term_frequency_ = " + term_frequency_.size());
    }

    /**
     * Set up the tag frequency with training set data
     */
    private void calculateTrainingTagFrequency(ArrayList tr_utts){

        TagNumber tn = new TagNumber();
                
        for(int i = 0; i < tr_utts.size(); i++){
            boolean hastopic = false;
            Utterance utterance = (Utterance)tr_utts.get(i);
            String tag = utterance.getPolarity(); // tag
            int tag_num = tn.tagNumberPolarity(tag);

            String utt = utterance.getContent(); // utterance
            utt = Util.filterIt(utt).toLowerCase();
            /* make it string safe */
            utt = utt.replace("\'", "\\\'");
            String topic = utterance.getTopic();
            if(!topic.equals("") && topic != null)
                hastopic = true;
            ArrayList utt_ngram = Ngram_gen(utt, hastopic);
//            ArrayList utt_ngram = Ngram_gen(utt);
            Object[] utt_ngram_array = utt_ngram.toArray();
            for(int j = 0; j < utt_ngram_array.length; j++){
                if(tag_frequency_.containsKey(utt_ngram_array[j].toString())){
                    int[] tag_f_array = tag_frequency_.get(utt_ngram_array[j].toString());
                    tag_f_array[tag_num-1] += 1;
                    tag_frequency_.put(utt_ngram_array[j].toString(), tag_f_array);
                }
                else{
                    int tagNum = tag_num_;
                    int[] tag_f_array = new int[tagNum+1];
                    tag_f_array[tag_num-1] += 1;
                    tag_frequency_.put(utt_ngram_array[j].toString(), tag_f_array);
                }
            }
        }
        System.out.println("Size of the tag_frequency_ = " + tag_frequency_.size());
    }

     /**
     * generate the training.dat file used by SVM.
     * @param trainingPath
     */
    private void writeToTrainingDat(ArrayList tr_utts, String trainingPath){
        TagNumber tn = new TagNumber();
        int hastopic_alias = term_alias_.get("hastopic");
        int notopic_alias = term_alias_.get("notopic");
        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(trainingPath));

            for(int i = 0; i < tr_utts.size(); i++){
                boolean hastopic = false;
                Utterance utterance = (Utterance)tr_utts.get(i);
                String tag = utterance.getPolarity();
                int tag_num = tn.tagNumberPolarity(tag); // tag alias num
                String utt = utterance.getContent();
                utt = Util.filterIt(utt).toLowerCase();
                /* make it string safe */
                utt = utt.replace("\'", "\\\'");
                String topic = utterance.getTopic();
                if(!topic.equals("") && topic != null)
                    hastopic = true;
                ArrayList utt_ngram = Ngram_gen(utt, hastopic);
//                ArrayList utt_ngram = Ngram_gen(utt);
                Object[] utt_ngram_array = utt_ngram.toArray();
                String line = "";
                HashMap<Integer, String> small_map = new HashMap<Integer, String>();
                for(int j = 0; j < utt_ngram_array.length; j++){
                    int total_freq = (Integer)term_frequency_.get(utt_ngram_array[j].toString());
                    int[] freq = tag_frequency_.get(utt_ngram_array[j].toString());
                    int tag_freq = freq[tag_num-1];
                    double fraction = (double)tag_freq/(double)total_freq;
                    int alias = (Integer)term_alias_.get(utt_ngram_array[j].toString());
                    small_map.put(alias, total_freq+":"+fraction);
                }
                Object[] keySet = small_map.keySet().toArray();
                /* Must be increasing order for SVM input file format */
                Arrays.sort(keySet);
                for(int k = 0; k < keySet.length; k++){
                    String freq_val = small_map.get((Integer)keySet[k]);
                    String[] freq_val2 = freq_val.split(":");
                    double total_freq = Double.parseDouble(freq_val2[0]);
                    double fraction = Double.parseDouble(freq_val2[1]);
                    if((Integer)keySet[k] == hastopic_alias ||
                            (Integer)keySet[k] == notopic_alias){
                        line = line + keySet[k] + ":" + fraction + " ";      
                    }
                    // use else if to avoid repeatly include the "hastopic" and "notopic"
                    else if(total_freq >= term_frequency_threshold_ &&
                            fraction >= fraction_threshold_){
                        line = line + keySet[k] + ":" + fraction + " ";
                    }
                }
                /* For training set, we only use non-blank lines to train; but actually they are the same as blank... */
                if(!line.equals("")){
                    bw.write(tag_num + " " + line);
                    bw.write("\n");
                }
            }
            bw.close();
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    /**
     * Generate the testing.dat file used by the SVM.
     * @param testPath
     */
    private void writeToTestingDat(ArrayList utts, String testPath){
        TagNumber tn = new TagNumber();
        int hastopic_alias = term_alias_.get("hastopic");
        int notopic_alias = term_alias_.get("notopic");
        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(testPath));
            
            for(int i = 0; i < utts.size(); i++){
                boolean hastopic = false;
                Utterance utterance = (Utterance)utts.get(i);
                String tag = utterance.getPolarity();
                int tag_num = tn.tagNumberPolarity(tag);
                String utt = utterance.getContent();
                utt = Util.filterIt(utt).toLowerCase();
                /* make it string safe */
                utt = utt.replace("\'", "\\\'");
                String topic = utterance.getTopic();
                if(!topic.equals("") && topic != null)
                    hastopic = true;
                ArrayList utt_ngram = Ngram_gen(utt, hastopic);
//                ArrayList utt_ngram = Ngram_gen(utt);
                Object[] utt_ngram_array = utt_ngram.toArray();
                String line = "";
                HashMap<Integer, String> small_map = new HashMap<Integer, String>();
                for(int j = 0; j < utt_ngram_array.length; j++){

                    int total_freq = 0;
                    int[] freq = new int[tag_num+1];
                    int tag_freq = 0;
                    double fraction = 0.0;

                    if(term_frequency_.containsKey(utt_ngram_array[j].toString())){ // this term also exits in the training set
                        total_freq = (Integer)term_frequency_.get(utt_ngram_array[j].toString());
                        freq = tag_frequency_.get(utt_ngram_array[j].toString());
                        tag_freq = freq[tag_num-1];
                        fraction = (double)tag_freq/(double)total_freq;
                    }
                    int alias = (Integer)term_alias_.get(utt_ngram_array[j].toString());
                    small_map.put(alias, total_freq+":"+fraction);
                }
                Object[] keySet = small_map.keySet().toArray();
                Arrays.sort(keySet);
                for(int k = 0; k < keySet.length; k++){
                    String freq_val = small_map.get((Integer)keySet[k]);
                    String[] freq_val2 = freq_val.split(":");
                    double total_freq = Double.parseDouble(freq_val2[0]);
                    double fraction = Double.parseDouble(freq_val2[1]);
                    if((Integer)keySet[k] == hastopic_alias ||
                            (Integer)keySet[k] == notopic_alias){
//                        line = line + keySet[k] + ":" + fraction + " ";
                        line = line + keySet[k] + ":" + total_freq + " ";
                    }
                    // use else if to avoid repeatly include the "hastopic" and "notopic"
                    else if(total_freq >= testing_term_frequency_min_
                            /*&& total_freq <= testing_term_frequency_max_*/){
//                        line = line + keySet[k] + ":" + fraction + " ";
                        line = line + keySet[k] + ":" + total_freq + " ";
                    }
                }
                /* For testing set, we have to use all the lines */
                bw.write(tag_num + " " + line);
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
