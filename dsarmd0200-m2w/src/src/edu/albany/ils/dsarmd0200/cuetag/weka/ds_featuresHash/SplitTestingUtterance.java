/*
 * This class is to split long utterances into several sub-sentences in the
 * testing corpus
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.util.SentenceSplitter;
import java.util.ArrayList;

/**
 *
 * @author gjiao
 */
public class SplitTestingUtterance {

    private ArrayList<Utterance> utts;
    SentenceSplitter sp = new SentenceSplitter();

    public SplitTestingUtterance(ArrayList<Utterance> utts){
        this.utts = utts;
    }

    /**
     * This method goes through all utterance utts(testing corpus) and
     * split a complex sentence(contains 2-3 sub-sentences) into several
     * sub sentence, everything else stays the same.
     * @return an enriched utts
     */
    public ArrayList<Utterance> getSplittedUtts(){
        ArrayList<Utterance> result = new ArrayList<Utterance>();

        for(Utterance utt : utts){
            String content = utt.getContent().toLowerCase().trim();
            ArrayList<String> subSentences = sp.split(content);
            if(subSentences.size() > 3){
                result.add(utt);
            }
            else if(subSentences.size() > 1){ // sub sentences num : 2 - 3
                for(String s : subSentences){
                    Utterance tmp = utt;
                    tmp.setContent(s);
                    result.add(tmp); // seperate them
                }
            }
            else{
                result.add(utt);
            }
        }
        return result;
    }

}
