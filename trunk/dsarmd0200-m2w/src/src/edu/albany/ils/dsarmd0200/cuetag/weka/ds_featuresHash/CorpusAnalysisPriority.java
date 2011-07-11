
package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

// author Laura G.H. Jiao

import java.util.ArrayList;
import java.util.HashSet;

public class CorpusAnalysisPriority extends CorpusAnalysis{

    HashSet<String> allFeaturesHash = new HashSet<String>();


    public CorpusAnalysisPriority(ArrayList all_utts, ArrayList utts, ArrayList tr_utts){
        this.all_utts = all_utts;
        this.utts = utts;
        this.tr_utts = tr_utts;        
    }

    @Override
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
                // by Laura Nov 30, 2010
                if(!allFeaturesHash.contains(term)){
                    
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
                
                    allFeaturesHash.add(term);
                    allFeatures.add(term);
                }
            }
        }
                
    }

    public HashSet<String> getAllFeaturesHash(){
        return allFeaturesHash;
    }

}
