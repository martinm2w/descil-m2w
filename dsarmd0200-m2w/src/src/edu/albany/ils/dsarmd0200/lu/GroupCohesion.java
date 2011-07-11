package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 2/20/2010
 * This class is used to analyze the cohesion of the group discussion
 */

import java.io.*;
import java.util.*;

public class GroupCohesion {

    /*******************************get information**************************/
    

    /*******************************set information**************************/
    public void setTaskFocus(TaskFocus tkf) { tkf_ = tkf; }
    public void setSpeakers(HashMap spks) { spks_ = spks; }
    public void setMesoTopics(ArrayList mts) { mts_ = mts; }
    public void setSociability(Sociability sb) { sb_ = sb; }
    public void calMSO() {
	if (tkf_ != null) {
	    mso_score_ = (tkf_.getMSM() + tkf_.getMGM())/2;
	}
	System.out.println("@MSO: " + mso_score_); 
	//System.out.println("@MSO: " + mso_); 
    }

    public void calSpksADM() {
	spks_adm_score_ = sb_.getSpksADMeasure();
	System.out.println("@ADM: " + spks_adm_score_);
    }

    public void calSBM() {
	System.out.println(sb_);
    }

    public void calculate(int fn) {
	double ov_thr = 0;
	double total = 5;
	//System.out.println("processing Group Cohesion...");
	calMSO();
	calSpksADM();
	calSBM();
	//System.out.println("\n\nProcessing Degree of Participation Measure...");
	dpm = new DPM (spks_);
	dpm.calDPM ();
	dpm_score_ = dpm.getDPM();
        //System.out.println("\n\nProcessing Persistence of Roles Measure...");
	prm = new PRM (spks_, mts_);
	if (mts_.size () >= 1) {
	    prm.calPRM (fn);
	}
	prm_score_ = prm.getPRM();
	sm_score_ = sb_.getScore();
	if (mso_score_ >= mso_thr_) ov_thr += 1;
	if (sm_score_ >= sm_thr_) ov_thr += 1;
	if (spks_adm_score_ >= spks_adm_thr_) ov_thr += 1;
	if (dpm_score_ >= dpm_thr_) ov_thr += 1;
	if (prm_score_ >= prm_thr_) ov_thr += 1;
	double confidence = ov_thr/total;
	if (confidence == 0) System.out.println("@Cohesion: notCohesive");
	else if (confidence < 0.6) System.out.println("@Cohesion: mixedIndicators \n@Confidence: \n" + confidence);
	else System.out.println("@Cohesion: cohesive \n@Confidence: \n" + confidence);
    }
    /*******************************  Attributes   **************************/
    private TaskFocus tkf_ = null;
    private Sociability sb_ = null;
    private HashMap<String, Speaker> spks_ = null;
    private ArrayList<MesoTopic> mts_ = null;
    private double mso_score_ = -1; //Measure of Shared Objective 
    private double spks_adm_score_ = -1; //Agreement Disagreement Measure on speakers
    private double sm_score_ = -1;
    private double dpm_score_ = -1;
    private double prm_score_ = -1;
    private DPM dpm = null;
    private PRM prm = null;
    private final static double mso_thr_ = 0.31;
    private final static double spks_adm_thr_ = 0.6;
    private final static double sm_thr_ = 0.69;
    private final static double dpm_thr_ = 0.70;
    private final static double prm_thr_ = 0.75;
    
    
}
