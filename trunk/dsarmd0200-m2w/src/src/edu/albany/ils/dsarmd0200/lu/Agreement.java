package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 01/25/2011
 * This class is used to judge which particants in a 
 * small discussion group agrees to each other
 */

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
import edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash.*;


public class Agreement {

    public Agreement(Speaker spk) {
	parent_ = spk;
    }

    /***********************get information*******************/
    public double getATX() {return atx_;}
    public int getATXCount() { return atx_count_; }
    public double getTAX() {return tax_;}
    public int getTAXCount() { return tax_count_; }


    /***********************set information*******************/
    public void calATX(ArrayList utts,
		       ArrayList all_utts) {
	atx_count_ = calATXCount(utts);
	all_atx_count_ = calATXCount(all_utts);
	if (all_atx_count_ != 0) {
	    atx_ = ((double)atx_count_/all_atx_count_);
	}
    }

    public int calATXCount(ArrayList utts) {
	int atx_count_ = 0;
	int atx_pl_ps_count = 0;
	int atx_tag_ag_count = 0;
	for (int i = 0; i < utts.size(); i++) {
	    Utterance utt_ = (Utterance)utts.get(i);
	    String cur_spk = utt_.getSpeaker();
	    if (utt_.getTag().equalsIgnoreCase(DATagger.AGREE_ACCEPT) ||
		(utt_.getTag().toLowerCase().indexOf(DATagger.NAGREE_ACCEPT1) != -1) ||
		(utt_.getTag().toLowerCase().indexOf(DATagger.NAGREE_ACCEPT2) != -1)/* ||
										       utt_.getPolarity().equalsIgnoreCase("positive")*/) {
		/*
		if (utt_.getPolarity().equalsIgnoreCase("positive")) {
		    atx_pl_ps_count++;
		}
		*/
		if (utt_.getTag().equalsIgnoreCase(DATagger.AGREE_ACCEPT) ||
		    (utt_.getTag().toLowerCase().indexOf(DATagger.NAGREE_ACCEPT1) != -1) ||
		    (utt_.getTag().toLowerCase().indexOf(DATagger.NAGREE_ACCEPT2) != -1)) {
		    atx_tag_ag_count++;
		}
		atx_count_++;
	    }
	}
	System.out.println("atx_pl_ps_count: " + atx_pl_ps_count);
	System.out.println("atx_tag_ag_count: " + atx_tag_ag_count);
	System.out.println("atx_overall_count: " + atx_count_);
	return atx_count_;
    }

    /***********************  Attributes   *******************/
    private double atx_ = 0; //Agree-Accept Index 
    private int atx_count_ = 0; 
    private int all_atx_count_ = 0;
    private double tax_ = 0; //Topical Agreement Index 
    private int tax_count_ = 0;
    private Speaker parent_ = null;
}
