package edu.albany.ils.dsarmd0200.evaltag;


import edu.albany.ils.dsarmd0200.util.xml.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import java.io.*;
import edu.albany.ils.dsarmd0200.util.*;
import java.util.*;
import java.lang.*;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Train on dsarmd and testing on dsarmd
 * @Author: Ting Liu
 */

public class CrossDsarmdValidation extends CrossValidation {
	
    String totalPath = "/projects/SHARED/data/dsarmd/annotated_files/new_ground_truth";//reynard/annotated";//"/home/ting/tools/scil_installation/data/mixed/annotated";//lauren_annotated";//"/projects/SHARED/data/dsarmd/15_tags_Ground_truth";
    String trainPath = "/projects/SHARED/data/dsarmd/dsarmd_train";
    String testPath = "/projects/SHARED/data/dsarmd/dsarmd_test";
    String daFilePath = "/projects/SHARED/data/dsarmd/dsarmd_train.ngram";
    String logPath = "/home/ting/develop/deer/data/log";
    
    String noMatchPath = "/home/ting/develop/deer/data/noMatch/noMatch";
    String wrongMatchPath = "/home/ting/develop/deer/data/wrongMatch/wrongMatch";
    String propertyMapPath = "/home/ting/develop/deer/data/propertyNGram/proertyNGram";
    String totalPropertyMapPath = "/home/ting/develop/deer/data/TotalPropertyNGram/TotalPropertyNGram";
	//public static String NL = System.getProperty("line.seperator");
	
    int Random_N = 19;
    int Random_Limit = 1155;
    int Validate_Time = 7;
    Random random = new Random();
    ArrayList<Integer> randomList = new ArrayList<Integer>();
    
    TagTraining tagTraining = new TagTraining();
    HashMap totalMap;
    DATagger daTagger = new DATagger();;
    boolean skip_sw_ = true; //whether to skip stop words processing
    HashSet processed_das_ = new HashSet();
    ArrayList train_docs_ = new ArrayList();
    ArrayList test_docs_ = new ArrayList();
    ArrayList docs_ = new ArrayList();

    final boolean clustered_ = true; //whether to clustered tag or original tag for dsarmd data
    final boolean add_SE_tags_ = true; //whether to add <start> and <finish> tags into utterance 
    public static String corpus_type_ = "dsarmd"; //which corpus is used now
    public final static String dl_act_str_ = "turn";
    
    public static DsarmdDP dsarmdDP_ = new DsarmdDP();

    private HashMap<Integer, Integer> doc_utts_ = new HashMap<Integer, Integer>(); //key: doc id, value: the number of utterance in correpoding doc
    private int total_utts_ = 0; //the total number of utterance in training data set
    private int ten_percentage_ = 0; //the 10 percent of the total number of utterance
    private HashMap<Document, String> doc_names_ = new HashMap<Document, String>(); //
    private int count_ = 0;

    private Utterance ada_ = new Utterance();

    public CrossDsarmdValidation() {
	ada_.setTagMaps();
    }

    
    public ArrayList getRandomList() {
	
	int random_node = 0;
	//		randomList.add(0); //for testing
	for(int i = 0; i<Random_N; i++) {
	    /*
	      random_node = random.nextInt(Random_Limit);
	      while(randomList.contains(Integer.valueOf(random_node))) {
	      random_node = random.nextInt(Random_Limit);
	      }
	      randomList.add(Integer.valueOf(random_node));
	    */
	    int count = i;
	    while (processed_das_.contains(Integer.valueOf(count))) {
		count++;
	    }
	    processed_das_.add(Integer.valueOf(count));
	    randomList.add(Integer.valueOf(count)); //for testing
	    
	}
	
	return randomList;
    }

