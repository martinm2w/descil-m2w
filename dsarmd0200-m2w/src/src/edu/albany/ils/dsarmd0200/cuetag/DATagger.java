package edu.albany.ils.dsarmd0200.cuetag;

import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.lu.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * this class is used for assigning the highest prediction tag for the utterance
 *
 * @author Ting Liu
 *
 */

public class DATagger {
	
    private String utteranceStr; //input string to be tagged
    private String utteranceTag; //tag to be assigned to the input string
    private String taggedUtterance; //output string with tag
    private int utteranceLength; //the length of the input string
    private String[] fileStr; //hold the DA n-gram data
    private HashSet stop_words_ = new HashSet(); //to hold all stop words
    private ArrayList<String> q_word; //special questin key word/phrase
    private int model; //the value of n in the n-gram
    private HashMap propertyMap; //hold the n-gram data
    private HashMap totalMap; 
    final static int LENGTH_LIMIT = 25; //if the input string/utterance is longer than 25 words, it will be discarded
    final static String NL = System.getProperty("line.separator");
    public String path ="/home/ting/develop/deer/data/swbd_202k_42tags_noplus.txt.ngram";
    public final static String DATAG = "datag";
    public final static String POLARITY = "polairty";
    public final static String COMMACT = "comact";
    public final static String DISAGREE_REJECT = "disagree-reject";
    public final static String NDISAGREE_REJECT1 = "Disagree-Reject";
    public final static String NDISAGREE_REJECT2 = "disagree-reject";
    public final static String AGREE_ACCEPT = "agree-accept";
    public final static String NAGREE_ACCEPT1 = "agree-accept";
    public final static String NAGREE_ACCEPT2 = "Agree-Accept";
    private Wordnet wn_ = null;
    private ArrayList spks_ = new ArrayList();
    private ArrayList utts_ = new ArrayList();

    public DATagger(String _path) {
	path = _path;
        init_q_word();
    }
	
    public DATagger() {
        init_q_word();
    }

    public void setPath(String _path) {
	path = _path;
    }

    public void setWN (Wordnet wn) { wn_ = wn; }
	
    //this method is used to read the property n gram data corpus, and build the global HashMap propertyMap
    //propertyMap --- Key = model_number(int):ngram(string)  Value = tag(String):frequency(int):prediction(double)   
    public void load_map() {
	String line;
	String[] str;
	String key;
	String value;
	propertyMap = new HashMap<String, String>();
	try{
	    BufferedReader br = new BufferedReader(new FileReader(new File(path)));
	    while((line=br.readLine())!=null) {//line = [2:%:all the <finish>:4:1.0] --- example
		line = line.replaceAll("[\\[\\]]", "");	
		//System.out.println("line is: " + line);		
		str = line.split(":");
		key = str[0]+":"+str[2];
		value = str[1]+":"+str[3]+":"+str[4];
		if (propertyMap.containsKey(key)) {
		    String ev = (String)propertyMap.get(key);
		    String [] evs = ev.split(":");
		    double ev_pred = (new Double(evs[2])).doubleValue();
		    int ev_fre = (new Integer(evs[1])).intValue();

		    double pred = (new Double(str[4])).doubleValue();
		    int fre = (new Integer(str[3])).intValue();
		    
		    if (ev_pred < pred) {
			propertyMap.put(key, value);
		    }
		    else if (ev_pred == pred &&
			     ev_fre < fre) {
			propertyMap.put(key, value);
		    }
		}
		else {
		    propertyMap.put(key, value);
		}
	    }
	} catch(Exception e) {
	    e.printStackTrace();
	}

    }
	
    
    
    
    //this method is used to read the property n gram data corpus, and build the global HashMap propertyMap
    //propertyMap --- Key = model_number(int):ngram(string)  Value = tag(String):frequency(int):prediction(double)   
    public void load_map(String _path) {
	setPath(_path);
	load_map();
    }
	
    
    
    
    private void init_q_word() {
	q_word = new ArrayList<String>();
	q_word.add("<start> what"); q_word.add("<start> how"); q_word.add("<start> when"); 
	q_word.add("<start> which"); q_word.add("<start> where"); q_word.add("<start> who");
	q_word.add("<start> does"); q_word.add("<start> do"); q_word.add("<start> did");
	q_word.add("<start> is"); q_word.add("<start> was"); q_word.add("<start> are");
	q_word.add("<start> were"); q_word.add("<start> am"); q_word.add("<start> can"); 
	q_word.add("<start> could"); q_word.add("<start> would"); q_word.add("<start> may"); 
	q_word.add("<start> might"); q_word.add("<start> should"); q_word.add("<start> have"); 
	q_word.add("<start> has"); q_word.add("<start> had");
    }
    
    
    public void setPropertyMap(HashMap map_property) {
	this.propertyMap = map_property;
    }
    
