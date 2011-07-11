package edu.albany.ils.dsarmd0200.util;

import java.util.ArrayList;

// Author: Ken Stahl

//This class compares automatic and human annotated summaries.
public class ListCompare {
	private XMLParse xp;
	//PRONOUN SECTION
	private ArrayList <String> automated = new ArrayList<String>();
	private ArrayList <String> autoTurnNo = new ArrayList<String>();
	private ArrayList <String> autoTarget = new ArrayList<String>();
	private ArrayList <String> autoTargetTurnNo = new ArrayList<String>();

	private ArrayList <String> annotated = new ArrayList<String>();
	private ArrayList <String> annoTurnNo = new ArrayList<String>();
	private ArrayList <String> annoTarget = new ArrayList<String>();
	private ArrayList <String> annoTargetTurnNo = new ArrayList<String>();
	//END PRONOUN SECTION

	private NounList listofNouns;
	private ArrayList <String> pos = new ArrayList<String>();
	private ArrayList <String> uniqueTopics = new ArrayList<String>();
	private ArrayList <AChain> chains = new ArrayList<AChain>();

	private int hits = 0;
	private int misses = 0;
	private int falsealarms = 0;

	public ListCompare(NounList param, XMLParse xparam){
		xp = xparam;
		// get an arraylist of pronouns
		ArrayList <NounToken> temp = param.getPronouns();
		pos = xp.getPOS();
		listofNouns = param;
		chains = new ArrayList<AChain>();
		for (int i = 0; i < temp.size(); i++)
		{
			NounToken tmp = temp.get(i);
			automated.add(tmp.getWord());
			autoTurnNo.add(Integer.toString(tmp.getTurnNo()));
			//System.out.print(tmp.getWord() + ", ID= " + tmp.getTurnNo());
			//System.out.println(", REF INT = " + tmp.getRefInt());
			if(tmp.getRefInt()!=-1)		
			{
				NounToken tmpTar = param.getTokenWithID(tmp.getRefInt());
				autoTarget.add(tmpTar.getWord());
				autoTargetTurnNo.add(Integer.toString(tmpTar.getTurnNo()));
			}
		}
		//createAnnoList();
		createUniqueTopics();
		makeChains();
	}

	public int maxTurnNo(){
		int retval = 0;
		String x = autoTurnNo.get(autoTurnNo.size() - 1);
		String y = annoTurnNo.get(annoTurnNo.size() - 1);
		if (x.indexOf(".")!= -1)
			x = x.substring(0, x.indexOf("."));
		if (y.indexOf(".") != -1)
			y = y.substring(0, y.indexOf("."));
		int xx = Integer.parseInt(x);
		int yy = Integer.parseInt(y);
		retval = (yy > xx) ? yy : xx;
		return retval;
	}

	public ArrayList <String> getAutomatedAt(int intParam){
		ArrayList <String> retval = new ArrayList<String>();
		for (int i = 0; i < autoTurnNo.size(); i++){
			String tmp = autoTurnNo.get(i);
			if (tmp.indexOf(".") != -1)
				tmp = tmp.substring(0, tmp.indexOf("."));
			double x = Double.parseDouble(tmp);
			if (x == intParam)
				retval.add(automated.get(i));
		}
		return retval;
	}

	public ArrayList <String> getAnnotatedAt(int intParam){
		ArrayList <String> retval = new ArrayList<String>();
		for (int i = 0; i < annoTurnNo.size(); i++){
			String tmp = annoTurnNo.get(i);
			if (tmp.indexOf(".") != -1)
				tmp = tmp.substring(0, tmp.indexOf("."));
			double x = Double.parseDouble(tmp);
			if (x == intParam)
				retval.add(annotated.get(i));
		}
		return retval;
	}

    public ArrayList <String> parsePOS(String param, int intParam){
		//this was written before repetition was implemented so the name is deceiving
		//it records annotated pronoun resolutions
		ArrayList <String> retval = new ArrayList<String>();
		if (param.indexOf("-pronoun-") != -1){
			String [] tokens = param.split(";");
			for (int i = 0; i < tokens.length; i++){
				int pronounLoc = tokens[i].indexOf("-pronoun-");
				if (pronounLoc != -1){
					int place = tokens[i].indexOf("(");
					// get start place
					int closeplace = place;
					for (; tokens[i].charAt(closeplace) != ')';)
						closeplace++;
					String refLoc = tokens[i].substring(place + 1, closeplace);
					String word = tokens[i].substring(0, place);
					place = pronounLoc + 19;
					String ref = "";
					for (int j = 0; j < (tokens[i].length() - place) && tokens[i].charAt(place + j) != '('; j++)
						ref += tokens[i].charAt(place + j);
					annotated.add(word);
					annoTurnNo.add(Integer.toString(intParam));
					annoTarget.add(ref);
					annoTargetTurnNo.add(refLoc);
					System.out.println(word + ";" + intParam + ";" + ref + ";" + refLoc);
				}
			}
		}
		return retval;
	}

