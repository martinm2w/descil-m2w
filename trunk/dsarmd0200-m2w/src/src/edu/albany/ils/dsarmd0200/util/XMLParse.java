package edu.albany.ils.dsarmd0200.util;

import edu.albany.ils.dsarmd0200.evaltag.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;


/**
 * This class is aim to parse the XML file
 * Author: Ken Stahl
 */
public class XMLParse {
	String dialogDate = "";
	String fileName;
	ArrayList <String> utterances;
	ArrayList <String> speakers;
	ArrayList <String> timestamps;
	ArrayList <String> turn_no;
	boolean commExists;
	ArrayList <String> comm_acts;
	ArrayList <String> commactlinks;
	ArrayList <String> pos;
	ArrayList <String> pos_origin;
	PhraseCheck pc;

	//added arraylist to hold values for outputting auto-annotation
	//ArrayList <String> raw_lines;

	public XMLParse(String filename, ArrayList utts_){
		fileName = filename;
		utterances = new ArrayList<String>();
		speakers = new ArrayList<String>();
		timestamps = new ArrayList<String>();
		turn_no = new ArrayList<String>();
		commExists = false;
		comm_acts = new ArrayList<String>();
		commactlinks = new ArrayList<String>();
		pos = new ArrayList<String>();
		pos_origin = new ArrayList<String>();
		//raw_lines = new ArrayList<String>();
		
		for(int index=0; index<utts_.size(); index++)
		{
		    Utterance utterance = (Utterance) utts_.get(index);
			utterances.add(utterance.getUtterance());
			speakers.add(utterance.getSpeaker());
			//timestamps.add(utterance.getTime());
			turn_no.add(utterance.getTurn());
			comm_acts.add(utterance.getCommActType());
			commactlinks.add(utterance.getRespTo());
			pos.add(utterance.getPOS());
			pos_origin.add(utterance.getPOSORIGIN());			
		}

/*		
		File tmpFile = new File(filename);
		try{
			BufferedReader br = new BufferedReader(new FileReader(tmpFile));
			String dummy;
			dummy = br.readLine(); raw_lines.add(dummy);
			//retrieve date
			int dateStart = dummy.indexOf("date=\"");
			if (dateStart != -1){
				dateStart += 6;
				for (int j = dateStart; j < dummy.length() && dummy.charAt(j) != '"'; j++)
					dialogDate += dummy.charAt(j);
			}
			String newL = br.readLine(); raw_lines.add(newL);
			newL = newL.trim();
			commExists = (newL.indexOf("comm_act_type=") != -1);
			processLine(newL);
			while (br.ready()){
				String newLine = br.readLine(); raw_lines.add(newLine);
				newLine = newLine.trim();
				//if (!newLine.contains("</turn>"))
				//	System.out.println(newLine);
				
				// retrieve turn_no
				if (newLine.length() > 0 && newLine.indexOf("</Dialogue>") == -1){
					int turnnoplace = newLine.indexOf("turn_no=\"") + 9;
					String tmpturnno = "";
					for (int i = turnnoplace; i < newLine.length() && newLine.charAt(i) != '"'; i++)
						tmpturnno += newLine.charAt(i);
					// deal with float turn numbers, merge two utterances where the later one is beyond X.0
					if (tmpturnno.contains(".")){
						if (tmpturnno.charAt(tmpturnno.indexOf(".") + 1) != '0'){
							for (int i = turnnoplace; i < newLine.length() && newLine.charAt(i) != '>'; i++)
								turnnoplace++; turnnoplace++;
							System.out.println("turnnoplace: " + turnnoplace);
							int turnend = newLine.indexOf("</turn>");
							System.out.println("turnend: " + turnend);
							if (turnend < 0) System.exit(0);
							if (turnend < 0) turnend = newLine.length() - 1;
							if (turnnoplace >= newLine.length()) turnnoplace = newLine.length() - 7;
							// get utterances
							String ut = newLine.substring(turnnoplace, turnend);
							String newut = utterances.get(utterances.size() - 1);
							newut += " " + ut;
							utterances.set(utterances.size() - 1, newut);
							//System.out.println(utterances.get(utterances.size() - 1));
						} else {
							String tmpval = "";
							int place = newLine.indexOf("turn_no=\"") + 9;
							for (int z = place; z<newLine.length() && newLine.charAt(z)!='.'; z++)
								tmpval += newLine.charAt(z);
							String replaceVal="";
							for (int z = place; z < newLine.length() && newLine.charAt(z) != '"';z++)
								replaceVal += newLine.charAt(z);
							newLine = newLine.replace(replaceVal,tmpval);
							processLine(newLine);
						}
					}else
						processLine(newLine);
				}
			}
			System.out.println("# of utterances: " + utterances.size());
		} catch (Exception e){
			e.printStackTrace();
		}
*/
	}
    public String getFileName(){
	return fileName;
    }
/*
	public String getRawLine(int place){
		return raw_lines.get(place);
	}

	public int rawSize(){
		return raw_lines.size();
	}
*/