    public void setTotalMap (HashMap map_total) {
	this.totalMap = map_total;
    }

    public void setSpeakers(ArrayList spks) { spks_ = spks; }
    public void setUtts (ArrayList utts) { utts_ = utts; }
    
    //before using this method, please remove "__", that is, call tagUtterance() rather than tagUtterance__()
    public String tagUtteranceWithSWBD(String chatStr,
				       boolean skip_sw) {
	boolean flag; 
	boolean isQuestion = false; //test whether the input string contains the question mark or not
	if(chatStr.trim().endsWith("?")) isQuestion = true;
	this.utteranceStr = chatStr;
	int i=0, l=0, m=0, j=0, k=0;
	
	//System.out.println("test opening of file: " + DAfile.getAbsolutePath() );
	String gramStr = "";
	String[] allGram;
	//System.out.println("hahaha: "+this.utteranceStr.toLowerCase().replaceAll("[^a-zA-Z_0-9'\\s]", ""));
	String[] word = this.utteranceStr.toLowerCase().replaceAll("[^a-zA-Z_0-9'\\s]", " ").split("\\s+");
	if (!skip_sw) {
	    ArrayList words = new ArrayList(Arrays.asList(word));
	    //	    System.out.println("before filter: " + words);
	    Util.sws_.filterIt(words);
	    //	    System.out.println("after filter: " + words);
	    word = new String [words.size()];
	    word = (String [])words.toArray(word);
	}
	this.utteranceLength = word.length; //get the utterance length
	String[] newWord = new String[this.utteranceLength + 2];
	
	//determine the model number by the word length of the utterance
	if(this.utteranceLength == 1) {
	    this.model = 1;
	} else if(this.utteranceLength >=2 && this.utteranceLength <=4) {
	    this.model = 2;
	} else if(this.utteranceLength >4 /*&& this.utteranceLength <= LENGTH_LIMIT*/) {
	    this.model = 3;
	} else {
	    //System.out.println("the Chat Utterance is empty, discard! the chatStr:" + chatStr);
	    return "";
	}
	
	newWord[0] = "<start>";
	newWord[this.utteranceLength + 1] = "<finish>";  //by Qi Feb 06
	for(i=0; i<this.utteranceLength; i++ ) {
	    newWord[i+1] = word[i];
	}
	if (newWord.length > 9) { //words length > 7 + (start and end)
	    String[] tmp_words = new String [8];
	    for (i = 0; i < 8; i++) {
		tmp_words[i] = newWord[i];
	    }
	    newWord = tmp_words;
	    //ori_words = (String[]) Arrays.asList(tmp_words).toArray();
	}
	//build the n-gram, where n can be 1, 2, 3 or 4
	m = newWord.length>4? 4 : newWord.length;
	if(m == 4) {
	    allGram = new String[4*newWord.length - 6];
	} else {
	    allGram = new String[(newWord.length+1)*newWord.length/2];
	}
	for(l=1; l<=m; l++) {// length of subgram
	    if(l>2) {
		for(i=0; i<newWord.length-l+1; i++){
		    gramStr = "";
		    for(j=i; j<i+l; j++) {
			gramStr +=newWord[j]+" ";
		    }
		    //if(!(l<3 && stopStr.contains(gramStr)))
		    allGram[k++] = gramStr.trim();
		}
	    } 
	    
	    else {
		for(i=0; i<newWord.length-l+1; i++){
		    gramStr = "";
		    flag = false;
		    for(j=i; j<i+l; j++) {
			// System.out.println(newWord[j]);
			gramStr +=newWord[j]+" ";
		    }
		    if(flag == false) allGram[k++] = gramStr.trim();
		    if(flag == true && l==2 && q_word.indexOf(gramStr.trim())>=0) {
			allGram[k++] = gramStr.trim();
		    }
		}			
	    }
	    
	}
	
	this.utteranceTag = getBestTagSWBD(this.model, allGram);

	
	/*****processing question mark***********/
	if (isQuestion == true && (this.utteranceTag.equals("sd") || this.utteranceTag.equals("sv"))) {
	    this.utteranceTag = "qy";
	}
	else if (isQuestion == true) {
	    this.utteranceTag = "qw";
	}
    
	
	this.taggedUtterance = this.utteranceTag + "###" + this.utteranceStr;
	//return this.taggedUtterance;
        return this.utteranceTag;
    }
    
