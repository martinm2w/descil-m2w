
package edu.albany.ils.dsarmd0200.lu;

// author Laura G.H. Jiao

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import org.w3c.dom.Document;

public class CiteDisparityIndex {

    public static DsarmdDP dsarmdDP_ = new DsarmdDP();
    private Wordnet wn_ = null;//new Wordnet();

    private LocalTopics lts_ = new LocalTopics();
    private NounList nls_ = null;
    
    private ArrayList<Utterance> utts_ = new ArrayList<Utterance>();
    private HashMap<String, Speaker> parts_ = new HashMap<String, Speaker>();    
    private ArrayList<String> spks_pairs_ = new ArrayList<String>(); // spk1:spk2, spk1:spk3, ...
    private HashMap<String, Double> cite_disparities_ = new HashMap<String, Double>(); // key: speaker pair(sorted); value: cite disparity score

    public CiteDisparityIndex(ArrayList<Utterance> utts_,
			      HashMap parts){
        this.utts_ = utts_;
	parts_ = parts;
        //loadSpeakers();
        calPossibleSpeakerPairs();
//
//    /*****************************************************************/
//        Settings.initialize();
//	ParseTools.initialize();
//	PronounFormMatching.initialize();
//	GenderCheck.initialize();
//        ParseTools.setWN(wn_);
//        XMLParse xp = new XMLParse(fl.getAbsolutePath(), list);
//        PhraseCheck phr_ch = new PhraseCheck(fl.getAbsolutePath(), xp.getUtterances());
//
//        buildLocalTopicList(phr_ch, xp);
//        /*****************************************************************/
//
    }

    public CiteDisparityIndex(String filePath){
        File fl = new File(filePath);
        Document doc = dsarmdDP_.getDocument(fl.getAbsolutePath());
        ArrayList<Utterance> list = dsarmdDP_.parseDAList(doc, "dsarmd", false, null);
        this.utts_ = list;

        loadSpeakers();
        calPossibleSpeakerPairs();

        /*****************************************************************/
        Settings.initialize();
	ParseTools.initialize();
	PronounFormMatching.initialize();
	GenderCheck.initialize();
        ParseTools.setWN(wn_);
        XMLParse xp = new XMLParse(fl.getAbsolutePath(), list);
        PhraseCheck phr_ch = new PhraseCheck(fl.getAbsolutePath(), xp.getUtterances());
        
        buildLocalTopicList(phr_ch, xp);
        /*****************************************************************/

        //calCiteDisparities();
	calCDI();
    }


    /*
     * load the speakers (list of Speaker)
     */
    private void loadSpeakers(){
        for(int i = 0; i < utts_.size(); i++){
            Utterance utt = utts_.get(i);
            String spk = utt.getSpeaker().toLowerCase().trim();
            if(spk == null || spk.equals("")){
                continue;
            }
            Set<String> spkSet = parts_.keySet();
            if(!spkSet.contains(spk)){
                Speaker speaker = new Speaker(spk);
                speaker.addUtterance(utt);
                parts_.put(spk, speaker);
            }
            else{
                Speaker speaker = parts_.get(spk);
                speaker.addUtterance(utt);
                parts_.put(spk, speaker);
            }
        }
//        System.out.println("Number of speakers = " + parts_.size());
    }

    /*
     * Generate all possible speaker pairs, each pair of speaker names is sorted
     */
    private void calPossibleSpeakerPairs(){
        Set keySet = parts_.keySet();
        int n = keySet.size(); // number of speakers
        Object[] keySetArray = keySet.toArray();
        Arrays.sort(keySetArray); // so that each pair will be sorted by names
        for(int i = 0; i < n; i++){
            for(int j = i+1; j < n; j++){
                String spk1 = (String)keySetArray[i];
                String spk2 = (String)keySetArray[j];
                String spk_pair = spk1 + ":" + spk2;
                spks_pairs_.add(spk_pair);
            }
        }
//        System.out.println("Number of possible speaker pairs = " + spks_pairs_.size());
    }

