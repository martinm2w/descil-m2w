package edu.albany.ils.dsarmd0200.cuetag;

import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.lu.*;

/**
 * this file is used to produce n-gram
 * @author Ting Liu
 */


import java.io.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
import java.util.*;
import java.util.TreeSet;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class TagTraining {
	
    private HashMap<String, Integer> trainMap = new HashMap<String, Integer>();// key=model_number:tag:ngram  value = frequency
    private HashMap<String, Integer> allMap = new HashMap<String, Integer>(); //key=model_number:all:ngram  value = frequency
    private HashMap<String, ObjValue> singleMap = new HashMap<String, ObjValue>(); //key=model_number:ngram  value=ObjValue
    // the totalMap is used to hold the all n-gram information built from the whole training data corpus, stay in memory
    private HashMap<String, ObjValue> totalMap = new HashMap<String, ObjValue>(); //key = model_number:ngram  value = ObjValue
    private HashMap<String, Integer> tags_in_trM_ = new HashMap<String, Integer>();
    private HashMap<String, Integer> tt_tags_in_trM_ = new HashMap<String, Integer>();
    double THRESHOLD_PREDICTION = 0.325; //exclusive
    double HI_THRESHOLD_PREDICTION = 0.7;
    double HII_THRESHOLD_PREDICTION = 1;
    int THRESHOLD_FREQUENCY = 3; //exclusive
    int HI_THRESHOLD_FREQUENCY = 2; //exclusive
    int HII_THRESHOLD_FREQUENCY = 2; //exclusive
    public static DsarmdDP dsarmdDP_ = new DsarmdDP();
    private ArrayList spks_ = new ArrayList();
    private Wordnet wn_ = null;
    private ArrayList utts_ = null;
    
    
	
    public TagTraining() {
    }

    public void init() {
	trainMap.clear();
	allMap.clear();
	singleMap.clear();
	totalMap.clear();
	tags_in_trM_.clear();
	//System.out.println("size of tags in total_training maps: " + tt_tags_in_trM_.size());
    }

    public void setWN(Wordnet wn) { wn_ = wn; }
	
    public void buildTrainMap_old(String path,
				  boolean skip_sw) {
	BufferedReader br;
	int splitIndex = 0;
	int i = 0;
	int value = 0;
	String keyStr = null;
        String allKeyStr = null;
	String keyFinal = null;
        String allKeyFinal = null;
	String text = null;
	//String valueStr = null;
	String line = null;
	String[] textGram=null;
	
	String[] lineGroup;
	
	/* for testing and file validation  */
	//String testPath = "/home/qty/TAG_Project/TagTraining/swbd_202k_42tags_noplus.test";
        
	
	try {
	    br = new BufferedReader( new FileReader( new File(path)));
	    // PrintWriter pwtest = new PrintWriter(new FileWriter(new File(testPath)));
	    while((line = br.readLine())!=null) {
		if(line.indexOf("Dialogue")<0/* && line.indexOf(":sd:")<0 */) {
		    lineGroup = line.split(":"); //line = model_number:utterance
		    splitIndex = line.lastIndexOf(":") + 1;
		    keyStr = line.substring(0, splitIndex);
		    allKeyStr = lineGroup[0]+":"+"all"+":";
		    text = line.substring(splitIndex);
		    //System.out.println("text: " + text);
		    textGram = getNGram(null, text, skip_sw);
		    for(i=0; i<textGram.length; i++) {
			keyFinal = keyStr + textGram[i];
			allKeyFinal = allKeyStr + textGram[i];
			//pwtest.println(keyFinal);
			if(!trainMap.containsKey(keyFinal)) {
			    //System.out.println("key: " + keyFinal);
			    trainMap.put(keyFinal, Integer.valueOf(1));
			}
			else {
			    value = (trainMap.get(keyFinal)).intValue() + 1;
			    trainMap.put(keyFinal, Integer.valueOf(value));
			}
                        if(!allMap.containsKey(allKeyFinal)) {
			    //System.out.println("key: " + keyFinal);
			    allMap.put(allKeyFinal, Integer.valueOf(1));
			}
			else {
			    value = (allMap.get(allKeyFinal)).intValue() + 1;
			    allMap.put(allKeyFinal, Integer.valueOf(value));
			}     
		    }
		    
		    
		}
	    }
	    br.close();
	    //pwtest.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	
    }
	
    /**
     * for SWBD data
     */
    public void buildTrainMap(String path,
			      boolean skip_sw) {
	BufferedReader br;
	int splitIndex = 0;
	int i = 0;
	int value = 0;
	String keyStr = null;
	String allKeyStr = null;
	String keyFinal = null;
	String allKeyFinal = null;
	String text = null;
	//String valueStr = null;
	String line = null;
	String[] textGram=null;
	
	String[] lineGroup;
	
	/* for testing and file validation  */
	//String testPath = "/home/qty/TAG_Project/TagTraining/swbd_202k_42tags_noplus.test";
        
	
	try {
	    br = new BufferedReader( new FileReader( new File(path)));
	    char[] cont = new char[(int)(new File(path)).length()];
	    br.read(cont);
	    br.close();
	    String conts = new String(cont);
	    br = new BufferedReader(new StringReader(conts));
	    // PrintWriter pwtest = new PrintWriter(new FileWriter(new File(testPath)));
	    while((line = br.readLine())!=null) {
		if(line.indexOf("Dialogue")<0/* && line.indexOf(":sd:")<0 */) {
		    lineGroup = line.split(":"); //line = model_number:utterance
		    //count tags in training map
		    String tag = lineGroup[1];
		    if (!tags_in_trM_.containsKey(tag)) {
			tags_in_trM_.put(tag, new Integer(0));
		    }
		    if (!tt_tags_in_trM_.containsKey(tag)) {
			tt_tags_in_trM_.put(tag, new Integer(0));
		    }
		    tags_in_trM_.put(tag, tags_in_trM_.get(tag) + 1);
		    tt_tags_in_trM_.put(tag, tt_tags_in_trM_.get(tag) + 1);
		    splitIndex = line.lastIndexOf(":") + 1;
		    keyStr = line.substring(0, splitIndex);
		    allKeyStr = lineGroup[0]+":"+"all"+":";
		    text = line.substring(splitIndex);
		    //System.out.println("text: " + text);
		    textGram = getNGram(null, text, skip_sw); //whether to skip stop words filtering
		    if (textGram == null) {
			continue;
		    }
		    for(i=0; i<textGram.length; i++) {
			keyFinal = keyStr + textGram[i];
                        allKeyFinal = allKeyStr + textGram[i];
                        //pwtest.println(keyFinal);
			if(!trainMap.containsKey(keyFinal)) {
			    //System.out.println("key: " + keyFinal);
			    trainMap.put(keyFinal, Integer.valueOf(1));
			}
			else {
			    value = (trainMap.get(keyFinal)).intValue() + 1;
			    trainMap.put(keyFinal, Integer.valueOf(value));
			}

                        if(!allMap.containsKey(allKeyFinal)) {
			    //System.out.println("key: " + keyFinal);
			    allMap.put(allKeyFinal, Integer.valueOf(1));
			}
			else {
			    value = (allMap.get(allKeyFinal)).intValue() + 1;
			    allMap.put(allKeyFinal, Integer.valueOf(value));
			} 
		    }
		}
	    }
	    br.close();
	    //pwtest.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	
    }
	
    /**
     * added for dsarmd tag training
     */
    public void buildTrainMap(Document document,
			      boolean skip_sw,
			      String corpus_type,
			      boolean clustered,
			      boolean add_SE_tags,
			      int doc_id,
			      HashMap doc_utts,
			      String dl_act_str) {
	BufferedReader br;
	int splitIndex = 0;
	int i = 0;
	int value = 0;
	String keyStr = null;
	String allKeyStr = null;
	String keyFinal = null;
	String allKeyFinal = null;
	String text = null;
	//String valueStr = null;
	String line = null;
	String[] textGram=null;
	
	String[] lineGroup;
	
	ArrayList list = dsarmdDP_.parseDAList(document,
					       corpus_type,
					       clustered,
					       dl_act_str);

	//System.out.println("list size: " + list.size());
	int total = 0;
	utts_ = list;
	spks_ = new ArrayList();
	for (i = 0; i < list.size(); i++) {
	    Utterance da = (Utterance)list.get(i);
	    if (!spks_.contains(da.getSpeaker())) { spks_.add(da.getSpeaker()); }
	}
	System.out.println("spks_: " + spks_);
	
	for (i = 0; i < list.size(); i++) {
	    Utterance da = (Utterance)list.get(i);
	    if (da.getTag() == null ||
		da.getTag().trim().length() == 0) {
		//System.out.println("empty tag!!!!!");
		continue;
	    }
	    //count the tags in train map
	    if (!tags_in_trM_.containsKey(da.getTag())) {
		tags_in_trM_.put(da.getTag(), new Integer(0));
	    }
	    tags_in_trM_.put(da.getTag(), tags_in_trM_.get(da.getTag()) + 1);
	    //count the tags for total train map
	    if (!tt_tags_in_trM_.containsKey(da.getTag())) {
		tt_tags_in_trM_.put(da.getTag(), new Integer(0));
	    }
	    tt_tags_in_trM_.put(da.getTag(), tt_tags_in_trM_.get(da.getTag()) + 1);
	    total++;
	    String utt = Util.filterIt(da.getUtterance());
	    String model = Util.getModel(utt);
	    if (add_SE_tags) {
		utt = "<start> " + utt + " <finish>";
	    }
	    keyStr = model + ":" + da.getTag() + ":";
	    allKeyStr = model + ":all:";
	    textGram = getNGram(da, utt, skip_sw);
	    if (textGram == null) {
		continue;
	    }
	    for(int j=0; j<textGram.length; j++) {
		if (textGram[j] == null) continue;
		if (textGram[j] != null &&
		    textGram[j].indexOf(" end ") != -1) {
		    System.out.println("textGram: " + textGram[j]);
		}
		keyFinal = keyStr + textGram[j];
		allKeyFinal = allKeyStr + textGram[j];
		//pwtest.println(keyFinal);
		if(!trainMap.containsKey(keyFinal)) {
		    //System.out.println("key: " + keyFinal);
		    trainMap.put(keyFinal, Integer.valueOf(1));
		}
		else {
		    value = (trainMap.get(keyFinal)).intValue() + 1;
		    trainMap.put(keyFinal, Integer.valueOf(value));
		}
		
		if(!allMap.containsKey(allKeyFinal)) {
		    allMap.put(allKeyFinal, Integer.valueOf(1));
		}
		else {
		    value = (allMap.get(allKeyFinal)).intValue() + 1;
		    allMap.put(allKeyFinal, Integer.valueOf(value));
		} 
		
	    }
	}
	//collect utterance number for training
	if (doc_utts == null ||
	    doc_utts.containsKey(Integer.valueOf(doc_id))) {
	    return;
	}
	doc_utts.put(Integer.valueOf(doc_id), Integer.valueOf(total));
    }
	
    public HashMap getTotalMap(ArrayList list,
			       boolean skip_sw,
			       boolean add_SE_tags,
			       String tag_type) {
	buildTrainMap(list, skip_sw, add_SE_tags, tag_type);
	printTotalMap();
	return totalMap;
    }
    public HashMap getPLTotalMap(ArrayList list,
				 boolean skip_sw,
				 boolean add_SE_tags) {
	buildPLTrainMap(list, skip_sw, add_SE_tags);
	printPLTotalMap();
	return totalMap;
    }

    /**
     * added for dsarmd communication act training
     */
    public void buildTrainMap(ArrayList list,
			      boolean skip_sw,
			      boolean add_SE_tags,
			      String tag_type) {
	BufferedReader br;
	int splitIndex = 0;
	int i = 0;
	int value = 0;
	String keyStr = null;
	String allKeyStr = null;
	String keyFinal = null;
	String allKeyFinal = null;
	String text = null;
	//String valueStr = null;
	String line = null;
	String[] textGram=null;
	
	String[] lineGroup;
	
	//System.out.println("list size: " + list.size());
	int total = 0;
	spks_ = new ArrayList();
	utts_ = list;
	for (i = 0; i < list.size(); i++) {
	    Utterance da = (Utterance)list.get(i);
	    if (!spks_.contains(da.getSpeaker())) { spks_.add(da.getSpeaker()); }
	}
	System.out.println("spks_: " + spks_);
	for (i = 0; i < list.size(); i++) {
	    Utterance da = (Utterance)list.get(i);
	    boolean has_mesotopic = false;
	    if (da.getTopic() != null &&
		da.getTopic().trim().length() > 0) {
		has_mesotopic = true;
	    }
	    String cma = null;
	    if (tag_type.equals(DATagger.POLARITY)) {
		cma = da.getPolarity();
	    } else if (tag_type.equals(DATagger.COMMACT)) {
		cma = da.getCommActType();
	    }
	    //System.out.println("cma: " + cma);
	    if (cma == null ||
		cma.trim().length() == 0) {
		//continue; //only trained on utterances with topic defined
		if (tag_type.equals(DATagger.POLARITY)) {
		    cma = Opinion.NEUTRAL;
		}else if (tag_type.equals(DATagger.COMMACT)) {
		    cma = CommunicationLink.ADDRESSED_TO;
		}
	    } else if (cma.equals(CommunicationLink.CONTINUATION_OF)) {
		continue;
	    }
	    //System.out.println("cma: " + cma);
	    
	    //count the tags in train map
	    if (!tags_in_trM_.containsKey(cma)) {
		tags_in_trM_.put(cma, new Integer(0));
	    }
	    tags_in_trM_.put(cma, tags_in_trM_.get(cma) + 1);
	    //count the tags for total train map
	    if (!tt_tags_in_trM_.containsKey(cma)) {
		tt_tags_in_trM_.put(cma, new Integer(0));
	    }
	    tt_tags_in_trM_.put(cma, tt_tags_in_trM_.get(cma) + 1);
	    total++;
	    String utt = Util.filterIt(da.getUtterance());
	    //utt = CommActGround.textToPosString(utt); //add POS
	    String model = Util.getModel(utt);
	    if (add_SE_tags) {
		utt = "<start> " + utt + " <finish>";
	    }
	    keyStr = model + ":" + cma + ":";
	    if (tag_type.equals(DATagger.POLARITY)) {
		if (has_mesotopic) { keyStr = model + ":" + cma + ":has_mesotopic_"; }
		else { keyStr = model + ":" + cma + ":no_mesotopic_"; }
	    }
	    allKeyStr = model + ":all:";
	    if (tag_type.equals(DATagger.POLARITY)) {
		if (has_mesotopic) { allKeyStr = model + ":all:has_mesotopic_"; }
		else { allKeyStr = model + ":all:no_mesotopic_"; }
	    }
	    textGram = getNGram(da, utt, skip_sw);
	    if (textGram == null) {
		continue;
	    }
	    for(int j=0; j<textGram.length; j++) {
		keyFinal = keyStr + textGram[j];
		allKeyFinal = allKeyStr + textGram[j];
		//pwtest.println(keyFinal);
		if(!trainMap.containsKey(keyFinal)) {
		    //System.out.println("key: " + keyFinal);
		    trainMap.put(keyFinal, Integer.valueOf(1));
		}
		else {
		    value = (trainMap.get(keyFinal)).intValue() + 1;
		    trainMap.put(keyFinal, Integer.valueOf(value));
		}
		
		if(!allMap.containsKey(allKeyFinal)) {
		    allMap.put(allKeyFinal, Integer.valueOf(1));
		}
		else {
		    value = (allMap.get(allKeyFinal)).intValue() + 1;
		    allMap.put(allKeyFinal, Integer.valueOf(value));
		} 
		
	    }
	}
    }
	
    /**
     * added for dsarmd speaker opinion training
     */
    public void buildPLTrainMap(ArrayList list,
				boolean skip_sw,
				boolean add_SE_tags) {
	BufferedReader br;
	int splitIndex = 0;
	int i = 0;
	int value = 0;
	String keyStr = null;
	String allKeyStr = null;
	String keyFinal = null;
	String allKeyFinal = null;
	String text = null;
	//String valueStr = null;
	String line = null;
	String[] textGram=null;
	
	String[] lineGroup;
	
	//System.out.println("list size: " + list.size());
	int total = 0;
	for (i = 0; i < list.size(); i++) {
	    Utterance da = (Utterance)list.get(i);
	    String cma = da.getPolarity();
	    if (cma == null ||
		cma.trim().length() == 0) {
		cma = Opinion.NEUTRAL;
	    }
	    
	    //count the tags in train map
	    if (!tags_in_trM_.containsKey(cma)) {
		tags_in_trM_.put(cma, new Integer(0));
	    }
	    tags_in_trM_.put(cma, tags_in_trM_.get(cma) + 1);
	    //count the tags for total train map
	    if (!tt_tags_in_trM_.containsKey(cma)) {
		tt_tags_in_trM_.put(cma, new Integer(0));
	    }
	    tt_tags_in_trM_.put(cma, tt_tags_in_trM_.get(cma) + 1);
	    total++;
	    String utt = Util.filterIt(da.getUtterance());
	    String model = Util.getModel(utt);
	    if (add_SE_tags) {
		utt = "<start> " + utt + " <finish>";
	    }
	    keyStr = model + ":" + cma + ":";
	    allKeyStr = model + ":all:";
	    textGram = getNGram(da, utt, skip_sw);
	    if (textGram == null) {
		continue;
	    }
	    for(int j=0; j<textGram.length; j++) {
		keyFinal = keyStr + textGram[j];
		allKeyFinal = allKeyStr + textGram[j];
		//pwtest.println(keyFinal);
		if(!trainMap.containsKey(keyFinal)) {
		    //System.out.println("key: " + keyFinal);
		    trainMap.put(keyFinal, Integer.valueOf(1));
		}
		else {
		    value = (trainMap.get(keyFinal)).intValue() + 1;
		    trainMap.put(keyFinal, Integer.valueOf(value));
		}
		
		if(!allMap.containsKey(allKeyFinal)) {
		    allMap.put(allKeyFinal, Integer.valueOf(1));
		}
		else {
		    value = (allMap.get(allKeyFinal)).intValue() + 1;
		    allMap.put(allKeyFinal, Integer.valueOf(value));
		} 
		
	    }
	}
    }
	
    /**
     * added for dsarmd tag training
     */
    public void buildTrainMap(HashMap training_set,
			      boolean skip_sw) {
	int splitIndex = 0;
	int i = 0;
	int value = 0;
	String keyStr = null;
	String allKeyStr = null;
	String keyFinal = null;
	String allKeyFinal = null;
	String text = null;
	//String valueStr = null;
	String line = null;
	String[] textGram=null;
	
	String[] lineGroup;
	
	int total = 0;
	ArrayList d_ids = new ArrayList(training_set.keySet());
	for (i = 0; i < d_ids.size(); i++) {
	    String d_id = (String)d_ids.get(i);
	    ArrayList utts = (ArrayList)training_set.get(d_id);
	    for (int j = 0; j < utts.size(); j++) {
		line = (String)utts.get(j);
		lineGroup = line.split(":"); //line = model_number:utterance
		//count tags in training map
		String tag = lineGroup[1];
		if (!tags_in_trM_.containsKey(tag)) {
		    tags_in_trM_.put(tag, new Integer(0));
		}
		tags_in_trM_.put(tag, tags_in_trM_.get(tag) + 1);
		splitIndex = line.lastIndexOf(":") + 1;
		keyStr = line.substring(0, splitIndex);
		allKeyStr = lineGroup[0]+":"+"all"+":";
		text = line.substring(splitIndex);
		//		text = "<start> " + text + " <finish>"; already been added
		//System.out.println("text: " + text);
		textGram = getNGram(null, text, skip_sw); //whether to skip stop words filtering
		if (textGram == null) {
		    continue;
		}
		for(int k = 0; k < textGram.length; k++) {
		    keyFinal = keyStr + textGram[k];
		    allKeyFinal = allKeyStr + textGram[k];
		    //pwtest.println(keyFinal);
		    if(!trainMap.containsKey(keyFinal)) {
			//System.out.println("key: " + keyFinal);
			trainMap.put(keyFinal, Integer.valueOf(1));
		    }
		    else {
			value = (trainMap.get(keyFinal)).intValue() + 1;
			trainMap.put(keyFinal, Integer.valueOf(value));
		    }
		    
		    if(!allMap.containsKey(allKeyFinal)) {
			//System.out.println("key: " + keyFinal);
			allMap.put(allKeyFinal, Integer.valueOf(1));
		    }
		    else {
			value = (allMap.get(allKeyFinal)).intValue() + 1;
			allMap.put(allKeyFinal, Integer.valueOf(value));
		    } 
		}
		
	    }
	}
    }
	
    public HashMap getTrainMap() {
	return trainMap;
    }
    
    public HashMap getAllMap() {
	return allMap;
    }
    
    public void clearMap() {
	trainMap.clear();
	allMap.clear();
    }

    public void clearTagsInTrMap() {
	tags_in_trM_.clear();
    }
    
    public void printTrainMap___(String path) {
	//	filterMap();
	PrintWriter pw;
	String keyStr=null;
	String allKeyStr = null;
	String[] arrayKeyStr;
	int value = 0;
	int allValue = 0;
	double prediction = 0.0;
	Iterator mapIterator = trainMap.entrySet().iterator();
	Iterator allMapIterator = allMap.entrySet().iterator();
	try {
	    pw = new PrintWriter(new FileWriter(new File(path)));
	    while(mapIterator.hasNext()) {
		Map.Entry mapping = (Map.Entry)mapIterator.next();
		keyStr =(String)mapping.getKey();
		arrayKeyStr = keyStr.split(":");
		allKeyStr = arrayKeyStr[0]+":" + "all" + ":" + arrayKeyStr[2];
		value = ((Integer)mapping.getValue()).intValue();
		allValue = ((Integer)allMap.get(allKeyStr)).intValue();
		prediction = (double)value/(double)allValue;
		// System.out.println("String: " +keyStr+" value: " + value + "  prediction: " + prediction);
		/* if(value >= THRESHOLD_FREQUENCY && prediction >= THRESHOLD_PREDICTION 
		   && !keyStr.equals("<start>") && !keyStr.endsWith("<finish>"))*/
		
		if(!arrayKeyStr[2].equals("<start>"))
		    {
			pw.println("["+keyStr+":"+value+":"+prediction+"]");
			// System.out.println("["+keyStr+":"+value+"]");
			// System.out.println("good");
		    }
		// System.out.println("what is wrong");
	    }
	    /*
	      while(allMapIterator.hasNext()) {
	      Map.Entry allMapping = (Map.Entry)allMapIterator.next();
	      allKeyStr = (String)allMapping.getKey();
	      value = ((Integer)allMapping.getValue()).intValue();
	      pw.println("[" + allKeyStr + ":" + value +"]");
	      }
	    */
	    
	    pw.close();
	    clearMap();
	    
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
    }
	
    public void printTrainMap(String path) {
	//	filterMap();
	PrintWriter pw;
	String keyStr=null; //model_number:tag:ngram
	String allKeyStr = null;//model_number:all:ngram
	String[] arrayKeyStr;
	
	String model;
	String tag;
	String words; //ngram
	String singleKey; //model_number:ngram
	int freq;
	double pred;
	
	int value = 0;
        int allValue = 0;
        double prediction = 0.0;
	Iterator mapIterator = trainMap.entrySet().iterator();
        //Iterator allMapIterator = allMap.entrySet().iterator();
	singleMap.clear(); //clear previous records
	try {
	    pw = new PrintWriter(new FileWriter(new File(path)));
	    while(mapIterator.hasNext()) {
		Map.Entry mapping = (Map.Entry)mapIterator.next();
		keyStr =(String)mapping.getKey();
		arrayKeyStr = keyStr.split(":");
                allKeyStr = arrayKeyStr[0]+":" + "all" + ":" + arrayKeyStr[2];
		value = ((Integer)mapping.getValue()).intValue();
                allValue = ((Integer)allMap.get(allKeyStr)).intValue();
                prediction = (double)value/(double)allValue;
                
                model = arrayKeyStr[0];
                tag = arrayKeyStr[1];
                words = arrayKeyStr[2];
                freq = value;
                pred = prediction;
                singleKey = model + ":" + words; 
		// System.out.println("String: " +keyStr+" value: " + value + "  prediction: " + prediction);
		/* if(value >= THRESHOLD_FREQUENCY && prediction >= THRESHOLD_PREDICTION 
		   && !keyStr.equals("<start>") && !keyStr.endsWith("<finish>"))*/
		
                if(!arrayKeyStr[2].equals("<start>") && !arrayKeyStr[2].equals("<finish>") && 
		   (freq>=THRESHOLD_FREQUENCY
		    && pred>=THRESHOLD_PREDICTION ||
		    freq>=HI_THRESHOLD_FREQUENCY
		    && pred>=HI_THRESHOLD_PREDICTION ||
		    freq>=HII_THRESHOLD_FREQUENCY
		    && pred>=HII_THRESHOLD_PREDICTION)) {
		    if(!singleMap.containsKey(singleKey)) {
                        ObjValue singleValue = new ObjValue(singleKey, tag, freq, pred);
                        singleMap.put(singleKey, singleValue);
                    } else {
                        double originalPred = (singleMap.get(singleKey)).getPrediction();
			int originalFreq = (singleMap.get(singleKey)).getFrequency();
                        if(pred > originalPred) {
			    
			    (singleMap.get(singleKey)).setTag(tag);
			    (singleMap.get(singleKey)).setFrequency(freq);
			    (singleMap.get(singleKey)).setPrediction(pred);
			    
                        } else if(prediction == originalPred) {
                            if(freq > originalFreq) {
				(singleMap.get(singleKey)).setTag(tag);
				(singleMap.get(singleKey)).setFrequency(freq);
				(singleMap.get(singleKey)).setPrediction(pred); 
                            }
                        }
                    }
		    
		    pw.println("["+keyStr+":"+value+":"+prediction+"]");
		    // System.out.println("["+keyStr+":"+value+"]");
		    // System.out.println("good");
		}
		// System.out.println("what is wrong");
	    }
	    /*
	      while(allMapIterator.hasNext()) {
	      Map.Entry allMapping = (Map.Entry)allMapIterator.next();
	      allKeyStr = (String)allMapping.getKey();
	      value = ((Integer)allMapping.getValue()).intValue();
	      pw.println("[" + allKeyStr + ":" + value +"]");
	      }
	    */
	    
	    pw.close();
	    clearMap();
	    
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
    }
	
    
    public void printTotalMap(String path) {
	//	filterMap();
	PrintWriter pw;
	String keyStr=null; //model_number:tag:ngram
	String allKeyStr = null;//model_number:all:ngram
	String[] arrayKeyStr;
	
	String model;
	String tag;
	String words; //ngram
	String singleKey; //model_number:ngram
	int freq;
	double pred;
	
	int value = 0;
        int allValue = 0;
        double prediction = 0.0;
	Iterator mapIterator = trainMap.entrySet().iterator();
	//System.out.println("cue list length before threshold: " + trainMap.size());
        //Iterator allMapIterator = allMap.entrySet().iterator();
	//singleMap.clear(); //clear previous records
	try {
	    int kept = 0;
	    pw = new PrintWriter(new FileWriter(new File(path)));
	    while(mapIterator.hasNext()) {
		Map.Entry mapping = (Map.Entry)mapIterator.next();
		keyStr =(String)mapping.getKey();
		arrayKeyStr = keyStr.split(":");
		if (arrayKeyStr.length < 3) {
		    //System.out.println("keyStr is: " + keyStr);
		}
		allKeyStr = arrayKeyStr[0]+":" + "all" + ":" + arrayKeyStr[2];
		//System.out.println("allMap: " + allMap);
		value = ((Integer)mapping.getValue()).intValue();
		//System.out.println("allKeyStr is: " + allKeyStr);
		allValue = ((Integer)allMap.get(allKeyStr)).intValue();
		prediction = (double)value/(double)allValue;
                
                model = arrayKeyStr[0];
                tag = arrayKeyStr[1];
                words = arrayKeyStr[2];
                freq = value;
                pred = prediction;
                singleKey = model + ":" + words; 
		// System.out.println("String: " +keyStr+" value: " + value + "  prediction: " + prediction);
		/* if(value >= THRESHOLD_FREQUENCY && prediction >= THRESHOLD_PREDICTION 
		   && !keyStr.equals("<start>") && !keyStr.endsWith("<finish>"))*/
		
                if(!arrayKeyStr[2].equals("<start>") && !arrayKeyStr[2].equals("<finish>") && 
		   freq>=THRESHOLD_FREQUENCY
		   /*&& pred>=THRESHOLD_PREDICTION*/) {
		    kept++;
		    if(!totalMap.containsKey(singleKey)) {
                        ObjValue singleValue = new ObjValue(singleKey, tag, freq, pred);
                        totalMap.put(singleKey, singleValue);
                    } else {
                        double originalPred = (totalMap.get(singleKey)).getPrediction();
			int originalFreq = (totalMap.get(singleKey)).getFrequency();
                        if(pred > originalPred) {
			    
			    (totalMap.get(singleKey)).setTag(tag);
			    (totalMap.get(singleKey)).setFrequency(freq);
			    (totalMap.get(singleKey)).setPrediction(pred);
			    
                        } else if(prediction == originalPred) {
                            if(freq > originalFreq) {
				(totalMap.get(singleKey)).setTag(tag);
				(totalMap.get(singleKey)).setFrequency(freq);
				(totalMap.get(singleKey)).setPrediction(pred); 
                            }
                        }
                    }
                    prediction = Math.rint(prediction * 1000)/1000;       	  
		    pw.println("["+keyStr+":"+value+":" + prediction+"]");
		    // System.out.println("["+keyStr+":"+value+"]");
		    // System.out.println("good");
		}
		// System.out.println("what is wrong");
	    }
	    //System.out.println("After threshold: " + kept);
	    /*
	      while(allMapIterator.hasNext()) {
	      Map.Entry allMapping = (Map.Entry)allMapIterator.next();
	      allKeyStr = (String)allMapping.getKey();
	      value = ((Integer)allMapping.getValue()).intValue();
	      pw.println("[" + allKeyStr + ":" + value +"]");
	      }
	    */
	    
	    pw.close();
	    clearMap();
	    
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public void printPLTotalMap() {
	//	filterMap();
	String keyStr=null; //model_number:tag:ngram
	String allKeyStr = null;//model_number:all:ngram
	String[] arrayKeyStr;
	
	String model;
	String tag;
	String words; //ngram
	String singleKey; //model_number:ngram
	int freq;
	double pred;
	
	int value = 0;
        int allValue = 0;
        double prediction = 0.0;
	Iterator mapIterator = trainMap.entrySet().iterator();
	//System.out.println("cue list length before threshold: " + trainMap.size());
        //Iterator allMapIterator = allMap.entrySet().iterator();
	//singleMap.clear(); //clear previous records
	    int kept = 0;
	    while(mapIterator.hasNext()) {
		Map.Entry mapping = (Map.Entry)mapIterator.next();
		keyStr =(String)mapping.getKey();
		arrayKeyStr = keyStr.split(":");
		if (arrayKeyStr.length < 3) {
		    //System.out.println("keyStr is: " + keyStr);
		}
		allKeyStr = arrayKeyStr[0]+":" + "all" + ":" + arrayKeyStr[2];
		value = ((Integer)mapping.getValue()).intValue();
		//		System.out.println("allKeyStr is: " + allKeyStr);
		allValue = ((Integer)allMap.get(allKeyStr)).intValue();
		prediction = (double)value/(double)allValue;
                
                model = arrayKeyStr[0];
                tag = arrayKeyStr[1];
                words = arrayKeyStr[2];
                freq = value;
                pred = prediction;
                singleKey = model + ":" + words; 
		// System.out.println("String: " +keyStr+" value: " + value + "  prediction: " + prediction);
		/* if(value >= THRESHOLD_FREQUENCY && prediction >= THRESHOLD_PREDICTION 
		   && !keyStr.equals("<start>") && !keyStr.endsWith("<finish>"))*/
		
                if(!arrayKeyStr[2].equals("<start>") && !arrayKeyStr[2].equals("<finish>") && freq>=THRESHOLD_FREQUENCY
		   /*&& pred>=THRESHOLD_PREDICTION*/) {
		    kept++;
		    if(!totalMap.containsKey(singleKey)) {
                        ObjValue singleValue = new ObjValue(singleKey, tag, freq, pred);
                        totalMap.put(singleKey, singleValue);
                    } else {
                        double originalPred = (totalMap.get(singleKey)).getPrediction();
			int originalFreq = (totalMap.get(singleKey)).getFrequency();
                        if(pred > originalPred) {
			    
			    (totalMap.get(singleKey)).setTag(tag);
			    (totalMap.get(singleKey)).setFrequency(freq);
			    (totalMap.get(singleKey)).setPrediction(pred);
			    
                        } else if(prediction == originalPred) {
                            if(freq > originalFreq) {
				(totalMap.get(singleKey)).setTag(tag);
				(totalMap.get(singleKey)).setFrequency(freq);
				(totalMap.get(singleKey)).setPrediction(pred); 
                            }
                        }
                    }
                    prediction = Math.rint(prediction * 1000)/1000;       	  
		    // System.out.println("["+keyStr+":"+value+"]");
		    // System.out.println("good");
		}
		// System.out.println("what is wrong");
	    }
	    //System.out.println("After threshold: " + kept);
	    /*
	      while(allMapIterator.hasNext()) {
	      Map.Entry allMapping = (Map.Entry)allMapIterator.next();
	      allKeyStr = (String)allMapping.getKey();
	      value = ((Integer)allMapping.getValue()).intValue();
	      pw.println("[" + allKeyStr + ":" + value +"]");
	      }
	    */
	    
	    clearMap();
	    
    }
    
    public void printTotalMap() {
	//	filterMap();
	String keyStr=null; //model_number:tag:ngram
	String allKeyStr = null;//model_number:all:ngram
	String[] arrayKeyStr;
	
	String model;
	String tag;
	String words; //ngram
	String singleKey; //model_number:ngram
	int freq;
	double pred;
	
	int value = 0;
        int allValue = 0;
        double prediction = 0.0;
	Iterator mapIterator = trainMap.entrySet().iterator();
	//System.out.println("cue list length before threshold: " + trainMap.size());
        //Iterator allMapIterator = allMap.entrySet().iterator();
	//singleMap.clear(); //clear previous records
	    int kept = 0;
	    while(mapIterator.hasNext()) {
		Map.Entry mapping = (Map.Entry)mapIterator.next();
		keyStr =(String)mapping.getKey();
		arrayKeyStr = keyStr.split(":");
		if (arrayKeyStr.length < 3) {
		    //System.out.println("keyStr is: " + keyStr);
		}
		//System.out.println("keyStr is: " + keyStr);
		allKeyStr = arrayKeyStr[0]+":" + "all" + ":" + arrayKeyStr[2];
		value = ((Integer)mapping.getValue()).intValue();
		//		System.out.println("allKeyStr is: " + allKeyStr);
		allValue = ((Integer)allMap.get(allKeyStr)).intValue();
		prediction = (double)value/(double)allValue;
                
                model = arrayKeyStr[0];
                tag = arrayKeyStr[1];
                words = arrayKeyStr[2];
                freq = value;
                pred = prediction;
                singleKey = model + ":" + words; 
		// System.out.println("String: " +keyStr+" value: " + value + "  prediction: " + prediction);
		/* if(value >= THRESHOLD_FREQUENCY && prediction >= THRESHOLD_PREDICTION 
		   && !keyStr.equals("<start>") && !keyStr.endsWith("<finish>"))*/
		
                if(!arrayKeyStr[2].equals("<start>") && !arrayKeyStr[2].equals("<finish>") && freq>=THRESHOLD_FREQUENCY
		   /*&& pred>=THRESHOLD_PREDICTION*/) {
		    kept++;
		    if(!totalMap.containsKey(singleKey)) {
                        ObjValue singleValue = new ObjValue(singleKey, tag, freq, pred);
                        totalMap.put(singleKey, singleValue);
                    } else {
                        double originalPred = (totalMap.get(singleKey)).getPrediction();
			int originalFreq = (totalMap.get(singleKey)).getFrequency();
                        if(pred > originalPred) {
			    
			    (totalMap.get(singleKey)).setTag(tag);
			    (totalMap.get(singleKey)).setFrequency(freq);
			    (totalMap.get(singleKey)).setPrediction(pred);
			    
                        } else if(prediction == originalPred) {
                            if(freq > originalFreq) {
				(totalMap.get(singleKey)).setTag(tag);
				(totalMap.get(singleKey)).setFrequency(freq);
				(totalMap.get(singleKey)).setPrediction(pred); 
                            }
                        }
                    }
                    prediction = Math.rint(prediction * 1000)/1000;       	  
		    // System.out.println("["+keyStr+":"+value+"]");
		    // System.out.println("good");
		}
		// System.out.println("what is wrong");
	    }
	    //System.out.println("After threshold: " + kept);
	    /*
	      while(allMapIterator.hasNext()) {
	      Map.Entry allMapping = (Map.Entry)allMapIterator.next();
	      allKeyStr = (String)allMapping.getKey();
	      value = ((Integer)allMapping.getValue()).intValue();
	      pw.println("[" + allKeyStr + ":" + value +"]");
	      }
	    */
	    
	    clearMap();
	    
    }
    
    public HashMap getTotalMap(String path,
			       ArrayList docs,
			       boolean skip_sw,
			       String corpus_type,
			       boolean clustered,
			       boolean add_SE_tags,
			       HashMap doc_utts_,
			       String dl_act_str) {
	for (int i = 0; i < docs.size(); i++) {
	    Document doc = (Document)docs.get(i);
	    buildTrainMap(doc, skip_sw, corpus_type, clustered, add_SE_tags, i, doc_utts_, dl_act_str);
	}
	printTotalMap(path+".ngram");
	//totalMap.putAll(singleMap);
	return totalMap;
    }
	
    public HashMap getTotalMap(String path,
			       boolean skip_sw) {
	buildTrainMap(path, skip_sw);
	printTotalMap(path+".ngram");
	//totalMap.putAll(singleMap);
	return totalMap;
    }
	
    public HashMap getPropertyMap(){
	return singleMap;
    }
    
    //the following code is used to output single map for testing, also the output will be sorted by the use frequency from Maximun to Minimum.
    // the insertion sort algorithm is used
    
    public void test_singleMap(String path) {
	int i = 0;
	int j = 0;
	int useFreq = 0;
	
	double averageUseFreq = 0.0;
	double averageUsePred = 0.0;
	double sumUseFreq = 0.0;
	double sumUsePred = 0.0;
	int useCount = 0;
	
	String singlePath = path;
	PrintWriter pwSingle;
	Iterator mapIterator = singleMap.entrySet().iterator();
	ObjValue[] sortArray = new ObjValue[singleMap.size()];
	try {
	    pwSingle = new PrintWriter(new FileWriter(new File(singlePath)));
	    pwSingle.println("model_number:ngram | tag | frequency | prediction | correct frequency | wrong frequency | use frequency");
	    pwSingle.println(" ");
	    while(mapIterator.hasNext()) {
		Map.Entry mapping = (Map.Entry)mapIterator.next();
		String keyStr =(String)mapping.getKey();
		ObjValue value = (ObjValue)mapping.getValue();
		//implementing of the insertion algorithm
		if(j==0) sortArray[j] = value;
		else {
		    i = j-1;					
		    useFreq = ((ObjValue)sortArray[i]).getUseFrequency();
		    while (i>=0 && useFreq > value.getUseFrequency()) {
			sortArray[i+1] = sortArray[i];
			i--;
			if(i>=0) {
			    useFreq = ((ObjValue)sortArray[i]).getUseFrequency();
			}
		    }
		    sortArray[i+1] = value;
		}
		j++;
		
		
	    }
	    
	    for(i = sortArray.length-1; i>=0; i--) {
		pwSingle.println(sortArray[i].toString());
		
		if(sortArray[i].getUseFrequency()>0) {
		    sumUseFreq += sortArray[i].getUseFrequency();
		    sumUsePred += sortArray[i].getPrediction();
		    useCount++;
		}
	    }
	    
	    averageUseFreq = sumUseFreq/useCount;
	    averageUsePred = sumUsePred/useCount;
	    
	    pwSingle.println("**********************************************");
	    pwSingle.println("the average frequency score of used N-Grams: " + averageUseFreq);
	    pwSingle.println("the average prediction score of used N-Grams: " + averageUsePred);
	    pwSingle.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    // path = "/home/sshaikh/TAG_Project/TagTraining/TotalPropertyNGram"
    public void test_totalMap(String path ) {
     	int i = 0;
	int j = 0;
	//int useFreq = 0;
        double correctPercentage = 0.0;
        double wrongPercentage = 0.0;
	
	double averageCorrectPercentage = 0.0;
	double sumCorrectPercentage = 0.0;
	//double sumUsePred = 0.0;
	int useCount = 0;
	
	String singlePath = path;
	PrintWriter pwTotal_correct;
        PrintWriter pwTotal_wrong;
	Iterator mapIterator = totalMap.entrySet().iterator();
	ObjValue[] sortArray = new ObjValue[totalMap.size()];
	try {
	    pwTotal_correct = new PrintWriter(new FileWriter(new File(singlePath+".correct")));
            pwTotal_wrong = new PrintWriter(new FileWriter(new File(singlePath+".wrong")));
	    pwTotal_correct.println("model_number:ngram : tag : prediction : frequency : use frequency : correct percentage : wrong percentage");
            pwTotal_wrong.println("model_number:ngram : tag : prediction : frequency : use frequency : correct percentage : wrong percentage");
	    pwTotal_correct.println(" ");
            pwTotal_wrong.println(" ");
	    
	    while(mapIterator.hasNext()) {
		Map.Entry mapping = (Map.Entry)mapIterator.next();
		String keyStr =(String)mapping.getKey();
		ObjValue value = (ObjValue)mapping.getValue();
		//implementing of the insertion algorithm
		if(j==0) sortArray[j] = value;
		else {
		    i = j-1;					
		    //useFreq = ((ObjValue)sortArray[i]).getUseFrequency();
                    correctPercentage = ((ObjValue)sortArray[i]).getCorrectPercentage();
                    wrongPercentage = ((ObjValue)sortArray[i]).getWrongPercentage();
		    while (i>=0 && ((correctPercentage > value.getCorrectPercentage()) ||
				    ((correctPercentage + wrongPercentage) != 0 && value.getWrongPercentage() == 0 &&
				     value.getCorrectPercentage() == 0))) {
			sortArray[i+1] = sortArray[i];
			i--;
			if(i>=0) {
			    correctPercentage = ((ObjValue)sortArray[i]).getCorrectPercentage();
			    wrongPercentage = ((ObjValue)sortArray[i]).getWrongPercentage();
			}
		    }
		    sortArray[i+1] = value;
		}
		
		j++;
				
		
	    }
	    
	    for(i = sortArray.length-1; i>=0; i--) {
		pwTotal_correct.println(sortArray[i].toTotalString());
		
		if(sortArray[i].getCorrectPercentage()>0) {
		    sumCorrectPercentage += sortArray[i].getCorrectPercentage();
		    useCount++;
		}
	    }
	    /*        
		      for(i = 0; i<sortArray.length; i++) {
		      pwTotal_wrong.println(sortArray[i].toTotalString());
		      
		      if(sortArray[i].getCorrectPercentage()>0) {
		      sumCorrectPercentage += sortArray[i].getCorrectPercentage();
		      useCount++;
		      }
		      
		      }
	    */
			
	    averageCorrectPercentage = sumCorrectPercentage/useCount;
	    
	    pwTotal_correct.println("**********************************************");
	    pwTotal_correct.println("the average percentage of correctly used N-Grams: " + averageCorrectPercentage);
	    pwTotal_correct.close();
	    pwTotal_wrong.println("**********************************************");
	    pwTotal_wrong.println("the average percentage of correctly used N-Grams: " + averageCorrectPercentage);
	    pwTotal_wrong.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
    }
    
    public String[] getNGramOld(Utterance utt,
				String text,
				boolean skip_sw) {
	String[] words;
	String[] NGram;
	String gramStr = null;
	int splitLength = 0;
	int l = 0, i = 0, j = 0, k = 0;
	
	words = text.split(" ");
	splitLength = words.length>4? 4 : words.length;
	//System.out.println("text: " + text);
	//System.out.println("wordslength: " + words.length);
	if(splitLength == 4) {
	    NGram = new String[4*words.length - 6];
	} else {
	    NGram = new String[(words.length+1)*words.length/2];
	}
	
	for(l=1; l<=splitLength; l++) {// length of subgram
	    for(i=0; i<words.length-l+1; i++){
		gramStr = "";
		for(j=i; j<i+l; j++) {
		    gramStr +=words[j]+" ";
		}
		NGram[k++] = gramStr.trim();
	    }
	}
	
	return NGram;
	
    }
    
    public String[] getNGram(Utterance utt,
			     String text,
			     boolean skip_sw) {
	String[] words;
	String[] NGram;
	String gramStr = null;
	int splitLength = 0;
	int l = 0, i = 0, j = 0, k = 0;
	
	//System.out.println("text for getNGram is: " + text);
	words = text.split("[\\s]+");
	//ArrayList wds = new ArrayList(Arrays.asList(words));
	//System.out.println("wds: " + wds);
	String[] ori_words = text.split("[\\s]+");
	if (!skip_sw) {
	    ArrayList wds = new ArrayList(Arrays.asList(words));
	    Util.sws_.filterIt(wds);
	    words = new String [wds.size()];
	    words = (String [])wds.toArray(words);
	    if (words.length == 2) {
		//	    System.out.println("only start and finish are left: " + Arrays.asList(words));
		return null;
	    }
	}
	if (words.length > 9) { //words length > 7 + (start and end)
	    String[] tmp_words = new String [8];
	    for (i = 0; i < 8; i++) {
		tmp_words[i] = words[i];
	    }
	    words = tmp_words;
	    ori_words = (String[]) Arrays.asList(tmp_words).toArray();
	}
	ArrayList wds = new ArrayList(Arrays.asList(words));
	for (i = 0; i < wds.size(); i++) {
	    String wd = (String)wds.get(i);
	    if (spks_.contains(wd)) {
		wds.set(i, "@SPEAKER@");
	    }
	    if (wd.equals("<start>") ||
		wd.equals("<finish>")) continue;
	    if (wn_ != null) {
		String lemma = wn_.getLemma(wd);
		if (lemma != null) {
		    wds.set(i, lemma);
		}
	    }
	    wd = (String)wds.get(i);
	    /*
	    if (wd.equals("end")) {
		System.out.println("utt content: " + utt.getContent());
		System.out.println("utt comm act type: " + utt.getCommActType());
	    }
	    */
	    if (utt != null &&
		utt.getCommActType() != null) {
		wds.set(i, utt.getCommActType() + "_" + wd);
	    }
	    /*
	    if (wd.equals("end")) {
		System.out.println("after set utt comm act type: " + wds);
	    }
	    */
	    wd = (String)wds.get(i);
	    if (utt != null) {
		String linkto = utt.getRespTo();
		if (linkto != null &&
		    linkto.indexOf(":") != -1) {
		    String turn = linkto.split(":")[1];
		    turn = turn.split("\\.")[0];
		    Utterance ln_utt = getUtterance(turn);
		    if (ln_utt != null) {
			wds.set(i, ln_utt.getTag() + "_" + wd);
		    }
		}
	    }

	}
	words = (String [])wds.toArray(words);
	/*
	if (text.indexOf(" end ") != -1) {
	    System.out.println("The filter words are: " + Arrays.asList(words));
	}
	*/
	splitLength = ori_words.length>4? 4 : words.length;
	//System.out.println("text: " + text);
	//System.out.println("wordslength: " + words.length);
	if(splitLength == 4) {
	    NGram = new String[4*ori_words.length - 6];
	} else {
	    NGram = new String[(ori_words.length+1)*ori_words.length/2];
	}
	
	for(l=1; l<=splitLength; l++) {// length of subgram
	    /*comment out at 11/16/10
	    if (l > 2) {
		words = ori_words;
	    }
	    */
	    for(i=0; i<words.length-l+1; i++){
		gramStr = "";
		for(j=i; j<i+l; j++) {
		    gramStr +=words[j]+" ";
		}
		if (gramStr.trim().endsWith("<start>") ||
		    gramStr.trim().endsWith("<finish>") ||
		    gramStr.trim().endsWith("_i") ||
		    gramStr.trim().endsWith("_my") ||
		    gramStr.trim().endsWith("_you") ||
		    gramStr.trim().endsWith("_your") ||
		    gramStr.trim().endsWith("_it") ||
		    gramStr.trim().endsWith("_him") ||
		    gramStr.trim().endsWith("_he") ||
		    gramStr.trim().endsWith("_his") ||
		    gramStr.trim().endsWith("_they") ||
		    gramStr.trim().endsWith("_our") ||
		    gramStr.trim().endsWith("_we") ||
		    gramStr.trim().endsWith("_myself") ||
		    gramStr.trim().endsWith("_yourself") ||
		    gramStr.trim().endsWith("_themselves") ||
		    gramStr.trim().endsWith("_himslef") ||
		    gramStr.trim().endsWith("_herself") ||
		    gramStr.trim().endsWith("_itself") ||
		    gramStr.trim().endsWith("_is") ||
		    gramStr.trim().endsWith("_am") ||
		    gramStr.trim().endsWith("_are") ||
		    gramStr.trim().endsWith("_was") ||
		    gramStr.trim().endsWith("_were") ||
		    gramStr.trim().endsWith("_she") ||
		    gramStr.trim().endsWith("_has") ||
		    gramStr.trim().endsWith("_have") ||
		    gramStr.trim().endsWith("_had") ||
		    gramStr.trim().endsWith(" i") ||
		    gramStr.trim().endsWith(" my") ||
		    gramStr.trim().endsWith(" you") ||
		    gramStr.trim().endsWith(" your") ||
		    gramStr.trim().endsWith(" it") ||
		    gramStr.trim().endsWith(" him") ||
		    gramStr.trim().endsWith(" he") ||
		    gramStr.trim().endsWith(" his") ||
		    gramStr.trim().endsWith(" they") ||
		    gramStr.trim().endsWith(" our") ||
		    gramStr.trim().endsWith(" we") ||
		    gramStr.trim().endsWith(" myself") ||
		    gramStr.trim().endsWith(" yourself") ||
		    gramStr.trim().endsWith(" themselves") ||
		    gramStr.trim().endsWith(" himslef") ||
		    gramStr.trim().endsWith(" herself") ||
		    gramStr.trim().endsWith(" itself") ||
		    gramStr.trim().endsWith(" is") ||
		    gramStr.trim().endsWith(" are") ||
		    gramStr.trim().endsWith(" was") ||
		    gramStr.trim().endsWith(" were") ||
		    gramStr.trim().endsWith(" has") ||
		    gramStr.trim().endsWith(" have") ||
		    gramStr.trim().endsWith(" had") ||
		    gramStr.trim().endsWith(" am") ||
		    gramStr.trim().endsWith(" she") ||
		    gramStr.trim().equals("i") ||
		    gramStr.trim().equals("my") ||
		    gramStr.trim().equals("you") ||
		    gramStr.trim().equals("your") ||
		    gramStr.trim().equals("it") ||
		    gramStr.trim().equals("him") ||
		    gramStr.trim().equals("he") ||
		    gramStr.trim().equals("his") ||
		    gramStr.trim().equals("they") ||
		    gramStr.trim().equals("our") ||
		    gramStr.trim().equals("we") ||
		    gramStr.trim().equals("myself") ||
		    gramStr.trim().equals("yourself") ||
		    gramStr.trim().equals("themselves") ||
		    gramStr.trim().equals("himslef") ||
		    gramStr.trim().equals("herself") ||
		    gramStr.trim().equals("itself") ||
		    gramStr.trim().equals("is") ||
		    gramStr.trim().equals("are") ||
		    gramStr.trim().equals("was") ||
		    gramStr.trim().equals("were") ||
		    gramStr.trim().equals("has") ||
		    gramStr.trim().equals("have") ||
		    gramStr.trim().equals("had") ||
		    gramStr.trim().equals("am") ||
		    gramStr.trim().equals("she")) continue;
		NGram[k++] = gramStr.trim();
	    }
	}
	if (!skip_sw) {
	    NGram = (String[])Arrays.asList(NGram).toArray();
	}
	return NGram;
			
    }



    public HashMap getTagsFromTrSet() {
	return tags_in_trM_;
    }
	
    public HashMap getTotalTagsFromTrSet() {
	return tt_tags_in_trM_;
    }

    public ArrayList getSpeakers() { return spks_; }

    public Utterance getUtterance(String turn) { 
	if (utts_ == null) return null;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt = (Utterance)utts_.get(i);
	    if (utt.getTurn().equals(turn)) return utt;
	}
	System.out.println("couldn't find the turn: " + turn);
	return null;
    }
	
	/*
	public static void main(String[] args) {
	    long start;
	    long end;
	    start = System.currentTimeMillis();
	    TagTraining tagTraining = new TagTraining();
	    String inPath = "/home/sshaikh/TAG_Project/TagTraining/swbd_202k_42tags_noplus";
	    String outPath = "/home/sshaikh/TAG_Project/TagTraining/swbd_202k_42tags_noplus.ngram";
	    tagTraining.buildTrainMap(inPath);
	    tagTraining.printTrainMap(outPath);
	    tagTraining.test_singleMap();
            end = System.currentTimeMillis();
            System.out.println("total running times: " + (end-start));
	    System.out.println("******* Tag Training is Over! ********");
	}
	*/
	
	
	

		
        		
		
		
		
		
	
	
	
	
	
	
	
	
	
}
