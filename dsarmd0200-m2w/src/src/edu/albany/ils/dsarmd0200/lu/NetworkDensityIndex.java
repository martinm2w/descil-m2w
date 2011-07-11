
package edu.albany.ils.dsarmd0200.lu;

// author Laura G.H. Jiao

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import org.w3c.dom.*;

public class NetworkDensityIndex {
    
    public static DsarmdDP dsarmdDP_ = new DsarmdDP();    
    private Wordnet wn_ = null;//new Wordnet();
    private LocalTopics lts_ = new LocalTopics();
    private NounList nls_ = null;
    private double cndi_ = -100; //Caliberating NDI
    private double mean_ = -100;
    private double sd_ = -100;

    private ArrayList<Utterance> utts_ = new ArrayList<Utterance>();
    private HashMap<String, Speaker> parts_ = new HashMap<String, Speaker>();
    private ArrayList<String> spks_pairs_ = new ArrayList<String>(); // spk1:spk2, spk1:spk3, ...
    private HashMap<String, Integer> addr_to_links_ = new HashMap<String, Integer>(); // key: speaker pairs(sorted); value: number of addr-to links
    private HashMap<String, Integer> resp_to_links_ = new HashMap<String, Integer>(); // key: speaker pairs(sorted); value: number of resp-to links
    private HashMap<String, Integer> top_cite_links_ = new HashMap<String, Integer>(); // key: speaker pairs(sorted); value: number of topic citation links
    private HashMap<String, Integer> links_sum_ = new HashMap<String, Integer>(); // key: speaker pairs(sorted); value: sum of all network links


    public NetworkDensityIndex(ArrayList<Utterance> utts_,
			       //NounList nls,
			       HashMap parts){
        this.utts_ = utts_;
	//nls_ = nls;
	parts_ = parts;

        //loadSpeakers();

        /*****************************************************************/
        //Settings.initialize();
	//ParseTools.initialize();
	//PronounFormMatching.initialize();
	//GenderCheck.initialize();
        //ParseTools.setWN(wn_);
        //XMLParse xp = new XMLParse(fl.getAbsolutePath(), list);
        //PhraseCheck phr_ch = new PhraseCheck(fl.getAbsolutePath(), xp.getUtterances());

        //buildLocalTopicList(phr_ch, xp);
        /*****************************************************************/

    }

    public NetworkDensityIndex(String filePath){
        File fl = new File(filePath);
        Document doc = dsarmdDP_.getDocument(fl.getAbsolutePath());
        ArrayList<Utterance> list = dsarmdDP_.parseDAList(doc, "dsarmd", false, null);
        this.utts_ = list;

        loadSpeakers();
        calPossibleSpeakerPairs();
        calAddressedToLinks();
        calResponseToLinks();

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

        calTopicCitationLinks();
        
        calLinksSum();                
    }    

    /*******************************set information**********************/

    public void calNDI() {
	//added by TL 04/11/11
        calPossibleSpeakerPairs();
        calAddressedToLinks();
        calResponseToLinks();
        calTopicCitationLinks();
        calLinksSum();
    }

