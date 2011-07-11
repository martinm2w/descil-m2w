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
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

public class TopicDisagreement {
    public TopicDisagreement(ArrayList all_utts_,
			     ArrayList tr_utts_,
			     ArrayList utts_,
			     Wordnet wn) {
	opinion_ = new Opinion(all_utts_, tr_utts_, utts_, wn);
    }

    

    /*******************************set information**************************/
    public void tagPolarity() {
	opinion_.tagPolarity();
    }

    public void buildTrainMap() {
	opinion_.buildTrainMap();
    }

    public void calDisagreement(HashMap parts) {
	parts_ = parts;
	ArrayList topics = new ArrayList();
	ArrayList keys = new ArrayList(parts_.keySet());
	//System.out.println("keys size: " + keys.size());
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    Speaker spk = parts_.get(key);
	    spk.collTPPolarities(topics);
	}
	//System.out.println("topics: " + topics);
	//calQTScore(topics, Opinion.POSITIVE);
	//calQTScore(topics, Opinion.NEGATIVE);
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    //System.out.println("processing: " + key);
	    Speaker spk = parts_.get(key);
	    spk.calTPDis(topics);
	}
	calTDM(topics);
	//calCTD(topics);
    }

    public void calQTDisagreement(HashMap parts) {
	parts_ = parts;
	ArrayList topics = new ArrayList();
	ArrayList keys = new ArrayList(parts_.keySet());
	//System.out.println("keys size: " + keys.size());
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    Speaker spk = parts_.get(key);
	    spk.collTPPolarities(topics);
	}
	//System.out.println("topics: " + topics);
	calQTScore(topics, Opinion.POSITIVE);
	calQTScore(topics, Opinion.NEGATIVE);
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    //System.out.println("processing: " + key);
	    Speaker spk = parts_.get(key);
	    spk.calQTDis(topics);
	}
	calQTTDM(topics);
	//calCTD(topics);
    }

    public void calSDDisagreement(HashMap parts) {
	parts_ = parts;
	ArrayList topics = new ArrayList();
	ArrayList keys = new ArrayList(parts_.keySet());
	//System.out.println("keys size: " + keys.size());
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    Speaker spk = parts_.get(key);
	    spk.collTPPolarities(topics);
	}
	//System.out.println("topics: " + topics);
	calPCScore(topics, Opinion.POSITIVE);
	calPCScore(topics, Opinion.NEGATIVE);
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    //System.out.println("processing: " + key);
	    Speaker spk = parts_.get(key);
	    spk.calPCDis(topics);
	}
	calSDTDM(topics);
	//calCTD(topics);
    }

    public void calQTScore(ArrayList topics,
			   String pola) {
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    System.out.println("processing the topic: " + topic);
	    ArrayList keys = new ArrayList(parts_.keySet());
	    ArrayList spks_pls = new ArrayList();
	    ArrayList polas = new ArrayList();
	    //System.out.println("keys: " + keys);
	    for (int jj = 0; jj < keys.size(); jj++) {
		String key = (String)keys.get(jj);
		Speaker spk = parts_.get(key);
		//System.out.println("processing: " + spk.getName());
		if (spk.getTPPola().get(topic) == null) continue; //this speaker didn't involve in this topic
		ArrayList spk_counts = (ArrayList)spk.getTPPola().get(topic);
		//System.out.println("topic: " + topic + " | speaker: " + key + " | opinion: " + spk_counts);
		Integer spk_count = new Integer(0);
		if (pola.equals(Opinion.POSITIVE)) {
		    spk_count = (Integer)spk_counts.get(0);
		}else {
		    spk_count = (Integer)spk_counts.get(1);
		}
		for (int k = 0; k < spks_pls.size(); k++) {
		    Speaker spk1 = (Speaker)spks_pls.get(k);
		    ArrayList spk1_counts = (ArrayList)spk1.getTPPola().get(topic);
		    Integer spk1_count = new Integer(0);
		    if (pola.equals(Opinion.POSITIVE)) {
			spk1_count = (Integer)spk1_counts.get(0);
		    }
		    if (pola.equals(Opinion.NEGATIVE)) {
			spk_count = (Integer)spk_counts.get(1);
			spk1_count = (Integer)spk1_counts.get(1);
		    }
		    if (spk1_count < spk_count) {
			spks_pls.add(k, spk);
			polas.add(k, spk_count);
			break;
		    }
		}
		if (!spks_pls.contains(spk)) {
		    spks_pls.add(spk);
		    polas.add(spk_count);
		}
	    }
	    System.out.println("topic: " + topic);
	    //System.out.println("spks_pls: " + spks_pls);
	    //System.out.println("polas: " + polas);
	    double[] pls = new double[polas.size()];
	    for (int j = 0; j < polas.size(); j++) {
		pls[j] = ((Integer)polas.get(j)).doubleValue();
	    }
	    double[] qt_thrs = genQTThrs(pls);
	    boolean same_thrs = true;
	    for (int j = 1; j < qt_thrs.length; j++) {
		if (qt_thrs[j] != qt_thrs[j - 1]) {
		    same_thrs = false;
		    break;
		}
	    }
	    if (same_thrs) {
		for (int j = 0; j < polas.size(); j++) {
		    double pl = ((Integer)polas.get(j)).doubleValue();
		    if (pl == qt_thrs[0]) {
			((Speaker)spks_pls.get(j)).setQTScore(topic, 3, pola);
			System.out.println("The quintile score of " + ((Speaker)spks_pls.get(j)).getName() + ": " + 3 + " --- actual score: " + pl);
		    } else if (pl > qt_thrs[0]) {
			((Speaker)spks_pls.get(j)).setQTScore(topic, 5, pola);
			System.out.println("The quintile score " + ((Speaker)spks_pls.get(j)).getName() + ": " + 5 + " --- actual score: " + pl);
		    } else if (pl < qt_thrs[0]) {
			((Speaker)spks_pls.get(j)).setQTScore(topic, 1, pola);
			System.out.println("The quintile score " + ((Speaker)spks_pls.get(j)).getName() + ": " + 1 + " --- actual score: " + pl);
		    }
		}
	    } else {
		for (int j = 0; j < polas.size(); j++) {
		    double pl = ((Integer)polas.get(j)).doubleValue();
		    if (qt_thrs[0] == qt_thrs[1] &&
			qt_thrs[1] == qt_thrs[2] &&
			pl == qt_thrs[0]) {
			((Speaker)spks_pls.get(j)).setQTScore(topic, 4, pola);
			System.out.println("The quintile score of " + ((Speaker)spks_pls.get(j)).getName() + ": " + 4 + " --- actual score: " + pl);
		    }
		    else if (pl > qt_thrs[0]) {
			((Speaker)spks_pls.get(j)).setQTScore(topic, 5, pola);
			System.out.println("The quintile score of " + ((Speaker)spks_pls.get(j)).getName() + ": " + 5 + " --- actual score: " + pl);
		    } else if (pl <= qt_thrs[0] &&
			       pl > qt_thrs[1]) {
			((Speaker)spks_pls.get(j)).setQTScore(topic, 4, pola);
			System.out.println("The quintile score " + ((Speaker)spks_pls.get(j)).getName() + ": " + 4 + " --- actual score: " + pl);
		    } else if (pl <= qt_thrs[1] &&
			       pl > qt_thrs[2]) {
			((Speaker)spks_pls.get(j)).setQTScore(topic, 3, pola);
			System.out.println("The quintile score " + ((Speaker)spks_pls.get(j)).getName() + ": " + 3 + " --- actual score: " + pl);
		    } else if (pl <= qt_thrs[2] &&
			       pl > qt_thrs[3]) {
			((Speaker)spks_pls.get(j)).setQTScore(topic, 2, pola);
			System.out.println("The quintile score " + ((Speaker)spks_pls.get(j)).getName() + ": " + 2 + " --- actual score: " + pl);
		    } else if (pl <= qt_thrs[3]) {
			((Speaker)spks_pls.get(j)).setQTScore(topic, 1, pola);
			System.out.println("The quintile score " + ((Speaker)spks_pls.get(j)).getName() + ": " + 1 + " --- actual score: " + pl);
		    }
		}
	    }
	}
    }


    public void calPCScore(ArrayList topics,
			   String pola) {
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    System.out.println("processing the topic: " + topic);
	    ArrayList keys = new ArrayList(parts_.keySet());
	    ArrayList spks_pls = new ArrayList();
	    ArrayList polas = new ArrayList();
	    int total = 0;
	    //System.out.println("keys: " + keys);
	    for (int jj = 0; jj < keys.size(); jj++) {
		String key = (String)keys.get(jj);
		Speaker spk = parts_.get(key);
		//System.out.println("processing: " + spk.getName());
		if (spk.getTPPola().get(topic) == null) continue; //this speaker didn't involve in this topic
		ArrayList spk_counts = (ArrayList)spk.getTPPola().get(topic);
		//System.out.println("topic: " + topic + " | speaker: " + key + " | opinion: " + spk_counts);
		Integer spk_count = new Integer(0);
		
		if (pola.equals(Opinion.POSITIVE)) {
		    spk_count = (Integer)spk_counts.get(0);
		}else {
		    spk_count = (Integer)spk_counts.get(1);
		}
		total += spk_count;
	    }
	    for (int jj = 0; jj < keys.size(); jj++) {
		String key = (String)keys.get(jj);
		Speaker spk = parts_.get(key);
		//System.out.println("processing: " + spk.getName());
		if (spk.getTPPola().get(topic) == null) continue; //this speaker didn't involve in this topic
		ArrayList spk_counts = (ArrayList)spk.getTPPola().get(topic);
		//System.out.println("topic: " + topic + " | speaker: " + key + " | opinion: " + spk_counts);
		Integer spk_count = new Integer(0);
		
		if (pola.equals(Opinion.POSITIVE)) {
		    spk_count = (Integer)spk_counts.get(0);
		}else {
		    spk_count = (Integer)spk_counts.get(1);
		}
		spk.setPCScore(topic, (spk_count.doubleValue())/total, pola);
	    }
	}
    }


    public void calQTTDM(ArrayList topics) {
	HashMap tp_ops = new HashMap();
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    System.out.println("processing the topic: " + topic);
	    ArrayList keys = new ArrayList(parts_.keySet());
	    ArrayList spks_pls = new ArrayList();
	    HashMap ops = new HashMap();
	    //System.out.println("keys: " + keys);
	    for (int jj = 0; jj < keys.size(); jj++) {
		String key = (String)keys.get(jj);
		Speaker spk = parts_.get(key);
		//System.out.println("processing: " + spk.getName());
		if (spk.getQTDis().get(topic) == null) continue; //this speaker didn't involve in this topic
		for (int k = 0; k < spks_pls.size(); k++) {
		    Speaker spk1 = (Speaker)spks_pls.get(k);
		    if (((Integer)spk1.getQTDis().get(topic)) < ((Integer)spk.getQTDis().get(topic))) {
			spks_pls.add(k, spk);
			break;
		    }
		}
		if (!spks_pls.contains(spk)) {
		    spks_pls.add(spk);
		}
	    }
	    //System.out.println("spks_pls size: " + spks_pls.size());
	    for (int jj = 0; jj < spks_pls.size() - 1; jj++) {
		Speaker spk1 = (Speaker)spks_pls.get(jj);
		for (int k = jj + 1; k < spks_pls.size(); k++) {
		    Speaker spk2 = (Speaker)spks_pls.get(k);
		    double op_dis = (((Integer)spk1.getQTDis().get(topic)) - ((Integer)spk2.getQTDis().get(topic)))/2.0 + 1;
		    ops.put(spk1.getName() + "_" + spk2.getName(), new Double(op_dis));
		}
	    }
	    tp_ops.put(topic, ops);
	}
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    HashMap ops = (HashMap)tp_ops.get(topic);
	    Iterator it = ops.keySet().iterator();
	    while (it.hasNext()) {
		String spk = (String)it.next();
		//System.out.print(spk + "\t");
	    }
	    if (topic.trim().length() == 0) continue;
	    System.out.println("++++++++++++++++++++++++++++++++\nTopic Disagreement on Meso-Topic \"" + topic + "\"");
	    it = ops.keySet().iterator();
	    while (it.hasNext()) {
		String spk = (String)it.next();
		//System.out.print(spk + ": " + ops.get(spk));
	    }
	    //System.out.println("+++++++++++++++++++++++++++++++++++quintile scores");
	    genQTThrs(ops);
	    System.out.println();
	    System.out.println();
	    System.out.println();
	}
    }

    public void calSDTDM(ArrayList topics) {
	HashMap tp_ops = new HashMap();
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    System.out.println("processing the topic: " + topic);
	    ArrayList keys = new ArrayList(parts_.keySet());
	    ArrayList spks_pls = new ArrayList();
	    HashMap ops = new HashMap();
	    //System.out.println("keys: " + keys);
	    for (int jj = 0; jj < keys.size(); jj++) {
		String key = (String)keys.get(jj);
		Speaker spk = parts_.get(key);
		//System.out.println("processing: " + spk.getName());
		if (spk.getPCDis().get(topic) == null) continue; //this speaker didn't involve in this topic
		//System.out.println("spk.getPCDis().get(topic): " + spk.getPCDis().get(topic));
		for (int k = 0; k < spks_pls.size(); k++) {
		    Speaker spk1 = (Speaker)spks_pls.get(k);
		    if (((Double)spk1.getPCDis().get(topic)) < ((Double)spk.getPCDis().get(topic))) {
			spks_pls.add(k, spk);
			break;
		    }
		}
		if (!spks_pls.contains(spk)) {
		    spks_pls.add(spk);
		}
	    }
	    //System.out.println("spks_pls size: " + spks_pls.size());
	    for (int jj = 0; jj < spks_pls.size() - 1; jj++) {
		Speaker spk1 = (Speaker)spks_pls.get(jj);
		for (int k = jj + 1; k < spks_pls.size(); k++) {
		    Speaker spk2 = (Speaker)spks_pls.get(k);
		    double op_dis = ((Double)spk1.getPCDis().get(topic)) - ((Double)spk2.getPCDis().get(topic));
		    ops.put(spk1.getName() + "_" + spk2.getName(), new Double(op_dis));
		}
	    }
	    tp_ops.put(topic, ops);
	}
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    HashMap ops = (HashMap)tp_ops.get(topic);
	    Iterator it = ops.keySet().iterator();
	    while (it.hasNext()) {
		String spk = (String)it.next();
		//System.out.print(spk + "\t");
	    }
	    if (topic.trim().length() == 0) continue;
	    System.out.println("++++++++++++++++++++++++++++++++\nTopic Disagreement on Meso-Topic \"" + topic + "\"");
	    it = ops.keySet().iterator();
	    while (it.hasNext()) {
		String spk = (String)it.next();
		//System.out.print(spk + ": " + ops.get(spk));
	    }
	    //System.out.println("+++++++++++++++++++++++++++++++++++quintile scores");
	    calSDScore(ops);
	    System.out.println();
	    System.out.println();
	    System.out.println();
	}
    }

    public void calTDM(ArrayList topics) {
	HashMap tp_ops = new HashMap();
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    //System.out.println("##########################processing the topic: " + topic);
	    ArrayList keys = new ArrayList(parts_.keySet());
	    ArrayList spks_pls = new ArrayList();
	    HashMap ops = new HashMap();
	    //System.out.println("keys: " + keys);
	    //System.out.println("Name\t|Positive\t|Negative\t|Distance");
	    for (int jj = 0; jj < keys.size(); jj++) {
		String key = (String)keys.get(jj);
		Speaker spk = parts_.get(key);
		if (spk.getTPDis().get(topic) == null) continue; //this speaker didn't involve in this topic
		//spk.printTPPolarInfo(topic);
		for (int k = 0; k < spks_pls.size(); k++) {
		    Speaker spk1 = (Speaker)spks_pls.get(k);
		    if (((Integer)spk1.getTPDis().get(topic)) < ((Integer)spk.getTPDis().get(topic))) {
			spks_pls.add(k, spk);
			break;
		    }
		}
		if (!spks_pls.contains(spk)) {
		    spks_pls.add(spk);
		}
	    }
	    //System.out.println("spks_pls size: " + spks_pls.size());
	    //System.out.println("speakers' distance on " + topic);
	    for (int jj = 0; jj < spks_pls.size() - 1; jj++) {
		Speaker spk1 = (Speaker)spks_pls.get(jj);
		for (int k = jj + 1; k < spks_pls.size(); k++) {
		    Speaker spk2 = (Speaker)spks_pls.get(k);
		    double op_dis = ((Integer)spk1.getTPDis().get(topic)) - ((Integer)spk2.getTPDis().get(topic));
		    //System.out.println(spk1.getName() + "_" + spk2.getName() + "\t" + op_dis);
		    ops.put(spk1.getName() + "_" + spk2.getName(), new Double(op_dis));
		}
	    }
	    tp_ops.put(topic, ops);
	}
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    HashMap ops = (HashMap)tp_ops.get(topic);
	    Iterator it = ops.keySet().iterator();
	    while (it.hasNext()) {
		String spk = (String)it.next();
		//System.out.print(spk + "\t");
	    }
	    if (topic == null || topic.trim().length() == 0) continue;
	    System.out.println("++++++++++++++++++++++++++++++++\nTopic Disagreement on Meso-Topic \"" + topic + "\"");
	    it = ops.keySet().iterator();
	    while (it.hasNext()) {
		String spk = (String)it.next();
		//System.out.print(spk + ": " + ops.get(spk));
	    }
	    //System.out.println("+++++++++++++++++++++++++++++++++++quintile scores");
	    genQTThrs(ops);
	    System.out.println();
	    System.out.println();
	    System.out.println();
	}
    }

    public void calCTD(ArrayList topics) {
	//calculate
	HashMap all_ops = new HashMap();
	ArrayList keys = new ArrayList(parts_.keySet());
	for (int i = 0; i < keys.size() - 1; i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    for (int j = i + 1; j < keys.size(); j++) {
		String key2 = (String)keys.get(j);
		Speaker spk2 = parts_.get(key2);
		double oa_ops = 0;
		int count = 0;
		for (int k = 0; k < topics.size(); k++) {
		    String topic = (String)topics.get(k);
		    Integer spk1_op = (Integer)spk1.getQTDis().get(topic);
		    Integer spk2_op = (Integer)spk2.getQTDis().get(topic);
		    if (spk1_op == null) continue; //spk1_op = new Integer(0);
		    if (spk2_op == null) continue; //spk2_op = new Integer(0);
		    count++;
		    if (spk1_op > spk2_op) {
			oa_ops += (spk1_op - spk2_op)/2.0 + 1;
		    } else {
			oa_ops += (spk2_op - spk1_op)/2.0 + 1;
		    }
		}
		oa_ops = oa_ops/count;//topics.size();
		all_ops.put(spk1.getName() + "_" + spk2.getName(), new Double(oa_ops));
	    }
	}
	
	Iterator it = all_ops.keySet().iterator();
	//System.out.print(" \t");
	while (it.hasNext()) {
	    String spk = (String)it.next();
	    //System.out.print(spk + "\t");
	}
	//System.out.print("\noverall");
	it = all_ops.keySet().iterator();
	while (it.hasNext()) {
	    String spk = (String)it.next();
	    //System.out.print("\t" + all_ops.get(spk));
	}
	genQTThrs(all_ops);
    }

    /*******************************get information**************************/
    public void genQTThrs(HashMap pls) {
	Iterator it = pls.keySet().iterator();
	ArrayList spk_pars = new ArrayList();
	ArrayList spk_pls = new ArrayList();
	while (it.hasNext()) {
	    String spk_par1 = (String)it.next();
	    Double spk_pl1 = (Double)pls.get(spk_par1);
	    boolean added = false;
	    for (int i = 0; i < spk_pls.size(); i++) {
		Double spk_pl2 = (Double)spk_pls.get(i);
		if (spk_pl1 > spk_pl2) {
		    spk_pls.add(i, spk_pl1);
		    spk_pars.add(i, spk_par1);
		    added = true;
		    break;
		}
	    }
	    if (!added) {
		spk_pls.add(spk_pl1);
		spk_pars.add(spk_par1);
	    }
	}
	double[] pls1 = new double[spk_pls.size()];
	for (int j = 0; j < spk_pls.size(); j++) {
	    pls1[j] = ((Double)spk_pls.get(j)).doubleValue();
	}
	double[] qt_thrs = genQTThrs(pls1);
	//System.out.print("\ntype");
	/*
	for (int j = 0; j < spk_pars.size(); j++) {
	    String spk_par = (String)spk_pars.get(j);
	    System.out.print("\t" + spk_par);
	}
	*/
	System.out.println();
	ArrayList spk_qts = new ArrayList();
	for (int j = 0; j < spk_pls.size(); j++) {
	    double pl = ((Double)spk_pls.get(j)).doubleValue();
	    if (pl > qt_thrs[0]) {
		spk_qts.add(new Integer(5));
		System.out.println("The quintile score of " + (String)spk_pars.get(j) + ": " + 5 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    } else if (pl <= qt_thrs[0] &&
		       pl > qt_thrs[1]) {
		spk_qts.add(new Integer(4));
		System.out.println("The quintile score of " + (String)spk_pars.get(j) + ": " + 4 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    } else if (pl <= qt_thrs[1] &&
		       pl > qt_thrs[2]) {
		spk_qts.add(new Integer(3));
		System.out.println("The quintile score of " + (String)spk_pars.get(j) + ": " + 3 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    } else if (pl <= qt_thrs[2] &&
		       pl > qt_thrs[3]) {
		spk_qts.add(new Integer(2));
		System.out.println("The quintile score of " + (String)spk_pars.get(j) + ": " + 2 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    } else if (pl <= qt_thrs[3]) {
		spk_qts.add(new Integer(1));
		System.out.println("The quintile score of " + (String)spk_pars.get(j) + ": " + 1 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    }
	    //System.out.print("\t" + (Integer)spk_qts.get(j));
	}
	System.out.println();
	
    }

    public double[] genQTThrs(double[] pls) {
	double[] qt_thrs = new double[4];
	qt_thrs[0] = pl_.evaluate(pls, 80);
	qt_thrs[1] = pl_.evaluate(pls, 60);
	qt_thrs[2] = pl_.evaluate(pls, 40);
	qt_thrs[3] = pl_.evaluate(pls, 20);
	System.out.print("qt_thrs: ");
	for (int i = 0; i < qt_thrs.length; i++) {
	    System.out.print(" " + qt_thrs[i]);
	}
	return qt_thrs;
    }

    public void calSDScore(HashMap pls) {
	Iterator it = pls.keySet().iterator();
	ArrayList spk_pars = new ArrayList();
	ArrayList spk_pls = new ArrayList();
	while (it.hasNext()) {
	    String spk_par1 = (String)it.next();
	    Double spk_pl1 = (Double)pls.get(spk_par1);
	    boolean added = false;
	    for (int i = 0; i < spk_pls.size(); i++) {
		Double spk_pl2 = (Double)spk_pls.get(i);
		if (spk_pl1 > spk_pl2) {
		    spk_pls.add(i, spk_pl1);
		    spk_pars.add(i, spk_par1);
		    added = true;
		    break;
		}
	    }
	    if (!added) {
		spk_pls.add(spk_pl1);
		spk_pars.add(spk_par1);
	    }
	}
	if (spk_pls.size() < 2) return;
	//System.out.println("spk_pls: " + spk_pls);
	double[] pls1 = new double[spk_pls.size()];
	for (int j = 0; j < spk_pls.size(); j++) {
	    pls1[j] = ((Double)spk_pls.get(j)).doubleValue();
	}
	double[] qt_thrs = genSDThrs(pls1);
	//System.out.print("\ntype");
	/*
	for (int j = 0; j < spk_pars.size(); j++) {
	    String spk_par = (String)spk_pars.get(j);
	    System.out.print("\t" + spk_par);
	}
	*/
	System.out.println();
	ArrayList spk_qts = new ArrayList();
	int[] scores = new int[spk_pls.size()];
	for (int j = 0; j < scores.length; j++) {
	    scores[j] = -1;
	}
	dynamicScoring(spk_pls, qt_thrs[0], 0.1 * qt_thrs[4], qt_thrs[0] - 0.5 * qt_thrs[4], 5, scores);
	dynamicScoring(spk_pls, qt_thrs[1], 0.1 * qt_thrs[4], qt_thrs[1] - 0.5 * qt_thrs[4], 4, scores);
	dynamicScoring(spk_pls, qt_thrs[3], -0.1 * qt_thrs[4], qt_thrs[3] + 0.5 * qt_thrs[4], 1, scores);
	dynamicScoring(spk_pls, qt_thrs[2], -0.1 * qt_thrs[4], qt_thrs[2] + 0.5 * qt_thrs[4], 2, scores);
	for (int j = 0; j < spk_pls.size(); j++) {
	    double pl = ((Double)spk_pls.get(j)).doubleValue();
	    if (scores[j] == -1) scores[j] = 3;
	    //System.out.println("The disagreement score between " + (String)spk_pars.get(j) + ": " + scores[j] + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    /*
	    if (j == 0) {
		spk_qts.add(new Integer(5));
		System.out.println("The disagreement score between " + (String)spk_pars.get(j) + ": " + 5 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
		continue;
	    }
	    if (j == spk_pls.size() - 1) {
		spk_qts.add(new Integer(1));
		System.out.println("The disagreement score between " + (String)spk_pars.get(j) + ": " + 1 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
		continue;
	    }
	    if (pl > qt_thrs[0]) {
		spk_qts.add(new Integer(5));
		System.out.println("The disagreement score between " + (String)spk_pars.get(j) + ": " + 5 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    } else if (pl <= qt_thrs[0] &&
		       pl > qt_thrs[1]) {
		spk_qts.add(new Integer(4));
		System.out.println("The disagreement score between " + (String)spk_pars.get(j) + ": " + 4 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    } else if (pl <= qt_thrs[1] &&
		       pl > qt_thrs[2]) {
		spk_qts.add(new Integer(3));
		System.out.println("The disagreement score between " + (String)spk_pars.get(j) + ": " + 3 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    } else if (pl <= qt_thrs[2] &&
		       pl > qt_thrs[3]) {
		spk_qts.add(new Integer(2));
		System.out.println("The disagreement score between " + (String)spk_pars.get(j) + ": " + 2 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    } else if (pl <= qt_thrs[3]) {
		spk_qts.add(new Integer(1));
		System.out.println("The disagreement score between " + (String)spk_pars.get(j) + ": " + 1 + " --- actual score: " + (Double)pls.get((String)spk_pars.get(j)));
	    }
	    */
	    //System.out.print("\t" + (Integer)spk_qts.get(j));
	}
	System.out.println();
	
    }

    public double[] genSDThrs(double[] pls) {
	double[] qt_thrs = new double[5];
	double mean = pl_.evaluate(pls, 50);
	double sd = sd_.evaluate (pls, mean);
	//System.out.println("mean: " + mean + " sd: " + sd);
	qt_thrs[0] = mean + 1.5 * sd;
	//qt_thrs[0] = mean + 1 * sd;
	qt_thrs[1] = mean + 0.5 * sd;
	qt_thrs[2] = mean - 0.5 * sd;
	qt_thrs[3] = mean - 1.5 * sd;
	qt_thrs[4] = sd;
	//qt_thrs[3] = mean - 1 * sd;
	System.out.print("mean_sd_thrs: ");
	for (int i = 0; i < qt_thrs.length; i++) {
	    System.out.print(" " + qt_thrs[i]);
	}
	return qt_thrs;
    }

    public void dynamicScoring(ArrayList spk_pls, 
			       double threshold, 
			       double adj,
			       double stop,
			       int score,
			       int[] scores) {
	boolean assigned = false;
	for (int j = 0; j < spk_pls.size(); j++) {
	    double pl = ((Double)spk_pls.get(j)).doubleValue();
	    if (score > 2) {
		if (pl > threshold && scores[j] == -1) {
		    scores[j] = score;
		    assigned = true;
		}
	    }else {
		if (pl < threshold && scores[j] == -1) {
		    scores[j] = score;
		    assigned = true;
		}
	    }
	}
	if (assigned) return;
	threshold -= adj;
	if (adj > 0) {
	    if (threshold <= stop) return;
	} else if (adj < 0) {
	    if (threshold >= stop) return;
	}
	dynamicScoring(spk_pls, threshold, adj, stop, score, scores);
    }

    /*******************************attributes*******************************/
    private Opinion opinion_ = null;
    private HashMap<String, Speaker> parts_ = null; //speakers
    private Percentile pl_ = new Percentile();
    private StandardDeviation sd_ = new StandardDeviation();
    private HashMap<String, ArrayList> sds_ = new HashMap<String, ArrayList>(); //key is topic, Arraylist contains mean and sd
}