    //this model is temporarily changed to finish some training-testing specifications
    public ObjValue[] tagUtterance(String chatStr, 
				   String realTag,
				   boolean skip_sw,
				   boolean with_SE_tags,
				   String tag_type,
				   boolean has_mesotopic,
				   Utterance utt) {
	boolean flag;
	boolean isQuestion = false; //test whether the input string contains the question mark or not
	if(chatStr.endsWith("?")) { isQuestion = true; }
	this.utteranceStr = chatStr;
	int i=0, l=0, m=0, j=0, k=0;
	ObjValue[] obj;
	
	//System.out.println("test opening of file: " + DAfile.getAbsolutePath() );
	String gramStr = "";
	String[] allGram;
	//System.out.println("hahaha: "+this.utteranceStr.toLowerCase().replaceAll("[^a-zA-Z_0-9'\\s]", ""));
	utteranceStr = Util.filterIt(utteranceStr).toLowerCase();
	//String[] word = this.utteranceStr.toLowerCase().replaceAll("[^a-zA-Z_0-9'\\s]", "").split("\\s+"); comment out 03/16
	//utteranceStr = CommActGround.textToPosString(utteranceStr);
	//System.out.println("utteranceStr: " + utteranceStr);
	String[] word = this.utteranceStr.split("[\\s]+");
	ArrayList words = new ArrayList(Arrays.asList(word));
	//System.out.println("words: " + words);
	if (!skip_sw) {
	    //ArrayList words = new ArrayList(Arrays.asList(word));
	    //System.out.println("words: " + words);
	    Util.sws_.filterIt(words);
	    word = new String [words.size()];
	    word = (String [])words.toArray(word);
	}
	/*
	  if (words.size() == 0) {
	  //only start and finish left
	  return null;
	  }
	*/
	this.utteranceLength = word.length; //get the utterance length
	String[] newWord = new String[this.utteranceLength];
	
	//determine the model number by the word length of the utterance
	if(this.utteranceLength == 1) {
	    this.model = 1;
	} else if(this.utteranceLength >=2 && this.utteranceLength <=4) {
	    this.model = 2;
	} else if(this.utteranceLength >4 /*&& this.utteranceLength <= LENGTH_LIMIT*/) {
	    this.model = 3;
	} else {
	    //System.out.println("the Chat Utterance is empty, discard!");
	    return null;
	}
	
	if (with_SE_tags) {
	    newWord = new String[this.utteranceLength + 2];
	    newWord[0] = "<start>";
	    newWord[this.utteranceLength + 1] = "<finish>";  //by TL 11/14/07
	}
	for(i=0; i<this.utteranceLength; i++ ) {
	    if (with_SE_tags) {
		newWord[i+1] = word[i];
	    }
	    else {
		newWord[i] = word[i];
	    }
	}
	ArrayList wds = new ArrayList(Arrays.asList(newWord));
	for (i = 0; i < wds.size(); i++) {
	    String wd = (String)wds.get(i);
	    if (spks_ != null &&
		spks_.contains(wd)) {
		wds.set(i, "@SPEAKER@");
	    }
	    if (wd.equals("<start>") ||
		wd.equals("<finish>")) continue;
	    if (wn_ != null) {
		String lemma = wn_.getLemma(wd);
		if (lemma != null) {
		    wds.set(i, lemma);
		}
	    }
	    wd = (String)wds.get(i);
	    if (utt != null &&
		utt.getCommActType() != null) {
		wds.set(i, utt.getCommActType() + "_" + wd);
	    }
	    wd = (String)wds.get(i);
	    if (utt != null) {
		String linkto = utt.getRespTo();
		if (linkto != null &&
		    linkto.indexOf(":") != -1) {
		    String turn = linkto.split(":")[1];
		    turn = turn.split("\\.")[0];
		    Utterance ln_utt = getUtterance(turn);
		    if (ln_utt != null) {
			//System.out.println("ln_utt's tag: " + ln_utt.getTag());
			wds.set(i, ln_utt.getTag() + "_" + wd);
		    } 
		}
	    }


	}
	newWord = (String [])wds.toArray(newWord);
	//build the n-gram, where n can be 1, 2, 3 or 4
	m = newWord.length>4? 4 : newWord.length;
	if(m == 4) {
	    allGram = new String[4*newWord.length - 6];
	} else {
	    allGram = new String[(newWord.length+1)*newWord.length/2];
	}
	for(l=1; l<=m; l++) {// length of subgram
	    for(i=0; i<newWord.length-l+1; i++){
		gramStr = "";
		for(j=i; j<i+l; j++) {
		    gramStr +=newWord[j]+" ";
		}
		if (tag_type.equals(POLARITY)) {
		    if (has_mesotopic) {
			allGram[k++] = "has_mesotopic_" + gramStr.trim();
		    } else {
			allGram[k++] = "no_mesotopic_" + gramStr.trim();
		    }
		} else allGram[k++] = gramStr.trim();

		//		System.out.println("n-gram is: " + gramStr);
	    } 
	}
	for (l=0; l < allGram.length; l++) {
	    //System.out.println("allGram " + l + ": " + allGram[l]);
	}
	
	
	//this.utteranceTag = getBestTag(this.model, allGram, realTag); // format: tag###model_number:ngram
	obj= getBestTag(this.model, allGram, realTag, tag_type); // format: tag###model_number:ngram
	this.utteranceTag = obj[0].getTag();
	if(isQuestion == true && (this.utteranceTag.equals("sd") || this.utteranceTag.equals("sv")))
	    this.utteranceTag = "q";
	
	this.taggedUtterance = this.utteranceTag + "###" + this.utteranceStr;
	//return this.taggedUtterance;
	//tag_Ngram[0] = this.utteranceTag;
	obj[0].setTag(this.utteranceTag);
	if (isQuestion && tag_type.equals(COMMACT)) { obj[0].setTag(CommunicationLink.ADDRESSED_TO); }
	return obj;
        //return this.utteranceTag; // format: tag###model_number:ngram
    }
    

