/*
 * This class is to evaluate training and testing corpus
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Laura G.H. Jiao
 */
public class CorpusAnalysis {

    protected ArrayList all_utts = new ArrayList(); // list of all Utterances
    protected ArrayList utts = new ArrayList();
    protected ArrayList tr_utts = new ArrayList();

    protected HashSet tagList = new HashSet(); // set of all the tags in the corpus
    
    protected HashMap<String, Integer> tr_utts_tagQuantity = new HashMap<String, Integer>(); // tag quantity in the training set, key: tag; value: quantity
    protected HashMap<String, Double> tr_utts_tagPercentage = new HashMap<String, Double>(); // tag percentage in the training set
    protected HashMap<String, Integer> utts_tagQuantity = new HashMap<String, Integer>(); // tag quantity in the testing set
    protected HashMap<String, Double> utts_tagPercentage = new HashMap<String, Double>(); // tag precentage in the testing set
    protected HashMap<String, Integer> all_utts_tagQuantity = new HashMap<String, Integer>(); // tag quantity in the corpus
    protected HashMap<String, Double> all_utts_tagPercentage = new HashMap<String, Double>(); // tag percentage in the corpus


    protected HashMap<String, Integer> tr_utts_termFrequency = new HashMap<String, Integer>(); // key: ngram term; value: term frequency
    protected HashMap<String, Integer> utts_termFrequancy = new HashMap<String, Integer>();
    
    protected HashMap<String, int[]> tr_utts_termTagFrequency = new HashMap<String, int[]>(); // key: ngram term; value: [tag1_freq][tag2_freq]...

    protected HashMap<String, ArrayList> featuresMap = new HashMap<String, ArrayList>(); // key: tag; value: {features...}
    protected ArrayList<String> allFeatures = new ArrayList<String>(); // all the features in the corpus

//    private PNWords pnw = new PNWords();

    public CorpusAnalysis(ArrayList all_utts, ArrayList utts, ArrayList tr_utts){
        this.all_utts = all_utts;
        this.utts = utts;
        this.tr_utts = tr_utts;
    }

    public CorpusAnalysis(){}

    public void runAnalysis(String tagType){
        tagAnalyze(tagType);
        ngramTermAnalyze(tagType);
    }