    public ArrayList getTestingList() {
	ArrayList testing_list = new ArrayList();
	ArrayList<Integer> doc_ids = new ArrayList<Integer>(doc_utts_.keySet());
	int testing_total = 0;
	for (int i = 0; i < doc_ids.size(); i++) {
	    Integer doc_id = doc_ids.get(i);
	    if (processed_das_.contains(doc_id)) {
		continue;
	    }
	    if (testing_total + doc_utts_.get(doc_id) < ten_percentage_ ||
		testing_total == 0) {
		testing_total += doc_utts_.get(doc_id);
		testing_list.add(doc_id);
		processed_das_.add(doc_id);
		return testing_list; //01/27/2011
	    }
	    else {
		//over the 10% limit
		//find the one nearer to the 10% limit
		if (ten_percentage_ - testing_total < testing_total + doc_utts_.get(doc_id) - ten_percentage_ &&
		    testing_total > 0) {
		    return testing_list;
		}
		else {
		    testing_list.add(doc_id);
		    processed_das_.add(doc_id);
		    return testing_list;
		}
	    }
	}
	if (testing_list.size() > 0) return testing_list;
	for (int i = 0; i < doc_ids.size(); i++) {
	    Integer doc_id = doc_ids.get(i);
	    if (processed_das_.contains(doc_id)) {
		continue;
	    } else {
		testing_list.add(doc_id);
		processed_das_.add(doc_id);
		return testing_list;
	    }
	}	
	return null;
    }
    
    public void emptyRandomList() {
	randomList.clear();
    }

