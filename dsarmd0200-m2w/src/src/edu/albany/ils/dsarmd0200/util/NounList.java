package edu.albany.ils.dsarmd0200.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.io.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import java.sql.SQLException;
/**
 *
 * Author: Ken Stahl
 *
 */

//NounList contains various lists...
//utterances = the spoken line of chat (content between <turn> and </turn>)
//speakers = speaker of each utterance.  speakers.get(50) is the owner of utterances.get(50)
//turn_no = turn_no part of the <turn> tag
//nouns = arraylist of nountokens created
//created boolean prevents reusing the class
//commacts and links are from the <turn> tag again
//xmlparse contains the entire log file in parsed arraylists


//NOTE: there are two procedures for creating a nounlist...one of them is
//createAnnotatedList() and the other is createList()
//first one is for annotated summarizing and the second is for automated summarizing

public class NounList {
	private static ArrayList <String> utterances;
	private ArrayList <String> speakers;
	private ArrayList <String> turn_no;
	private HashMap <String, Integer> speakTokens;
	private HashMap<Integer,PronounResolution2> pronounTargets;
	private Wordnet wrdnt;
	public ArrayList <NounToken> nouns; // creatlist
        private int sizeofThP = 0;
	boolean created;
	private ArrayList <String> commacts;
	private ArrayList <String> links;
	private XMLParse xp;
	private PhraseCheck pc;
        private ArrayList<Utterance> utts_;

        
        private boolean isUrdu_ = false;
        private String urdu_path = "";
    
        //m2w: chinese. 5/17/11 12:23 PM
        private boolean isChinese_ = false;
        private boolean isEnglish_ = false;
        private ChineseWordnet cnwn = new ChineseWordnet("mysql", "localhost", "3306", "atur");
        
	//NOTE: createlist is the automated list.  It tags and creates noun phrases
	//makeAnnotatedList is the annotated list.  It parses annotated tags and creates
	//Noun Tokens from them

    public NounList(XMLParse x, Wordnet wparam, PhraseCheck check, ArrayList utts_){
		nouns = new ArrayList<NounToken>();
		speakTokens = new HashMap<String, Integer>();
		pronounTargets = new HashMap<Integer, PronounResolution2>();
		created = false;
		utterances = x.getUtterances();
		speakers = x.getSpeakers();
		wrdnt = wparam;
		commacts = x.getCommActs();
		links = x.getCommActLinks();
		turn_no = x.getTurnNo();
		xp = x;
		this.pc = check;
		this.utts_ = utts_;
		
	}
    public NounList(ArrayList <String> uparam, ArrayList <String> sparam,
	Wordnet wparam, ArrayList <String> cmacts, ArrayList <String> lnks,
		    ArrayList <String> tn, PhraseCheck phr_ch, XMLParse x){
        nouns = new ArrayList<NounToken>();
        speakTokens = new HashMap<String, Integer>();
        pronounTargets = new HashMap<Integer, PronounResolution2>();
        created = false;
        utterances = uparam;
        speakers = sparam;
        wrdnt = wparam;
        commacts = cmacts;
        links = lnks;
        turn_no = tn;
	pc = phr_ch;
	xp = x;
    }

    public void countThP() {
        for (int i = 0; i < nouns.size(); i++) {
            NounToken noun = nouns.get(i);
            if (isThP(noun.getWord())) {
                sizeofThP++;
            }
        }
        //System.out.println("sizeofThP: " + sizeofThP);
        //System.out.println("spkears: " + speakers);
    }

