package edu.albany.ils.dsarmd0200.util;
import java.util.HashMap;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import edu.albany.ils.dsarmd0200.lu.*;
/**
 *
 * Author: Ken Stahl
 *
 *	This class contains hard coded plural and singular pronouns.
 *  Combined with gender and human/non-human comparisons, hopefully
 *  accurate pronoun chaining and noun pairing will be possible
 *  we should know whether or not a noun is plural from Stanford POS
 *  tag NNS is plural, NNPS is proper plural
 * 
 */
public abstract class PronounFormMatching {
	private static HashMap <String, Integer> strictlySingular;
	private static HashMap <String, Integer> strictlyPlural;
    private HashMap <String, Integer> males;
    private HashMap <String, Integer> females;
	private static boolean initialized = false;

        public static void initialize(){
	    strictlySingular = new HashMap();
		strictlyPlural = new HashMap();
		String humanpronouns = Settings.getValue("formpronouns");
		if (Settings.getValue("language").equals("chinese")) {
		    humanpronouns = Settings.getValue("formpronouns_chinese");
		}
		File infile = new File(humanpronouns);
		try{
			BufferedReader br = new BufferedReader(new FileReader(infile));
			String dummy = br.readLine(); int count = 0;//header
			while (dummy.length() > 1 && br.ready()){
				dummy = br.readLine();
				if (dummy.length() > 1)
					strictlySingular.put(dummy, count++);
			}
			dummy = br.readLine(); count = 0;  //header
			while (dummy.length() > 1 && br.ready()){
				dummy = br.readLine();
				if (dummy.length() > 1)
					strictlyPlural.put(dummy, count++);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		/*
		strictlySingular.put("he", 0); strictlySingular.put("her", 1);
		strictlySingular.put("hers", 2); strictlySingular.put("herself", 3);
		strictlySingular.put("him", 4); strictlySingular.put("himself", 5);
		strictlySingular.put("his", 6); strictlySingular.put("it", 7);
		strictlySingular.put("its", 8); strictlySingular.put("itself", 9);
		strictlySingular.put("myself", 10); strictlySingular.put("one", 11);
		strictlySingular.put("oneother", 12); strictlySingular.put("oneself", 13);
		strictlySingular.put("she", 14);
		strictlyPlural.put("both", 0); strictlyPlural.put("few", 1);
		strictlyPlural.put("many", 2); strictlyPlural.put("others", 3);
		strictlyPlural.put("ours", 4); strictlyPlural.put("ourselves", 5);
		strictlyPlural.put("everyone", 6); strictlyPlural.put("theirs", 7);
		strictlyPlural.put("them", 8); strictlyPlural.put("themselves", 9);
		strictlyPlural.put("these", 10); strictlyPlural.put("they", 11);
		strictlyPlural.put("those", 12); */
		initialized = true;
	}

	public static boolean isWordPronoun(String param){
		if (!initialized)
			initialize();
		return (strictlySingular.containsKey(param) || strictlyPlural.containsKey(param));
	}

	public static int isPronounPlural(String param){
		if (!initialized)
			initialize();
		int retval = 0;
		String p = param.toLowerCase();
			if (strictlyPlural.containsKey(p))
				retval = 1;
			else if (strictlySingular.containsKey(p))
				retval = -1;
		return retval;
	}

	public static int doesPronounMatchNounForm(String pronoun, String nounTag){
		String p1 = pronoun.toLowerCase();
		String nt = nounTag.toUpperCase();
		int check = isPronounPlural(p1);
		int check2; int retval = 0;
		if (nt.equalsIgnoreCase("NNS") || nt.equalsIgnoreCase("NNPS"))
			check2 = 1;
		else
			check2 = -1;
		if (check == 0)
			retval = 0;
		else if (check == check2)
			retval = 1;
		else 
			retval = -1;
		return retval;
	}

	public static int doPronounFormsMatch(String pronoun1, String pronoun2){
		int retval;
		String p1 = pronoun1.toLowerCase();
		String p2 = pronoun2.toLowerCase();
		int check1 = isPronounPlural(p1);
		int check2 = isPronounPlural(p2);
		if (check1 == 0 || check2 == 0)
			retval = 0;
		else if (check1 == check2)
			retval = 1;
		else
			retval = -1;
		return retval;
	}

}