    //this model is temporarily changed to finish some training-testing specifications
    public ObjValue[] tagUtterance(String chatStr, 
				   String realTag,
				   boolean skip_sw) {
	boolean flag;
	boolean isQuestion = false; //test whether the input string contains the question mark or not
	if(chatStr.trim().endsWith("?")) isQuestion = true;
	this.utteranceStr = chatStr;
	int i=0, l=0, m=0, j=0, k=0;
	ObjValue[] obj;
	
	//System.out.println("test opening of file: " + DAfile.getAbsolutePath() );
	String gramStr = "";
	String[] allGram;
	//System.out.println("hahaha: "+this.utteranceStr.toLowerCase().replaceAll("[^a-zA-Z_0-9'\\s]", ""));
	String[] word = this.utteranceStr.toLowerCase().replaceAll("[^a-zA-Z_0-9'\\s]", "").split("\\s+");
	if (!skip_sw) {
	    ArrayList words = new ArrayList(Arrays.asList(word));
	    Util.sws_.filterIt(words);
	    word = new String [words.size()];
	    word = (String [])words.toArray(word);
	}
	/*
	  if (words.size() == 0) {
	  //only start and finish left
	  return null;
	  }
	*/
	this.utteranceLength = word.length; //get the utterance length
	String[] newWord = new String[this.utteranceLength + 2];
	
	//determine the model number by the word length of the utterance
	if(this.utteranceLength == 1) {
	    this.model = 1;
	} else if(this.utteranceLength >=2 && this.utteranceLength <=4) {
	    this.model = 2;
	} else if(this.utteranceLength >4 /*&& this.utteranceLength <= LENGTH_LIMIT*/) {
	    this.model = 3;
	} else {
	    //System.out.println("the Chat Utterance is empty, discard!");
	    return null;
	}
	
	newWord[0] = "<start>";
	newWord[this.utteranceLength + 1] = "<finish>";  //by Qi Feb 06
	for(i=0; i<this.utteranceLength; i++ ) {
	    newWord[i+1] = word[i];
	}
	//build the n-gram, where n can be 1, 2, 3 or 4
	m = newWord.length>4? 4 : newWord.length;
	if(m == 4) {
	    allGram = new String[4*newWord.length - 6];
	} else {
	    allGram = new String[(newWord.length+1)*newWord.length/2];
	}
	for(l=1; l<=m; l++) {// length of subgram
	    for(i=0; i<newWord.length-l+1; i++){
		gramStr = "";
		for(j=i; j<i+l; j++) {
		    gramStr +=newWord[j]+" ";
		}
		allGram[k++] = gramStr.trim();
	    } 
	    
	    
	}
	
	//this.utteranceTag = getBestTag(this.model, allGram, realTag); // format: tag###model_number:ngram
	obj= getBestTag(this.model, allGram, realTag); // format: tag###model_number:ngram
	this.utteranceTag = obj[0].getTag();
	/*****processing question mark***********/
	if (isQuestion == true && (this.utteranceTag.equals("sd") || this.utteranceTag.equals("sv"))) {
	    this.utteranceTag = "qy";
	}
	else if (isQuestion == true) {
	    this.utteranceTag = "qw";
	}
	/*    
	if(isQuestion == true && (this.utteranceTag.equals("sd") || this.utteranceTag.equals("sv")))
	    this.utteranceTag = "q";
	*/
	this.taggedUtterance = this.utteranceTag + "###" + this.utteranceStr;
	//return this.taggedUtterance;
	//tag_Ngram[0] = this.utteranceTag;
	obj[0].setTag(this.utteranceTag);
	return obj;
        //return this.utteranceTag; // format: tag###model_number:ngram
    }
    
