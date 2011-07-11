package edu.albany.ils.dsarmd0200.evaltag;

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import edu.albany.ils.dsarmd0200.util.xml.*;

/**
 * The tagger for data.
 * @author Ting
 * */
public abstract class Tagger {
    //
    //fields
    //
    //constants
    
    //total number of dialog acts
    public int total;
    
    //total number of correctly tagged dialog acts
    public int totalCorrect;
    
    //total number of dialog acts that have no mappings to swbd standard
    public int totalNoMapping;
    
    //total number of dialog acts that have no tags
    public int totalNoTag;
    
    //total number of dialog acts that are too long to tag
    public int totalTooLong;
    
    //the swbd dialog act tagger
    public DATagger tagger;
    
    //the hash table storing the frequencies of high-level amities tag
    public HashMap map = new HashMap();
    
    //the hash table storing the correctly tagged counts of amities tag
    public HashMap cmap = new HashMap();
    
    //correct mapping, Hashmap of Hashmap of Integer
    public HashMap c_amit_swbd = new HashMap(); 
    public HashMap c_crp1_crp2 = new HashMap(); 
    //wrong mapping, Hashmap of Hashmap of Integer
    public HashMap w_amit_swbd = new HashMap(); 
    public HashMap w_crp1_crp2 = new HashMap(); 

    //count amities tag frequency
    public HashMap amit_t = new HashMap(); 

    public boolean skip_sw_ = true; //whether to skip the stop word filtering
    
    public HashMap tags_eval_ = new HashMap(); //key is tag name and values is arraylist of three integers, correct extracted, total extracted, and total exist,
    /**
     * Initialization method.
     * */
    public abstract void init();
    
    /**
     * Tag the file.
     * */

    public abstract void tagFile(File file);
    
    
    public void startTagging(String path) {

	File dir = new File(path);
	
	
	System.out.println("Start");
	System.out.println("processs file: " + path);
	tagFile(dir);
    }

    public void printTagResult() {
	
	System.out.println();
	System.out.println("Results");
	System.out.println("Total dialog acts: " + total);
	//System.out.println("Total dialog acts having no swbd tag: " + totalNoMapping);
	//System.out.println("Total dialog acts too long: " + totalTooLong);
	System.out.println("Total dialog acts having no tags: " + totalNoTag);
	System.out.println("Total dialog acts having no mapping: " + totalNoMapping);
	System.out.println("Total correct tagged dialog acts: " + totalCorrect);
	System.out.println("Accuracy: " + (double)totalCorrect / total);
	System.out.println();
	Iterator iterator = map.keySet().iterator();
	//		System.out.println("Tag\t\t\tFrequency\t\t\tPercentage\t\t\tCorrect");
	ArrayList tags_b_c = new ArrayList();
	ArrayList tags_b_c_c = new ArrayList();
	ArrayList tags_b_p = new ArrayList();
	ArrayList tags_b_p_c = new ArrayList();
	double mf_p = 0;
	int mf_c = 0;
	String mf_tag = null;
	int mcf_c = 0;
	String mcf_tag = null;
	System.out.println("eval of each tagger");
	while(iterator.hasNext()) {
	    String tag = (String)iterator.next();
	    Integer freq = (Integer)map.get(tag);
	    Integer correct = (Integer)cmap.get(tag);
	    if (correct == null) {
		correct = new Integer(0);
	    }
	    
	    if (freq.intValue() > mf_c) {
		mf_tag = tag;
		mf_c = freq.intValue();
	    }
	    System.out.println("*********************************************************************************");
	    System.out.println("Tag: " + tag + "\nFrequency: " + freq + "\nPercentage of Freq (based on total): " + (double)freq / total
			       + "\nCorrect: " + ((correct == null) ? 0 : correct));
	    
	    //based on number
	    boolean added = false;
	    for (int i = 0; i < tags_b_c_c.size(); i++) {
		Integer e_correct = (Integer)tags_b_c_c.get(i);
		if (correct.compareTo(e_correct) > 0) {
		    tags_b_c_c.add(i, correct);
		    tags_b_c.add(i, tag);
		    added = true;
		    break;
		}
	    }
	    if (!added) {
		tags_b_c_c.add(correct);
		tags_b_c.add(tag);
	    }
	    
	    //based on percentage
	    added = false;
	    for (int i = 0; i < tags_b_p_c.size(); i++) {
		Double e_correct = (Double)tags_b_p_c.get(i);
		Double p_correct = new Double((double)correct / freq);
		if (p_correct.compareTo(e_correct) > 0) {
		    tags_b_p_c.add(i, p_correct);
		    tags_b_p.add(i, tag);
		    added = true;
		    break;
		}
	    }
	    if (!added) {
		tags_b_p_c.add(new Double((double)correct/freq));
		tags_b_p.add(tag);
	    }
	    
	}

	System.out.println("*********************************************************************************");
	System.out.println("most frequent tag: " + mf_tag);
	System.out.println("percentage: " + (double)mf_c/total);
	System.out.println("number of occurance: " + mf_c);
	
	System.out.println("*********************************************************************************");
	String most_c_t = (String)tags_b_c.get(0);
	Integer most_c_t_c = (Integer)tags_b_c_c.get(0);
	System.out.println("right most (tag): " + most_c_t);
	System.out.println("right most (number): " + most_c_t_c);
	
	System.out.println("*********************************************************************************");
	most_c_t = (String)tags_b_p.get(0);
	Double most_c_t_p = (Double)tags_b_p_c.get(0);
	System.out.println("right most (tag): " + most_c_t);
	System.out.println("right most (percetage): " + most_c_t_p);

	
	System.out.println("*********************************************************************************");
	String most_w_t = (String)tags_b_c.get(tags_b_c.size() - 1);
	Integer most_w_t_c = (Integer)map.get(most_w_t) - (Integer)tags_b_c_c.get(tags_b_c.size() - 1);
	System.out.println("wrong most (tag): " + most_w_t);
	System.out.println("wrong most (number): " + most_w_t_c);
	
	System.out.println("*********************************************************************************");
	most_w_t = (String)tags_b_p.get(tags_b_p.size() - 1);
	Double most_w_t_p = 1 - (Double)tags_b_p_c.get(tags_b_p.size() - 1);
	System.out.println("wrong most (tag): " + most_w_t);
	System.out.println("wrong most (percentage): " + most_w_t_p);
    }

}
