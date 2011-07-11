/*
 * This class is used to generate arff files for weka's input format
 * training input file, and testing input file
 * Those two files should go through batch filtering before can be used by weka's
 * training + supplied test set
 */

package edu.albany.ils.dsarmd0200.cuetag.weka;

import edu.albany.ils.dsarmd0200.cuetag.svm.TagNumber;
import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.util.Util;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author gjiao
 */
public class PolarityArffGen {

    public String folderName = "Polarity/";

    private ArrayList all_utts_ = new ArrayList(); // list of all the Utterance turn nodes
    private ArrayList tr_utts_ = new ArrayList(); // list of training Utterance turn nodes
    private ArrayList utts_ = new ArrayList(); // list of testing Utterance turn nodes

    private HashMap<String, Integer> tag_no_ = new HashMap(); // key: tag; value: increment number
    private HashMap<Integer, String> tag_no_reverse_ = new HashMap(); // reversed hashmap of above
    private HashMap<String, Integer> term_frequency_ = new HashMap<String, Integer>(); // key: ngram term in training set; value: frequency
    private HashMap<String, int[]> tag_frequency_ = new HashMap<String, int[]>(); // key: ngram term in training set; value: [tag1_frequency][tag2_frequency]...

    static int term_frequency_threshold_ = 4;
    static double fraction_threshold_ = 0.325;
    static int testing_term_frequency_min_ = 4;

    /**
     * Constructor
     * @param all_utts list of all Utterance turn nodes in the training + testing files
     * @param tr_utts list of Utterance turn nodes in the training files
     * @param utts list of Utterance turn nodes in the testing files
     */
    public PolarityArffGen(ArrayList all_utts, 
            ArrayList tr_utts,
            ArrayList utts){
        init();
        this.all_utts_ = all_utts;
        this.tr_utts_ = tr_utts;
        this.utts_ = utts;
    }

    public void generateArffFile(String trainingPath, String testingPath){
        calculateTrainingFrequency(tr_utts_);
        calculateTrainingTagFrequency(tr_utts_);
        writeToTrainingArff(trainingPath);
        writeToTestingArff(testingPath);
    }