	public void printAutoChain(){
		ArrayList <AChain> nounchains = listofNouns.getChains();
		int size = nounchains.size();
		int [] indices = new int[size];
		for (int i = 0; i < size; i++){
			indices[i] = i;
		}
		for (int j = 0; j < size - 1; j++){
				int tmp = nounchains.get(indices[j]).size();
			for (int k = j + 1; k < size; k++){
				int tmp2 = nounchains.get(indices[k]).size();
				if (tmp < tmp2){
					int swp = indices[k];
					indices[k] = indices[j];
					indices[j] = swp;
					swp = tmp;
					tmp = tmp2;
					tmp2 = swp;
				}
			}
		}
		for (int i = 0; i < size; i++){
			AChain tmp = nounchains.get(indices[i]);
			System.out.println("=======Chain #" + i + "=======");
			for (int j = 0; j < tmp.size(); j++){
				System.out.println(tmp.getValue(j) + ": " + tmp.getValLocation(j) + " and target = " + tmp.getTargetLoc(j));
			}
		}
	}

	public void printAnnoChain(){
		int size = chains.size();

		for (int i = 0; i < size; i++){
			AChain tmp = chains.get(i);
			System.out.println("=======Chain #" + i + "=======");
			for (int j = 0; j < tmp.size(); j++){
				System.out.println(tmp.getValue(j) + ": " + tmp.getValLocation(j) + " and target = " + tmp.getTargetLoc(j));
			}
		}
	}

	public void createAnnoList()
	{
		ArrayList <String> temp = xp.getPOS();
		for (int i = 0; i < temp.size(); i++)
		{
			String tmpPos = temp.get(i);
			if (tmpPos.contains("-pronoun-"))
			{
				// get POS which has -pronoun- and parse it
				// getPronouns acturally has nothing, this function updates the 4 annotated arrays
				ArrayList <String> getPronouns = parsePOS(tmpPos, i + 1);
			}
		}
	}

	public int getLoc(String param){
	    int retval = -1;
		int intstart = param.indexOf("(");
		int intfinish = intstart; String intstr = "";
		for (int i = 0; i < param.length() && param.charAt(i) != ')'; i++)
			intstr += param.charAt(i);
		retval = Integer.parseInt(intstr);
		return retval;
	}

	//this creates an arraylist of AChains for comparison and output
	public void makeChains(){
		//make unique topics first
		ArrayList <String> poses = xp.getPOS();
		for (int i = 0; i < uniqueTopics.size(); i++){
			String temp = uniqueTopics.get(i);
			// get topic words
			String word = temp.substring(0, temp.indexOf("("));
			int plcstart = temp.indexOf("(");
			String plc = "";
			for (int z = plcstart + 1; z < temp.length() && temp.charAt(z) != ')' &&
					temp.charAt(z) != '.'; z++)
				plc += temp.charAt(z);
			// get topics turn number and convert it to integer
			int plcval = Integer.parseInt(plc);
			System.out.println(temp);
			AChain ac = new AChain();
			ac.add(word, plcval, "", -1);
			for (int j = 0; j < poses.size(); j++){
				String tmp = poses.get(j);
				String [] splitpos = tmp.split(";");
				for (int z = 1; z < splitpos.length; z++){
					if (splitpos[z].contains(temp)){
						System.out.println("\t"+(j+1)+":" + splitpos[z]);
						int wordend = splitpos[z].indexOf("(");
						String word2 = splitpos[z].substring(0, wordend);
						ac.add(word2, (j + 1), word, plcval);
					}
				}
			}
			chains.add(ac);
		}
	}

