package edu.albany.ils.dsarmd0200.evaltag;

//import edu.albany.ils.dsarmd0200.util.convertor.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import java.io.*;
import java.util.*;
import java.lang.*;
/**
 * @Author: Ting Liu
 */

public class CrossValidation {
	
    
    //String totalPath = "/home/ting/develop/deer/data/swbd_202k_42tags_noplus_NETag.xml";
    //String totalPath = "/home/ting/develop/deer/data/swbd_202k_42tags_noplus_NETag_Refined";
    String totalPath = "/home/ting/develop/deer/data/swbd_202k_42tags_noplus.txt";
    String trainPath = "/home/ting/develop/deer/data/swbd_202k_42tags_noplus_train";
    String testPath = "/home/ting/develop/deer/data/swbd_202k_42tags_noplus_test";
    String testNewFilePath = "/home/ting/develop/deer/data/swbd_result/HHQuestion";
    String outPath = "/home/ting/develop/deer/data/swbd_result/HHQuestion.tag1";
    String daFilePath = "/home/ting/develop/deer/data/swbd_202k_42tags_noplus_train.ngram";
    String logPath = "/home/ting/develop/deer/data/swbd_result/log";
    
    String noMatchPath = "/home/ting/develop/deer/data/swbd_result/noMatch";
    String wrongMatchPath = "/home/ting/develop/deer/data/swbd_result/wrongMatch";
    String propertyMapPath = "/home/ting/develop/deer/data/swbd_result/propertyNGram";
    String totalPropertyMapPath = "/home/ting/develop/deer/data/swbd_result/TotalPropertyNGram";
	//public static String NL = System.getProperty("line.seperator");
	
    int Random_N = 19;
    int Random_Limit = 1155;
    int Validate_Time = 10;
    //int Random_N = Random_Limit/Validate_Time;
    Random random_ = new Random();
    ArrayList<Integer> random_list_ = new ArrayList<Integer>();
    ArrayList<Integer> randomList = new ArrayList<Integer>();
    
    TagTraining tagTraining = new TagTraining();
    HashMap totalMap;
    DATagger daTagger = new DATagger();
    boolean skip_sw_ = true; //whether to skip stop words processing
    HashSet processed_das_ = new HashSet();
    HashMap tags_eval_ = new HashMap(); //key is tag name and values is arraylist of three integers, correct extracted, total extracted, and total exist,
    HashMap all_tags_eval_ = new HashMap(); //key is tag name and values is arraylist of three integers, correct extracted, total extracted, and total exist,
    HashMap training_docs_ = new HashMap();
    int	size_of_tr_utts_ = 0;
    int size_of_tr_ = 0;

    public static double total_score_ = 0;
    protected HashMap<String, HashMap> tags_in_tsR_ = new HashMap<String, HashMap>(); //hashmap of hashmap of tagged string and frequency
    protected HashMap<String, HashMap> tt_tags_in_tsR_ = new HashMap<String, HashMap>(); //hashmap of hashmap of tagged string and frequency
    

    

    public void setTotalPath(String total_path) {
	totalPath = total_path;
	trainPath = total_path + "_train";
	testPath = total_path + "_test";
	daFilePath = total_path + "_train.ngram";
	loadTrainingDocs(totalPath);
    }

    public void setTotalPath() {
	loadTrainingDocs(totalPath);
    }

    public ArrayList getRandomInts() {
	int count = 0;
	ArrayList randomList = new ArrayList();
	while (true) {
	    int random_int = random_.nextInt(size_of_tr_);
	    if (!random_list_.contains(Integer.valueOf(random_int))) {
		random_list_.add(Integer.valueOf(random_int));
		randomList.add(Integer.valueOf(random_int));
		count++;
		if (count < (size_of_tr_ % 10)) {
		    continue;
		}
		else {
		    return randomList;
		}
	    }
	}
    }


