package edu.albany.ils.dsarmd0200.lu;

/**
 * @file: CommunicationLinkX.java
 * @author: Xin Shi
 * This file is the update of CommunicationLink
 */

import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import java.util.*;

public class CommunicationLinkX 
{
    public ArrayList<Utterance> utts = new ArrayList<Utterance>();	// all utterances
    private HashSet<String> speaker_names = new HashSet<String>();	// all speakers names
    private TreeMap<String,Integer> comm_total = new TreeMap<String,Integer>();	// saves all speaker's preference
//    private HashMap<String,String> comm_speaker = new HashMap<String,String>();	// saves preferences for a certain pair of speakers
    public static final String CONTINUATION_OF="continuation-of";
    public static final String ADDRESSED_TO="addressed-to";
    public static final String RESPONSE_TO="response-to";
    public static final int COMM_THRESHOLD = 10;
    public static final int SHORT_THRESHOLD = 4;	// for short turn lengths
    public static final int LONG_THRESHOLD = 4;	// for long turn lengths
    public static final double SIM_THRESHOLD = 0.2;	// for similarity 
    public static final int SHORT_THRESHOLD_QUESTION = 3;
    public static final int LONG_THRESHOLD_QUESTION = 3;
    public static final int NOUN_SIM = 5;
    private boolean isEnglish_ = false;
    private boolean isChinese_ = false;
    public int response_to_count;
    public int hit;	// correct count
    private Utterance name_utt;
    public TreeMap<Integer,String> map = new TreeMap<Integer,String>();
    public ArrayList<Integer> cl_arr = new ArrayList<Integer>();
    
    public CommunicationLinkX(ArrayList<Utterance> utts,
			      boolean isEnglish,
			      boolean isChinese) 
    {
    	// get all utterances for one file
	isEnglish_ = isEnglish;
	isChinese_ = isChinese;
    	this.utts = utts;
//    	StanfordPOSTagger.initialize();
    	ParseTools.initialize();
    }
    
    public void collectCLFtsX() 
    {
    	// loop all utterances and get all speaker names
    	for(int i=0; i<utts.size(); i++)
    	{
    		Utterance u_speaker = utts.get(i);
    		speaker_names.add(u_speaker.getSpeaker().toLowerCase());		
    	}
    	
    	/*
    	Iterator<String> iterator = speaker_names.iterator();
    	while(iterator.hasNext())
    	{
    		String name = iterator.next();
    		System.out.println(name);
    	}
    	*/
    	
    	for(int index=0; index<utts.size(); index++)
    	{
    		Utterance u_content = utts.get(index);
    		if(u_content.getCommActType().equals(RESPONSE_TO))
    		{
    			response_to_count++;
    			String content = contentExtraction(u_content, isEnglish_, isChinese_);
    			int turn_length = ParseTools.wordCount(content);
    			if(turn_length < 5)
    				shortUtterance(index);
    			else
    				longUtterance(index);
    		}
    	} 
    	// here print out the final result
    	//System.out.println("Hit: " + hit);
    	//System.out.println("Number of response utt: " + response_to_count);
    	//System.out.println("Precision: " + (double)((double)hit)/((double)response_to_count));
    }
    
