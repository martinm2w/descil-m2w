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
import java.util.*;

public class DisagreeRejectIndex {
    public DisagreeRejectIndex(ArrayList utts) {
	utts_ = utts;
    }
    /*********************************get attributes**********************/
    /*********************************set attributes**********************/
    public void calDRI() {
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    //System.out.println("processing: " + utt_.getTag());
	    if (utt_.getTag().equals(DATagger.DISAGREE_REJECT) ||
		(utt_.getTag().indexOf(DATagger.NDISAGREE_REJECT1) != -1) ||
		(utt_.getTag().indexOf(DATagger.NDISAGREE_REJECT2) != -1) /*|| //1/26/2011
									    utt_.getPolarity().equalsIgnoreCase("negative")*/) {
		//System.out.println("utt_.getTag(): " + utt_.getTag());
		total_dri_++;
		String cur_spk = utt_.getSpeaker();
		String resp_spk = getRespSpk(utt_.getRespTo(), i);
		if (resp_spk == null || resp_spk.equals("all users") || cur_spk.equals("all users") || resp_spk.equals(cur_spk)) continue;
		String spk_pair = cur_spk + "_" + resp_spk;
		Integer dis_count = (Integer)dris_.get(spk_pair);
		if (dis_count == null) {
		    spk_pair = resp_spk + "_" + cur_spk;
		    dis_count = (Integer)dris_.get(spk_pair);
		    if (dis_count == null) {
			dis_count = new Integer(0);
		    }
		}
		dis_count += 1;
		dris_.put(spk_pair, dis_count);
	    }
	}
	Iterator keys = dris_.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    Integer dis_count = (Integer) dris_.get(key);
	    dris_sc_.put(key, new Double(dis_count.doubleValue()/total_dri_));
	}
	//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	//System.out.println("++++++++++++++++++++++++++++++++\ncalculate Expressive Disagreement - DRX quintile");
	genQuintileSc();
    }

    public void genQuintileSc() {
	Iterator keys = dris_sc_.keySet().iterator();
	ArrayList spks = new ArrayList();
	ArrayList dis = new ArrayList();
	ArrayList qt_dis = new ArrayList();
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
		if (di > qt_thrs[0]) {
		    qt_dis.add(new Integer(5));
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"\nThe quintile score of " + */(String)spks.get(j) + " : "/* + 5 + " --- actual score: " */+ di);
		} else if (di == qt_thrs[0]) {
		    qt_dis.add(new Integer(3));
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 3 + " --- actual score: " */+ di);
		} else if (di < qt_thrs[1]) {
		    qt_dis.add(new Integer(1));
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 1 + " --- actual score: " */+ di);
		}
	    }
	} else {
	    for (int j = 0; j < dis.size(); j++) {
		double di = ((Double)dis.get(j)).doubleValue();
		if (qt_thrs[0] == qt_thrs[1] &&
		    qt_thrs[1] == qt_thrs[2] &&
		    di == qt_thrs[0]) {
		    qt_dis.add(new Integer(4));
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 4 + " --- actual score: " */+ di);
		} else if (di > qt_thrs[0]) {
		    qt_dis.add(new Integer(5));
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"\nThe quintile score of " + */(String)spks.get(j) + " : "/* + 5 + " --- actual score: " */+ di);
		} else if (di <= qt_thrs[0] &&
			   di > qt_thrs[1]) {
		    qt_dis.add(new Integer(4));
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 4 + " --- actual score: " */+ di);
		} else if (di <= qt_thrs[1] &&
			   di > qt_thrs[2]) {
		    qt_dis.add(new Integer(3));
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 3 + " --- actual score: " */+ di);
		} else if (di <= qt_thrs[2] &&
			   di > qt_thrs[3]) {
		    qt_dis.add(new Integer(2));
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 2 + " --- actual score: " */+ di);
		} else if (di <= qt_thrs[3]) {
		    qt_dis.add(new Integer(1));
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */(String)spks.get(j) + " : "/* + 1 + " --- actual score: " */+ di);
		}
	    }
	}
    }

    public String getRespSpk(String resp_to,
			    int startP) {
	String[] turn_no = resp_to.split(":");
	if (true) return turn_no[0];
	if (turn_no.length < 2) return null;
	for (int j = startP - 1; j >= 0;  j--) {
	    Utterance utt_ = (Utterance)utts_.get(j);
	    if (utt_.getTurn().equals(turn_no[1])) {
		return utt_.getSpeaker();
	    }
	}
	return null;
    }

    /*******************************   Attributes  **************************/
    private int total_dri_ = 0; //direct index
    private HashMap dris_ = new HashMap();
    private HashMap dris_sc_ = new HashMap();
    private ArrayList utts_ = new ArrayList();
}