    public boolean isThP(String word) {
        //System.out.println("word: " + word);
        String[] ws = word.split("[\\W]+");
        for (int i = 0; i < ws.length; i++) {
            //System.out.println("w: " + ws[i]);
            String wl = ws[i].toLowerCase();
            if (!speakers.contains(wl)) {
                if (GenderCheck.isThPP(wl) ||
                    GenderCheck.isName(wl)) {
                    //System.out.println("find a ThP: " + wl);
                    return true;
                }
            }
        }
        return false;
    }
    public boolean isThPrp(String word) {
        //System.out.println("word: " + word);
        String[] ws = word.split("[\\W]+");
        for (int i = 0; i < ws.length; i++) {
            //System.out.println("w: " + ws[i]);
            String wl = ws[i].toLowerCase();
            if (!speakers.contains(wl)) {
                if (GenderCheck.isThPP(wl)) {
                    //System.out.println("find a ThP: " + wl);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPerson(String word) {
        //System.out.println("word: " + word);
        String[] ws = word.split("[\\W]+");
        for (int i = 0; i < ws.length; i++) {
            //System.out.println("w: " + ws[i]);
            String wl = ws[i].toLowerCase();
            if (!speakers.contains(wl)) {
                if (GenderCheck.isName(wl)) {
                    //System.out.println("find a ThP: " + wl);
                    return true;
                }
            }
        }
        return false;
    }

    //added at 03/28/11 by TL
    public boolean isSpeaker(String word) {
        //System.out.println("word: " + word);
        String[] ws = word.split("[\\W]+");
        for (int i = 0; i < ws.length; i++) {
            //System.out.println("w: " + ws[i]);
            String wl = ws[i].toLowerCase();
            if (speakers.contains(wl)) {
		return true;
            }
        }
        return false;
    }


    public int sizeofThP() { return sizeofThP; }

    public static boolean isAThP(String word, ArrayList spks) {
        //System.out.println("word: " + word);
        String[] ws = word.split("[\\W]+");
        for (int i = 0; i < ws.length; i++) {
            //System.out.println("w: " + ws[i]);
            String wl = ws[i].toLowerCase();
            if (!spks.contains(wl)) {
                if (GenderCheck.isThPP(wl) ||
                    GenderCheck.isName(wl)) {
		    //System.out.println("find a ThP: " + wl);
		    return true;
		}
	    }
	}
        return false;
    }

	//using this class for holding annotated noun phrases and chains
	public void createAnnotatedList(){
	   	if (!created){
			HashMap <String, Integer> targetIDs = new HashMap<String, Integer>();
			ArrayList <String> posorig = xp.getPOSOrigin();
			ArrayList <String> poses = xp.getPOS();
			String noun = "NN"; String pronoun = "PRP"; int idd = -1;
			for (int i = 0; i < utterances.size(); i++){
				String posorg = posorig.get(i);
				String pos = poses.get(i);
				String speaker = speakers.get(i);
				String turn_num = "";
				int turnNum = Integer.parseInt(turn_no.get(i));
				String [] orgbrk = posorg.split(";");
				for (int j = 1; j < orgbrk.length; j++){
					//System.out.println(orgbrk[j]);
					int place = orgbrk[j].indexOf("(");
					turn_num = "";
					for (int z = place + 1; z < orgbrk[j].length() && orgbrk[j].charAt(z) != ')'; z++)
						turn_num += orgbrk[j].charAt(z);
					String word = orgbrk[j].substring(0, place);
					idd++;
					targetIDs.put(turn_num, idd);
					//System.out.print("w:" + word + " tg=" + noun + " spk=" + speaker);
					//System.out.println(" turn_no=" + turnNum + " idd=" + idd);
					NounToken newToken = new NounToken(word, noun, speaker, turnNum, idd, wrdnt);
					nouns.add(newToken);
				}
				String [] posbreak = pos.split(";");
				for (int j = 1; j < posbreak.length; j++){
					//System.out.println(posbreak[j]);
					int place = posbreak[j].indexOf("(");
					String word = posbreak[j].substring(0, place);
					turn_num = "";
					for (int z = place + 1; z < posbreak[j].length() && posbreak[j].charAt(z) != ')'; z++)
						turn_num += posbreak[j].charAt(z);
					boolean isPronoun = (posbreak[j].contains("pronoun"));
					if (isPronoun){
						idd++;
						int targetid = -1;
						System.out.println("This is the turn_num " + turn_num);
						if (turn_num.length() > 0) {
							if (targetIDs.containsKey(turn_num)){
								targetid = targetIDs.get(turn_num);
							} else
								System.out.println("==========" + posbreak[j]);
						}
						System.out.println("THIS IS THE TARGET: " + targetid);
						NounToken newToken = new NounToken(word, pronoun, speaker, turnNum, idd, wrdnt);
						NounToken temp = getTokenWithID(targetid);
						System.out.println(temp);
					}
				}
			}
		} else{
			System.err.println("Please use a different instance to create a list.");
		}
		
	}

	public int numberOfNouns(){
		return nouns.size();
	}

	public boolean valid(String word){
		//check if all tokens in a noun phrase are either a word or a name
		return ParseTools.isWord(word);
	}

	public int numberOfUniqueNouns(){
		int retval = 0;
		for (int i = 0; i < nouns.size(); i++)
			if (nouns.get(i).firstMention())
				retval++;
		return retval;
	}

	public void printUniqueNouns(){
		int count = 0;
		for (int i = 0; i < nouns.size(); i++){
			NounToken temp = nouns.get(i);
			if (temp.firstMention()){
			    //System.out.println(temp.getWord() + " - turn " + temp.getTurnNo());
				count++;
			}
		}
		//System.out.println(count + " found.");
	}

	public void printNounsWithSubsequentMentions(){
		System.out.println("Nouns with subsequent mentions:\n");
		int total = 0;
		for (int i = 0;i < nouns.size(); i++){
			NounToken nt = nouns.get(i);
			if (nt.firstMention() && nt.isNoun() && nt.anySubsequentMentions()){
			    //System.out.println(nouns.get(i).getWord() + ", ID = " + nouns.get(i).getID()
			    //+ ", # of mentions: " + nt.numberOfMentions());
				total++;
			}
		}
		//System.out.println("Total: " + total);
	}

	//keeps following subsequent mention chains (pronouns) back to the root
	public NounToken findTarget(int IDparam){
		NounToken retval = getTokenWithID(IDparam);
		while (!retval.firstMention())
			retval = getTokenWithID(retval.getRefInt());
		return retval;
	}

	public void printRefferals(){
		for (int i = 0; i < nouns.size(); i++){
			NounToken nt = nouns.get(i);
			if (!nt.firstMention()){
				System.out.println(nt);
			}
		}
	}

	public ArrayList <NounToken> getSubsequentMentions(NounToken head){
            int IDparam = head.getID();
		ArrayList <NounToken> retval = new ArrayList<NounToken>();
		for (int i = 0; i < nouns.size(); i++){
			NounToken temp = nouns.get(i);
			if (temp.getRefInt() == IDparam) {
                                if (!head.containsRef(temp.getID())){
                                    System.out.println("head " + head.getWord() + " not contains ref: " + temp.getWord() + " " + temp.getID());
                                }
				retval.add(temp);
                        }
		}
		return retval;
	}

	//for auto-evaluation...comparison with human annotation
	//this returns an arraylist of chains
	public ArrayList <AChain> getChains(){
		ArrayList <AChain> retval = new ArrayList<AChain>();
		for (int i = 0; i < nouns.size(); i++)
		{
			NounToken temp = nouns.get(i);
			//filter the topic
			if(!TopicFilter.needFiltered(temp.getWord()))
			{
				if (temp.firstMention())
				{
					String word = temp.getWord();
					int loc = temp.getTurnNo();
					String target = "";
					AChain newNode = new AChain();
					// 1: root word
					// 2. turn number of root word
					// 3. reference word
					// 4. turn number of reference word
					newNode.add(word, loc, target, -1);
				
					ArrayList <NounToken> submen = getSubsequentMentions(temp);
					for (int j = 0; j < submen.size(); j++)
					{
						NounToken curnoun = submen.get(j);
						String word2 = curnoun.getWord();
						int loc2 = curnoun.getTurnNo();
						newNode.add(word2, loc2, word, loc);
					}
					retval.add(newNode);
				}
			}
		}
		return retval;
	}

	//convenient tabbed output for chains
	public void printChains(){
		for (int i = 0; i < nouns.size(); i++){
			NounToken temp = nouns.get(i);
			if (temp.firstMention()){
				System.out.println(temp.getTurnNo() + ": " + temp.getWord());
				for (int j = i + 1; j < nouns.size(); j++){
					NounToken temp2 = nouns.get(j);
					if (temp2.getRefInt() == temp.ID){
						System.out.println("\t" + temp2.getTurnNo() + ": " + temp2.getWord());
					}
				}
			}
		}
	}

	//just prints the noun phrases in the order they appeared
	public void printList(){
		System.out.println("List of nouns\n-------------");
		if (nouns.size() < 1)
			System.out.println("*empty*");
		else for (int i = 0; i < nouns.size(); i++){
			NounToken nt = nouns.get(i);
			System.out.println(nt.toString());
		}
	}

	//this is where noun phrases are defined...nouns next to nouns will be combined
	//connectors can be specified such as "of" ie. United States of America...
	//look in englishparsetools.txt -> that would be tokenconnectors section
	public ArrayList<String> getTokens(String [] tags)
	{
		ArrayList<String> retval = new ArrayList<String>();
		for (int i = 0; i < tags.length; i++)
		{
			String tag = ParseTools.getTag(tags[i]);
			String word = ParseTools.getWord(tags[i]).toLowerCase().trim();
			//remove punctuations
			word = ParseTools.removePunctuation(word, isEnglish_, isChinese_); //add isEnglish and isChinese 06/14/2011
			boolean p = ParseTools.isPronoun(tag);
			boolean n = ParseTools.isNoun(tag);
			// this word is a pronoun
			if (PronounFormMatching.isWordPronoun(word))
			{
				p = true; n = false;
				tag = "PRP"; tags[i] = word + "/" + tag;
			}
			// tags[i] = word/PRP
			// if it is noun, find the phrase-1
			if (n)
			{
				String cmptag = "";
				boolean cont = true;
				for (int j = i + 1; cont && j < tags.length; j++)
				{
					// cmptag is the tag of afterward word
					cmptag = ParseTools.getTag(tags[j]).trim();
					// word2 is the afterward word
					String word2 = ParseTools.getWord(tags[j].trim());
					boolean nn = ParseTools.isNoun(cmptag);
					boolean tc = ParseTools.isTokenConnector(word2);
					if (nn || tc)
					{
						// the word is noun
						if (nn)
						{
							word2 = ParseTools.removePunctuation(word2, isEnglish_, isChinese_);
							if (word2.length() > 0)
								word += " " + word2;
							i++;
						}
						// the word is a token connector
						else 
						{
							i++;  //move up even if not using the connector
							word2 = ParseTools.removePunctuation(word2, isEnglish_, isChinese_);
							if (word2.length() > 0 && (j + 1) < tags.length)
							{
								String word3 = ParseTools.getWord(tags[j + 1]).toLowerCase().trim();
								String word3tag = ParseTools.getTag(tags[j + 1]);
								boolean nouncheck = ParseTools.isNoun(word3tag);
								if (nouncheck)
								{
									word += " " + word2 + " " + word3;
									j += 2;
									i += 2;  //connector used, move to word after connecting noun
								}
							}
						}
					} 
					else cont = false;
				}
				//System.out.println("word: " + word);
				// save all pronoouns and nouns and noun phrases in an ArrayList
				
				//check proper phrase
				if(!pc.check(word))
				{
					//break it
					StringTokenizer st = new StringTokenizer(word);
					while(st.hasMoreTokens())
					{
						String w = st.nextToken();
						word = w + "/" + tag;
						retval.add(word);
					}
					
				}
				else
				{
					word += "/" + tag;
					retval.add(word);				
				}
				//System.out.println("retval: " + retval);
			} 
			else if (p) 
			{
				retval.add(tags[i]);
			}
		}
		return retval;
	}

    public void setUrdu (boolean isUrdu)
    { isUrdu_ = isUrdu; }
    public void setUrduPath (String path)
    { urdu_path = path; } 
    
    //m2w : Chinese 5/17/11 12:27 PM
    public void setChinese (boolean isChinese)
    { isChinese_ = isChinese; }
    
    public void setEnglish (boolean isEnglish)
    { isEnglish_ = isEnglish; }
	//create the automated list with tagging and simple heuristics
	// publc void createList(FindVerb fv)
	public void createList(FindVerb fv){
	    if (isEnglish_)
		{
                    System.out.println("in here");
		if (!created){
			for (int i = 0; i < utterances.size(); i++){
			    Utterance utt_ = utts_.get(i);
				String utterance = utterances.get(i);
				String speaker = speakers.get(i);
				if (!speakTokens.containsKey(speaker))
					speakTokens.put(speaker, speakTokens.size());
				// remove emoticons in each utterance
				String noEmotes = ParseTools.removeEmoticons(utterance);
//				StanfordPOSTagger.initialize();
				// tag the utterance
				String tagged = StanfordPOSTagger.tagString(noEmotes).trim();
				//System.out.println("Turn "+(i+1)+": "+tagged);
				fv.add(tagged);
				utt_.setTaggedContent(tagged);
				// split to X/Xtag
				String [] tagsplit = tagged.split("\\s+");
				// get words and phrases in one utterance, saves WORDS WITH TAGS from each utterance
				// into arraylist tokens Eg. A/Atag B/Btag
				ArrayList <String> tokens = getTokens(tagsplit); int count = 0;
				//System.out.println("***" + (i+1) + "***");
				for (int j = 0; j < tokens.size(); j++){
					String token = tokens.get(j);
					//get the word
					String word = ParseTools.getWord(token).trim();
					word = word.replaceAll("\\s+", " ");
					//get the tag
					String tag = ParseTools.getTag(token);
					int ID = numberOfNouns();
					int turnno = ParseTools.getTurnNo(turn_no.get(i));
					
					// Find appropriate verbs and if it is first mentioned or it has been mentioned in nouns
					// in ParseTool class
					
					// extract all nouns and analyse
					if (ParseTools.isNoun(tag)){
						// 1st param: word
						// 2nd param: tag
						// 3rd param: speaker
						// 4th param: turn no
						// 5th param: wordnet object
						NounToken nt =
								new NounToken(word, tag, speaker, turnno, ID, wrdnt);
//								new NounToken(word, tag, speaker, i + 1, ID, wrdnt);
						if ((!ParseTools.isBadNoun(word)) && ParseTools.isWord(word)){
							// resolve noun token
							resolve(nt);
							if (nt.firstMention()){
								String ft = Integer.toString(i+1) + ".";
								ft += Integer.toString((++count));
								nt.setFloatTag(ft);
							}
							nouns.add(nt);
						}
					} else if (ParseTools.isPronoun(tag) && !ParseTools.ignorePronoun(word)){
						NounToken nt = new NounToken(word, tag, speaker, i + 1, ID, wrdnt);
						if (!ParseTools.ignorePronoun(word)) { //add bracket 06/17 by TL
							resolve(nt);
							nouns.add(nt);
						}
					}
				}
			}
			created = true;
		}else System.err.println("Please use another instance to create a list.");
		}
            //m2w: chinese 5/17/11 12:41 PM
            else if(isChinese_){
                if (!created){
			for (int i = 0; i < utterances.size(); i++){
			    Utterance utt_ = utts_.get(i);
				String utterance = utterances.get(i);
				String speaker = speakers.get(i);
				if (!speakTokens.containsKey(speaker))
					speakTokens.put(speaker, speakTokens.size());
				// remove emoticons in each utterance
//                                System.out.println(utterance);
				String noEmotes = ParseTools.removeEmoticons(utterance);
//                                System.out.println(noEmotes);
//				StanfordPOSTagger.initializeChinese();
				// tag the utterance
//                                System.out.println("inited chinese");
				String tagged = StanfordPOSTagger.tagChineseString(noEmotes).trim();
				//System.out.println("Turn "+(i+1)+": "+tagged);
				fv.add(tagged);
				utt_.setTaggedContent(tagged);
				// split to X/Xtag
				String [] tagsplit = tagged.split("\\s+");
				// get words and phrases in one utterance, saves WORDS WITH TAGS from each utterance
				// into arraylist tokens Eg. A/Atag B/Btag
				ArrayList <String> tokens = getTokens(tagsplit); int count = 0;
				//System.out.println("***" + (i+1) + "***");
				for (int j = 0; j < tokens.size(); j++){
					String token = tokens.get(j);
//                                        System.out.println(token);
					//get the word
					String word = ParseTools.getWord(token).trim();
					word = word.replaceAll("\\s+", " ");
//                                        System.out.println(word);
                                        
					//get the tag
					String tag = ParseTools.getTag(token);
					int ID = numberOfNouns();
					int turnno = ParseTools.getTurnNo(turn_no.get(i));
					
					// Find appropriate verbs and if it is first mentioned or it has been mentioned in nouns
					// in ParseTool class
					
					// extract all nouns and analyse
					if (ParseTools.isNoun(tag)){
						// 1st param: word
						// 2nd param: tag
						// 3rd param: speaker
						// 4th param: turn no
						// 5th param: wordnet object
						NounToken nt =
								new NounToken(word, tag, speaker, turnno, ID, getCnwn());
//								new NounToken(word, tag, speaker, i + 1, ID, wrdnt);
//						if ((!ParseTools.isBadNoun(word)) && ParseTools.isWord(word)){
						if (!ParseTools.isBadNoun(word)) { //modified by 06/20/2011 TL
							// resolve noun token
							resolveCHN(nt);
							if (nt.firstMention()){
								String ft = Integer.toString(i+1) + ".";
								ft += Integer.toString((++count));
								nt.setFloatTag(ft);
							}
							nouns.add(nt);
						}
					} else if (ParseTools.isPronoun(tag) && !ParseTools.ignorePronoun(word)){
					    //System.out.println("find a pronoun: " + word);
						NounToken nt = new NounToken(word, tag, speaker, i + 1, ID, getCnwn());
						if (!ParseTools.ignorePronoun(word) &&
						    !ParseTools.isBadNoun(word)) { //add bracket 06/17 by TL, add isBadNoun
						    resolveCHN(nt);
						    //System.out.println("after resolve: " + nt);
						    nouns.add(nt);
						}
					}
				}
			}
			created = true;
		}else System.err.println("Please use another instance to create a list.");
            }
	    else if (isUrdu_)
		{
		    if (!created)
			{
			    ArrayList tosend = new ArrayList ();
			    for (int i = 0; i < utterances.size(); i++)
				{
				    Utterance utt_ = utts_.get(i);
				    String utterance = utterances.get(i);
				    String speaker = speakers.get(i);
				    if (!speakTokens.containsKey(speaker))
					speakTokens.put(speaker, speakTokens.size());
				    // remove emoticons in each utterance
				    String noEmotes = ParseTools.removeEmoticons(utterance);
				    if (noEmotes.equals (""))
					noEmotes = "(No Utterance)";
				    tosend.add (noEmotes);
				}
			    if (!urdu_path.equals ("") && tosend.size () > 0)
				{
				    try 
					{
					    PrintWriter outFile = new PrintWriter
						(new OutputStreamWriter
						 (new FileOutputStream (new File (urdu_path + "/input/", "totag.txt") )));
					    for (int i = 0; i < tosend.size (); i++)
						outFile.println ((String) tosend.get (i));
					    outFile.close ();
					    //call the tagger   
					    System.err.println ("Calling urdu pos tagger");
					    String cmd = "/usr/bin/python " + urdu_path + "/tagger.py " + urdu_path + "/input/ " + urdu_path + "/input/totag.txt " + urdu_path + "/output/out.txt";
					    Process urdutagger = null;
					    urdutagger = Runtime.getRuntime ().exec (cmd);
					    InputStreamReader myIStreamReader = new InputStreamReader (urdutagger.getInputStream ());
					    BufferedReader bufferedReader = new BufferedReader (myIStreamReader);
					    
					    String line;
					    while ((line = bufferedReader.readLine ()) != null)
						{
						    System.err.println (line);
						}
					    InputStreamReader myErrorStreamReader = new InputStreamReader (urdutagger.getErrorStream ());
					    BufferedReader bufferedErrorReader = new BufferedReader (myErrorStreamReader);
					    String eLine;
					    while ((eLine = bufferedErrorReader.readLine ()) != null)
						{
						    System.err.println (eLine);
						}
					    
					    if (urdutagger.waitFor () != 0)
						System.err.println ("ERROR" + urdutagger.exitValue ());
					    else 
						{
						    System.err.println ("tagged urdu file");
						    BufferedReader inFile = new BufferedReader
							(new InputStreamReader
							 (new FileInputStream (new File (urdu_path + "/output/", "out.txt"))));
						    String strline = inFile.readLine ();
						    int i = 0;
						    while (strline != null)
							{
							    
							    Utterance utt_ = (Utterance) utts_.get (i);
							    utt_.setTaggedContent (strline);
							    //System.err.println ("Tagged content" + strline);
							    fv.add (strline);
							    String speaker = speakers.get(i);
							    // split to X/Xtag
							    String [] tagsplit = strline.split("\\s+");
							    // get words and phrases in one utterance, saves WORDS WITH TAGS from each utterance
							    // into arraylist tokens Eg. A/Atag B/Btag
							    ArrayList <String> tokens = getTokens(tagsplit); int count = 0;
							    for (int j = 0; j < tokens.size(); j++)
								{
								    String token = tokens.get(j);
								    //get the word
								    String word = ParseTools.getWord(token).trim();
								    word = word.replaceAll("\\s+", " ");
								    //get the tag
								    String tag = ParseTools.getTag(token);
								    int ID = numberOfNouns();
								    int turnno = ParseTools.getTurnNo(turn_no.get(i));
								    
								    // Find appropriate verbs and if it is first mentioned or it has been mentioned in nouns
								    // in ParseTool class
								    
								    // extract all nouns and analyse
								    if (ParseTools.isNoun(tag))
									{
									    // 1st param: word
									    // 2nd param: tag
									    // 3rd param: speaker
									    // 4th param: turn no
									    // 5th param: wordnet object
									    NounToken nt = new NounToken(word, tag, speaker, turnno, ID, wrdnt);
									    nt.makeNewRef (ID);
									    nt.setReference (nt.getID ());
									    nouns.add(nt);
									} 
								    else if (ParseTools.isPronoun(tag) && !ParseTools.ignorePronoun(word))
									{
									    NounToken nt = new NounToken(word, tag, speaker, i + 1, ID, wrdnt);
									    nt.makeNewRef (ID);
									    nt.setReference (nt.getID ());
									    nouns.add(nt);
									}
								}
							    strline = inFile.readLine ();
							    if (strline == null)
								break;
							    
							}
							inFile.close ();

						}
					    myIStreamReader.close ();
					    

					}
				    catch (Exception ee)
					{
					    ee.printStackTrace ();
					}
				}
			    created = true;
			}
		    else 
			System.err.println ("Please use another instance to create a list.");
		}
	}

	//link a word to what it's meant to be referring to
	public void resolve(NounToken param)
	{
		boolean cont = true;
		// get word itself
		String word1 = param.getWord();
		/*
		if (param.getID() == 113) {
		    System.out.println("param: " + param.getWord() + " === param turnno: " + param.getTurnNo());
		}
		if (word1.trim().equalsIgnoreCase("jerry")) {
		    System.out.println("param.getID: " + param.getID() + " === param turnno: " + param.getTurnNo());
		    //System.out.println("nt.getID: " + nt.getID() + " === turnno: " + nt.getTurnNo());
		}
		//if (word1.trim().equalsIgnoreCase("Dr. Bodvarsson")) {
		System.out.println("param.getID: " + param.getID() + " === param content: " + param.getWord() + " ==== param turnno: " + param.getTurnNo());
		*/
		    //System.out.println("nt.getID: " + nt.getID() + " === turnno: " + nt.getTurnNo());
		    //}
		word1 = word1.replaceAll("\\s+", " ");
		// get tag
		String tag = param.getTag().toUpperCase();
		ArrayList <String> synonyms = new ArrayList<String>();
		//tag = "" + tag.charAt(0);
		// if it is noun, find HiSynonyms
		if (tag.charAt(0) == 'N')
		{
			tag = "noun";
			synonyms = wrdnt.getHiSynonym(word1, tag);
			//System.out.println("The word: " + word1);
			/*
			for (int i = 0; i < synonyms.size(); i++)
				System.out.println(synonyms.get(i));
			*/
			int num = numberOfNouns();
			// find all other repetition words, update boolean rep 
			for (int i = 0; cont && i < num; i++)
			{
				String word2 = nouns.get(i).getWord();
				boolean rep = word1.equalsIgnoreCase(word2);
				boolean syn = false;
				if (!rep)
				{
					//find all other words are synonyms of the original word
					syn = wrdnt.isASynonym(word1, word2, tag);
					//syn = synonyms.contains(word1);
				}
				if (syn || rep)
				{
					// get that word
					NounToken nt = nouns.get(i);
					//modified by Ting Liu for submentions
					while (nt.getRefInt() != -1) {
					    nt = nouns.get(nt.getRefInt());
					}
					// make a new reference to the original word, save the reference words's ID in
					// subsequentMentions arraylist in NounToken.java
					/*
					if (word1.trim().equalsIgnoreCase("jerry")) {
					    System.out.println("param.getID: " + param.getID() + " === param turnno: " + param.getTurnNo());
					    System.out.println("nt.getID: " + nt.getID() + " === turnno: " + nt.getTurnNo());
					}
					*/
					nt.makeNewRef(param.getID());//modified by Ting 10/04 it was ID before
					// vice versa
					param.setReference(nt.getID());
					cont = false;
				}
			}
			// do pronoun resolution
		} 
		else 
		{
		    //System.out.println("it's a pronoun");
			PronounResolution2 pr2 = new PronounResolution2(param,turn_no,commacts,links,nouns,utterances);
			
			//resolve pronouns here
			//get arraylist of nountokens...implement comm.acts
			//2nd parameter of pronounresolution should be an arraylist of
			//nountokens
			// get turn no's lower bound
			//int lowbound = Integer.parseInt(turn_no.get(0));
			// get turn no's upper bound
			//int upbound = Integer.parseInt(turn_no.get(turn_no.size() - 1));
			// get this turn no of this word
			//int uttNum = param.getTurnNo();
			//String comm_act = "";
			//if ((uttNum >= lowbound) && (uttNum <= upbound))
				//comm_act = commacts.get(uttNum - 1);
			// resolute pronoun, find "continuation" and "respose to"
			//PronounResolution pr;
			//if (comm_act.equalsIgnoreCase("continuation-of") ||
					//comm_act.equalsIgnoreCase("response-to"))
			//{
				//System.out.println(links.get(uttNum - 1));
				// get the link number
				//int lnk = getLink(links.get(uttNum - 1));
				// 1st param: link number
				// 2nd param: this word's ID
				//ArrayList<NounToken> temp = arrangeNouns(lnk, param.getID());
				// temp: an arraylist of nouns before the nountoken of the same turn number and nountokens in the "link-to line"
				//if (temp.size() > 0)
					//temporarily checking if nouns is better than temp
					//pr = new PronounResolution(word1, temp);
				//else
					//pr = new PronounResolution(word1, nouns);
			//} 
			//else 
			//{
				//pr = new PronounResolution(word1, nouns);
			//}
			pronounTargets.put(param.getID(), pr2);
			NounToken tempToken = null;
			int tempID = pr2.getTargetID();
			if(tempID!=-1)
			{
				tempToken = this.getTokenWithID(tempID);
			}
			while (tempToken != null && tempToken.getReferenceNumber() != -1) {
			    tempToken = nouns.get(tempToken.getReferenceNumber());
			}
			//modified by Ting
			if (tempToken != null) {
			    param.setReference(tempToken.getID());
			    tempToken.makeNewRef(param.getID());//modified by Ting 10/04 it was ID before param.getID());
			}
			/*
			    
			//check if it still has reference
			if(tempToken!=null && tempToken.getReferenceNumber()!=-1)
			{
				param.setReference(tempToken.getReferenceNumber());
			}
			else
			{
				param.setReference(pr2.getTargetID());
			}
			//System.out.println(pr.getTargetID());
			if(pr2.getTargetID()!=-1)
			{
				NounToken nt = nouns.get(pr2.getTargetID());
				nt.makeNewRef(param.getID());
			}
			*/
		}
	}
        
        /**
         * m2w: for chinese resolving
         * @param param 
         * @date 7/11/11 3:54 PM
         */
        public void resolveCHN(NounToken param){
		boolean cont = true;
		// get word itself
		String word1 = param.getWord();
		
		    //System.out.println("nt.getID: " + nt.getID() + " === turnno: " + nt.getTurnNo());
		    //}
		word1 = word1.replaceAll("\\s+", " ");
		// get tag
		String tag = param.getTag().toUpperCase();
		ArrayList <String> synonyms = new ArrayList<String>();
		//tag = "" + tag.charAt(0);
		// if it is noun, find HiSynonyms
		if (tag.charAt(0) == 'N' && !word1.contains("%")){//m2w : added word1 not equals % cuz it stuck at runtime.
			tag = "noun";
                        try{
//                            System.out.println("word: >" + word1 + "<");
                            synonyms = getCnwn().getChineseSynlist(word1);
                        }catch(SQLException e){e.printStackTrace();}
			//System.out.println("The word: " + word1);
			/*
			for (int i = 0; i < synonyms.size(); i++)
				System.out.println(synonyms.get(i));
			*/
			int num = numberOfNouns();
			// find all other repetition words, update boolean rep 
			for (int i = 0; cont && i < num; i++){
				String word2 = nouns.get(i).getWord();
				boolean rep = word1.equalsIgnoreCase(word2);
				boolean syn = false;
				if (!rep){
                                    syn = getCnwn().isChineseSyn(word1, word2);
//                                    syn = synonyms.contains(word2);
				}
				if (syn || rep){
					// get that word
					NounToken nt = nouns.get(i);
					//modified by Ting Liu for submentions
					while (nt.getRefInt() != -1) {
					    nt = nouns.get(nt.getRefInt());
					}
					nt.makeNewRef(param.getID());//modified by Ting 10/04 it was ID before
					// vice versa
					param.setReference(nt.getID());
					cont = false;
				}
			}
			// do pronoun resolution
		} 
                
                
//		else 
//		{
//		    //System.out.println("it's a pronoun");
//			PronounResolution2 pr2 = new PronounResolution2(param,turn_no,commacts,links,nouns,utterances);
//			
//			//resolve pronouns here
//			//get arraylist of nountokens...implement comm.acts
//			//2nd parameter of pronounresolution should be an arraylist of
//			//nountokens
//			// get turn no's lower bound
//			//int lowbound = Integer.parseInt(turn_no.get(0));
//			// get turn no's upper bound
//			//int upbound = Integer.parseInt(turn_no.get(turn_no.size() - 1));
//			// get this turn no of this word
//			//int uttNum = param.getTurnNo();
//			//String comm_act = "";
//			//if ((uttNum >= lowbound) && (uttNum <= upbound))
//				//comm_act = commacts.get(uttNum - 1);
//			// resolute pronoun, find "continuation" and "respose to"
//			//PronounResolution pr;
//			//if (comm_act.equalsIgnoreCase("continuation-of") ||
//					//comm_act.equalsIgnoreCase("response-to"))
//			//{
//				//System.out.println(links.get(uttNum - 1));
//				// get the link number
//				//int lnk = getLink(links.get(uttNum - 1));
//				// 1st param: link number
//				// 2nd param: this word's ID
//				//ArrayList<NounToken> temp = arrangeNouns(lnk, param.getID());
//				// temp: an arraylist of nouns before the nountoken of the same turn number and nountokens in the "link-to line"
//				//if (temp.size() > 0)
//					//temporarily checking if nouns is better than temp
//					//pr = new PronounResolution(word1, temp);
//				//else
//					//pr = new PronounResolution(word1, nouns);
//			//} 
//			//else 
//			//{
//				//pr = new PronounResolution(word1, nouns);
//			//}
//			pronounTargets.put(param.getID(), pr2);
//			NounToken tempToken = null;
//			int tempID = pr2.getTargetID();
//			if(tempID!=-1)
//			{
//				tempToken = this.getTokenWithID(tempID);
//			}
//			while (tempToken != null && tempToken.getReferenceNumber() != -1) {
//			    tempToken = nouns.get(tempToken.getReferenceNumber());
//			}
//			//modified by Ting
//			if (tempToken != null) {
//			    param.setReference(tempToken.getID());
//			    tempToken.makeNewRef(param.getID());//modified by Ting 10/04 it was ID before param.getID());
//			}
//			/*
//			    
//			//check if it still has reference
//			if(tempToken!=null && tempToken.getReferenceNumber()!=-1)
//			{
//				param.setReference(tempToken.getReferenceNumber());
//			}
//			else
//			{
//				param.setReference(pr2.getTargetID());
//			}
//			//System.out.println(pr.getTargetID());
//			if(pr2.getTargetID()!=-1)
//			{
//				NounToken nt = nouns.get(pr2.getTargetID());
//				nt.makeNewRef(param.getID());
//			}
//			*/
//		}
	}
        
        
        

	//select a particular noun phrase
	public NounToken getTokenWithID(int ID){
		NounToken retval = null;
		for (int i = 0; retval == null && i < nouns.size(); i++){
			NounToken tmp = nouns.get(i);
			if (tmp.getID() == ID)
				retval = tmp;
		}
		return retval;
	}

	public ArrayList <NounToken> getPronouns(){
		ArrayList <NounToken> retval = new ArrayList<NounToken>();
		for (int i = 0; i < nouns.size(); i++){
			NounToken temp = nouns.get(i);
			if (temp.isPronoun())
				retval.add(temp);
		}
		return retval;
	}

	public void printPronouns(){
		int pCount = 0;
		System.out.println("Printing pronouns");
		for (int i = 0; i < nouns.size(); i++){
			NounToken temp = nouns.get(i);
			if (temp.isPronoun()){
				System.out.println(temp);
				pCount++;
			}
		}
		System.out.println("Pronoun Count = " + pCount);
			
	}

	public void printPronounResolutions(){
		System.out.println("Printing the top 10 candidates for pronouns");
		for (int i = 0; i < pronounTargets.size(); i++){
			PronounResolution2 pr = pronounTargets.get(i);
			//System.out.println(pr.toString());
		}
	}
    /*
	public void annotate(){
		//replaces human annotation (or annotation from a properly formatted log file)
		//with automated annotation.

		//note: the filename can be annotated xml, doesn't matter.  this will
		//overwrite any values in the tags and print to a new file.
		String filename = xp.getFileName();
		filename = filename.replace(".xml", "_auto.xml");
		File outfile = new File(filename);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
			String temp = xp.getRawLine(0);
			bw.write(temp + "\n"); bw.flush();
			System.out.println(temp);
			int rawsize = xp.rawSize();
			for (int i = 1; i < (rawsize - 1); i++){  //rawsize - 1 is </Dialogue>
				String templine = xp.getRawLine(i);
				int posplace = templine.indexOf("pos=\"") + 5;
				String outline = templine.substring(0, posplace);
				int posplace2 = templine.indexOf("\" pos_count=\"");
				outline += templine.substring(posplace2, posplace2 + 12) + "\"";
				posplace2 = templine.indexOf("\" pos_origin=\"");
				outline += templine.substring(posplace2, posplace2 + 14);
				int posplace3 = templine.indexOf("\" speaker=\"");
				outline += templine.substring(posplace3);
				//System.err.println(outline);
				String origin = "pos_origin=\"";
				String neworigin = origin;
				String pos = "pos=\"";
				String newpos = pos;
				ArrayList <NounToken> n = getUtteranceNouns(i);
				int num = n.size(); int poscount = 0;
				for (int j = 0; j < num; j++){
					NounToken tmp = n.get(j);
					if (tmp.firstMention()){ //modify pos_origin
						neworigin += ";" + tmp.getWord() + "(" + tmp.getFloatTag();
						neworigin += ")"; poscount++;
					} else{ //modify pos
						newpos += ";" + tmp.getWord() + "(" + getTargetFloatTag(tmp);
						newpos += ")";
						String separator = "";
						if (tmp.isPronoun())
							separator = "-pronoun-reference-";
						else
							separator = "-repetition-";
						newpos += separator + getTargetWord(tmp) + "(";
						newpos += getTargetFloatTag(tmp) + ")";

					}
				}
				if (poscount > 0){
					String cstring = "pos_count=\"" + Integer.toString(poscount);
					outline = outline.replace("pos_count=\"", cstring);
				}
				outline = outline.replace(origin, neworigin);
				outline = outline.replace(pos, newpos);
				bw.write(outline + "\n"); bw.flush();
				System.out.println(outline);
			}
			System.out.println("</Dialogue>");
			bw.write("</Dialogue>"); bw.flush();
			//System.out.println(xp.getRawLine(rawsize - 1));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
    */
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

	//locate the target and get it's value
	private String getTargetWord(NounToken param){
		String retval = "";
		NounToken target = findTarget(param.getRefInt());
		retval = target.getWord();
		return retval;
	}

	//get the floating point value of the turn number in the nountoken here
	private String getTargetFloatTag(NounToken param){
		String retval = "";
		NounToken target = findTarget(param.getRefInt());
		retval = target.getFloatTag();
		return retval;
	}
	
	public ArrayList<NounToken> getNouns()
	{
		return this.nouns;
	}
	
	public ArrayList<NounToken> getThPNouns()
	{
	    ArrayList thr_p_nouns = new ArrayList();
	    for (int i = 0; i < nouns.size(); i++) {
		NounToken noun = nouns.get(i);
		if (!noun.getWord().equalsIgnoreCase("i") &&
		    !noun.getWord().equalsIgnoreCase("we") &&
		    !noun.getWord().equalsIgnoreCase("our") &&
		    !noun.getWord().equalsIgnoreCase("you") &&
		    !noun.getWord().equalsIgnoreCase("my") &&
		    !noun.getWord().equalsIgnoreCase("your") &&
		    !noun.getWord().equalsIgnoreCase("ourselve") &&
		    !noun.getWord().equalsIgnoreCase("myself") &&
		    !noun.getWord().equalsIgnoreCase("yourself") &&
		    !noun.getWord().equalsIgnoreCase("ours") &&
		    !noun.getWord().equalsIgnoreCase("mine") &&
		    !noun.getWord().equalsIgnoreCase("yours") &&
		    !noun.getWord().equalsIgnoreCase("ours")) {
		    thr_p_nouns.add(noun);
		}
	    }
		return thr_p_nouns;
	}
	
	public static ArrayList<String> getUtterances()
	{
		return utterances;
	}

    public void sortIt() { //based on the size of subsequent mention
        for (int i = 0; i < nouns.size() - 1; i++) {
            NounToken n1 = nouns.get(i);
            for (int j = i + 1; j < nouns.size(); j++) {
                NounToken n2 = nouns.get(j);
                if (n1.sizeOfSubSM() < n2.sizeOfSubSM()) {
                    nouns.set(i, n2);
                    nouns.set(j, n1);
                    n1 = n2;
                }
            }
        }
        //System.out.println("after sort\n: " + nouns);
    }

    /**
     * @return the cnwn
     */
    public ChineseWordnet getCnwn() {
        return cnwn;
    }

    /**
     * @param cnwn the cnwn to set
     */
    public void setCnwn(ChineseWordnet cnwn) {
        this.cnwn = cnwn;
    }
}
