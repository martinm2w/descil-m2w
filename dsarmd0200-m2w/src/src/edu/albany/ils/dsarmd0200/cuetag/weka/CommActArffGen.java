/*
 * This class is used to generate arff files for weka's input format
 * training input file, and testing input file
 * Those two files should go through batch filtering before can be used by weka's
 * training + supplied test set
 */

package edu.albany.ils.dsarmd0200.cuetag.weka;

import edu.albany.ils.dsarmd0200.cuetag.svm.TagNumber;
import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.lu.CommunicationLink;
import edu.albany.ils.dsarmd0200.lu.Settings;
import edu.albany.ils.dsarmd0200.util.Util;
import edu.albany.ils.dsarmd0200.util.Wordnet;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author gjiao
 */
public class CommActArffGen {

    public String foldName = "";

    private ArrayList all_utts_ = new ArrayList(); // list of all the Utterance turn nodes
    private ArrayList tr_utts_ = new ArrayList(); // list of training Utterance turn nodes
    private ArrayList utts_ = new ArrayList(); // list of testing Utterance turn nodes
    private Wordnet wn_ = null;
    private HashMap parts_ = null;
    private CommunicationLink cl;
    
    private HashSet<String>tag_no_ = new HashSet(); // key: tag;
    private HashSet speakers_ = new HashSet(); // list of speaker names
    private HashMap<String, Integer> term_frequency_ = new HashMap<String, Integer>(); // key: ngram term in training set; value: frequency
    private HashMap<String, int[]> tag_frequency_ = new HashMap<String, int[]>(); // key: ngram term in training set; value: [tag1_frequency][tag2_frequency]...

    static int term_frequency_threshold_ = 3;
    static double fraction_threshold_ = 0.325;
    static int testing_term_frequency_min_ = 3;

    /**
     * Constructor
     * @param all_utts list of all Utterance turn nodes in the training + testing files
     * @param tr_utts list of Utterance turn nodes in the training files
     * @param utts list of Utterance turn nodes in the testing files
     */
    public CommActArffGen(ArrayList all_utts,
			  ArrayList tr_utts,
			  ArrayList utts, 
			  HashMap parts,
			  Wordnet wn){
        init();
        this.all_utts_ = all_utts;
        this.tr_utts_ = tr_utts;
        this.utts_ = utts;
	parts_ = parts;
	wn_ = wn;
	/*
        System.err.println("Laura debug: size of all_utts_ = " + all_utts_.size() + "\n" +
                "size of tr_utts_ = " + tr_utts_.size() + "\n" +
                "size of utts_ = " + utts_.size());
	*/
    }

    public void generateArffFile(String trainingPath, String testingPath){
        heading(all_utts_, tr_utts_, utts_, parts_);
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
        term_frequency_.clear();
        tag_frequency_.clear();
        speakers_.clear();
    }