    public void loadTrainingDocs(String training_path) {
	String line = null;
	Integer dialogue_ID = null;
	ArrayList list = null;
	training_docs_.clear();
	try {
	    File fl_fl = new File(training_path);
	    BufferedReader br = new BufferedReader(new FileReader(fl_fl));
	    char[] fl_ct_cs = new char[(int)fl_fl.length()];
	    br.read(fl_ct_cs);
	    String fl_ct = new String(fl_ct_cs);
	    br = new BufferedReader(new StringReader(fl_ct));
	    ArrayList utts = new ArrayList();
	    String dia = null;
	    while((line = br.readLine()) != null) {
		if(line.indexOf("Dialogue")>=0) {
		    if (dia != null) {
			training_docs_.put(dia, utts);
			utts = new ArrayList();
		    }
		    dia = line;
		    size_of_tr_++;
		    continue;
		}
		utts.add(line);
		size_of_tr_utts_++;
	    }
	    if (dia != null &&
		!training_docs_.containsKey(dia)) {
		training_docs_.put(dia, utts);
	    }
	    br.close();
	    System.out.println("size of training corpus: " + size_of_tr_);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
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
    
    public void emptyRandomList() {
	randomList.clear();
    }
    
    public void splitFile() {
	String line = null;
	Integer dialogue_ID = null;
	ArrayList list = null;
	try {
	    ArrayList keys = new ArrayList(training_docs_.keySet());
	    PrintWriter pwTrain = new PrintWriter(new BufferedWriter(new FileWriter(new File(trainPath))));
	    PrintWriter pwTest = new PrintWriter(new BufferedWriter(new FileWriter(new File(testPath))));
	    list = getRandomInts();
	    //System.out.println("list size: " + list.size());
	    for (int i = 0; i < keys.size(); i++) {
		String key = (String)keys.get(i);
		ArrayList utts = (ArrayList)training_docs_.get(key);
		PrintWriter pw = pwTrain;
		if (list.contains(new Integer(i))) {
		    pw = pwTest;
		}
		pw.println(key);
		for (int j = 0; j < utts.size(); j++) {
		    String utt = (String)utts.get(j);
		    pw.println(utt);
		}
	    }
	    
	    pwTrain.close();
	    pwTest.close();
	    
	    
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	
    }
    
    public void _splitFile() {
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
	
	//String[] tagKeyStr;
	HashMap propertyMap;
	int d_t_s = 0;
	int d_r_s = 0;
	String d_nm = null;
	tags_eval_.clear();
	tags_in_tsR_.clear();
	    
	splitFile();
	tagTraining.buildTrainMap(trainPath, skip_sw_);
	System.out.println("finished building training map");
	tagTraining.printTrainMap(daFilePath);
	//tagTraining.test_singleMap(path_propertyMap);
	//daTagger = new DATagger();
	propertyMap = tagTraining.getPropertyMap();
	daTagger.setPropertyMap(propertyMap);
	
	try {
	    BufferedReader br = new BufferedReader(new FileReader(new File(testPath)));
	    PrintWriter pw_noMatch = new PrintWriter(new FileWriter(new File(path_noMatch)));
	    PrintWriter pw_wrongMatch = new PrintWriter(new FileWriter(new File(wrongMatch)));
	    while((line = br.readLine())!=null) {
		if(line.indexOf("Dialogue ") < 0) {
		    textTag = line.split(":")[1];


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
		    ////////////////////done for collecting///////////////////


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
		    text = line.split(":")[2].replaceAll("<start>", "").replaceAll("<finish>", "");
		    total_sentence++;
		    d_t_s++;
		    //tagKeyStr = daTagger.tagUtterance(text, textTag);
		    daReturn = daTagger.tagUtterance(text, textTag, skip_sw_);
		    
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
			String ex_tag = daReturn[0].getTag();
			if (ex_tag != null &&
			    !ex_tag.equals("unknown")) {
			    ArrayList ex_tag_scr = (ArrayList)tags_eval_.get(ex_tag);
			    if (ex_tag_scr == null) {
				ex_tag_scr = new ArrayList();
				ex_tag_scr.add(new Integer(0));
				ex_tag_scr.add(new Integer(0));
				ex_tag_scr.add(new Integer(0));
				tags_eval_.put(textTag, ex_tag_scr);
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
		else {
		    /*
		    if (d_t_s != 0 &&
			d_nm != null) {
			System.out.println("The accuracy of " + d_nm + ": " + (double)d_r_s/(double)d_t_s);
		    }
		    */
		    d_t_s = 0;
		    d_r_s = 0;
		    d_nm = line.trim();
		}
	    }
	    /*
	    if (d_t_s != 0 &&
		d_nm != null) {
		System.out.println("The accuracy of " + d_nm + ": " + (double)d_r_s/(double)d_t_s);
	    }
	    */
	    //print each tag precision, recall , and f score
	    /*
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
		System.out.println(tag + "'s score: " + precision + "(p),   " + recall + "(r),   " + f_measure + "(f)");
	    }
	    */
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
    
    //this method is used for 10-cross validation, before using it, please remove "__"
    public void crossValidate(String tag_statistics_path) {
	
	double[] accuracy = new double[Validate_Time];
	totalMap = tagTraining.getTotalMap(totalPath, skip_sw_);
	daTagger.setTotalMap(totalMap);
	double final_sc = 0;
	double best_sc = 0;
	for(int i=0; i<Validate_Time; i++) {
	    accuracy[i] = testValidate(noMatchPath+i, propertyMapPath+i, wrongMatchPath + i);
            System.out.println(i+" round of Testing score: " + accuracy[i]);		
	    final_sc += accuracy[i];
	    if (best_sc < accuracy[i]) {
		best_sc = accuracy[i];
	    }
	    printTagEvals(tagTraining, i, tag_statistics_path);
	}
	printTagEvals(tagTraining, -1, tag_statistics_path);
	//print each tag precision, recall , and f score
	ArrayList tags = new ArrayList(all_tags_eval_.keySet());

	/********done for converting iscsi tags to swbd tags********/

	for (int i = 0; i < tags.size(); i++) {
	    String tag = (String)tags.get(i);
	    //	    System.out.println("tag is: " + tag);
	    ArrayList tag_scr = (ArrayList)all_tags_eval_.get(tag);
	    //ArrayList tag_scr = (ArrayList)all_swbd_tags.get(tag);
	    //	    System.out.println("scores are: " + tag_scr);
	    if (((Integer)tag_scr.get(1)).doubleValue() == 0) {
		continue;
	    }
	    double precision = ((Integer)tag_scr.get(0)).doubleValue()/((Integer)tag_scr.get(1)).doubleValue();
	    double recall = ((Integer)tag_scr.get(0)).doubleValue()/((Integer)tag_scr.get(2)).doubleValue();
	    double f_measure = 2 * precision * recall / (precision + recall);
	    System.out.println(tag + "'s score: " + precision + "(p),   " + recall + "(r),   " + f_measure + "(f)");
	}
	total_score_ += final_sc/Validate_Time;
	System.out.println("Final average score: " + final_sc/Validate_Time);
	System.out.println("Best score: " + best_sc);
	
	
        tagTraining.test_totalMap(totalPropertyMapPath);
	
        printLog(accuracy);
	
			

    }
	
    //this method is used to tag some new utterances
    public void crossValidate__() {
	
	double[] accuracy = new double[Validate_Time];
	totalMap = tagTraining.getTotalMap(totalPath, skip_sw_);
	daTagger.setTotalMap(totalMap);
	tagNewFile(outPath);
	
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
	if (args.length == 0) {
	    long start = System.currentTimeMillis();        
	    CrossValidation cv = new CrossValidation();
	    cv.setTotalPath();
	    cv.crossValidate("/home/ting/develop/edu/albany/ils/deer/data/swbd_result");
	    long end = System.currentTimeMillis();
	    System.out.println("total running time is(seconds):  " + (end-start)/1000);
	    System.out.println("*** Congratulations! Ends! ***");
	} else if (args.length == 2) {
	    //input part of total path name and loop count
	    String total_path = args[0];
	    int count = (new Integer(args[1])).intValue();
	    for (int i = 0; i < count; i++) {
		long start = System.currentTimeMillis();        
		CrossValidation cv = new CrossValidation();
		System.out.println("Processing corpus: " + total_path + "_" + (new Integer(i + 1)).toString());
		cv.setTotalPath(total_path + "_" + (new Integer(i + 1)).toString());
		cv.crossValidate("/home/ting/develop/edu/albany/ils/deer/data/swbd_result");
		long end = System.currentTimeMillis();
		System.out.println("total running time is(seconds):  " + (end-start)/1000);
		System.out.println("*** Congratulations! Ends! ***");
	    }

	    System.out.println("average score of seventy run: " + total_score_/count);
	} else if (args.length == 1) {
	    //input part of total path name and loop count
	    String total_path = args[0];
	    long start = System.currentTimeMillis();        
	    CrossValidation cv = new CrossValidation();
	    System.out.println("Processing corpus: " + total_path);
	    cv.setTotalPath(total_path);
	    cv.crossValidate("/home/ting/develop/edu/albany/ils/deer/data/icsi/eval");
	    long end = System.currentTimeMillis();
	    System.out.println("total running time is(seconds):  " + (end-start)/1000);
	    System.out.println("*** Congratulations! Ends! ***");
	}
    }

    public void printTagEvals(TagTraining tagTraining, int lc, String fl_path) {
	
	ArrayList<String> keys = null;
	HashMap <String, HashMap> test_tags = null;
	try {
	    BufferedWriter bw = null;
	    //write training tags
	    HashMap <String, Integer> train_tags = null;
	    if (lc > -1) {
		bw = new BufferedWriter(new FileWriter(new File(fl_path + "/tags_in_training_5_tags_" + lc + ".txt")));
		train_tags = tagTraining.getTagsFromTrSet();
	    } else {
		bw = new BufferedWriter(new FileWriter(new File(fl_path + "/total_tags_in_training_5_tags" + ".txt")));
		train_tags = tagTraining.getTotalTagsFromTrSet();
	    }
	    keys = new ArrayList(train_tags.keySet());
	    bw.write("tag type\tnumber");
	    bw.newLine();
	    for (int i = 0; i < keys.size(); i++) {
		String key = (String)keys.get(i);
		if (lc > -1) {
		    bw.write(key + "\t" + train_tags.get(key));
		} else {
		    bw.write(key + "\t" + (int)(train_tags.get(key)/10));
		}
		bw.newLine();
	    }
	    bw.close();
	    tagTraining.clearTagsInTrMap();

	    //write test tags result	    
	    if (lc > -1) {
		bw = new BufferedWriter(new FileWriter(new File(fl_path + "/tags_in_test_5_tags_" + lc + ".txt")));
		keys = new ArrayList<String>(tags_in_tsR_.keySet());
		test_tags = tags_in_tsR_;
	    } else {
		bw = new BufferedWriter(new FileWriter(new File(fl_path + "/total_tags_in_test_5_tags" + ".txt")));
		keys = new ArrayList<String>(tt_tags_in_tsR_.keySet());
		test_tags = tt_tags_in_tsR_;
	    }
	    keys = new ArrayList(test_tags.keySet());
	    bw.write("tag type\ttotal");
	    for (int i = 0; i < keys.size(); i++) {
		String key = (String)keys.get(i);
		bw.write("\t" + key);
	    }
	    bw.newLine();
	    
	    for (int i = 0; i < keys.size(); i++) {
		String key = keys.get(i);
		HashMap tags = test_tags.get(key);
		bw.write (key + "\t" + tags.get("totals"));
		for (int j = 0; j < keys.size(); j++) {
		    String tags_key = (String)keys.get(j);
		    if (tags.containsKey(tags_key)) {
			bw.write("\t" + tags.get(tags_key));
		    } else {
			bw.write("\t0");
		    }
		}
		bw.newLine();
	    }
	    bw.close();
	    
	}catch (IOException ioe) {
	    ioe.printStackTrace();
	}
	/*
	System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	for (int i = 0; i < keys.size(); i++) {
	    String key = keys.get(i);
	    HashMap tags = test_tags.get(key);
	    System.out.println("tag: " + key);
	    System.out.print("tags defined and frenquecy: ");
	    ArrayList tags_keys = new ArrayList(tags.keySet());
	    for (int j = 0; j < tags_keys.size(); j++) {
		String tags_key = (String)tags_keys.get(j);
		System.out.println("^^^tag: " + tags_key + " frequency: " + tags.get(tags_key));
	    }
	    System.out.println("-----------------------------------------------");
	}
	*/
    }
    
}
