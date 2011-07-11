/*
 * This class is to combine prediction tags with the same turn no.
 *
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author gjiao
 */
public class CombineTestingUtterance {

    ArrayList<Utterance> utts; // original utts, for reference
    ArrayList<Utterance> splitUtts; // expanded utts
    TreeMap<Integer, ArrayList<Utterance>> uttsMap = new TreeMap<Integer, ArrayList<Utterance>>();
    // key: turn no.; value: a list of Utterances
    
    public CombineTestingUtterance(ArrayList<Utterance> utts,
            ArrayList<Utterance> splitUtts){
        this.utts = utts;
        this.splitUtts = splitUtts;
    }

    public ArrayList<Utterance> getCombinedUtts(){
        uttsMap.clear();
        ArrayList<Utterance> result = new ArrayList<Utterance>();

        int i = 0;
        int j = 0;
        Utterance tmp = null;
        String combinedTag = "";
        while(i < utts.size() && j < splitUtts.size()){
            Utterance originUtt = utts.get(i);
            Utterance currUtt = splitUtts.get(j);
            String origContent = originUtt.getContent().toLowerCase().trim();
            String currContent = currUtt.getContent().toLowerCase().trim();
            tmp = originUtt;
            combinedTag = currUtt.getTag().toLowerCase().trim();
            if(origContent.contains(currContent)){
                if(!currUtt.getTag().toLowerCase().trim().equals(DsarmdDATag.AA)){
                    combinedTag = currUtt.getTag().toLowerCase().trim();
                }
                j++;                
            }
            else{
                tmp.setTag(combinedTag);
                result.add(tmp);
                i++;
            }            
        }
        tmp.setTag(combinedTag); // add the last one
        result.add(tmp);
        return result;
    }

}