    /*
     * load the speakers (list of Speaker)
     */
    private void loadSpeakers(){
        for(int i = 0; i < utts_.size(); i++){
            Utterance utt = utts_.get(i);
            String spk = utt.getSpeaker().toLowerCase().trim();

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
     * Set up the Addressed-To communicative links
     */
    private void calAddressedToLinks(){
        for(int i = 0; i < utts_.size(); i++){
            Utterance utt = utts_.get(i);
            String commAct = utt.getCommActType().toLowerCase().trim();

            if(commAct.equals("addressed-to")){

                String speaker = utt.getSpeaker().toLowerCase().trim();
                if(speaker == null || !parts_.containsKey(speaker)){
//                    System.err.println("don't have this speaker: " + speaker);
                    continue;
                }
                String content = utt.getContent().toLowerCase().trim();
                if(content == null){
//                    System.err.println("don't have content");
                    continue;
                }
                String[] contentArray = content.split("\\s+");
                for(String spkName : parts_.keySet()){
                    if(Arrays.asList(contentArray).contains(spkName)){
                        String addr_to_spk = spkName;
                        if(speaker.compareTo(addr_to_spk) < 0){
                            String key = speaker + ":" + addr_to_spk;
                            if(!addr_to_links_.containsKey(key)){
                                addr_to_links_.put(key, 1);
                            }
                            else{
                                int tmp = addr_to_links_.get(key);
                                addr_to_links_.put(key, new Integer(tmp+1));
                            }
                        }
                        else if(speaker.compareTo(addr_to_spk) > 0){
                            String key = addr_to_spk + ":" + speaker;
                            if(!addr_to_links_.containsKey(key)){
                                addr_to_links_.put(key, 1);
                            }
                            else{
                                int tmp = addr_to_links_.get(key);
                                addr_to_links_.put(key, new Integer(tmp+1));
                            }
                        }
                        else{
//                            System.err.println("Self refering??? turn no = " + utt.getTurn());
                        }
                    }
                }
            }
        }
//        System.out.println("Size of addressed to links = " + addr_to_links_.size());
//        System.out.println(addr_to_links_);
    }

    /*
     * Set up the Response-To links
     */
    private void calResponseToLinks(){
        for(int i = 0; i < utts_.size(); i++){
            Utterance utt = utts_.get(i);
            String commAct = utt.getCommActType().toLowerCase().trim();
            if(commAct.equals("response-to")){
                String speaker = utt.getSpeaker().toLowerCase().trim();
                if(speaker == null || !parts_.containsKey(speaker)){
//                    System.err.println("don't have this speaker: " + speaker);
                    continue;
                }
                String linkTo = utt.getRespTo().toLowerCase().trim();
                if(linkTo == null || !linkTo.contains(":")){
//                    System.err.println("don't have link_to value, turn No = " + utt.getTurn());
                    continue;
                }
                String[] tmp = linkTo.split(":");
                if(tmp.length == 2){
                    String resp_to_spk = tmp[0];
                    if(!parts_.containsKey(resp_to_spk)){
                        continue;
                    }
                    if(speaker.compareTo(resp_to_spk) < 0){
                        String key = speaker + ":" + resp_to_spk;
                        if(!resp_to_links_.containsKey(key)){
                            resp_to_links_.put(key, 1);
                        }
                        else{
                            int temp = resp_to_links_.get(key);
                            resp_to_links_.put(key, new Integer(temp+1));
                        }
                    }
                    else if(speaker.compareTo(resp_to_spk) > 0){
                        String key = resp_to_spk + ":" + speaker;
                        if(!resp_to_links_.containsKey(key)){
                            resp_to_links_.put(key, 1);
                        }
                        else{
                            int temp = resp_to_links_.get(key);
                            resp_to_links_.put(key, new Integer(temp+1));
                        }
                    }
                    else{
//                        System.err.println("self talk??? + turn No = " + utt.getTurn());
                    }
                }
            }
        }
//        System.out.println("Size of response to links = " + resp_to_links_.size());
//        System.out.println(resp_to_links_);
    }

    /*
     * Set up Topic Citation links
     */
    private void calTopicCitationLinks(){
        for(String speaker : parts_.keySet()){
            Speaker spk = parts_.get(speaker);
            LocalTopics clts = spk.getCLTs(); // list of cited LocalTopics
            for(int i = 0; i < clts.size(); i++){
                LocalTopic clt = (LocalTopic)clts.get(i);
                String introSpeaker = clt.getIntroducer().toLowerCase(); // who introduced it
                if(speaker.compareTo(introSpeaker) < 0){
                    String key = speaker + ":" + introSpeaker;
                    if(!top_cite_links_.containsKey(key)){
                        top_cite_links_.put(key, 1);
                    }
                    else{
                        int tmp = top_cite_links_.get(key);
                        top_cite_links_.put(key, new Integer(tmp+1));
                    }
                }
                else if(speaker.compareTo(introSpeaker) > 0){
                    String key = introSpeaker + ":" + speaker;
                    if(!top_cite_links_.containsKey(key)){
                        top_cite_links_.put(key, 1);
                    }
                    else{
                        int tmp = top_cite_links_.get(key);
                        top_cite_links_.put(key, new Integer(tmp+1));
                    }
                }
                else{
//                    System.err.println("self citing...");
                }
            }
        }
    }


    /*
     * Calculate the entire Network Links
     */
    private void calLinksSum(){
        for(String spk_pair : addr_to_links_.keySet()){
            int numLinks = addr_to_links_.get(spk_pair);
            if(!links_sum_.containsKey(spk_pair)){
                links_sum_.put(spk_pair, numLinks);
            }
            else{
                int tmp = links_sum_.get(spk_pair);
                links_sum_.put(spk_pair, new Integer(tmp + numLinks));
            }
        }

        for(String spk_pair : resp_to_links_.keySet()){
            int numLinks = resp_to_links_.get(spk_pair);
            if(!links_sum_.containsKey(spk_pair)){
                links_sum_.put(spk_pair, numLinks);
            }
            else{
                int tmp = links_sum_.get(spk_pair);
                links_sum_.put(spk_pair, new Integer(tmp + numLinks));
            }
        }

        for(String spk_pair : top_cite_links_.keySet()){
            int numLinks = top_cite_links_.get(spk_pair);
            if(!links_sum_.containsKey(spk_pair)){
                links_sum_.put(spk_pair, numLinks);
            }
            else{
                int tmp = links_sum_.get(spk_pair);
                links_sum_.put(spk_pair, new Integer(tmp + numLinks));
            }
        }
    }    


    public double calCNDI() {
	//added by TL 04/12/11
	//double mnd = (2*((double)utts_.size())/parts_.size())/((parts_.size() - 1) * parts_.size());
	double mnd = (2*((double)utts_.size()))/((parts_.size() - 1) * parts_.size());
	cndi_ = (mean_ - sd_) / mnd; //modified at 04/14 by TL
	if (cndi_ < 0) cndi_ = 0;
	//cndi_ = mean_/((2*((double)utts_.size())/parts_.size())) - sd_;
	return cndi_;
    }

    /**********************************************get Information**************/

    /*
     * Calculate the NDI Mean
     */
    public double getNDIMean(){               
	if (mean_ != -100) return mean_;
        int num_spk_pairs = links_sum_.keySet().size();
        int sum = 0;
        for(String spk_pair : links_sum_.keySet()){
            int num_links = links_sum_.get(spk_pair);
            sum += num_links;
        }

        double mean = (double)sum / (double) num_spk_pairs;
	mean_ = mean;
        return mean;
        
    }

    public double getCNDI() {
	//added by TL 04/12/11
	if (cndi_ == -100) return calCNDI();
	return cndi_;
    }

    /*
     * Calculate the NDI Standard Deviation
     */
    public double getNDIStandardDeviation(){

	if (sd_ != -100) return sd_;
        double mean = getNDIMean();

        double differanceSquaresSum = 0.0;

        for(String spk_pair : links_sum_.keySet()){
            int num_links = links_sum_.get(spk_pair);
            double difference = Math.abs(num_links - mean);
            double differenceSquare = difference * difference;
            differanceSquaresSum += differenceSquare;
        }

        int n = links_sum_.keySet().size();

        double tmp = differanceSquaresSum / (double)n;        

        double standardDeviation = Math.sqrt(tmp);
	sd_ = standardDeviation;
        return standardDeviation;
        
    }

 

    public static void main(String[] args){
        String docs_path = "/home/jgh/Desktop/Human_Terrain_Studies/lauren_annotated/"
            + "GroundTruth/ground_truth/with_new_annotated/";
        NetworkDensityIndex ndi = new NetworkDensityIndex(docs_path + "Cheney_gt.xml");

        System.out.println("NDI Mean = " + ndi.getNDIMean());
        System.out.println("NDI Standard Deviation = " + ndi.getNDIStandardDeviation());
    }

}