    /**
     * Set tagQuantity and tagPercentage in corpus, training set, and testing set
     * @param tagType Type of the tag, such as "da15", "da3", and so on
     */
    protected void tagAnalyze(String tagType){
        for(int i = 0; i < all_utts.size(); i++){ // for all_utts
            Utterance utterance = (Utterance)all_utts.get(i);
            String tag = "";
            if(tagType.equals("da15")){
                tag = utterance.getTag().toLowerCase().trim();
                tag = tag.replace("--", "");
            }
            else if(tagType.equals("da3"))
                tag = utterance.getMTag().toLowerCase().trim();
            else{
                System.err.println("unrecognized tag type");
                return;
            }
//            // Laura debug:
//            if(tag.toLowerCase().contains("disagree-reject")){
//                System.out.println(utterance.toString());
//            }
            if(tag == null || tag.equals("")) 
                    tag = "unknown";
            /* get all tags */
            tagList.add(tag);
            /* get tag quantities */
            if(all_utts_tagQuantity.containsKey(tag)){
                all_utts_tagQuantity.put(tag, Integer.valueOf(all_utts_tagQuantity.get(tag)+1));
            }
            else{
                all_utts_tagQuantity.put(tag, Integer.valueOf(1));
            }
        }
//        System.out.println("\n****************** Corpus: *****************" +
//                "\nTag quantity of corpus: " + all_utts_tagQuantity.toString() +
//                "\n*****************************************************");
        /* get tag percentages */
        for(int i = 0; i < all_utts_tagQuantity.size(); i++){
            String tag = (String)all_utts_tagQuantity.keySet().toArray()[i];
            int tagQuantity = all_utts_tagQuantity.get(tag);
            double tagPercentage = (double)tagQuantity/(double)all_utts.size();
            all_utts_tagPercentage.put(tag, tagPercentage);
        }
//        System.out.println("Tag percentage of corpus: " + all_utts_tagPercentage.toString() +
//                "\n******************************************************");

        for(int i = 0; i < tr_utts.size(); i++){ // for tr_utts
            Utterance utterance = (Utterance)tr_utts.get(i);
            String tag = "";
            if(tagType.equals("da15")){
                tag = utterance.getTag().toLowerCase().trim();
                tag = tag.replace("--", "");
            }
            else if(tagType.equals("da3"))
                tag = utterance.getMTag().toLowerCase().trim();
            else{
                System.err.println("unrecognized tag type");
                return;
            }
            if(tag == null || tag.equals("")) 
                    tag = "unknown";
            /* get tag quantities */
            if(tr_utts_tagQuantity.containsKey(tag)){
                tr_utts_tagQuantity.put(tag, Integer.valueOf(tr_utts_tagQuantity.get(tag)+1));
            }
            else{
                tr_utts_tagQuantity.put(tag, Integer.valueOf(1));
            }
        }
        assert(true):"\n****************** Training set: *****************" +
                "\nTag quantity of training set: " + tr_utts_tagQuantity.toString() +
                "\n****************************************************";
        /* get tag percentages */
        for(int i = 0; i < tr_utts_tagQuantity.size(); i++){
            String tag = (String)tr_utts_tagQuantity.keySet().toArray()[i];
            int tagQuantity = tr_utts_tagQuantity.get(tag);
            double tagPercentage = (double)tagQuantity/(double)tr_utts.size();
            tr_utts_tagPercentage.put(tag, tagPercentage);
        }
        assert(true):"Tag percentage of training set: " + tr_utts_tagPercentage.toString() +
                "\n*****************************************************";

        for(int i = 0; i < utts.size(); i++){ // for utts
            Utterance utterance = (Utterance)utts.get(i);
            String tag = "";
            if(tagType.equals("da15")){
                tag = utterance.getTag().toLowerCase().trim();
                tag = tag.replace("--", "");
            }
            else if(tagType.equals("da3"))
                tag = utterance.getMTag().toLowerCase().trim();
            else{
                System.err.println("unrecognized tag type");
                return;
            }
            if(tag == null || tag.equals("")) 
                    tag = "unknown";
            /* get tag quantities */
            if(utts_tagQuantity.containsKey(tag)){
                utts_tagQuantity.put(tag, Integer.valueOf(utts_tagQuantity.get(tag)+1));
            }
            else{
                utts_tagQuantity.put(tag, Integer.valueOf(1));
            }
        }
        assert(true):"\n****************** Testing set: *****************" +
                "\nTag quantity of testing set: " + utts_tagQuantity.toString() +
                "\n********************************************************";
        /* get tag percentages */
        for(int i = 0; i < utts_tagQuantity.size(); i++){
            String tag = (String)utts_tagQuantity.keySet().toArray()[i];
            int tagQuantity = utts_tagQuantity.get(tag);
            double tagPercentage = (double)tagQuantity/(double)utts.size();
            utts_tagPercentage.put(tag, tagPercentage);
        }
        assert(true):"Tag percentage of testing set: " + utts_tagPercentage.toString() +
                "\n********************************************************";
        
    }

