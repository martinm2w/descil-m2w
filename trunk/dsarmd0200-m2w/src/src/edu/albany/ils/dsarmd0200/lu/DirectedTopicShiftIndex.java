package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 07/12/2010
 * This class is used to judge which particant in a 
 * small discussion group has the most power defining 
 * the goal and leading to the goal
 */

import java.io.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import java.util.*;

public class DirectedTopicShiftIndex {
    public DirectedTopicShiftIndex(Speaker spk) {
	spk_ = spk;
    }

    /*********************************get attributes**********************/
    public double getDTSI() { return dtsi_; }
    public double getCount() { return ac_oc_; }

    /*********************************set attributes**********************/
    public void initialize(double total_ac_oc,
			   ArrayList utts) {
	total_ac_oc_ = total_ac_oc;
	utts_ = utts;
    }

    public void calDTSI() {
	ac_oc_ = 0;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    if (utt_.getSpeaker().equals(spk_.getName())) {
		if (utt_.getTag().equalsIgnoreCase("action-directive") ||
		    utt_.getTag().equalsIgnoreCase("offer-commit") ||
		    (utt_.getTag().indexOf("Action-Directive") != -1) ||
		    (utt_.getTag().indexOf("Offer-Commit") != -1)) {
		    String topic = utt_.getTopic();
		    if (topic == null) continue;
		    int count = 0;
		    boolean shifted = false;
		    int same_topic = 0;
		    for (int j = i + 1; j < utts_.size() && count < 10; j++, count++) {
			Utterance fl_utt_ = (Utterance)utts_.get(j);
			String fl_topic = fl_utt_.getTopic();
			if (fl_topic != null &&
			    fl_topic.equals(topic)) {
			    same_topic++;
			}
		    }
		    if (same_topic * 2 >= count) {
			ac_oc_ += 1; 
		    }
		}
	    }
	}
	dtsi_ = ((double)ac_oc_)/total_ac_oc_;
	System.out.println("The di of " + spk_.getName() + " is: " + dtsi_);
    }


    /*******************************   Attributes  **************************/
    Speaker spk_ = null; //the person who has the index
    double dtsi_ = 0; 
    double total_ac_oc_;
    double ac_oc_ = 0;
    ArrayList utts_ = null;
}