    private void buildLocalTopicList(PhraseCheck phr_ch, XMLParse xp) {
	ArrayList utts = new ArrayList();
	ArrayList spks = new ArrayList();
	ArrayList comm_acts = new ArrayList();
	ArrayList resps_to = new ArrayList();
	ArrayList turn_nums = new ArrayList();

        for(int i = 0; i < utts_.size(); i++){
            Utterance utt = utts_.get(i);
            String spk = utt.getSpeaker().toLowerCase().trim();
            if(spk == null || spk.equals("")){
                continue;
            }
            spks.add(spk);
            comm_acts.add(utt.getCommActType());
            resps_to.add(utt.getRespTo());
            turn_nums.add(utt.getTurn());
            utts.add(utt.getContent());
        }
	FindVerb fv = new FindVerb();
        nls_ = new NounList(utts, spks, wn_, comm_acts, resps_to, turn_nums, phr_ch, xp);
        nls_.createList(fv);

        ArrayList nouns = nls_.getNouns();

        for (int i = 0; i < nouns.size(); i++) {
            NounToken nt = (NounToken)nouns.get(i);
            String spk = nt.getSpeaker();
            LocalTopic lt = lts_.add(nt, spk);
            Speaker part = parts_.get(spk);
            if (lt == null || part == null) continue;
            if (nt.firstMention() ||
                spk.equals(lt.getIntroducer())) {
                part.addILT(lt);
            } else {
                part.addCLT(lt);
            }
            part.addNoun(nt);

            parts_.put(spk, part);
        }
    }

    /*
     * Set up the Cite Disparity scores
     */
    public void calCDI() {//calCiteDisparities(){
        for(String pair : spks_pairs_){
            String[] spks = pair.split(":");
            String spkName1 = spks[0];
            String spkName2 = spks[1];
            Speaker spk1 = parts_.get(spkName1);
            Speaker spk2 = parts_.get(spkName2);
            LocalTopics clts1 = spk1.getCLTs();
            LocalTopics clts2 = spk2.getCLTs();
            /* spk1 cites spk2 */
            int spk1ToSpk2 = 0;
            for(int i = 0; i < clts1.size(); i++){
                LocalTopic clt = (LocalTopic)clts1.get(i);
                String introSpeaker = clt.getIntroducer().toLowerCase();
//                System.err.println(introSpeaker);
                if(introSpeaker.equals(spkName2)){
                    spk1ToSpk2++;
                }
            }
            /* spk2 cites spk1 */
            int spk2ToSpk1 = 0;
            for(int i = 0; i < clts2.size(); i++){
                LocalTopic clt = (LocalTopic)clts2.get(i);
                String introSpeaker = clt.getIntroducer().toLowerCase();
                if(introSpeaker.equals(spkName1)){
                    spk2ToSpk1++;
                }
            }
            if(spk1ToSpk2 == 0 && spk2ToSpk1 == 0){ // nothing's going on with this pair of speakers
                continue;
            }
            int base = (spk1ToSpk2 > spk2ToSpk1) ? spk1ToSpk2 : spk2ToSpk1; // base is the larger one ???
            double CDI = (double)(Math.abs(spk2ToSpk1 - spk1ToSpk2)) / (double)base;
            cite_disparities_.put(pair, CDI);
        }
    }

    /*
     * Calculate the Cite Disparity Index
     */
    public double getCDI(){

        double CDISum = 0.0;
        int numOfPairs = cite_disparities_.keySet().size();

//	System.out.println("CDI Map: " + cite_disparities_);
        for(String pair : cite_disparities_.keySet()){
            double thisCDI = cite_disparities_.get(pair);
            CDISum += thisCDI;
        }

        double CDI = CDISum / (double)numOfPairs;
        return CDI;
    }
    

    public static void main(String[] args){
        String docs_path = "/home/jgh/Desktop/Human_Terrain_Studies/lauren_annotated/"
            + "GroundTruth/ground_truth/with_new_annotated/";
        CiteDisparityIndex cdi = new CiteDisparityIndex(docs_path + "Cheney_gt.xml");
        System.out.println("Cite Disparity Index = " + cdi.getCDI());
    }

}