    /**
     * Collect all fisrt words for "continuation-of" tagging
     * Save the heading classes for arff file
     * Save all the speaker names of the training and testing files
     * @param tr_utts_ list of Utterance turn nodes in the training files
     * @param utts_ list of Utterance turn nodes in the testing files
     */
    private void heading(ArrayList all_utts_, ArrayList tr_utts_, ArrayList utts_, HashMap parts){
        cl = new CommunicationLink(all_utts_, tr_utts_, wn_, utts_, parts);
        cl.setUtts(utts_);
        //cl.collectCLFts();
        cl.calCLFts();
	//System.out.println("utts:\n" + utts_);
        
        for(int i = 0;  i < tr_utts_.size(); i++){
            Utterance utterance = (Utterance)all_utts_.get(i);
            String speaker = utterance.getSpeaker().toLowerCase().trim(); // speaker name
            speakers_.add(speaker);
            String tag = utterance.getCommActType().toLowerCase().trim();
            if(tag == null || tag.equals("")){
                tag = "unknown";
            }
            tag_no_.add(tag);
        }

        //System.out.println("Number of tags = " + tag_no_.size());
        //System.out.println("Tags: " + Arrays.toString(tag_no_.toArray()));
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
        for(int i = 0; i < tr_utts.size(); i++){
            Utterance utterance = (Utterance)tr_utts.get(i); // Utterance turn node
            String utt = utterance.getContent(); // utterance content
            utt = Util.filterIt(utt).toLowerCase();
            /* make it string safe */
            utt = utt.replace("\'", "\\\'");
            for(int k = 0; k < speakers_.size(); k++){
                String speakerName = speakers_.toArray()[k].toString();
                if(utt.contains(" " + speakerName + " "))
                    utt = utt.replace(speakerName, "<speaker>");
            }
            ArrayList utt_ngram = Ngram_gen(utt);
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
        //System.out.println("Size of the term_frequency_ = " + term_frequency_.size());
    }

    /**
     * Set up ngram's tag frequency with the training set data
     * @param tr_utts list of traininig Utterance turn nodes
     */
    private void calculateTrainingTagFrequency(ArrayList tr_utts){
        TagNumber tn = new TagNumber();

        for(int i = 0; i < tr_utts.size(); i++){
            Utterance utterance = (Utterance)tr_utts.get(i); // Utterance turn node
            String tag = utterance.getCommActType(); // utterance comm_act_type tag
            int tag_num = tn.tagNumberCommAct(tag.toLowerCase());
            String utt = utterance.getContent(); // utterance content
            utt = Util.filterIt(utt).toLowerCase();
            /* make it string safe */
            utt = utt.replace("\'", "\\\'");
            for(int k = 0; k < speakers_.size(); k++){
                String speakerName = speakers_.toArray()[k].toString();
                if(utt.contains(" " + speakerName + " "))
                    utt = utt.replace(speakerName, "<speaker>");
            }
            ArrayList utt_ngram = Ngram_gen(utt);
            Object[] utt_ngram_array = utt_ngram.toArray();
            for(int j = 0; j < utt_ngram_array.length; j++){
                if(tag_frequency_.containsKey(utt_ngram_array[j].toString())){
                    int[] tag_f_array = tag_frequency_.get(utt_ngram_array[j].toString());
                    tag_f_array[tag_num-1] += 1;
                    tag_frequency_.put(utt_ngram_array[j].toString(), tag_f_array);
                }
                else{
                    int tagNum = Integer.parseInt(Settings.getValue("tagNum"));
                    int[] tag_f_array = new int[tagNum+1];
                    tag_f_array[tag_num-1] += 1;
                    tag_frequency_.put(utt_ngram_array[j].toString(), tag_f_array);
                }
            }
        }
        //System.out.println("Size of the tag_frequency_ = " + tag_frequency_.size());
    }


    /**
     * generate the training.arff file used as weka input
     * @param trainingPath output arff file path for training
     */
    private void writeToTrainingArff(String trainingPath){

        TagNumber tn = new TagNumber();
        Object[] tag_no_keySet_array = tag_no_.toArray();

        BufferedWriter bw;

        try {
            bw = new BufferedWriter(new FileWriter(trainingPath));

            bw.write("@RELATION " + trainingPath + ".arff\n\n");
            bw.write("@ATTRIBUTE tag { ");

	    String unknown = "unknown";
	    boolean has_ukn = false;
            for(int i = 0; i < tag_no_keySet_array.length; i++){
                bw.write(tag_no_keySet_array[i] + ", ");
		if (unknown.equals(tag_no_keySet_array[i])) {
		    has_ukn = true;
		}
            }
	    if (!has_ukn) {
		bw.write(unknown + ",");
	    }
            bw.write("}\n");
            bw.write("@ATTRIBUTE utterance string\n");
            bw.write("\n@DATA\n");

            for(int i = 0; i < tr_utts_.size(); i++){
                Utterance utterance = (Utterance)tr_utts_.get(i);
                String tag = utterance.getCommActType(); // comm_act tag
                tag = tag.toLowerCase().trim();
                String daTag = utterance.getTag().toLowerCase().trim(); // dialog act tag
                if(tag.equals("") || tag == null)
                    tag = "unknown";
                String line = "";
                if(tag.equals("continuation-of")){
                    line += "continuation-of";
                }
                else if(daTag.contains("acknowledge") ||
                        daTag.contains("response-answer") ||
                        daTag.contains("response-non-answer") ||
                        daTag.contains("agree-accept") ||
                        daTag.contains("disagree-reject") ||
                        daTag.contains("signal-non-understanding")){
                    line += "response-to";
                }
                else{ // not continuation-of, not response-to da tag, use ngram to training set
                    int tagNum = tn.tagNumberCommAct(tag.toLowerCase());
                    String utt = utterance.getContent(); // utterance content
                    utt = Util.filterIt(utt).toLowerCase();
                    utt = utt.replace("\'", "\\\'");
                    for(int k = 0; k < speakers_.size(); k++){
                        String speakerName = speakers_.toArray()[k].toString();
                        if(utt.contains(" " + speakerName + " "))
                            utt = utt.replace(speakerName, "<speaker>");
                    }
                    ArrayList utt_ngram_terms = Ngram_gen(utt);
                    Object[] ngram = utt_ngram_terms.toArray();

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
        Object[] tag_no_keySet_array = tag_no_.toArray();

        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(testingPath));

	    String unknown = "unknown";
	    boolean has_ukn = false;
            bw.write("@RELATION " + testingPath + ".arff\n\n");
            bw.write("@ATTRIBUTE tag { ");

            for(int i = 0; i < tag_no_keySet_array.length; i++){
                bw.write(tag_no_keySet_array[i] + ", ");
		if (unknown.equals(tag_no_keySet_array[i])) {
		    has_ukn = true;
		}
            }
	    if (!has_ukn) {
		bw.write(unknown + ",");
	    }
            bw.write("}\n");
            bw.write("@ATTRIBUTE utterance string\n");
            bw.write("\n@DATA\n");

            for(int i = 0; i < utts_.size(); i++){
                Utterance utterance = (Utterance)utts_.get(i);
                String tag = utterance.getCommActType();
                tag = tag.toLowerCase().trim();
                String daTag = "";//utterance.getTag().toLowerCase().trim();
                if(tag.equals("") || tag == null)
                    tag = "unknown";
                int tagNum = tn.tagNumberCommAct(tag.toLowerCase());
                String utt = utterance.getContent();
                String[] utt_array = utt.split("\\s+");
		if (utt_array == null || utt_array.length == 0) continue;
                String firstword = utt_array[0].toLowerCase();
                utt = Util.filterIt(utt).toLowerCase();
                utt = utt.replace("\'", "\\\'");

                for(int k = 0; k < speakers_.size(); k++){
                    String speakerName = speakers_.toArray()[k].toString();
                    if(utt.contains(" " + speakerName + " "))
                        utt = utt.replace(speakerName, "<speaker>");
                }
                ArrayList utt_ngram_terms = Ngram_gen(utt);
                Object[] ngram = utt_ngram_terms.toArray();
                String line = "";
                
                if(cl.isCont(utterance, i, firstword)){
//                    System.err.println("continuation-of: true");
                    line += "continuation-of";
                }
                else if(daTag.contains("acknowledge") ||
                        daTag.contains("response-answer") ||
                        daTag.contains("response-non-answer") ||
                        daTag.contains("agree-accept") ||
                        daTag.contains("disagree-reject") ||
                        daTag.contains("signal-non-understanding")){
//                    System.err.println("response-to: true");
                    line += "response-to";
                }
                else{ // not tagged to "continuation-of"
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

                            if(total_freq >= testing_term_frequency_min_)
    //                        if(total_freq >= testing_term_frequency_min_ &&
    //                                fraction >= 0.325)
                                line += "|" + ngram[j];
                        }
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
