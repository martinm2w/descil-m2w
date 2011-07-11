package edu.albany.ils.dsarmd0200.util;

import java.util.*;

// Xin
// this class is to find all "link-to" utterances which point at each utterance in chain
// add them in chain 

public class CommLink 
{
	ArrayList<AChain> chainList;
	int turn;
	//int currentChainNumber;
	ArrayList<String> comm_act_type_list;
	ArrayList<String> link_to_list;
	
	ArrayList<Integer> retval;
	
	public CommLink(ArrayList<AChain> a, int turn, XMLParse xp)
	{
		this.chainList = a;
		this.turn = turn;
		//this.currentChainNumber = ChainNumber;
		this.comm_act_type_list = xp.getCommActs();
		this.link_to_list = xp.getCommActLinks();
		retval = new ArrayList<Integer>();
	}

	public ArrayList<Integer> fixComm()
	{
		// find utts link to this turn;
		for(int index1=0;index1<link_to_list.size();index1++)
		{
			String link_to_string = link_to_list.get(index1);
			if((link_to_string.indexOf(":"))!=-1)
			{
				int link_to = PronounResolution2.getLink(link_to_string);
				if(link_to == turn)
				{
					retval.add(index1+1);
				}
			}
		}
		/*
		// find if they are already in chains
		// loop chain list
		for(int index2=0;index2<chainList.size();index2++)
		{
			// get each chain
			AChain temp = chainList.get(index2);
			//if(temp.valuelocation.size()>6)
			//{
				// loop each chain
				for(int index3=0;index3<temp.size();index3++)
				{
					// get each chain's turn number
					int valLocation = temp.getValLocation(index3);
					if(retval.size()!=0)
					{
						for(int index4=0;index4<retval.size();index4++)
						{
							int tempNo = retval.get(index4).intValue();
							if(tempNo == valLocation)
							{
								retval.remove(index4);
							}
						}
					}
				}	
			//}
		}
		
		*/
		// generate an arraylist of turn numbers for this utt
		return retval;
	}
	
}
