package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 03/09/2010
 * This class is used to judge which particant in a 
 * small discussion group has the most power defining 
 * the goal and leading to the goal
 */

import java.io.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import java.util.*;

public class ProcessManagementSuccessIndex {
    public ProcessManagementSuccessIndex(Speaker spk) {
	spk_ = spk;
    }

    /*********************************get attributes**********************/
    public double getPMSI() { return pmsi_; }
    public double getCount() { return ac_oc_; }

    /*********************************set attributes**********************/
    public void initialize(double total_ac_oc,
			   ArrayList utts) {
	total_ac_oc_ = total_ac_oc;
	utts_ = utts;
    }
    public void calPMSI() {
	ac_oc_ = 0;
	ArrayList pm_utts = new ArrayList();
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    if (utt_.getSpeaker().equals(spk_.getName()) &&
		utt_.getMTag() != null) {
		if ((utt_.getMTag().equalsIgnoreCase("TASK-MGMT") ||
		     utt_.getMTag().equalsIgnoreCase("PRCS-MGMT")) &&
		    (utt_.getTag().equalsIgnoreCase("Assertion-Opinion") ||
		     utt_.getTag().equalsIgnoreCase("Action-Directive") ||
		     utt_.getTag().equalsIgnoreCase("Offer-Commit"))) {
		    ac_oc_ += 1; 
		    pm_utts.add(utt_);
		}
	    }
	    if (utt_.getMTag() != null) {
		if ((utt_.getMTag().equalsIgnoreCase("TASK-MGMT") ||
		     utt_.getMTag().equalsIgnoreCase("PRCS-MGMT"))) {
		    String resp = utt_.getRespTo();
		    if (resp != null) {
			String[] resps = resp.split(":");
			if (resps.length > 0) {
			    boolean found = false;
			    for (int j = pm_utts.size() - 1; j >= 0; j--) {
				Utterance pm_utt = (Utterance)pm_utts.get(j);
				if (pm_utt.getTurn().equalsIgnoreCase(resp)) {
				    if ((utt_.getTag().equalsIgnoreCase("Agree-Accept") ||
					 utt_.getTag().equalsIgnoreCase("Acknowledge"))) {
					ac_oc_ += 1;
				    } else if (utt_.getTag().equalsIgnoreCase("Disagree-Reject")) {
					ac_oc_ -= 1;
				    }
				    break;
				}
			    }
			}
		    }
		}
	    }
	}
	pmsi_ = ((double)ac_oc_)/total_ac_oc_;
	System.out.println("The di of " + spk_.getName() + " is: " + pmsi_);
    }

    /*******************************   Attributes  **************************/
    Speaker spk_ = null; //the person who has the index
    double pmsi_ = 0; 
    double total_ac_oc_;
    double ac_oc_ = 0;
    ArrayList utts_ = null;
}
