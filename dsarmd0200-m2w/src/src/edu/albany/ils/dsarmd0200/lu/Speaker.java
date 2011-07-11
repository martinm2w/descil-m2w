package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 02/08/2010
 * This class is used to hold the information of each participants
 */

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class Speaker {
    public Speaker(String name) {
	name_ = name;
    }

    /*******************************get information**************************/
    public String getName() { return name_; }
    public ArrayList<Utterance> getUtterances() {return utts_;}
    public LocalTopics getILTs() {return ilts_;}
    public int sizeOfILTs() {
	//modified 03/15 by TL
	int size = 0;
	for (int i = 0; i < ilts_.size(); i++) {
	    LocalTopic ilt = (LocalTopic)ilts_.get(i);
	    //if (ilt.sizeofCO() < 50) {
		NounToken nt = ilt.getContent();
		if (Util.filterIt(name_, nt)){
		    continue;
		}
		//}
	    size++;
	}
	//return ilts_.size();
    return size;
    }
    // added by Laura, Mar 8, 2011
    public LocalTopics getCLTs() {
        return clts_;
    }
    //public int sizeOfILTs() {return ilts_.size();}
    public int sizeOfCLTs() {
	int count = 0;
	for (int i = 0; i < ilts_.size(); i++) {
	    LocalTopic clt = (LocalTopic)ilts_.get(i);
	    //modified by Ting 01/07/2011
	    /*
	    if (clt.getCited(name_).size() > 5) 
		count += 1;
	    */
	    //Added at 03/15 by TL
	    if (clt.sizeofCO() < 15) {
		NounToken nt = clt.getContent();
		if (Util.filterIt(name_, nt)) {
		    continue;
		}
	    }
	    count += clt.getCited(name_).size();
	}
	return count;
    }
    public int sizeofCO() { //size of citing others
	int count = 0;
	for (int i = 0; i < clts_.size(); i++) {
	    LocalTopic clt = (LocalTopic)clts_.get(i);
	    if (clt == null) continue;
	    count += clt.sizeofCO(name_);
	}
	//System.out.println("clts_: " + clts_);
	return count;
    }
    public int sizeOfSSLTs() {
	int count = 0;
	for (int i = 0; i < ilts_.size(); i++) {
	    LocalTopic ilt = (LocalTopic)ilts_.get(i);
	    count += ilt.getMentions().size() - 1;
	}
	return count;
    }
    public int sizeofSMs() {
	int count = 0;
	for (int i = 0; i < ilts_.size(); i++) {
	    LocalTopic clt = (LocalTopic)ilts_.get(i);
	    count += clt.getMentions(name_, false).size();
	}

	for (int i = 0; i < clts_.size(); i++) {
	    LocalTopic clt = (LocalTopic)clts_.get(i);
	    if (clt == null) continue;
	    ArrayList clt_mentions = clt.getMentions(name_, true);
	    if (clt_mentions != null) {
		count += clt_mentions.size();
	    }
	}
	return count;
    }
    public TopicControl getTC() { return tc_; }
    public ArrayList getNounList() { return nls_; }
    public Involvement getInv() { return inv_; }
    public HashMap getTPPola() { return polarities_; }
    public HashMap getQTDis() {return qt_polas_dis_; }
    public HashMap getPCDis() {return pc_polas_dis_; }
    public HashMap getTPDis() {return polas_dis_; }
    public HashMap getQTScore() {return qt_polarities_; }
    public HashMap getPCScore() {return pc_polarities_; }
    public double getDI() { return di_.getDI(); }
    public double getDICount() { return di_.getCount(); }
    public double getDTSI() { return dtsi_.getDTSI(); }
    public double getDTSICount() { return dtsi_.getCount(); }
    public double getPMI() { return pmi_.getPMI(); }
    public double getPMICount() { return pmi_.getCount(); }
    public double getPMSI() { return pmsi_.getPMSI(); }
    public double getPMSICount() { return pmsi_.getCount(); }
    public int getTkCScore() { return tkc_; }
    public void printTPPolarInfo(String topic) {
	ArrayList counts = polarities_.get(topic);
	//System.out.println(name_ + "\t|" + counts.get(0) + "\t|" + counts.get(1) + "\t|" + polas_dis_.get(topic));
    }
    public double getLeadership() { return leadership_.getScore(); }
    public double getLeadershipR() { return leadership_.getScoreR(); }
    public Leadership getLeadershipInfo() { return leadership_; }
    public String getLeadershipRInfo() { return leadership_.toStringR(); }
    
    public int getTPCRank() { return tpc_rank; }
    public int getTKCRank() { return tkc_rank; }
    public int getEXDRank() { return exd_rank; }
    public int getINVRank() { return inv_rank; }
    public boolean rankFirst() {
	if (tpc_rank == 1 || 
	    tkc_rank == 1 ||
	    exd_rank == 1 ||
	    inv_rank == 1) {
	    return true;
	}
	return false;
    }
    public double getATX() { return agr_.getATX(); } //get Agreement
    public boolean isLocalTopic(String word, int count) { return ilts_.isLocalTopic(word, count); }
    public double getDisagreement() { return disagreement_; }

    /*******************************set information**************************/
    public void addILT(LocalTopic lt) {
	if (!ilts_.contains(lt)) {
	    ilts_.add(lt);
	}
    }
    public void addCLT(LocalTopic lt) {
	if (!clts_.contains(lt)) {
	    clts_.add(lt);
	}
    }
    public void addAILT(LocalTopic lt) {
	if (!ilts_.contains(lt)) {
	    ilts_.add(lt);
	}
    }
    public void addACLT(LocalTopic lt) {
	if (!clts_.contains(lt)) {
	    clts_.add(lt);
	}
    }
    public void setAllUtts(ArrayList utts) { all_utts_ = utts; } //1/26/2011
    public void addUtterance(Utterance utt) {utts_.add(utt);}
    public void addNoun(NounToken nt) { if (!nls_.contains(nt)) nls_.add(nt); }
    public void calTpCtl(LocalTopics lcs_,
			 ArrayList all_utts) {
	tc_.calTpCtl(lcs_, all_utts);
	leadership_.setTopicControl(tc_.getPower()); //modi 03/15 by TL
	//System.out.println(name_ + "'s size of turns: " + utts_.size());
    }

    public void calInv(LocalTopics lcs_,
		       ArrayList all_utts,
		       NounList nls,
		       ArrayList top10nouns) {
	inv_.calInvolvement(lcs_, all_utts, nls, top10nouns);
	leadership_.setInvolvement(inv_.getPower());
	//System.out.println(name_ + "'s size of turns: " + utts_.size());
    }
    public void calAInv(LocalTopics lcs_,
			ArrayList all_utts,
			ArrayList nls,
			ArrayList top10nouns,
			ArrayList spks) {
	inv_.calAInvolvement(lcs_, all_utts, nls, top10nouns, spks);
	leadership_.setInvolvement(inv_.getPower());
	//System.out.println(name_ + "'s size of turns: " + utts_.size());
    }
    public void collTPPolarities(ArrayList topics) {
	//System.out.println("utts size: " + utts_.size());
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt = (Utterance)utts_.get(i);
	    String pola = null;
	    if (Settings.getValue(Settings.PROCESS_TYPE).equals("annotated")) {
		pola = utt.getPolarity(); //from human annotated
	    } else if (Settings.getValue(Settings.PROCESS_TYPE).equals("automated")) {
		pola = utt.getSysPolarity(); //from system extracted
	    }
	    //System.out.println("pola: " + pola);
	    if (pola == null ||
		pola.trim().length() == 0) {
		pola = Opinion.NEUTRAL;
	    }
	    String topic = utt.getTopic();
	    if (!topics.contains(topic)) {topics.add(topic);}
	    ArrayList counts = (ArrayList)polarities_.get(topic);
	    if (counts == null) {
		counts = new ArrayList();
		counts.add(new Integer(1)); //positive
		counts.add(new Integer(1)); //negative
		polarities_.put(topic, counts);
	    }
	    if (pola.equals(Opinion.POSITIVE)) {
		Integer count = (Integer)counts.get(0);
		count++;
		counts.set(0, count);
	    } else if (pola.equals(Opinion.NEGATIVE)) {
		Integer count = (Integer)counts.get(1);
		count++;
		counts.set(1, count);
	    }
	}
	//System.out.println("polarities: " + polarities_);
    }

    public void setQTScore(String tp, 
			   int score,
			   String pola) {
	ArrayList counts = (ArrayList)qt_polarities_.get(tp);
	if (counts == null) {
	    counts = new ArrayList();
	    counts.add(new Integer(0)); //positive
	    counts.add(new Integer(0)); //negative
	    qt_polarities_.put(tp, counts);
	}
	if (pola.equals(Opinion.POSITIVE)) {
	    Integer count = new Integer(score);
	    counts.set(0, count);
	} else if (pola.equals(Opinion.NEGATIVE)) {
	    Integer count = new Integer(score);
	    //System.out.println("negative score: " + count);
	    counts.set(1, count);
	}
    }

    public void setPCScore(String tp, 
			   double score,
			   String pola) {
	ArrayList counts = (ArrayList)pc_polarities_.get(tp);
	if (counts == null) {
	    counts = new ArrayList();
	    counts.add(new Double(1)); //positive
	    counts.add(new Double(1)); //negative
	    pc_polarities_.put(tp, counts);
	}
	if (pola.equals(Opinion.POSITIVE)) {
	    Double count = new Double(score);
	    counts.set(0, count);
	} else if (pola.equals(Opinion.NEGATIVE)) {
	    Double count = new Double(score);
	    //System.out.println("negative score: " + count);
	    counts.set(1, count);
	}
    }

    public void calQTDis(ArrayList topics) {
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    calQTDis(topic);
	}
    }
    public void calQTDis(String tp) {
	ArrayList counts = (ArrayList)qt_polarities_.get(tp);
	if (counts == null) return;
	Integer p = (Integer)counts.get(0);
	Integer n = (Integer)counts.get(1);
	qt_polas_dis_.put(tp, p - n);
    }

    public void calPCDis(ArrayList topics) {
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    calPCDis(topic);
	}
    }
    public void calPCDis(String tp) {
	ArrayList counts = (ArrayList)pc_polarities_.get(tp);
	if (counts == null) return;
	Double p = (Double)counts.get(0);
	Double n = (Double)counts.get(1);
	pc_polas_dis_.put(tp, p - n);
    }

    public void calTPDis(ArrayList topics) {
	for (int i = 0; i < topics.size(); i++) {
	    String topic = (String)topics.get(i);
	    calTPDis(topic);
	}
    }
    public void calTPDis(String tp) {
	//System.out.println("tp: " + tp);
	ArrayList counts = (ArrayList)polarities_.get(tp);
	if (counts == null) {/*System.out.println("counts is null!!!");*/ return;}
	Integer p = (Integer)counts.get(0);
	Integer n = (Integer)counts.get(1);
	polas_dis_.put(tp, p - n);
    }

    public void calDI(ArrayList utts,
		      double total_ac_oc) {
	//all_utts_ = utts; 1/26/2011
	di_.initialize(total_ac_oc, all_utts_);
	di_.calDI();
	leadership_.setTaskControl(di_.getDI());
    }
    
    public void calDTSI(ArrayList utts,
			double total_ac_oc) {
	//all_utts_ = utts;  1/26/2011
	dtsi_.initialize(total_ac_oc, all_utts_);
	dtsi_.calDTSI();
    }
    
    public void calPMI(ArrayList utts,
		       double total_ac_oc) {
	//all_utts_ = utts;  1/26/2011
	pmi_.initialize(total_ac_oc, all_utts_);
	pmi_.calPMI();
    }
    
    public void calPMSI(ArrayList utts,
			double total_ac_oc) {
	//all_utts_ = utts;  1/26/2011
	pmsi_.initialize(total_ac_oc, all_utts_);
	pmsi_.calPMSI();
    }

    public void calATX() {
	agr_.calATX(utts_, all_utts_);
    }

    public void calLeadership() {
	leadership_.calculate();
    }
    
    public void calLeadershipR() {
	leadership_.calculateR();
    }
    
    public void setDisagreement(double disagreement) { disagreement_ = disagreement; leadership_.setDisagreement(disagreement);}
    
    public void setTkCScore(int tkc) { tkc_ = tkc; }
    
    public void setTpQScore(int tpqs, String ind_type) { tc_.setQScore(tpqs, ind_type); }
		      
    public void setInvQScore(int invqs, String ind_type) { inv_.setQScore(invqs, ind_type); }
    
    public void setTPCRank(int rank) { 
	tpc_rank = rank; /*if (rank == 1)*/ 
	if (rank == 3) {
	    ArrayList spks = new ArrayList(parts_.values());
	    double diff_from_first = -1;
	    double diff_from_third = -1;
	    double first = 0;
	    double second = 0;
	    Speaker spk2 = null;
	    for (int i = 0; i < spks.size(); i++) {
		Speaker spk = (Speaker)spks.get(i);
		if (spk.equals(this)) continue;
		if (spk.getTPCRank() == 1) {
		    first = spk.getTC().getPower();
		} else if (spk.getTPCRank() == 2){
		    second = spk.getTC().getPower();
		    spk2 = spk;
		}
	    }
	    diff_from_first = first - second;
	    diff_from_third = second - tc_.getPower();
	    //System.out.println("diff_from_first tpc: " + diff_from_first);
	    //System.out.println("diff_from_third tpc: " + diff_from_third);
	    if (diff_from_first < diff_from_third) {
		spk2.getLeadershipInfo().setTopicControl2R(2);
	    }
	} else {
	    leadership_.setTopicControlR(rank);
	}
    }
    public void setTKCRank(int rank) { 
	tkc_rank = rank; /*if (rank == 1)*/ 
	if (rank == 3) {
	    ArrayList spks = new ArrayList(parts_.values());
	    double diff_from_first = -1;
	    double diff_from_third = -1;
	    double first = 0;
	    double second = 0;
	    Speaker spk2 = null;
	    for (int i = 0; i < spks.size(); i++) {
		Speaker spk = (Speaker)spks.get(i);
		if (spk.equals(this)) continue;
		if (spk.getTKCRank() == 1) {
		    first = spk.getDI(); 
		} else if (spk.getTKCRank() == 2) {
		    second = spk.getDI();
		    spk2 = spk;
		}
	    }
	    diff_from_first = first - second;
	    diff_from_third = second - getDI(); 
	    if (diff_from_first < diff_from_third) {
		leadership_.setTaskControl2R(2);
	    }
	}else {
	    leadership_.setTaskControlR(rank);
	}
    }
    public void setEXDRank(int rank) { 
	exd_rank = rank; /*if (rank == 1)*/ 
	if (rank == 2) {
	    ArrayList spks = new ArrayList(parts_.values());
	    double diff_from_first = -1;
	    double diff_from_third = -1;
	    for (int i = 0; i < spks.size(); i++) {
		Speaker spk = (Speaker)spks.get(i);
		if (spk.equals(this)) continue;
		if (spk.getEXDRank() == 1) {
		    diff_from_first = spk.getDisagreement() - getDisagreement();
		} else if (spk.getEXDRank() == 3) {
		    diff_from_third = getDisagreement() - spk.getDisagreement();
		}
	    }
	    if (diff_from_first < diff_from_third) {
		leadership_.setDisagreement2R(rank);
	    }
	} else {
	    leadership_.setDisagreementR(rank);
	}
    }
    public void setINVRank(int rank) { 
	inv_rank = rank; /*if (rank == 1)*/ 
	if (rank == 2) {
	    ArrayList spks = new ArrayList(parts_.values());
	    double diff_from_first = -1;
	    double diff_from_third = -1;
	    for (int i = 0; i < spks.size(); i++) {
		Speaker spk = (Speaker)spks.get(i);
		if (spk.equals(this)) continue;
		if (spk.getINVRank() == 1) {
		    diff_from_first = spk.getInv().getPower() - getInv().getPower();
		} else if (spk.getINVRank() == 3) {
		    diff_from_third =  getInv().getPower() - spk.getInv().getPower();
		}
	    }
	    if (diff_from_first < diff_from_third) {
		leadership_.setInvolvement2R(rank);
	    }
	} else {
	    leadership_.setInvolvementR(rank);
	}
    }
    public double getLeadershipConfidence() {
	double confidence = 0;
	if (tpc_rank == 1 &&
	    leadership_.getTopicControlR() == 1.0) { //ME type
	    confidence += 0.7;
	    //task control
	    if (tkc_rank == 1 &&
		leadership_.getTaskControlR() > 0) {
		confidence += 0.15;
	    } else if (tkc_rank == 2 &&
		leadership_.getTaskControlR() > 0) {
		confidence += 0.075;
	    }
	    //involvement
	    if (inv_rank == 1 &&
		leadership_.getInvolvementR() > 0) {
		confidence += 0.05;
	    } else if (inv_rank == 2 &&
		leadership_.getInvolvementR() > 0) {
		confidence += 0.025;
	    }
	    //disagreement
	    if (exd_rank == 1 &&
		leadership_.getDisagreementR() > 0) {
		confidence += 0.10;
	    } else if (exd_rank == 2 &&
		leadership_.getDisagreementR() > 0) {
		confidence += 0.05;
	    }
	} else if (tkc_rank == 1 &&
		   leadership_.getTaskControlR() == 1.3) { //QA type
	    confidence += 0.7;
	    //topic control
	    if (tpc_rank == 1 &&
		leadership_.getTopicControlR() > 0) {
		confidence += 0.15;
	    } else if (tpc_rank == 2 &&
		leadership_.getTopicControlR() > 0) {
		confidence += 0.075;
	    }
	    //involvement
	    if (inv_rank == 1 &&
		leadership_.getInvolvementR() > 0) {
		confidence += 0.05;
	    } else if (inv_rank == 2 &&
		leadership_.getInvolvementR() > 0) {
		confidence += 0.025;
	    }
	    //disagreement
	    if (exd_rank == 1 &&
		leadership_.getDisagreementR() > 0) {
		confidence += 0.10;
	    } else if (exd_rank == 2 &&
		leadership_.getDisagreementR() > 0) {
		confidence += 0.05;
	    }
	} else if (tkc_rank == 1 &&
		   leadership_.getTaskControlR() == 0.8) { //ME type
	    confidence += 0.6;
	    //topic control
	    if (tpc_rank == 1 &&
		leadership_.getTopicControlR() > 0) {
		confidence += 0.25;
	    } else if (tpc_rank == 2 &&
		leadership_.getTopicControlR() > 0) {
		confidence += 0.125;
	    }
	    //involvement
	    if (inv_rank == 1 &&
		leadership_.getInvolvementR() > 0) {
		confidence += 0.05;
	    } else if (inv_rank == 2 &&
		leadership_.getInvolvementR() > 0) {
		confidence += 0.025;
	    }
	    //disagreement
	    if (exd_rank == 1 &&
		leadership_.getDisagreementR() > 0) {
		confidence += 0.10;
	    } else if (exd_rank == 2 &&
		leadership_.getDisagreementR() > 0) {
		confidence += 0.05;
	    }
	} else { //no rank 1 on neither tpc and tkc
	    confidence += 0;
	    //topic control
	    if (tkc_rank == 2 &&
		leadership_.getTaskControlR() > 0) {
		confidence += 0.075;
	    } 
	    if (tkc_rank == 1 &&
		leadership_.getTaskControlR() > 0) {
		confidence += 0.15;
	    } 
	    if (tpc_rank == 2 &&
		leadership_.getTopicControlR() > 0) {
		confidence += 0.35;
	    }
	    if (tpc_rank == 1 &&
		leadership_.getTopicControlR() > 0) {
		confidence += 0.15;
	    }
	    //involvement
	    if (inv_rank == 1 &&
		leadership_.getInvolvementR() > 0) {
		confidence += 0.05;
	    } else if (inv_rank == 2 &&
		leadership_.getInvolvementR() > 0) {
		confidence += 0.025;
	    }
	    //disagreement
	    if (exd_rank == 1 &&
		leadership_.getDisagreementR() > 0) {
		confidence += 0.10;
	    } else if (exd_rank == 2 &&
		leadership_.getDisagreementR() > 0) {
		confidence += 0.05;
	    }
	}
	return confidence;
    }
    /*
    public void setTPCRank(int rank) { tpc_rank = rank; if (rank == 1) leadership_.setTopicControlR(rank);}
    public void setTKCRank(int rank) { tkc_rank = rank; if (rank == 1) leadership_.setTaskControlR(rank);}
    public void setEXDRank(int rank) { exd_rank = rank; if (rank == 1) leadership_.setDisagreementR(rank);}
    public void setINVRank(int rank) { inv_rank = rank; if (rank == 1) leadership_.setInvolvementR(rank);}
    /*
    public void setTPCRank(int rank) { tpc_rank = rank; }
    public void setTKCRank(int rank) { tkc_rank = rank; }
    public void setEXDRank(int rank) { exd_rank = rank; }
    public void setINVRank(int rank) { inv_rank = rank; }
    */
    public void setSpeakers (HashMap parts) {
	parts_ = parts;
    }
    
    public void setDPM (double dpm) { if (DPM < dpm) DPM = dpm; }
    public double getDPM () { return DPM; }
    public void setPRM (double prm) { if (PRM < prm) PRM = prm; /*System.out.println("prm: " + PRM);*/}
    public double getPRM () { return PRM; }

    /*******************************   Attributes  **************************/
    private String name_ = null;
    private ArrayList<Utterance> utts_ = new ArrayList<Utterance>(); //list of Utterances by this speaker
    private ArrayList<Utterance> all_utts_ = new ArrayList<Utterance>(); //list of Utterances of the whole chat
    private LocalTopics ilts_ = new LocalTopics(); //list of introduced LocalTopics
    private LocalTopics clts_ = new LocalTopics();//list of cite LocalTopics
    private TopicControl tc_ = new TopicControl(this);
    private DirectiveIndex di_ = new DirectiveIndex(this); //directive index
    private DirectedTopicShiftIndex dtsi_ = new DirectedTopicShiftIndex(this); //directive index
    private Agreement agr_ = new Agreement(this); //Agreement
    private ProcessManagementIndex pmi_ = new ProcessManagementIndex(this); //directive index
    private ProcessManagementSuccessIndex pmsi_ = new ProcessManagementSuccessIndex(this); //directive index
    private double disagreement_ = 0;
    private int tkc_ = 0;
    private Involvement inv_ = new Involvement(this);
    private ArrayList nls_ = new ArrayList(); //all mentioned nouns by this speaker
    private HashMap<String, ArrayList> polarities_ = new HashMap<String, ArrayList>(); //value is ArrayList of Integer, p, n
    private HashMap<String, Integer> polas_dis_ = new HashMap<String, Integer>(); //value is Integer, p-n
    private HashMap qt_polarities_ = new HashMap(); //value is ArrayList of Integer, p, n
    private HashMap qt_polas_dis_ = new HashMap(); //value is Integer, p-n
    private HashMap pc_polarities_ = new HashMap(); //value is ArrayList of Integer, p, n
    private HashMap pc_polas_dis_ = new HashMap(); //value is Integer, p-n
    private Leadership leadership_ = new Leadership();
    private HashMap<String, Speaker> parts_ = null;
    private int tpc_rank = -1;
    private int tkc_rank = -1;
    private int exd_rank = -1;
    private int inv_rank = -1;

    private double DPM = 0;
    private double PRM = 0;
}
