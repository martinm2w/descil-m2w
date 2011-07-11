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

public class DirectiveIndex {
    public DirectiveIndex(Speaker spk) {
	spk_ = spk;
    }

    /*********************************get attributes**********************/
    public double getDI() { return di_; }
    public double getCount() { return ac_oc_; }

    /*********************************set attributes**********************/
    public void initialize(double total_ac_oc,
			   ArrayList utts) {
	total_ac_oc_ = total_ac_oc;
	utts_ = utts;
    }
    public void calDI() {
	ac_oc_ = 0;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    if (utt_.getSpeaker().equals(spk_.getName())) {
		if (utt_.getTag().equalsIgnoreCase("action-directive") ||
		    utt_.getTag().equalsIgnoreCase("offer-commit") ||
		    (utt_.getTag().indexOf("Action-Directive") != -1) ||
		    (utt_.getTag().indexOf("Offer-Commit") != -1) ||
		    utt_.getTag().equalsIgnoreCase("information-request") ||
		    (utt_.getTag().indexOf("Information-Request") != -1) ||
		    utt_.getMTag().equalsIgnoreCase("TASK-MGMT") ||
		    utt_.getMTag().equalsIgnoreCase("PRCS-MGMT")) {
		    if (utt_.getTag().equalsIgnoreCase("information-request") ||
			utt_.getTag().equalsIgnoreCase("confirmation-request") ||
			(utt_.getTag().indexOf("Information-Request") != -1) ||
			(utt_.getTag().indexOf("Confirmation-Request") != -1)) {
			if (hasResponse(utt_, utts_)) {
			    if (Assertions.ddt_.getType().equals(Assertions.ddt_.QA)) {
				ac_oc_ += 0.2;
			    }else {
				ac_oc_ += 1;
			    }
			} else {
			    //ac_oc_--;
			}
		    } else {
			ac_oc_ += 1; 
		    }
		}
	    }
	}
	if (total_ac_oc_ != 0) 
	    di_ = ((double)ac_oc_)/total_ac_oc_;
	else di_ = -1;
	//System.out.println("The di of " + spk_.getName() + " is: " + di_);
    }
    
    public boolean hasResponse(Utterance utt_, ArrayList utts_) {
	String turn_no = utt_.getSpeaker() + ":" + utt_.getTurn();
	//System.out.println("turn_no: " + turn_no);
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt = (Utterance)utts_.get(i);
	    if (utt.getRespTo().equals(turn_no) &&
		!utt.getSpeaker().equals(utt_.getSpeaker())) {
		//System.out.println("Has response: " + utt + "\n" + utt_);
		return true;
	    }
	}
	return false;
    }

    /*******************************   Attributes  **************************/
    Speaker spk_ = null; //the person who has the index
    double di_ = 0; 
    double total_ac_oc_;
    double ac_oc_ = 0;
    ArrayList utts_ = null;
}