    //this method will use dynamic prediction computation to get the best tag
    private String getBestTagSWBD/*has dynamic computing*/(int model_number, String[] str) {
	String key = null;
	String value[] = null;
	//ObjValue totalValue = null; // used for totalMap
	
	double prediction = 0.0;
	double maxPrediction = 0.0;
	int frequency = 0;
	int maxFrequency = 0;
	int maxLength = 0; //dynamic computing
	int curLength = 0;// dynamic computing
	int difLength = 0;// dynamic computing
	double preDif = 0.0; //dynamic computing
	String tag = null;
	String maxTag = null;
	String maxStr = null;//dynamic computing
	
	for(int i = 0; i<str.length; i++) {
	    if(str[i] == null) break;
	    key = model_number + ":" + str[i];
	    if(!propertyMap.containsKey(key)){
		//System.out.println("Key is not in This Map: " + key);
		continue;
	    }
	    //System.out.println("Key is in This Map: " + key);
	    value = ((String)propertyMap.get(key)).split(":");
	    tag=value[0];
	    frequency = Integer.parseInt(value[1]);
	    prediction = Double.parseDouble(value[2]);
	    //for dynamic computing
	    if(maxStr!=null) {
		maxLength = maxStr.split(" ").length;
		curLength = str[i].split(" ").length;
		difLength = curLength - maxLength;
		
		if(Math.abs(difLength) == 1) preDif = difLength * 0.15;
		else if(Math.abs(difLength) ==2) preDif = (difLength/2)*0.25;
		else if(Math.abs(difLength) ==3) preDif = (difLength/3)*0.30;
		else preDif = 0.0;
	    }
	    
	    if(prediction + preDif > maxPrediction) {
		maxPrediction = prediction;
		maxFrequency = frequency;
		maxTag = tag;
		maxStr = str[i];
	    } else if(prediction + preDif == maxPrediction && frequency > maxFrequency) {
		maxPrediction = prediction;
		maxFrequency = frequency;
		maxTag = tag;
		maxStr = str[i];			        
	    }
	    
	    
	    
	    
	}
	
	
	if (maxTag == null) return "sd";//default to be a st tagged statement

	//System.out.println("maxTag is: " + maxTag);
	return maxTag;
	
    }
	
	
	
    //before calling this method, please remove"__" in the method name
    
