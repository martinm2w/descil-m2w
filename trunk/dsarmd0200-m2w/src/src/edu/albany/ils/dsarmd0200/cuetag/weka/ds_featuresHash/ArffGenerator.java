/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.lu.Settings;
import java.io.*;
import java.util.*;

/**
 *
 * @author Laura G.H. Jiao
 */
public class ArffGenerator {

    private String arffTrainingFileName = "";
    private String arffTrainingFileLocation = "";
    private String arffTestingFileName = "";
    private String arffTestingFileLocation = "";
    private ArrayList tr_utts = new ArrayList();
    private ArrayList utts = new ArrayList();
    private ArrayList tags = new ArrayList(); // tags of the corpus
    private HashMap<String, ArrayList> featuresMap = new HashMap<String, ArrayList>(); // key: tag; value: {features}
    private ArrayList<String> allFeatures = new ArrayList<String>(); // all features in the training set
    private static String tagType = Settings.getValue("tagType");

//    private static PNWords pnw = new PNWords();
    private TagRulesPredefined trp = new TagRulesPredefined();
    
    public ArffGenerator(String tr_fileName, String tr_fileLocation, String te_fileName, String te_fileLocation,
            ArrayList tr_utts, ArrayList utts, ArrayList tags, HashMap featuresMap, ArrayList allFeatures){
        arffTrainingFileName = tr_fileName;
        arffTrainingFileLocation = tr_fileLocation;
        arffTestingFileName = te_fileName;
        arffTestingFileLocation = te_fileLocation;
        this.tr_utts = tr_utts;
        this.utts = utts;
        this.tags = tags;
        this.featuresMap = featuresMap;
        this.allFeatures = allFeatures;
    }

    public void writeTrainingArff(){
        writeArffHeading(arffTrainingFileLocation, arffTrainingFileName, tags);
        appendToTrainingArff(arffTrainingFileLocation, tr_utts, featuresMap);
    }

    public void writeTestingArff(Boolean nonBlank){
        writeArffHeading(arffTestingFileLocation, arffTestingFileName, tags);
        appendToTestingArff(arffTestingFileLocation, utts, allFeatures, nonBlank);
    }

   
    /**
     * Prepare arff heading
     * @param tags Arff heading: classes
     */
    private void writeArffHeading(String arffFileLocation, String arffFileName, ArrayList tags){
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(arffFileLocation));
            /* write the arff heading */
            bw.write("@RELATION " + arffFileName + "\n");
            bw.write("@ATTRIBUTE tag { ");
	    String unknown = "unknown";
	    boolean has_ukn = false;
            for(int i = 0; i < tags.size(); i++){
                bw.write(tags.get(i) + ", ");
		if (unknown.equals(tags.get(i))) {
		    has_ukn = true;
		}
            }
	    if (!has_ukn) {
		bw.write(unknown + ",");
	    }
            bw.write("}\n");
            bw.write("@ATTRIBUTE utterance string\n");
            bw.write("\n@DATA\n");

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private void appendToTrainingArff(String arffFileLocation, ArrayList tr_utts, HashMap featuresMap){
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(arffFileLocation, true));
            
            for(int i = 0; i < tr_utts.size(); i++){ // tr_utts
                Utterance utterance = (Utterance)tr_utts.get(i);
                String daTag = "";
                String str = "";
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
                ArrayList featuresOfTheTag = (ArrayList)featuresMap.get(daTag);

                
                // by Laura Nov 30, 2010
                String commActType = utterance.getCommActType().toLowerCase();

                String content = utterance.getContent();
               
                // by Laura Nov 30, 2010
                content = Ngram.urlNormalize(content);
                content = Ngram.filterUtterance(content);

//                // by Laura Dec 07, 2010
//                content = pnw.replaceSentence(content);

                ArrayList<String> ngrams = Ngram.generateNgramList(content);

                // by Laura Jan 04, 2011
                if(daTag.toLowerCase().contains(DsarmdDATag.AD)){ // Action-Directive System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.AD);
//                    System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                }
                // by Laura Jan 25, 2011
                if(daTag.toLowerCase().contains(DsarmdDATag.DR)){ // Disagree-Reject System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.DR);
                }

                for(int j = 0; j < ngrams.size(); j++){
                    String term = ngrams.get(j);
                    String[] wordsInTerm = term.split("\\s+");
                    // by Laura Jan 04, 2011
                    if(!term.equals(DsarmdDATag.AD) &&
                            !term.equals(DsarmdDATag.DR) &&
                            wordsInTerm.length < 2){
                        continue;
                    }
                    if(featuresOfTheTag != null && featuresOfTheTag.contains(term)){
                        str += "|" + term;
                    }
                }
                if(!str.equals("")){
                // by Laura Nov 30, 2010
                    bw.write(daTag + ", '" + str + "|" + commActType + "'\n");
//                    bw.write(tag + ", '" + str + "'\n");
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendToTestingArff(String arffFileLocation, ArrayList utts, Collection allFeatures, Boolean nonBlank){
         BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(arffFileLocation, true));

            for(int i = 0; i < utts.size(); i++){ // utts
                Utterance utterance = (Utterance)utts.get(i);
                String tag = "";
                String str = "";
                if(tagType.equals("da15")){
                    tag = utterance.getTag().toLowerCase().trim();
                    tag = tag.replace("--", "");
                }
                else if(tagType.equals("da3")){
                    tag = utterance.getMTag().toLowerCase().trim();
                }
                else{
                    System.err.println("unrecognized tag type");
                    return;
                }
                if(tag == null || tag.equals("")) {// testing file may not have tag attribute
                    tag = "unknown";
		}

                // by Laura Nov 30, 2010
                String commActType = utterance.getCommActType().toLowerCase();
                
                String content = utterance.getContent();
                                
                // by Laura Nov 30, 2010
                content = Ngram.urlNormalize(content);
                content = Ngram.filterUtterance(content);


                // by Laura Jan 04, 2011
                // Add pre-defined features
                content = trp.rules_filtered(content);
                

//                // by Laura Dec 07, 2010
//                content = pnw.replaceSentence(content);
                
                ArrayList<String> ngrams = Ngram.generateNgramList(content);                
                for(int j = 0; j < ngrams.size(); j++){
                    String term = ngrams.get(j);
                    String[] wordsInTerm = term.split("\\s+");
                    // by Laura Jan 04, 2011
                    if(!term.equals(DsarmdDATag.AD) &&
		       !term.equals(DsarmdDATag.DR) &&
		       wordsInTerm.length < 2)
                        continue;
                    if(allFeatures.contains(term)){
                        str += "|" + term;
                    }
                }
                if(nonBlank == true){
                    if(!str.equals(""))
                        // by Laura Nov 30, 2010
                        bw.write(tag + ", '" + str + "|" + commActType + "'\n");
//                        bw.write(tag + ", '" + str + "'\n");
                }
                else{
                    // by Laura Nov 30, 2010
                    bw.write(tag + ", '" + str + "|" + commActType + "'\n");
//                    bw.write(tag + ", '" + str + "'\n");
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
}