    // This fuction tries to find naming relations between current and previous utterances
    public boolean findName(Utterance utt)
    {
    	int index = utts.indexOf(utt);//Integer.parseInt(utt.getTurn()) - 1; //modified by Ting 03/03
    	String name = "";
    	boolean name_found = false;  	
    	boolean link_found = false;
		// get current utterance content
		String curr_content = contentExtraction(utt, isEnglish_, isChinese_);
		String curr_speaker = utt.getSpeaker().toLowerCase();
		StringTokenizer tokenizer = new StringTokenizer(curr_content);
		while(tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			if(speaker_names.contains(token.toLowerCase()))
			{
				// a speaker name found in current utterance
				name_found = true;
				name = token.toLowerCase();
				break;
			}
		}
		
		ArrayList<Integer> pre_utt_list = new ArrayList<Integer>();	// saves previous turn numbers
		ArrayList<String> pre_speaker_list = new ArrayList<String>();	// saves previous speaker names
		
		for(int i=1; i<=SHORT_THRESHOLD; i++)
		{
			// get one previous utterance
			if(index >= i)
			{	
				Utterance utterance = utts.get(index-i);
				// cannot response self
				if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker))
				{
					String pre_content = contentExtraction(utterance, isEnglish_, isChinese_);
					// add turn number and speaker name of previous utterance one by one
					pre_utt_list.add(Integer.parseInt(utterance.getTurn()));
					pre_speaker_list.add(utterance.getSpeaker().toLowerCase());
					// name in current utterance find in previous speakers
					if(name_found)
					{
						if(name.equals(utterance.getSpeaker().toLowerCase()))
						{
							link_found = true;
							setNameUtt(utterance);
							break;
						}
					}
					if(!link_found && !name_found)
					{
						StringTokenizer pre_tokenizer = new StringTokenizer(pre_content);
						// name equals to current speaker found in a specific previous utterance
						while(pre_tokenizer.hasMoreTokens())
						{
							String pre_token = pre_tokenizer.nextToken();
							if(curr_speaker.equals(pre_token.toLowerCase()))
							{
								link_found = true;
								setNameUtt(utterance);
								break;
							}
						}
					}
				}
			}
		}
		return link_found;
    }
    

    
    // This function deals with utterance length under 5
	private void shortUtterance(int index) 
	{
		boolean found = false;
		Utterance utt = utts.get(index);
		String curr_speaker = utt.getSpeaker().toLowerCase();
		ArrayList<Integer> pre_utt_list = new ArrayList<Integer>();	// saves previous turn numbers
		
		if(findName(utt))
		{
		    //System.out.println("Source turn: " + Integer.parseInt(utt.getTurn()));
		    //System.out.println("Target: " + (utts.get(Integer.parseInt(getNameUtt().getTurn())-1)).getSpeaker() + ":" + Integer.parseInt(getNameUtt().getTurn()));
			map.put(Integer.parseInt(utt.getTurn()), (utts.get(Integer.parseInt(getNameUtt().getTurn())-1)).getSpeaker() + ":" + Integer.parseInt(getNameUtt().getTurn()));
			String sysRespTo = (utts.get(Integer.parseInt(getNameUtt().getTurn())-1)).getSpeaker() + ":" + Integer.parseInt(getNameUtt().getTurn());
			utt.setRespTo(sysRespTo);
//			utt.setRespTo(utts.get(utts.get(Integer.parseInt(getNameUtt().getTurn())-1)).getSpeaker() + ":" + Integer.parseInt(getNameUtt().getTurn()));
			//evaluate(index,Integer.parseInt(getNameUtt().getTurn()),11);
			found = true;
		}
		
		// no name matches, start looking for ? sign
		if(!found)
		{
			for(int i=1; i<=SHORT_THRESHOLD_QUESTION; i++)
			{
				if(index >= i)
				{
					Utterance utterance = utts.get(index-i);
					if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker))
					{
						String pre_content = utterance.getContent();
						pre_utt_list.add(Integer.parseInt(utterance.getTurn()));
						if(pre_content.indexOf('?')!=-1)
						{	
							found = true;
							map.put(Integer.parseInt(utt.getTurn()), (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i));
							String sysRespTo = (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i);
							utt.setRespTo(sysRespTo);
							//evaluate(index,Integer.parseInt(utts.get(index).getTurn())-i,12);		
							break;
						}
					}
				}
			}
		}
		
		// all attempts failed, default to the first previous one
		if(!found)
		{
			if(pre_utt_list.size() != 0)
			{
				map.put(Integer.parseInt(utt.getTurn()), (utts.get(pre_utt_list.get(0)-1).getSpeaker() + ":" + pre_utt_list.get(0)));
				String sysRespTo = (utts.get(pre_utt_list.get(0)-1).getSpeaker() + ":" + pre_utt_list.get(0));
				utt.setRespTo(sysRespTo);
				//evaluate(index,pre_utt_list.get(0),13);
				found = true;
			}
		}
	}

	// This function deals with utterance length over 5
	private void longUtterance(int index) 
	{
		boolean found = false;
		Utterance utt = utts.get(index);
//		String curr_content = contentExtraction(utts.get(index), isEnglish_, isChinese_);
		String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
//		TreeMap<Double,Integer> sims = new TreeMap<Double,Integer>();
		ArrayList<Integer> pre_utt_turn = new ArrayList<Integer>();
		
		// code 21
		if(findName(utt))
		{
			map.put(Integer.parseInt(utt.getTurn()), (utts.get(Integer.parseInt(getNameUtt().getTurn())-1)).getSpeaker() + ":" + Integer.parseInt(getNameUtt().getTurn()));
			String sysRespTo = (utts.get(Integer.parseInt(getNameUtt().getTurn())-1)).getSpeaker() + ":" + Integer.parseInt(getNameUtt().getTurn());
			utt.setRespTo(sysRespTo);
			//evaluate(index,Integer.parseInt(getNameUtt().getTurn()),21);
			found = true;
		}

		
		// find question, code 22
		if(!found)
		{
			for(int i=1; i<=LONG_THRESHOLD_QUESTION; i++)
			{
				if(index >= i)
				{
					Utterance utterance = utts.get(index-i);
					if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker))
					{	
						// calculate similarity
						// String pre_content = contentExtraction(utterance);
						// double sim = Util.compareUtts(pre_content, curr_content);
						int pre_turn_no = (Integer.parseInt(utts.get(index).getTurn()))-i;
						//System.out.println("pre_turn_no: " + pre_turn_no);
						pre_utt_turn.add(pre_turn_no);
						// sims.put(sim, pre_turn_no);
					
						// if ? found, just use this utterance
						String pre_raw_content = utterance.getContent();
						if(pre_raw_content.indexOf('?')!=-1)
						{
							found = true;
							map.put(Integer.parseInt(utt.getTurn()), (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i));
							String SysRespTo = (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i);
							utt.setRespTo(SysRespTo);
							//evaluate(index,Integer.parseInt(utts.get(index).getTurn())-i,22);				
							break;
						}			
					}
				}
			}
			
			if(pre_utt_turn.size() == 0) {
			    //System.out.println("(size 0)pre_utt_turn: " + (Integer.parseInt(utts.get(index).getTurn())-(LONG_THRESHOLD + 1)));
			    if (Integer.parseInt(utts.get(index).getTurn()) > (LONG_THRESHOLD + 1)) {
				pre_utt_turn.add(Integer.parseInt(utts.get(index).getTurn())-(LONG_THRESHOLD + 1));
			    }
			}
		}
		
		
		// find identical nouns, code 24 
