package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 02/08/2010
 * This class is used to hold a list of local topics
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

public class LocalTopics extends ArrayList{
    public void LocalTopics() {
    }

    /*******************************get information**************************/
    public int sizeOfMentions() {
	int num_of_ments = 0;
	for (int i = 0; i < size(); i++) {
	    LocalTopic lt = (LocalTopic)get(i);
	    num_of_ments += lt.getMentions().size() - 1;
	}
	return num_of_ments;
    }

    public int filteredSize() {
	int size = 0;
	for (int i = 0; i < size(); i++) {
	    LocalTopic lt = (LocalTopic)get(i);
	    NounToken nt = lt.getContent();
	    String spk = lt.getIntroducer();
	    if (Util.filterIt(spk, nt)) {
		continue;
	    }
	    size++;
	}
	return size;
    }

    public boolean isLocalTopic(String word,
			  int count) {
	for (int i = 0; i < size(); i++) {
	    LocalTopic lt = (LocalTopic)get(i);
	    NounToken nt = lt.getContent();
	    if (nt.getWord().equalsIgnoreCase(word) &&
		lt.getMentions().size() >= count) {
		//System.out.println(word + " appears " + lt.getMentions().size() + " times");
		//System.out.println("noneToken: \n" + lt.getContent());
		return true;
	    }
	}
	return false;
    }

    public int sizeOfMentions(String spk) {
	//added at 03/15  by TL
	int num_of_ments = 0;
	for (int i = 0; i < size(); i++) {
	    LocalTopic lt = (LocalTopic)get(i);
	    NounToken nt = lt.getContent();
	    if (lt.sizeofCO() < 15) {
		if (Util.filterIt(spk, nt)) continue;
	    }
	    num_of_ments += lt.getMentions().size() - 1;
	}
	return num_of_ments;
    }

    public int sizeOfCO() {
	int num_of_co = 0;
	for (int i = 0; i < size(); i++) {
	    LocalTopic lt = (LocalTopic)get(i);
	    num_of_co += lt.sizeofCO();
	}
	return num_of_co;
    }


    /*******************************set information**************************/
    public LocalTopic add(NounToken nt,
			  String spk) {
	if (nt.firstMention()) {
	    if (spk.equals("lauren") &&
		(nt.getWord().equals("b") ||
		 nt.getWord().equals("water"))) {
		return null;
	    }
	    /*
	    if (spk.equalsIgnoreCase("carol")) {
		System.out.println("add new lc to carol: " + nt.getWord());
	    }
	    if (spk.equalsIgnoreCase("ashley")) {
		System.out.println("add new lc to ashley: " + nt.getWord());
	    }
	    if (spk.equalsIgnoreCase("jessicad")) {
		System.out.println("add new lc to jessicad: " + nt.getWord());
	    }
	    */
	    LocalTopic lt = new LocalTopic();
	    lt.setContent(nt);
	    lt.setIntroducer(spk);
	    lt.addMention(nt);
	    add(lt);
	    return lt;
	} else {
	    for (int i = 0; i < size(); i++) {
		LocalTopic lt = (LocalTopic)get(i);
		NounToken lt_nt = lt.getContent();
		if (nt.getRefInt() == lt_nt.getID()) {
		    lt.addMention(nt);
		    return lt;
		}
	    }
	}
	return null;
    }
    
    public LocalTopic addA(NounToken nt,
			  String spk) {
	if (nt.firstAMention()) {
	    LocalTopic lt = new LocalTopic();
	    lt.setContent(nt);
	    lt.setIntroducer(spk);
	    lt.addMention(nt);
	    add(lt);
	    return lt;
	} else {
	    //System.out.println("not first mention");
	    for (int i = 0; i < size(); i++) {
		LocalTopic lt = (LocalTopic)get(i);
		NounToken lt_nt = lt.getContent();
		if (nt.getRef().equals(lt_nt.getId())) {
		    lt.addMention(nt);
		    return lt;
		}
	    }
	}
	return null;
    }
    

    /*******************************   Attributes  **************************/
}