    /**
     * Set training term frequency map; Set training tagF/tf map;
     * @param tagType Type of the tag, such as "da15", "da3", and so on
     */
    protected void ngramTermAnalyze(String tagType){
        /* get term frequency */
        for(int i = 0; i < tr_utts.size(); i++){ // for tr_utts
            Utterance utterance = (Utterance)tr_utts.get(i);
            String daTag = "";
            if(tagType.equals("da15")){
                daTag = utterance.getTag().toLowerCase().trim();
                daTag = daTag.replace("--", "");
            }
            else if(tagType.equals("da3")){
                daTag = utterance.getMTag().toLowerCase().trim();
            }
            else{
                System.err.println("unrecognized tag type");
                return;
            }
            if(daTag == null || daTag.equals("")) 
                    daTag = "unknown";
            String content = utterance.getContent();

            
            content = Ngram.urlNormalize(content);
            content = Ngram.filterUtterance(content);
            
//            // by Laura Dec 07, 2010
//            content = pnw.replaceSentence(content);
            
            ArrayList<String> terms = Ngram.generateNgramList(content);


//            // by Laura Jan 04, 2011
            if(daTag.toLowerCase().contains(DsarmdDATag.AD)){ // Action-Directive
                terms.add(DsarmdDATag.AD);
//                System.err.println(Arrays.toString(terms.toArray()));
            }
             // by Laura Jan 25, 2011
            if(daTag.toLowerCase().contains(DsarmdDATag.AD)){ // Disagree-Reject
                terms.add(DsarmdDATag.DR);
            }

            for(int j = 0; j < terms.size(); j++){
                if(tr_utts_termFrequency.containsKey(terms.get(j))){
                    tr_utts_termFrequency.put(terms.get(j), Integer.valueOf(tr_utts_termFrequency.get(terms.get(j))+1));
                }
                else{
                    tr_utts_termFrequency.put(terms.get(j), Integer.valueOf(1));
                }
            }
        }
        assert(true):"\n******************** Training set terms **********************" +
                "\nSize of terms in training set: " + tr_utts_termFrequency.size() +
                "\n********************************************************";
        for(int i = 0; i < utts.size(); i++){ // for utts
            Utterance utterance = (Utterance)utts.get(i);

            String content = utterance.getContent();

            content = Ngram.urlNormalize(content);
            content = Ngram.filterUtterance(content);


//            // by Laura Dec 07, 2010
//            content = pnw.replaceSentence(content, true, daTag);

            ArrayList<String> terms = Ngram.generateNgramList(content);
            for(int j = 0; j < terms.size(); j++){
                if(utts_termFrequancy.containsKey(terms.get(j))){
                    utts_termFrequancy.put(terms.get(j), Integer.valueOf(utts_termFrequancy.get(terms.get(j))+1));
                }
                else{
                    utts_termFrequancy.put(terms.get(j), Integer.valueOf(1));
                }
            }
        }
        assert(true):"\n******************* Testing set terms *************************" +
                "\nSize of terms in testing set: " + utts_termFrequancy.size() +
                "\n**********************************************************";

//        System.out.println("\n********************* Positive words in testing set ****************************"
//                + "\n" + pnw.printPWordsFreq() + "******************* End of positive words in testing set "
//                + "*******************\n");
//        System.out.println("\n********************* Negative words in testing set ****************************"
//                + "\n" + pnw.printNWordsFreq() + "******************* End of negative words in testing set "
//                + "*******************\n");

         /* set tagF/tf map */
        int tagNum = 0;
        if(tagType.equals("da15")){
            tagNum = 15;
        }
        else if(tagType.equals("da3")){
            tagNum = 3;
        }
        else{
            System.err.println("unrecognized tag type");
            return;
        }

        for(int i = 0; i < tr_utts.size(); i++){ // tr_utts
            Utterance utterance = (Utterance)tr_utts.get(i);
            String daTag = "";
            if(tagNum == 15){
                daTag = utterance.getTag();
                daTag = daTag.replace("--", "");
            }
            else if(tagNum == 3){
                daTag = utterance.getMTag();
            }
            else{
                System.err.println("unrecognized tag type");
                return;
            }
            if(daTag == null || daTag.equals("")) 
                    daTag = "unknown";
            int tagIndex = DsarmdDATag.getTagAliasNum(daTag, tagNum) - 1;
            String content = utterance.getContent();
            
            content = Ngram.urlNormalize(content);
            content = Ngram.filterUtterance(content);

            
//            // by Laura Dec 07, 2010
//            content = pnw.replaceSentence(content);

            ArrayList<String> terms = Ngram.generateNgramList(content);

//            // by Laura Jan 04, 2011
            if(daTag.toLowerCase().contains(DsarmdDATag.AD)){ // Action-Directive
                terms.add(DsarmdDATag.AD);
            }
            // by Laura Jan 25, 2011
            if(daTag.toLowerCase().contains(DsarmdDATag.DR)){ // Disagree-Reject
                terms.add(DsarmdDATag.DR);
            }
            
            for(int j = 0; j < terms.size(); j++){
                if(tr_utts_termTagFrequency.containsKey(terms.get(j))){
                    int[] tag_f_array = tr_utts_termTagFrequency.get(terms.get(j));
                    tag_f_array[tagIndex] += 1;
                    tr_utts_termTagFrequency.put(terms.get(j), tag_f_array);
                 }
                else{
                    int[] tag_f_array = new int[tagNum+1]; // 16 including "unknown"
                    tag_f_array[tagIndex] += 1;
                    tr_utts_termTagFrequency.put(terms.get(j), tag_f_array);
                }
            }
        }
        assert(true):"\n************************* Training set tagF/tf *********************" +
                "\nSize of tagF/tf in training set: " + tr_utts_termTagFrequency.size() +
                "\n****************************************************************";
        
    }

    
    /**
     * Set featureMap for named tag, set allFeatures list
     * @param tagType Type of the tag, such as "da15", "da3", and so on
     * @param tagName Each named tag, such as "agree-accept"
     * @param minTF Min term frequency
     * @param maxTF Max term frequency
     * @param minWordsNum Min words in a ngram term
     * @param minTermTagF Min tag_frequency/term_freuency
     */
    public void setFeatureMap(String tagType, String tagName, int minTF, int maxTF,
            double minTermTagF, int minWordsNum){
        tagName = tagName.toLowerCase();
        int tagNum = 0;
        if(tagType.equals("da15")){
            tagNum = 15;
        }
        else if(tagType.equals("da3")){
            tagNum = 3;
        }
        else{
            System.err.println("unrecognized tag type");
            return;
        }
        /* set features HashSet */
        Object[] tr_terms = tr_utts_termFrequency.keySet().toArray();
        for(int i = 0; i < tr_terms.length; i++){ // for each term
            String term = (String)tr_terms[i];
            String[] wordsInTerm = term.split("\\s+");
            // by Laura Jan 04, 2011
            if(!term.equals(DsarmdDATag.AD) &&
                    !term.equals(DsarmdDATag.DR) &&
                    wordsInTerm.length < minWordsNum){
                continue;
            }
            int tf = tr_utts_termFrequency.get(term);
            int[] tag_f_array = tr_utts_termTagFrequency.get(term);
            int tagIndex = DsarmdDATag.getTagAliasNum(tagName, tagNum) - 1;
            int tag_freq = tag_f_array[tagIndex];
            double fraction = (double)tag_freq/(double)tf;
            if(tf >= minTF && tf <= maxTF && fraction >= minTermTagF){
                allFeatures.add(term);
                if(featuresMap.containsKey(tagName)){
                    ArrayList features = featuresMap.get(tagName);
                    features.add(term);
                    featuresMap.put(tagName, features);
                }
                else{
                    ArrayList features = new ArrayList();
                    features.add(term);
                    featuresMap.put(tagName, features);
                }
            }
        }        
    }

