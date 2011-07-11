package edu.albany.ils.dsarmd0200.util;

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.evaltag.*;

/**
 * this file is used to parse document in text format.
 * @author Ting Liu
 */

public class DocumentProcessor {
    public DocumentProcessor() {
    }

    public static ArrayList parseUtts(File doc) {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(doc));
	    String a_line = null;
	    ArrayList utts = new ArrayList();
	    int count = 1;
	    while ((a_line = br.readLine()) != null) {
		if (a_line.trim().length() == 0) continue;
		String[] items = a_line.split("\\: ");
		if (items.length != 2) { /*System.out.println("dialogue format is wrong: " + a_line);*/ continue;}
		Utterance utt = new Utterance();
		String[] time_spk = items[0].split("\\) ");
		if (time_spk.length > 1) {
		    utt.setTime(time_spk[0].replace("\\(", ""));
		    utt.setSpeaker(time_spk[1]);
		    utt.setContent(items[1]);
		    utt.setTurn((new Integer(count)).toString());
		}else {
		    time_spk = items[0].split("\\(");
		    utt.setTime(time_spk[1].split(" ")[0]);
		    utt.setSpeaker(time_spk[0].split(" ")[1]);
		    utt.setContent(items[1]);
		    utt.setTurn((new Integer(count)).toString());
		}
		//System.out.println("utt content: " + utt.getContent());
		//System.out.println("utt speaker: " + utt.getSpeaker());
		count++;
		utts.add(utt);
	    }
	    return utts;
	}catch (IOException ioe) {
	    ioe.printStackTrace();
	}
	return null;
    }
}
