package edu.albany.ils.dsarmd0200.util;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.*;
import edu.albany.ils.dsarmd0200.lu.*;

/**
 *
 * Author: Ken Stahl
 * This class makes comparisons between pronoun and proper names
 * and pronouns with other pronouns.  The inter-pronoun comparison
 * is by gender and human/unhuman.  return values are 1, 0, -1
 * 1 means match, 0 means ambiguous, and -1 means conflict
 * pronounAmbiguityComparison for the pronoun-pronoun comparison
 * nameGenderMatch for the pronoun-proper name comparison
 *
 */


public abstract class GenderCheck {
	private static boolean initialized = false;
	private static HashMap <String, Integer> maleNames;
	private static HashMap <String, Integer> femaleNames;
	private static HashMap <String, Integer> malePronouns;
	private static HashMap <String, Integer> femalePronouns;
	private static HashMap <String, Integer> unhumanPronouns;
    private static HashSet<String> third_person_pronouns; //added by Ting

       public static void initialize(){
	third_person_pronouns = new HashSet();
	third_person_pronouns.add("he");
	third_person_pronouns.add("his");
	third_person_pronouns.add("him");
	third_person_pronouns.add("himself");
	third_person_pronouns.add("she");
	third_person_pronouns.add("her");
	third_person_pronouns.add("hers");
	third_person_pronouns.add("herself");
	third_person_pronouns.add("they");
	third_person_pronouns.add("them");
	third_person_pronouns.add("themselves");
	third_person_pronouns.add("their");
	third_person_pronouns.add("theirs");
		malePronouns = new HashMap<String, Integer>();
		femalePronouns = new HashMap<String, Integer>();
		maleNames = new HashMap<String, Integer>();
		femaleNames = new HashMap<String, Integer>();
		unhumanPronouns = new HashMap<String, Integer>();
		File infile = new File(Settings.getValue("eng_hum_pronouns"));
		try{
			BufferedReader b = new BufferedReader(new FileReader(infile));
			String dummy = b.readLine(); int count = 0; //male:
			while (dummy.length() > 0 && b.ready()){
				dummy = b.readLine();
				if (dummy.length() > 0) {
				    malePronouns.put(dummy, count++);
				    //added by TL 06/17
				    if (!third_person_pronouns.contains(dummy)) {
					third_person_pronouns.add(dummy);
				    }
				}
			}
			//System.out.println("male pronouns: " + malePronouns);
			dummy = b.readLine(); count = 0; //female:
			while (dummy.length() == 0) dummy = b.readLine();
			while (dummy.length() > 0 && b.ready()){
				dummy = b.readLine();
				if (dummy.length() > 0) {
				    femalePronouns.put(dummy, count++);
				    //added by TL 06/17
				    if (!third_person_pronouns.contains(dummy)) {
					third_person_pronouns.add(dummy);
				    }
				}				    
			}
			//System.out.println("female pronouns: " + femalePronouns);
			dummy = b.readLine(); count = 0; //unhuman:
			while (dummy.length() == 0) dummy = b.readLine();
			while (dummy.length() > 0 && b.ready()){
				dummy = b.readLine();
				if (dummy.length() > 1)
					unhumanPronouns.put(dummy, count++);
			} 
		} catch (Exception e){
			e.printStackTrace();
		}
		/*
		malePronouns.put("he", 0); malePronouns.put("his", 1);
		malePronouns.put("him", 2); malePronouns.put("himself", 3);
		femalePronouns.put("she", 0); femalePronouns.put("her", 1);
		femalePronouns.put("hers", 2); femalePronouns.put("herself", 3);
		unhumanPronouns.put("anything", 0); unhumanPronouns.put("everything", 1);
		unhumanPronouns.put("it", 2); unhumanPronouns.put("its", 3);
		unhumanPronouns.put("itself", 4); unhumanPronouns.put("nothing", 5);
		unhumanPronouns.put("something", 6); unhumanPronouns.put("whichever", 7);
		*/
		String maleFile = Settings.getValue("malenames");
		String femaleFile = Settings.getValue("femalenames");
		File inFileMale = new File(maleFile);
		File inFileFemale = new File(femaleFile);
		try{
			BufferedReader br = new BufferedReader(new FileReader(inFileMale));
			while (br.ready()){
			    int counter = 0;
			    String newName = br.readLine().toLowerCase().trim();
			    if (newName != null && newName.length() > 0)
				maleNames.put(newName, counter++);
			}
			br = new BufferedReader(new FileReader(inFileFemale));
			while (br.ready()){
				int counter = 0;
				String newName = br.readLine().toLowerCase().trim();
				if (newName != null && newName.length() > 0)
					femaleNames.put(newName, counter++);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
			initialized = true;
	}

    public static boolean isThPP(String word) {
	//if (word.equals("she")) System.out.println("word is: " + word + "\nthird_person_pronouns: "  + third_person_pronouns);
	return third_person_pronouns.contains(word);
    }

    public static boolean sameGender(String word1, String word2) {
	if ((isNameMale(word1) ||
	     isMaleProN(word1)) &&
	    (isNameMale(word2) ||
	     isMaleProN(word2)) ||
	    (isNameFemale(word1) ||
	     isFemaleProN(word1)) &&
	    (isNameFemale(word2) ||
	     isFemaleProN(word2))) {
	    return true;
	}
	return false;
    }

    public static boolean isMaleProN(String word) {
	if (word.equals("he") ||
	    word.equals("his") ||
	    word.equals("him") ||
	    word.equals("himself")) 
	    return true;
	return false;
    }

    public static boolean isFemaleProN(String word) {
	if (word.equals("she") ||
	    word.equals("her") ||
	    word.equals("hers") ||
	    word.equals("herself")) 
	    return true;
	return false;
    }

	public static boolean isName(String word){
		if (!initialized)
			initialize();
		return (femaleNames.containsKey(word.toLowerCase().trim()) || maleNames.containsKey(word.toLowerCase().trim())); //modified by TL 04/15
	}

	public static boolean isNameMale(String word){
		if (!initialized)
			initialize();
		String lower = word.toLowerCase();
		return (maleNames.containsKey(lower));
	}
	
	public static boolean isNameFemale(String word){
		if (!initialized)
			initialize();
		String lower = word.toLowerCase().trim();
		return (femaleNames.containsKey(lower));
	}

	public static boolean couldPronounBeMale(String word){
		if (!initialized)
			initialize();
		String pn = word.toLowerCase().trim();
		return (!femalePronouns.containsKey(pn) && !unhumanPronouns.containsKey(pn));
	}

	public static boolean couldPronounBeFemale(String pronoun){
		if (!initialized)
			initialize();
		String pn = pronoun.toLowerCase();
		System.out.println("test pronoun: " + pn);
		return (!malePronouns.containsKey(pn) && !unhumanPronouns.containsKey(pn));
	}
	
	public static boolean couldPronounBeNonHuman(String pronoun)
	{
		if(!initialized)
			initialize();
		String pn = pronoun.toLowerCase();
		return(unhumanPronouns.containsKey(pn));
	}

	public static boolean couldPronounsGenderMatch(String pronoun1, String pronoun2){
		if (!initialized)
			initialize();
		String pn1 = pronoun1.toLowerCase();
		String pn2 = pronoun2.toLowerCase();
		boolean female = couldPronounBeFemale(pn1) && couldPronounBeFemale(pn2);
		boolean male = couldPronounBeMale(pn1) && couldPronounBeMale(pn2);
		return (male || female);
	}

	public static boolean isPronounHuman(String pronoun){
		//basically anything not in the unhuman pronouns qualifies
		//the reason some pronouns like 'none' are not included in inhuman
		//are pronoun chains where it could refer to 'us' which is human
		//of course, this is assuming none is tagged as a pronoun to begin with.
		if (!initialized)
			initialize();
		String pn = pronoun.toLowerCase();
		//System.out.println("is pronoun human: " + pn);
		return (malePronouns.containsKey(pn) || femalePronouns.containsKey(pn));
	}
	
	public static int pronounAmbiguityComparison(String pronoun1, String pronoun2){
		//1  = direct match
		//0  = ambiguous match
		//-1 = direct conflict
		if (!initialized)
			initialize();
		boolean subStringCheck = (pronoun1.contains(pronoun2) || pronoun2.contains(pronoun1));
		if (subStringCheck)
			subStringCheck = subStringCheck && !pronoun1.equalsIgnoreCase("he")
					&& !pronoun2.equalsIgnoreCase("he") 
					&& !pronoun1.equalsIgnoreCase("his")
					&& !pronoun2.equalsIgnoreCase("his")
					&& !pronoun1.equalsIgnoreCase("me")
					&& !pronoun2.equalsIgnoreCase("me");
		boolean genderCheck = pronounGenderMatch(pronoun1, pronoun2);
		if (genderCheck || subStringCheck)
			return 1;  //direct match between two gendered pronouns
		//boolean humanCheck = pronounHumanMatch(pronoun1, pronoun2);
		if (!isPronounHuman(pronoun1) && !isPronounHuman(pronoun2))
			return 0;
		if (!couldPronounsGenderMatch(pronoun1, pronoun2))
			return -1;  //direct conflict found with gender
		return 0;  //ambiguous match
	}

	
	public static boolean pronounHumanMatch(String pronoun1, String pronoun2){
		String pn1 = pronoun1.toLowerCase();
		String pn2 = pronoun2.toLowerCase();
		return ((isPronounHuman(pn1) && isPronounHuman(pn2))
			  || (!isPronounHuman(pn1) && !isPronounHuman(pn2)));
	}
	
	public static boolean pronounGenderMatch(String pronoun1, String pronoun2){
		//checks to see if there is a direct match between pronoun genders
		//does not take ambiguity into account
		if (!initialized)
			initialize();
		String pn1 = pronoun1.toLowerCase();
		String pn2 = pronoun2.toLowerCase();
		return (malePronouns.containsKey(pn1) && malePronouns.containsKey(pn2))
			|| (femalePronouns.containsKey(pn1) && femalePronouns.containsKey(pn2));
	}

	public static boolean nameGenderMatch(String pronoun, String name){
		//comparing pronoun with noun (person's first name) only!!
		if (!initialized)
			initialize();
		//System.out.println("Comparing " + pronoun + " with " + name);
		String nm = name.toLowerCase().trim();
		String pn = pronoun.toLowerCase().trim();
		if (femalePronouns.containsKey(pn)) //female gender 
			return isNameFemale(nm);
		else if (malePronouns.containsKey(pn)) //male gender
			return isNameMale(nm);
		return false;
	}

}
