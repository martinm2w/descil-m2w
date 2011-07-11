package edu.albany.ils.dsarmd0200.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

// Author: Ken Stahl
//this class is meant to contain one topic chain.

//didn't make an annotated token because wanted the sprawl to be partially borne here

//noun phrases are stored as strings instead of noun tokens here
//the reason for this is AChain ArrayLists can be created from both annotated and automated
//evaluations.  NounList has a function that returns an ArrayList of AChains.

public class AChain {
	public ArrayList <String> value; //m2w: testing value
	ArrayList <String> target;
	ArrayList <Integer> valuelocation;
	ArrayList <Integer> targetlocation;

	static ArrayList<String> filteredTopic;

	public AChain(){
		value = new ArrayList();
		target = new ArrayList();
		valuelocation = new ArrayList();
		targetlocation = new ArrayList();
	}

	public void add(String vparam, int vlparam, String tparam, int tlparam){
		value.add(vparam);
		valuelocation.add(vlparam);
		target.add(tparam);
		targetlocation.add(tlparam);
	}

	/**
         * m2w: this is a method that returns a arraylist of Strings which contains the turn number chain of a topic,starts with the topic in the 0'th entry.
         * @param a  ArrayList<AChain
         * @param xp XMLParse
         * @return ArrayList<String> a chain of turn numbers, 0th entry is topic.
         */
	public ArrayList<String> getChain(ArrayList<AChain> a, XMLParse xp){

		ArrayList<String> chainTurns = new ArrayList<String>();
		boolean doit = false;
		ArrayList<Integer> moreturns = new ArrayList<Integer>();
		int s = size();
		String topic = value.get(0);//put topic in 0'th entry
		chainTurns.add(topic);

		for (int i = 0; i < s; i++)
		{
			if(valuelocation.size()>6)
			{
				doit = true;
				CommLink comm = new CommLink(a, getValLocation(i), xp);
				moreturns = comm.fixComm();
			}

			chainTurns.add(new Integer(getValLocation(i)).toString());

			if(doit)
			{
				if(moreturns.size()!=0)
				{
					for(int index=0;index<moreturns.size();index++)
					{
						chainTurns.add(moreturns.get(index).toString());

					}
				}
			}
		}
		return chainTurns;

	}

	//original method. writes to a file.
	public void printOut(ArrayList<AChain> a, XMLParse xp, BufferedWriter bw)
	{
		boolean doit = false;
		ArrayList<Integer> moreturns = new ArrayList<Integer>();
		//This is Xin format, it is elite
		int s = size();
		String topic = value.get(0);
		//filteredTopic.add(topic);
		try {
			bw.write(topic);
			bw.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(topic);
		for (int i = 0; i < s; i++)
		{
			if(valuelocation.size()>6)
			{
				doit = true;
				CommLink comm = new CommLink(a, getValLocation(i), xp);
				moreturns = comm.fixComm(); 
			}
			//if (i > 0) System.out.print("\t");
			//System.out.println("#" + (i+1) + " " + value.get(i) + " location = " + getValLocation(i) + " and target = " + target.get(i));
			try {
			    bw.write(new Integer(getValLocation(i)).toString());
			    bw.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("getValLocation(): " + getValLocation(i));
			//System.out.println("flag3");
			if(doit)
			{
				if(moreturns.size()!=0)
				{
					for(int index=0;index<moreturns.size();index++)
					{
						try {
						    bw.write(moreturns.get(index).toString());
						    bw.newLine();
						} catch (IOException e) {
							// TODO Auto-generated catch block
						    e.printStackTrace();
						}
						//System.out.println("moreturens.get(index).intValue(): " + moreturns.get(index).toString());
					}
				}
			}
		}
	}

	public boolean match(String param){
		String p = param.toLowerCase();
		String val = value.get(0).toLowerCase();
		return (p.contains(val) || val.contains(p));
	}

	// compare 2 chains
	public double matchVal(AChain param){
		//root match can easily
		//be checked with the match function right above ^^
		double retval = 0.0;
		double hits = 0.0;
		double misses = 0.0;
		double falsealarms = 0.0;
		double total = 0.0;
		//param.printOut();
		int place = 1; int csize = size(); int paramsize = param.size();

		// add 1 point if root number matches
		if (match(param.getValue(0)) && param.getValLocation(0) == getValLocation(0))
		{
			hits++;
			boolean match = false;
			// do comparison from 1
			for (int i = place; i < csize; i++)
			{
				String compare = getValue(i);
				int comparelocation = getValLocation(i);
				int comparetargetlocation = getTargetLoc(i);
				int plocation = 0;
				boolean fa = true;
				for (int j = 0; (j < paramsize) && (plocation <= comparelocation); j++)
				{
					String pcompare = param.getValue(j);
					int ptargetloc = param.getTargetLoc(j);
					plocation = param.getValLocation(j);
					if ((compare.contains(pcompare) || pcompare.contains(compare)) &&
							(plocation == comparelocation))
					{
						match = true;
						if (ptargetloc == comparetargetlocation)
						{
							hits++;
							fa = false;
						}
						else
						{
							misses++;
							fa = false;
						}
					}
				} if (fa) falsealarms++;
				if (match)
					hits++;
				else
					misses++;
			}
		} else
			misses++;
		total = hits + misses + falsealarms;
		System.out.print("AnTopic = " + getValue(0) + ", AuTopic = " + param.getValue(0));
		System.out.print(" Anlocation = " + valuelocation.get(0));
		System.out.println(" total = " + total + " hits = " + hits);
		System.out.println("Aulocation = " + param.getValLocation(0));
		if (total > 0.0){
			retval = hits / total;
		}
		return retval;
	}

	public int location(){
		//returns the location of the root of the chain
		return valuelocation.get(0);
	}

	public String getValue(int place){
		return value.get(place);
	}

	public int getValLocation(int place){
		return valuelocation.get(place);
	}

	public int getTargetLoc(int place){
		return targetlocation.get(place);
	}

	public int size(){
		//returns the size of the chain
		return value.size();
	}

	public static ArrayList getFilteredTopic()
	{
		return filteredTopic;
	}

}
