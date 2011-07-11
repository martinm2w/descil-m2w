package edu.albany.ils.dsarmd0200.util;

import java.util.*;
// Xin
// This class rewrites the pronoun resolution for better performance
public class PronounResolution2 
{
	NounToken token;
	int targetID = -1;
	int ID = -1;
	
	ArrayList<String> turn_no;
	ArrayList<String> commacts;
	ArrayList<String> links;
	ArrayList<NounToken> nouns;
	ArrayList<String> utterances;
	
	boolean a = false;
	boolean b = false;
	boolean c = false;
	
	public PronounResolution2(NounToken nt, ArrayList<String> turn_no, ArrayList<String> commacts, ArrayList<String> links, ArrayList<NounToken> nouns, ArrayList<String> utterances)
	{
		this.turn_no = turn_no;
		this.commacts = commacts;
		this.links = links;
		this.nouns = nouns;
		this.utterances = utterances;
		
		this.token = nt;

		
		ID = resolve(nt);
		

	}
	
	private int resolve(NounToken nt) 
	{
		String pronounword = nt.getWord();
		int pronounTurnNo = nt.getTurnNo();
		int pronounID = nt.getID();
		// saves nouns in the "link-to" part
		ArrayList<NounToken> temp1 = new ArrayList<NounToken>();
		// saves nouns before the pronoun in the same line
		ArrayList<NounToken> temp2 = new ArrayList<NounToken>();
		// saves nouns before the particular line
		ArrayList<NounToken> temp3 = new ArrayList<NounToken>();		
		
		// get turn no's lower bound
		int lowbound = Integer.parseInt(turn_no.get(0));
		// get turn no's upper bound
		int upbound = Integer.parseInt(turn_no.get(turn_no.size() - 1));
		// get this turn no of this word
		String comm_act = "";
		if ((pronounTurnNo >= lowbound) && (pronounTurnNo <= upbound))
			comm_act = commacts.get(pronounTurnNo - 1);
				
		// if the utterance has "link-to", find nouns in link-to utterances and its own utterance
		if (comm_act.equalsIgnoreCase("continuation-of") ||
				comm_act.equalsIgnoreCase("response-to"))
		{
			// get the link number
			int lnk = getLink(links.get(pronounTurnNo - 1));
			temp1 = getUtteranceNouns(lnk);
		}
			
		temp2 = nounsBeforeX(pronounID);
		// reverse the arraylist to find the nearest names for pronoun
		Collections.reverse(temp2);
		
		temp3 = nounsBeforeLine(pronounID);
		

		if(GenderCheck.isPronounHuman(pronounword))
		{
			// male pronoun
			if(GenderCheck.couldPronounBeMale(pronounword))
			{
				if(!a)
				{
					// first find the nearest male name in its utterance
					if(temp2.size()>0)
					{
						for(int i=0;i<temp2.size();i++)
						{
						
							NounToken maleToken = temp2.get(i);
							String maleWord = maleToken.getWord();
							if(GenderCheck.isNameMale(maleWord) && !TopicFilter.hasparName(maleWord))
							{	
								targetID = maleToken.getID();
								a = true;
								break;
							}								
						}
					}
				}
				// fail to find male names in the line
				// start find pronouns in the "link-to" line to refer
				
				if(!a)
				{
					if(temp1.size()>0)
					{
						for(int j=0;j<temp1.size();j++)
						{
							NounToken linktomaleToken = temp1.get(j);
							String linktomaleWord = linktomaleToken.getWord();
							if(GenderCheck.isNameMale(linktomaleWord) && !TopicFilter.hasparName(linktomaleWord))
							{
								targetID = linktomaleToken.getID();
								a = true;
								break;
							}
							
							// find pronoun in "link to" line, recursively to deal with "link to" line
							if(GenderCheck.pronounGenderMatch(linktomaleWord, pronounword))
							{
								this.resolve(linktomaleToken);
								break;
							}
						}
					}	
				}
				
				// both search failed, start to find male names in previous lines, line by line
				if(!a)
				{
					if(temp3.size()>0)
					{
						for(int k=0;k<temp3.size();k++)
						{
							NounToken previousmaleToken = temp3.get(k);
							String previousmaleWord = previousmaleToken.getWord();
							if(GenderCheck.isNameMale(previousmaleWord) && !TopicFilter.hasparName(previousmaleWord))
							{
								targetID = previousmaleToken.getID();
								a = true;
								break;
							}							
						}
					}					
				}
			}
			//_+_+_+_+_+_+_+_+_+_ start doing females _+_+_+_+_+_+_+_+_+_+_+_+_+
			else if(GenderCheck.couldPronounBeFemale(pronounword))
			{
			    //System.out.println("it's a female pronoun");
				if(!b)
				{
					// first find the nearest female name in its utterance
					if(temp2.size()>0)
					{
						//System.out.println("flag 1.1");
						for(int i=0;i<temp2.size();i++)
						{
							NounToken femaleToken = temp2.get(i);
							String femaleWord = femaleToken.getWord();
							//System.out.println("Applicant word: "+femaleWord);
							if(GenderCheck.isNameFemale(femaleWord)&& !TopicFilter.hasparName(femaleWord))
							{
								//System.out.println("flag 1.2");
								targetID = femaleToken.getID();
								b = true;
								//System.out.println(femaleWord);
								break;
							}								
						}
					}
				}
				// fail to find female names in the line
				// start find pronouns in the "link-to" line to refer
				
				if(!b)
				{
					if(temp1.size()>0)
					{
						for(int j=0;j<temp1.size();j++)
						{
							//System.out.println("flag 2.1");
							NounToken linktofemaleToken = temp1.get(j);
							String linktofemaleWord = linktofemaleToken.getWord();
							if(GenderCheck.isNameFemale(linktofemaleWord) && !TopicFilter.hasparName(linktofemaleWord))
							{
								//System.out.println("flag 2.2");
								targetID = linktofemaleToken.getID();
								b = true;
								break;
							}
							
							// find pronoun in "link to" line, recursively to deal with "link to" line
							if(GenderCheck.pronounGenderMatch(linktofemaleWord, pronounword))
							{
								//System.out.println("flag 2.3");
								this.resolve(linktofemaleToken);
								//System.out.println(linktofemaleWord);
								break;
							}
						}
					}
				}
				
				if(!b)
				{
					// both search failed, start to find female names in previous lines, line by line
					if(temp3.size()>0)
					{
						//System.out.println("flag 3.1");
						for(int k=0;k<temp3.size();k++)
						{
							NounToken previousfemaleToken = temp3.get(k);
							String previousfemaleWord = previousfemaleToken.getWord();
							if(GenderCheck.isNameFemale(previousfemaleWord) && !TopicFilter.hasparName(previousfemaleWord))
							{
								//System.out.println("flag 3.2");
								targetID = previousfemaleToken.getID();
								//System.out.println(previousfemaleWord);
								break;
							}							
						}
					}					
				}
			}
		}		
		// deal with non-human pronouns
		
		else if(GenderCheck.couldPronounBeNonHuman(pronounword))
		{
			if(!c)
			{
				if(temp2.size()>0)
				{
					for(int i=0;i<temp2.size();i++)
					{
						NounToken nonhumanToken = temp2.get(i);
						String nonhumanWord = nonhumanToken.getWord();
						if(!GenderCheck.isName(nonhumanWord))
						{
							targetID = nonhumanToken.getID();
							c = true;
							break;
						}			
					}
				}
			}
			
			if(!c)
			{	
				if(temp1.size()>0)
				{
					for(int j=0;j<temp1.size();j++)
					{
						NounToken linktononhumanToken = temp1.get(j);
						String linktononhumanWord = linktononhumanToken.getWord();
						if(!GenderCheck.isName(linktononhumanWord))
						{
							targetID = linktononhumanToken.getID();
							c = true;
							break;
						}
						
						// find pronoun in "link to" line, recursively to deal with "link to" line
						if(!GenderCheck.isPronounHuman(linktononhumanWord))
						{
							this.resolve(linktononhumanToken);
							break;
						}
					}
				}
			}
			
			if(!c)
			{
				if(temp3.size()>0)
				{
					for(int k=0;k<temp3.size();k++)
					{
						NounToken previousnonhumanToken = temp3.get(k);
						String previousnonhumanWord = previousnonhumanToken.getWord();
						if(!GenderCheck.isName(previousnonhumanWord))
						{
							targetID = previousnonhumanToken.getID();
							c = true;
							break;
						}
					}		
				}
			}
			
		}	
		
		
		return targetID;
	}
	
	
	