    public ArrayList getTagList(){
        return new ArrayList(tagList);
    }

    public HashMap<String, Integer> getTr_utts_tagQuantity(){
        return tr_utts_tagQuantity;
    }

    public HashMap<String, Double> getTr_utts_tagPercentage(){
        return tr_utts_tagPercentage;
    }

    public HashMap<String, Integer> getUtts_tagQuantity(){
        return utts_tagQuantity;
    }

    public HashMap<String, Double> getUtts_tagPercentage(){
        return utts_tagPercentage;
    }

    public HashMap<String, Integer> getAll_utts_tagQuantity(){
        return all_utts_tagQuantity;
    }

    public HashMap<String, Double> getAll_utts_tagPercentage(){
        return all_utts_tagPercentage;
    }

    public HashMap<String, Integer> getTr_utts_termFrequency(){
        return tr_utts_termFrequency;
    }

    public HashMap<String, Integer> getUtts_termFrequency(){
        return utts_termFrequancy;
    }

    public HashMap<String, int[]> getTr_utts_termTagFrequency(){
        return tr_utts_termTagFrequency;
    }

    public HashMap<String, ArrayList> getFeaturesMap(){
        return featuresMap;
    }

    public ArrayList<String> getAllFeatures(){
        return allFeatures;
    }

}