    private String getBestTag/*__no dynamic computing*/(int model_number, String[] str) {
	String key = null;
	ObjValue value = null;
	double prediction = 0.0;
	double maxPrediction = 0.0;
	int frequency = 0;
	int maxFrequency = 0;
	String tag = null;
	String maxTag = null;
	
	for(int i = 0; i<str.length; i++) {
	    if(str[i] == null) break;
	    key = model_number + ":" + str[i];
	    if(!propertyMap.containsKey(key)){
                //System.out.println("Key is not in This Map: " + key);
		continue;
	    }
            //System.out.println("Key is in This Map: " + key);
	    value = (ObjValue)propertyMap.get(key);
	    prediction = value.getPrediction();
	    frequency = value.getFrequency();
	    tag = value.getTag();
	    if(prediction > maxPrediction) {
		maxPrediction = prediction;
		maxFrequency = frequency;
		maxTag = tag;
	    } else if(prediction == maxPrediction) {
		if(frequency > maxFrequency) {
		    maxPrediction = prediction;
		    maxFrequency = frequency;
		    maxTag = tag;
		}
	    }
	}
	
	if (maxTag == null) return "unknown";
	else return maxTag;
	
	
    }
    private ObjValue[] getBestTag/*has dynamic computing*/(int model_number, String[] str, String realTag) {
	String key = null;
	ObjValue value = null;
        ObjValue totalValue = null; // used for totalMap
	
	double prediction = 0.0;
	double maxPrediction = 0.0;
	int frequency = 0;
	int maxFrequency = 0;
	int maxLength = 0; //dynamic computing
	int curLength = 0;// dynamic computing
	int difLength = 0;// dynamic computing
	double preDif = 0.0; //dynamic computing
	String tag = null;
	String maxTag = null;
	String maxStr = null;//dynamic computing
	String maxKey = null; //specify the n-gram which is used to tag the utterance
	
	ObjValue maxValue = null;
	//ObjValue[0]stores the ngram information used to tag the utterance
	//ObjValue[1]stores the ngram information that would be right if used to tag the utterance
	ObjValue[] obj = new ObjValue[2];
	String correctNgram = null;
	double maxCorrectPrediction = 0.0;
	int maxCorrectFrequency =0;
	
	ObjValue maxCorrectValue = null;
	
	for(int i = 0; i<str.length; i++) {
	    if(str[i] == null) break;
	    key = model_number + ":" + str[i];
            if(!propertyMap.containsKey(key)){
                //System.out.println("Key is not in This Map: " + key);
		continue;
	    }
            //System.out.println("Key is in This Map: " + key);
	    value = (ObjValue)propertyMap.get(key);
       	    prediction = value.getPrediction();
	    frequency = value.getFrequency();
	    tag = value.getTag();
	    //for dynamic computing
	    if(maxStr!=null) {
		maxLength = maxStr.split(" ").length;
		curLength = str[i].split(" ").length;
		difLength = curLength - maxLength;
		
		if(Math.abs(difLength) == 1) preDif = difLength * 0.15;
		else if(Math.abs(difLength) ==2) preDif = (difLength/2)*0.25;
		else if(Math.abs(difLength) ==3) preDif = (difLength/3)*0.30;
		else preDif = 0.0;
	    }
	    
	    if(prediction + preDif > maxPrediction) {
		maxPrediction = prediction;
		maxFrequency = frequency;
		maxTag = tag;
		maxStr = str[i];
		maxValue = value;
	    } else if(prediction + preDif == maxPrediction && frequency > maxFrequency) {
		maxPrediction = prediction;
		maxFrequency = frequency;
		maxTag = tag;
		maxStr = str[i];
		maxValue = value;
	    }
	    
	    if(value.getTag().equals(realTag)) {
		if(value.getPrediction()>maxCorrectPrediction){
		    correctNgram = key;
		    maxCorrectValue = value;
		}
		
		else if(value.getPrediction() == maxCorrectPrediction && value.getFrequency()>maxCorrectFrequency) {
		    correctNgram = key;
		    maxCorrectValue = value;
		}
		
	    }
	    
	    
	}
	
	
	if (maxTag == null) {
	    //obj[0] = new ObjValue(null, "unknown", 0, 0.0);
	    obj[0] = new ObjValue(null, "sd", 0, 0.0);
	    //obj[0] = new ObjValue(null, "Assertion-Opinion", 0, 0.0);
	    return obj;
	}
	else {
	    maxKey = model_number + ":" + maxStr;
	    value = (ObjValue)propertyMap.get(maxKey);
            totalValue = (ObjValue)totalMap.get(maxKey);
            //for testing, will be removed
	    //if(value == null) //System.out.println("the value is null");
            if(totalValue == null) {
            	//System.out.println("key is: " + maxKey);
            	//System.out.println("the total value is null");
            }
	    
	    if(maxTag.equals(realTag)) {
		value.increaseUseFrequency();
		value.increaseCorrectFrequency();
                totalValue.increaseUseFrequency();
		totalValue.increaseCorrectFrequency();
                
	    } else {
		value.increaseUseFrequency();
		value.increaseWrongFrequency();
		totalValue.increaseUseFrequency();
		totalValue.increaseWrongFrequency();
	    }
	    obj[0] = maxValue;
	    if(!maxTag.equals(realTag)) obj[1] = maxCorrectValue;
	    else obj[1] = maxValue;
	    return obj;
	}
	
    }


