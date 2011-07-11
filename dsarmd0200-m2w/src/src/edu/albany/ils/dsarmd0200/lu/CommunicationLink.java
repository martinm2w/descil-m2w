package edu.albany.ils.dsarmd0200.lu;

/**
 * @file: CommunicationLink.java
 * @author: Ting Liu
 * This file is used to create the Communication link of the current utterance
 */

import edu.albany.ils.dsarmd0200.util.*;
//import edu.albany.ils.dsarmd0200.ml.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class CommunicationLink extends Opinion{
    
    public CommunicationLink(ArrayList all_utts_,
			     ArrayList tr_utts_,
			     Wordnet wn,
			     ArrayList<Utterance> utts_,
			     HashMap parts) {
	super();
	wn_ = wn;
	parts_ = parts;
	//sls_.setParts(parts_);
	this.all_utts_ = all_utts_;
	this.tr_utts_ = tr_utts_;
	this.utts_ = utts_;
	tt_ = new TagTraining();
	dat_ = new DATagger();
	loadFeatures();
    }

    /*******************************get information**************************/
    public void calCLFts() {
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = utts_.get(i);
	    String cont = utt_.getContent();
	    String[] words = cont.split("[\\s]+");
	    if (words.length != 0) {
		String first_word = words[0].toLowerCase();
		if (isCont(utt_, i, first_word)) {
		    //System.out.println("find a continueation of");
		    //utt_.setCommActType(CONTINUATION_OF);
		    utt_.setCommActType(CONTINUATION_OF);
		}
	    }
	}
	//evaluate();
    }
    
    public void training(ArrayList tr_utts) {
	CommActGround.training(tr_utts);
    }
    
    public void tagIt(String tag_type) {
	int d_t_s = 0;
	int d_r_s = 0;
	for (int i = 0; i < utts_.size(); i++){
	    Utterance utterance = (Utterance)utts_.get(i);
	    String content = utterance.getUtterance();
	    String comm_act = utterance.getCommActType();
	    if (comm_act.trim().length() == 0) {
		comm_act = ADDRESSED_TO;
	    }
	    ArrayList std_tag_scr = (ArrayList)tags_eval_.get(comm_act);
	    if (std_tag_scr == null) {
		std_tag_scr = new ArrayList();
		std_tag_scr.add(new Integer(0));
		std_tag_scr.add(new Integer(0));
		std_tag_scr.add(new Integer(0));
		tags_eval_.put(comm_act, std_tag_scr);
	    }
	    std_tag_scr.set(2, (Integer)std_tag_scr.get(2) + 1);
	    ArrayList all_std_tag_scr = (ArrayList)all_tags_eval_.get(comm_act);
	    if (all_std_tag_scr == null) {
		all_std_tag_scr = new ArrayList();
		all_std_tag_scr.add(new Integer(0));
		all_std_tag_scr.add(new Integer(0));
		all_std_tag_scr.add(new Integer(0));
		all_tags_eval_.put(comm_act, all_std_tag_scr);
	    }
	    all_std_tag_scr.set(2, (Integer)all_std_tag_scr.get(2) + 1);
	    d_t_s++;
	    String cat_tag = null;
	    String[] words = content.split("[\\s]+");
	    if (words.length != 0) {
		String first_word = words[0].toLowerCase();

		if (isCont(utterance, i, first_word)) {
		    cat_tag = CONTINUATION_OF;
		    utterance.setCommActType(CONTINUATION_OF);
		} else {
		    
		    //cat_tag = CommActGround.tagIt(utterance);/*
		    ObjValue[] cat_return = dat_.tagUtterance(content, comm_act, true, true, tag_type, false, utterance);
		    if (cat_return == null || cat_return[0] == null) {
			System.out.println("no cat_return: " + utterance);
			continue;
		    }
		    cat_tag = cat_return[0].getTag(); 
		    if (cat_tag.equals(CONTINUATION_OF)) { cat_tag = RESPONSE_TO; }
		    utterance.setCommActType(cat_tag);
		}
	    }
	    if(cat_tag.equals(comm_act)) {
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
			tags_eval_.put(comm_act, ex_tag_scr);
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
	    }
	}
	if (d_t_s != 0) {
	    System.out.println("The accuracy of dialogue: " + (double)d_r_s/(double)d_t_s);
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
	    System.out.println(tag + "'s score: " + precision + "(p),   " + recall + "(r),   " + f_measure + "(f)");
	}
	//accuracy = (double)right_sentence/(double)total_sentence;

    }

    public boolean isCont(Utterance utt_,
			  int i,
			  String fw) {
	if (i > 0) {
	    Utterance pre_utt_ = utts_.get(i -1);
	    if (pre_utt_.getSpeaker().equals(utt_.getSpeaker())) {
		HashMap cont_fws_ = afirst_words_;
		HashMap cont_cfws_ = (HashMap)acfirst_words_.get(CONTINUATION_OF);
		if (isCont(fw, cont_fws_, cont_cfws_)) {
		    utt_.setRespTo(pre_utt_.getSpeaker() +":" + pre_utt_.getTurn());
		    return true;
		}
	    }
	}
	if (i > 1) {
	    Utterance pre2_utt_ = utts_.get(i - 2);
	    Utterance pre_utt_ = utts_.get(i - 1);
		HashMap cont_fws_ = aafirst_words_;
		HashMap cont_cfws_ = (HashMap)aacfirst_words_.get(CONTINUATION_OF);
	    if (pre2_utt_.getSpeaker().equals(utt_.getSpeaker())/* &&
								   pre_utt_.getSpeaker().equals(utt_.getSpeaker())*/) {
		if (isCont(fw, cont_fws_, cont_cfws_)) {
		    utt_.setRespTo(pre2_utt_.getSpeaker() +":" + pre2_utt_.getTurn());
		    return true;
		}
	    }
	}
	return false;
    }


    public boolean isCont(String fw,
			  HashMap fws_,
			  HashMap cfws_) {
	Integer count = (Integer)cfws_.get(fw);
	if (count != null) {
	    double conf = count.doubleValue()/(Integer)fws_.get(fw);
	    if (conf >= 0.8) return true;
	    if (conf >= 0.5 && count > 1) return true;
	}
	return false;
    }

    public void evaluate() {
	HashMap evals = new HashMap(); //hashmap of arraylist of integer
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = utts_.get(i);
	    String ann_cat = utt_.getCommActType();
	    if (ann_cat != null) {
		//annotated
		ArrayList cat_evals = (ArrayList)evals.get(ann_cat);
		if (cat_evals == null) {
		    cat_evals = new ArrayList();
		    Integer ann = new Integer(0);
		    Integer ex = new Integer(0);
		    Integer cr = new Integer(0);
		    Integer lt_ex = new Integer(0);
		    Integer lt_cr = new Integer(0);
		    cat_evals.add(ann);
		    cat_evals.add(ex);
		    cat_evals.add(cr);
		    cat_evals.add(lt_ex);
		    cat_evals.add(lt_cr);
		    evals.put(ann_cat, cat_evals);
		}
		Integer ann_count = (Integer)cat_evals.get(0);
		cat_evals.set(0, ann_count + 1);
		String ex_cat = utt_.getSysCommActType();
		if (ex_cat != null) {
		    cat_evals = (ArrayList)evals.get(ex_cat);
		    if (cat_evals == null) {
			cat_evals = new ArrayList();
			Integer ann = new Integer(0);
			Integer ex = new Integer(0);
			Integer cr = new Integer(0);
			Integer lt_ex = new Integer(0);
			Integer lt_cr = new Integer(0);
			cat_evals.add(ann);
			cat_evals.add(ex);
			cat_evals.add(cr);
			cat_evals.add(lt_ex);
			cat_evals.add(lt_cr);
			evals.put(ex_cat, cat_evals);
		    }
		    Integer ex_count = (Integer)cat_evals.get(1);
		    cat_evals.set(1, ex_count + 1);
		    if (ann_cat.equals(ex_cat)) {
			Integer cr_count = (Integer)cat_evals.get(2);
			cat_evals.set(2, cr_count + 1);
		    } else {
			System.out.println("extract wrong: " + ann_cat + "(annotated) --- " + ex_cat + "(autmated): " + utt_.getContent()); 
		    }


		    String lt_ann_dir = utt_.getRespTo();
		    String lt_ex_dir = utt_.getSysRespTo();
		    Integer lt_ex_count = (Integer)cat_evals.get(3);
		    cat_evals.set(3, lt_ex_count + 1);
		    if (lt_ann_dir.equals(lt_ex_dir)) {
			Integer lt_cr_count = (Integer)cat_evals.get(4);
			cat_evals.set(4, lt_cr_count + 1);
		    }
		}
	    }
	}
	System.out.println("=======================\nperformance of communication act\nActs\tAnnotated\tExtracted\tCorrect\tlt_extracted\tlt_correct\tPrecision\tRecall\tF-measure\tlt_precision");
	Iterator keys = evals.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    System.out.print(key + "\t");
	    ArrayList counts = (ArrayList)evals.get(key);
	    Integer ann = (Integer)counts.get(0);
	    Integer ex = (Integer)counts.get(1);
	    Integer cr = (Integer)counts.get(2);
	    Integer lt_ex = (Integer)counts.get(3);
	    Integer lt_cr = (Integer)counts.get(4);
	    double pre = cr.doubleValue()/ex;
	    double rec = cr.doubleValue()/ann;
	    double fm = (2*pre*rec)/(pre + rec);
	    double lt_pre = lt_cr.doubleValue()/lt_ex;
	    System.out.println(ann + "\t" + ex + "\t" + cr + "\t" + lt_ex + "\t" + lt_cr + "\t" + pre + "\t" + rec + "\t" + fm + "\t" + lt_pre);
	}
    }


    public void loadFeatures() {
	afirst_words_ = (HashMap)Util.loadFile(Settings.getValue(Settings.AFIRST_WORDS));
	acfirst_words_ = (HashMap)Util.loadFile(Settings.getValue(Settings.ACFIRST_WORDS));
	aafirst_words_ = (HashMap)Util.loadFile(Settings.getValue(Settings.AAFIRST_WORDS));
	aacfirst_words_ = (HashMap)Util.loadFile(Settings.getValue(Settings.AACFIRST_WORDS));
	st_wds_ = Util.loadStWds(Settings.getValue(Settings.STOP_WORDS));
	//System.out.println("done load features");
    }


    /*******************************set information**************************/
    public void setUtts(ArrayList utts) {
	utts_ = utts;
    }

    public void setNounList(NounList nls) {
	nls_ = nls;
    }

    public void buildTrainMap() {
	buildTrainMap(DATagger.COMMACT);
    }

    public void writeFeatures() {
	Util.writeToFile(Settings.getValue(Settings.AFIRST_WORDS), afirst_words_);
	Util.writeToFile(Settings.getValue(Settings.ACFIRST_WORDS), acfirst_words_);
	Util.writeToFile(Settings.getValue(Settings.AAFIRST_WORDS), aafirst_words_);
	Util.writeToFile(Settings.getValue(Settings.AACFIRST_WORDS), aacfirst_words_);
    }
    /*
    public void collectSampleLinks() {
	sls_.setUtts(utts_);
	sls_.setNounList(nls_);
	sls_.collectFeatures();
    }

    public void collectTestLinks() {
	
	sls_.setUtts(utts_);
	sls_.setNounList(nls_);
	sls_.applyFeatures();
    }
    */
	public void callContOf()
    {
    	for (int i = 0; i < utts_.size(); i++) 
    	{
    		
    		Utterance utt_ = utts_.get(i);
    		int count = 0;
    		for (int j = i - 1; j >= 0 && count < 10; j--, count++) 
    		{			
    			Utterance rsp_utt = utts_.get(j);
    			String rsp_utt_content = rsp_utt.getContent();
//		   	 	double sim = Util.compareUtts(utt_content, rsp_utt_content);   
    			if (utt_.getCommActType().equals(CONTINUATION_OF)) 
    			{
//    				System.out.println("Doing turn " + utt_.getTurn());
    				if (rsp_utt.getSpeaker().equals(utt_.getSpeaker())) 
    				{
    					utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
//    					System.out.println("----- Turn " + utt_.getTurn() + ": " + "is link to " + utt_.getSysRespTo());
//    					map.put(Integer.parseInt(utt_.getTurn()), utt_.getSysRespTo());
//		    			link_found = true;
    					break;
    				}
    			}
    		} 	
    	}
    }
	
	public ArrayList<Utterance> getUtts()
    {
    	return utts_;
    }
	

    public void collectCLFts() {
	HashMap dists = new HashMap();
	HashMap totals = new HashMap();
	int num_adj_usr = 0;
	int num_nex_same_usr = 0;
	int num_nex2_same_usr = 0;
	int num_nex3_same_usr = 0;
	int num_nex4_same_usr = 0;
	int mid_same = 0;
	ArrayList unique_rp_utts = new ArrayList();
	ArrayList answered_questions = new ArrayList();
	ArrayList question_utts = new ArrayList();
	HashMap utts_responded = new HashMap(); //whether a speaker replied this utterance
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = utts_.get(i);
	    String cont = utt_.getContent();
	    String[] words = cont.split("[\\s]+");
	    String comm_act = utt_.getCommActType();
	    HashMap cas_bw = (HashMap)cas_bw_.get(comm_act);
	    if (cas_bw == null) {
		cas_bw = new HashMap();
		cas_bw_.put(comm_act, cas_bw);
	    }
	    HashMap trls_bw = (HashMap)trls_bw_.get(comm_act);
	    if (trls_bw == null) {
		trls_bw = new HashMap();
		trls_bw_.put(comm_act, trls_bw);
	    }
	    if (words.length != 0) {
		String first_word = words[0].toLowerCase();
		if (utt_.getCommActType() != null) {
		    HashMap first_words = (HashMap)cfirst_words_.get(utt_.getCommActType());
		    if (first_words == null) {
			first_words = new HashMap();
			cfirst_words_.put(utt_.getCommActType(), first_words);
		    }
		    Integer count = (Integer)first_words.get(first_word);
		    if (count == null) {
			count = new Integer(0);
		    }
		    first_words.put(first_word, count + 1);
		    
		    count = (Integer)first_words_.get(first_word);
		    if (count == null) {
			count = new Integer(0);
		    }
		    first_words_.put(first_word, count + 1);
		}
	    }
	    if (i > 0) {
		Utterance pre_utt_ = utts_.get(i -1);
		if (pre_utt_.getSpeaker().equals(utt_.getSpeaker())) {
		    num_adj_usr++;
		    /*
		    System.out.println("================================================\n" + pre_utt_
				       + "\n+++++++++++++++++++++++++++++++++++\n" + utt_);
		    */
		    if (words.length != 0) {
			String first_word = words[0].toLowerCase();
			if (utt_.getCommActType() != null) {
			    HashMap first_words = (HashMap)acfirst_words_.get(utt_.getCommActType());
			    if (first_words == null) {
				first_words = new HashMap();
				acfirst_words_.put(utt_.getCommActType(), first_words);
			    }
			    Integer count = (Integer)first_words.get(first_word);
			    if (count == null) {
				count = new Integer(0);
			    }
			    first_words.put(first_word, count + 1);
			    
			    count = (Integer)afirst_words_.get(first_word);
			    if (count == null) {
				count = new Integer(0);
			    }
			    afirst_words_.put(first_word, count + 1);
			}
		    }
		}
	    }
	    if (i > 1) {
		Utterance pre2_utt_ = utts_.get(i - 2);
		Utterance pre_utt_ = utts_.get(i - 1);
		if (pre2_utt_.getSpeaker().equals(utt_.getSpeaker()) &&
		    pre_utt_.getSpeaker().equals(utt_.getSpeaker())) {
		    num_nex_same_usr++;
		}
		if (pre2_utt_.getSpeaker().equals(utt_.getSpeaker())) {
		    if (words.length != 0) {
			String first_word = words[0].toLowerCase();
			if (utt_.getCommActType() != null) {
			    HashMap first_words = (HashMap)aacfirst_words_.get(utt_.getCommActType());
			    if (first_words == null) {
				first_words = new HashMap();
				aacfirst_words_.put(utt_.getCommActType(), first_words);
			    }
			    Integer count = (Integer)first_words.get(first_word);
			    if (count == null) {
				count = new Integer(0);
			    }
			    first_words.put(first_word, count + 1);
			    
			    count = (Integer)aafirst_words_.get(first_word);
			    if (count == null) {
				count = new Integer(0);
			    }
			    aafirst_words_.put(first_word, count + 1);
			}
		    }
		}
	    }
	    if (i > 2) {
		Utterance pre3_utt_ = utts_.get(i - 3);
		Utterance pre2_utt_ = utts_.get(i - 2);
		Utterance pre_utt_ = utts_.get(i - 1);
		if (pre3_utt_.getSpeaker().equals(utt_.getSpeaker()) &&
		    pre2_utt_.getSpeaker().equals(utt_.getSpeaker()) &&
		    pre_utt_.getSpeaker().equals(utt_.getSpeaker())) {
		    num_nex2_same_usr++;
		}
	    }
	    if (i > 3) {
		Utterance pre4_utt_ = utts_.get(i - 4);
		Utterance pre3_utt_ = utts_.get(i - 3);
		Utterance pre2_utt_ = utts_.get(i - 2);
		Utterance pre_utt_ = utts_.get(i - 1);
	    }
	    if (utt_.getRespTo() != null) {
		String[] lkto = utt_.getRespTo().split(":");
		if (lkto.length < 2) continue;
		boolean get_it = false;
		ArrayList resp_sims = new ArrayList();
		ArrayList other_sims = new ArrayList();
		String utt_content = utt_.getContent();
		int count = 0;
		int utt_tl = ParseTools.wordCount(utt_content);
		Integer total = (Integer)totals.get(utt_.getCommActType());
		if (total == null) {
		    total = new Integer(0);
		}
		totals.put(utt_.getCommActType(), total + 1);
		if (utt_.getCommActType().equals(RESPONSE_TO)) {
		    count = 0;
		    if (utt_tl < 4) { System.out.println("utt length is shorter than 5"); }
		    System.out.println("utt_content(" + utt_.getSpeaker() + "): " + utt_content);
		    for (int j = i - 1; j >= 0 && count < 10; j--, count++) {
			Utterance rsp_utt = utts_.get(j);
			if (rsp_utt.getTurn().equals(lkto[1])) {
			    System.out.println("resp to utt_content(" + rsp_utt.getSpeaker() + "): " + rsp_utt.getContent());
			} else {
			    System.out.println("other utt_content(" + rsp_utt.getSpeaker() + "): " + rsp_utt.getContent());
			}			
		    }
		}
		count = 0;
		boolean link_found = false;
		if (utt_.getCommActType().equals(RESPONSE_TO)) {
		    for (int j = i - 1; j >= 0 && count < 10; j--, count++) {
			Utterance rsp_utt = utts_.get(j);
			String rsp_utt_content = rsp_utt.getContent();
			int tl = ParseTools.wordCount(rsp_utt_content);
			if (utt_tl == 0) {
			    if (count == 0) {
				utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
				link_found = true;
				System.out.println("System thought link (utt_tl == 0): " + rsp_utt_content);
				break;
			    }
			} else if (!link_found && utt_tl < 4 && count < 3) {
			    if (rsp_utt.getSpeaker().equals(utt_.getSpeaker())) {
				continue;
			    } else if (utt_content.toLowerCase().indexOf(rsp_utt.getSpeaker().toLowerCase()) != -1) {
				utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
				link_found = true;
				System.out.println("System thought link (utt_tl < 4): " + rsp_utt_content);
				break;
			    } else if (tl < 4) {
				continue;
			    } else {
				utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
				link_found = true;
				System.out.println("System thought link (utt_tl < 4): " + rsp_utt_content);
				break;
			    }
			}
		    }
		}
		count = 0;
		boolean processed_id_spk = false; 
		boolean question_added = false;
		Utterance question_utt = null;
		//link_found = false;
		if (!link_found &&
		    utt_.getCommActType().equals(RESPONSE_TO)) {
		    ArrayList<Double> sims = new ArrayList<Double>();
		    for (int j = i - 1; j >= 0 && count < 10; j--, count++) {
			Utterance rsp_utt = utts_.get(j);
			String rsp_utt_content = rsp_utt.getContent();
			double sim = Util.compareUtts(utt_content, rsp_utt_content);
			sims.add(sim);
		    }
		    int highest = -1;
		    double highest_value = -99;
		    for (int j = 0; j < sims.size(); j++) {
			if (sims.get(j) > highest_value) {
			    highest = j;
			    highest_value = sims.get(j);
			}
		    }
		    if (highest_value > 0) {
			Utterance rsp_utt = (Utterance)utts_.get(i - highest - 1);
			utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
			link_found = true;
			System.out.println("System thought link (highest sim): " + rsp_utt.getContent());
		    }
		}
		for (int j = i - 1; j >= 0 && count < 10; j--, count++) {
		    Utterance rsp_utt = utts_.get(j);
		    String rsp_utt_content = rsp_utt.getContent();
		    double sim = Util.compareUtts(utt_content, rsp_utt_content);
		    boolean curr_is_question = false;
		    if (rsp_utt.getTag().equals("IRCR") ||
			rsp_utt_content.indexOf("?") != -1) {
			curr_is_question = true;
		    }
		    if (utt_.getCommActType().equals(RESPONSE_TO) && !question_added) {
			if (rsp_utt.getTag().equals("IRCR") ||
			    rsp_utt_content.indexOf("?") != -1) {
			    if (!responded_questions.contains(rsp_utt_content) &&
				!rsp_utt.getSpeaker().equals(utt_.getSpeaker())) {
				question_added = true;
				total_question++;
				responded_questions.add(rsp_utt_content);
			    }
			}
		    }
		    if (!link_found &&
			utt_.getCommActType().equals(RESPONSE_TO)) {
			if (rsp_utt.getSpeaker().equals(utt_.getSpeaker()) &&
			    !processed_id_spk) {
			    if (get_it) get_to_before_id_spk++;
			    processed_id_spk = true;
			}
			int tl = ParseTools.wordCount(rsp_utt_content);
			Integer tl_count = total_wl_dist.get(tl);
			if (tl_count == null) {
			    tl_count = new Integer(0);
			}
			total_wl_dist.put(tl, ++tl_count);
			if (utt_tl == 0) {
			    if (count == 0) {
				utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
				link_found = true;
				System.out.println("System thought link (utt_tl == 0): " + rsp_utt_content);
				break;
			    }
			} else if (!link_found && utt_tl < 4 && count < 3) {
			    if (rsp_utt.getSpeaker().equals(utt_.getSpeaker())) {
				continue;
			    } else if (utt_content.toLowerCase().indexOf(rsp_utt.getSpeaker().toLowerCase()) != -1) {
				utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
				link_found = true;
				System.out.println("System thought link: " + rsp_utt_content);
				break;
			    } else if (tl < 4) {
				continue;
			    } else {
				utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
				link_found = true;
				System.out.println("System thought link: " + rsp_utt_content);
				break;
			    }
			} else if (count > 0 && rsp_utt.getSpeaker().equals(utt_.getSpeaker())) break; //over limit
			else if (!link_found && 
				 ((utt_content.toLowerCase().indexOf(rsp_utt.getSpeaker().toLowerCase()) != -1 ||
				   rsp_utt_content.toLowerCase().indexOf(utt_.getSpeaker().toLowerCase()) != -1) &&
				  !utt_.getSpeaker().equals(rsp_utt.getSpeaker()) &&
				  !link_found/* &&
						!link_founds_.contains(rsp_utt)*/)) {
			    ArrayList utts_rsp = (ArrayList)utts_responded.get(rsp_utt_content);
			    if (utts_rsp == null) {
				utts_rsp = new ArrayList();
				utts_responded.put(rsp_utt_content, utts_rsp);
			    }
			    if (true) {//if rsp_utt is a question, it may have a low possiblitity as response to
				if ((utt_content.toLowerCase().indexOf(rsp_utt.getSpeaker().toLowerCase()) != -1 &&
				     !utts_rsp.contains(rsp_utt.getSpeaker())) ||
				    (rsp_utt_content.toLowerCase().indexOf(utt_.getSpeaker().toLowerCase()) != -1 &&
				     !utts_rsp.contains(utt_.getSpeaker()))) {
				    if (rsp_utt_content.toLowerCase().indexOf(utt_.getSpeaker().toLowerCase()) != -1)
					utts_rsp.add(utt_.getSpeaker());
				    if (utt_content.toLowerCase().indexOf(rsp_utt.getSpeaker().toLowerCase()) != -1) 
					utts_rsp.add(rsp_utt.getSpeaker());
				    
				    if (utt_tl < 3 && count > 2) {}
				    else {
					utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
					link_found = true;
					link_founds_.add(rsp_utt);
					if (curr_is_question) {
					    if (!answered_questions.contains(rsp_utt_content)) {
						answered_questions.add(rsp_utt_content);
					    }
					}
					System.out.println("System thought link: " + rsp_utt_content);
					break;
				    }
				}
			    }
			}
		    }
		    if (utt_.getCommActType().equals(CONTINUATION_OF)) {
			if (rsp_utt.getSpeaker().equals(utt_.getSpeaker()) &&
			    !link_found) {
			    utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
			    link_found = true;
			}
		    }
		    if (rsp_utt.getTurn().equals(lkto[1])) {
			String tl_vs_resp_dist_key = utt_tl + "_" + count;
			Integer tlrs_count = tl_vs_resp_dist_.get(tl_vs_resp_dist_key);
			Integer tlrs_size = tl_vs_resp_dist_count_.get(tl_vs_resp_dist_key);
			if (tlrs_count == null) { tlrs_count = new Integer(0); tlrs_size = new Integer(0); }
			tlrs_count++;
			tlrs_size += count + 1;
			tl_vs_resp_dist_.put(tl_vs_resp_dist_key, tlrs_count);
			tl_vs_resp_dist_count_.put(tl_vs_resp_dist_key, tlrs_size);
			if (utt_.getCommActType().equals(RESPONSE_TO)) {
			    if (utt_content.toLowerCase().indexOf(rsp_utt.getSpeaker().toLowerCase()) != -1) {
				resp_spk_count++;
			    }
			    if (!unique_rp_utts.contains(rsp_utt)) unique_rp_utts.add(rsp_utt);
			    if (rsp_utt.getTag().equals("IRCR") ||
				rsp_utt_content.indexOf("?") != -1) {
				if (!question_utts.contains(rsp_utt_content)) {
				    is_question++;
				    question_utts.add(rsp_utt_content);
				}
			    }
			    int tl = ParseTools.wordCount(rsp_utt_content);
			    ArrayList tl_utts = utts_dist.get(tl);
			    Integer tl_count = wl_dist.get(tl);
			    if (tl_count == null) {
				tl_count = new Integer(0);
				tl_utts = new ArrayList();
				utts_dist.put(tl, tl_utts);
			    }
			    tl_utts.add(rsp_utt_content);
			    wl_dist.put(tl, ++tl_count);
			    if (tl == 0) { System.out.println(rsp_utt_content + "'s length is 0");}
			    word_length += tl;
			    if (shortest_length > tl) {
				shortest_length = tl;
			    }
			    //find same speaker as response one
			    for (int k = i - 1; k > j; k--) {
				Utterance utt_btw = utts_.get(k);
				if (utt_btw.getSpeaker().equals(rsp_utt.getSpeaker())) {
				    same_spk_in_btw++;
				    break;
				}
			    }
			    //find diff speaker as response one
			    for (int k = i - 1; k > j; k--) {
				Utterance utt_btw = utts_.get(k);
				if (!utt_btw.getSpeaker().equals(rsp_utt.getSpeaker()) &&
				    !utt_btw.getSpeaker().equals(utt_.getSpeaker())) {
				    other_spk_in_btw++;
				    break;
				}
			    }
			    //System.out.println("resp of utt_content(" + rsp_utt.getSpeaker() + "): " + rsp_utt_content);
			    resp_count_++;
			    resp_sims.add(sim);
			}
			get_it = true;
			//System.out.println("find one");
			HashMap dis_counts = (HashMap)dists.get(utt_.getCommActType());
			if (dis_counts == null) {
			    dis_counts = new HashMap();
			    dists.put(utt_.getCommActType(), dis_counts);
			}
			Integer dis = new Integer(i - j);
			if (dis == 2 &&
			    utt_.getCommActType().equals(CONTINUATION_OF)) {
			    Utterance pre_utt_ = utts_.get(i - 1);
			    if (utt_.getSpeaker().equals(pre_utt_.getSpeaker())) {
				mid_same++;
			    }
			    /*
			    System.out.println("*********************************************\n" + rsp_utt
					       + "\n-------------------------------\n" + pre_utt_
					       + "\n-------------------------------\n" + utt_);
			    */
			}
			Integer dis_count = (Integer)dis_counts.get(dis);
			if (dis_count == null) {
			    dis_count = new Integer(0);
			}
			dis_counts.put(dis, dis_count + 1);
			HashMap link_cat_ = (HashMap)link_cats_.get(utt_.getCommActType());
			if (link_cat_ == null) {
			    link_cat_ = new HashMap();
			    link_cats_.put(utt_.getCommActType(), link_cat_);
			}
			Integer cat_count = (Integer)link_cat_.get(rsp_utt.getCommActType());
			if (cat_count == null) {
			    cat_count = new Integer(0);
			}
			link_cat_.put(rsp_utt.getCommActType(), cat_count + 1);
		    } else {
			if (utt_.getCommActType().equals(RESPONSE_TO)) {
			    //System.out.println("other of utt_content(" + rsp_utt.getSpeaker() + "): " + rsp_utt_content);
			    other_sims.add(sim);
			}
		    }
		    int pre = i - j;
		    if (pre > 0 && !get_it && pre < 9) {
			HashMap ca_bw = (HashMap)cas_bw.get("pre" + pre);
			if (ca_bw == null) {
			    ca_bw = new HashMap();
			    cas_bw.put("pre" + pre, ca_bw);
			}
			Integer ca_bw_count = (Integer)ca_bw.get(rsp_utt.getCommActType());
			if (ca_bw_count == null) {
			    ca_bw_count = new Integer(0);
			}
			ca_bw.put(rsp_utt.getCommActType(), ca_bw_count + 1); 

			HashMap trl_bw = (HashMap)trls_bw.get("pre" + pre);
			String rsp_utt_cont = rsp_utt.getContent();
			int rsp_utt_wc = ParseTools.wordCount(rsp_utt_cont);
			if (trl_bw == null) {
			    trl_bw = new HashMap();
			    trls_bw.put("pre" + pre, trl_bw);
			}
			Integer trl_bw_count = (Integer)trl_bw.get(rsp_utt.getCommActType());
			if (trl_bw_count == null) {
			    trl_bw_count = new Integer(0);
			}
			trl_bw.put(rsp_utt.getCommActType(), trl_bw_count + rsp_utt_wc); 
		    }
		}
		/*
		count = 0;
		for (int j = i - 1; j >= 0 && count < 10; j--, count++) {
		    Utterance rsp_utt = utts_.get(j);
		    String rsp_utt_content = rsp_utt.getContent();
		    if (utt_.getCommActType().equals(RESPONSE_TO)) {
			if (rsp_utt.getTag().equals("IRCR") ||
			    rsp_utt_content.indexOf("?") != -1) {
			    //System.out.println("find a question: " + rsp_utt_content);
			    //System.out.println("answered questions: " + answered_questions);
			    if (!answered_questions.contains(rsp_utt_content) &&
				!rsp_utt.getSpeaker().equals(utt_.getSpeaker())) {
				//System.out.println("link not found");
				utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
				System.out.println("System thought link: " + rsp_utt_content);
				answered_questions.add(rsp_utt_content);
				link_found = true;
			    }
			}
		    }
		}
		*/
		if (!link_found &&
		    utt_.getCommActType().equals(RESPONSE_TO)) {
		    count = 0;
		    for (int j = i - 1; j >= 0 && count < 10; j--, count++) {
			Utterance rsp_utt = utts_.get(j);
			if (!rsp_utt.getSpeaker().equals(utt_.getSpeaker())/* &&
									      rsp_utt.getCommActType().equals(ADDRESSED_TO)*/) {
			    utt_.setRespTo(rsp_utt.getSpeaker() + ":" + rsp_utt.getTurn());
			    link_found = true;
			    System.out.println("default the nearer, the better: " + rsp_utt.getContent());
			    break;
			}
		    }
		}
		if (utt_.getCommActType().equals(RESPONSE_TO)) {
		    if (resp_sims.size() > 0) {
			System.out.println("resp_sims: " + resp_sims);
			System.out.println("other_sims: " + other_sims);
			double resp_sim = ((Double)resp_sims.get(0)).doubleValue();
			int rank = 0;
			for (int kk = 0; kk < other_sims.size(); kk++) {
			    double other_sim = ((Double)other_sims.get(kk)).doubleValue();
			    if (resp_sim <= other_sim) rank++;
			}
			total_rank_ += rank;
		    }
		}
	    }
	}
	System.out.println("average rank of resp sim vs other sims: " + ((double)total_rank_)/resp_count_);
	System.out.println("link_cats_: " + link_cats_);
	Iterator it = link_cats_.keySet().iterator();
	//ArrayList cat_keys = new ArrayList(Arrays.asList(link_cats_.keySet()));
	//cat_keys.addAll(Arrays.asList(link_cats_.keySet()));
	//System.out.println("cat_keys: " + cat_keys);
	//for (int i = 0; i < cat_keys.size(); i++) {
	while (it.hasNext()) {
	    String cat_key = (String)it.next();
	    if (cat_key.trim().length() == 0) continue;
	    System.out.println("$$$$$$$$$$$$$$$$$$$\nthe links of " + cat_key + ": \n");
	    HashMap link_cat_ = (HashMap)link_cats_.get(cat_key);
	    HashMap dis_counts = (HashMap)dists.get(cat_key);
	    Iterator it1 = link_cat_.keySet().iterator();
	    //ArrayList resp_keys = new ArrayList(Arrays.asList(link_cat_.keySet()));
	    int all = 0;
	    while (it1.hasNext()) {
		//for (int j = 0; j < resp_keys.size(); j++) {
		String resp_key = (String)it1.next();
		all += (Integer)link_cat_.get(resp_key);
	    }
	    System.out.println("total number of backwards links: " + all);
	    it1 = link_cat_.keySet().iterator();
	    while (it1.hasNext()) {
		//for (int j = 0; j < resp_keys.size(); j++) {
		String resp_key = (String)it1.next();
		//String resp_key = (String)resp_keys.get(j);
		System.out.println("percentage of linking to " + resp_key + ": " + ((Integer)link_cat_.get(resp_key)).doubleValue()/all);
	    }
	    if (dis_counts != null) {
		it1 = dis_counts.keySet().iterator();
		while (it1.hasNext()) {
		    //for (int j = 0; j < resp_keys.size(); j++) {
		    Integer dis = (Integer)it1.next();
		    //String resp_key = (String)resp_keys.get(j);
		    System.out.println("percentage of distance(" + dis + " --- " + (Integer)dis_counts.get(dis) + ") between the link is: " + ((Integer)dis_counts.get(dis)).doubleValue()/all);
		}
		//only first words
	    }
	    HashMap first_words = (HashMap)cfirst_words_.get(cat_key);
	    it1 = first_words.keySet().iterator();
	    while (it1.hasNext()) {
		String first_word = (String)it1.next();
		Integer count = (Integer)first_words.get(first_word);
		//System.out.println("percentage of first word(" + first_word + " --- " + count + ") is: " + count.doubleValue()/((Integer)first_words_.get(first_word)));
	    }	    
	    //first words with two cont utts from one spk
	    first_words = (HashMap)acfirst_words_.get(cat_key);
	    it1 = first_words.keySet().iterator();
	    while (it1.hasNext()) {
		String first_word = (String)it1.next();
		Integer count = (Integer)first_words.get(first_word);
		//System.out.println("percentage of first word(" + first_word + " --- " + count + ") in two cont utts from same speaker is: " + count.doubleValue()/((Integer)afirst_words_.get(first_word)));
	    }	    
	    //first words with two utts from one spk with one utt in between
	    first_words = (HashMap)aacfirst_words_.get(cat_key);
	    it1 = first_words.keySet().iterator();
	    while (it1.hasNext()) {
		String first_word = (String)it1.next();
		Integer count = (Integer)first_words.get(first_word);
		//System.out.println("percentage of first word(" + first_word + " --- " + count + ") in two cont utts from same speaker with one utt in between is: " + count.doubleValue()/((Integer)aafirst_words_.get(first_word)));
	    }	    
	}
	
	System.out.println("number of adjacent utterance belonging to same speaker: " + num_adj_usr);
	System.out.println("number of next 2nd utterance belonging to same speaker: " + num_nex_same_usr);
	System.out.println("number of next 3nd utterance belonging to same speaker: " + num_nex2_same_usr);
	System.out.println("number of mid same: " + mid_same);
	

	
	Iterator it1 = cas_bw_.keySet().iterator();
	while (it1.hasNext()) {
	    String key = (String)it1.next();
	    System.out.println("**************************\n" + key);
	    HashMap cas_bw = (HashMap)cas_bw_.get(key);
	    HashMap trls_bw = (HashMap)trls_bw_.get(key);
	    Iterator it2 = cas_bw.keySet().iterator();
	    while (it2.hasNext()) {
		key = (String)it2.next();
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + key);
		HashMap ca_bw = (HashMap)cas_bw.get(key);
		HashMap trl_bw = (HashMap)trls_bw.get(key);
		if (ca_bw == null) continue;
		Iterator it3 = ca_bw.keySet().iterator();
		while (it3.hasNext()) {
		    key = (String)it3.next();
		    System.out.println(key + ": " + (Integer)ca_bw.get(key));
		    System.out.println("average length:" + ((Integer)trl_bw.get(key)).doubleValue()/(Integer)ca_bw.get(key));
		}		
	    }
	}
	Integer total_resp = (Integer)totals.get(RESPONSE_TO);
	Integer total_cont = (Integer)totals.get(CONTINUATION_OF);
	//evaluation response link performance
	int au_resp = 0;
	int au_corr_resp = 0;
	int total_resp1 = 0;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = utts_.get(i);
	    String an_lt = utt_.getRespTo();
	    String au_lt = utt_.getSysRespTo();
	    if (utt_.getCommActType() != null &&
		utt_.getCommActType().equals(RESPONSE_TO) &&
		an_lt.split(":").length > 1) {
		total_resp1++;
		if (au_lt != null) {
		    au_resp++;
		    if (au_lt.equals(an_lt)) {
			au_corr_resp++;
		    }
		}
	    }
	}
	System.out.println("Precision of response link: " + ((double)au_corr_resp)/au_resp);
	System.out.println("Recall of response link: " + ((double)au_corr_resp)/total_resp);
	//done evaluation
	//evaluation response link performance
	int au_cont = 0;
	int au_corr_cont = 0;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = utts_.get(i);
	    String an_lt = utt_.getRespTo();
	    String au_lt = utt_.getSysRespTo();
	    if (utt_.getCommActType() != null &&
		utt_.getCommActType().equals(CONTINUATION_OF)) {
		if (au_lt != null) {
		    au_cont++;
		    if (au_lt.equals(an_lt)) {
			au_corr_cont++;
		    }
		}
	    }
	}
	System.out.println("Precision of continuation link: " + ((double)au_corr_cont)/au_cont);
	System.out.println("Recall of continuation link: " + ((double)au_corr_cont)/total_cont);
	//done evaluation
	System.out.println("total_resp: " + total_resp);
	System.out.println("total_resp1: " + total_resp1);
	System.out.println("auto found: " + au_resp);
	System.out.println("total_cont: " + total_cont);
	System.out.println("auto found: " + au_cont);
	System.out.println("average times of utternace with same speaker as responsor appeared in between utt and resp utt: " + (double)same_spk_in_btw/total_resp);
	System.out.println("average times of utternace with diff speaker as origin speaker appeared in between utt and resp utt: " + (double)other_spk_in_btw/total_resp);
	System.out.println("percentage of questions are responed to: " + ((double)is_question)/total_question);
	System.out.println("percentage of questions on responded utterances: " + ((double)is_question)/total_resp);
	System.out.println("average word length of responded utterances: " + ((double)word_length)/total_resp);
	System.out.println("average size of replies on each responed utterances: " + ((double)total_resp)/unique_rp_utts.size());
	System.out.println("percentage of responding utterance contains responded speaker: " + (double)resp_spk_count/total_resp);
	System.out.println("shortest length of responded utterance: " + shortest_length);
	System.out.println("the percentage of finding responded utterance before the first utterance which has identical speaker as responded utterance: " + (double)get_to_before_id_spk/total_resp);
	System.out.println("distribution on length of responded utterances:");
	get_to_before_id_spk = 0;
	resp_spk_count = 0;
	Iterator key = total_wl_dist.keySet().iterator();
	while (key.hasNext()) {
	    Integer tl = (Integer)key.next();
	    if (wl_dist.containsKey(tl)) {
		System.out.println(tl + " " + wl_dist.get(tl).doubleValue()/total_resp);
	    }
	    if (!wl_dist.containsKey(tl)) {
		System.out.println("no " + tl + " length utterance is responded utterance");
	    } else {
		System.out.println("percentage of " + tl + " length utterances is responded utterance: " + wl_dist.get(tl).doubleValue()/total_wl_dist.get(tl));
	    }
	    System.out.println("responded utterances: \n" + utts_dist.get(tl));
	}
	for (int i = 0; i < 100; i++) {
	    key = tl_vs_resp_dist_.keySet().iterator();
	    int total_count = 0;
	    int total_dist = 0;
	    while (key.hasNext()) {
		String tl = (String)key.next();
		if (tl.startsWith(i + "_")) {
		    total_count += tl_vs_resp_dist_.get(tl);
		    total_dist += tl_vs_resp_dist_count_.get(tl);
		}
	    }
	    if (total_count > 0) {
		System.out.println("The average distance between responded utterance which term length is: " + i + " is: " + ((double)total_dist)/total_count);
		System.out.println("The percentage of responded utterance which term length is: " + i + " on total responded utterance is: " + ((double)total_count)/total_resp);
	    }
	}
	key = tl_vs_resp_dist_.keySet().iterator();
	while (key.hasNext()) {
	    String tl = (String)key.next();
	    Integer tlrs_count = tl_vs_resp_dist_.get(tl);
	    Integer tlrs_size = tl_vs_resp_dist_count_.get(tl);
	    System.out.println("The distance between responded utterance which term length is: " + tl + " is: " + tlrs_count);
	    System.out.println("The average distance between responded utterance which term length is: " + tl + " is: " + tlrs_size.doubleValue());
	}
	wl_dist.clear();
	shortest_length = 100;
	word_length = 0;
	same_spk_in_btw = 0;
	other_spk_in_btw = 0;
	is_question = 0;
	total_question = 0;
	responded_questions.clear();
	tl_vs_resp_dist_.clear();
	//writeFeatures();
    }
    /*
    public void writeSamples(String name, String info, String type) {
	sls_.writeSamples(name, info, type);
    }
    */

    /*******************************   Attributes  **************************/
    private ArrayList<Utterance> utts_ = new ArrayList<Utterance>();
    public static final String CONTINUATION_OF="continuation-of";
    public static final String ADDRESSED_TO="addressed-to";
    public static final String RESPONSE_TO="response-to";
    private HashMap link_cats_ = new HashMap();
    private HashMap cfirst_words_ = new HashMap(); //the first word of each dialogue based on comm_act_types
    private HashMap first_words_ = new HashMap(); //the first word of each dialogue
    private HashMap acfirst_words_ = new HashMap(); //the first word of each dialogue
    private HashMap afirst_words_ = new HashMap(); //the first word of each dialogue
    private HashMap aacfirst_words_ = new HashMap(); //the first word of each dialogue
    private HashMap aafirst_words_ = new HashMap(); //the first word of each dialogue
    private HashMap cas_bw_ = new HashMap(); //the communication acts in betweens
    private HashMap trls_bw_ = new HashMap(); //the turn length in betweens
    private ArrayList st_wds_ = null; //stop words
    private ArrayList resp_sims_ = new ArrayList(); //ArrayList of ArrayList
    private ArrayList total_sims_ = new ArrayList(); //ArrayList of ArrayList
    private int total_rank_ = 0;
    private int resp_count_ = 0;
    private int same_spk_in_btw = 0; //how many utterances in bwteen has same speacker as the one from response utterance
    private int other_spk_in_btw = 0; //how many utterances in between has other speakers 
    private int is_question = 0; //how many utterance responded to are questions
    private int total_question = 0; //how many question in 10 utternaces before response one
    private int word_length = 0; //the length of the utternaces responded
    private int shortest_length = 100; //the shortest of the responded utterances
    private HashMap<Integer, Integer> wl_dist = new HashMap<Integer, Integer>();
    private HashMap<Integer, ArrayList>utts_dist = new HashMap<Integer, ArrayList>();
    private HashMap<Integer, Integer> total_wl_dist = new HashMap<Integer, Integer>();
    private int get_to_before_id_spk = 0;
    private ArrayList responded_questions = new ArrayList();
    private ArrayList responded_total = new ArrayList();
    private int resp_spk_count = 0; //number of responding utterance containing responded speaker
    private HashSet<Utterance> link_founds_ = new HashSet<Utterance>();
    private HashMap<String, Integer> tl_vs_resp_dist_ = new HashMap<String, Integer>();
    private HashMap<String, Integer> tl_vs_resp_dist_count_ = new HashMap<String, Integer>(); 
    private NounList nls_ = null;
    //private LinkSamples sls_ = null; //new LinkSamples();
    private HashMap parts_ = null;
}
