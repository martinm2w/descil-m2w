package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 03/19/2010
 * This class is used to judge which particant in a 
 * small discussion group has the most power defining 
 * the goal and leading to the goal
 */

import java.io.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import java.util.*;

public class ProcessManagementIndex {
    public ProcessManagementIndex(Speaker spk) {
	spk_ = spk;
    }

    /*********************************get attributes**********************/
    public double getPMI() { return pmi_; }
    public double getCount() { return ac_oc_; }

    /*********************************set attributes**********************/
    public void initialize(double total_ac_oc,
			   ArrayList utts) {
	total_ac_oc_ = total_ac_oc;
	utts_ = utts;
    }
    public void calPMI() {
	ac_oc_ = 0;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    if (utt_.getSpeaker().equals(spk_.getName()) &&
		utt_.getMTag() != null) {
		if (utt_.getMTag().equalsIgnoreCase("TASK-MGMT") ||
		    utt_.getMTag().equalsIgnoreCase("PRCS-MGMT")) {
		    ac_oc_ += 1; 
		}
	    }
	}
	pmi_ = ((double)ac_oc_)/total_ac_oc_;
	System.out.println("The pmi of " + spk_.getName() + " is: " + pmi_);
    }

    /*******************************   Attributes  **************************/
    Speaker spk_ = null; //the person who has the index
    double pmi_ = 0; 
    double total_ac_oc_;
    double ac_oc_ = 0;
    ArrayList utts_ = null;
}
