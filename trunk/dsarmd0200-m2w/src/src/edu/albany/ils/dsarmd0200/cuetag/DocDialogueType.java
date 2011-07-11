package edu.albany.ils.dsarmd0200.cuetag;

import edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash.DsarmdDATag;
import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import java.util.*;

/**
 * Check the dialogue type of this document: question-like or meetings
 * Author: Ting Liu
 */

public class DocDialogueType {
    public DocDialogueType(ArrayList utts) {
	utts_ = utts;
    }

    /***************************get information*********************/
    public String getType() { 
	if (ddt_ == null) {
	    decideDDT();
	}
	return ddt_; 
    }
    /***************************set information*********************/
    public void decideDDT() {
	int size_of_ircr = 0;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = utts_.get(i);
	    if (utt_.getTag().equals(DsarmdDATag.CR) ||
		utt_.getTag().equals(DsarmdDATag.IR)/* ||
						       utt_.getContent().indexOf("?") != -1*/) {
		size_of_ircr ++;
	    }
	}
	double perOfIRCR = (double)size_of_ircr/utts_.size();
	System.out.println("size of ircr: " + size_of_ircr);
	System.out.println("percentage of ircr: " + perOfIRCR);
	if (perOfIRCR >= 0.25) {
	    ddt_ = QA;
	} else { ddt_ = ME; }
    }
    /***********************Attributes*********************/
    private ArrayList<Utterance> utts_ = null;
    private String ddt_ = null; //either QA or ME
    public final static String QA = "question-like";
    public final static String ME = "meetings";
    
}
