package edu.albany.ils.dsarmd0200.util;
import java.util.ArrayList;

/**
 *
 * Author: Ken Stahl
 */

//this class finds the pronouns in utteranceSource and attempts
//to resolve what they are all referring to


public class PronounResolution {
	String pronounSource;
	NounToken pronounTarget;
	ArrayList <NounToken> nounTokens;
	// ArrayList <String> [] candidates;
	ArrayList <Integer> humanScores;
	ArrayList <Integer> formScores;
	ArrayList <Integer> combinedScores;
	
    // prnSrc -- original pronoun
	// ns -- an arraylist of its references
	public PronounResolution(String prnSrc, ArrayList <NounToken> ns){
		pronounSource = prnSrc;
		nounTokens = ns;
		humanScores = new ArrayList<Integer>();
		formScores = new ArrayList<Integer>();
		combinedScores = new ArrayList<Integer>();
		setScores();
	}  //end of constructor

	public void setScores(){
		// set 4 scores
		setHumanScores();
		setFormScores();
		combineScores();
		chooseTarget();
	}

	public void setHumanScores()
	{
		// find if the source pronoun is "human pronoun"
		boolean phuman = GenderCheck.isPronounHuman(pronounSource);
		for (int i = 0; i < nounTokens.size(); i++){
			NounToken nt = nounTokens.get(i);
			String nword = nt.getWord();
			// nt is a pronoun
			if (nt.isPronoun())
			{
				int gchk = GenderCheck.pronounAmbiguityComparison(pronounSource, nword);
				humanScores.add(gchk);
			} 
			else 
			{
				if (GenderCheck.isName(nword))
				{
					if (GenderCheck.nameGenderMatch(pronounSource, nword))
						humanScores.add(1);
					else
						humanScores.add(-1);
				} 
				else 
				{
					if (phuman)
						if (ParseTools.isNounHuman(nword))
							humanScores.add(1);  //both human
						else
							humanScores.add(-1);  //conflict
					else if (ParseTools.isNounHuman(nword))
							humanScores.add(-1);  //conflict
						else
							humanScores.add(1);  //both non-human
				}
			}
		}

	}

	public void setFormScores(){
		for (int i = 0; i < nounTokens.size(); i++){
			NounToken nt = nounTokens.get(i);
			if (nt.isPronoun()){
				formScores.add(PronounFormMatching.doPronounFormsMatch(pronounSource, nt.getWord()));
			} else{
				formScores.add(PronounFormMatching.doesPronounMatchNounForm(pronounSource, nt.getTag()));
			}
		}
	}

	public void combineScores(){
		for (int i = 0; i < nounTokens.size(); i++){
			int humanScore = humanScores.get(i);
			int formScore = formScores.get(i);
			if ((humanScore + formScore) != 0)
				combinedScores.add(humanScore + formScore);
			else if (humanScore == -1 || formScore == -1)
				combinedScores.add(-1);
			else
				combinedScores.add(0);
		}
	}

	/*
	//testing nearest candidate without any scoring
	public void chooseTarget(){
		int target = nounTokens.size() - 1;
		pronounTarget = nounTokens.get(target);
	} */

	
	public void chooseTarget(){
		int target = nounTokens.size() - 1;  //default is the closest
		int score;
		if (target > -1)
			score = combinedScores.get(target);
		else
			score = 0;
		int startPlace = target - 1;
		for (int i = startPlace; i > -1; i--){  //this will find the closest score with highest value
			int tmpScore = combinedScores.get(i);
			if (tmpScore > score){
				score = tmpScore;
				target = i;
			}
		}
		if (target > -1)
			pronounTarget = nounTokens.get(target);
		else
			pronounTarget = null;
	}


	@Override public String toString(){ //return only 10 candidates atm
		String retval = "Pronoun: " + pronounSource + "\n";
		for (int i = 0; i < 10 && i < nounTokens.size(); i++){
			NounToken nt = nounTokens.get(i);
			retval += nt.getWord() + ", ID = " + nt.getID() + ", Score = " + combinedScores.get(i);
			retval += "\n";
		}
		return retval;
	}

	public int getTargetID(){
		if (pronounTarget != null)
			return (pronounTarget.getID());
		else
			return -1;
	}

}
