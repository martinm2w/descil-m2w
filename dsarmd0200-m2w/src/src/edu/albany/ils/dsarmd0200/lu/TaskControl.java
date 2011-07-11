package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 01/08/2010
 * This class is used to judge which particant in a 
 * small discussion group has the most power defining 
 * the goal and leading to the goal
 */

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;


public class TaskControl {

    public TaskControl(HashMap spks) {
	spks_ = spks;
    }

    /*********************************get attributes**********************/

    /*********************************set attributes**********************/
    public void setUtterances(ArrayList utts) { utts_ = utts; }
    public void calTotalAdOc() {
	total_ac_oc_ = 0;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    if (utt_.getTag().equalsIgnoreCase("action-directive") ||
		utt_.getTag().equalsIgnoreCase("offer-commit") ||
		(utt_.getTag().indexOf("Action-Directive") != -1) ||
		(utt_.getTag().indexOf("Offer-Commit") != -1) ||
		utt_.getTag().equalsIgnoreCase("information-request") ||
		//utt_.getTag().equalsIgnoreCase("confirmation-request") ||
		(utt_.getTag().indexOf("Information-Request") != -1) ||/*
									 (utt_.getTag().indexOf("Confirmation-Request") != -1) ||*/
		utt_.getMTag().equalsIgnoreCase("TASK-MGMT") ||
		utt_.getMTag().equalsIgnoreCase("PRCS-MGMT")) {
		if (utt_.getTag().equalsIgnoreCase("information-request") ||
		    utt_.getTag().equalsIgnoreCase("confirmation-request") ||
		    (utt_.getTag().indexOf("Information-Request") != -1) ||
		    (utt_.getTag().indexOf("Confirmation-Request") != -1)) {
		    if (hasResponse(utt_, utts_)) {
			if (Assertions.ddt_.getType().equals(Assertions.ddt_.QA)) {
			    total_ac_oc_ += 0.2;
			}else {
			    total_ac_oc_ += 1;
			}
		    } else {
			//ac_oc_--;
		    }
		} else {
		    total_ac_oc_ += 1;
		}
		
	    }
	}
    }
    public void calTotalDTSI() {
	total_ac_oc_ = 0;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    if (utt_.getTag().equalsIgnoreCase("action-directive") ||
		utt_.getTag().equalsIgnoreCase("offer-commit") ||
		(utt_.getTag().indexOf("Action-Directive") != -1) ||
		(utt_.getTag().indexOf("Offer-Commit") != -1) ||
		utt_.getTag().equalsIgnoreCase("information-request") ||
		//utt_.getTag().equalsIgnoreCase("confirmation-request") ||
		(utt_.getTag().indexOf("Information-Request") != -1)/* ||
		(utt_.getTag().indexOf("Confirmation-Request") != -1)*/ ||
		utt_.getMTag().equalsIgnoreCase("TASK-MGMT") ||
		utt_.getMTag().equalsIgnoreCase("PRCS-MGMT")) {
		String topic = utt_.getTopic();
		if (topic == null) continue;
		int count = 0;
		boolean shifted = false;
		int same_topic = 0;
		for (int j = i + 1; j < utts_.size() && count < 10; j++, count++) {
		    Utterance fl_utt_ = (Utterance)utts_.get(j);
		    String fl_topic = fl_utt_.getTopic();
		    if (fl_topic != null &&
			fl_topic.equals(topic)) {
			same_topic++;
		    }
		}
		if (same_topic * 2 >= count) {
		    total_ac_oc_ += 1; 
		}
	    }
	}
    }
    public void calTotalPM() {
	total_ac_oc_ = 0;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    if (utt_.getMTag() != null) {
		if (utt_.getMTag().equalsIgnoreCase("TASK-MGMT") ||
		    utt_.getMTag().equalsIgnoreCase("PRCS-MGMT")) {
		    total_ac_oc_ += 1; 
		}
	    }
	}
    }
    public void calTotalPMS() {
	total_ac_oc_ = 0;
	ArrayList pm_utts = new ArrayList();
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    if (utt_.getMTag() != null) {
		if ((utt_.getMTag().equalsIgnoreCase("TASK-MGMT") ||
		     utt_.getMTag().equalsIgnoreCase("PRCS-MGMT")) &&
		    (utt_.getTag().equalsIgnoreCase("Assertion-Opinion") ||
		     utt_.getTag().equalsIgnoreCase("Action-Directive") ||
		     utt_.getTag().equalsIgnoreCase("Offer-Commit"))) {
		    total_ac_oc_ += 1; 
		    pm_utts.add(utt_);
		}
	    }
	    if (utt_.getMTag() != null) {
		if ((utt_.getMTag().equalsIgnoreCase("TASK-MGMT") ||
		     utt_.getMTag().equalsIgnoreCase("PRCS-MGMT"))) {
		    String resp = utt_.getRespTo();
		    if (resp != null) {
			String[] resps = resp.split(":");
			if (resps.length > 1) {
			    boolean found = false;
			    for (int j = pm_utts.size() - 1; j >= 0; j--) {
				Utterance pm_utt = (Utterance)pm_utts.get(j);
				if (pm_utt.getTurn().equalsIgnoreCase(resps[1])) {
				    if ((utt_.getTag().equalsIgnoreCase("Agree-Accept") ||
					 utt_.getTag().equalsIgnoreCase("Acknowledge"))) {
					total_ac_oc_ += 1;
				    } else if (utt_.getTag().equalsIgnoreCase("Disagree-Reject")) {
					total_ac_oc_ -= 1;
				    }
				    break;
				}
			    }
			}
		    }
		}
	    }
	}
    }

    public void processAD(NounList nls_) {
	if (Assertions.ddt_.getType().equals(DocDialogueType.ME)) return;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    Utterance utt_post_ = null;
	    boolean utt_post_new_spk = false;
	    if (utt_.getTag().equalsIgnoreCase("agree-accept") ||
		utt_.getTag().equalsIgnoreCase("disagree-reject")) {
		continue;
	    }
	    if ((i+1) < utts_.size()) {
		utt_post_ = utts_.get(i+1);
		utt_post_new_spk = isNewSpk(utt_post_, i);
	    }
	    String uttc_ = utt_.getTaggedContent();
	    if (uttc_ == null) continue;
	    //String[] ws = uttc_.split("[\\W]+");
	    String[] ws = uttc_.split("[\\s]+");
	    //if (ws[ws.length - 1].indexOf("?") != -1) {
	    if (ws.length < 15) {
		//System.out.println("utt: " + utt_.getContent());
		for (int k = 0; k < ws.length; k++) {
		    /*
		    if (ws.length > 1) {
			System.out.println("last two words: " + ws[ws.length -2] + " " + ws[ws.length -1]);
		    }else {
			System.out.println("last one words: " + ws[ws.length -1]);
		    }
		    */
		    String ws1 = ws[k].toLowerCase();
		    String[] ws1s = ws1.split("[\\W]+");
		    /*
		    System.out.println("ws1s length: " + ws1s.length);
		    for (int j = 0; j < ws1s.length; j++) {
			System.out.println("ws1s[" + j + "]: " + ws1s[j]);
		    }
		    */
		    if (ws1s.length == 2) {
			if (ws1s[1].startsWith("n") &&
			    (nls_.isSpeaker(ws1s[0]) ||
			     nls_.isPerson(ws1s[0]))) {
			    if (!isLocalTopic(ws1s[0]) &&
				utt_post_new_spk) {
				System.out.println("find a person name: " + uttc_ + " ----- speaker: " + utt_.getSpeaker());
				utt_.setTag("action-directive");
				break;
			    }
			} 
		    }
		    /*
		    String ws1 = ws[ws.length - 1].toLowerCase();
		    String[] ws1s = ws1.split("[\\W]+");
		    System.out.println("ws1s length: " + ws1s.length);
		    for (int j = 0; j < ws1s.length; j++) {
			System.out.println("ws1s[" + j + "]: " + ws1s[j]);
		    }
		    if (ws1s.length == 2) {
			if (ws1s[1].startsWith("n") &&
			    (nls_.isSpeaker(ws1s[0]) ||
			     nls_.isPerson(ws1s[0]))) {
			    if (!isLocalTopic(ws1s[0])) {
				System.out.println("find a person name: " + uttc_ + " ----- speaker: " + utt_.getSpeaker());
				utt_.setTag("action-directive");
			    }
			} else if (ws.length > 1) {
			    ws1 = ws[ws.length - 2].toLowerCase();
			    ws1s = ws1.split("[\\W]+");
			    if (ws1s.length > 1 &&
				ws1s[1].startsWith("n") &&
				(nls_.isSpeaker(ws1s[0]) ||
				 nls_.isPerson(ws1s[0]))) {
				 if (!isLocalTopic(ws1s[0])) {
				     System.out.println("find a person name: " + uttc_ + " ----- speaker: " + utt_.getSpeaker());
				     utt_.setTag("action-directive");
				}
			    }
			}
		    } else System.out.println("no pos tag, last one word: " + ws[ws.length - 1]);
		    */
		}
		//}
	    }
	}
    }

    public boolean isNewSpk(Utterance utt_pos, int i) {
	String uttc_ = utt_pos.getContent();
	if (uttc_ == null) {/*System.out.println("post utterance is empty!!!!!");*/ return false;}
	String[] ws = uttc_.split("[\\W]+");
	//String[] ws = uttc_.split("[\\s]+");
	//if (ws[ws.length - 1].indexOf("?") != -1) {
	for (int k = 0; k < ws.length; k++) {
	    String word = ws[k];
	    if (word.equalsIgnoreCase(utt_pos.getSpeaker())) //introduce himself/herself
		return true;
	}	    
	for (int j = 0; j < i; j++) {
	    Utterance utt_ = utts_.get(j);
	    if (utt_.getSpeaker().equals(utt_pos.getSpeaker())) { //appeared before
		return false;
	    }
	}
	return true;
    }

    public boolean isLocalTopic(String word) {
	ArrayList spks = new ArrayList(spks_.values());
	for (int i = 0; i < spks.size(); i++) {
	    Speaker spk = (Speaker)spks.get(i);
	    if (spk.isLocalTopic(word, 4)) return true;
	}
	return false;
    }

    public void calTkC(PtsReportXMLParser prxmlp, ArrayList spks, NounList nls_) {
	processAD(nls_); //added at 03/28/11 by TL
	calTotalAdOc();
	//System.out.println("total_ac_oc_: " + total_ac_oc_);
	//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	//System.out.println("++++++++++++++++++++++++++++++++\ncalculate Task Control"/* - DI+Ifr quintile*/);
	calDI(null, spks);
	/*
	calTotalPM();
	System.out.println("total_ac_oc_: " + total_ac_oc_);
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	System.out.println("++++++++++++++++++++++++++++++++\ncalculate Task Control - PMI quintile");
	calPMI(prxmlp, spks);
	calTotalPMS();
	System.out.println("total_ac_oc_: " + total_ac_oc_);
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	System.out.println("++++++++++++++++++++++++++++++++\ncalculate Task Control - PMSI quintile");
	calPMSI(null, spks);
	calTotalDTSI();
	System.out.println("total_ac_oc_: " + total_ac_oc_);
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	System.out.println("++++++++++++++++++++++++++++++++\ncalculate Task Control - DTSI quintile");
	calDTSI(null, spks);
	*/
    }
    public void calDI(PtsReportXMLParser prxmlp, ArrayList ordered_spks) {
	Iterator keys = spks_.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    Speaker spk = spks_.get(key);
	    spk.calDI(utts_, total_ac_oc_);
	}	
	keys = spks_.keySet().iterator();
	ArrayList spks = new ArrayList();
	ArrayList dis = new ArrayList();
	ArrayList dis_count = new ArrayList();
	boolean over_thr = false;
	boolean over_str_thr = false;
	while (keys.hasNext()) {
	    String key1 = (String)keys.next();
	    Speaker spk1 = spks_.get(key1);
	    boolean added = false;
	    for (int j = 0; j < spks.size(); j++) {
		Speaker spk2 = (Speaker)spks.get(j);
		if (spk1.getDI() > spk2.getDI()) {
		    spks.add(j, spk1);
		    dis.add(j, new Double(spk1.getDI()));
		    dis_count.add(j, new Double(spk1.getDICount()));
		    added = true;
		    break;
		}
	    }
	    if (!added) {
		spks.add(spk1);
		dis.add(new Double(spk1.getDI()));
		dis_count.add(new Double(spk1.getDICount()));
	    }
	    if (spk1.getDICount() >= threshold_) { over_thr = true; }
	    if (spk1.getDICount() >= str_threshold_) { over_str_thr = true; }
	    if (Assertions.ddt_.getType().equals(Assertions.ddt_.QA)) { 
		if (spk1.getDICount() >= threshold_QA_) { over_thr = true; }
		if (spk1.getDICount() >= str_threshold_QA_) { over_str_thr = true; }
	    }
	}	
	evaluate(dis, dis_count, spks, over_thr, over_str_thr, prxmlp, ordered_spks);
    }

    public void calDTSI(PtsReportXMLParser prxmlp, ArrayList ordered_spks) {
	Iterator keys = spks_.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    Speaker spk = spks_.get(key);
	    spk.calDTSI(utts_, total_ac_oc_);
	}	
	keys = spks_.keySet().iterator();
	ArrayList spks = new ArrayList();
	ArrayList dis = new ArrayList();
	boolean over_thr = false;
	boolean over_str_thr = false;
	while (keys.hasNext()) {
	    String key1 = (String)keys.next();
	    Speaker spk1 = spks_.get(key1);
	    boolean added = false;
	    for (int j = 0; j < spks.size(); j++) {
		Speaker spk2 = (Speaker)spks.get(j);
		if (spk1.getDTSI() > spk2.getDTSI()) {
		    spks.add(j, spk1);
		    dis.add(j, new Double(spk1.getDTSI()));
		    added = true;
		    break;
		}
	    }
	    if (!added) {
		spks.add(spk1);
		dis.add(new Double(spk1.getDTSI()));
	    }
	    if (spk1.getDTSICount() >= threshold_) { over_thr = true; }
	    if (spk1.getDTSICount() >= str_threshold_) { over_str_thr = true; }
	}	
	evaluate(dis, null, spks, over_thr, over_str_thr, prxmlp, ordered_spks);
    }

    public void calPMSI(PtsReportXMLParser prxmlp, ArrayList ordered_spks) {
	Iterator keys = spks_.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    Speaker spk = spks_.get(key);
	    spk.calPMSI(utts_, total_ac_oc_);
	}	
	keys = spks_.keySet().iterator();
	ArrayList spks = new ArrayList();
	ArrayList dis = new ArrayList();
	boolean over_thr = false;
	boolean over_str_thr = false;
	while (keys.hasNext()) {
	    String key1 = (String)keys.next();
	    Speaker spk1 = spks_.get(key1);
	    boolean added = false;
	    for (int j = 0; j < spks.size(); j++) {
		Speaker spk2 = (Speaker)spks.get(j);
		if (spk1.getPMSI() > spk2.getPMSI()) {
		    spks.add(j, spk1);
		    dis.add(j, new Double(spk1.getPMSI()));
		    added = true;
		    break;
		}
	    }
	    if (!added) {
		spks.add(spk1);
		dis.add(new Double(spk1.getPMSI()));
	    }
	    if (spk1.getPMSICount() >= threshold_) { over_thr = true; }
	    if (spk1.getPMSICount() >= str_threshold_) { over_str_thr = true; }
	}	
	evaluate(dis, null, spks, over_thr, over_str_thr, prxmlp, ordered_spks);
    }

    public void calPMI(PtsReportXMLParser prxmlp, ArrayList ordered_spks) {
	Iterator keys = spks_.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    Speaker spk = spks_.get(key);
	    spk.calPMI(utts_, total_ac_oc_);
	}	
	keys = spks_.keySet().iterator();
	ArrayList spks = new ArrayList();
	ArrayList dis = new ArrayList();
	boolean over_thr = false;
	boolean over_str_thr = false;
	while (keys.hasNext()) {
	    String key1 = (String)keys.next();
	    Speaker spk1 = spks_.get(key1);
	    boolean added = false;
	    for (int j = 0; j < spks.size(); j++) {
		Speaker spk2 = (Speaker)spks.get(j);
		if (spk1.getPMI() > spk2.getPMI()) {
		    spks.add(j, spk1);
		    dis.add(j, new Double(spk1.getPMI()));
		    added = true;
		    break;
		}
	    }
	    if (!added) {
		spks.add(spk1);
		dis.add(new Double(spk1.getPMI()));
	    }
	    //System.out.println("spk's pmi: " + spk1.getPMI());
	    if (spk1.getPMICount() >= threshold_) { over_thr = true; }
	    if (spk1.getPMICount() >= str_threshold_) { over_str_thr = true; }
	}	
	evaluate(dis, null, spks, over_thr, over_str_thr, prxmlp, ordered_spks);
    }

    public void evaluate (ArrayList dis,
			  ArrayList dis_count,
			  ArrayList spks,
			  boolean over_thr,
			  boolean over_str_thr,
			  PtsReportXMLParser prxmlp, 
			  ArrayList spknms) {
	HashMap qscs = new HashMap();
	HashMap final_qscs = new HashMap();
	ArrayList<Integer> rank = new ArrayList<Integer>();
	rank.add(1);
	for (int j = 1; j < dis.size(); j++) {
	    Double spk2_dis = (Double)dis.get(j);
	    Double spk1_dis = (Double)dis.get(j - 1);
	    //System.out.println("spk2_dis: " + spk2_dis);
	    //System.out.println("spk1_dis: " + spk1_dis);
	    if (spk1_dis.doubleValue() == spk2_dis.doubleValue()) {
		rank.add(rank.get(rank.size() - 1));
	    } else {
		rank.add(j + 1);
	    }
	}
	//System.out.println("task control: " + rank);
	if ((total_ac_oc_ < 4 ||
	     !over_thr) &&
	    !over_str_thr) {
	    for (int j = 0; j < dis.size(); j++) {
		int index = spknms.indexOf(((Speaker)spks.get(j)).getName());
		ArrayList val_evi = new ArrayList();
		val_evi.add(-1);
		val_evi.add("");
 		final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
		// modified by Laura, Apirl 13, 2011
                System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : " + -1);
	    }
	    if (prxmlp != null) { prxmlp.setTaskControl(final_qscs); }
	    return;
	}
	/*
	double[] dis1 = new double[7];
	dis1[0] = 0;
	dis1[1] = 0;
	dis1[2] = 0;
	dis1[3] = 10;
	dis1[4] = 23;
	dis1[5] = 29;
	dis1[6] = 39;
	double[]  qt_thrs = Util.genQTThrs(dis1);
	*/
	double[] dis1 = new double[dis.size()];
	for (int j = 0; j < dis.size(); j++) {
	    dis1[j] = ((Double)dis.get(j)).doubleValue();
	}
	double[] qt_thrs = Util.genQTThrs(dis1);
	//System.out.println("\n=====================================\n");
	boolean same_thrs = true;
	for (int j = 1; j < qt_thrs.length; j++) {
	    if (qt_thrs[j] != qt_thrs[j - 1]) {
		same_thrs = false;
		break;
	    }
	}
	if (same_thrs) {
	    for (int j = 0; j < dis.size(); j++) {
		double di = ((Double)dis.get(j)).doubleValue();
		int index = spknms.indexOf(((Speaker)spks.get(j)).getName());
		((Speaker)spks.get(j)).setTKCRank(j + 1);
		if (di == qt_thrs[0]) {
		    ((Speaker)spks.get(j)).setTkCScore(3);
                    Speaker spk = (Speaker)spks.get(j);
                    StringBuffer evidence = new StringBuffer();
                    evidence.append("Participant " + spk.getName() + " introduces " + spk.getPMI() * 100 + "% of PMI Index");
		    ArrayList val_evi = new ArrayList();
		    val_evi.add(3);//qscs.get(id_spk));
                    val_evi.add(evidence.toString());
                    //final_qscs.put(id_spk, val_evi);
 		    final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
		    if (dis_count == null) {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 3 + " --- actual score: " */+ di);
		    } else {
                    // modified by Laura, Apirl 13, 2011
			System.out.println("The quintile score of " + ((Speaker)spks.get(j)).getName() + " : "/* + 3 + " --- actual score: " */
					   /*  + "(" + dis_count.get(j) + ")"*/ + di);
		    }
		} else if (di > qt_thrs[0]) {
		    ((Speaker)spks.get(j)).setTkCScore(5);
                    Speaker spk = (Speaker)spks.get(j);
                    StringBuffer evidence = new StringBuffer();
                    evidence.append("Participant " + spk.getName() + " introduces " + spk.getPMI() * 100 + "% of PMI Index");
                    ArrayList val_evi = new ArrayList();
                    val_evi.add(5);//qscs.get(id_spk));
                    val_evi.add(evidence.toString());
                    //final_qscs.put(id_spk, val_evi);
                    final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
		    if (dis_count == null) {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 5 + " --- actual score: " */+ di);
		    } else {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 5 + " --- actual score: " */
					   /*+ "(" + dis_count.get(j) + ")"*/ + di);
		    }
 		    //System.out.println("The quintile score " + ((Speaker)spks.get(j)).getName() + " is: " + 5 + " --- actual score: " + di);
		} else if (di < qt_thrs[1]) {
		    ((Speaker)spks.get(j)).setTkCScore(1);
                    Speaker spk = (Speaker)spks.get(j);
                    StringBuffer evidence = new StringBuffer();
                    evidence.append("Participant " + spk.getName() + " introduces " + spk.getPMI() * 100 + "% of PMI Index");
                    ArrayList val_evi = new ArrayList();
                    val_evi.add(1);//qscs.get(id_spk));
                    val_evi.add(evidence.toString());
                    //final_qscs.put(id_spk, val_evi);
                    final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
		    // qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), 1);
		    if (dis_count == null) {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 1 + " --- actual score: " */+ di);
		    } else {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 1 + " --- actual score: " */
					   /*  + "(" + dis_count.get(j) + ")" */+ di);
		    }
		    //System.out.println("The quintile score " + ((Speaker)spks.get(j)).getName() + " is: " + 1 + " --- actual score: " + di);
		}	
	    }
	    if (prxmlp != null) { prxmlp.setTaskControl(qscs); }
	}
	else {
	    for (int j = 0; j < dis.size(); j++) {
		double di = ((Double)dis.get(j)).doubleValue();
		int index = spknms.indexOf(((Speaker)spks.get(j)).getName());
		((Speaker)spks.get(j)).setTKCRank(rank.get(j).intValue());
		if (qt_thrs[0] == qt_thrs[1] &&
		    qt_thrs[1] == qt_thrs[2] &&
		    di == qt_thrs[0] &&
		    di != 0) {
		    ((Speaker)spks.get(j)).setTkCScore(4);
		    if (dis_count == null) {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 4 + " --- actual score: " */+ di);
		    } else {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 4 + " --- actual score: "*/
					   /* + "(" + dis_count.get(j) + ")" */ + di);
		    }
		    //System.out.println("The quintile score " + ((Speaker)spks.get(j)).getName() + " is: " + 4 + " --- actual score: " + di);
                    Speaker spk = (Speaker)spks.get(j);
                    StringBuffer evidence = new StringBuffer();
                    evidence.append("Participant " + spk.getName() + " introduces " + spk.getPMI() * 100 + "% of PMI Index");
                    ArrayList val_evi = new ArrayList();
                    val_evi.add(4);//qscs.get(id_spk));
                    val_evi.add(evidence.toString());
                    //final_qscs.put(id_spk, val_evi);
                    final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
		} else if (di == 0) {
                    Speaker spk = (Speaker)spks.get(j);
                    StringBuffer evidence = new StringBuffer();
                    evidence.append("Participant " + spk.getName() + " introduces " + spk.getPMI() * 100 + "% of PMI Index");
                    ArrayList val_evi = new ArrayList();
                    val_evi.add(1);//qscs.get(id_spk));
                    val_evi.add(evidence.toString());
                    //final_qscs.put(id_spk, val_evi);
                    final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
		    if (dis_count == null) {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 1 + " --- actual score: " */+ di);
		    } else {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 1 + " --- actual score: " */
					   /*  + "(" + dis_count.get(j) + ")"*/ + di);
		    }
 		    //System.out.println("The quintile score " + ((Speaker)spks.get(j)).getName() + " is: " + 1 + " --- actual score: " + di);
		} else if (di > qt_thrs[0]) {
		    ((Speaker)spks.get(j)).setTkCScore(5);
                    Speaker spk = (Speaker)spks.get(j);
                    StringBuffer evidence = new StringBuffer();
                    evidence.append("Participant " + spk.getName() + " introduces " + spk.getPMI() * 100 + "% of PMI Index");
                    ArrayList val_evi = new ArrayList();
                    val_evi.add(5);//qscs.get(id_spk));
                    val_evi.add(evidence.toString());
                    //final_qscs.put(id_spk, val_evi);
                    final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
 		    //qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), 5);
		    if (dis_count == null) {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 5 + " --- actual score: "*/ + di);
		    } else {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 5 + " --- actual score: " */
					   /*  + "(" + dis_count.get(j) + ")" */ + di);
		    }
		    //System.out.println("The quintile score of " + ((Speaker)spks.get(j)).getName() + " is: " + 5 + " --- actual score: " + di);
		} else if (di <= qt_thrs[0] &&
			   di > qt_thrs[1]) {
		    ((Speaker)spks.get(j)).setTkCScore(4);
                    Speaker spk = (Speaker)spks.get(j);
                    StringBuffer evidence = new StringBuffer();
                    evidence.append("Participant " + spk.getName() + " introduces " + spk.getPMI() * 100 + "% of PMI Index");
                    ArrayList val_evi = new ArrayList();
                    val_evi.add(4);//qscs.get(id_spk));
                    val_evi.add(evidence.toString());
                    //final_qscs.put(id_spk, val_evi);
                    final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
		    if (dis_count == null) {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : " /*+ 4 + " --- actual score: "*/ + di);
		    } else {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : " /*+ 4 + " --- actual score: " */
					   /*  + "(" + dis_count.get(j) + ")"*/ + di);
		    }
 		    //System.out.println("The quintile score " + ((Speaker)spks.get(j)).getName() + " is: " + 4 + " --- actual score: " + di);
		} else if (di <= qt_thrs[1] &&
			   di > qt_thrs[2]) {
		    ((Speaker)spks.get(j)).setTkCScore(3);
                    Speaker spk = (Speaker)spks.get(j);
                    StringBuffer evidence = new StringBuffer();
                    evidence.append("Participant " + spk.getName() + " introduces " + spk.getPMI() * 100 + "% of PMI Index");
                    ArrayList val_evi = new ArrayList();
                    val_evi.add(3);//qscs.get(id_spk));
                    val_evi.add(evidence.toString());
                    //final_qscs.put(id_spk, val_evi);
                    final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
		    if (dis_count == null) {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " +*/ ((Speaker)spks.get(j)).getName() + " : "/* + 3 + " --- actual score: " */+ di);
		    } else {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " +*/ ((Speaker)spks.get(j)).getName() + " : "/* + 3 + " --- actual score: "*/
					   /*  + "(" + dis_count.get(j) + ")" */ + di);
		    }
		    //System.out.println("The quintile score " + ((Speaker)spks.get(j)).getName() + " is: " + 3 + " --- actual score: " + di);
		} else if (di <= qt_thrs[2] &&
			   di > qt_thrs[3]) {
		    ((Speaker)spks.get(j)).setTkCScore(2);
                    Speaker spk = (Speaker)spks.get(j);
                    StringBuffer evidence = new StringBuffer();
                    evidence.append("Participant " + spk.getName() + " introduces " + spk.getPMI() * 100 + "% of PMI Index");
                    ArrayList val_evi = new ArrayList();
                    val_evi.add(2);//qscs.get(id_spk));
                    val_evi.add(evidence.toString());
                    //final_qscs.put(id_spk, val_evi);
                    final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
		    if (dis_count == null) {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 2 + " --- actual score: " */+ di);
		    } else {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 2 + " --- actual score: " */
					   /*   + "(" + dis_count.get(j) + ")" */ + di);
		    }
		    //System.out.println("The quintile score " + ((Speaker)spks.get(j)).getName() + " is: " + 2 + " --- actual score: " + di);
		} else if (di <= qt_thrs[3]) {
		    ((Speaker)spks.get(j)).setTkCScore(1);
                    Speaker spk = (Speaker)spks.get(j);
                    StringBuffer evidence = new StringBuffer();
                    evidence.append("Participant " + spk.getName() + " introduces " + spk.getPMI() * 100 + "% of PMI Index");
                    ArrayList val_evi = new ArrayList();
                    val_evi.add(1);//qscs.get(id_spk));
                    val_evi.add(evidence.toString());
                    //final_qscs.put(id_spk, val_evi);
                    final_qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), val_evi);
 		    //qscs.put(index + "_" + ((Speaker)spks.get(j)).getName(), 1);
		    if (dis_count == null) {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 1 + " --- actual score: " */+ di);
		    } else {
                    // modified by Laura, Apirl 13, 2011
			System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 1 + " --- actual score: " */
					   /*  + "(" + dis_count.get(j) + ")" */ + di);
		    }
		    //System.out.println("The quintile score " + ((Speaker)spks.get(j)).getName() + " is: " + 1 + " --- actual score: " + di);
		}
	    }
	    if (prxmlp != null) { prxmlp.setTaskControl(final_qscs); }
	}
    }

    public boolean hasResponse(Utterance utt_, ArrayList utts_) {
	String turn_no = utt_.getSpeaker() + ":" + utt_.getTurn();
	//System.out.println("turn_no: " + turn_no);
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt = (Utterance)utts_.get(i);
	    if (utt.getRespTo().equals(turn_no) &&
		!utt.getSpeaker().equals(utt_.getSpeaker())) {
		//System.out.println("Has response: " + utt + "\n" + utt_);
		return true;
	    }
	}
	return false;
    }

    /*******************************   Attributes  **************************/
    double total_ac_oc_ = 0; //direct index
    HashMap<String, Speaker> spks_ = null; //the speaker who has this control
    ArrayList<Utterance> utts_ = null;
    int str_threshold_ = 3;
    int str_threshold_QA_ = 1;
    int threshold_ = 2;
    int threshold_QA_ = 1;
}
