package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 01/12/2010
 * This class is used to judge the positive and negative
 * support for one local topic 
 */

import java.util.*;
import java.io.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class Opinion {
    public Opinion() {}
    public Opinion(ArrayList all_utts_,
		   ArrayList tr_utts_,
		   ArrayList utts_,
		   Wordnet wn) {
	wn_ = wn;
	this.all_utts_ = all_utts_;
	this.tr_utts_ = tr_utts_;
	this.utts_ = utts_;
	tt_ = new TagTraining();
	tt_.setWN(wn_);
	dat_ = new DATagger();
	dat_.setWN(wn_);
	loadSupports();
    }

    

    /*******************************set information**************************/
    public void setXMLParser(XMLParse xmlps) {xmlps_ = xmlps;}
    public void setWordnet(Wordnet wn) { wn_ = wn; }
    public void loadSupports() {
	String pw = Settings.getValue(Settings.PW);
	String nw = Settings.getValue(Settings.NW);
	//System.out.println("pw: " + pw);
	File pwf = new File(pw);
	File nwf = new File(nw);
	String yes = Settings.getValue(Settings.YES);
	String no = Settings.getValue(Settings.NO);
	//System.out.println("pw: " + pw);
	File yesf = new File(yes);
	File nof = new File(no);
	try{
	    BufferedReader br = new BufferedReader(new FileReader(pwf));
	    String a_line = null;
	    while ((a_line = br.readLine()) != null){
		String[] words = a_line.split("[ ]+");
		pws_.add(words[0].split("#")[0]);
	    }
	    br.close();
	    br = new BufferedReader(new FileReader(yesf));
	    while ((a_line = br.readLine()) != null){
		String[] words = a_line.split("[ ]+");
		pws_.add(words[0].split("#")[0]);
	    }
	    br.close();
	    br = new BufferedReader(new FileReader(nwf));
	    while ((a_line = br.readLine()) != null){
		String[] words = a_line.split("[ ]+");
		nws_.add(words[0].split("#")[0]);
	    }
	    br.close();
	    br = new BufferedReader(new FileReader(nof));
	    while ((a_line = br.readLine()) != null){
		String[] words = a_line.split("[ ]+");
		nws_.add(words[0].split("#")[0]);
	    }
	    br.close();
	    /*
	    System.out.println("pw: " + pws_);
	    System.out.println("=================================================");
	    System.out.println("nw: " + nws_);
	    */
	} catch (IOException ioe){
	    ioe.printStackTrace();
	}
	String posi_confs = Settings.getValue(Settings.POSI_CONFS);
	posi_confs_ = (HashMap)Util.loadFile(posi_confs);
	String neg_confs = Settings.getValue(Settings.NEG_CONFS);
	neg_confs_ =  (HashMap)Util.loadFile(neg_confs);
    }


    public void buildTrainMap(String tag_type) {
	HashMap totalMap = tt_.getTotalMap(all_utts_, true, true, tag_type);
	//System.out.println("done for getting totalMap: " + totalMap.size());
	dat_.setTotalMap(totalMap);
	//tt_.buildPLTrainMap(all_utts_, true, true);
	tt_.buildTrainMap(tr_utts_, true, true, tag_type);
	tt_.printTrainMap(Settings.getValue(Settings.POLARITY_CUES));
	propertyMap = tt_.getPropertyMap();
	//System.out.println("propertyMap: " + propertyMap.size());
	dat_.setPropertyMap(propertyMap);
    }

    public void buildTrainMap() {
	buildTrainMap(DATagger.POLARITY);
	/*
	HashMap totalMap = tt_.getPLTotalMap(all_utts_, true, true);
	dat_.setTotalMap(totalMap);
	//tt_.buildPLTrainMap(all_utts_, true, true);
	tt_.buildPLTrainMap(tr_utts_, true, true);
	tt_.printTrainMap(Settings.POLARITY_CUES);
	propertyMap = tt_.getPropertyMap();
	dat_.setPropertyMap(propertyMap);
	*/
    }

    public void tagPolarity() {
	tagIt(DATagger.POLARITY);
    }

    public void tagIt(String tag_type) {
	int d_t_s = 0;
	int d_r_s = 0;
	dat_.setSpeakers(tt_.getSpeakers());
	for (int i = 0; i < utts_.size(); i++){
	    Utterance utterance = (Utterance)utts_.get(i);
	    boolean has_mesotopic = false;
	    if (utterance.getTopic() != null &&
		utterance.getTopic().trim().length() > 0) {
		has_mesotopic = true;
	    }
	    String content = utterance.getUtterance();
	    String polarity = utterance.getPolarity();
	    if (polarity == null ||
		polarity.trim().length() == 0) {
		//continue; //only check the utterances with topic defined
		polarity = NEUTRAL;
	    }
	    ArrayList std_tag_scr = (ArrayList)tags_eval_.get(polarity);
	    if (std_tag_scr == null) {
		std_tag_scr = new ArrayList();
		std_tag_scr.add(new Integer(0));
		std_tag_scr.add(new Integer(0));
		std_tag_scr.add(new Integer(0));
		tags_eval_.put(polarity, std_tag_scr);
	    }
	    std_tag_scr.set(2, (Integer)std_tag_scr.get(2) + 1);
	    ArrayList all_std_tag_scr = (ArrayList)all_tags_eval_.get(polarity);
	    if (all_std_tag_scr == null) {
		all_std_tag_scr = new ArrayList();
		all_std_tag_scr.add(new Integer(0));
		all_std_tag_scr.add(new Integer(0));
		all_std_tag_scr.add(new Integer(0));
		all_tags_eval_.put(polarity, all_std_tag_scr);
	    }
	    all_std_tag_scr.set(2, (Integer)all_std_tag_scr.get(2) + 1);
	    d_t_s++;
	    ObjValue[] cat_return = dat_.tagUtterance(content, polarity, true, true, tag_type, has_mesotopic, utterance);
	    if (cat_return == null || cat_return[0] == null) {
		//System.out.println("no cat_return: " + utterance);
		continue;
	    }
	    String cat_tag = cat_return[0].getTag();
	    //String cat_tag = tagPolarity(utts_, utterance);
	    if (cat_tag.equals(POSITIVE) ||
		cat_tag.equals(NEGATIVE)) {
		//System.out.println(utterance.getSpeaker() + " on " + utterance.getTopic() + " is " + cat_tag + " based on: " + content);
	    }
	    utterance.setSysPolarity(cat_tag);
	    if(cat_tag.equals(polarity)) {
		d_r_s++;
		std_tag_scr.set(0, (Integer)std_tag_scr.get(0) + 1);
		std_tag_scr.set(1, (Integer)std_tag_scr.get(1) + 1);
		all_std_tag_scr.set(0, (Integer)all_std_tag_scr.get(0) + 1);
		all_std_tag_scr.set(1, (Integer)all_std_tag_scr.get(1) + 1);
	    }
	    else {
		String ex_tag = cat_tag;
		if (ex_tag != null &&
		    !ex_tag.equals("unknown")) {
		    ArrayList ex_tag_scr = (ArrayList)tags_eval_.get(ex_tag);
		    if (ex_tag_scr == null) {
			ex_tag_scr = new ArrayList();
			ex_tag_scr.add(new Integer(0));
			ex_tag_scr.add(new Integer(0));
			ex_tag_scr.add(new Integer(0));
			tags_eval_.put(polarity, ex_tag_scr);
		    }
		    ex_tag_scr.set(1, (Integer)ex_tag_scr.get(1) + 1);
		    ArrayList all_ex_tag_scr = (ArrayList)all_tags_eval_.get(ex_tag);
		    if (all_ex_tag_scr == null) {
			all_ex_tag_scr = new ArrayList();
			all_ex_tag_scr.add(new Integer(0));
			all_ex_tag_scr.add(new Integer(0));
			all_ex_tag_scr.add(new Integer(0));
			all_tags_eval_.put(ex_tag, all_ex_tag_scr);
		    }
		    all_ex_tag_scr.set(1, (Integer)all_ex_tag_scr.get(1) + 1);
		}
		if (polarity != null 
		    && polarity.length() > 0) {
		    //System.out.println("system annotated: " + cat_tag + " ===== human annotated: " + polarity + " content: " + utterance.getContent());
		}
	    }
	}
	if (d_t_s != 0) {
	    //System.out.println("The accuracy of dialogue: " + (double)d_r_s/(double)d_t_s);
	}
	ArrayList tags = new ArrayList(tags_eval_.keySet());
	for (int i = 0; i < tags.size(); i++) {
	    String tag = (String)tags.get(i);
	    ArrayList tag_scr = (ArrayList)tags_eval_.get(tag);
	    if (((Integer)tag_scr.get(1)).doubleValue() == 0) {
		continue;
	    }
	    double precision = ((Integer)tag_scr.get(0)).doubleValue()/((Integer)tag_scr.get(1)).doubleValue();
	    double recall = ((Integer)tag_scr.get(0)).doubleValue()/((Integer)tag_scr.get(2)).doubleValue();
	    double f_measure = 2 * precision * recall / (precision + recall);
	    //System.out.println(tag + "'s score: " + precision + "(p),   " + recall + "(r),   " + f_measure + "(f)");
	    //System.out.println(tag + "'s percentage on total: " + ((Integer)tag_scr.get(2)).doubleValue()/d_t_s + " size: " + (Integer)tag_scr.get(2));
	}
	//accuracy = (double)right_sentence/(double)total_sentence;

    }

    public String tagPolarity(ArrayList utterances,
			      Utterance utterance,
			      boolean isEnglish_,
			      boolean isChinese_) {
	int st_pos = 0;
	int ex_pos = 0;
	int cr_pos = 0;
	int st_neg = 0;
	int ex_neg = 0;
	int cr_neg = 0;
	int st_neu = 0;
	int ex_neu = 0;
	int cr_neu = 0;
	String content = utterance.getUtterance();
	ArrayList lemmas = new ArrayList();
	getWordList(content, lemmas, isEnglish_, isChinese_);
	int has_pos = 0;
	int has_neg = 0;
	ArrayList pos_ws = new ArrayList();
	ArrayList neg_ws = new ArrayList();
	for (int j = 0; j < lemmas.size(); j++) {
	    String lemma = (String)lemmas.get(j);
	    if (pws_.contains(lemma.toUpperCase())) { has_pos++; pos_ws.add(lemma); }
	    if (nws_.contains(lemma.toUpperCase())) { has_neg++; neg_ws.add(lemma); }
	}

	//get polarity indicator's confidence
	boolean is_neg = false;
	boolean is_pos = false;
	boolean is_neutral = false;
	String ind = null;
	double conf = -1;
	for (int j = 0; j < neg_ws.size(); j++) {
	    String neg_w = (String)neg_ws.get(j);
	    Double cf = neg_confs_.get(neg_w);
	    if (cf != null &&
		cf > conf) {
		conf = cf;
		ind = neg_w;
		is_neg = true;
	    }
	}
	for (int j = 0; j < pos_ws.size(); j++) {
	    String pos_w = (String)pos_ws.get(j);
	    Double cf = posi_confs_.get(pos_w);
	    if (cf != null &&
		cf >= conf) {
		conf = cf;
		ind = pos_w;
		is_pos = true;
		is_neg = false;
	    }
	}
	if (is_pos || is_neg) {
	    if (conf < 0.5) {
		//indicator is not strong enough
		Utterance resp = getRespTo(utterances, utterance);
		/*
		System.out.println("**********************************************");
		System.out.println("current:\n" + utterance);
		System.out.println("is_pos: " + is_pos);
		System.out.println("is_neg: " + is_neg);
		*/
		if (resp != null) {
		    //System.out.println("repond to:\n" + resp.toString());
		}
		if (resp == null) {
		    //System.out.println("response to is null: " + utterance.getRespTo());
		    /*
		      is_pos = false;
		      is_neg = false;
		    */
		}
		else if (resp.getSysPolarity().equals(NEGATIVE)) {
		    //repose to previous opinion, support negative means negative, oppose negative means positive
		    /*
		    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		    System.out.println("current:\n" + utterance);
		    System.out.println("is_pos: " + is_pos);
		    System.out.println("is_neg: " + is_neg);
		    System.out.println("repond to:\n" + resp.toString());
		    */
		    if (is_pos) { is_neg = true; is_pos = false; }
		    else if (is_neg) { is_neg = false; is_pos = true; }
		} else if (resp.getSysPolarity().equals(NEUTRAL)) {
		    is_pos = false;
		    is_neg = false;
		}
	    } 
	}
	return getPolarity(is_pos, is_neg);
    }

    public String getPolarity(boolean is_pos,
			      boolean is_neg) {
	if (is_pos) {
	    return POSITIVE;
	}else if (is_neg) {
	    return NEGATIVE;
	} else return NEUTRAL;
    }

    public void calDisagr(ArrayList utterances,
			  boolean isEnglish_,
			  boolean isChinese_) {
	int st_pos = 0;
	int ex_pos = 0;
	int cr_pos = 0;
	int st_neg = 0;
	int ex_neg = 0;
	int cr_neg = 0;
	int st_neu = 0;
	int ex_neu = 0;
	int cr_neu = 0;
	for (int i = 0; i < utterances.size(); i++){
	    Utterance utterance = (Utterance)utterances.get(i);
	    String content = utterance.getUtterance();
	    ArrayList lemmas = new ArrayList();
	    getWordList(content, lemmas, isEnglish_, isChinese_);
	    int has_pos = 0;
	    int has_neg = 0;
	    ArrayList pos_ws = new ArrayList();
	    ArrayList neg_ws = new ArrayList();
	    for (int j = 0; j < lemmas.size(); j++) {
		String lemma = (String)lemmas.get(j);
		if (pws_.contains(lemma.toUpperCase())) { has_pos++; pos_ws.add(lemma); }
		if (nws_.contains(lemma.toUpperCase())) { has_neg++; neg_ws.add(lemma); }
	    }

	    //get polarity indicator's confidence
	    boolean is_neg = false;
	    boolean is_pos = false;
	    boolean is_neutral = false;
	    String ind = null;
	    double conf = -1;
	    for (int j = 0; j < neg_ws.size(); j++) {
		String neg_w = (String)neg_ws.get(j);
		Double cf = neg_confs_.get(neg_w);
		if (cf != null &&
		    cf > conf) {
		    conf = cf;
		    ind = neg_w;
		    is_neg = true;
		}
	    }
	    for (int j = 0; j < pos_ws.size(); j++) {
		String pos_w = (String)pos_ws.get(j);
		Double cf = posi_confs_.get(pos_w);
		if (cf != null &&
		    cf >= conf) {
		    conf = cf;
		    ind = pos_w;
		    is_pos = true;
		    is_neg = false;
		}
	    }
	    if (is_pos || is_neg) {
		if (conf < 0.5) {
		    //indicator is not strong enough
		    Utterance resp = getRespTo(utterances, utterance);
		    /*
		    System.out.println("**********************************************");
		    System.out.println("current:\n" + utterance);
		    System.out.println("is_pos: " + is_pos);
		    System.out.println("is_neg: " + is_neg);
		    */
		    if (resp != null) {
			//System.out.println("repond to:\n" + resp.toString());
		    }
		    if (resp == null) {
			//System.out.println("response to is null: " + utterance.getRespTo());
			/*
			is_pos = false;
			is_neg = false;
			*/
		    }
		    else if (resp.getSysPolarity().equals(NEGATIVE)) {
			//repose to previous opinion, support negative means negative, oppose negative means positive
			/*
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			System.out.println("current:\n" + utterance);
			System.out.println("is_pos: " + is_pos);
			System.out.println("is_neg: " + is_neg);
			System.out.println("repond to:\n" + resp.toString());
			*/
			if (is_pos) { is_neg = true; is_pos = false; }
			else if (is_neg) { is_neg = false; is_pos = true; }
		    } else if (resp.getSysPolarity().equals(NEUTRAL)) {
			is_pos = false;
			is_neg = false;
		    }
		} 
	    }
	    setPolarity(utterance, is_pos, is_neg);

	    //if (has_neg > 0) {
	    if (is_neg) {
		if (utterance.getPolarity().equals(NEGATIVE)) { 
		    st_neg++; ex_neg++; cr_neg++; 
		    incCorrect(negatives_, neg_ws);
		    incTotal(positives_, pos_ws);
		    //System.out.println("correct match(neg/neg): " + "pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		}
		else if (utterance.getPolarity().equals(POSITIVE)) { 
		    st_pos++; ex_neg++; 
		    ArrayList list = anas_.get("pos/neg");
		    if (list == null) {
			list = new ArrayList();
			anas_.put("pos/neg", list);
		    }
		    list.add("pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		    incTotal(negatives_, neg_ws);
		    incCorrect(positives_, pos_ws);
		    //System.out.println("wrong match(pos/neg): " + "pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content); 
		}
		else {
		    st_neu++; ex_neg++; 
		    ArrayList list = anas_.get("neu/neg");
		    if (list == null) {
			list = new ArrayList();
			anas_.put("neu/neg", list);
		    }
		    list.add("pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		    incTotal(negatives_, neg_ws);
		    incTotal(positives_, pos_ws);
		    //System.out.println("wrong match(neu/neg): " + "pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		}
		//} else if (has_pos > 0) {
	    } else if (is_pos) {
		if (utterance.getPolarity().equals(NEGATIVE)) { 
		    st_neg++; ex_pos++; 
		    ArrayList list = anas_.get("neg/pos");
		    if (list == null) {
			list = new ArrayList();
			anas_.put("neg/pos", list);
		    }
		    incCorrect(negatives_, neg_ws);
		    incTotal(positives_, pos_ws);
		    list.add("pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		    //System.out.println("wrong match(neg/pos): " + "pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		}
		else if (utterance.getPolarity().equals(POSITIVE)) { 
		    st_pos++; ex_pos++; cr_pos++; 
		    incTotal(negatives_, neg_ws);
		    incCorrect(positives_, pos_ws);
		    //System.out.println("correct match(pos/pos): " + "pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		}
		else {
		    st_neu++; ex_pos++; 
		    ArrayList list = anas_.get("neu/pos");
		    if (list == null) {
			list = new ArrayList();
			anas_.put("neu/pos", list);
		    }
		    incTotal(negatives_, neg_ws);
		    incTotal(positives_, pos_ws);
		    list.add("pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		    //System.out.println("wrong match(neu/pos): " + "pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		}
	    } else {
		if (utterance.getPolarity().equals(NEGATIVE)) { 
		    st_neg++; ex_neu++; 
		    ArrayList list = anas_.get("neg/neu");
		    if (list == null) {
			list = new ArrayList();
			anas_.put("neg/neu", list);
		    }
		    list.add("pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		    //System.out.println("wrong match(neg/neu): " + "pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		}
		else if (utterance.getPolarity().equals(POSITIVE)) { 
		    st_pos++; ex_neu++; 
		    ArrayList list = anas_.get("pos/neu");
		    if (list == null) {
			list = new ArrayList();
			anas_.put("pos/neu", list);
		    }
		    list.add("pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		    //System.out.println("wrong match(pos/neu): " + "pos words: " + pos_ws + " =========== neg words: " + neg_ws + " ========content: " + content);
		}
		else {st_neu++; ex_neu++; cr_neu++;}
	    }
	}	
	/*
	System.out.println("precision: " + (double)(cr_pos + cr_neg + cr_neu)/(ex_pos + ex_neg + ex_neu));
	System.out.println("positive precision: " + (double)(cr_pos)/(ex_pos));
	System.out.println("negative precision: " + (double)(cr_neg)/(ex_neg));
	System.out.println("neutral precision: " + (double)(cr_neu)/(ex_neu));
	System.out.println("positive recall: " + (double)(cr_pos)/(st_pos));
	System.out.println("negative recall: " + (double)(cr_neg)/(st_neg));
	System.out.println("neutral recall: " + (double)(cr_neu)/(st_neu));
	System.out.println("all neutral: " + st_neu);
	System.out.println("percentage of neutral vs. total: " + (double)st_neu/utterances.size());
	*/
	ArrayList keys = new ArrayList();
	keys.addAll(anas_.keySet());
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    //System.out.print("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" + key);
	    ArrayList list = anas_.get(key);
	    //System.out.println("         size of miss match: " + list.size());
	    for (int j = 0; j < list.size(); j++) {
		//System.out.println((String)list.get(j));
	    }
	}
	keys = new ArrayList();
	keys.addAll(positives_.keySet());
	//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n++++++++++++positive words:\n");
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    ArrayList counts = positives_.get(key);
	    //System.out.println(key + ": \t" + (Integer)counts.get(0) + " \t" + (Integer)counts.get(1) + "\t" + ((Integer)counts.get(0)).doubleValue()/((Integer)counts.get(1)).doubleValue());
	    posi_confs_.put(key, ((Integer)counts.get(0)).doubleValue()/((Integer)counts.get(1)).doubleValue());
	}
	keys = new ArrayList();
	keys.addAll(negatives_.keySet());
	//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n++++++++++++negative words:\n");
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    ArrayList counts = negatives_.get(key);
	    //System.out.println(key + ": \t" + (Integer)counts.get(0) + " \t" + (Integer)counts.get(1) + "\t" + ((Integer)counts.get(0)).doubleValue()/((Integer)counts.get(1)).doubleValue());
	    neg_confs_.put(key, ((Integer)counts.get(0)).doubleValue()/((Integer)counts.get(1)).doubleValue());
	}
	//Util.writeToFile(Settings.getValue(Settings.POSI_CONFS), posi_confs_);
	//Util.writeToFile(Settings.getValue(Settings.NEG_CONFS), neg_confs_);
    }

    public void setPolarity(Utterance utt,
			    boolean is_pos,
			    boolean is_neg) {
	if (is_pos) {
	    utt.setSysPolarity(POSITIVE);
	}else if (is_neg) {
	    utt.setSysPolarity(NEGATIVE);
	} else utt.setSysPolarity(NEUTRAL);
    }

    public void incCorrect(HashMap<String, ArrayList> op_ws,
			   ArrayList<String> words) {
	for (int j = 0; j < words.size(); j++) {
	    String neg_w = (String)words.get(j);
	    ArrayList counts = op_ws.get(neg_w);
	    if (counts == null) {
		counts = new ArrayList();
		counts.add(new Integer(0));
		counts.add(new Integer(0));
		op_ws.put(neg_w, counts);
	    }
	    Integer cor = (Integer)counts.get(0);
	    cor++;
	    counts.set(0, cor);
	    Integer all = (Integer)counts.get(1);
	    all++;
	    counts.set(1, all);
	}
    }

    public void incTotal(HashMap<String, ArrayList> op_ws,
			   ArrayList<String> words) {
	for (int j = 0; j < words.size(); j++) {
	    String neg_w = (String)words.get(j);
	    ArrayList counts = op_ws.get(neg_w);
	    if (counts == null) {
		counts = new ArrayList();
		counts.add(new Integer(0));
		counts.add(new Integer(0));
		op_ws.put(neg_w, counts);
	    }
	    Integer all = (Integer)counts.get(1);
	    all++;
	    counts.set(1, all);
	}
    }

    /*******************************get information**************************/
    public ArrayList getTokens(String [] tags,
			       boolean isEnglish_,
			       boolean isChinese_){
	ArrayList retval = new ArrayList();
	for (int i = 0; i < tags.length; i++){
	    String tag = ParseTools.getTag(tags[i]);
	    String word = ParseTools.getWord(tags[i]).toLowerCase().trim();
	    word = ParseTools.removePunctuation(word, isEnglish_, isChinese_);
	    word += "/" + tag;
	    retval.add(word);
	}
	return retval;
    }


    public ArrayList getWordList(String utterance,
				 ArrayList lemmas,
				 boolean isEnglish_,
				 boolean isChinese_) {
	String tagged = StanfordPOSTagger.tagString(utterance).trim();
	String [] tagsplit = tagged.split("\\s+");
	ArrayList <String> tokens = getTokens(tagsplit, isEnglish_, isChinese_);
	for (int j = 0; j < tokens.size(); j++){
	    String token = tokens.get(j);
	    String word = ParseTools.getWord(token).trim();
	    word = word.replaceAll("\\s+", " ");
	    String lemma = null;
	    String tag = ParseTools.getTag(token);
	    if (tag.startsWith("N")) {
		lemma = wn_.getLemma(word, "noun");
	    } else if (tag.startsWith("V")) {
		lemma = wn_.getLemma(word, "verb");
	    }
	    if (lemma == null) { lemmas.add(word); }
	    else { lemmas.add(lemma); }
	}
	return lemmas;
    }

    public Utterance getRespTo(ArrayList utts,
					 Utterance utt) {
	String rpt = utt.getRespTo();
	if (rpt == null ||
	    rpt.trim().length() == 0) return null;
	if (rpt.split(":").length < 2) { return null; }
	rpt = rpt.split(":")[1];
	for (int i = 0; i < utts.size(); i++) {
	    Utterance dda = (Utterance)utts.get(i);
	    if (dda.getTurn().equals(rpt)) return dda;
	}
	return null;
    }





    /*******************************attributes*******************************/
    protected HashSet pws_ = new HashSet();
    protected HashSet nws_ = new HashSet();
    protected XMLParse xmlps_ = null;
    protected ArrayList utts_ = null;
    protected ArrayList tr_utts_ = null;
    protected ArrayList all_utts_ = null;
    protected Wordnet wn_ = null;
    public final static String POSITIVE = "positive";
    public final static String NEGATIVE = "negative";
    public final static String NEUTRAL = "neutral";
    protected HashMap<String, ArrayList> anas_ = new HashMap<String, ArrayList>();
    protected HashMap<String, ArrayList> positives_ = new HashMap<String, ArrayList>();
    protected HashMap<String, ArrayList> negatives_ = new HashMap<String, ArrayList>();
    protected HashMap<String, Double> posi_confs_ = new HashMap<String, Double>();
    protected HashMap<String, Double> neg_confs_ = new HashMap<String, Double>();
    protected HashMap<String, ArrayList> missed_ = new HashMap<String, ArrayList>();
    protected TagTraining tt_ = null;
    protected DATagger dat_ = null;
    protected HashMap propertyMap = null;
    protected HashMap tags_eval_ = new HashMap();
    protected HashMap all_tags_eval_ = new HashMap();
}
