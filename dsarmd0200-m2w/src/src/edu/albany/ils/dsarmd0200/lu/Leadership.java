package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 12/28/2010
 * This class is used to find the leader of a group
 */

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
//import edu.albany.ils.dsarmd0200.ml.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class Leadership {

    /******************************get infromation*******************/
    public double getTopicControl() { return topic_control_; }
    public double getTaskControl() { return task_control_; }
    public double getInvolvement() { return involvement_; }
    public double getDisagreement() { return disagreement_; }
    public double getScore() { return leadership_; }
    public double getTopicControlR() { return topic_control_r_; }
    public double getTaskControlR() { return task_control_r_; }
    public double getInvolvementR() { return involvement_r_; }
    public double getDisagreementR() { return disagreement_r_; }
    public double getScoreR() { return leadership_r_; }
    public String toString() {
	StringBuffer out = new StringBuffer();
	out.append("topic control: " + topic_control_ + "\n" + 
		   "task control: " + task_control_ + "\n" + 
		   "involvement: " + involvement_ + "\n" + 
		   "disagreement: " + disagreement_ + "\n" + 
		   "itcm: " + itcm_ + "\n" + 
		   "cdm: " + cdm_ + "\n" + 
		   "leadership: " + leadership_);
	return out.toString();
    }

    public String toStringR() {
	StringBuffer out = new StringBuffer();
	out.append(//"topic control: " + topic_control_r_ + "\n" + 
		   //"task control: " + task_control_r_ + "\n" + 
		   //"involvement: " + involvement_r_ + "\n" + 
		   //"disagreement: " + disagreement_r_ + "\n" + 
		   //"itcm: " + itcm_ + "\n" + 
		   //"cdm: " + cdm_ + "\n" + 
		   leadership_r_);
	return out.toString();
    }

    /******************************get infromation*******************/
    public void setTopicControl(double topic_control) { topic_control_ = topic_control; /*System.out.println("set topic control: " + topic_control);*/}
    public void setTaskControl(double task_control) { task_control_ = task_control; }
    public void setInvolvement(double involvement) { involvement_ = involvement; }
    public void setDisagreement(double disagreement) { disagreement_ = disagreement; }
    public void setTopicControlR(int rank) { 
	if (rank == 1) {
	    if (Assertions.ddt_.getType().equals(Assertions.ddt_.ME)) {
		topic_control_r_ += /*0.4*/ 1.0; itcm_ = 1.0; cdm_ = 1.0;
	    } else {
		topic_control_r_ += 0.5; //meeting
	    }
	}
    }
    public void setTaskControlR(int rank) { 
	if (rank == 1) {
	    if (Assertions.ddt_.getType().equals(Assertions.ddt_.ME)) {
		task_control_r_ += /*0.32*/ 0.8;
	    } else {
		task_control_r_ += 1.3;
	    }
	} 
    }
    public void setInvolvementR(int rank) { 
	if (rank == 1) {
	    involvement_r_ += /*0.12*/ 0.3;
	    itcm_ += 0.3;
	    cdm_ += 0.3;
	    //if (topic_control_r_ == 1.0) {ictm_ = 1.0;}
	} 
    }
    public void setDisagreementR(int rank) { 
	if (rank == 1) {
	    disagreement_r_ += /*0.16*/ 0.4;
	    //if (topic_control_r_ == 1 or ictm_ == 1) {
	    cdm_ += 0.4;
	    //}
	} 
    }
    public void setTopicControl2R(int rank) { 
	if (rank == 2) {
	    if (Assertions.ddt_.getType().equals(Assertions.ddt_.ME)) {
		topic_control_r_ += 1.0/2; 
	    } else {
		topic_control_r_ += 0.5/2; 
	    }
	}//half of the first
    }
    public void setTaskControl2R(int rank) { 
	if (rank == 2) {
	    if (Assertions.ddt_.getType().equals(Assertions.ddt_.ME)) {
		task_control_r_ += 0.8/2; 
	    } else {
		task_control_r_ += 1.3/2; 
	    }
	} //half of the first
    }
    public void setInvolvement2R(int rank) { 
	if (rank == 2) {involvement_r_ += 0.3/2;} //half of the first
    }
    public void setDisagreement2R(int rank) { 
	if (rank == 2) {disagreement_r_ += 0.4/2; } //half of the first
    }
    public void calculate() {
	//modified 03/03/2011
	int count = 0;
	double total = 0;
	if (topic_control_ != -1) {
	    total += topic_control_ * 0.45;
	    count++;
	}
	if (task_control_ != -1) {
	    total += task_control_ * 0.40;
	    count++;
	}
	if (involvement_ != -1) {
	    total += involvement_ * 0.05;
	    count++;
	}
	if (disagreement_ != -1) {
	    total += disagreement_ * 0.1;
	    count++;
	}
	leadership_ = total/count;
    }

    public void calculateR() {
	leadership_r_ = topic_control_r_ + task_control_r_ + involvement_r_ + disagreement_r_;
    }

    /******************************Attributes*******************/
    private double topic_control_ = -1;
    private double task_control_ = -1;
    private double involvement_  = -1;
    private double disagreement_ = -1;
    private double topic_control_r_ = 0; //rank in topic control
    private double task_control_r_ = 0;  
    private double involvement_r_  = 0;
    private double disagreement_r_ = 0;
    private double itcm_ = 0;
    private double cdm_ = 0;
    private double leadership_;
    private double leadership_r_;
}
