package edu.albany.ils.dsarmd0200.util;

import java.util.*;

// This class HARDCODED words which are not suitable for being chose as topic
// like participant names, greetings...
// return true if this word needs to be filtered

public class TopicFilter 
{
	public static boolean needFiltered(String word)
	{
		ArrayList<String> filterwords = new ArrayList<String>();
		filterwords.add("kerri");
		filterwords.add("kara");
		filterwords.add("ken");
		filterwords.add("chris");
		filterwords.add("sarah");
		filterwords.add("jennifer");
		filterwords.add("tony");
		filterwords.add("jessica");
		filterwords.add("ashley");
		filterwords.add("Laura");
		filterwords.add("john");
		filterwords.add("farina");
		filterwords.add("lauren");
		
		filterwords.add("hi");
		filterwords.add("morning");
		filterwords.add("evening");
		filterwords.add("afternoon");
		filterwords.add("night");
		filterwords.add("hello");
		filterwords.add("thanks");
		filterwords.add("bye");
		filterwords.add("thing");
		filterwords.add("things");
		filterwords.add("person");
		filterwords.add("people");
		filterwords.add("ok");
		filterwords.add("okay");
		filterwords.add("yeah");
		filterwords.add("no");
		filterwords.add("have");
		filterwords.add("has");
		filterwords.add("do");
		filterwords.add("not");
		filterwords.add("it");
		filterwords.add("the");
		// of course you could add more...
		
		for(int index=0;index<filterwords.size();index++)
		{
			String w = filterwords.get(index);
			if(word.equalsIgnoreCase(w))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasparName(String word)
	{
		ArrayList<String> participants = new ArrayList<String>();
		participants.add("kerri");
		participants.add("kara");
		participants.add("ken");
		participants.add("chris");
		participants.add("sarah");
		participants.add("jennifer");
		participants.add("lance");
		
		word = word.toLowerCase();		
		return(participants.contains(word));
	}
}
