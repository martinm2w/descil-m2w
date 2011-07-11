package edu.albany.ils.dsarmd0200.util;
//****************************************************************************
//* Program Name: Wordnet.java
//* Programmer: Ting Liu
//* Date: May 12, 2005
//* Purpose: Find the relationship amoung words in a passage.
//* Note:    Uses JWNL, java wordnet library.
//****************************************************************************


import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.relationship.*;
import net.didion.jwnl.data.list.*;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNL.*;
import net.didion.jwnl.dictionary.*;
import net.didion.jwnl.dictionary.morph.*;
import java.lang.Object.*;
import java.util.TreeMap;
import javax.xml.parsers.*;
import java.io.FileInputStream;
import java.util.TreeSet;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.Locale.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.*;
import java.util.BitSet;
import java.util.Arrays;
import java.util.List;
import net.didion.jwnl.data.PointerUtils.*;
import net.didion.jwnl.util.*;
import java.io.*;
import net.didion.jwnl.JWNL.Version.*;
import net.didion.jwnl.JWNL.Version;
import java.lang.Exception;
import java.lang.Throwable;
public class Wordnet implements Serializable {

    Dictionary dico;
    //int num = 0;
    BufferedWriter outputFile;

    public Wordnet() {
	try{
		//System.out.println("flag1");
	    JWNL.initialize(new FileInputStream("/home/ruobo/develop/scil0200/conf/file_properties.xml"));
	    
	}
	catch (JWNLException je){
	    System.out.println(je);
	}
	catch (java.io.FileNotFoundException fnfe){
	    System.out.println(fnfe);
	}
	catch (java.io.IOException ioe){
	    System.out.println(ioe);
	}
	dico = Dictionary.getInstance();
	//initBadSenses();
    }

    public void setDictionary(Dictionary _dico) {
	dico = _dico;
    }

    public void setPU(PointerUtils pu) {
	pu_ = pu;
    }

    public boolean isASynonym(String word1, String word2, String pos){
	//System.out.println("***************Checking: " + word1 + " " + word2);
	word1 = word1.replaceAll(" ", "_");
	word2 = word2.replaceAll(" ", "_");
	if (isSynonym(word1, word2, pos)){
	    //System.out.println("true");
	    return true;
	}
	if (isSynonym(word2, word1,pos)){
	    //System.out.println("true");
	    return true;
	}
	return false;
    }

    public String getLemma(String phrase) {
	IndexWordSet iws = lookupAllIndexWords(phrase);
	if (iws == null) {
	    return null;
	}
	if (iws.getIndexWord(NOUN) == null ||
	    iws.getIndexWord(VERB) == null) {
	    return null;
	}
	if (iws.getIndexWord(NOUN) != null)
	    return iws.getIndexWord(NOUN).getLemma();
	if (iws.getIndexWord(VERB) != null)
	    return iws.getIndexWord(VERB).getLemma();
	return null;
    }

    public String getLemma(String phrase, String pos) {
	ArrayList pair = new ArrayList();
	pair.add(phrase);
	pair.add(pos);
	if (lemma_.keySet().contains(pair)) {
	    return (String)lemma_.get(pair);
	}
	IndexWordSet iws = lookupAllIndexWords(phrase);
	if (iws == null) {
	    return null;
	}
	if (pos.equals("noun")) {
	    if (iws.getIndexWord(NOUN) == null) {
		return null;
	    }
	    String lemma = iws.getIndexWord(NOUN).getLemma();
	    lemma_.put(pair, lemma);
	    return lemma;
	}
	if (pos.equals("verb")) {
	    if (iws.getIndexWord(POS.VERB) == null) {
		return null;
	    }
	    String lemma = iws.getIndexWord(POS.VERB).getLemma();
	    lemma_.put(pair, lemma);
	    return lemma;
	}
	return phrase;
    }

    public boolean isSynonym(Synset sense1,
			     String word2) {
	ArrayList senses = getSenses(word2, sense1.getPOS());
	for (int i = 0; i < senses.size(); i++) {
	    Synset sense2 = (Synset)senses.get(i);
	    if (isSynonym(sense1, sense2)) {
		return true;
	    }
	}
	return false;
    }


    public boolean isSynonym(Synset sense1,
			     Synset sense2) {
	if (sense1.equals(sense2)) {
	    return true;
	}
	return false;
    }


    public boolean isSynonym(String word1, String word2, String flag){
	//IF EQUAL RETURN TRUE
	ArrayList synonyms = getSynonym(word2, flag);
	//	System.out.println("The synonyms of " + word2 + " are: " + synonyms);
	if (synonyms != null) {
	    if (synonyms.contains(word1)) {
		return true;
	    }
	}
	return false;
    }

    public boolean isSynonym(String word1, String word2){
	//IF EQUAL RETURN TRUE
	ArrayList synonyms = getSynonymAllSenses(word2);
	System.out.println("The synonyms of " + word2 + " are: " + synonyms);
	if (synonyms != null) {
	    if (synonyms.contains(word1)) {
		return true;
	    }
	}
	return false;
    }

    public boolean isSynonym(String word1, String word2, String flag, int arrange){
	//IF EQUAL RETURN TRUE
	ArrayList synonyms = getSynonym(word2, flag, arrange);
	//	System.out.println("The synonyms of " + word2 + " are: " + synonyms);
	if (synonyms != null) {
	    if (synonyms.contains(word1)) {
		return true;
	    }
	}
	return false;
    }

    public boolean isSynonymAllSenses(String word1, String word2, String flag){
	//IF EQUAL RETURN TRUE
	ArrayList synonyms = getSynonymAllSenses(word2, flag);
	//	System.out.println("The synonyms of " + word2 + " are: " + synonyms);
	if (synonyms != null) {
	    if (synonyms.contains(word1)) {
		return true;
	    }
	}
	return false;
    }

    private boolean checkSenses(Synset[] ss1,String word) throws JWNLException{
	for (int i = 0;i<ss1.length;i++){
	    Synset ss2 = ss1[i];
	    //	    System.out.println("The synset is: " + ss2.toString());
	    if (ss2.containsWord(word)) {
		return true;
	    }
	    /*
	    PointerTarget[] pts1 = ss2.getTargets(PointerType.HYPERNYM);
	    //IF ONE OF THE WORDS SYNOYNMS IS THE OTHER WORD RETURN TRUE
	    if (checkPointers(pts1,word))
		return true;
	    */
	}
	return false;
    }

    private boolean checkPointers(PointerTarget[] pt1, String word)
	throws JWNLException{
	for (int i = 0;i<pt1.length;i++){
	    //System.out.println("target: " + pt1[i].toString());
	    PointerTargetNode ptn1 = new PointerTargetNode(pt1[i]);
	    Synset ssl = ptn1.getSynset();
	    //System.out.println("synset: " + ssl);

	    if (ssl.containsWord(word)){
		//System.out.println("return true");
		return true;
	    }
	}
	return false;
    }

    public IndexWord lookupIndexWord(POS pos, String phrase) {
	try {
	    //	    String[] words = phrase.split("[-_ ]+");
	    IndexWord iw = dico.lookupIndexWord(pos, phrase);
	    /*
	    if (iw == null) {
		return iw;
	    }
	    String lem = iw.getLemma();
	    String[] lem_words = lem.split("[-_ ]+");
	    if (lem_words.length != words.length) {
		return null;
	    }
	    */
	    return iw;
	} catch (net.didion.jwnl.JWNLException jwnlex) {
	    jwnlex.printStackTrace();
	}
	return null;
    }

    public IndexWordSet lookupAllIndexWords(String phrase) {
	try {

	    IndexWordSet iws = null;
	    //	    System.out.println("The iws of United States are:\n" + iws);
	    //	    System.exit(0);
	    //	    phrase = phrase.replaceAll("-", "_");
	    //	    String[] words = phrase.split("[-_ ]+");
	    iws = dico.lookupAllIndexWords(phrase);
	    /*
	    if (iws == null) {
		return null;
	    }
	    IndexWord[] iwa = iws.getIndexWordArray();
	    for (int i = 0; i < iwa.length; i++) {
		IndexWord iw = iwa[i];
		String lem = iw.getLemma();
		String[] lem_words = lem.split("[-_ ]+");
		if (lem_words.length != words.length) {
		    iws.remove(iw.getPOS());
		}
	    }
	    */
	    return iws;
	} catch (net.didion.jwnl.JWNLException jwnlex) {
	    jwnlex.printStackTrace();
	}
	return null;
    }

    public Dictionary getDictionary() {
	return dico;
    }

