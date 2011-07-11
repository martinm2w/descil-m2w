package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 01/08/2010
 * This class is used to judge which particant in a 
 * small discussion group has the most influence on 
 * the dicussing topic
 */

import java.io.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.util.*;
import java.util.*;

public class TopicControl {

    public TopicControl(Speaker spk) {
	spk_ = spk;
    }

    /*******************************get information**************************/
    public int getLTIR() { return lti_r_; }
    public int getSMTR() { return smt_r_; }
    public int getCSR() { return cs_r_; }
    public int getTLR() { return tl_r_; }
    public int getCLTIR() { return c_lti_r_; }

    public double getLTI() { return lti_; }
    public double getSMT() { return smt_; }
    public double getCS() { return cs_; }
    public double getTL() { return tl_; }
    public double getCLTI() { return c_lti_; }
    public double getPower() { return power_; }



    /*******************************set information**************************/
    public void calLTI() {
	lti_ = (double)spk_.sizeOfILTs()/lts_.filteredSize(); //modified at 03/15 by TL
    }
    
    public void calSMT() {
	LocalTopics ilts = spk_.getILTs();
	smt_ = (double)ilts.sizeOfMentions(spk_.getName())/lts_.sizeOfMentions();
    }

    public void calCS() {
	cs_ = (double)spk_.sizeOfCLTs()/(lts_.sizeOfCO());
    }
    
    public void calTL(ArrayList<Utterance> all_utts) {
	ArrayList<Utterance> utts = spk_.getUtterances();
	//System.out.println("processing TL of the utterances from " + spk_.getName());
	int utts_wc = 0;
	for (int i = 0; i < utts.size(); i++) {
	    Utterance utt = utts.get(i);
	    //System.out.println("utt: " + utt.getContent());
	    int l = ParseTools.wordCount(utt.getContent());
	    utts_wc += l;
	    //System.out.println("length: " + l);
	}
	/*
	int all_utts_wc = 0;
	for (int i = 0; i < all_utts.size(); i++) {
	    Utterance all_utt = all_utts.get(i);
	    all_utts_wc += ParseTools.wordCound(utt.getContent());
	}
	*/
	//System.out.println("utts_wc: " + utts_wc);
	//System.out.println("utts: " + utts.size());
	tl_ = (double)utts_wc/utts.size();
	//System.out.println("turn length: " + tl_);
    }

    public void calCLTI() {
	//System.out.println("sizeOfSSLTs: " + spk_.sizeOfSSLTs());
	//System.out.println("sizeOfILTs: " + spk_.sizeOfILTs());
	c_lti_ = (double)spk_.sizeOfCLTs()/(spk_.sizeOfILTs());
    }

    public void calTpCtl(LocalTopics lts_,
			 ArrayList all_utts) {
	this.lts_ = lts_;
	calLTI(); //topic introduction
	calSMT(); //subsequent mention of topics
	calCS();  //cite score
	calTL(all_utts);  //turn length
	calCLTI();//cite score vs topic introduction
	calPower();
    }

    public void calTLPt(double total_tl) {
	tl_pt_ = tl_/total_tl;
    }

    public void setQScore(double sc, String type) {
	if (type.equals("LTI")) {
	    qlti_ = sc;
	} else if (type.equals("SMT")) {
	    qsmt_ = sc;
	} else if (type.equals("CS")) {
	    qcs_ = sc;
	} else if (type.equals("TL")) {
	    qtl_ = sc;
	} else if (type.equals("CLTI")) {
	    qc_lti_ = sc;
	} else if (type.equals("Power")) {
	    qpower_ = sc;
	}
    }

    public void calPower() {
	//power_ = ((double)lti_ + smt_ + cs_)/3;// + tl_pt_/2)/4; //8 03/21/2011 for JNLP paper
	power_ = ((double)lti_ + smt_ + cs_ + tl_pt_/2)/4; //8 03/22/2011 change it back
    }
    public void incLTIR() { lti_r_++; }
    public void incSMTR() { smt_r_++; }
    public void incCSR() { cs_r_++; }
    public void incTLR() { tl_r_++; }
    public void incCLTIR() { c_lti_r_++; }

    public String toString() {
	StringBuffer out = new StringBuffer();
	out.append("From: " + spk_.getName());
	out.append("\nparameters: \nLTI: " + lti_ + "\nSMTI: " + smt_ + "\nCite Score: " + cs_ + "\nTLI: " + tl_ + "\n C-LTI: " + c_lti_);
	return out.toString();
    }

    /*******************************   Attributes  **************************/
    private double lti_ = 0; //local topic introduction
    private double smt_ = 0; //subsequent mentions of topic
    private double cs_ = 0; //subsequent mention of lcoal topics excluding self mention
    private double tl_ = 0; //turn length
    private double tl_pt_ = 0; //the percentage of turn length
    private double c_lti_ = 0; //subsequent mentions per local topic introduction
    private double qlti_ = 0; //local topic introduction
    private double qsmt_ = 0; //subsequent mentions of topic
    private double qcs_ = 0; //subsequent mention of lcoal topics excluding self mention
    private double qtl_ = 0; //turn length
    private double qc_lti_ = 0; //subsequent mentions per local topic introduction
    //ranks of the parameter compare with other speakers
    private int lti_r_ = 0; //local topic introduction
    private int smt_r_ = 0; //subsequent mentions of topic
    private int cs_r_ = 0; //subsequent mention of lcoal topics excluding self mention
    private int tl_r_ = 0; //turn length
    private int c_lti_r_ = 0; //subsequent mentions per local topic introduction
    private double power_ = 0;
    private double qpower_ = 0;
    private Speaker spk_ = null; //the user who paritcipates the discussion
    private LocalTopics lts_ = null;
}
