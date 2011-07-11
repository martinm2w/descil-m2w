package edu.albany.ils.dsarmd0200.util;

import java.io.*;
import java.util.*;

public class PhraseCheck 
{
	String filename;
	ArrayList<String> utterance;
	
    public PhraseCheck(String filename, ArrayList utts_)
	{
		this.filename = filename;
		utterance = utts_;
	}
	
	public boolean check(String s)
	{
		int positive = 0, negative = 0;
		for(int i=0;i<utterance.size();i++)
		{
		    try {
			//System.out.println("utterance: " + utterance.get(i));
			if(utterance.get(i).indexOf(s) != -1)
			    {positive++;}				
			else
			    negative++;
		    }catch (Exception e) {
			e.printStackTrace();
			System.out.println("s: " + s);
			//System.out.println("utterance: " + utterance.get(i));
			System.exit(1);
		    }
		}
		
		if(positive !=0)
			{return true;}
		else
			return false;
		
	}

}