	//uttNum = link_to, uttID = source(original) utterances
	//using commacts for lists of noun phrases not necessarily the best way
	//temptation to just return the whole nouns list
	// 1st param: link number
	// 2nd param: word's ID
	// This function takes the "link-to" utterance number and the original word's ID
	// Then find nouns in "link-to" utterance and nouns in the same line before the original word
	// and add them all in an arraylist of uttNouns and return  
	public ArrayList<NounToken> arrangeNouns(int uttNum, int uttId)
	{
		// uttNouns is an arraylist of "link-to" target utterance nouns
		ArrayList <NounToken> uttNouns = getUtteranceNouns(uttNum);
		int lowbound = Integer.parseInt(turn_no.get(0));
		if ((uttNum - lowbound) <= 0 || uttNum > utterances.size())
			return nouns;
		//here we can add more noun tokens based on link_to chains
		String cmmact = "";
		if (uttNum - lowbound > 0)
			cmmact = commacts.get(uttNum - lowbound);
		/*
		int u = uttNum;
		while ( u > 0 && (cmmact.equalsIgnoreCase("continuation-of") ||
				cmmact.equalsIgnoreCase("response-to"))){
			ArrayList <NounToken> tmpNouns = getUtteranceNouns(u);
			u = getLink(links.get(u - 1));
			cmmact = commacts.get(u - 1);

		} */
		//System.out.println("uttNum=" + uttNum + "  comm_act = " + cmmact);
		ArrayList <NounToken> rest = nounsBeforeX(uttId);
		for (int i = 0; i < rest.size(); i++)
			uttNouns.add(rest.get(i));
		return uttNouns;
	}
	