    private void init(){
        all_utts_.clear();
        tr_utts_.clear();
        utts_.clear();
        tag_no_.clear();
        tag_no_reverse_.clear();
        term_frequency_.clear();
        tag_frequency_.clear();
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
     * Generate list of 1-4 ngram terms from a utterance String
     * @param utterance a String to generate ngram from
     * @return a list of 1-4 ngram terms
     */
    private ArrayList Ngram_gen(String utterance){
        String[] words = null;
        utterance = "<start> " + utterance + " <finish>";
        words = utterance.split("\\s+");
        ArrayList Ngram_list = new ArrayList();
        /* generate 1-4gram */
        for(int i = 0; i < words.length; i++){ // 1-gram
            if(!words[i].equals("<start>") && !words[i].equals("<finish>"))
                Ngram_list.add(words[i].trim());            
        }
        for(int i = 0; i < words.length-1; i++) // 2-gram
            Ngram_list.add(words[i].trim() + " " + words[i+1].trim());
        for(int i = 0; i < words.length-2; i++) // 3-gram
            Ngram_list.add(words[i].trim() + " " + words[i+1].trim() + " " + words[i+2].trim());
        for(int i = 0; i < words.length-3; i++) // 4-gram
            Ngram_list.add(words[i].trim() + " " + words[i+1].trim() + " " + words[i+2].trim() + " " + words[i+3].trim());
                
        return Ngram_list;
    }

    /**
     * Set up ngram's term frequency with the training set data
     * @param tr_utts list of traininig Utterance turn nodes
     */
    private void calculateTrainingFrequency(ArrayList tr_utts){
        int increment = 1;
        for(int i = 0; i < tr_utts.size(); i++){
            boolean hastopic = false;
            Utterance utterance = (Utterance)tr_utts.get(i); // Utterance turn node
            String tag = utterance.getPolarity(); // utterance polarity
            if(tag.equals(""))
                tag = "neutral";
            if(!tag_no_.containsKey(tag)){
                tag_no_.put(tag, increment);
                tag_no_reverse_.put(increment++, tag);
            }
            String utt = utterance.getContent(); // utterance content
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
     * Set up ngram's tag frequency with the training set data
     * @param tr_utts list of traininig Utterance turn nodes
     */
    private void calculateTrainingTagFrequency(ArrayList tr_utts){
        TagNumber tn = new TagNumber();
        
        for(int i = 0; i < tr_utts.size(); i++){
            boolean hastopic = false;
            Utterance utterance = (Utterance)tr_utts.get(i); // Utterance turn node
            String tag = utterance.getPolarity(); // utterance polarity tag
            int tag_num = tn.tagNumberPolarity(tag);
            String utt = utterance.getContent(); // utterance content
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
//                    int tagNum = Integer.parseInt(Settings.getValue("tagNum"));
                    int tagNum = tag_no_.size();
                    int[] tag_f_array = new int[tagNum+1];
                    tag_f_array[tag_num-1] += 1;
                    tag_frequency_.put(utt_ngram_array[j].toString(), tag_f_array);
                }
            }
        }
        System.out.println("Number of tags = " + tag_no_.size());
        System.out.println("Size of the tag_frequency_ = " + tag_frequency_.size());
    }


    /**
     * generate the training.arff file used as weka input
     * @param trainingPath output arff file path for training
     */
    private void writeToTrainingArff(String trainingPath){
        
        TagNumber tn = new TagNumber();
        Object[] tag_no_keySet_array = tag_no_.keySet().toArray();

        BufferedWriter bw;

        try {
            bw = new BufferedWriter(new FileWriter(trainingPath));
            
            bw.write("@RELATION " + trainingPath + ".arff\n");
            bw.write("@ATTRIBUTE tag { ");

            for(int i = 0; i < tag_no_keySet_array.length; i++){
                bw.write(tag_no_keySet_array[i] + ", ");            
            }
            bw.write("}\n");
            bw.write("@ATTRIBUTE utterance string\n");
            bw.write("\n@DATA\n");

            for(int i = 0; i < tr_utts_.size(); i++){
                boolean hastopic = false;
                Utterance utterance = (Utterance)tr_utts_.get(i);
                String tag = utterance.getPolarity(); // polarity tag
                if(tag.equals(""))
                    tag = "neutral";
                int tagNum = tn.tagNumberPolarity(tag);
                String utt = utterance.getContent(); // utterance content
                utt = Util.filterIt(utt).toLowerCase();
                utt = utt.replace("\'", "\\\'");
                String topic = utterance.getTopic();
                if(!topic.equals("") && topic != null)
                    hastopic = true;
                ArrayList utt_ngram_terms = Ngram_gen(utt, hastopic);
//                ArrayList utt_ngram_terms = Ngram_gen(utt);
                Object[] ngram = utt_ngram_terms.toArray();
                String line = "";
                for(int j = 0; j < ngram.length; j++){
                    int total_freq = (Integer)term_frequency_.get(ngram[j].toString());
                    int[] freq = tag_frequency_.get(ngram[j].toString());
                    int tag_freq = freq[tagNum-1];
                    double fraction = (double)tag_freq/(double)total_freq;
                    if(ngram[j].toString().equals("hastopic") ||
                            ngram[j].toString().equals("notopic")){
                        line += "|" + ngram[j];
                    }
                    // use else if to avoid repeatly include the "hastopic" and "notopic"
                    else if(total_freq >= term_frequency_threshold_ &&
                            fraction >= fraction_threshold_){
                        line += "|" + ngram[j];
                    }
                }

                /* For training set, we only use non-blank lines to train, but actually they are the same of using blank lines too... */
                if(!line.equals(""))
                    bw.write(tag + ", '" + line + "'\n");
            }
            
            bw.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * generate the testing.arff file used as weka input
     * @param testingPath output arff file path for testing
     */
    private void writeToTestingArff(String testingPath){
        
        TagNumber tn = new TagNumber();
        Object[] tag_no_keySet_array = tag_no_.keySet().toArray();

        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(testingPath));

            bw.write("@RELATION " + testingPath + ".arff\n\n");
            bw.write("@ATTRIBUTE tag { ");

            for(int i = 0; i < tag_no_keySet_array.length; i++){
                bw.write(tag_no_keySet_array[i] + ", ");
            }
            bw.write("}\n");
            bw.write("@ATTRIBUTE utterance string\n");
            bw.write("\n@DATA\n");

            for(int i = 0; i < utts_.size(); i++){
                boolean hastopic = false;
                Utterance utterance = (Utterance)utts_.get(i);
                String tag = utterance.getPolarity();
                if(tag.equals(""))
                    tag = "neutral";
                int tagNum = tn.tagNumberPolarity(tag);
                String utt = utterance.getContent();
                utt = Util.filterIt(utt).toLowerCase();
                utt = utt.replace("\'", "\\\'");
                String topic = utterance.getTopic();
                if(!topic.equals("") && topic != null)
                    hastopic = true;
                ArrayList utt_ngram_terms = Ngram_gen(utt, hastopic);
//                ArrayList utt_ngram_terms = Ngram_gen(utt);
                Object[] ngram = utt_ngram_terms.toArray();
                String line = "";
                
                for(int j = 0; j < ngram.length; j++){

                    int total_freq = 0;
                    int[] freq = new int[tag_no_keySet_array.length];
                    int tag_freq = 0;
                    double fraction = 0.0;

                    if(term_frequency_.containsKey(ngram[j].toString())){ // this term exists in the training data set
                        total_freq = (Integer)term_frequency_.get(ngram[j].toString());
                        freq = tag_frequency_.get(ngram[j].toString());
                        tag_freq = freq[tagNum-1];
                        fraction = (double)tag_freq/(double)total_freq;
                        if(ngram[j].toString().equals("hastopic") ||
                                ngram[j].toString().equals("notopic")){
                            line += "|" + ngram[j];
                        }
                        // use else if to avoid repeatly include the "hastopic" and "notopic"
                        else if(total_freq >= testing_term_frequency_min_)
                            line += "|" + ngram[j];
                    }                    
                }
                /* For testing set, we need to use all the lines, unless its bootstrapping */
                if(!line.equals("")){
                    bw.write(tag + ", '" + line + "'\n");
                }
                else{
                    bw.write(tag + ", \'\'\n");
                }
            }

            bw.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
