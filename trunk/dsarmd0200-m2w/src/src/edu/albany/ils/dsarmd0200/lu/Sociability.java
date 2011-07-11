package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 2/22/2010
 * This class is used to analyze the cohesion of the group discussion
 */

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.util.NounList;

public class Sociability {

    public Sociability(ArrayList<Utterance> utts,
		       HashMap spks/*,
				     NounList nls*/) {
	utts_ = utts;
	spks_ = spks;
	cnm_ = new ConversationalNormsMeasure(utts_);
	adm_ = new AgreementDisagreementMeasure(utts_, spks);
	ndi_ = new NetworkDensityIndex(utts_, spks);
	cdi_ = new CiteDisparityIndex(utts_, spks);
    }

    /*******************************get information**************************/
    public double getSpksADMeasure() { 
	if (spks_adm_ == -1) calSpksADMeasure(); 
	return spks_adm_; 
    }
    public String toString() {
	StringBuffer out = new StringBuffer();
	//out.append("***************Socialbility output*********\n");
	//out.append("Conversational Norms Measure: " + cnm_val_);
	//out.append("\nNetwork Density Index Mean = " + ndi_.getNDIMean());
	//out.append("\nNetwork Density Index Standard Deviation = " + ndi_.getNDIStandardDeviation());
	//out.append("\nCalibrating Network Density Index Standard Deviation = " + ndi_.getCNDI());
	//out.append("@ADM: " + adm_val_);
	out.append("@SM: " + overal_);
	//out.append("\nCited Disparity Index: " + cdi_.getCDI());
	return out.toString();
    }
    public double getScore() { return overal_; }

    /*******************************set information**************************/
    public void calCNM() { cnm_val_ = cnm_.getCNM(); /*System.out.println("conversational Norms Measure: " + cnm_val_);*/}
    public void calSpksADMeasure() {
	spks_adm_ = adm_.calSpksADMeasure();
	//System.out.println("@ADM: " + spks_adm_);
    }
    public void calADMeasure() {
	//calculate the overall Agreement Disagreement measure
	adm_val_ = adm_.getADM();
    }
    public void calNDI() {
	ndi_.calNDI();
	ndi_mean_ = ndi_.getNDIMean();
        //System.out.println("NDI Mean = " + ndi_.getNDIMean());
	ndi_sd_ = ndi_.getNDIStandardDeviation();
        //System.out.println("NDI Standard Deviation = " + ndi_.getNDIStandardDeviation());
	ndi_.calCNDI();
    }

    public void calCDI() {
	cdi_.calCDI();
	cdi_val_ = cdi_.getCDI();
	//System.out.println("CDI: " + cdi_.getCDI());
    }
    
    public void calculate() {
	calCNM();
	calADMeasure();
	calNDI();
	calCDI();
	overal_ = (cnm_val_ + adm_val_ + cdi_val_)/3; 
    }

    /*******************************  Attributes   **************************/
    private ArrayList<Utterance> utts_ = null;
    private ConversationalNormsMeasure cnm_ = null; //Conversational Norms Measure 
    private AgreementDisagreementMeasure adm_ = null; //Agreement Disagreement Measure 
    private NetworkDensityIndex ndi_ = null; //network density index
    private CiteDisparityIndex cdi_ = null; //Cited Disparity index
    private double overal_ = 0;
    private double cnm_val_ = -1;
    private double adm_val_ = -1;
    private double ndi_mean_ = -1;
    private double ndi_sd_ = -1;
    private double cdi_val_ = -1;
    private double spks_adm_ = -1;
    private HashMap spks_ = null; 
}
