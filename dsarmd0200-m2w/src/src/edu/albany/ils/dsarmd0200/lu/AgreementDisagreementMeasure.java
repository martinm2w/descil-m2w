
package edu.albany.ils.dsarmd0200.lu;

// author Laura G.H. Jiao
// modified by TL 04/06

import edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash.DsarmdDATag;
import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.util.xml.DsarmdDP;
import edu.albany.ils.dsarmd0200.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import org.w3c.dom.Document;

public class AgreementDisagreementMeasure {

    private ArrayList<Utterance> utts = new ArrayList<Utterance>();
    private HashMap<String, Speaker> spks_ = null;

    // key: da tag; value: frequency
    private HashMap<String, Integer> TagFreq = new HashMap<String, Integer>();
    private HashMap<String, HashMap<String, Integer>> spksTagFreq = new HashMap<String, HashMap<String, Integer>>();

    DsarmdDP dp = new DsarmdDP();

    public AgreementDisagreementMeasure(ArrayList<Utterance> utts){
        this.utts = utts;
        loadMaps();
    }

    public AgreementDisagreementMeasure(HashMap<String, Speaker> spks){
	//added by TL
        spks_ = spks;
        loadMaps();
    }

    public AgreementDisagreementMeasure(ArrayList<Utterance> utts,
					HashMap<String, Speaker> spks){
	//added by TL
        spks_ = spks;
	this.utts = utts;
        loadMaps();
    }

    public AgreementDisagreementMeasure(String filePath){
        File fl = new File(filePath);
        Document doc = dp.getDocument(fl.getAbsolutePath());
        ArrayList<Utterance> list = dp.parseDAList(doc, "dsarmd", false, null);
        this.utts = list;
        loadMaps();
    }

    /*
     * Save tag frequency
     */
    private void loadMaps(){
        for(Utterance utt : utts){
            String daTag = utt.getTag().toLowerCase().trim();
            daTag = daTag.replace("--", "");
            if(TagFreq.containsKey(daTag)){
                int tmp = TagFreq.get(daTag);
                TagFreq.put(daTag, Integer.valueOf(tmp+1));
            }
            else{
                TagFreq.put(daTag, Integer.valueOf(1));
            }
        }
    }

    /*
     * Save tag frequency based on speakers
     * added by TL
     */
    private void loadSpksMaps(){
	Iterator keys = spks_.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    if (key.startsWith("ils")) continue;
//	    System.out.println("key: " + key);
	    Speaker spk_ = spks_.get(key);
	    String spk_nm = spk_.getName();
	    HashMap<String, Integer> TagFreq = spksTagFreq.get(spk_nm);
	    if (TagFreq == null) {
		TagFreq = new HashMap<String, Integer>();
		spksTagFreq.put(spk_nm, TagFreq);
	    }
	    ArrayList<Utterance> utts = spk_.getUtterances();
	    for(Utterance utt : utts){
		String daTag = utt.getTag().toLowerCase().trim();
		daTag = daTag.replace("--", "");
		if(TagFreq.containsKey(daTag)){
		    int tmp = TagFreq.get(daTag);
		    TagFreq.put(daTag, Integer.valueOf(tmp+1));
		}
		else{
		    TagFreq.put(daTag, Integer.valueOf(1));
		}
	    }
        }
    }

    /*
     * Calculate ADM for this corpus
     */
    public double getADM(){
        double result = 0.0;
        int A = 0;
        int D = 0;
        if(TagFreq.containsKey(DsarmdDATag.AA)){
            A = TagFreq.get(DsarmdDATag.AA);
        }
        if(TagFreq.containsKey(DsarmdDATag.DR)){
            D = TagFreq.get(DsarmdDATag.DR);
        }

        result = (double)(A+1)/(double)(A+D+1);

        return result;
    }


    /*
     * Calculate ADM for this corpus based on each speaker
     * Added by TL
     */
    public double[] getSpksADM(){
	double[] ads = new double[spks_.size()];
	int count = 0;
	Iterator keys = spks_.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    if (key.startsWith("ils")) continue;
	    Speaker spk = spks_.get(key);
	    double result = spk.getDisagreement() - spk.getATX();
	    ads[count] = result;
	    count++;
	}
	Arrays.sort(ads);
	return ads;
    }
    
    /*
     * calculate how many Agr-DisAgre outside the Standard Deviation
     */
    public int outsideSD(double[] ads) {
	int outliers = 0;
	double mean = Util.genMean(ads);
	double sd = Util.genSD(ads);
	//System.out.println("************************");
	//System.out.println("Agreement Disagreement mean: " + mean);
	//System.out.println("Agreement Disagreement standard deviation: " + sd);
	for (double ad : ads) {
	    //System.out.println("Agreem Disagreement difference: " + ad);
	    if (ad < mean - sd ||
		ad > mean + sd) {
		outliers++;
	    }
	}
	//System.out.println("outliers: " + outliers);
	return outliers;
    }

    /*
     * added by TL
     */
    public double calSpksADMeasure() {
	double[] ads = getSpksADM();
	int outliers = outsideSD(ads);
        return 1 - ((double)outliers)/spks_.size();
    }


    /*
     * Calculate ADM for these two tags, in this corpus
     */
    public double getADMMeasure(String tag1, String tag2){
        double result = 0.0;

        int freq1 = TagFreq.get(tag2.toLowerCase());
        int freq2 = TagFreq.get(tag2.toLowerCase());

        result = (double)(freq1+1)/(double)(freq1+freq2+1);

        return result;
    }


    /* illustration */
    public static void main(String[] args){
        String docs_path = "/home/jgh/Desktop/Human_Terrain_Studies/lauren_annotated/"
            + "GroundTruth/ground_truth/with_new_annotated/";
        AgreementDisagreementMeasure ADM = new AgreementDisagreementMeasure(docs_path + "Cheney_gt.xml");
        
        System.out.println(ADM.getADM());
    }


}
