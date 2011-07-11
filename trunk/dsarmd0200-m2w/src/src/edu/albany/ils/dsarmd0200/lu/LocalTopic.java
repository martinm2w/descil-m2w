package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 02/08/2010
 * This class is used to hold the information of each local topic
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

public class LocalTopic {
    public void LocalTopic() {
    }

    /*******************************get information**************************/
    public ArrayList<NounToken> getMentions() { return mentions_; }
    public NounToken getContent() {return content_; }
    public String getIntroducer() { return intro_; }
    public ArrayList getCited(String spk) {
	/*
	if (intro_.equals(spk)) {
	    return null;
	}
	*/
	ArrayList cites = new ArrayList();
	for (int i = 0; i < mentions_.size(); i++) {
	    NounToken mention = mentions_.get(i);
	    if (!mention.getSpeaker().equals(spk)) {
		cites.add(mention);
	    }
	}
	//if (cites.size() > 5) return 1;
	//return 0;
	return cites;
    }

    //return the number of mentions from other speaker other than introducers.
    public int sizeofCO() {
	int cites = 0;
	for (int i = 0; i < mentions_.size(); i++) {
	    NounToken mention = mentions_.get(i);
	    if (!mention.getSpeaker().equals(intro_)) {
		cites++;
	    }
	}
	//modified by Ting 01/07/2011
	/*
	if (cites > 5) return 1;
	return 0;
	*/
	return cites;
    }

    //return the size of citting by a specific speaker
    public int sizeofCO(String spk) {
	int cites = 0;
	if (spk.equals(intro_)) return cites;
	//System.out.println("sizeof mentions: " + mentions_.size());
	for (int i = 0; i < mentions_.size(); i++) {
	    NounToken mention = mentions_.get(i);
	    if (mention.getSpeaker().equals(spk)) {
		cites++;
	    }
	}
	//01/07/2011
	if (cites > 5) return 1;
	return 0;
	//	return cites;
    }

    public ArrayList getMentions(String spk,
				 boolean include) {
	ArrayList mentions = new ArrayList();
	for (int i = 0; i < mentions_.size(); i++) {
	    if (i == 0 &&
		!include) { continue; }
	    NounToken mention = mentions_.get(i);
	    if (mention.getSpeaker().equals(spk)) {
		mentions.add(mention);
	    }
	}
	return mentions;
    }

    public String toString() {
	StringBuffer out = new StringBuffer();
	out.append(content_.toString());
	return out.toString();
    }


    /*******************************set information**************************/
    public void addMention(NounToken nt) { mentions_.add(nt); }
    public void setContent(NounToken content) { content_ = content; }
    public void setIntroducer(String intro) { intro_ = intro; }


    /*******************************   Attributes  **************************/
    private NounToken content_ = null; //first appearance
    private String intro_ = null; //who introduced it
    private ArrayList<NounToken> mentions_ = new ArrayList<NounToken>(); //all appearance of this local topic
}