    public void loadFiles() {
	try {
	    System.out.println("load files...");
	    File tp = new File(totalPath);
	    ArrayList ps = new ArrayList();
	    while (tp != null) {
		File[] fls = tp.listFiles();
		for (int i = 0; i < fls.length; i++) {
		    File fl = fls[i];
		    if (fl.isDirectory()) {
			ps.add(fl);
		    }
		    else {
			System.out.print("loading " + fl.getAbsolutePath() + "...");
			Document doc = dsarmdDP_.getDocument(fl.getAbsolutePath());
			docs_.add(doc);
			doc_names_.put(doc, fl.getName());
			//System.out.println("done!!!");
		    }
		}
		if (ps.size() > 0) {
		    tp = (File)ps.get(0);
		    ps.remove(0);
		}
		else {
		    tp = null;
		}
	    }
	}catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    public void splitDocs() {
	String line = null;
	Integer dialogue_ID = null;
	ArrayList list = null;
	//	list = getRandomList();
	list = getTestingList();
	//System.out.println("list size: " + list.size());
	test_docs_.clear();
	for (int i = 0; i < docs_.size(); i++) {
	    Document doc = (Document)docs_.get(i);
	    System.out.println("doc uri: " + doc.getDocumentURI());
	    dialogue_ID =Integer.valueOf(i);
	    if(list.contains(dialogue_ID)) {
		test_docs_.add(doc);
		//train_docs_.add(doc);
	    }
	    else {
		train_docs_.add(doc);
	    }
	}
	emptyRandomList();
    }
    
    public void splitDocsOnName(String training) {
	String line = null;
	Integer dialogue_ID = null;
	ArrayList list = null;
	//	list = getRandomList();
	list = getTestingList();
	//System.out.println("list size: " + list.size());
	test_docs_.clear();
	for (int i = 0; i < docs_.size(); i++) {
	    Document doc = (Document)docs_.get(i);
	    String uri = doc.getDocumentURI();
	    if(uri.indexOf(training) == -1) {
		test_docs_.add(doc);
	    }
	    else {
		train_docs_.add(doc);
	    }
	}
	emptyRandomList();
    }
    
    public void splitFile() {
	String line = null;
	Integer dialogue_ID = null;
	ArrayList list = null;
	try {
	    BufferedReader br = new BufferedReader(new FileReader(new File(totalPath)));
	    PrintWriter pwTrain = new PrintWriter(new BufferedWriter(new FileWriter(new File(trainPath))));
	    PrintWriter pwTest = new PrintWriter(new BufferedWriter(new FileWriter(new File(testPath))));
	    list = getRandomList();
	    //System.out.println("list size: " + list.size());
	    line = br.readLine();
	    while(line !=null) {
		if(line.indexOf("Dialogue")>=0) {
		    dialogue_ID =Integer.valueOf(line.split(" ")[1]);
		    if(list.contains(dialogue_ID)) {
			pwTest.println("Dialogue " + dialogue_ID);
			while((line = br.readLine())!=null && (line.indexOf("Dialogue")<0)) {
			    pwTest.println(line);
			}
		    } else {
			pwTrain.println("Dialogue " + dialogue_ID);
			while((line = br.readLine())!=null && (line.indexOf("Dialogue")<0)) {
			    pwTrain.println(line);
			}
			
		    }
		}
	    }
	    
	    emptyRandomList();
	    br.close();
	    pwTrain.close();
	    pwTest.close();
	    
	    
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    /* the first path is used to output the sentences that cannot find match tag
     * the second path is used to output the n-gram file ordered by the use frequency
     * */
    public  double testValidate(String path_noMatch, String path_propertyMap, String wrongMatch) {
	double accuracy = 0.0;
	int total_sentence = 0;
	int right_sentence = 0;
	int nomatch_sentence = 0;
	String line;
	String text = null;
	String textTag = null;
	ObjValue[] daReturn;

	//for evaluation each dialogue and tag
	int d_t_s = 0;
	int d_r_s = 0;
	String d_nm = null;
	tags_eval_.clear();
	tags_in_tsR_.clear();

	
	//String[] tagKeyStr;
	HashMap propertyMap;
	
	//splitDocsOnName("session");
	splitDocs();
	for (int i = 0; i < train_docs_.size(); i++) {
	    Document doc = (Document)train_docs_.get(i);
	    tagTraining.buildTrainMap(doc, skip_sw_, corpus_type_, clustered_, add_SE_tags_, i, null, dl_act_str_);
	}
	System.out.println("finished building training map");
	tagTraining.printTrainMap(daFilePath + count_);
	count_++;
	//tagTraining.test_singleMap(path_propertyMap);
	//daTagger = new DATagger();
	propertyMap = tagTraining.getPropertyMap();
	daTagger.setPropertyMap(propertyMap);
	
	try {
	    PrintWriter pw_noMatch = new PrintWriter(new FileWriter(new File(path_noMatch)));
	    PrintWriter pw_wrongMatch = new PrintWriter(new FileWriter(new File(wrongMatch)));
	    for (int ii = 0; ii < test_docs_.size(); ii++) {
		Document test_doc = (Document)test_docs_.get(ii);
		ArrayList utts = dsarmdDP_.parseDAList(test_doc, corpus_type_, clustered_, dl_act_str_);
		daTagger.setUtts(utts);
		d_t_s = 0;
		d_r_s = 0;
		d_nm = doc_names_.get(test_doc);
		for (int jj = 0; jj < utts.size(); jj++) {
		    Utterance da = (Utterance)utts.get(jj);
		    text = Util.filterIt(da.getUtterance());
		    textTag = da.getTag();
		    if (textTag == null ||
			textTag.trim().length() == 0) {
			//			System.out.println("empty tag from testing corpus");
			continue;
		    }
		    
		    //check whether the test tag already exists in the tags_in_tsR_ and tt_tags_in_tsR_
		    //and then get the tags already extracted by this test tag for later process
		    HashMap<String, Integer> tags = tags_in_tsR_.get(textTag);
		    if (tags == null) {
			tags = new HashMap<String, Integer>();
			tags.put("totals", new Integer(0));
			tags_in_tsR_.put(textTag, tags);
		    }
		    
		    HashMap<String, Integer> tt_tags = tt_tags_in_tsR_.get(textTag);
		    if (tt_tags == null) {
			tt_tags = new HashMap<String, Integer>();
			tt_tags.put("totals", new Integer(0));
			tt_tags_in_tsR_.put(textTag, tt_tags);
		    }
		    
		    ArrayList std_tag_scr = (ArrayList)tags_eval_.get(textTag);
		    if (std_tag_scr == null) {
			std_tag_scr = new ArrayList();
			std_tag_scr.add(new Integer(0));
			std_tag_scr.add(new Integer(0));
			std_tag_scr.add(new Integer(0));
			tags_eval_.put(textTag, std_tag_scr);
		    }
		    std_tag_scr.set(2, (Integer)std_tag_scr.get(2) + 1);
		    ArrayList all_std_tag_scr = (ArrayList)all_tags_eval_.get(textTag);
		    if (all_std_tag_scr == null) {
			all_std_tag_scr = new ArrayList();
			all_std_tag_scr.add(new Integer(0));
			all_std_tag_scr.add(new Integer(0));
			all_std_tag_scr.add(new Integer(0));
			all_tags_eval_.put(textTag, all_std_tag_scr);
		    }
		    all_std_tag_scr.set(2, (Integer)all_std_tag_scr.get(2) + 1);
		    d_t_s++;
		    total_sentence++;
		    //tagKeyStr = daTagger.tagUtterance(text, textTag);
		    daReturn = daTagger.tagUtterance(text, textTag, skip_sw_, true, "datag", false, da);

		    //process found tag for test tag
		    if (!tags.containsKey(daReturn[0].getTag())) {
			tags.put(daReturn[0].getTag(), new Integer(0));
		    }
		    tags.put(daReturn[0].getTag(), tags.get(daReturn[0].getTag()) + 1);
		    tags.put("totals", tags.get("totals") + 1);

		    if (!tt_tags.containsKey(daReturn[0].getTag())) {
			tt_tags.put(daReturn[0].getTag(), new Integer(0));
		    }
		    tt_tags.put(daReturn[0].getTag(), tt_tags.get(daReturn[0].getTag()) + 1);
		    tt_tags.put("totals", tt_tags.get("totals") + 1);
		    ///////////done for process/////////////////

		    if(daReturn[0].getTag().equals(textTag)) {
			right_sentence++;
			d_r_s++;
			std_tag_scr.set(0, (Integer)std_tag_scr.get(0) + 1);
			std_tag_scr.set(1, (Integer)std_tag_scr.get(1) + 1);
			all_std_tag_scr.set(0, (Integer)all_std_tag_scr.get(0) + 1);
			all_std_tag_scr.set(1, (Integer)all_std_tag_scr.get(1) + 1);
		    }
		    else {
			if (daReturn[0].getTag().indexOf("Disagree-Reject") != -1 ||
			    daReturn[0].getTag().indexOf("Agree-Accept") != -1 ||
			    daReturn[0].getTag().indexOf("Action-Directive") != -1 ||
			    daReturn[0].getTag().indexOf("Offer-Commit") != -1 ||
			    textTag.indexOf("Disagree-Reject") != -1 ||
			    textTag.indexOf("Agree-Accept") != -1 ||
			    textTag.indexOf("Action-Directive") != -1 ||
			    textTag.indexOf("Offer-Commit") != -1) {
			    System.out.println(text + "\ncorrect tag: " + textTag + " --- system tag: " + daReturn[0].getTag() +
					       "objValue: " + daReturn[0]);
			    Utterance pre = getPreUtt(utts, da);
			    if (pre != null) {
				System.out.println("previouse utterance: " + pre.getContent() + "\ntag: " + pre.getTag());
			    }
			    System.out.println("+++++++++++++++++++++++++++++++++++++++");
			}
			String ex_tag = daReturn[0].getTag();
			if (ex_tag != null &&
			    !ex_tag.equals("unknown")) {
			    ArrayList ex_tag_scr = (ArrayList)tags_eval_.get(ex_tag);
			    if (ex_tag_scr == null) {
				ex_tag_scr = new ArrayList();
				ex_tag_scr.add(new Integer(0));
				ex_tag_scr.add(new Integer(0));
				ex_tag_scr.add(new Integer(0));
				tags_eval_.put(ex_tag, ex_tag_scr);
			    }
			    ex_tag_scr.set(1, (Integer)ex_tag_scr.get(1) + 1);
			    ArrayList all_ex_tag_scr = (ArrayList)all_tags_eval_.get(ex_tag);
			    if (all_ex_tag_scr == null) {
				all_ex_tag_scr = new ArrayList();
				all_ex_tag_scr.add(new Integer(0));
				all_ex_tag_scr.add(new Integer(0));
				all_ex_tag_scr.add(new Integer(0));
				all_tags_eval_.put(ex_tag, all_ex_tag_scr);
			    }
			    all_ex_tag_scr.set(1, (Integer)all_ex_tag_scr.get(1) + 1);
			}
			if(daReturn[0].getTag().equals("unknown")) {
			    nomatch_sentence++;
			    pw_noMatch.println(text +"  ***original tag is: " + textTag);
			    pw_noMatch.println(" ");
			} else if(daReturn[1]!=null) {
			    
			    pw_wrongMatch.println(text);
			    pw_wrongMatch.println("******the right n-gram and tag are: " + daReturn[1].getNGram() + " --- " + 
						  daReturn[1].getTag() + " --- " + "prediction: " +daReturn[1].getPrediction() + " --- " +
						  "frequency: " + daReturn[1].getFrequency());
			    pw_wrongMatch.println("******the wrong n-gram and tag used are: " + daReturn[0].getNGram() + " --- " + 
						  daReturn[0].getTag() + " --- " + "prediction: " +daReturn[0].getPrediction() + " --- " +
						  "frequency: " + daReturn[0].getFrequency());
			} else {
			    pw_wrongMatch.println(text);
			    pw_wrongMatch.println("******the right n-gram and tag are: " + null + ", " + textTag);
			    pw_wrongMatch.println("******the wrong n-gram and tag used are: " + daReturn[0].getNGram() + " --- " + 
						  daReturn[0].getTag() + " --- " + "prediction: " +daReturn[0].getPrediction() + " --- " +
						  "frequency: " + daReturn[0].getFrequency());
			}
		    }
		    
		}
		if (d_t_s != 0 &&
		    d_nm != null) {
		    System.out.println("The accuracy of dialogue in " + d_nm + ": " + (double)d_r_s/(double)d_t_s);
		}
	    }
	    //print each tag precision, recall , and f score
	    ArrayList tags = new ArrayList(tags_eval_.keySet());
	    for (int i = 0; i < tags.size(); i++) {
		String tag = (String)tags.get(i);
		ArrayList tag_scr = (ArrayList)tags_eval_.get(tag);
		if (((Integer)tag_scr.get(1)).doubleValue() == 0) {
		    continue;
		}
		double precision = ((Integer)tag_scr.get(0)).doubleValue()/((Integer)tag_scr.get(1)).doubleValue();
		double recall = ((Integer)tag_scr.get(0)).doubleValue()/((Integer)tag_scr.get(2)).doubleValue();
		double f_measure = 2 * precision * recall / (precision + recall);
		System.out.println(tag + "'s score: " + precision + "(p),   " + recall + "(r),   " + f_measure + "(f)" 
				   + "   total detected: " + tag_scr.get(1) + "      corrected detected: " + tag_scr.get(0));
	    }
	    accuracy = (double)right_sentence/(double)total_sentence;
	    System.out.println("total number of testing sentences: " + total_sentence);
	    System.out.println("no match number of testing sentences: " + nomatch_sentence);
	    pw_noMatch.close();
	    pw_wrongMatch.close();
	    tagTraining.test_singleMap(path_propertyMap);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	return accuracy;
	
    }
    
    //the following method is used to tag new file 
    public  void tagNewFile (String path) {
	
	String line;
	String text = null;
	String textTag = null;
	ObjValue[] daReturn;
	
	HashMap propertyMap;
	
	splitFile();
	tagTraining.buildTrainMap(trainPath, skip_sw_);
	tagTraining.printTrainMap(daFilePath);
	propertyMap = tagTraining.getPropertyMap();
	daTagger.setPropertyMap(propertyMap);
	
	try {
	    BufferedReader br = new BufferedReader(new FileReader(new File(testNewFilePath)));
	    PrintWriter pw = new PrintWriter(new FileWriter(new File(path)));
	    while((line = br.readLine())!=null) {
		textTag = "";
		text = line;
		daReturn = daTagger.tagUtterance(text, textTag, skip_sw_);
		pw.println(daReturn[0].getTag() + " : " + text);
		
		
		
	    }
	    pw.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
    }

    public Utterance getPreUtt(ArrayList utts,
			       Utterance da) {
	if (da == null) return null;
	String linkto = da.getRespTo();
	if (linkto != null &&
	    linkto.indexOf(":") != -1) {
	    String turn = linkto.split(":")[1];
	    turn = turn.split("\\.")[0];
	    for (int i = 0; i < utts.size(); i++) {
		Utterance utt = (Utterance)utts.get(i);
		if (utt.getTurn().equals(turn)) return utt;
	    }
	}
	//System.out.println("couldn't find the turn: " + turn);
	return null;
    }
    
    //this method is used for 10-cross validation, before using it, please remove "__"
    public void crossValidate() {
	
	double[] accuracy = new double[Validate_Time];
	loadFiles();
	totalMap = tagTraining.getTotalMap(totalPath, docs_, skip_sw_, corpus_type_, clustered_, add_SE_tags_, doc_utts_, dl_act_str_);
	daTagger.setTotalMap(totalMap);

	/****************init the number of utterances for testing************/
	ArrayList<Integer> values = new ArrayList<Integer>(doc_utts_.values());
	for (int i = 0; i < values.size(); i++) {
	    total_utts_ += values.get(i);
	}
	System.out.println("total_utts_ is: " + total_utts_);
	ten_percentage_ = total_utts_ / 10;

	/****************start crossing validation****************************/
	double final_sc = 0;
	double best_sc = 0;
	for(int i=0; i<Validate_Time; i++) {
	    train_docs_.clear();
	    test_docs_.clear();
	    accuracy[i] = testValidate(noMatchPath+i, propertyMapPath+i, wrongMatchPath + i);
            System.out.println(i+" round of Testing score: " + accuracy[i]);		
	    final_sc += accuracy[i];
	    if (best_sc < accuracy[i]) {
		best_sc = accuracy[i];
	    }
	    printTagEvals(tagTraining, i, "/projects/SHARED/data/dsarmd/dsarmd_result");
	}
	printTagEvals(tagTraining, -1, "/projects/SHARED/data/dsarmd/dsarmd_result");



	//print each tag precision, recall , and f score
	ArrayList tags = new ArrayList(all_tags_eval_.keySet());
	System.out.println();
	for (int i = 0; i < tags.size(); i++) {
	    String tag = (String)tags.get(i);
	    ArrayList tag_scr = (ArrayList)all_tags_eval_.get(tag);
	    //ArrayList tag_scr = (ArrayList)all_swbd_tags.get(tag);
	    if (((Integer)tag_scr.get(1)).doubleValue() == 0) {
		continue;
	    }
	    double precision = ((Integer)tag_scr.get(0)).doubleValue()/((Integer)tag_scr.get(1)).doubleValue();
	    double recall = ((Integer)tag_scr.get(0)).doubleValue()/((Integer)tag_scr.get(2)).doubleValue();
	    double f_measure = 2 * precision * recall / (precision + recall);
	    System.out.println(tag + "'s score: " + precision + "(p),   " + recall + "(r),   " + f_measure + "(f)" 
			       + "   total detected: " + tag_scr.get(1) + "      corrected detected: " + tag_scr.get(0));
	    //System.out.println(tag + "'s score: " + precision + "(p),   " + recall + "(r),   " + f_measure + "(f)");
	}

	System.out.println("Final average score: " + final_sc/Validate_Time);
	System.out.println("Best score: " + best_sc);
	
	
        tagTraining.test_totalMap(totalPropertyMapPath);
	
        printLog(accuracy);
        
			
    }
	
    public double getAverage(double[] acc) {
	double sum = 0.0;
	for(int i=0; i<acc.length; i++) {
	    sum+=acc[i];
	}
	
	return sum/acc.length;
	
    }
	
    public void printLog(double[] acc) {
	PrintWriter pwLog;
	try {
	    pwLog = new PrintWriter(new BufferedWriter(new FileWriter(new File(logPath))));
	    for(int i=0; i<acc.length; i++) pwLog.println(i + " round of Testing score: " + acc[i]);
	    pwLog.println("the average score is: " + getAverage(acc));
	    pwLog.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
    }
    
    public static void main(String[] args) {
        long start = System.currentTimeMillis();        
	CrossDsarmdValidation cv = new CrossDsarmdValidation();
	cv.crossValidate();
	long end = System.currentTimeMillis();
	System.out.println("total running time is(seconds):  " + (end-start)/1000);
	System.out.println("*** Congratulations! Ends! ***");
    }
    
}
