package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 01/08/2010
 * This class is used to judge which particant in a 
 * small discussion group has the most influence on 
 * the dicussing topic
 */

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.util.*;

public class Involvement {

    public Involvement(Speaker spk) {
	spk_ = spk;
    }

    /*******************************get information**************************/
    public int getNPIR() { return np_i_r_; }
    public int getTIR() { return ti_r_; }
    public int getTCIR() { return tci_r_; }
    public int getALLOTPR() { return allo_tp_r_; }
    public int getASMIR() { return asm_i_r_; }

    public double getNPI() { return np_i_; }
    public double getTI() { return ti_; }
    public double getTCI() { return tci_; }
    public double getALLOTP() { return allo_tp_; }
    public double getASMI() { return asm_i_; }
    public double getPower() { return power_; }


    /*******************************set information**************************/
    public void calInvolvement(LocalTopics lts,
			       ArrayList utts,
			       NounList nls,
			       ArrayList top10nouns) {
	top10nouns_ = top10nouns;
	all_lts_ = lts;
	all_utts_ = utts;
	all_nls_ = nls;
	calNPInd();
	calTI();
	calTCI();
	calASM();
	calAllotopicality();
	calPower();
    }

    public void calAInvolvement(LocalTopics lts,
				ArrayList utts,
				ArrayList nls,
				ArrayList top10nouns, 
				ArrayList spks) {
	top10nouns_ = top10nouns;
	all_lts_ = lts;
	all_utts_ = utts;
	nls_ = nls;
	spks_ = spks;
	calANPInd();
	calTI();
	calTCI();
	calASM();
	calAllotopicality();
	calPower();
    }

    public void calNPInd() {
	ArrayList nouns = all_nls_.getThPNouns();
	int npc = 0;
	int total_npc = 0;
	//System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&persons mentioned by " + spk_.getName());
	//System.out.println("nouns: \n" + nouns);
	for (int i = 0; i < nouns.size(); i++) {
	    NounToken nt = (NounToken)nouns.get(i);
	    if (nt.getSpeaker().equalsIgnoreCase(spk_.getName())/* && //comment it out 03/22
								   all_nls_.isThP(nt.getWord())*/) {
		//System.out.println("found " + nt.getWord() + " from " + nt.getTurnNo());
		npc++;
	    }
	    //if (all_nls_.isThP(nt.getWord())) { //comment it out 03/22/11
		total_npc++;
		//}
	}
	np_i_ = ((double)npc)/total_npc;
    }

    public void calANPInd() {
	ArrayList nouns = nls_;
	if (nouns == null) return;
	int npc = 0;
	int total_npc = 0;
	//System.out.println("nouns size is: " + nouns.size());
	//System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&persons mentioned by " + spk_.getName());
	for (int i = 0; i < nouns.size(); i++) {
	    NounToken nt = (NounToken)nouns.get(i);
	    if (//nt.getSpeaker().equalsIgnoreCase(spk_.getName()) && //comment it out 03/22
		NounList.isAThP(nt.getWord(), spks_)) {
		//System.out.println("found " + nt.getWord() + " from " + nt.getTurnNo());
		npc++;
	    }
	    //if (NounList.isAThP(nt.getWord(), spks_)) { //comment it out 03/22
		total_npc++;
		//}
	}
	np_i_ = ((double)npc)/total_npc;
    }

    public void calTI() {
	ti_ = ((double)spk_.getUtterances().size())/all_utts_.size();
    }

    public void calTCI() {
	ArrayList nouns = new ArrayList();
	//System.out.println("in Invo, sizeof top10nouns_: " + top10nouns_.size());
	for (int i = 0; i < top10nouns_.size(); i++) {
	    NounToken tn = (NounToken)top10nouns_.get(i);
	    if (tn.getSpeaker().equals(spk_.getName())) {
		nouns.add(tn);
	    }
	}
	tci_ = ((double) nouns.size()) / top10nouns_.size();
    }

    public void calASM() {
	int count = spk_.sizeofSMs();
	//System.out.println("all_lts_: " + all_lts_);
	asm_i_ = ((double)count)/all_lts_.sizeOfMentions();
    }

    public void calAllotopicality() {
	int sz_spk_co = spk_.sizeofCO();
	//System.out.println("sz_spk_co: " + sz_spk_co);
	//System.out.println("sizeof all_lts_ CO: " + all_lts_.sizeofCO());
	allo_tp_ = ((double)sz_spk_co)/all_lts_.sizeOfCO();
    }

    public void setQScore(double sc, String type) {
	if (type.equals("NPI")) {
	    qnp_i_ = sc;
	} else if (type.equals("TI")) {
	    qti_ = sc;
	} else if (type.equals("TCI")) {
	    qtci_ = sc;
	} else if (type.equals("ASMI")) {
	    qasm_i_ = sc;
	} else if (type.equals("ALLOTP")) {
	    qallo_tp_ = sc;
	} else if (type.equals("Power")) {
	    qpower_ = sc;
	}
    }

    public void calPower() {
	//power_ = ((double)np_i_ + /*ti_*/ + tci_ + asm_i_ + allo_tp_)/4;//5;03/21 for JNLP paper
	power_ = ((double)np_i_ + ti_ + tci_ + asm_i_ + allo_tp_)/5;//03/22 change it back
    }
    public void incNPIR() { np_i_r_++; }
    public void incTIR() { ti_r_++; }
    public void incTCIR() { tci_r_++; }
    public void incALLOTPR() { allo_tp_r_++; }
    public void incASMIR() { asm_i_r_++; }



    public String toString() {
	StringBuffer out = new StringBuffer();
	/*
	out.append("NPI: " + np_i_);
	out.append("\nTI: " + ti_);
	out.append("\nTCI: " + tci_);
	out.append("\nASMI: " + asm_i_);
	out.append("\nALLOTOPICALITY: " + allo_tp_);
	*/
	out.append("\n" + np_i_);
	out.append("\n" + ti_);
	out.append("\n" + tci_);
	out.append("\n" + asm_i_);
	out.append("\n" + allo_tp_);
	return out.toString();
    }

    /*******************************   Attributes  **************************/
    private double np_i_ = 0; //NP index
    private double ti_ = 0; //turn index
    private double tci_ = 0; //topic chain index
    private double asm_i_ = 0; //percentage of subsequent mentions of local topics
    private double allo_tp_ = 0; //number of mentions of local topic topics by others
    private double qnp_i_ = 0; //NP index
    private double qti_ = 0; //turn index
    private double qtci_ = 0; //topic chain index
    private double qasm_i_ = 0; //percentage of subsequent mentions of local topics
    private double qallo_tp_ = 0; //number of mentions of local topic topics by others
    //the rank of each parameters of this peaker among all speakers
    private int np_i_r_ = 0; //NP index
    private int ti_r_ = 0; //turn index
    private int tci_r_ = 0; //topic chain index
    private int asm_i_r_ = 0; //percentage of subsequent mentions of local topics
    private int allo_tp_r_ = 0; //number of mentions of local topic topics by others
    private double power_= 0;
    private double qpower_= 0;
    private String usr_id_ = null; //the user who paritcipates the discussion
    private LocalTopics all_lts_ = null;
    private Speaker spk_ = null;
    private ArrayList all_utts_ = null; //all utterance of the chat
    private NounList all_nls_ = null; //all noun list extracted from chat
    private ArrayList nls_ = null;
    private ArrayList top10nouns_ = new ArrayList(); //the nouns and mentions of top 10 nouns which have longest chains
    private ArrayList spks_ = null;
}
