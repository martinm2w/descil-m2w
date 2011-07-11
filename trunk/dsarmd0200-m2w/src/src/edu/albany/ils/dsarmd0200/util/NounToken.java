package edu.albany.ils.dsarmd0200.util;
import java.util.ArrayList;

/**
 *
 * @author Ken
 *
 *
 */

//This class holds a single noun phrase (in String word)
//It's given a unique ID (ID)  and the turn in which it occurs is recorded.
//reference is the ID of another NounToken if it's a subsequent mention
//and -1 if it is a new chain
public class NounToken {
	String word;
	String tag;
	String speaker;
    String ref = null;
    String id = null;
	int reference;
	int ID;
	int turnNo;
	//isHuman is used to help facilitate pronoun resolution
	boolean isHuman;
	//float_tag added as identifier for automatic annotation
	String float_tag;

//this is a list of all subsequent mentions.  
	ArrayList <String> subsequentMentions;
	Wordnet wn;
        ChineseWordnet cnwn;

	public NounToken(String wrd, String tg, String spkr, int trnno, int idd, Wordnet w){
		word = wrd;
		tag = tg;
		speaker = spkr;
		turnNo = trnno;
		ID = idd;
		reference = -1;
		subsequentMentions = new ArrayList<String>();
		wn = w; float_tag = "";
	}
        
        public NounToken(String wrd, String tg, String spkr, int trnno, int idd, ChineseWordnet w){
		word = wrd;
		tag = tg;
		speaker = spkr;
		turnNo = trnno;
		ID = idd;
		reference = -1;
		subsequentMentions = new ArrayList<String>();
		cnwn = w; float_tag = "";
	}

    public String getId() { return id; }
    public String getRef() { return ref; }

    public NounToken(String wrd, String tg, String spk, int tnno, String id, String ref) {
	word = wrd;
	tag = tg;
	speaker = spk;
	turnNo = tnno;
	this.id = id;
	this.ref = ref;
	subsequentMentions = new ArrayList();
    }

@Override public String toString(){
	String retval = "***************\nLocal topic: " + word;
	retval += "\nID: " + ID;
	//retval += "\nTag: " + tag;
	retval += "\nSpeaker: " + speaker;
	retval += "\nref: " + reference + "\n";
	retval += "\nTurnNo: " + turnNo + "\n---Subsequent Mentions---\n";
	for (int i = 0; i < subsequentMentions.size(); i++){
		retval += subsequentMentions.get(i) + "\n";
	}
	return retval;
}

	public String getTag(){
		return tag;
	}

	public String getWord(){
		return word;
	}

    public String getSpeaker() { return speaker; }
	public int getID(){
		return ID;
	}

	public int getTurnNo(){
		return turnNo;
	}

	public boolean isNoun(){
		return ParseTools.isNoun(tag);
	}
	
	public boolean isPronoun(){
		return ParseTools.isPronoun(tag);
	}

	public boolean firstMention(){
		return (reference == -1);
	}

    public boolean firstAMention(){
	//System.out.println("ref: " + ref);
		return (ref == null);
    }

	public boolean anySubsequentMentions(){
		return (subsequentMentions.size() > 0);
	}

	public int numberOfMentions(){
		return subsequentMentions.size();
	}

	public int getRefInt(){
		return reference;
	}

	public String getReference(){
		String retval = "Word = " + word + ", ID = " + ID;
		retval += ", refers to ID -> " + reference;
		return retval;
	}

	public void makeNewRef(int param){
		String newValue = Integer.toString(param);
		subsequentMentions.add(newValue);
	}

	public void setReference(int param){
		reference = param;
	}

	public boolean isSynonym(NounToken param){
		boolean retval = false;
		//part of speech is same and not pronouns
		if (tag.equalsIgnoreCase(param.tag) && !ParseTools.isPronoun(tag)) 
			return (wn.isSynonym(word, param.word));
		return retval;
	}

	public boolean isEqual(NounToken param){
		boolean retval = false;
		if (word.equalsIgnoreCase(param.word))
			retval = true;
		return retval;
	}

	public void setFloatTag(String param){
		float_tag = param;
	}

	public String getFloatTag(){
		return float_tag;
	}
	
	public int getReferenceNumber()
	{
		return reference;
	}

    public void addSubsequent(String sub_id) {
	subsequentMentions.add(sub_id);
    }

    public boolean containsRef(int id) {
        return subsequentMentions.contains((new Integer(id)).toString());
    }

    public int sizeOfSubSM() {
	return subsequentMentions.size();
    }

}