	// this functions takes a token id and returns all tokens before this token's line
	public ArrayList<NounToken> nounsBeforeLine(int uttId)
	{
		ArrayList<NounToken> retval = new ArrayList<NounToken>();
		if(uttId>0)
		{
		NounToken nt = nouns.get(uttId - 1);
		
		for(int i = 0; i < uttId;i++)
		{
			NounToken temp = nouns.get(i);
			if((temp.getTurnNo() < nt.getTurnNo()) && (temp.getID() < nt.getID()))
			{
				retval.add(nouns.get(i));
			}			
		}
		Collections.reverse(retval);
		}
		return retval;
	}

	//takes a token id and returns any tokens in the same line which occur
	//before the noun token id parameter
	public ArrayList<NounToken> nounsBeforeX(int uttId)
	{
		ArrayList <NounToken> retval = new ArrayList<NounToken>();
		if(uttId>0)
		{
			NounToken nt = nouns.get(uttId-1);
		
		
		for (int i = 0; i < uttId ; i++)
		{
			NounToken temp = nouns.get(i);
			if ((temp.getTurnNo() == (nt.getTurnNo())) &&
					(temp.getID() <= nt.getID()))
				retval.add(nouns.get(i));
		}
		}
		return retval;
	}

	//returns all the noun phrases from a particular line of chat
	public ArrayList<NounToken> getUtteranceNouns(int uttNum)
	{
		// uttNum: link number
		ArrayList <NounToken> retval = new ArrayList<NounToken>();
		// get all nouns in utterance of link number and save into arraylist
		for (int i = 0; i < nouns.size(); i++)
			if (nouns.get(i).getTurnNo() == uttNum)
				retval.add(nouns.get(i));
		return retval;
	}

	//parse the number out of the link_to tag in the xml file
	public static int getLink(String lnkTo)
	{
		int retval = -1;
		if (lnkTo.length() > 0 && lnkTo.contains("."))

		{
		    String desVal = lnkTo.substring(lnkTo.indexOf(":") + 1);
		    String[]desVals = desVal.split("\\.");
		    retval = Integer.parseInt(desVals[0]);
		}
		return retval;
	}

	public int getTargetID()
	{
		return this.ID;
	}
		
}
