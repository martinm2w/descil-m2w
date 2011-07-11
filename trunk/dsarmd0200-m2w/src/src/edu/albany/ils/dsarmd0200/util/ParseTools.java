package edu.albany.ils.dsarmd0200.util;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import net.didion.jwnl.data.*;
import edu.albany.ils.dsarmd0200.lu.*;

public abstract class ParseTools {
	private static boolean initialized = false;
        private static Wordnet wn = null;
	private static ArrayList <String> emotes;
	private static IndexWord iw;
	private static HashMap <String, Integer> nounFamily;
	private static HashMap <String, Integer> pronounFamily;
	private static HashMap <String, Integer> ignorePronouns;
	private static HashMap <String, Integer> notNouns;
	private static HashMap <String, Integer> nounTokenConnectors;
	private static HashMap <String, Integer> verbFamily;

	// creates hashmaps and saves words in it
	public static void initialize(){
		String location = Settings.getValue(Settings.EMOTES);
		String infile = Settings.getValue("eng_parsetools");
		emotes = new ArrayList<String>();
		nounFamily = new HashMap<String, Integer>();
		pronounFamily = new HashMap<String, Integer>();
		ignorePronouns = new HashMap<String, Integer>();
		notNouns = new HashMap<String, Integer>(64);
		nounTokenConnectors = new HashMap<String, Integer>();
		verbFamily = new HashMap<String, Integer>();
		
		int i = 0;
		File nounfile = new File(infile);
		try{
		    BufferedReader b = new BufferedReader(new FileReader(nounfile));
			String temp = b.readLine();  //read noun family header
			temp = b.readLine(); //read first value
			while (temp.length() > 0){
			    nounFamily.put(temp, i++);
				//nounFamily.put("NN", i);
				//nounFamily.put("NNS", i++);
				//nounFamily.put("NNP", i++);
				//nounFamily.put("NNPS", i++); i = 0;
				temp = b.readLine();
			} //System.err.println("nounFamily size: " + nounFamily.size());
			while (temp.trim().length() == 0)  //added by TL at 06/21/2011
			    temp = b.readLine();  //read pronoun header
			temp = b.readLine();  //read first pronoun
			while (temp.length() > 0){
				//pronounFamily.put("PRP", i);
				//pronounFamily.put("PRP$", i++);
				//pronounFamily.put("WP", i++);
				//pronounFamily.put("WP$", i++); i = 0;
				pronounFamily.put(temp, i++);
				temp = b.readLine();
			} //System.out.println("pronounFamily size: " + pronounFamily.size());
			while (temp.trim().length() == 0) //added by TL 06/21/2011
			    temp = b.readLine();  //read pronoun header
			//temp = b.readLine(); //read pronoun header
			temp = b.readLine(); //read first pronoun
			while (temp.length() > 0){
				//ignorePronouns.put("me", i); ignorePronouns.put("i", i++);
				//ignorePronouns.put("us", i++); ignorePronouns.put("we", i++);
				//ignorePronouns.put("you", i++); ignorePronouns.put("my", i++);
				//ignorePronouns.put("s", i++); i = 0;
				//ignorePronouns.put("", i++);
				ignorePronouns.put(temp, i++);
				temp = b.readLine();
			} //System.out.println("ignorePronouns size: " + ignorePronouns.size());
			while (temp.trim().length() == 0) //added by TL 06/21/2011
			    temp = b.readLine();  //read pronoun header
			temp = b.readLine();  //read token connectors header
			while (temp.length() > 0 && b.ready()){
				nounTokenConnectors.put(temp, i++);
				temp = b.readLine();
			}
			while (temp.trim().length() == 0) //added by TL 06/21/2011
			    temp = b.readLine();  //read pronoun header
			temp = b.readLine();  //read not noun header
			while (temp.length() > 0 && b.ready()){
				temp = b.readLine();
				if (temp.length() > 0)
					notNouns.put(temp, i++);
				//notNouns.put("yet", i); notNouns.put("yep", i++);
				//notNouns.put("oh", i++); notNouns.put("hello", i++);
				//notNouns.put("ok", i++); notNouns.put("let's", i++);
				//notNouns.put("i'm", i++); notNouns.put("yes", i++);
				//notNouns.put("though", i++); notNouns.put("yea", i++);
				//notNouns.put("she's", i++); notNouns.put("i e", i++);
				//notNouns.put("her", i++);
				//notNouns.put("that's", i++);
				//notNouns.put("too", i++); notNouns.put("heh", i++);
				//notNouns.put("true", i++); notNouns.put("boring", i++);
				//notNouns.put("someone", i++); notNouns.put("who's", i++);
				//notNouns.put("at", i++); notNouns.put("hmm", i++);
				//notNouns.put("something", i++); notNouns.put("hehe", i++);
				//notNouns.put("lol", i++); notNouns.put("like", i++);
				//notNouns.put("k", i++); notNouns.put("can't", i++);
				//notNouns.put("yeah", i++); notNouns.put("didnt", i++);
				//notNouns.put("him", i++);
				//notNouns.put("it's", i++);
				//notNouns.put("myself", i++); notNouns.put("he's", i++);
				//notNouns.put("oops", i++); notNouns.put("dunno", i++);
				//notNouns.put("others", i++); notNouns.put("we", i++);
				//notNouns.put("we're", i++); notNouns.put("tho", i++);
				//notNouns.put("doesnt", i++); notNouns.put("anything", i++);
				//notNouns.put("everyone", i++); notNouns.put("nothing", i++);
				//notNouns.put("thats", i++); notNouns.put("was", i++);
				//notNouns.put("btw", i++); notNouns.put("aka", i++);
				//notNouns.put("it", i++); notNouns.put("dont", i++);
				//notNouns.put("hahaha", i++); notNouns.put("hi", i++);
				//notNouns.put("ah", i++); notNouns.put("in", i++);
				//notNouns.put("for", i++); notNouns.put("haha", i++);
				//notNouns.put("ha", i++); notNouns.put("y", i++);
				//notNouns.put("or", i++); notNouns.put("one", i++);
			} //System.out.println("notNouns size: " + notNouns.size());
		} catch (Exception e){
			e.printStackTrace();
		}
		
		verbFamily.put("VB", 1);
		verbFamily.put("VBD", 2);
		verbFamily.put("VBG", 3);
		verbFamily.put("VBN", 4);
		verbFamily.put("VBP", 5);
		verbFamily.put("VBZ", 6);
		
		File emotesfile = new File(location);
		try{
			BufferedReader br = new BufferedReader(new FileReader(emotesfile));
			while (br.ready()){
				String getEmote = br.readLine();
				emotes.add(getEmote);
			}
			initialized = true;
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void setWN(Wordnet wordn){
		wn = wordn;
	}

	public static boolean isNounHuman(String word){
		return (wn.isHypernym("human", word, "noun"));
	}

	public static boolean isWord(String word){
		if (!initialized)
			initialize();
		boolean retval = true;
		if (word.equalsIgnoreCase("she"))
			return retval;
		String [] wordTokens = word.split("\\s+");
		for (int i = 0; retval && i < wordTokens.length; i++){
		    //System.out.println("wordToken: " + wordTokens[i]);
			if (notNouns.containsKey(wordTokens[i]))
				retval = false;
			else
			    iw = wn.lookupIndexWord(POS.NOUN, wordTokens[i]);
			if (retval && iw == null)
				if (!GenderCheck.isName(wordTokens[i]))
				retval = false;
		}
		//System.out.println("retval: " + retval);
		return retval;
	}

	public static boolean ignorePronoun(String pronoun){
	    String p = pronoun.toLowerCase();
	    //System.out.println("ignorePronoun: " + ignorePronouns.containsKey(p));
	    return (ignorePronouns.containsKey(p));
	}

	public static boolean isBadNoun(String word){
	    //add toLowerCase() 06/21/11 by TL
	    //System.out.println("isBadNoun: " + notNouns.containsKey(word.toLowerCase()));
	    return (notNouns.containsKey(word.toLowerCase()));
	}

	public static boolean isNoun(String tag){
		if (!initialized)
			initialize();
		//Based on Penn Treebank project
		//Nouns are NN - noun, singular or mass
		//NNS - noun, plural
		//NNP - proper noun, singular
		//NNPS - proper noun, plural
		//System.out.println("is Noun? " + nounFamily.containsKey(tag));
			return (nounFamily.containsKey(tag));
	}
	
	public static boolean isVerb(String tag)
	{
		if(!initialized)
			initialize();
		
		return (verbFamily.containsKey(tag));
	}

	public static boolean isPronoun(String tag){
		if (!initialized)
			initialize();
		//to ignore pronouns, just return false
		//return false;

		//Based on Penn Treebank project
		//Pronouns are PRP - personal pronoun
		//PRP$ - possessive pronoun
		//WP is a wh-pronoun...not recognized at the moment but can
		//be entered in the initialize() function in this class
		return (pronounFamily.containsKey(tag));
	}

	public static boolean isTokenConnector(String param){
		return nounTokenConnectors.containsKey(param);
	}

	public static String getWord(String param){
		//This is for Stanford POS in the format word/tag
		//ex. dog/NN
		String retval = param;
		if (param.indexOf("/") != -1)
			retval = param.substring(0, param.indexOf("/"));
		return retval;
	}

	public static String getTag(String param){
		//This is for Stanford POS in the format word/tag
		//ex. dog/NN
		String retval = "";
		if (param.indexOf("/") != -1)
			retval = param.substring(param.indexOf("/") + 1);
		if (param.indexOf("Bodvarsson") != -1) {
		    //System.out.println("param is: " + param + " ===== and tag is: " + retval);
		}
		return retval;
	}

	public static String removeEmoticons(String param){
		if (!initialized)
			initialize();
		String retval = param;
		for (int i = 0; i < emotes.size(); i++){
			String emote = (String) emotes.get(i);
			retval = retval.replace(emote, "");
		}
		return retval;
	}

    public static String removePunctuation(String param,
					   boolean isEnglish,
					   boolean isChinese){
		String retval = param;
		if ((param != null) && (param.length() > 0)){
			param.replace("\n", " ");
			char[] keepletters = new char[retval.length()];
			int place = 0;
			if (isEnglish) {
			    for (int i = 0; i < retval.length(); i++){
				int tempval = (int) retval.charAt(i);
				//32 is space, 39 is single quote, 48-57 is 0-9
				//65-90 is capitals, 97-122 is lower case
				if ((tempval > 64 && tempval < 91) ||
				    (tempval > 96 && tempval < 123) ||
				    (tempval > 47 && tempval < 58) ||
				    (tempval == 39) || (tempval == 32))
				    keepletters[place++] = retval.charAt(i);
				else keepletters[place++] = ' ';
			    }
			    retval = new String(keepletters);
			}
			retval = retval.replaceAll("(\\s)+", " ");
		}
		return retval;
	}

	public static int getTurnNo(String param){
		int retval = -1;
		int place = param.indexOf(".");
		String temp;
		if (place > -1){
			temp = param.substring(0, place);
		} else temp = param;
		retval = Integer.parseInt(temp);
		return retval;
	}

	public static int wordCount(String param){
		//0- make sure emoticons arraylist has been populated (use setEmoticons)
		//1- remove emoticons
		//2- remove punctuation, replace multiple spaces with single space
		int retval = 0;
		int index = 0;
		if (!initialized)
			initialize();
		String tmp = removeEmoticons(param);

		boolean inaword = false;
		while (index < tmp.length()){
			char c = tmp.charAt(index++);
			if (((Character.isLetter(c) || Character.isDigit(c)) && (inaword == false))){
				retval++;
				inaword = true;
			} else if (Character.isWhitespace(c)) inaword = false;

		}
		//System.out.println( param + "  ---> " + retval);
		return retval;
	}

	public static void stats(){
		System.out.println("# of Emotes: " + emotes.size());
		System.out.println("# in nounFamily: " + nounFamily.size());
		System.out.println("# in pronounFamily: " + pronounFamily.size());
		System.out.println("# in ignorePronouns: " + ignorePronouns.size());
		System.out.println("# in notNouns: " + notNouns.size());
	}

}
