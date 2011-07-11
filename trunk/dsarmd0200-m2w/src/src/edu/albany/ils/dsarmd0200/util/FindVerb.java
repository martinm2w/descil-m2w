package edu.albany.ils.dsarmd0200.util;

import java.util.*;

// Xin

public class FindVerb 
{
	// define Nouns
	private static final String N1 = "NN";
	private static final String N2 = "NNS";
	private static final String N3 = "NNP";
	private static final String N4 = "NNPS";
	
	ArrayList<String> taggedUtterance;
	ArrayList<String> verbList;
	ArrayList<verbToken> tokenList;
	
	// constructor, initialize the arraylist
	public FindVerb()
	{
		taggedUtterance = new ArrayList<String>();
		verbList = new ArrayList<String>();
		tokenList = new ArrayList<verbToken>();
	}
	
	// add each taggedUtterance in the ArrayList
	public void add(String string)
	{
		taggedUtterance.add(string);
	}
	
	// get the tagged Utterance arraylist
	public ArrayList<String> getTaggedUtterance()
	{
		return taggedUtterance;
	}
	
	// this function checks each token to find if 
	// the word is verb, then loop through each
	// utterance to check if it has a noun style
    public void checkWord(ArrayList<String> taggedUtterance,
			  boolean isEnglish_,
			  boolean isChinese_)
	{
		// loop the arraylist
		for(Iterator<String> iterator = taggedUtterance.iterator(); iterator.hasNext();)
		{
			String tagged = iterator.next();
			String [] tagsplit = tagged.split("\\s+");
			for(int i=0;i<tagsplit.length;i++)
			{
				String verbToken = tagsplit[i];		
				// get the word
				String word = ParseTools.getWord(verbToken).toLowerCase().trim();
				word = ParseTools.removePunctuation(word, isEnglish_, isChinese_).trim();
				//if((word.indexOf("?")!=-1) || (word.indexOf(".")!=-1) || (word.indexOf(",")!=-1))
					//word = word.substring(0, word.length()-1);
				// get the tag
				String tag = ParseTools.getTag(verbToken);
				// this word is verb
				if(ParseTools.isVerb(tag))
				{
					if(!verbList.contains(word))
						verbList.add(word);
				}
			}
		}
		
		// loop the word Iterator to grab each verb
		for(Iterator<String> wordIterator = verbList.iterator(); wordIterator.hasNext();)
		{
			ArrayList<Integer> repeatMentions = new ArrayList<Integer>();
			boolean nounpattern = false;
			String verb = wordIterator.next();
//			String pattern1 = verb + "/" + N1;
//			String pattern2 = verb + "/" + N2;
//			String pattern3 = verb + "/" + N3;
//			String pattern4 = verb + "/" + N4;
			for(int index = 0; index < taggedUtterance.size(); index++)
			{
				String taggedUtt = taggedUtterance.get(index);
				StringTokenizer tokenizer = new StringTokenizer(taggedUtt);
				while(tokenizer.hasMoreTokens())
				{
					String taggedV = tokenizer.nextToken();
//					System.out.print("Turn No "+(index+1)+" : " + taggedV);
					String wordV = ParseTools.getWord(taggedV).toLowerCase().trim();			
					wordV = ParseTools.removePunctuation(wordV, isEnglish_, isChinese_).trim();
					//System.out.println("\t" + wordV);
					if(verb.equalsIgnoreCase(wordV))
					{
						String Ntag = ParseTools.getTag(taggedV);
//						System.out.println("VERB:" + Ntag);
						if(Ntag.equalsIgnoreCase(N1) || Ntag.equalsIgnoreCase(N2)|| Ntag.equalsIgnoreCase(N3)|| Ntag.equalsIgnoreCase(N4))
						{
							if(wordV.equalsIgnoreCase("torture"))
							{
								System.out.println("touture found!");
								//System.exit(0);
							}
							nounpattern = true;
							break;
						}
					}
					// this verb has a noun style
//					if((tagged.equalsIgnoreCase(pattern1)) || (tagged.equalsIgnoreCase(pattern2)) || (tagged.equalsIgnoreCase(pattern3)) || (tagged.equalsIgnoreCase(pattern4)))
//					{
//						nounpattern = true;
//						break;
//					}
				}
			}
//			if(!verbList.contains("torture"))
//			{	System.out.println("NONONONONO"); System.exit(0);}
			
			if(nounpattern)
			{
				// find this word in untagged utterances
				ArrayList<String> utterances = NounList.getUtterances();
				for(int i = 0; i < utterances.size(); i++)
				{
					StringTokenizer tokenizer2 = new StringTokenizer(utterances.get(i));
					while(tokenizer2.hasMoreTokens())
					{
						String utt = tokenizer2.nextToken();
						if(utt.equalsIgnoreCase(verb))
						{
							// get the turn numbers
							repeatMentions.add(i+1);
						}
					}
				}
				verbToken vt = new verbToken(verb,repeatMentions);
				// create the verb token list
				tokenList.add(vt);
				System.out.println("Token List created: "+vt.getWord());
			}
		}
	}
	
	// get the token list
	public ArrayList<verbToken> getTokenList()
	{
		return tokenList;
	}
	
// end of class
}
