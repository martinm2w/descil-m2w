package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 03/19/2010
 * This class is used to calculate the indices of 
 * disagreement between two speakers
 */

import java.io.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
import java.util.*;

public class CumulativeEDisagreementIndex {
    public CumulativeEDisagreementIndex(ArrayList utts) {
	utts_ = utts;
    }
    /*********************************get attributes**********************/
    /*********************************set attributes**********************/
    public void calCEDI(PtsReportXMLParser prxmlp, ArrayList spks,
			HashMap<String, Speaker> parts) {
	if (parts == null) {System.out.println("parts are null!!!!"); return;}
	int total_pl_neg = 0;
	int total_ex_dis = 0;
	int total_count = 0;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    String cur_spk = utt_.getSpeaker();
	    //System.out.println("processing: " + utt_.getTag());
	    if (utt_.getTag().equalsIgnoreCase(DATagger.DISAGREE_REJECT) ||
		(utt_.getTag().toLowerCase().indexOf(DATagger.NDISAGREE_REJECT1) != -1) || //1/26/2011
		(utt_.getTag().toLowerCase().indexOf(DATagger.NDISAGREE_REJECT2) != -1)/* || //1/26/2011
											  utt_.getPolarity().equalsIgnoreCase("negative")*/) { 
		//System.out.println("utt_.getTag(): " + utt_.getTag());
		/*
		if (utt_.getPolarity().equalsIgnoreCase("negative")) {
		    System.out.println("negative opion: " + utt_.getContent());
		    total_pl_neg++;
		}
		*/
		if (utt_.getTag().equalsIgnoreCase(DATagger.DISAGREE_REJECT) ||
		    (utt_.getTag().toLowerCase().indexOf(DATagger.NDISAGREE_REJECT1) != -1) || //1/26/2011
		    (utt_.getTag().toLowerCase().indexOf(DATagger.NDISAGREE_REJECT2) != -1)) {
		    total_ex_dis++;
		}
		total_count++;
		total_dri_++;
		Integer dis_count = (Integer)dris_.get(cur_spk);
		if (dis_count == null) {
		    dis_count = new Integer(0);
		}
		dis_count += 1;
		dris_.put(cur_spk, dis_count);
	    }
	    Integer count = (Integer)utts_count_.get(cur_spk);
	    if (count == null) {
		count = new Integer(0);
	    }
	    count++;
	    utts_count_.put(cur_spk, count);
	}
	System.out.println("total negative: " + total_pl_neg);
	System.out.println("total dis: " + total_ex_dis);
	System.out.println("total count: " + total_count);
	//System.out.println("utts_count:\n" + utts_count_);
	//System.out.println("dris_count:\n" + dris_);
	boolean over_thr = false;
	Iterator keys = dris_.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    Integer dis_count = (Integer) dris_.get(key);
	    if (dis_count >= threshold_) {over_thr = true; break;}
	}
	keys = dris_.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    Integer dis_count = (Integer) dris_.get(key);
	    if (over_thr) {
		System.out.println("number of disagreement of " + key + " is: " + dis_count);
		dris_sc_.put(key, new Double(dis_count.doubleValue()/total_dri_));
		if (parts.get(key) == null) {
		    //System.out.println(key + "is not a part!!!");
		    continue;
		}
		parts.get(key).setDisagreement(dis_count.doubleValue()/total_dri_);
	    }else {
		dris_sc_.put(key, new Double(-1));
		parts.get(key).setDisagreement(-1);
	    }
	}
	for (int i = 0; i < spks.size(); i++) {
	    if (!dris_sc_.containsKey(spks.get(i))) {
		if (over_thr) {
		    dris_sc_.put(spks.get(i), new Double(0));
		    parts.get(spks.get(i)).setDisagreement(0);
		} else {
		    dris_sc_.put(spks.get(i), new Double(-1));
		    //System.out.println("speaker: " + spks.get(i));
		    //System.out.println("parts: " + parts);
		    parts.get(spks.get(i)).setDisagreement(-1);
		}
	    }
	}
	//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	//System.out.println("++++++++++++++++++++++++++++++++\ncalculate Expressive Disagreement - CDXI quintile");
	genQuintileSc(prxmlp, spks, parts);
    }

    public void genQuintileSc(PtsReportXMLParser prxmlp, ArrayList spknms, HashMap<String, Speaker> parts) {
	Iterator keys = dris_sc_.keySet().iterator();
	ArrayList spks = new ArrayList();
	ArrayList dis = new ArrayList();
	ArrayList qt_dis = new ArrayList();
	HashMap qscs = new HashMap();
	while (keys.hasNext()) {
	    String key1 = (String)keys.next();
	    Double dis_sc1 = (Double)dris_sc_.get(key1);
	    boolean added = false;
	    for (int j = 0; j < spks.size(); j++) {
		Double dis_sc2 = (Double)dis.get(j);
		if (dis_sc1 > dis_sc2) {
		    spks.add(j, key1);
		    dis.add(j, dis_sc1);
		    added = true;
		    break;
		}
	    }
	    if (!added) {
		spks.add(key1);
		dis.add(dis_sc1);
	    }
	}	
	ArrayList<Integer> rank = new ArrayList<Integer>();
	rank.add(1);
	for (int j = 1; j < dis.size(); j++) {
	    Double spk2_dis = (Double)dis.get(j);
	    Double spk1_dis = (Double)dis.get(j - 1);
	    if (spk1_dis.doubleValue() == spk2_dis.doubleValue()) {
		rank.add(rank.get(rank.size() - 1));
	    } else {
		rank.add(j + 1);
	    }
	}
	double[] dis1 = new double[dis.size()];
	boolean is_available = true;
	for (int j = 0; j < dis.size(); j++) {
	    dis1[j] = ((Double)dis.get(j)).doubleValue();
	    if (dis1[j] == -1 ||
		dis1[j] == Double.NaN) {is_available = false; break;}
	}
	if (!is_available) {
	    for (int j = 0; j < dis.size(); j++) {
		int index = spknms.indexOf(spks.get(j));
		ArrayList val_evi = new ArrayList();
		val_evi.add(-1);
		val_evi.add("");
		qscs.put(index + "_" + spks.get(j), val_evi);
                // modified by Laura, Apirl 13, 2011
		System.out.println(/*"\nThe quintile score of " + */(String)spks.get(j) + " : " + -1);
	    }
	    if (prxmlp != null) { prxmlp.setExpressiveDisagreementClaim(qscs); }
	    return;
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
		int index = spknms.indexOf(spks.get(j));
		String spk = (String)spknms.get(index);
		ArrayList val_evi = new ArrayList();
		parts.get(spk).setEXDRank(-1);
		if (di > qt_thrs[0]) {
		    qt_dis.add(new Integer(5));
		    val_evi.add(5);
		    val_evi.add("Participant " + spk + " contributes " + di * 100 + "% of CDX Index;");
		    qscs.put(index + "_" + spks.get(j), val_evi);
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"\nThe quintile score of " + */(String)spks.get(j) + " : "/* + 5 + " --- actual score: "*/ + di);
		} else if (di == qt_thrs[0]) {
		    qt_dis.add(new Integer(3));
		    val_evi.add(3);
		    val_evi.add("Participant " + spk + " contributes " + di * 100 + "% of CDX Index;");
		    qscs.put(index + "_" + spks.get(j), val_evi);
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 3 + " --- actual score: "*/ + di);
		} else if (di < qt_thrs[1]) {
		    qt_dis.add(new Integer(1));
		    val_evi.add(1);
		    val_evi.add("Participant " + spk + " contributes " + di * 100 + "% of CDX Index;");
		    qscs.put(index + "_" + spks.get(j), val_evi);
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 1 + " --- actual score: " */+ di);
		}
	    }
	} else {
	    for (int j = 0; j < dis.size(); j++) {
		double di = ((Double)dis.get(j)).doubleValue();
		int index = spknms.indexOf(spks.get(j));
		if (index == -1) continue;
		String spk = (String)spknms.get(index);
		ArrayList val_evi = new ArrayList();
		parts.get(spk).setEXDRank(rank.get(j).intValue());
		if (qt_thrs[0] == qt_thrs[1] &&
		    qt_thrs[1] == qt_thrs[2] &&
		    di == qt_thrs[0]) {
		    qt_dis.add(new Integer(4));
		    val_evi.add(4);
		    val_evi.add("Participant " + spk + " contributes " + di * 100 + "% of CDX Index;");
		    qscs.put(index + "_" + spks.get(j), val_evi);
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 4 + " --- actual score: "*/ + di);
		} else if (di > qt_thrs[0]) {
		    qt_dis.add(new Integer(5));
		    val_evi.add(5);
		    val_evi.add("Participant " + spk + " contributes " + di * 100 + "% of CDX Index;");
		    qscs.put(index + "_" + spks.get(j), val_evi);
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"\nThe quintile score of " + */(String)spks.get(j) + " : "/* + 5 + " --- actual score: "*/ + di);
		} else if (di <= qt_thrs[0] &&
			   di > qt_thrs[1]) {
		    qt_dis.add(new Integer(4));
		    val_evi.add(4);
		    val_evi.add("Participant " + spk + " contributes " + di * 100 + "% of CDX Index;");
		    qscs.put(index + "_" + spks.get(j), val_evi);
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 4 + " --- actual score: "*/ + di);
		} else if (di <= qt_thrs[1] &&
			   di > qt_thrs[2]) {
		    qt_dis.add(new Integer(3));
		    val_evi.add(3);
		    val_evi.add("Participant " + spk + " contributes " + di * 100 + "% of CDX Index;");
		    qscs.put(index + "_" + spks.get(j), val_evi);
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 3 + " --- actual score: "*/ + di);
		} else if (di <= qt_thrs[2] &&
			   di > qt_thrs[3]) {
		    qt_dis.add(new Integer(2));
		    val_evi.add(2);
		    val_evi.add("Participant " + spk + " contributes " + di * 100 + "% of CDX Index;");
		    qscs.put(index + "_" + spks.get(j), val_evi);
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 2 + " --- actual score: "*/ + di);
		} else if (di <= qt_thrs[3]) {
		    qt_dis.add(new Integer(1));
		    val_evi.add(1);
		    val_evi.add("Participant " + spk + " contributes " + di * 100 + "% of CDX Index;");
		    qscs.put(index + "_" + spks.get(j), val_evi);
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 1 + " --- actual score: "*/ + di);
		}
	    }
	}
	prxmlp.setExpressiveDisagreementClaim(qscs);
    }

    /*******************************   Attributes  **************************/
    private int total_dri_ = 0; //direct index
    private HashMap dris_ = new HashMap();
    private HashMap dris_sc_ = new HashMap();
    private HashMap utts_count_ = new HashMap();
    private ArrayList utts_ = new ArrayList();
    private int threshold_ = 2; //at least one speaker has 3 directive actions
}