//		if(!found)
//		{					
//			for(int i=1; i<=NOUN_SIM; i++)
//			{
//				if(index >= i)
//				{
//					Utterance curr_utt = utts.get(index);
//					String curr_raw_content = curr_utt.getContent();
//					Utterance utterance = utts.get(index-i);
//					ArrayList<String> curr_nouns = getNouns(curr_raw_content);
//					if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker))
//					{	
//						int pre_turn_number = (Integer.parseInt(utts.get(index).getTurn()))-i;
//						String pre_content = utterance.getContent();
//						ArrayList<String> pre_nouns = getNouns(pre_content);	
//						// identical noun found
//						if(nounMatch(curr_nouns,pre_nouns))
//						{
//						    //System.out.println("curr_nouns: " + curr_nouns.toString());
//						    //System.out.println("pre_nouns: " + pre_nouns.toString());
//							found = true;
//							String sysRespTo = (utts.get(pre_turn_number-1).getSpeaker() + ":" + pre_turn_number);
//							utt.setRespTo(sysRespTo);
//							map.put(Integer.parseInt(utt.getTurn()), (utts.get(pre_turn_number-1).getSpeaker() + ":" + pre_turn_number));
//							//evaluate(index,pre_turn_number,24);
//							break;
//						}
//					}
//				}
//			}
//		}
		
		// default to previous one, code 21
		if(!found && pre_utt_turn.size() > 0)
		{
			// get the highest similarity value
			 // double best_sim = sims.keySet().iterator().next();
			// set the target to the utterance of its sim value over threshold
//			if(best_sim > SIM_THRESHOLD)
//				evaluate(index,sims.get(best_sim));
			// just tried to default to the previous one
//			else
			//System.out.println("No prob here");
			//if(pre_utt_turn.get(0) != null)
		    //evaluate(index,pre_utt_turn.get(0),23);
		    String sysRespTo = (utts.get(pre_utt_turn.get(0)-1).getSpeaker() + ":" + pre_utt_turn.get(0));
				utt.setRespTo(sysRespTo);
				map.put(Integer.parseInt(utt.getTurn()), (utts.get(pre_utt_turn.get(0)-1).getSpeaker() + ":" + pre_utt_turn.get(0)));
				//System.out.println("pre_utt_turn_0: " + pre_utt_turn.get(0));
			//else
			//{
				//evaluate(index,Integer.parseInt(utts.get(index).getTurn())-(LONG_THRESHOLD_QUESTION + 1),23);
				//System.out.println("Enter here");
			//}	//System.out.print("Best similarity for turn no " + index + " is ");
				//System.out.println(best_sim + ", " + ((Integer.parseInt(utts.get(index).getTurn()))-sims.get(best_sim))
					//+ " previous from current utterance");
		}
	}
	
    // This function extract content from an utterance and filter it
    private static String contentExtraction(Utterance utterance,
					    boolean isEnglish_,
					    boolean isChinese_)
    {    			
    	String content = utterance.getContent();
    	content = ParseTools.removeEmoticons(content);
    	content = ParseTools.removePunctuation(content, isEnglish_, isChinese_);
    	return content;	
    }
    
    // evaluation
	private void evaluate(int curr_index, int sysTurnNo, int code) 
	{
		String curr_turn_no = utts.get(curr_index).getTurn();
		String link_to = utts.get(curr_index).getRespTo();
		if(link_to.indexOf(":")!=-1)
		{
			String[] lkto = link_to.split(":");
			int anno_turn = Integer.parseInt(lkto[1]);
			if(sysTurnNo == anno_turn)
				hit++;
			else {}
			    //errorAnalysis(curr_turn_no,sysTurnNo,anno_turn,code);
		}
	}
    
	// This function updates relations between speakers
	// the tendency is if one speaker prefer to speak with another speaker,
	// we assume this speaker is always willing to speak with him/her 
	private void setRelations(String orig_speaker_name, String target_speaker_name, boolean has_Threshold) 
	{
		String s1 = orig_speaker_name + ":" + target_speaker_name;
		String s2 = target_speaker_name + ":" + orig_speaker_name;
		Integer count1 = comm_total.get(s1);
		if(count1 == null)
			count1 = new Integer(0);
		if(has_Threshold)
			{if(count1 < COMM_THRESHOLD)	
				{comm_total.put(s1, ++count1);}}
		else
			comm_total.put(s1, ++count1);
		Integer count2 = comm_total.get(s2);
		if(count2 == null)
			count2 = new Integer(0);
		if(has_Threshold)
			{if(count2 < COMM_THRESHOLD)
				{comm_total.put(s2, ++count2);}}
		else
			comm_total.put(s2, ++count2);
	}
	
	// getters and setters
    private void setNameUtt(Utterance name_utt)
    {
    	this.name_utt = name_utt;
    }
    
    private Utterance getNameUtt()
    {
    	return name_utt;
    }
    
    private boolean nounMatch(ArrayList<String> srcArr, ArrayList<String> destArr)
    {
    	if(srcArr.size() == 0 || destArr.size() == 0)
    		return false;
    	else
    	{
    		for(int i=0; i<srcArr.size(); i++)
    		{
    			String srcWord = srcArr.get(i);
    			for(int j=0; j<destArr.size(); j++)
    			{
    				String destWord = destArr.get(j);
    				if(srcWord.equalsIgnoreCase(destWord))
    					return true;
    			}
    		}
    		return false;
    	}
    }
    
    // extract all nouns in each utterance content and save in arraylist
    private ArrayList<String> getNouns(String content)
    {
    	String noEmotes = ParseTools.removeEmoticons(content);
		String tagged = StanfordPOSTagger.tagString(noEmotes).trim();
		String [] tagsplit = tagged.split("\\s+");
		ArrayList <String> tokens = getTokens(tagsplit);
    	return tokens; 	
    }
	
    private ArrayList<String> getTokens(String[] tokens)
    {
    	ArrayList<String> retval = new ArrayList<String>();
    	for(int i=0; i<tokens.length; i++)
    	{
    		String tag = ParseTools.getTag(tokens[i]);
    		String word = ParseTools.getWord(tokens[i]);
    		boolean is_noun = ParseTools.isNoun(tag);
    		if(is_noun)
    			retval.add(word);
    	}
    	return retval;
    }
    
    public TreeMap<Integer,String> getMap()
    {
    	return map;
    }
    
    public ArrayList<Utterance> getUtts()
    {
    	return utts;
    }
	// ***************************** functions below do overall statistics ****************************
	
	// This function calculates distances between current utterance and its link
	public void distanceCal()
	{
		int total = 0;
		HashMap<Integer,Integer> dis_map = new HashMap<Integer,Integer>();
		for(int i=0; i<utts.size(); i++)
		{
			Utterance utt = utts.get(i);
			if(utt.getCommActType().equals(RESPONSE_TO))
			{
				total++;
				String lkto = utts.get(i).getRespTo();
				if(lkto.indexOf(":")!=-1)
				{
					String[] link_to = lkto.split(":");
					int dis_ = Integer.parseInt(utt.getTurn()) - Integer.parseInt(link_to[1]);
					Integer distance = new Integer(dis_);
					Integer times = dis_map.get(distance);
					if(times == null)
						times = new Integer(0);
					dis_map.put(distance, ++times);
				}
			}
		}
		//System.out.println();
		//System.out.println("Total number of utterances has respond type: " + total);
		for(Iterator<Integer> it = dis_map.keySet().iterator(); it.hasNext();)
		{
			Integer next = it.next();
			//System.out.println("Distance:" + next + "\tTimes:" + dis_map.get(next));
		}
		//System.out.println("Percentage of distance < 4: " + ((double)((double)(dis_map.get(1).intValue() + dis_map.get(2).intValue() 
				//+ dis_map.get(3).intValue() + dis_map.get(4).intValue())/((double)(total)))) + "%");
	}
	
	// this function calcualtes relations between speakers
	public void relationCal()
	{
		for(int i=0; i<utts.size(); i++)
		{
			Utterance utt = utts.get(i);
			if(utt.getCommActType().equals(RESPONSE_TO))
			{
				String orig_speaker = utt.getSpeaker().toLowerCase();
				String lkto = utts.get(i).getRespTo();
				if(lkto.indexOf(":")!=-1)
				{
					String[] link_to = lkto.split(":");
					String target_speaker = link_to[0];
					setRelations(orig_speaker,target_speaker,false);
				}
			}
		}
		//Collections.sort(comm_total.keySet());
		for(Iterator<String> it = comm_total.keySet().iterator(); it.hasNext();)
		{
			String next = it.next();
			//System.out.println("Speaker " + next + " speak " + comm_total.get(next) + " Times");
		}
		//System.out.println();
	}
	
	public void errorAnalysis(String curr_turn_no, int auto_turn_no, int anno_turn_no, int code)
	{
		System.out.println("Turn " + curr_turn_no + "(" + utts.get(Integer.parseInt(curr_turn_no)-1).getSpeaker() + "): " + 
				utts.get(Integer.parseInt(curr_turn_no)-1).getContent());
		System.out.println("Code: " + code);
		System.out.println("System thought it linked to " + auto_turn_no + "(" + utts.get(auto_turn_no - 1).getSpeaker() + "): " + utts.get(auto_turn_no - 1).getContent());
		System.out.println("Annotated linked to " + anno_turn_no + "(" + utts.get(anno_turn_no - 1).getSpeaker() + "): " + utts.get(anno_turn_no - 1).getContent());
		System.out.println();
	}
}