    private ObjValue[] getBestTag/*has dynamic computing*/(int model_number, 
							   String[] str, 
							   String realTag, 
							   String tag_type) {
	String key = null;
	ObjValue value = null;
        ObjValue totalValue = null; // used for totalMap
	
	double prediction = 0.0;
	double maxPrediction = 0.0;
	int frequency = 0;
	int maxFrequency = 0;
	int maxLength = 0; //dynamic computing
	int curLength = 0;// dynamic computing
	int difLength = 0;// dynamic computing
	double preDif = 0.0; //dynamic computing
	String tag = null;
	String maxTag = null;
	String maxStr = null;//dynamic computing
	String maxKey = null; //specify the n-gram which is used to tag the utterance
	
	ObjValue maxValue = null;
	//ObjValue[0]stores the ngram information used to tag the utterance
	//ObjValue[1]stores the ngram information that would be right if used to tag the utterance
	ObjValue[] obj = new ObjValue[2];
	String correctNgram = null;
	double maxCorrectPrediction = 0.0;
	int maxCorrectFrequency =0;
	
	ObjValue maxCorrectValue = null;
	boolean hasdont = false;
	for (int i = 0; i < str.length; i++) {
	    if (str[i].indexOf("don't") != -1) {
		//hasdont = true;
		break;
	    }
	}
	for(int i = 0; i<str.length; i++) {
	    if(str[i] == null) break;
	    key = model_number + ":" + str[i];
            if(!propertyMap.containsKey(key)){
		/*
		if (key.indexOf("don't") != -1) {
		    System.out.println("Key is not in This Map: " + key);
		}
		*/
		continue;
	    }
	    /*
	    if (key.indexOf("don't") != -1) {
		System.out.println("Key is in This Map: " + key);
	    }
	    */
	    value = (ObjValue)propertyMap.get(key);
       	    prediction = value.getPrediction();
	    frequency = value.getFrequency();
	    tag = value.getTag();
	    //for dynamic computing
	    if(maxStr!=null) {
		maxLength = maxStr.split(" ").length;
		curLength = str[i].split(" ").length;
		difLength = curLength - maxLength;
		
		if(Math.abs(difLength) == 1) preDif = difLength * 0.15;
		else if(Math.abs(difLength) ==2) preDif = (difLength/2)*0.25;
		else if(Math.abs(difLength) ==3) preDif = (difLength/3)*0.30;
		else preDif = 0.0;
	    }
	    
	    if(prediction + preDif > maxPrediction) {
		maxPrediction = prediction;
		maxFrequency = frequency;
		maxTag = tag;
		maxStr = str[i];
		maxValue = value;
	    } else if(prediction + preDif == maxPrediction && frequency > maxFrequency) {
		maxPrediction = prediction;
		maxFrequency = frequency;
		maxTag = tag;
		maxStr = str[i];
		maxValue = value;
	    }
	    
	    if(value.getTag().equals(realTag)) {
		if(value.getPrediction()>maxCorrectPrediction){
		    correctNgram = key;
		    maxCorrectValue = value;
		}
		
		else if(value.getPrediction() == maxCorrectPrediction && value.getFrequency()>maxCorrectFrequency) {
		    correctNgram = key;
		    maxCorrectValue = value;
		}
		
	    }
	    if (hasdont) {
		System.out.println("maxTag: " + maxTag);
		System.out.println("maxPrediction: " + maxPrediction);
		System.out.println("maxStr: " + maxStr);
		System.out.println("maxValue: " + maxValue);
	    }
	}
	
	if (hasdont) {
	    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\nmaxTag: " + maxTag);
	    System.out.println("maxPrediction: " + maxPrediction);
	    System.out.println("maxStr: " + maxStr);
	    System.out.println("maxValue: " + maxValue);
	    for (int i = 0; i < str.length; i++) {
		System.out.print(" ," + str[i]);
	    }
	    System.out.println();
	}
	
	
	if (maxTag == null) {
	    //obj[0] = new ObjValue(null, "unknown", 0, 0.0);
	    if (tag_type.equals(DATAG)) {
		obj[0] = new ObjValue(null, "unknown"/*"Assertion-Opinion"*/, 0, 0.0);
	    } else if (tag_type.equals(POLARITY)) {
		obj[0] = new ObjValue(null, Opinion.NEUTRAL, 0, 0.0);
	    } else if (tag_type.equals(COMMACT)) {
		//obj[0] = new ObjValue(null, CommunicationLink.ADDRESSED_TO, 0, 0.0);
		obj[0] = new ObjValue(null, CommunicationLink.RESPONSE_TO, 0, 0.0);
		//obj[0] = new ObjValue(null, null, 0, 0.0);
	    }
	    return obj;
	}
	else {
	    maxKey = model_number + ":" + maxStr;
	    value = (ObjValue)propertyMap.get(maxKey);
            totalValue = (ObjValue)totalMap.get(maxKey);
            //for testing, will be removed
	    //if(value == null) System.out.println("the value is null");
            if(totalValue == null) {
            	//System.out.println("key is: " + maxKey);
            	//System.out.println("the total value is null");
		obj[0] = maxValue;
		if(!maxTag.equals(realTag)) obj[1] = maxCorrectValue;
		else obj[1] = maxValue;
		return obj;
            }
	    
	    if(maxTag.equals(realTag)) {
		value.increaseUseFrequency();
		value.increaseCorrectFrequency();
                totalValue.increaseUseFrequency();
		totalValue.increaseCorrectFrequency();
                
	    } else {
		value.increaseUseFrequency();
		value.increaseWrongFrequency();
		totalValue.increaseUseFrequency();
		totalValue.increaseWrongFrequency();
	    }
	    obj[0] = maxValue;
	    if(!maxTag.equals(realTag)) obj[1] = maxCorrectValue;
	    else obj[1] = maxValue;
	    return obj;
	}
	
    }
	
	
    public void setUtterance(String chatStr) {
	this.utteranceStr = chatStr;
    }
	
    
    