	public int getTurnCount(){
		return utterances.size();
	}
/*
	public String getDialogDate(){
		return dialogDate;
	}
*/

	public ArrayList<String> getTurnNo(){
		return turn_no;
	}

	public ArrayList<String> getTimeStamps(){
		return timestamps;
	}

	public ArrayList<String> getPOS(){
		return pos;
	}

	public ArrayList<String> getPOSOrigin(){
		return pos_origin;
	}

	public ArrayList<String> getSpeakers(){
		return speakers;
	}

	public ArrayList<String> getUtterances(){
		return utterances;
	}

	public ArrayList<String> getCommActs(){
		return comm_acts;
	}

	public ArrayList<String> getCommActLinks(){
		return commactlinks;
	}
	
	/*
	private void processLine(String param){
		// retrieve turn no
		int turnnoloc = param.indexOf("turn_no=\"") + 9;
		String tnno = "";
		for (int i = turnnoloc; i < param.length() && param.charAt(i) !='"'; i++)
			tnno += param.charAt(i);
		turn_no.add(tnno);
		// retrieve pos
		int posloc = param.indexOf("pos=\"") + 5;
		String posVal = "";
		for (int i = posloc; i < param.length() && param.charAt(i) != '"'; i++)
			posVal += param.charAt(i);
		//retrieve pos origin
		int posorig = param.indexOf("pos_origin=\"") + 12;
		String posO = "";
		for (int i = posorig; i < param.length() && param.charAt(i) != '"'; i++)
			posO += param.charAt(i);
		pos_origin.add(posO);
		// retrieve speaker 
		int speakloc = param.indexOf("speaker=\"") + 9;
		String speakerName = "";
		String timeVal = "";
		String uttVal = "";
		for (int i = speakloc; i < param.length() && param.charAt(i) != '"'; i++)
			speakerName += param.charAt(i);
		//retrieve end time
		int timeloc = param.indexOf("end_time=") + 10;
		for (int i = timeloc; i < param.length() && param.charAt(i) != '"'; i++)
			timeVal += param.charAt(i);
		int uttloc = timeloc;
		for (int i = timeloc; i < param.length() && param.charAt(i) != '>'; i++)
			uttloc++; uttloc++;
		int uttend = param.indexOf("</turn>");
		if (uttloc > uttend){
			System.out.println("uttloc = " + uttloc + " and uttend = " + uttend);
			System.out.println(param);
		}
		if (uttend != -1 && uttloc > -1){
			//System.out.println("uttloc = " + uttloc + " and uttend = " + uttend);
			uttVal = param.substring(uttloc, uttend);
			uttVal.replace("&lt;", "<");
			uttVal.replace("&gt;", ">");
		} else uttVal = "";
		pos.add(posVal);
		speakers.add(speakerName);
		timestamps.add(timeVal);
		utterances.add(uttVal);
		if (commExists){
			String commVal = "";
			// retrieve comm_act_type
			int commStart = param.indexOf("comm_act_type=") + 15;
			for (int i = commStart; i < param.length() && param.charAt(i) != '"'; i++)
				commVal += param.charAt(i);
			comm_acts.add(commVal);
			// retrieve link_to
			int linktoStart = param.indexOf("link_to=\"") + 9;
			String linkTo = "";
			for (int i = linktoStart; i < param.length() && param.charAt(i) != '"'; i++){
				linkTo += param.charAt(i);
			}
			commactlinks.add(linkTo);
		}
	}
	*/

    public void setCommActLinks(ArrayList comm){
	commactlinks.clear();
	commactlinks.addAll(comm);
    }
	
}