	// this reads POSorigin and create uniqueTopics arraylist with non-repetive topics
	public void createUniqueTopics()
	{
		ArrayList <String> temp = xp.getPOSOrigin();
		for (int i = 0; i < temp.size(); i++)
		{
			String tmp = temp.get(i);
			String [] tmpSplit = tmp.split(";");
			for (int j = 1; j < tmpSplit.length; j++){
				String tempstr = tmpSplit[j];
				//System.out.println("TEMPSTR=" + tempstr);
				if (!uniqueTopics.contains(tempstr)){
					uniqueTopics.add(tempstr);
				}
			}
		}
	}

	public void tryComparison(){
		ArrayList <AChain> autochain = listofNouns.getChains();
		double total; double count = chains.size(); double tally = 0.0;
		for (int i = 0; i < count; i++)
		{
			AChain temp = chains.get(i);
			//temp.printOut();
			int cloc = temp.location();
			total = 0.0;
			for (int j = 0; j < autochain.size(); j++){
				double placeholder = 0.0;
				AChain temp2 = autochain.get(j);
				int ploc = temp2.location();
				if (cloc == ploc){
					if (temp.match(temp2.getValue(0))){
						placeholder = temp.matchVal(temp2);
						if (placeholder > total) total = placeholder;
						System.out.println("#" + i + ": " + placeholder);
					}
				}
			}
			tally += total;
		}
		System.out.println("Tally = " + tally + " and Count = " + count);
		System.out.println("Overall, chain precision = " + (tally / count));
		System.out.println("SIZE OF ANNOTATED = " + chains.size());
	}

	public int annoChainSize(){
		int retval = 0;
		for (int i = 0; i < annoTargetTurnNo.size(); i++){
			String temp = annoTargetTurnNo.get(i);
			System.out.println("=============" + temp);
			if (temp.equalsIgnoreCase("-1"))
				retval++;
		}
		return retval;
	}

	public ArrayList<String> getUniqueTopics(){
		return uniqueTopics;
	}

	public int uniqueTopicCount(){
		return uniqueTopics.size();
	}

	public void printAnnotatedUniqueTopics(){
		for (int i = 0; i < uniqueTopics.size(); i++)
			System.out.println(uniqueTopics.get(i));
	}

	public void printLists(){
		int turnCount = xp.getTurnCount();
		System.out.println("==Automated==");
		for (int i = 0; i < automated.size(); i++){
			String tmp = autoTurnNo.get(i);
			System.out.print(automated.get(i) + ": TurnNo = " + tmp);
			String att = autoTargetTurnNo.get(i);
			System.out.println(", Target = " + autoTarget.get(i) + ", Target turn No = " + att);
		}
		int runningCount = 0;
		System.out.println("==Annotated==");
		for (int i = 0; i < turnCount; i++){
			String turnno = "0";
			if (runningCount < annoTurnNo.size())
				turnno = annoTurnNo.get(runningCount);
			int turnval = Integer.parseInt(turnno);
			if (turnval == i){
				while ((runningCount < annoTurnNo.size()) && turnval == i){
					String annotgt = annoTarget.get(runningCount);
					String wrd = annotated.get(runningCount);
					String antgtloc = annoTargetTurnNo.get(runningCount++);
					if (runningCount < annoTurnNo.size()){
						turnno = annoTurnNo.get(runningCount);
						turnval = Integer.parseInt(turnno);
					} else {
						turnval = i - 1;
					}
					System.out.println("TurnNo = " + (i + 1) + " Pronoun = " + wrd + ", " + "Target = " + annotgt + ", Target turn No = " + antgtloc);
				}
			} else {
				System.out.println("TurnNo = " + (i + 1) + " --- ");
			}
		}
		System.out.println("Completed.");
		//here, we print the annotated repetitions
		for (int i = 0; i < pos.size(); i++){
			if (pos.get(i).length() > 0){
				String tmp = pos.get(i);
				//System.out.println(tmp);
				String [] poses = tmp.split(";");
				for (int z = 1; z < poses.length; z++){
					int repplace = poses[z].indexOf("-repetition-");
					int reptar = 0;
					if (repplace < 0) {
						repplace = poses[z].indexOf("-reference-");
						reptar = repplace + 11;
					} else reptar = repplace + 12;
					if (repplace != -1){
						String value = poses[z].substring(0, repplace);
						String target = poses[z].substring(reptar);
						System.out.println("turn no = " + (i + 1) + ", " + "VALUE = " + value + ", TARGET = " + target);
					}
				}
			}
		}
	}
}