    public String getTaggedUtterance() {
	return this.taggedUtterance;
    }
    
    public String getUtterance() {
	return this.utteranceStr;
    }
    
    public int getLength() {
	return this.utteranceLength;
    }
    
    public int getModel() {
	return this.model;
    }
    
    public String getTag() {
	return this.utteranceTag;
    }
    
    /* Utility method read the contents of a file and return it as a String */
    
    public String[] readFile(File file){
    	StringBuffer fileBuffer1 = new StringBuffer();
    	StringBuffer fileBuffer2 = new StringBuffer();
    	StringBuffer fileBuffer3 = new StringBuffer();
    	String[] str = new String[3];
        if( !file.exists() ) {
	    System.out.println("the DA Tagger file cannot be found!");
	    return null;
        }
        try{
	    String line;            
            BufferedReader in = new BufferedReader ( new FileReader (file));
            while ((line = in.readLine()) != null) {
            	if(line.indexOf("[1:")>=0) {
		    fileBuffer1.append(line + NL);
            	} else if(line.indexOf("[2:")>=0) {
		    fileBuffer2.append(line + NL);
            	} else 
		    fileBuffer3.append(line + NL);
            }
            str[0] = fileBuffer1.toString();
            str[1] = fileBuffer2.toString();
            str[2] = fileBuffer3.toString();
            in.close();
            return str;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    public String readStopWords(File file){
	StringBuffer fileBuffer = new StringBuffer();
    	String str;
        if( !file.exists() ) {
	    System.out.println("the stop-words file cannot be found!");
	    return null;
        }
        try{
	    String line;        
	    int fl = (int)file.length();
	    char[] f_chs = new char [fl];
            BufferedReader in = new BufferedReader ( new FileReader (file));
	    in.read(f_chs);
	    String f_str = new String(f_chs);
	    in = new BufferedReader(new StringReader(f_str));
            while ((line = in.readLine()) != null) {
		fileBuffer.append(line + NL);
		stop_words_.add(line.trim());
            }
            str = fileBuffer.toString();
            in.close();
            return str;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public Utterance getUtterance(String turn) { 
	if (utts_ == null) return null;
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt = (Utterance)utts_.get(i);
	    if (utt.getTurn().equals(turn)) return utt;
	}
	return null;
    }
	
}