    public void testRelations() {
	try {

	    String test_s = "(aksdjfa)aksdfkajsdf";
	    //	    System.out.println("@#@#@#@#@@#@#@##@@@@@@@@@@@@@@@@@@@@#######################");
	    System.out.println(test_s.replaceAll("\\(.+\\)", ""));
	    IndexWord test1 = lookupIndexWord(POS.NOUN, "biological weapons");
	    if (test1 != null) {
		Synset[] ss1s = test1.getSenses();
		IndexWord test2 = lookupIndexWord(POS.NOUN, "sarin");
		if (test2 != null) {
		    Synset[] ss2s = test2.getSenses();
		    for (int i = 0; i < ss1s.length; i++) {
			Synset ss1 = ss1s[i];
			for (int j = 0; j < ss2s.length; j++) {
			    Synset ss2 = ss2s[j];
			    System.out.println("$$$$$$$$$$$$$$$$$the synset of Albany is: " + ss1.toString());
			    System.out.println("$$$$$$$$$$$$$$$$$the synset of Tampa is: " + ss2.toString());
			    getRelationship(ss1, ss2, "noun");
			}
		    }
		}
		//		System.out.println("^^^^^^^^^^^^^^^^^^^" + test1 + " " + test2 + " have relations? " + relates);
	    }
	}catch (JWNLException jwnle){
	    System.out.println("Exception in Wordnet: " + jwnle);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    //***********************************************GETRELATIONSHIP
    //find relationship among sysnets

    public void getRelationship(Synset ss1, Synset ss2, String flag)throws JWNLException{
	RelationshipList RL1;
	RelationshipFinder rf = RelationshipFinder.getInstance();
	RL1 = rf.findRelationships(ss1,ss2,PointerType.HYPERNYM);
	int test = RL1.size();

	if (test > 0){
	    Relationship rshipdeep = RL1.getDeepest();
	    Relationship rshipshallow = RL1.getShallowest();

	    int i = rshipdeep.getSize();
	    int j = ((AsymmetricRelationship)rshipdeep).getCommonParentIndex();

	    for (int w = 0; w < test; w++) {
		Relationship rsh = (Relationship) RL1.get(w);
		int comm = ((AsymmetricRelationship)rsh).getCommonParentIndex();
		PointerTargetNodeList ptnl = rsh.getNodeList();
		PointerTargetNode ptn = (PointerTargetNode)ptnl.get(comm);

		//		System.out.println("\nThe node list size for " + w + "th relation is: " + ptnl.size());
		//		System.out.println("The type of this relation is: " + rsh.getType().toString());
		//		System.out.println("Is this relationship symmetric: " + rsh.getType().isSymmetric());
		//		System.out.println("The common parent index is: " + comm +
		//				   "\nThe common parent is: " + ptn.getSynset().getWord(0).getLemma());

		for (int k = 0; k < ptnl.size(); k++) {
		    ptn = (PointerTargetNode)ptnl.get(k);
		    if (ptn != null) {
			Word[] words = ptn.getSynset().getWords();
			System.out.print("The offset is: " + ptn.getSynset().getOffset() + " and the words in the synset are: ");
			for (int kk = 0; kk < words.length; kk++) {
			    System.out.print(words[kk].getLemma() + " " + words[kk].getIndex() + ", " );
			}
			//			System.out.println();
		    }
		}
	    }

	    PointerTargetNodeList ptnl = rshipshallow.getNodeList();
	    PointerTargetNode ptn = null;//(PointerTargetNode)ptnl.get(j);

	    //	    System.out.println("\n\nThe node list size of shallowest relation is: " + ptnl.size());

	    for (int k = 0; k < ptnl.size(); k++) {
		ptn = (PointerTargetNode)ptnl.get(k);
		if (ptn != null)
		    System.out.println(ptn.getSynset().getWord(0).getLemma());
	    }


	    ptnl = rshipdeep.getNodeList();

	    System.out.println("\n\nThe node list size of deepest relation is: " + ptnl.size());

	    for (int k = 0; k < ptnl.size(); k++) {
		ptn = (PointerTargetNode)ptnl.get(k);
		if (ptn != null)
		    System.out.println(ptn.getSynset().getWord(0).getLemma());
	    }


	    /*
	    Synset ssc = ptn.getSynset();
	    int size = ssc.getWordsSize();

	    Word common1 = ssc.getWord(0);
	    String word = new String(common1.getLemma());

	    //remove some concepts to base

	    if (flag == "noun"){
		if ((stopConcepts.contains(word)) || stemmer_.isStopWord(word)){
		}
		else{
		    //System.out.println("noun relationship: " + word);
		    nounRelates.add(word);
		}
	    }
	    if (flag == "verb"){
		if (stopConcepts.contains(word)){

		}
		else{
		    //System.out.println("verb relationship: " + word);
		    verbRelates.add(word);
		}
	    }
	    */
	}
    }


    public LinkedList sortMap(TreeMap nounConcepts){
	Set keySet = nounConcepts.keySet();
	Iterator it = keySet.iterator();
	String phrase;
	Integer count;
	ArrayList entry = new ArrayList();
	Integer highest = new Integer(0);
	Integer found = new Integer(0);

	LinkedList ll = new LinkedList();

	//System.out.println("*******Tree Map: " + nounConcepts.toString());

	if (!it.hasNext())
	    return ll;
	phrase = (String)(it.next());
	count = (Integer)(nounConcepts.get(phrase));

	//always start by ading the first phrase
	entry.add(count);
	entry.add(phrase);
	ll.add(entry);

	//then begin from the second
	while (it.hasNext()){
	    phrase = (String)(it.next());
	    count = (Integer)(nounConcepts.get(phrase));
	    entry = new ArrayList();
	    entry.add(count);
	    entry.add(phrase);
	    boolean inserted =false;

	    //loop through the linked list to find where to place this passage
	    for (int h = 0;h<ll.size();h++){
		//get the element in the list
		ArrayList checkit = (ArrayList)ll.get(h);
		//IF THE SIZE OF THE ONE WE ARE LOOKING TO INSERT IS
		//LESS OR EQUAL TO THE CURRENT ELEMENT,
		//THEN INSERT AND BREAK THE FOR LOOP, ELSE CONTINUE
		//Integer countInt = new Integer(count);
		Integer newInt = (Integer)checkit.get(0);
		//System.out.println("count: " + count);
		//System.out.println("newInt: " + newInt);
		//System.out.println(count.compareTo(newInt));
		if (newInt.compareTo(count) <= 0){
		    //want to add it behind longer phrases
		    ll.add(h,entry);
		    //System.out.println("LinkedList After: " + ll.toString());
		    inserted=true;
		    break;
		}
	    }

		//if we went through whole list and haven't inserted then insert at end
		if (!inserted)
		    ll.add(entry);
	}
	//System.out.println("LinkedList After: " + ll.toString());

	return ll;
    }




    private String stripURLs(String input){

	//common URL format
	//System.out.println("***INPUT: " + input);
	String output =
	    Pattern.compile("http:.*( |\\.)").matcher(input).replaceAll("");

	//System.out.println("OUTPUT: " + output);
	return output;
    }



    //converts an arraylist of strings where strings are
    //phrases separated by commas to one string
    private String concatInput(ArrayList input){

	String list = new String();

	for (int i = 0; i<input.size();i++){
	    list = list + " " + (String)input.get(i);
	}

	return list;
    }







    //***********************************************GETRELATIONSHIP
    //find relationship among sysnets

	public boolean getRelationships(Synset ss1, Synset ss2, IndexWord iw1, IndexWord iw2,
				    String flag)throws JWNLException, IOException{
	RelationshipList RL1;
	RelationshipFinder rf = RelationshipFinder.getInstance();


	int i = rf.getImmediateRelationship(iw1,iw2);
	//System.out.println("%%%Int: " + i);



	RL1 = rf.findRelationships(ss1,ss2,PointerType.HYPERNYM,10);
	RelationshipList RL2 = rf.findRelationships(ss1,ss2,PointerType.ENTAILMENT,10);
	int test = RL1.size();

	if (test > 0){
	    Relationship shallow = RL1.getShallowest();
	    Relationship deepest = RL1.getDeepest();
	    //System.out.println("shallow: " + shallow.toString());
	    //System.out.println("entail: " + deepest.toString());
	    if (shallow.getDepth() < 4)
		return true;

	}
	return false;
    }

    //**********************************************getSynsets
    // getSynsets - get the number of synsets this word/phrase has
    //

    public int getSynsets(String text, String pos) throws JWNLException {

	IndexWord base;

	if (pos == "noun"){
	    base = lookupIndexWord(POS.NOUN, text);
	}
	else {
	    base = lookupIndexWord(POS.VERB, text);
	}

	//double check sense count

	if (base != null){
	    return base.getSenseCount();
	}
	//needed in case word is not in wordnet
	return 100;

    }

    //************************************************checkabstraction
    //checkAbstraction - return boolean if word is abstract

    public boolean checkAbstraction(String word, String pos) throws JWNLException {

	IndexWord wordIndex, abstractIndex;
	Synset wordSynset, abstractSynset;
	PointerTargetTree ptt, ptta;
	PointerTargetTreeNode found;
	boolean flag = false;
	PointerTargetNode ptn,ptn2;
	PointerUtils pu = PointerUtils.getInstance();

	wordIndex = null;
	if (pos == "noun"){
	    wordIndex = lookupIndexWord(POS.NOUN, word);
	}
	else if (pos == "verb") {
	    wordIndex = lookupIndexWord(POS.VERB, word);
	}
	else if (pos == "adjective") {
	    wordIndex = lookupIndexWord(POS.ADJECTIVE, word);
	}
	//word not in wordnet
	if (wordIndex == null){
	    return false;
	}

	int size = wordIndex.getSenseCount();
	//System.out.println("This is size: " + size);

	for (int i = 1; i <= size; i++){

	    wordSynset = wordIndex.getSense(i);
	    ptt = pu.getHypernymTree(wordSynset);

	    //System.out.println("This is ptt: " + ptt.toString());

	    PointerTargetNodeList[] pttnlw = ptt.reverse();
	    Iterator it = pttnlw[0].iterator(); //abstraction

	    while(it.hasNext()){
		ptn = (PointerTargetNode)it.next(); //abstraaction
		//System.out.println("*************NEW WORD");
		//System.out.println("This is ptn: " + ptn.toString());

		Synset wss = ptn.getSynset();
		long wsslong = wss.getOffset();
		if (wsslong == 13018 ||            //abstraction
		    wsslong == 12865 ){            //phsycological
		    //System.out.println("TRUE$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		    return true;
		}

	    }
	}
	return false;
    }


    String gatepathname = new String("file:/tmp/Seed.html");
    String pathname = new String("/tmp/Seed.html");

    ArrayList stopConcepts;



    //=======================================START OF TINGS WORDPROCESSOR CLASSES
    //FROM HERE TO THE END OF FILE
    public ArrayList getSynonym(String word, String pos) {
	if (pos.equals("noun") && n_synonym_list_.containsKey(word)) {
	    ArrayList synonyms = (ArrayList) n_synonym_list_.get(word);
	    return (ArrayList)synonyms.clone();
	}
	else if (pos.equals("adjective") && a_synonym_list_.containsKey(word)) {
	    ArrayList synonyms = (ArrayList) a_synonym_list_.get(word);
	    return (ArrayList)synonyms.clone();
	}
	else if (pos.equals("verb") && v_synonym_list_.containsKey(word)) {
	    ArrayList synonyms = (ArrayList) v_synonym_list_.get(word);
	    return (ArrayList)synonyms.clone();
	}

	ArrayList list = new ArrayList();
	try{
	    POS _pos = POS.getPOSForLabel(pos);
	    PointerTargetNodeList synonym;

	    if(_pos!=null){
				//System.out.println("_pos is not null");
		IndexWord iw=lookupIndexWord(_pos, word);
		if (iw == null) {
		    return list;
		}
		//System.out.println("flag");
		Synset [] synsets=iw.getSenses();
		for(int i=0;(i < synsets.length) && (i < 1);i++)
		    //use the first sense
		    {
			Synset synset = synsets[i];
			//if (isBadSense(synset)) {
			    //continue;
			//}
			synonym = pu_.getSynonyms(synset);
			if(synonym.isEmpty()) {
			    continue;
			  //  			    synonym = pu_.getDirectHypernyms(synset);
			}
			Set set = getLemmas(synonym);

			if(set != null) {
			    			    System.out.println("getLemmas got null");
			    list.addAll(set);
			    break;
			}
		    }
	    }
	    else {
		IndexWordSet iws = lookupAllIndexWords(word);
		IndexWord [] iw_array = iws.getIndexWordArray();

		for(int j=0;j<iw_array.length;j++){
		    IndexWord iw=iw_array[j];
		    _pos=iw.getPOS();
		    Synset [] synsets=iw.getSenses();
		    for(int i=0;i<synsets.length;i++)
			{
			    Synset synset = synsets[i];
			    if (isBadSense(synset)) {
				continue;
			    }
			    synonym = pu_.getSynonyms(synset);
			    if(synonym.isEmpty())
			      synonym = pu_.getDirectHypernyms(synset);
			    Set set=getLemmas(synonym);
			    if(set!=null)
				list.addAll(set);
			    //			    else System.out.println("getLemmas got null");
			}
		}
	    }


	}
	catch(JWNLException e){
	    e.printStackTrace();
	    list.clear();
	}
	catch(Exception ie){
	    ie.printStackTrace();
	    //System.out.println(ie);
	}

	//System.out.println("**********");
	//System.out.println("'"+word+"'s"+" Synonym List: "+list);
	if (list != null &&
	    list.size() != 0) {
	    if (pos.equals("noun")) {
		n_synonym_list_.put(word, list);
	    }
	    else if (pos.equals("adjective")) {
		a_synonym_list_.put(word, list);
	    }
	    else if (pos.equals("verb")) {
		v_synonym_list_.put(word, list);
	    }
	}
	return (ArrayList)list.clone();
    }

    public ArrayList getSynonymNoBS(String word, String pos) {
	ArrayList list = new ArrayList();
	try{
	    POS _pos = POS.getPOSForLabel(pos);
	    PointerTargetNodeList synonym;

	    if(_pos!=null){
		//		System.out.println("_pos is not null");
		IndexWord iw=lookupIndexWord(_pos, word);
		if (iw == null) {
		    return list;
		}
		Synset [] synsets=iw.getSenses();
		for(int i=0;(i < synsets.length) && (i < 1);i++)
		    //use the first sense
		    {
			Synset synset = synsets[i];
			if (isBadSense(synset)) {
			    continue;
			}
			synonym = pu_.getSynonyms(synset);
			if(synonym.isEmpty()) {
			    continue;
			    //			    synonym = pu_.getDirectHypernyms(synset);
			}
			Set set = getLemmas(synonym);

			if(set != null) {
			    //			    System.out.println("getLemmas got null");
			    list.addAll(set);
			    break;
			}
		    }
	    }
	    else {
		IndexWordSet iws = lookupAllIndexWords(word);
		IndexWord [] iw_array = iws.getIndexWordArray();

		for(int j=0;j<iw_array.length;j++){
		    IndexWord iw=iw_array[j];
		    _pos=iw.getPOS();
		    Synset [] synsets=iw.getSenses();
		    for(int i=0;i<synsets.length;i++)
			{
			    Synset synset = synsets[i];
			    if (isBadSense(synset)) {
				continue;
			    }
			    synonym = pu_.getSynonyms(synset);
			    if(synonym.isEmpty())
			      synonym = pu_.getDirectHypernyms(synset);
			    Set set=getLemmas(synonym);
			    if(set!=null)
				list.addAll(set);
			    //			    else System.out.println("getLemmas got null");
			}
		}
	    }


	}
	catch(JWNLException e){
	    e.printStackTrace();
	    list.clear();
	}
	catch(Exception ie){
	    ie.printStackTrace();
	    //System.out.println(ie);
	}

	return (ArrayList)list.clone();
    }

    public ArrayList getSynonym(String word, String pos, int arrange) {
	if (pos.equals("noun") && n_synonym_list_.containsKey(word)) {
	    ArrayList synonyms = (ArrayList) n_synonym_list_.get(word);
	    return synonyms;
	}
	else if (pos.equals("adjective") && a_synonym_list_.containsKey(word)) {
	    ArrayList synonyms = (ArrayList) a_synonym_list_.get(word);
	    return synonyms;
	}
	else if (pos.equals("verb") && v_synonym_list_.containsKey(word)) {
	    ArrayList synonyms = (ArrayList) v_synonym_list_.get(word);
	    return synonyms;
	}

	ArrayList list = new ArrayList();
	try{
	    POS _pos = POS.getPOSForLabel(pos);
	    PointerTargetNodeList synonym;

	    if(_pos!=null){
		//		System.out.println("_pos is not null");
		IndexWord iw=lookupIndexWord(_pos, word);
		if (iw == null) {
		    return list;
		}
		Synset [] synsets=iw.getSenses();
		for(int i=0;(i < synsets.length) && (i < arrange);i++)
		    //use the first sense
		    {
			Synset synset = synsets[i];
			if (isBadSense(synset)) {
			    continue;
			}
			synonym = pu_.getSynonyms(synset);
			if(synonym.isEmpty()) {
			    continue;
			    //			    synonym = pu_.getDirectHypernyms(synset);
			}
			Set set = getLemmas(synonym);

			if(set != null) {
			    //			    System.out.println("getLemmas got null");
			    list.addAll(set);
			    break;
			}
		    }
	    }
	    else {
		IndexWordSet iws = lookupAllIndexWords(word);
		IndexWord [] iw_array = iws.getIndexWordArray();

		for(int j=0;j<iw_array.length;j++){
		    IndexWord iw=iw_array[j];
		    _pos=iw.getPOS();
		    Synset [] synsets=iw.getSenses();
		    for(int i=0;i<synsets.length;i++)
			{
			    Synset synset = synsets[i];
			    synonym = pu_.getSynonyms(synset);
			    if(synonym.isEmpty())
			      synonym = pu_.getDirectHypernyms(synset);
			    Set set=getLemmas(synonym);
			    if(set!=null)
				list.addAll(set);
			    //			    else System.out.println("getLemmas got null");
			}
		}
	    }


	}
	catch(JWNLException e){
	    e.printStackTrace();
	    list.clear();
	}
	catch(Exception ie){
	    ie.printStackTrace();
	    //System.out.println(ie);
	}

	//System.out.println("**********");
	//System.out.println("'"+word+"'s"+" Synonym List: "+list);
	if (list != null &&
	    list.size() != 0) {
	    if (pos.equals("noun")) {
		n_synonym_list_.put(word, list);
	    }
	    else if (pos.equals("adjective")) {
		a_synonym_list_.put(word, list);
	    }
	    else if (pos.equals("verb")) {
		v_synonym_list_.put(word, list);
	    }
	}
	return list;
    }

    public ArrayList getHiSynonym(String word, String pos) {
	ArrayList list = new ArrayList();
	try{
	    POS _pos = POS.getPOSForLabel(pos);
	    PointerTargetNodeList synonym;

	    if(_pos!=null){
		//		System.out.println("_pos is not null");
		IndexWord iw=lookupIndexWord(_pos, word);
		if (iw == null) {
		    return list;
		}
		Synset [] synsets=iw.getSenses();
		for(int i=0;(i < synsets.length) && (i < 1);i++)
		    //use the first sense
		    {
			Synset synset = synsets[i];
			if (isBadSense(synset)) {
			    continue;
			}
			synonym = pu_.getSynonyms(synset);
			if(synonym.isEmpty()) {
			    continue;
			    //			    synonym = pu_.getDirectHypernyms(synset);
			}
			Set set = getLemmas(synonym);

			if(set != null) {
			    //			    System.out.println("getLemmas got null");
			    list.addAll(set);
			    break;
			}
		    }
	    }
	    else {
		IndexWordSet iws = lookupAllIndexWords(word);
		IndexWord [] iw_array = iws.getIndexWordArray();

		for(int j=0;j<iw_array.length;j++){
		    IndexWord iw=iw_array[j];
		    _pos=iw.getPOS();
		    Synset [] synsets=iw.getSenses();
		    for(int i=0;i<synsets.length;i++)
			{
			    Synset synset = synsets[i];
			    if (isBadSense(synset)) {
				continue;
			    }
			    synonym = pu_.getSynonyms(synset);
			    if(synonym.isEmpty())
			      synonym = pu_.getDirectHypernyms(synset);
			    Set set=getLemmas(synonym);
			    if(set!=null)
				list.addAll(set);
			    //			    else System.out.println("getLemmas got null");
			}
		}
	    }


	}
	catch(JWNLException e){
	    e.printStackTrace();
	    list.clear();
	}
	catch(Exception ie){
	    ie.printStackTrace();
	    //System.out.println(ie);
	}

	//System.out.println("**********");
	//System.out.println("'"+word+"'s"+" Synonym List: "+list);
	if (list != null &&
	    list.size() != 0) {
	    if (pos.equals("noun")) {
		n_synonym_list_.put(word, list);
	    }
	    else if (pos.equals("adjective")) {
		a_synonym_list_.put(word, list);
	    }
	    else if (pos.equals("verb")) {
		v_synonym_list_.put(word, list);
	    }
	}
	return list;
    }

    public ArrayList getSynonymAllSenses(String word, String pos) {
	if (pos.equals("noun") && n_synonym_list_.containsKey(word)) {
	    ArrayList synonyms = (ArrayList) n_synonym_list_.get(word);
	    return synonyms;
	}
	else if (pos.equals("adjective") && a_synonym_list_.containsKey(word)) {
	    ArrayList synonyms = (ArrayList) a_synonym_list_.get(word);
	    return synonyms;
	}

	ArrayList list = new ArrayList();
	try{
	    POS _pos = POS.getPOSForLabel(pos);
	    PointerTargetNodeList synonym;

	    if(_pos!=null){
		//		System.out.println("_pos is not null");
		IndexWord iw=lookupIndexWord(_pos, word);
		if(iw==null)
		{
			return list;
		}
		Synset [] synsets=iw.getSenses();

		for(int i = 0; i < synsets.length; i++)
		    //use the first sense
		    {
			Synset synset = synsets[i];
			if (isBadSense(synset)) {
			    continue;
			}
			synonym = pu_.getSynonyms(synset);
			if(synonym.isEmpty())
			  synonym = pu_.getDirectHypernyms(synset);
			Set set = getLemmas(synonym);

			if(set != null) {
			    //			    System.out.println("getLemmas got null");
			    list.addAll(set);
			}
		    }
	    }
	    else {
		IndexWordSet iws = lookupAllIndexWords(word);
		IndexWord [] iw_array = iws.getIndexWordArray();

		for(int j=0;j<iw_array.length;j++){
		    IndexWord iw=iw_array[j];
		    _pos=iw.getPOS();
		    Synset [] synsets=iw.getSenses();
		    for(int i=0;i<synsets.length;i++)
			{
			    Synset synset = synsets[i];
			    if (isBadSense(synset)) {
				continue;
			    }
			    synonym = pu_.getSynonyms(synset);
			    if(synonym.isEmpty())
			      synonym = pu_.getDirectHypernyms(synset);
			    Set set=getLemmas(synonym);
			    if(set!=null)
				list.addAll(set);
			    //			    else System.out.println("getLemmas got null");
			}
		}
	    }


	}
	catch(JWNLException e){
	    e.printStackTrace();
	    list.clear();
	}
	catch(Exception ie){
	    ie.printStackTrace();
	    //System.out.println(ie);
	}

	//System.out.println("**********");
	//System.out.println("'"+word+"'s"+" Synonym List: "+list);
	if (list != null &&
	    list.size() != 0) {
	    if (pos.equals("noun")) {
		n_synonym_list_.put(word, list);
	    }
	    else if (pos.equals("adjective")) {
		a_synonym_list_.put(word, list);
	    }
	}
	return list;
    }

    public ArrayList getSynonymAllSenses(String word) {
	ArrayList list = new ArrayList();
	try{
	    PointerTargetNodeList synonym;
	    IndexWordSet iws = lookupAllIndexWords(word);
	    IndexWord [] iw_array = iws.getIndexWordArray();

	    for(int j=0;j<iw_array.length;j++){
		IndexWord iw=iw_array[j];
		Synset [] synsets=iw.getSenses();
		for(int i=0;i<synsets.length && i<2;i++)
		    //no more than the first 2 senses
		    {
			Synset synset = synsets[i];
			if (isBadSense(synset)) {
			    continue;
			}
			synonym = pu_.getSynonyms(synset);
			if(synonym.isEmpty())
			    synonym = pu_.getDirectHypernyms(synset);
			Set set=getLemmas(synonym);
			if(set!=null)
			    list.addAll(set);
			//			    else System.out.println("getLemmas got null");
		    }
	    }

	}
	catch(JWNLException e){
	    e.printStackTrace();
	    list.clear();
	}
	catch(Exception ie){
	    ie.printStackTrace();
	    //System.out.println(ie);
	}

	return list;
    }

    /**
     * get derived word between noun and verb
     */
    public ArrayList getDerived(String word) {
	ArrayList list = new ArrayList();
	try{
	    PointerTargetNodeList derived;
	    IndexWordSet iws = lookupAllIndexWords(word);
	    IndexWord [] iw_array = iws.getIndexWordArray();

	    for(int j=0;j<iw_array.length;j++){
		IndexWord iw=iw_array[j];
		Synset [] synsets=iw.getSenses();
		for(int i=0;i<synsets.length && i<2;i++)
		    //no more than the first 2 senses
		    {
			Synset synset = synsets[i];
			derived = pu_.getDerived(synset);
			if (derived == null) {
			    continue;
			}
			Set set=getLemmas(derived);
			if(set!=null)
			    list.addAll(set);
		    }
	    }

	}
	catch(JWNLException e){
	    e.printStackTrace();
	    list.clear();
	}
	catch(Exception ie){
	    ie.printStackTrace();
	    //System.out.println(ie);
	}

	return list;
    }

    public PointerUtils pu_ = PointerUtils.getInstance();
    public ArrayList getLemmas (String word, String pos) {

	 POS _pos = POS.getPOSForLabel(pos);

	 if (lemmas_.keySet().contains(word)) {
	     HashMap lemma = (HashMap)lemmas_.get(word);
	     if (lemma.keySet().contains(_pos)) {
		 return (ArrayList)lemma.get(_pos);
	     }
	 }

	ArrayList lemmas = new ArrayList();


	try {

	    IndexWordSet iws = lookupAllIndexWords(word);

	    if(_pos != null){
		if (iws.isValidPOS(_pos))
		    {
			lemmas.add(iws.getIndexWord(_pos).getLemma());
			//			System.out.println("The lemmas of " + word + "(" + pos + ") are: " + lemmas);
			HashMap lemma = null;
			if (lemmas_.keySet().contains(word)) {
			    lemma = (HashMap)lemmas_.get(word);
			}
			else {
			    lemma = new HashMap();
			}
			lemma.put(_pos, lemmas);
			lemmas_.put(word, lemma);
			return lemmas;
		    }
		else {
		    lemmas.add("nolemma");
		    return lemmas;
		}
	    }
	    else {
		/*
		if (iws.isValidPOS(VERB))
		    {
			lemmas.add(iws.getIndexWord(VERB).getLemma());

		    }
		*/
		if(iws.isValidPOS(NOUN))
		    {
			lemmas.add(iws.getIndexWord(NOUN).getLemma());
		    }
		else {
		    lemmas.add("nolemma");
		}
		/*
		if (iws.isValidPOS(ADJECTIVE))
		    {
			lemmas.add(iws.getIndexWord(ADJECTIVE).getLemma());
		    }
		if (!((iws.isValidPOS(ADJECTIVE)) || (iws.isValidPOS(NOUN)))) {
		    lemmas.add("nolemma");
		}

		if (iws.isValidPOS(ADVERB))
		    {
			lemmas.add(iws.getIndexWord(ADVERB).getLemma());

		    }
		*/
	    }
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	return lemmas;
    }

    public ArrayList getDirectHyponym(String word, String pos) {
	ArrayList list = new ArrayList();
	try{
	    POS _pos = POS.getPOSForLabel(pos);
	    PointerTargetNodeList dir_hyponym;

	    if(_pos!=null){
		//		System.out.println("_pos is not null");
		IndexWord iw=lookupIndexWord(_pos, word);
		Synset [] synsets=iw.getSenses();
		for(int i=0;i<synsets.length;i++)
		    {
			Synset synset = synsets[i];
			dir_hyponym = pu_.getDirectHyponyms(synset);
			Set set = getLemmas(dir_hyponym);

			if(set!=null)
			    list.addAll(set);
		    }
	    }
	    else {
		IndexWordSet iws = lookupAllIndexWords(word);
		IndexWord [] iw_array = iws.getIndexWordArray();

		for(int j=0;j<iw_array.length;j++){
		    IndexWord iw=iw_array[j];
		    _pos=iw.getPOS();
		    Synset [] synsets=iw.getSenses();
		    for(int i=0;i<synsets.length;i++)
			{
			    Synset synset = synsets[i];
			    dir_hyponym = pu_.getDirectHyponyms(synset);
			    Set set = getLemmas(dir_hyponym);
			    if(set!=null)
				list.addAll(set);
			    //			    else System.out.println("getLemmas got null");
			}
		}
	    }


	}
	catch(JWNLException e){
	    e.printStackTrace();
	    list.clear();
	    return null;
	}
	catch(Exception ie){
	    ie.printStackTrace();
	    //System.out.println(ie);
	}

	//System.out.println("**********");
	//System.out.println("'"+word+"'s"+" Synonym List: "+list);

	return list;
    }
    /*
    public String getLemma(String phrase, String pos) {

	try {
	    POS _pos = POS.getPOSForLabel(pos);
	    DefaultMorphologicalProcessor dmp = new DefaultMorphologicalProcessor();
	    if(_pos != null){
		return dmp.lookupBaseForm(_pos, phrase).getLemma();
	    }
	    else {
		return null;
	    }
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}

	String [] words = phrase.split(" ");
	for (int i = 0; i < words.length; i++) {
	    System.out.print( " " + words[i]);
	}
	System.out.println();
	BitSet bt = new BitSet(words.length);
	//	bt.set(0, words.length - 1, true);
	//	ArrayList words = new ArrayList();
	String lemma = net.didion.jwnl.dictionary.morph.Util.getLemma(words, bt, "_");
	System.out.println(phrase + "'s lemma is: " + lemma);
	return lemma;
    }
    */


   /*******************getLemma**********************************************/
    public ArrayList getLemmasWithTp(String word) {
	try {
	    if (w_lemmas_.containsKey(word)) {
		return (ArrayList) w_lemmas_.get(word);
	    }
	    IndexWordSet iws = lookupAllIndexWords(word);
	    IndexWord [] iw_array = iws.getIndexWordArray();
	    ArrayList lemmas = new ArrayList();
	    for (int i = 0; i < iw_array.length; i++) {
		IndexWord iw = iw_array[i];
		String lemma = iw.getLemma();
		POS pos = iw.getPOS();
		if (pos.equals(NOUN)) {
		    lemmas.add(lemma + "_N");
		}
		else if (pos.equals(VERB)) {
		    lemmas.add(lemma + "_V");
		}
		if (pos.equals(ADJECTIVE) ||
		    pos.equals(ADVERB)) {
		    lemmas.add(lemma + "_A");
		}
		//get derived
		PointerTargetNodeList ptnl = pu_.getDerived(iw.getSense(1));
		for (int j = 0; j < ptnl.size(); j++) {
		    PointerTargetNode ptn = (PointerTargetNode)ptnl.get(j);
		    Word w = ptn.getWord();
		    lemma = w.getLemma();
		    pos = iw.getPOS();
		    if (pos.equals(NOUN)) {
			lemmas.add(lemma + "_N");
		    }
		    else if (pos.equals(VERB)) {
			lemmas.add(lemma + "_V");
		    }
		    if (pos.equals(ADJECTIVE) ||
			pos.equals(ADVERB)) {
			lemmas.add(lemma + "_A");
		    }
		}
	    }
	    w_lemmas_.put(word, lemmas);
	    return lemmas;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public ArrayList getSenses(PointerTargetNodeList ptnl)
    {
	ArrayList set = new ArrayList();
	// ptnl.print();
	Iterator iter = ptnl.iterator();
	PointerTargetNode ptn;
	Synset syn;
	Word[] words;

	while(iter.hasNext())
            {
		ptn = (PointerTargetNode)iter.next();
		syn = ptn.getSynset();
		if( syn != null )
		    {
			set.add(syn);
		    }
	    }  // the end of the while loop.

	return set;
    }

    public Set getSynsets(PointerTargetNodeList ptnl)
    {
	Set set = new TreeSet();
	// ptnl.print();
	Iterator iter = ptnl.iterator();
	PointerTargetNode ptn;
	Synset syn;
	Word[] words;

	while(iter.hasNext())
            {
		ptn = (PointerTargetNode)iter.next();
		syn = ptn.getSynset();
		set.add(syn);
	    }
	return set;
    }

    public ArrayList getLemmas(Word[] words) {
	ArrayList set = new ArrayList();
	for(int j = 0; j < words.length; j++)
	    {
		set.add(words[j].getLemma());
		//System.out.println("word: " + words[j].getLemma());
	    }
	return set;
    }

    public Set getLemmas(PointerTargetNodeList ptnl)
    {
	Set set = new TreeSet();
	// ptnl.print();
	Iterator iter = ptnl.iterator();
	PointerTargetNode ptn;
	Synset syn;
	Word[] words;

	while(iter.hasNext())
            {
		ptn = (PointerTargetNode)iter.next();
		syn = ptn.getSynset();
		if( syn != null )
		    {
			words = syn.getWords();
			for(int j = 0; j < words.length; j++)
			    {
				set.add(words[j].getLemma());
				//System.out.println("word: " + words[j].getLemma());
			    }
		    }
		else
		    {
			if( ptn.getWord() != null)
			    {
				set.add(ptn.getWord().getLemma());
				// System.out.println("lemma from getWord() method: " + ptn.getWord().getLemma());
			    }
			else set = null;
		    }
	    }  // the end of the while loop.

	return set;
    }

    public Set getLemmas(ArrayList ptnls)
    {
	Set set = new TreeSet();
	// ptnl.print();
	Iterator iter = ptnls.iterator();
	PointerTargetNode ptn;
	Synset syn;
	Word[] words;

	while(iter.hasNext())
            {
		PointerTargetNodeList ptnl = (PointerTargetNodeList)iter.next();
		for (int i = 0; i < ptnl.size(); i++) {
		    ptn = (PointerTargetNode)ptnl.get(i);
		    syn = ptn.getSynset();
		    if( syn != null )
			{
			    words = syn.getWords();
			    for(int j = 0; j < words.length; j++)
				{
				    set.add(words[j].getLemma());
				    //System.out.println("word: " + words[j].getLemma());
				}
			}
		    else
			{
			    if( ptn.getWord() != null)
				{
				    set.add(ptn.getWord().getLemma());
				    // System.out.println("lemma from getWord() method: " + ptn.getWord().getLemma());
				}
			    else set = null;
			}
		}
	    }  // the end of the while loop.

	return set;
    }

    /**
     *  return the lemma which synset is the first one of it's synet list
     */
    public Set getHiLemmas(PointerTargetNodeList ptnl) throws Exception
    {
	Set set = new TreeSet();
	// ptnl.print();
	Iterator iter = ptnl.iterator();
	PointerTargetNode ptn;
	Synset syn;
	Word[] words;

	while(iter.hasNext())
            {
		ptn = (PointerTargetNode)iter.next();
		syn = ptn.getSynset();
		if( syn != null )
		    {
			words = syn.getWords();
			for(int j = 0; j < words.length; j++)
			    {
				String lemma = words[j].getLemma();
				POS _pos = words[j].getPOS();
				IndexWord iw = lookupIndexWord(_pos, lemma);
				if (iw == null) {
				    continue;
				}
				Synset[] senses = iw.getSenses();
				if (senses.length > 0) {
				    if (syn.equals(senses[0])) {
					set.add(words[j].getLemma());
				    }
				}
				//System.out.println("word: " + words[j].getLemma());
			    }
		    }
		else
		    {
			if( ptn.getWord() != null)
			    {
				set.add(ptn.getWord().getLemma());
				// System.out.println("lemma from getWord() method: " + ptn.getWord().getLemma());
			    }
			else set = null;
		    }
	    }  // the end of the while loop.

	return set;
    }

    public HashMap lemma_for_hype_ = new HashMap();

    /**
       check whether word1 is the hypernym of word2
    */
    public boolean isHypernym(String word1, String word2){
	    ArrayList hypernyms = getHypernym(word2);
	    /*
	    if (word2.equals("bag")) {
		System.out.println("&&&&&&&&&&&&&&&&&The hypernyms of bag are:\n" + hypernyms);
	    }
	    */

	    String lemma = (String)lemma_for_hype_.get(word1);
	    if (lemma == null) {
		lemma = getLemma(word1);
		lemma_for_hype_.put(word1, lemma);
	    }


	    if (hypernyms.contains(lemma)) {
		return true;
	    }
	    return false;
    }


    public boolean isHypernym(Synset sense1,
			      String word2) throws Exception {
	ArrayList senses = getSenses(word2, sense1.getPOS());
	for (int i = 0; i < senses.size(); i++) {
	    Synset sense2 = (Synset)senses.get(i);
	    if (isHypernym(sense1, sense2)) {
		return true;
	    }
	}
	return false;
    }


    public boolean isHypernym(Synset sense1,
			      Synset sense2) throws Exception{
	//	RelationshipList rls = rf_.findRelationships(sense1, sense2, PointerType.HYPERNYM, 5);
	PointerTargetTree ptt = pu_.getHypernymTree(sense2);
	List ptnls = ptt.toList();
	for (int i = 0; i < ptnls.size(); i++) {
	    PointerTargetNodeList ptnl = (PointerTargetNodeList)ptnls.get(i);
	    for (int j = 0; j < ptnl.size(); j++) {
		PointerTargetNode ptn = (PointerTargetNode)ptnl.get(j);
		if (sense1.equals(ptn.getSynset())) {
		    return true;
		}
	    }
	}
	return false;
    }


    public boolean isDirHypernym(Synset sense1,
				 Synset sense2) throws Exception{
	//	RelationshipList rls = rf_.findRelationships(sense1, sense2, PointerType.HYPERNYM, 5);
	PointerTargetNodeList ptnl = pu_.getDirectHypernyms(sense2);
	for (int j = 0; j < ptnl.size(); j++) {
	    PointerTargetNode ptn = (PointerTargetNode)ptnl.get(j);
	    if (sense1.equals(ptn.getSynset())) {
		return true;
	    }
	}
	return false;
    }


    public boolean isHyponym(Synset sense1,
			     Synset sense2) throws Exception{
	//	RelationshipList rls = rf_.findRelationships(sense1, sense2, PointerType.HYPERNYM, 5);
	PointerTargetTree ptt = pu_.getHyponymTree(sense2);
	List ptnls = ptt.toList();
	for (int i = 0; i < ptnls.size(); i++) {
	    PointerTargetNodeList ptnl = (PointerTargetNodeList)ptnls.get(i);
	    for (int j = 0; j < ptnl.size(); j++) {
		PointerTargetNode ptn = (PointerTargetNode)ptnl.get(j);
		if (sense1.equals(ptn.getSynset())) {
		    return true;
		}
	    }
	}
	return false;
    }


    public boolean isDirHyponym(Synset sense1,
				Synset sense2) throws Exception{
	PointerTargetNodeList ptnl = pu_.getDirectHyponyms(sense2);
	for (int j = 0; j < ptnl.size(); j++) {
	    PointerTargetNode ptn = (PointerTargetNode)ptnl.get(j);
	    if (sense1.equals(ptn.getSynset())) {
		return true;
	    }
	}
	return false;
    }


    public boolean isHypernym(String word1, String word2, String pos){
	ArrayList hypernyms = getHypernym(word2, pos);

	if (hypernyms == null) {
	    return false;
	}
	String lemma = getLemma(word1, pos);

	if (hypernyms.contains(lemma)) {
	    return true;
	}
	return false;
    }

    /**
     * check the hypernym relation between phrase
     */
    public boolean isHypernymPhr(String word1, String word2, String pos){
	String[] words = word2.split("_");
	String single_word = words[0];
	ArrayList single_hyps = getHypernymPhr(single_word, pos);
	ArrayList hypernyms = getHypernymPhr(word2, pos);

	if (single_hyps != null &&
	    single_hyps.size() > 0 &&
	    words.length > 1 &&
	    hypernyms.contains(single_hyps.get(0))) {
	    return false;
	}
	if (hypernyms == null) {
	    return false;
	}
	String lemma = getLemma(word1, pos);

	if (hypernyms.contains(lemma)) {
	    return true;
	}
	return false;
    }

    /**
     * we have to filter out the bad sense first
     */
    public boolean isHypernymNoBS(String word1, String word2, String pos){
	ArrayList hypernyms = getHypernymNoBS(word2, pos);

	String lemma = getLemma(word1, pos);

	if (hypernyms.contains(lemma)) {
	    return true;
	}
	return false;
    }


    public boolean isHypernym(String word1, String word2, String pos, int arrange){
	ArrayList hypernyms = getHypernym(word2, pos, arrange);

	String lemma = getLemma(word1, pos);

	if (hypernyms.contains(lemma)) {
	    return true;
	}
	return false;
    }


   /****** find hypernyms *************************/
    public ArrayList getHypernym(String word) {
	try {
	    if (hyper_noun_lists_.containsKey(word)) {
		return (ArrayList) hyper_noun_lists_.get(word);
	    }

	    ArrayList list = new ArrayList();
	    IndexWordSet iws = lookupAllIndexWords(word);

	    Set set = new TreeSet();
	    if (iws.isValidPOS(NOUN))
		{
		    IndexWord noun = iws.getIndexWord(NOUN);
		    PointerTargetTree ptt  =  pu_.getHypernymTree(noun.getSense(1));
		    Word [] word_list = ptt.getRootNode().getSynset().getWords();
		    ArrayList pttList = (ArrayList) ptt.toList();
		    Iterator iter0 = pttList.iterator();
		    while(iter0.hasNext())
			{
			    PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			    set.addAll(getLemmas(ptnl));
			}
		    /*
		    if (noun.getSenseCount() > 1) {

			ptt  =  pu_.getHypernymTree(noun.getSense(2));
			word_list = ptt.getRootNode().getSynset().getWords();
			pttList = (ArrayList) ptt.toList();
			iter0 = pttList.iterator();
			while(iter0.hasNext())
			    {
				PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
				set.addAll(getLemmas(ptnl));
			    }
		    }
		    */
		}
	    list.addAll(set);

	    // testing the unique.
	    /*Iterator iter = list.iterator();
	      System.out.println("checking the unique.");
	      while(iter.hasNext())
	      {
	      System.out.println("unique: " + (String)iter.next());
	      } */
	    //	    System.out.println("WORD: " + word);
	    //	    System.out.println("Hypernym List: "+list);
	    hyper_noun_lists_.put(word, list);
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public ArrayList getHypernym(String word, String pos) {
	try {
	    if (hyper_noun_lists_.containsKey(word) &&
		pos.equals("noun")) {
		return (ArrayList) hyper_noun_lists_.get(word);
	    }
	    if (hyper_adj_lists_.containsKey(word) &&
		pos.equals("adjective")) {
		return (ArrayList) hyper_adj_lists_.get(word);
	    }
	    if (hyper_verb_lists_.containsKey(word) &&
		pos.equals("verb")) {
		return (ArrayList) hyper_verb_lists_.get(word);
	    }
	    ArrayList list = new ArrayList();
	    POS _pos = null;
	    if (pos.equals("noun")) {
		_pos = NOUN;
		hyper_noun_lists_.put(word, list);
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
		hyper_verb_lists_.put(word, list);
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
		hyper_adj_lists_.put(word, list);
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		//System.out.println(word + " " + _pos + " has no wordnet definition!!!");
		return list;
	    }

	    Set set = new TreeSet();
	    boolean compared = false;
	    ArrayList senses = new ArrayList(Arrays.asList(iw.getSenses()));
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getHypernymTree(sense);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * the hypernym has to be the exact phrase instead of one of words
     */
    public ArrayList getHypernymPhr(String word, String pos) {
	try {
	    String word1 = word.replaceAll(" ", "_");
	    ArrayList list = new ArrayList();
	    POS _pos = null;
	    if (pos.equals("noun")) {
		_pos = NOUN;
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		//System.out.println(word + " " + _pos + " has no wordnet definition!!!");
		return list;
	    }

	    Set set = new TreeSet();
	    boolean compared = false;
	    ArrayList senses = new ArrayList(Arrays.asList(iw.getSenses()));
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getHypernymTree(sense);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		ArrayList lemmas = new ArrayList();
		lemmas.addAll(getLemmas(pttList));
		if (!compared &&
		    (!lemmas.contains(word) &&
		     !lemmas.contains(word1))) {
		    return new ArrayList();
		}
		compared = true;
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * the hypernym has to be the exact phrase instead of one of words
     */
    public ArrayList getHypernymPhrSs(String word, String pos) {
	try {
	    String word1 = word.replaceAll(" ", "_");
	    ArrayList list = new ArrayList();
	    POS _pos = null;
	    if (pos.equals("noun")) {
		_pos = NOUN;
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		//System.out.println(word + " " + _pos + " has no wordnet definition!!!");
		return list;
	    }

	    Set set = new TreeSet();
	    boolean compared = false;
	    ArrayList senses = new ArrayList(Arrays.asList(iw.getSenses()));
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getHypernymTree(sense);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		ArrayList lemmas = new ArrayList();
		lemmas.addAll(getLemmas(pttList));
		if (!compared &&
		    (!lemmas.contains(word) &&
		     !lemmas.contains(word1))) {
		    return new ArrayList();
		}
		compared = true;
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			ArrayList ss = getSenses(ptnl);
			if (ss != null &&
			    ss.size() > 0) {
			    list.addAll(ss);
			}
		    }
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * the hypernym has to be the exact phrase instead of one of words
     */
    public ArrayList getHiHypernymPhr(String word, String pos) {
	try {
	    String word1 = word.replaceAll(" ", "_");
	    ArrayList list = new ArrayList();
	    POS _pos = null;
	    if (pos.equals("noun")) {
		_pos = NOUN;
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		//System.out.println(word + " " + _pos + " has no wordnet definition!!!");
		return list;
	    }

	    Set set = new TreeSet();
	    boolean compared = false;
	    ArrayList senses = new ArrayList();
	    senses.add(iw.getSense(1));
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getHypernymTree(sense);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		ArrayList lemmas = new ArrayList();
		lemmas.addAll(getLemmas(pttList));
		if (!compared &&
		    (!lemmas.contains(word) &&
		     !lemmas.contains(word1))) {
		    return new ArrayList();
		}
		compared = true;
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * the hypernym has to be the exact phrase instead of one of words
     */
    public ArrayList getHyponymPhr(String word, String pos) {
	try {
	    String word1 = word.replaceAll(" ", "_");
	    ArrayList list = new ArrayList();
	    POS _pos = null;
	    if (pos.equals("noun")) {
		_pos = NOUN;
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		//System.out.println(word + " " + _pos + " has no wordnet definition!!!");
		return list;
	    }

	    Set set = new TreeSet();
	    boolean compared = false;
	    ArrayList senses = new ArrayList(Arrays.asList(iw.getSenses()));
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getHyponymTree(sense);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		ArrayList lemmas = new ArrayList();
		lemmas.addAll(getLemmas(pttList));
		if (!compared &&
		    (!lemmas.contains(word) &&
		     !lemmas.contains(word1))) {
		    return new ArrayList();
		}
		compared = true;
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * the hypernym has to be the exact phrase instead of one of words
     */
    public ArrayList getHiHyponymPhr(String word, String pos) {
	try {
	    String word1 = word.replaceAll(" ", "_");
	    ArrayList list = new ArrayList();
	    POS _pos = null;
	    if (pos.equals("noun")) {
		_pos = NOUN;
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		//System.out.println(word + " " + _pos + " has no wordnet definition!!!");
		return list;
	    }

	    Set set = new TreeSet();
	    boolean compared = false;
	    ArrayList senses = new ArrayList();
	    senses.add(iw.getSense(1));
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getHyponymTree(sense);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		ArrayList lemmas = new ArrayList();
		lemmas.addAll(getLemmas(pttList));
		if (!compared &&
		    (!lemmas.contains(word) &&
		     !lemmas.contains(word1))) {
		    return new ArrayList();
		}
		compared = true;
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * the hypernym has to be the exact phrase instead of one of words
     */
    public ArrayList getSynonymPhr(String word, String pos) {
	try {
	    String word1 = word.replaceAll(" ", "_");
	    ArrayList list = new ArrayList();
	    POS _pos = null;
	    if (pos.equals("noun")) {
		_pos = NOUN;
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		//System.out.println(word + " " + _pos + " has no wordnet definition!!!");
		return list;
	    }

	    Set set = new TreeSet();
	    boolean compared = false;
	    ArrayList senses = new ArrayList(Arrays.asList(iw.getSenses()));
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getSynonymTree(sense, 1);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		ArrayList lemmas = new ArrayList();
		lemmas.addAll(getLemmas(pttList));
		if (!compared &&
		    (!lemmas.contains(word) &&
		     !lemmas.contains(word1))) {
		    return new ArrayList();
		}
		compared = true;
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * the hypernym has to be the exact phrase instead of one of words
     */
    public ArrayList getHiSynonymPhr(String word, String pos) {
	try {
	    String word1 = word.replaceAll(" ", "_");
	    ArrayList list = new ArrayList();
	    POS _pos = null;
	    if (pos.equals("noun")) {
		_pos = NOUN;
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		//System.out.println(word + " " + _pos + " has no wordnet definition!!!");
		return list;
	    }

	    Set set = new TreeSet();
	    boolean compared = false;
	    ArrayList senses = new ArrayList();
	    senses.add(iw.getSense(1));
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getSynonymTree(sense, 1);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		ArrayList lemmas = new ArrayList();
		lemmas.addAll(getLemmas(pttList));
		if (!compared &&
		    (!lemmas.contains(word) &&
		     !lemmas.contains(word1))) {
		    return new ArrayList();
		}
		compared = true;
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * with no bad sense, such speech attack for attack event
     */
    public ArrayList getHypernymNoBS(String word, String pos) {
	try {
	    ArrayList list = new ArrayList();
	    POS _pos = null;
	    if (pos.equals("noun")) {
		_pos = NOUN;
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		//System.out.println(word + " " + _pos + " has no wordnet definition!!!");
		return list;
	    }

	    Set set = new TreeSet();
	    ArrayList senses = new ArrayList(Arrays.asList(iw.getSenses()));
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    //System.out.println("find a bad sense: " + sense);
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getHypernymTree(sense);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public ArrayList getHypernymSs(String word, String pos) {
	try {
	    ArrayList list = new ArrayList();
	    POS _pos = POS.getPOSForLabel(pos);
	    ArrayList senses = getSenses(word, _pos);
	    if (senses == null ||
		senses.size() == 0) {
		return list;
	    }
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getHypernymTree(sense);
		ArrayList pttList = (ArrayList) ptt.toList();
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			ArrayList ss = getSenses(ptnl);
			if (ss != null &&
			    ss.size() > 0) {
			    list.addAll(ss);
			}
		    }
		//		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public ArrayList getSenses(String word, String pos) {
	POS pos_ = getPOS(pos, word);
	if (pos_ == null ||
	    word == null ||
	    word.length() == 0) {
	    return new ArrayList();
	}
	return getSenses(word, pos_);
    }

    public ArrayList getSenses(String word, POS pos) {
	try {
	    ArrayList senses = new ArrayList();
	    IndexWord iw = null;

	    String key = word + "_" + pos;
	    if (senses_.containsKey(key)) {
		iw = (IndexWord)senses_.get(key);
		if (iw == null) {
		    //processed before
		    return senses;
		}
	    }
	    if (iw == null) {
		iw = lookupIndexWord(pos, word);
	    }
	    senses_.put(key, iw);
	    if (iw == null) {
		//		System.out.println(word + " " + pos + " has no wordnet definition!!!");
		return senses;
	    }

	    senses = new ArrayList(Arrays.asList(iw.getSenses()));
	    return senses;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public ArrayList getHypernym(String word, String pos, int arrange) {
	try {
	    String key = word + "_" + arrange;
	    if (hyper_noun_lists_.containsKey(key) &&
		pos.equals("noun")) {
		return (ArrayList) hyper_noun_lists_.get(key);
	    }
	    if (hyper_adj_lists_.containsKey(key) &&
		pos.equals("adjective")) {
		return (ArrayList) hyper_adj_lists_.get(key);
	    }
	    if (hyper_verb_lists_.containsKey(key) &&
		pos.equals("verb")) {
		return (ArrayList) hyper_verb_lists_.get(key);
	    }

	    POS _pos = null;
	    ArrayList list = new ArrayList();
	    if (pos.equals("noun")) {
		_pos = NOUN;
		hyper_noun_lists_.put(key, list);
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
		hyper_verb_lists_.put(key, list);
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
		hyper_adj_lists_.put(key, list);
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		return list;
	    }

	    Set set = new TreeSet();
	    ArrayList senses = new ArrayList(Arrays.asList(iw.getSenses()));
	    for (int i = 0; i < senses.size() && i < arrange; i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getHypernymTree(sense);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public ArrayList getHyponym(String word, String pos) {
	try {

	    if (hypo_noun_lists_.containsKey(word) &&
		pos.equals("noun")) {
		return (ArrayList) ((ArrayList) hypo_noun_lists_.get(word)).clone();
	    }
	    if (hypo_verb_lists_.containsKey(word) &&
		pos.equals("verb")) {
		return (ArrayList) ((ArrayList) hypo_verb_lists_.get(word)).clone();
	    }
	    //	    System.out.println("looking for the hyponym of " + word + " " + pos);
	    POS _pos = POS.getPOSForLabel(pos);

	    ArrayList list = new ArrayList();
	    //	    IndexWordSet iws = lookupAllIndexWords(word);
	    IndexWord iw = lookupIndexWord(_pos, word);

	    Set set = new TreeSet();
	    if (iw != null) {
		PointerTargetTree ptt = pu_.getHyponymTree(iw.getSense(1));
		int i = 1;
		int up_limmit = iw.getSenses().length;
		ArrayList pttList = new ArrayList();
		while (ptt != null &&
		       i <= up_limmit &&
		       i <= 3) {
		    ptt  =  pu_.getHyponymTree(iw.getSense(i));
		    Word [] word_list = ptt.getRootNode().getSynset().getWords();
		    if (word_list != null &&
			word_list.length > 0) {
			pttList.addAll((ArrayList) ptt.toList());
		    }
		    i++;
		}
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		    list.addAll(set);
		    if (pos.equals("noun") ||
			pos.equals("adjective")) {
			hypo_noun_lists_.put(word, list);
		    }
		    else {
			hypo_verb_lists_.put(word, list);
		    }
	    }
	    // testing the unique.
	    /*Iterator iter = list.iterator();
	      System.out.println("checking the unique.");
	      while(iter.hasNext())
	      {
	      System.out.println("unique: " + (String)iter.next());
	      } */
	    //	    System.out.println("WORD: " + word);
	    //	    System.out.println("Hyponym List: "+list);
	    /*
	    if (word.equals("shoot")) {
		System.out.println("The hyponym of shoot is: " + list);
	    }
	    if (word.equals("fire")) {
		System.out.println("The hyponym of fire is: " + list);
	    }
	    */
	    return (ArrayList) list.clone();
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public ArrayList getHyponymNoBS(String word, String pos) {
	try {
	    ArrayList list = new ArrayList();
	    POS _pos = null;
	    if (pos.equals("noun")) {
		_pos = NOUN;
	    }
	    else if (pos.equals("verb")) {
		_pos = VERB;
	    }
	    else if (pos.equals("adjective")) {
		_pos = ADJECTIVE;
	    }
	    IndexWord iw = lookupIndexWord(_pos, word);
	    if (iw == null) {
		//System.out.println(word + " " + _pos + " has no wordnet definition!!!");
		return list;
	    }

	    Set set = new TreeSet();
	    ArrayList senses = new ArrayList(Arrays.asList(iw.getSenses()));
	    for (int i = 0; i < senses.size(); i++) {
		Synset sense = (Synset) senses.get(i);
		if (isBadSense(sense)) {
		    continue;
		}
		PointerTargetTree ptt  =  pu_.getHyponymTree(sense);
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext())
		    {
			PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
			set.addAll(getLemmas(ptnl));
		    }
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public ArrayList getHiHyponym(String word, String pos) {
	try {
	    //	    System.out.println("looking for the hi_hyponym of " + word + " " + pos);
	    POS _pos = POS.getPOSForLabel(pos);

	    ArrayList list = new ArrayList();
	    //	    IndexWordSet iws = lookupAllIndexWords(word);
	    IndexWord iw = lookupIndexWord(_pos, word);

	    Set set = new TreeSet();
	    if (iw != null) {
		PointerTargetTree ptt = null;
		ptt  =  pu_.getHyponymTree(iw.getSense(1));
		Word [] word_list = ptt.getRootNode().getSynset().getWords();
		ArrayList pttList = (ArrayList) ptt.toList();
		Iterator iter0 = pttList.iterator();
		while(iter0.hasNext()) {
		    PointerTargetNodeList ptnl = (PointerTargetNodeList)iter0.next();
		    set.addAll(getHiLemmas(ptnl));
		}
		list.addAll(set);
	    }
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /****** find Indirect hypernyms *************************/
    public ArrayList getIHypernym(String word) {
	//get the second level hypernyms
	try {
	    if (hyper_noun_lists_.containsKey(word)) {
		return (ArrayList) hyper_noun_lists_.get(word);
	    }

	    ArrayList list = new ArrayList();
	    IndexWordSet iws = lookupAllIndexWords(word);

	    Set set = new TreeSet();
	    if (iws.isValidPOS(NOUN))
		{
		    IndexWord noun = iws.getIndexWord(NOUN);
		    PointerTargetTree ptt  =  pu_.getHypernymTree(noun.getSense(1));
		    Word [] word_list = ptt.getRootNode().getSynset().getWords();
		    list = getSecondLevelWords(ptt);
		}
	    list.addAll(set);

	    //System.out.println("The second level hypernym List: " + list);
	    return list;
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
    /*find indirected hypernyms */
    public ArrayList getSecondLevelWords(PointerTargetTree ptt) {
	try {
	    //return the second level (except root node) elements from
	    ArrayList word_list = new ArrayList();
	    PointerTargetTreeNode pttn = ptt.getRootNode();
	    PointerTargetTreeNodeList pttnl = pttn.getChildTreeList();
	    for (int i = 0; i < pttnl.size(); i++) {
		PointerTargetTreeNode pttn1 = (PointerTargetTreeNode) pttnl.get(i);
		PointerTargetTreeNodeList pttnl1 = pttn1.getChildTreeList();
		if (pttnl1 == null)
		    return null;
		for (int j = 0; j < pttnl1.size(); j++) {
		    //		System.out.println("The pointerTargetTreeNode is: " + pttn1.toString());
		    PointerTargetTreeNode pttn2 = (PointerTargetTreeNode) pttnl1.get(j);
		    Word[] word_l = pttn2.getSynset().getWords();
		    for (int k = 0; k < word_l.length; k++) {
			word_list.add(word_l[k].getLemma());
		    }
		}
	    }
	    return word_list;

	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }


    public void loadSynonymList(String file_name) {
	try {
	    File file = new File(file_name);
	    file.createNewFile();
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    loadList(br, n_synonym_list_);
	    synonym_file_ = file_name;
	}catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void loadHypernymList(String file_name) {
	try {
	    File file = new File(file_name);
	    file.createNewFile();
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    loadList(br, hyper_noun_lists_);
	    hypernym_file_ = file_name;
	}catch (Exception e) {
	    e.printStackTrace();
	}
    }


    public void loadList(BufferedReader br,
			 HashMap container) throws Exception {
	String a_line = null;
	while ((a_line = br.readLine()) != null) {
	    if (a_line.trim().endsWith(":")) {
		String key = a_line.trim().substring(0, a_line.trim().length() - 1);
		a_line = br.readLine();
		if (a_line == null ||
		    a_line.trim().length() == 0) {
		    //no definition
		    continue;
		}
		else {
		    a_line = a_line.replaceAll("\\[\\]", "");
		    String[] words = a_line.split("[, ]+");
		    container.put(key, new ArrayList(Arrays.asList(words)));
		}
	    }
	}
    }


    public void writeListToFile() {
	try {
	    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(synonym_file_)));
	    writeListToFile(bw, n_synonym_list_);
	    bw.close();
	    bw = new BufferedWriter(new FileWriter(new File(hypernym_file_)));
	    writeListToFile(bw, hyper_noun_lists_);
	    bw.close();
	}catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void writeListToFile(BufferedWriter bw,
				HashMap container) throws Exception {
	ArrayList keys = new ArrayList(container.keySet());
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    bw.write(key + ":");
	    bw.newLine();
	    bw.write(((ArrayList)container.get(key)).toString());
	    bw.newLine();
	    bw.newLine();
	}
    }

    public void cleanUp() {
	hyper_noun_lists_.clear();
	hyper_verb_lists_.clear();
	hypo_noun_lists_.clear();
	hypo_verb_lists_.clear();
	n_synonym_list_.clear();
	a_synonym_list_.clear();
	v_synonym_list_.clear();
	lemmas_.clear();
	lemma_.clear();
    }

    /**
     * set bad senses for triggers
     */
    public void initBadSenses() {
	//process conflict attack event
	try {
	    ArrayList bad_senses = new ArrayList();
	    POS _pos = POS.getPOSForLabel("noun");
	    IndexWord iw = lookupIndexWord(_pos, "attack");
	    bad_senses.add(iw.getSense(3)); //intense adverse criticism
	    _pos = POS.getPOSForLabel("verb");
	    iw = lookupIndexWord(_pos, "attack");
	    bad_senses.add(iw.getSense(2)); //attack in speech or writing
	    _pos = POS.getPOSForLabel("verb");
	    iw = lookupIndexWord(_pos, "kill");
	    bad_senses.addAll(Arrays.asList(iw.getSenses())); //attack in speech or writing
	    _pos = POS.getPOSForLabel("noun");
	    iw = lookupIndexWord(_pos, "kill");
	    bad_senses.addAll(Arrays.asList(iw.getSenses())); //attack in speech or writing
	    _pos = POS.getPOSForLabel("noun");
	    iw = lookupIndexWord(_pos, "killing");
	    bad_senses.addAll(Arrays.asList(iw.getSenses())); //attack in speech or writing
	    bad_senses_.put("Conflict_Attack", bad_senses);
	    bad_senses = new ArrayList();
	    _pos = POS.getPOSForLabel("noun");
	    iw = lookupIndexWord(_pos, "recompense");
	    bad_senses.add(iw.getSense(2)); //intense adverse criticism
	    _pos = POS.getPOSForLabel("verb");
	    iw = lookupIndexWord(_pos, "recompense");
	    bad_senses.add(iw.getSense(1)); //attack in speech or writing
	    bad_senses_.put("Transaction_Transfer-Money", bad_senses);
	}catch (JWNLException je){
	    je.printStackTrace();
	}
    }

    /**
     * whether this sense or the hypernym of this sense contains bad senses
     */

    public boolean isBadSense(Synset sense) throws Exception{
/*
	String key = Bootstrap.type_ + "_" + Bootstrap.sub_type_;
	ArrayList bad_senses = (ArrayList)bad_senses_.get(key);
	if (bad_senses == null) {
	    return false;
	}
	if (bad_senses.contains(sense)) {
	    return true;
	}
	PointerTargetTree ptt = pu_.getHypernymTree(sense);
	List ptnls = ptt.toList();
	for (int i = 0; i < ptnls.size(); i++) {
	    PointerTargetNodeList ptnl = (PointerTargetNodeList)ptnls.get(i);
	    for (int j = 0; j < ptnl.size(); j++) {
		PointerTargetNode ptn = (PointerTargetNode)ptnl.get(j);
		for (int k = 0; k < bad_senses.size(); k++) {
		    Synset bad_sense = (Synset)bad_senses.get(k);
		    if (sense.getWord(0).getLemma().equals("abuse")) {
			//System.out.println("compare abuse with it's hypernym" + ptn);
			//System.out.println("bad sense is: " + bad_sense);
		    }
		    if (bad_sense.equals(ptn.getSynset())) {
			//			System.out.println("find a bad one!!!!!!!!!!!!");
			//			throwable_.printStackTrace();
			return true;
		    }
		}
	    }
	} */
	return false;
    }

    public POS getPOS (String pos,
		       String cont) {
	return POS.getPOSForLabel(getPOS(pos));
    }

    public String getPOS(String pos) {
	if (pos.equals("N")) {
	    pos = "noun";
	    return pos;
	}
	else if (pos.equals("V")) {
	    pos = "verb";
	    return pos;
	}
	else if (pos.equals("A")) {
	    pos = "adjective";
	    return pos;
	}

	return null;
    }



    public HashMap hyper_noun_lists_ = new HashMap();
    public HashMap hyper_adj_lists_ = new HashMap();
    public HashMap hyper_verb_lists_ = new HashMap();
    public HashMap hypo_noun_lists_ = new HashMap();
    public HashMap hypo_verb_lists_ = new HashMap();
    public final POS NOUN = POS.NOUN;
    public final POS VERB = POS.VERB;
    public final POS ADJECTIVE = POS.ADJECTIVE;
    public final POS ADVERB = POS.ADVERB;
    public HashMap n_synonym_list_ = new HashMap();
    public HashMap a_synonym_list_ = new HashMap();
    public HashMap v_synonym_list_ = new HashMap();
    public String synonym_file_ = null;
    public String hypernym_file_ = null;
    public HashMap lemmas_ = new HashMap(); //hashmap of hashmap
    public HashMap lemma_ = new HashMap();
    public HashMap w_lemmas_ = new HashMap();
    public HashMap senses_ = new HashMap();
    public HashMap bad_senses_ = new HashMap(); //key is type_sub-type and the value is arraylist of senses which shouldn't be used to for trigger
    RelationshipFinder rf_ = RelationshipFinder.getInstance();
    //    public Throwable throwable_ = new Throwable();
}
