package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 01/28/2010
 * This class is used to judge the positive and negative
 * support for one local topic 
 */

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
//import edu.albany.ils.dsarmd0200.ml.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import java.sql.SQLException;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class Assertions {
    public Assertions(String data_path) {
	Settings.initialize();
	ParseTools.initialize();
	PronounFormMatching.initialize();
	GenderCheck.initialize();
	//System.out.println("pw: " + Settings.getValue(Settings.WEB_SERVICE));
	ParseTools.setWN(wn_);
	docs_path_ = data_path;
//	System.out.println("Preparing...");

	loadAndParseTraining ();
	loadAndParse ();
        //m2w: for chinese
        this.setLanguageAndInitPosTaggers();
//        this.initChineseWordnet();
	/*
	if (Settings.getValue(Settings.PROCESS_TYPE).equals("automated")) {
	    preprocess();
        }
	*/
    }

    /*******************************get information**************************/

    /*******************************set information**************************/


    public static void main(String[] args) {
	Assertions assertions = new Assertions(args[0]);
	//assertions.calTpDis();
	//assertions.calTpCtl();
	//assertions.calInv();
	//assertions.calTkCtl();
	//assertions.calExDis();
	//assertions.calCommLink();
	//assertions.createReport();
	//assertions.printReport();
	//assertions.calQuintile();
	assertions.makeAssertions();
    }

    public void makeAssertions() {
	for (int i = 0; i < docs_utts_.size(); i++) {
	    String fn = (String)doc_names_.get(i);
	    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\nprocessing: " + fn);
	    //utts_ = (ArrayList)docs_utts_.get(i);
	    all_utts_.clear();
	    all_utts_.addAll(tr_utts_);
	    gch_ = new GroupCohesion();
	    if (Settings.getValue(Settings.PROCESS_TYPE).equals("automated")) {
		PhraseCheck phr_ch = (PhraseCheck)phr_checks_.get(i);
		XMLParse xp = xmlps_.get(i);
		utts_ = (ArrayList)docs_utts_.get(i);
		all_utts_.addAll(utts_);
		tagCommType();
		//System.exit(0);
		tagDAct();
		calCommLink(xp);
		buildLocalTopicList(phr_ch, xp);

                //meso_topic and task_focus
                //System.out.println("------------Generate Meso-topics of " + (String)doc_names_.get(i));
                MesoTopic mt = new MesoTopic();
                mt.calMesoTopic((String)doc_names_.get(i), utts_, xp, phr_ch, wn_, nls_);
		mts_.add(mt);
                //ArrayList<ArrayList<String>> mts = mt.getMeso_topics_();
                //testing the mesotopic list
		/*
                for (int ai = 0; ai < mts.size(); ai ++){
                    System.out.print("Topic Speaker : " + utts_.get(Integer.parseInt(mts.get(ai).get(1)) - 1).getSpeaker());
                    System.out.println(Arrays.asList(mts.get(ai).toArray()));
                }
		*/
		ArrayList spks = new ArrayList(parts_.values());
		/*
		for (int k = 0; k < spks.size(); k++) {
		    Speaker part_ = (Speaker)spks.get(k);
		    System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&speaker: " + part_.getName() + "local topics: \n");
		    LocalTopics lts = part_.getILTs();
		    for (int j = 0; j < lts.size(); j++) {
			System.out.print("===" + ((LocalTopic)lts.get(j)).getContent().getWord() + "===");
		    }
		}
		System.out.println("====================================================================================");
		for (int k = 0; k < spks.size(); k++) {
		    Speaker part_ = (Speaker)spks.get(k);
		    System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&speaker: " + part_.getName() + " --- contributed utterances: \n");
		    ArrayList utts = part_.getUtterances();
		    for (int j = 0; j < utts.size(); j++) {
			Utterance utt_ = (Utterance)utts.get(j);
			System.out.println(utt_.getTurn() + " || " + utt_.getSpeaker() + " || " + utt_.getTag() + " || " + utt_.getRespTo() + ": " + utt_.getContent());
		    }
		}
		System.out.println("====================================================================================");
		for (int k = 0; k < utts_.size(); k++) {
		    Utterance utt_ = (Utterance)utts_.get(k);
		    System.out.println(utt_.getTurn() + " || " + utt_.getSpeaker() + " || " + utt_.getTag() + " || " + utt_.getCommActType() 
				       + " || " + utt_.getRespTo() + ": " + utt_.getContent());
		}
		*/

                //mt.clear();
                //tf.clear();
                
	    }else {
		PhraseCheck phr_ch = (PhraseCheck)phr_checks_.get(i);
		XMLParse xp = xmlps_.get(i);
		utts_ = (ArrayList)docs_utts_.get(i);
		all_utts_.addAll(utts_);
		buildALocalTopicList(phr_ch, xp);
	    }
	    ArrayList spks = new ArrayList(parts_.values());
	    for (int k = 0; k < spks.size(); k++) {
		Speaker spk = (Speaker)spks.get(k);
		spk.setSpeakers(parts_);
	    }
	    ddt_ = new DocDialogueType(utts_);
	    //System.out.println("ddt_ type: " + ddt_.getType());
	    gch_.setSpeakers(parts_);
	    gch_.setMesoTopics(mts_);
	    prxmlp_ = new PtsReportXMLParser(Settings.getValue(Settings.REPORT) + fn.split("\\.")[0] + "_AssertionReport.xml");
	    prxmlp_.initClaim();
	    prxmlp_.setDataUnit(fn.split("\\.")[0], fn.split("\\.")[0]);
	    calTpCtl(i);
	    System.out.println("@Involvement");
	    calInv(i);
	    System.out.println("@Task Control");
	    calTkCtl(i);
	    System.out.println("@Expressive Disagreement");
	    calExDis(i);
	    System.out.println ("@Leadership");
	    calLeader();
	    //System.out.println("@Agreement");
	    calAgr();
	    //System.out.println("@Task Focus");
	    calTaskFocus(i);
	    //System.out.println("@Sociability Measure...");
	    calSociability();
	    //System.out.println("\n\nProcessing L...");
	    //System.out.println("\n\nProcessing Sociability Measure...");
	    calGroupCohesion(i);
	    //System.out.println("\n\nprocessing topic disagreement...");
	    //calTpDis(i);
	    //createReport();
	    //printReport();
//            try{
//                if(!ChineseWordnet.conn.isClosed()){
//                        ChineseWordnet.closeConn();
//                }
//            }catch(SQLException e){e.printStackTrace();}
	}


	if ((Settings.getValue (Settings.WEB_SERVICE)).equals ("yes"))
	    {
		String DIR = "/home/samirashaikh/sshaikh/develop/NLTEST/scil0200/data/webservice/";
		String BACKUP = "/home/samirashaikh/sshaikh/develop/NLTEST/scil0200/data/backup/";
		String QUERY_FILE = "webservice.txt";
		
		
		String cmd = "mv " + DIR + "/" + QUERY_FILE + " " + BACKUP + "/" + getTimeStamp () + QUERY_FILE;
		Process moveQueryFile = null;
		try
		    {
			moveQueryFile = Runtime.getRuntime ().exec (cmd);
			InputStreamReader myIStreamReader = new InputStreamReader (moveQueryFile.getInputStream ());
			BufferedReader bufferedReader = new BufferedReader (myIStreamReader);
			
			String line;
			while ((line = bufferedReader.readLine ()) != null)
			    {
				System.err.println (line);
			    }
			InputStreamReader myErrorStreamReader = new InputStreamReader (moveQueryFile.getErrorStream ());
			BufferedReader bufferedErrorReader = new BufferedReader (myErrorStreamReader);
			String eLine;
			while ((eLine = bufferedErrorReader.readLine ()) != null)
			    {
				System.err.println (eLine);
			    }
			
			if (moveQueryFile.waitFor () != 0)
			    System.err.println ("ERROR" + moveQueryFile.exitValue ());
			else System.err.println ("moved query file");
			
			myIStreamReader.close ();
		    }
		catch (Exception ioe )
		    {
			ioe.printStackTrace ();
		    }
	    }
                            
    }

    public void preprocess() {
	for (int i = 0; i < docs_utts_.size(); i++) {
	    PhraseCheck phr_ch = (PhraseCheck)phr_checks_.get(i);
	    XMLParse xp = xmlps_.get(i);
	    utts_ = (ArrayList)docs_utts_.get(i);
	    System.out.println("processing document " + i);
	    all_utts_.clear();
	    all_utts_.addAll(tr_utts_);
	    all_utts_.addAll(utts_);
	    tagCommType();
	    //System.exit(0);
	    System.out.println("go into tagDAct...");
	    tagDAct();
	    calCommLink(xp);
	    //System.out.println("------------Generate Meso-topics of " + (String)doc_names_.get(i));
	    //MesoTopic mt = new MesoTopic();
	    //mt.callMesoTopic((String)doc_names_.get(i), utts_, xp, phr_ch, wn_);
	}
	//System.out.println("utts_: \n" + utts_);
	
    }

    public void tagCommType() {
	CommunicationType commT = new CommunicationType(all_utts_, tr_utts_, utts_, parts_, wn_);
	commT.tagIt();
    }

    public void tagDAct() {
	//information level
	Settings.setValue("tagNum", "3");
	Settings.setValue("tagType", "da3");
	DialogueActType daT = new DialogueActType(all_utts_, tr_utts_, utts_);
	daT.tagIt3();
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    //System.out.println("utt_ information level tag: " + utt_.getMTag());
	    break;
	}
	//dialogue action level
	//System.exit(0);
	Settings.setValue("tagNum", "15");
	Settings.setValue("tagType", "da15");
	//System.out.println("tagNum: " + Settings.getValue("tagNum"));
	daT = new DialogueActType(all_utts_, tr_utts_, utts_);
	daT.tagIt();
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt_ = (Utterance)utts_.get(i);
	    //System.out.println("utt_ DA tag: " + utt_.getTag());
	    break;
	}
    }

    public void calExDis(int j) {
	is_inv_ = false;
	is_tskc_ = false;
	//for (int j = 0; j < docs_utts_.size(); j++) {
	//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\nprocessing: " + (String)doc_names_.get(j));
	//utts_ = (ArrayList)docs_utts_.get(j);
	exd_ = new ExpressiveDisagreement(utts_);
	/*
	PhraseCheck phr_ch = (PhraseCheck)phr_checks_.get(j);
	XMLParse xp = xmlps_.get(j);
	buildLocalTopicList(phr_ch, xp);
	*/
	//System.out.println("after polarities calculated...");
	exd_.calCEDI(prxmlp_, spks_, parts_);
	//}
    }

    public void calTaskFocus(int i) {
	//task_focus
	MesoTopic mt = (MesoTopic)mts_.get(i);
	TaskFocus tf = new TaskFocus();
	gch_.setTaskFocus(tf);
	tf.calTaskFocus(mt, utts_);
	gch_.setTaskFocus(tf);
	//System.out.println("@MSM: " + String.valueOf(tf.getMSM()));
	//System.out.println("@MGM: " + String.valueOf(tf.getMGM()));
	//System.out.println("Task Focus: " + String.valueOf(tf.getTaskFocus()));
	//System.out.println("-----------------------------------------------");
	//System.out.println();
	
    }

    public void calSociability() {
	sb_ = new Sociability(utts_, parts_);
	sb_.calculate();
	gch_.setSociability(sb_);
    }

    public void calLeader() {
	//System.out.println("leadership(vote): " );
	Iterator keys = parts_.keySet().iterator();
	Speaker leader = null;
	double ld_score = -100;
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    if (key.startsWith("ils")) continue;
	    //System.out.println("key: " + key);
	    Speaker part_ = parts_.get(key);
	    part_.calLeadershipR();
	    if (part_.getLeadershipR() > ld_score &&
		part_.rankFirst()) {
		leader = part_;
		ld_score = part_.getLeadershipR();
	    }
	    System.out.println(part_.getName() + ":" + part_.getLeadershipRInfo());
	}
	if (leader != null) {
	    System.out.println("@Leader");
	    System.out.println (leader.getName());
	    System.out.println("@Confidence:\n" + leader.getLeadershipConfidence());
	}
	/*
	  System.out.println("leadership(Weight): " );
	  keys = parts_.keySet().iterator();
	  leader = null;
	  ld_score = -100;
	  while (keys.hasNext()) {
		String key = (String)keys.next();
		if (key.startsWith("ils")) continue;
		System.out.println("key: " + key);
		Speaker part_ = parts_.get(key);
		part_.calLeadership();
		if (part_.getLeadership() > ld_score &&
		    part_.rankFirst()) {
		    leader = part_;
		    ld_score = part_.getLeadershipR();
		}
		System.out.println("======================" + part_.getName() + ": \n" + part_.getLeadershipInfo());
	    }
	    if (leader != null) {
		System.out.println("==============leader is: " + leader.getName());
	    }
	*/
    }
    public void calGroupCohesion(int fn) {
	gch_.calculate(fn);
    }

    public void calAgr() {
	is_inv_ = false;
	is_tskc_ = false;
	ArrayList keys = new ArrayList(parts_.keySet());
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    Speaker spk = parts_.get(key);
	    spk.calATX();
	    if (spk.getName().startsWith("ils")) continue;
            // modified by Laura, April 13, 2011
	    //System.out.println(/*"The actural score of ATX of " + */spk.getName() + /*" is: "*/ " : " + spk.getATX());
	}
    }

    public void calTpDis(int j) {
	is_inv_ = false;
	is_tskc_ = false;
	//ArrayList tr_utts_ = new ArrayList();
	//parts_.clear();
	//for (int j = 0; j < docs_utts_.size(); j++) {
	//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\nprocessing: " + (String)doc_names_.get(j));
	//String doc_name = (String)doc_names_.get(j);
	//if (doc_name.indexOf("Utterance") == -1) continue;
	//utts_ = (ArrayList)docs_utts_.get(j);
	//tr_utts_.clear();
	//for (int i = 0; i < docs_utts_.size(); i++) {
	//if (i != j) {
	//tr_utts_.addAll((ArrayList)docs_utts_.get(i));
	//}
	//}
	    //tpd_.calDisagr(utts_);
	tpd_ = new TopicDisagreement(all_utts_, tr_utts_, utts_, wn_);
	tpd_.buildTrainMap();
	tpd_.tagPolarity();
	/*
	PhraseCheck phr_ch = (PhraseCheck)phr_checks_.get(j);
	XMLParse xp = xmlps_.get(j);
	buildLocalTopicList(phr_ch, xp);
	*/
	//System.out.println("after polarities calculated...");
	tpd_.calDisagreement(parts_);
	//}
    }

    public void calTpCtl(int k) {
	is_inv_ = false;
	is_tskc_ = false;
	//for (int k = 0; k < docs_utts_.size(); k++) {
	System.out.println("@Topic Control");
	PhraseCheck phr_ch = (PhraseCheck)phr_checks_.get(k);
	XMLParse xp = xmlps_.get(k);
	//utts_ = (ArrayList)docs_utts_.get(k);
	//parts_.clear();
	//spks_.clear();
	if (Settings.getValue(Settings.PROCESS_TYPE).equals("annotated")) {
	    //parts_.clear();
	    //spks_.clear();
	    //buildALocalTopicList(phr_ch, xp);
	} else if (Settings.getValue(Settings.PROCESS_TYPE).equals("automated")) {
	    //buildLocalTopicList(phr_ch, xp);
	    //return; //only for top10 localtopics
	}
	//prxmlp_.setDuParticipants(spks_);
	ArrayList keys = new ArrayList(parts_.keySet());
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    Speaker spk = parts_.get(key);
	    spk.calTpCtl(lts_, utts_);
	    //System.out.println("==========================================");
	    //System.out.println(spk.getTC());
	}
	
	keys = new ArrayList(parts_.keySet());
	for (int i = 1; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    for (int j = 0; j < i; j++) {
		String key2 = (String)keys.get(j);
		Speaker spk2 = parts_.get(key2);
		if (spk1.getTC().getLTI() < spk2.getTC().getLTI()) {
		    spk1.getTC().incLTIR();
		} else {
		    spk2.getTC().incLTIR();
		}
		if (spk1.getTC().getSMT() < spk2.getTC().getSMT()) {
		    spk1.getTC().incSMTR();
		} else {
		    spk2.getTC().incSMTR();
		}
		if (spk1.getTC().getCS() < spk2.getTC().getCS()) {
		    spk1.getTC().incCSR();
		} else {
		    spk2.getTC().incCSR();
		}
		if (spk1.getTC().getTL() < spk2.getTC().getTL()) {
		    spk1.getTC().incTLR();
		} else {
		    spk2.getTC().incTLR();
		}
		if (spk1.getTC().getCLTI() < spk2.getTC().getCLTI()) {
		    spk1.getTC().incCLTIR();
		    } else {
		    spk2.getTC().incCLTIR();
		}
	    }
	}	
	    
	    
	//System.out.println("\n\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	//process LTI quintile
	ArrayList spks = new ArrayList();
	ArrayList spks_LTI = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_LTI = new Double(spk1.getTC().getLTI());
	    boolean added = false;
	    for (int j = 0; j < spks_LTI.size(); j++) {
		Double spk2_LTI = (Double)spks_LTI.get(j);
		if (spk1_LTI > spk2_LTI &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_LTI.add(j, spk1_LTI);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_LTI.add(spk1_LTI);
	    }
	}	
	ArrayList rank = new ArrayList();
	rank.add(1);
	for (int j = 1; j < spks_LTI.size(); j++) {
	    Double spk2_LTI = (Double)spks_LTI.get(j);
	    Double spk1_LTI = (Double)spks_LTI.get(j - 1);
	    if (spk1_LTI.doubleValue() == spk2_LTI.doubleValue()) {
		rank.add(rank.get(rank.size() - 1));
	    } else {
		rank.add(j + 1);
	    }
	}

	/*
	System.out.println("\n++++++++++++++++++++++++++++++++\ncalculate Topic Control - LTI quintile");
	calQuintile(spks_LTI, spks, "TPC", "LTI", null);
	*/
	
	//process SMT quintile
	spks = new ArrayList();
	ArrayList spks_SMT = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_SMT = new Double(spk1.getTC().getSMT());
	    boolean added = false;
	    for (int j = 0; j < spks_SMT.size(); j++) {
		Double spk2_SMT = (Double)spks_SMT.get(j);
		if (spk1_SMT > spk2_SMT &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_SMT.add(j, spk1_SMT);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_SMT.add(spk1_SMT);
	    }
	}	
	/*
	System.out.println("\n++++++++++++++++++++++++++++++++\ncalculate Topic Control -  SMT quintile: ");
	calQuintile(spks_SMT, spks, "TPC", "SMT", null);
	*/
	//process CS quintile
	spks = new ArrayList();
	ArrayList spks_CS = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_CS = new Double(spk1.getTC().getCS());
	    boolean added = false;
	    for (int j = 0; j < spks_CS.size(); j++) {
		Double spk2_CS = (Double)spks_CS.get(j);
		if (spk1_CS > spk2_CS &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_CS.add(j, spk1_CS);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_CS.add(spk1_CS);
	    }
	}	
	/*
	System.out.println("\n++++++++++++++++++++++++++++++++\ncalculate Topic Control -  CS quintile");
	calQuintile(spks_CS, spks, "TPC", "CS", null);
	*/
	
	//process TL quintile
	spks = new ArrayList();
	double total_tl = 0;
	ArrayList spks_TL = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    total_tl += spk1.getTC().getTL();
	    Double spk1_TL = new Double(spk1.getTC().getTL());
	    boolean added = false;
	    for (int j = 0; j < spks_TL.size(); j++) {
		Double spk2_TL = (Double)spks_TL.get(j);
		if (spk1_TL > spk2_TL &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_TL.add(j, spk1_TL);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_TL.add(spk1_TL);
	    }
	}	
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    spk1.getTC().calTLPt(total_tl);
	}
	/*
	System.out.println("\n++++++++++++++++++++++++++++++++\ncalculate Topic Control -  TL quintile");
	calQuintile(spks_TL, spks, "TPC", "TL", null);
	*/

	//process CLTI quintile
	spks = new ArrayList();
	ArrayList spks_CLTI = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_CLTI = new Double(spk1.getTC().getCLTI());
	    boolean added = false;
	    for (int j = 0; j < spks_CLTI.size(); j++) {
		Double spk2_CLTI = (Double)spks_CLTI.get(j);
		if (spk1_CLTI > spk2_CLTI &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_CLTI.add(j, spk1_CLTI);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_CLTI.add(spk1_CLTI);
	    }
	}	
	/*
	    System.out.println("++++++++++++++++++++++++++++++++\ncalculate CLTI quintile");
	    calQuintile(spks_CLTI, spks, "TPC", "CLTI", null);
	*/
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    spk1.getTC().calPower();
	    //System.out.println("The topic control merged socre of " + spk1.getName() + " is: " + spk1.getTC().getPower());
	}
	
	
	
	//process Power quintile
	spks = new ArrayList();
	ArrayList spks_Power = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_Power = new Double(spk1.getTC().getPower());
	    boolean added = false;
	    for (int j = 0; j < spks_Power.size(); j++) {
		Double spk2_Power = (Double)spks_Power.get(j);
		if (spk1_Power > spk2_Power &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_Power.add(j, spk1_Power);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_Power.add(spk1_Power);
		}
	}	
	rank = new ArrayList();
	rank.add(1);
	for (int j = 1; j < spks_Power.size(); j++) {
	    Double spk2_Power = (Double)spks_Power.get(j);
	    Double spk1_Power = (Double)spks_Power.get(j - 1);
	    if (spk1_Power.doubleValue() == spk2_Power.doubleValue()) {
		rank.add(rank.get(rank.size() - 1));
	    } else {
		rank.add(j + 1);
	    }
	}
	//System.out.println("++++++++++++++++++++++++++++++++\ncalculate Merged quintile");
	calQuintile(spks_Power, spks, "TPC", "Merged", rank);
    }


    public void calQuintile() {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(new File("/projects/SHARED/data/dsarmd/eval_2010_aug_auto/auto_actual_score.txt")));///projects/SHARED/data/dsarmd/eval_2010_aug_postsession_survey/average_scores")));//home/ting/tools/scil_installation/data/reynard/back/annotated.txt")));
	    br.readLine(); //scrip title
	    String a_line = null;
	    ArrayList spks = new ArrayList();
	    ArrayList dis = new ArrayList();
	    String session = null;
	    while ((a_line = br.readLine()) != null) {
		String[] items = a_line.split("\\t");
		if (session == null) session = items[1];
		if (//items[0].equals("Average")
		    !items[1].equals(session)) {
		    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n++++++++++++++++++++++++++++++++\n\nprocessing session (" + session + "): ");
		    calQuintile(dis, spks);
		    dis.clear();
		    spks.clear();
		    spks.add(items[0]);
		    dis.add(new Double(items[2].trim()));
		    session = items[1];
		} else {
		    spks.add(items[0]);
		    dis.add(new Double(items[2].trim()));
		}
	    }
	    if (spks.size() > 0) {
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n++++++++++++++++++++++++++++++++\n\nprocessing session (" + session + "): ");
		calQuintile(dis, spks);
	    }
	}catch (Exception e) {
	    e.printStackTrace();
	}
	/*
	ArrayList dis = new ArrayList();
	dis.add(new Double(6.55));
	dis.add(new Double(5.5));
	dis.add(new Double(5.4));
	dis.add(new Double(4.65));
	dis.add(new Double(4.5));
	dis.add(new Double(4.3));
	double[] dis1 = new double[dis.size()];
	for (int j = 0; j < dis.size(); j++) {
	    dis1[j] = ((Double)dis.get(j)).doubleValue();
	}
	double[] qt_thrs = Util.genQTThrs(dis1);
	*/
    }

    public void calQuintile(ArrayList dis, ArrayList spks) {
	double[] dis1 = new double[dis.size()];
	for (int j = 0; j < dis.size(); j++) {
	    dis1[j] = ((Double)dis.get(j)).doubleValue();
	}
	double[] qt_thrs = Util.genQTThrs(dis1);
	HashMap qscs = new HashMap();
	//System.out.println("\n=====================================\n");
	boolean same_thrs = true;
	for (int j = 1; j < qt_thrs.length; j++) {
	    if (qt_thrs[j] != qt_thrs[j - 1]) {
		same_thrs = false;
		break;
	    }
	}
	if (same_thrs) {
	    for (int j = 0; j < dis.size(); j++) {
		double di = ((Double)dis.get(j)).doubleValue();
		String spk = ((String)spks.get(j));
		int index = spks_.indexOf(spk);
		String id_spk = index + "_" + spk;
		if (di == qt_thrs[0]) {
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */((String)spks.get(j)) + /*" is: "*/ " : " + 3);// + " --- actual score: " + di);
		    qscs.put(id_spk, 3);
		} else if (di > qt_thrs[0]) {
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((String)spks.get(j)) + /*" is: "*/ " : "+ 5);// + " --- actual score: " + di);
		    qscs.put(id_spk, 5);
		} else if (di < qt_thrs[1]) {
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((String)spks.get(j)) + /*" is: "*/ " : " + 1);// + " --- actual score: " + di);
		    qscs.put(id_spk, 1);
		}	    
	    }
	} else {
	    for (int j = 0; j < dis.size(); j++) {
		double di = ((Double)dis.get(j)).doubleValue();
		String spk = ((String)spks.get(j));
		int index = spks_.indexOf(spk);
		String id_spk = index + "_" + spk;
		if (qt_thrs[0] == qt_thrs[1] &&
		    qt_thrs[1] == qt_thrs[2] &&
		    di == qt_thrs[0]) {
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */((String)spks.get(j)) + /*" is: "*/" : " + 4);// + " --- actual score: " + di);
		    qscs.put(id_spk, 4);
		}
		else if (di > qt_thrs[0] &&
			 di > 0) {
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */((String)spks.get(j)) + /*" is: "*/" : " + 5);// + " --- actual score: " + di);
		    qscs.put(id_spk, 5);
		} else if (di <= qt_thrs[0] &&
			   di > qt_thrs[1] &&
			   di > 0) {
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((String)spks.get(j)) + /*" is: "*/" : " + 4);// + " --- actual score: " + di);
		    qscs.put(id_spk, 4);
		} else if (di <= qt_thrs[1] &&
		       di > qt_thrs[2] &&
			   di > 0) {
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((String)spks.get(j)) + /*" is: "*/" : " + 3);// + " --- actual score: " + di);
		    qscs.put(id_spk, 3);
		} else if (di <= qt_thrs[2] &&
			   di > qt_thrs[3] &&
			   di > 0) {
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((String)spks.get(j)) + /*" is: "*/" : " + 2);// + " --- actual score: " + di);
		    qscs.put(id_spk, 2);
		} else if (di <= qt_thrs[3]) {
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((String)spks.get(j)) + /*" is: "*/" : " + 1);// + " --- actual score: " + di);
		    qscs.put(id_spk, 1);
		}
	    }
	}
	/*
	if (ind_type.equals("Merged")) {
	    HashMap final_qscs = new HashMap();
	    if (ass_type.equals("TPC")) {
		for (int j = 0; j < dis.size(); j++) {
		    double di = ((Double)dis.get(j)).doubleValue();
		    String spknm = ((Speaker)spks.get(j)).getName();
		    int index = spks_.indexOf(spknm);
		    String id_spk = index + "_" + spknm;
		    Speaker spk = (Speaker)spks.get(j);
		    StringBuffer evidence = new StringBuffer();
		    evidence.append("Participant " + spk.getName() + " introduces " + spk.getTC().getLTI() * 100 + "% of LTI Index, ");
		    evidence.append(spk.getTC().getSMT() * 100 + "% of SMT Index, ");
		    evidence.append(spk.getTC().getCS() * 100 + "% of Cite Score, ");
		    evidence.append(" and has " + spk.getTC().getTL() + " TL Index.");
		    ArrayList val_evi = new ArrayList();
		    val_evi.add(qscs.get(id_spk));
		    val_evi.add(evidence.toString());
		    final_qscs.put(id_spk, val_evi);
		}			
		prxmlp_.setTopicControl(final_qscs);
	    } else {
		for (int j = 0; j < dis.size(); j++) {
		    double di = ((Double)dis.get(j)).doubleValue();
		    String spknm = ((Speaker)spks.get(j)).getName();
		    int index = spks_.indexOf(spknm);
		    String id_spk = index + "_" + spknm;
		    Speaker spk = (Speaker)spks.get(j);
		    StringBuffer evidence = new StringBuffer();
		    evidence.append("Participant " + spk.getName() + " introduces " + spk.getInv().getNPI() * 100 + "% of NPI Index, ");
		    evidence.append(spk.getInv().getTI() * 100 + "% of TI Index, ");
		    evidence.append(spk.getInv().getTCI() * 100 + "% of TCI Index, ");
		    evidence.append(spk.getInv().getASMI() * 100 + "% of ASM Index, and");
		    evidence.append(spk.getInv().getALLOTP() * 100 + "% of ALLOTP Index. ");
		    ArrayList val_evi = new ArrayList();
		    val_evi.add(qscs.get(id_spk));
		    val_evi.add(evidence.toString());
		    final_qscs.put(id_spk, val_evi);
		}
		prxmlp_.setInvolvementClaim(final_qscs);
	    }
	}
	*/
    }

    public void calQuintile(ArrayList dis, ArrayList spks, String ass_type, String ind_type, ArrayList<Integer> ranks) {
	double[] dis1 = new double[dis.size()];
	for (int j = 0; j < dis.size(); j++) {
	    dis1[j] = ((Double)dis.get(j)).doubleValue();
	}
	double[] qt_thrs = Util.genQTThrs(dis1);
	HashMap qscs = new HashMap();
	//System.out.println("\n=====================================\n");
	boolean same_thrs = true;
	for (int j = 1; j < qt_thrs.length; j++) {
	    if (qt_thrs[j] != qt_thrs[j - 1]) {
		same_thrs = false;
		break;
	    }
	}
	ArrayList out_spks = new ArrayList();
	ArrayList out_qts = new ArrayList();
	ArrayList out_acts = new ArrayList();
	ArrayList out_actcs = new ArrayList(); //actual counts
	if (same_thrs) {
	    for (int j = 0; j < dis.size(); j++) {
		double di = ((Double)dis.get(j)).doubleValue();
		String spk = ((Speaker)spks.get(j)).getName();
		int index = spks_.indexOf(spk);
		String id_spk = index + "_" + spk;
		//System.out.println("ass_type: " + ass_type + " --- ind_type: " + ind_type);
		if (ass_type.equals("TPC") &&
		    ind_type.equals("Merged")) {
		    System.out.println("set tpc cs rank on " + ((Speaker)spks.get(j)).getName() + " with " + (j + 1));
		    ((Speaker)spks.get(j)).setTPCRank(ranks.get(j).intValue());
		} else if (ass_type.equals("INV") &&
			   ind_type.equals("Merged")) {
		    ((Speaker)spks.get(j)).setINVRank(ranks.get(j).intValue());
		}
		if (di == qt_thrs[0]) {
		    if (ass_type.equals("TPC")) {
			((Speaker)spks.get(j)).setTpQScore(3, ind_type);
			
		    } else if (ass_type.equals("INV")) {
			((Speaker)spks.get(j)).setInvQScore(3, ind_type);
		    }
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : " /*+ 3 + " --- actual score: "*/ + di);
		    //System.out.println("The actual count of 
		    out_spks.add(((Speaker)spks.get(j)).getName());
		    out_qts.add(3);
		    out_acts.add(di);
		    out_actcs.add(((Speaker)spks.get(j)).sizeOfILTs());
		    qscs.put(id_spk, 3);
		} else if (di > qt_thrs[0]) {
		    if (ass_type.equals("TPC")) {
			((Speaker)spks.get(j)).setTpQScore(5, ind_type);
		    } else if (ass_type.equals("INV")) {
			((Speaker)spks.get(j)).setInvQScore(5, ind_type);
		    }
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((Speaker)spks.get(j)).getName() + " : " /*+ 5 + " --- actual score: " */+ di);
		    out_spks.add(((Speaker)spks.get(j)).getName());
		    out_qts.add(5);
		    out_acts.add(di);
		    out_actcs.add(((Speaker)spks.get(j)).sizeOfILTs());
		    qscs.put(id_spk, 5);
		} else if (di < qt_thrs[1]) {
		    if (ass_type.equals("TPC")) {
			((Speaker)spks.get(j)).setTpQScore(1, ind_type);
		    } else if (ass_type.equals("INV")) {
			((Speaker)spks.get(j)).setInvQScore(1, ind_type);
		    }
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((Speaker)spks.get(j)).getName() + " : "/* + 1 + " --- actual score: " */+ di);
		    out_spks.add(((Speaker)spks.get(j)).getName());
		    out_qts.add(1);
		    out_acts.add(di);
		    out_actcs.add(((Speaker)spks.get(j)).sizeOfILTs());
		    qscs.put(id_spk, 1);
		}	    
	    }
	} else {
	    for (int j = 0; j < dis.size(); j++) {
		double di = ((Double)dis.get(j)).doubleValue();
		String spk = ((Speaker)spks.get(j)).getName();
		int index = spks_.indexOf(spk);
		String id_spk = index + "_" + spk;
		if (ass_type.equals("TPC") &&
		    ind_type.equals("Merged")) {
		    //System.out.println("set tpc cs rank on " + ((Speaker)spks.get(j)).getName() + " with " + (j + 1));
		    ((Speaker)spks.get(j)).setTPCRank(ranks.get(j).intValue());
		} else if (ass_type.equals("INV") &&
			   ind_type.equals("Merged")) {
		    ((Speaker)spks.get(j)).setINVRank(ranks.get(j).intValue());
		}
		if (qt_thrs[0] == qt_thrs[1] &&
		    qt_thrs[1] == qt_thrs[2] &&
		    di == qt_thrs[0]) {
		    if (ass_type.equals("TPC")) {
			((Speaker)spks.get(j)).setTpQScore(4, ind_type);
		    } else if (ass_type.equals("INV")) {
			((Speaker)spks.get(j)).setInvQScore(4, ind_type);
		    }
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 4 + " --- actual score: " */+ di);
		    out_spks.add(((Speaker)spks.get(j)).getName());
		    out_qts.add(4);
		    out_acts.add(di);
		    out_actcs.add(((Speaker)spks.get(j)).sizeOfILTs());
		    qscs.put(id_spk, 4);
		}
		else if (di > qt_thrs[0] &&
			 di > 0) {
		    if (ass_type.equals("TPC")) {
			((Speaker)spks.get(j)).setTpQScore(5, ind_type);
		    } else if (ass_type.equals("INV")) {
			((Speaker)spks.get(j)).setInvQScore(5, ind_type);
		    }
		    out_spks.add(((Speaker)spks.get(j)).getName());
		    out_qts.add(5);
		    out_acts.add(di);
		    out_actcs.add(((Speaker)spks.get(j)).sizeOfILTs());
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score of " + */((Speaker)spks.get(j)).getName() + " : "/* + 5 + " --- actual score: "*/ + di);
		    qscs.put(id_spk, 5);
		} else if (di <= qt_thrs[0] &&
			   di > qt_thrs[1] &&
			   di > 0) {
		    if (ass_type.equals("TPC")) {
			((Speaker)spks.get(j)).setTpQScore(4, ind_type);
		    } else if (ass_type.equals("INV")) {
			((Speaker)spks.get(j)).setInvQScore(4, ind_type);
		    }
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((Speaker)spks.get(j)).getName() + " : "/* + 4 + " --- actual score: " */+ di);
		    out_spks.add(((Speaker)spks.get(j)).getName());
		    out_qts.add(4);
		    out_acts.add(di);
		    out_actcs.add(((Speaker)spks.get(j)).sizeOfILTs());
		    qscs.put(id_spk, 4);
		} else if (di <= qt_thrs[1] &&
		       di > qt_thrs[2] &&
			   di > 0) {
		    if (ass_type.equals("TPC")) {
			((Speaker)spks.get(j)).setTpQScore(3, ind_type);
		    } else if (ass_type.equals("INV")) {
			((Speaker)spks.get(j)).setInvQScore(3, ind_type);
		    }
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((Speaker)spks.get(j)).getName() + " : "/* + 3 + " --- actual score: " */+ di);
		    out_spks.add(((Speaker)spks.get(j)).getName());
		    out_qts.add(3);
		    out_acts.add(di);
		    out_actcs.add(((Speaker)spks.get(j)).sizeOfILTs());
		    qscs.put(id_spk, 3);
		} else if (di <= qt_thrs[2] &&
			   di > qt_thrs[3] &&
			   di > 0) {
		    if (ass_type.equals("TPC")) {
			((Speaker)spks.get(j)).setTpQScore(2, ind_type);
		    } else if (ass_type.equals("INV")) {
			((Speaker)spks.get(j)).setInvQScore(2, ind_type);
		    }
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((Speaker)spks.get(j)).getName() + " : " /*+ 2 + " --- actual score: "*/ + di);
		    out_spks.add(((Speaker)spks.get(j)).getName());
		    out_qts.add(2);
		    out_acts.add(di);
		    out_actcs.add(((Speaker)spks.get(j)).sizeOfILTs());
		    qscs.put(id_spk, 2);
		} else if (di <= qt_thrs[3]) {
		    if (ass_type.equals("TPC")) {
			((Speaker)spks.get(j)).setTpQScore(1, ind_type);
		} else if (ass_type.equals("INV")) {
			((Speaker)spks.get(j)).setInvQScore(1, ind_type);
		    }
                    // modified by Laura, Apirl 13, 2011
		    System.out.println(/*"The quintile score " + */((Speaker)spks.get(j)).getName() + " : "/* + 1 + " --- actual score: " */+ di);
		    out_spks.add(((Speaker)spks.get(j)).getName());
		    out_qts.add(1);
		    out_acts.add(di);
		    out_actcs.add(((Speaker)spks.get(j)).sizeOfILTs());
		    qscs.put(id_spk, 1);
		}
	    }
	}

	if (ind_type.equals("Merged")) {
	    HashMap final_qscs = new HashMap();
	    if (ass_type.equals("TPC")) {
		for (int j = 0; j < dis.size(); j++) {
		    double di = ((Double)dis.get(j)).doubleValue();
		    String spknm = ((Speaker)spks.get(j)).getName();
		    int index = spks_.indexOf(spknm);
		    String id_spk = index + "_" + spknm;
		    Speaker spk = (Speaker)spks.get(j);
		    StringBuffer evidence = new StringBuffer();
		    evidence.append("Participant " + spk.getName() + " introduces " + spk.getTC().getLTI() * 100 + "% of LTI Index, ");
		    evidence.append(spk.getTC().getSMT() * 100 + "% of SMT Index, ");
		    evidence.append(spk.getTC().getCS() * 100 + "% of Cite Score, ");
		    evidence.append(" and has " + spk.getTC().getTL() + " TL Index.");
		    ArrayList val_evi = new ArrayList();
		    val_evi.add(qscs.get(id_spk));
		    val_evi.add(evidence.toString());
		    final_qscs.put(id_spk, val_evi);
		}			
		//prxmlp_.setTopicControl(final_qscs);
	    } else {
		for (int j = 0; j < dis.size(); j++) {
		    double di = ((Double)dis.get(j)).doubleValue();
		    String spknm = ((Speaker)spks.get(j)).getName();
		    int index = spks_.indexOf(spknm);
		    String id_spk = index + "_" + spknm;
		    Speaker spk = (Speaker)spks.get(j);
		    StringBuffer evidence = new StringBuffer();
		    evidence.append("Participant " + spk.getName() + " introduces " + spk.getInv().getNPI() * 100 + "% of NPI Index, ");
		    evidence.append(spk.getInv().getTI() * 100 + "% of TI Index, ");
		    evidence.append(spk.getInv().getTCI() * 100 + "% of TCI Index, ");
		    evidence.append(spk.getInv().getASMI() * 100 + "% of ASM Index, and");
		    evidence.append(spk.getInv().getALLOTP() * 100 + "% of ALLOTP Index. ");
		    ArrayList val_evi = new ArrayList();
		    val_evi.add(qscs.get(id_spk));
		    val_evi.add(evidence.toString());
		    final_qscs.put(id_spk, val_evi);
		}
		//prxmlp_.setInvolvementClaim(final_qscs);
	    }
	}
	/*

	for (int i = 0; i < out_spks.size(); i++) {
	    System.out.print("," + out_spks.get(i));
	}
	System.out.println();
	for (int i = 0; i < out_qts.size(); i++) {
	    System.out.print(", " + out_qts.get(i));
	}
	System.out.println();
	for (int i = 0; i < out_actcs.size(); i++) {
	    System.out.print(", " + out_actcs.get(i));
	}
	System.out.println();
	*/
	
	
    }

    public void calTkCtl(int j) {
	is_inv_ = false;
	is_tskc_ = true;
	//for (int j = 0; j < docs_utts_.size(); j++) {
	//utts_ = (ArrayList)docs_utts_.get(j);
	//System.out.println("\n\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\nprocessing: " + doc_names_.get(j));
	/*
	parts_.clear();
	PhraseCheck phr_ch = (PhraseCheck)phr_checks_.get(j);
	XMLParse xp = xmlps_.get(j);
	buildLocalTopicList(phr_ch, xp);
	*/
	tkc_ = new TaskControl(parts_);
	tkc_.setUtterances(utts_);
	tkc_.calTkC(prxmlp_, spks_, nls_);
	//}
    }

    public void calInv(int k) {
	is_inv_ = true;
	is_tskc_ = false;
	//for (int k = 0; k < docs_utts_.size(); k++) {
	//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\nprocessing: " + (String)doc_names_.get(k));
	//utts_ = (ArrayList)docs_utts_.get(k);
	/*
	parts_.clear();
	*/
	PhraseCheck phr_ch = (PhraseCheck)phr_checks_.get(k);
	XMLParse xp = xmlps_.get(k);
	if (Settings.getValue(Settings.PROCESS_TYPE).equals("annotated")) {
	    //parts_.clear();
	    //buildALocalTopicList(phr_ch, xp);
	} else if (Settings.getValue(Settings.PROCESS_TYPE).equals("automated")) {
	    //buildLocalTopicList(phr_ch, xp );
	}
	ArrayList keys = new ArrayList(parts_.keySet());
	for (int i = 0; i < keys.size(); i++) {
	    String key = (String)keys.get(i);
	    Speaker spk = parts_.get(key);
	    if (Settings.getValue(Settings.PROCESS_TYPE).equals("annotated")) {
		spk.calAInv(lts_, utts_, nls1_, top10nouns_, spks_);
	    } else if (Settings.getValue(Settings.PROCESS_TYPE).equals("automated")) {
		spk.calInv(lts_, utts_, nls_, top10nouns_);
	    }
	    /*
	      System.out.println("==========================================");
	      System.out.println("speaker name: " + spk.getName());
	      System.out.println(spk.getInv());
	    */
	}
	keys = new ArrayList(parts_.keySet());
	for (int i = 1; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    for (int jj = 0; jj < i; jj++) {
		String key2 = (String)keys.get(jj);
		Speaker spk2 = parts_.get(key2);
		if (spk1.getInv().getNPI() < spk2.getInv().getNPI()) {
		    spk1.getInv().incNPIR();
		}else {
		    spk2.getInv().incNPIR();
		}
		if (spk1.getInv().getTI() < spk2.getInv().getTI()) {
		    spk1.getInv().incTIR();
		}else {
		    spk2.getInv().incTIR();
		}
		if (spk1.getInv().getTCI() < spk2.getInv().getTCI()) {
		    spk1.getInv().incTCIR();
		}else {
		    spk2.getInv().incTCIR();
		}
		if (spk1.getInv().getALLOTP() < spk2.getInv().getALLOTP()) {
		    spk1.getInv().incALLOTPR();
		}else {
		    spk2.getInv().incALLOTPR();
		}
		if (spk1.getInv().getASMI() < spk2.getInv().getASMI()) {
		    spk1.getInv().incASMIR();
		}else {
		    spk2.getInv().incASMIR();
		}
	    }
	}
	    
	//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	//process NPI quintile
	ArrayList spks = new ArrayList();
	ArrayList spks_NPI = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_NPI = new Double(spk1.getInv().getNPI());
	    boolean added = false;
	    for (int j = 0; j < spks_NPI.size(); j++) {
		Double spk2_NPI = (Double)spks_NPI.get(j);
		if (spk1_NPI > spk2_NPI &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_NPI.add(j, spk1_NPI);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_NPI.add(spk1_NPI);
	    }
	}	
	//System.out.println("\n++++++++++++++++++++++++++++++++\ncalculate Involvement - NPI quintile");
	/*
	System.out.print("NPI");
	calQuintile(spks_NPI, spks, "INV", "NPI", null);
	*/
	//process TI quintile
	spks = new ArrayList();
	ArrayList spks_TI = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_TI = new Double(spk1.getInv().getTI());
	    boolean added = false;
	    for (int j = 0; j < spks_TI.size(); j++) {
		Double spk2_TI = (Double)spks_TI.get(j);
		if (spk1_TI > spk2_TI &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_TI.add(j, spk1_TI);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_TI.add(spk1_TI);
	    }
	}	
	//System.out.println("\n++++++++++++++++++++++++++++++++\ncalculate Involvement - TI quintile");
	/*
	System.out.print("TI");
	calQuintile(spks_TI, spks, "INV", "TI", null);
	*/
	//process TCI quintile
	spks = new ArrayList();
	ArrayList spks_TCI = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_TCI = new Double(spk1.getInv().getTCI());
	    boolean added = false;
	    for (int j = 0; j < spks_TCI.size(); j++) {
		Double spk2_TCI = (Double)spks_TCI.get(j);
		if (spk1_TCI > spk2_TCI &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_TCI.add(j, spk1_TCI);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_TCI.add(spk1_TCI);
	    }
	}	
	//System.out.println("\n++++++++++++++++++++++++++++++++\ncalculate Involvement - TCI quintile: " + spks_TCI);
	/*
	System.out.print("TCI");
	calQuintile(spks_TCI, spks, "INV", "TCI", null);
	*/
	//process ALLOTP quintile
	spks = new ArrayList();
	ArrayList spks_ALLOTP = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_ALLOTP = new Double(spk1.getInv().getALLOTP());
	    boolean added = false;
	    for (int j = 0; j < spks_ALLOTP.size(); j++) {
		Double spk2_ALLOTP = (Double)spks_ALLOTP.get(j);
		if (spk1_ALLOTP > spk2_ALLOTP &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_ALLOTP.add(j, spk1_ALLOTP);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_ALLOTP.add(spk1_ALLOTP);
	    }
	}	
	//System.out.println("\n++++++++++++++++++++++++++++++++\ncalculate Involvement - ALLOTP quintile");
	/*
	System.out.print("ALLOTP");
	calQuintile(spks_ALLOTP, spks, "INV", "ALLOTP", null);
	*/
	//process ASMI quintile
	spks = new ArrayList();
	ArrayList spks_ASMI = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_ASMI = new Double(spk1.getInv().getASMI());
	    boolean added = false;
	    for (int j = 0; j < spks_ASMI.size(); j++) {
		Double spk2_ASMI = (Double)spks_ASMI.get(j);
		if (spk1_ASMI > spk2_ASMI &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_ASMI.add(j, spk1_ASMI);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_ASMI.add(spk1_ASMI);
	    }
	}	
	//System.out.println("\n++++++++++++++++++++++++++++++++\ncalculate Involvement - ASMI quintile");
	/*
	System.out.print("ASMI");
	calQuintile(spks_ASMI, spks, "INV", "ASMI", null);
	*/
	
	/*
	 */
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    spk1.getInv().calPower();
	    //System.out.println("The involvement power of " + spk1.getName() + " is: " + spk1.getInv().getPower());
	}
	//process Power quintile
	spks = new ArrayList();
	ArrayList spks_Power = new ArrayList();
	for (int i = 0; i < keys.size(); i++) {
	    String key1 = (String)keys.get(i);
	    Speaker spk1 = parts_.get(key1);
	    Double spk1_Power = new Double(spk1.getInv().getPower());
	    boolean added = false;
	    for (int j = 0; j < spks_Power.size(); j++) {
		Double spk2_Power = (Double)spks_Power.get(j);
		if (spk1_Power > spk2_Power &&
		    !(spk1.getName().startsWith("ilsp") ||
		      spk1.getName().startsWith("samira") ||
		      spk1.getName().startsWith("tomek"))) {
		    spks.add(j, spk1);
		    spks_Power.add(j, spk1_Power);
		    added = true;
		    break;
		}
	    }
	    if (!added &&
		!(spk1.getName().startsWith("ilsp") ||
		  spk1.getName().startsWith("samira") ||
		  spk1.getName().startsWith("tomek"))) {
		spks.add(spk1);
		spks_Power.add(spk1_Power);
	    }
	}	
	ArrayList rank = new ArrayList();
	rank.add(1);
	for (int j = 1; j < spks_Power.size(); j++) {
	    Double spk2_Power = (Double)spks_Power.get(j);
	    Double spk1_Power = (Double)spks_Power.get(j - 1);
	    if (spk1_Power.doubleValue() == spk2_Power.doubleValue()) {
		rank.add(rank.get(rank.size() - 1));
	    } else {
		rank.add(j + 1);
	    }
	}
	//System.out.println("++++++++++++++++++++++++++++++++\ncalculate Merged quintile");
	calQuintile(spks_Power, spks, "INV", "Merged", rank);
    }
    
    //}

    public void calCommLink(XMLParse xp) {
	/*
	for (int i = 0; i < docs_utts_.size(); i++) 
	{		
	    utts_ = (ArrayList<Utterance>)docs_utts_.get(i);
	    //	    utts_ = cl.getUtts();
	    */
	CommunicationLinkX clx = new CommunicationLinkX(utts_, isEnglish_, isChinese_);
	    //cl.setUtts(utts_);
	    //cl.calCLFts();
	    clx.collectCLFtsX();
	    utts_ = clx.getUtts();
	    //    	WriteXML wlx = new WriteXML(clx.getMap(),utts_);
	    //    	wlx.outputXML();

	    //}    
	ArrayList tr_utts = new ArrayList();
	CommunicationLink cl = new CommunicationLink(all_utts_, tr_utts, wn_, utts_, parts_);
	cl.setUtts(utts_);
	cl.callContOf();
	utts_ = cl.getUtts();
	
	ArrayList cls = new ArrayList();
	for(int index=0; index<utts_.size(); index++)
	    {
		if(utts_.get(index).getRespTo() == null) {
		    utts_.get(index).setRespTo("all-users");
		    cls.add("all-users");
		} else { cls.add(utts_.get(index).getRespTo()); }
		
		//System.out.println("Turn no " + (index+1) + " : " + utts_.get(index).getSysRespTo());
	    }
	xp.setCommActLinks(cls);
	//	    WriteXML wx = new WriteXML(cl.getMap(),utts_);
	//	    wx.outputXML(); 
    }

    public void loadAndParse() {
	try {
	    //System.out.println("load files...");
	    //loadAndParseTraining();
	    File tp = new File(docs_path_);
	    ArrayList ps = new ArrayList();
	    while (tp != null) {
		File[] fls = tp.listFiles();
		ArrayList fls1 = new ArrayList(Arrays.asList(fls));
		Collections.sort(fls1);
		for (int i = 0; i < fls1.size(); i++) {
		    File fl = (File)fls1.get(i);
		    //System.out.println("+++++++++++++++++++++++++++++++++++++++++\nprocessing: " + fl.getName());
		    if (fl.isDirectory()) {
			ps.add(fl);
		    }
		    else {
//			System.out.println("loading " + fl.getName() + "...");
			doc_names_.add(fl.getName());
			if (fl.getName().endsWith("xml")) {
			    Document doc = dsarmdDP_.getDocument(fl.getAbsolutePath());
			    ArrayList list = dsarmdDP_.parseDAList(doc,
								   "dsarmd",
								   false,
								   null);
			    XMLParse xp = new XMLParse(fl.getAbsolutePath(), list);
			    xmlps_.add(xp);
			    PhraseCheck phr_ch = new PhraseCheck(fl.getAbsolutePath(), xp.getUtterances());
			    phr_checks_.add(phr_ch);
			    docs_.add(doc);
			    //utts_.addAll(list);
			    docs_utts_.add(list); //for test agreement between annotators
			    //all_utts_.addAll(list);
			    fls_.add(fl.getName());
			} else {
			    ArrayList list = DocumentProcessor.parseUtts(fl);
			    //System.out.println("list: \n" + list);
			    if (list != null) {
				XMLParse xp = new XMLParse(fl.getAbsolutePath(), list);
				xmlps_.add(xp);
				PhraseCheck phr_ch = new PhraseCheck(fl.getAbsolutePath(), xp.getUtterances());
				phr_checks_.add(phr_ch);
				//docs_.add(doc);
				//utts_.addAll(list);
				docs_utts_.add(list); //for test agreement between annotators
				//all_utts_.addAll(list);
				fls_.add(fl.getName());
			    }
			}
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

    public void loadAndParseTraining() {
	try {
	    //System.out.println("load files...");
	    File tp = new File(Settings.getValue(Settings.TRAINING) + "_" + Settings.getValue(Settings.LANGUAGE));
	    ArrayList ps = new ArrayList();
	    while (tp != null) {
		File[] fls = tp.listFiles();
		ArrayList fls1 = new ArrayList(Arrays.asList(fls));
		Collections.sort(fls1);
		for (int i = 0; i < fls1.size(); i++) {
		    File fl = (File)fls1.get(i);
		    //System.out.println("+++++++++++++++++++++++++++++++++++++++++\nprocessing: " + fl.getName());
		    //prxmlp_.setDataUnit(fl.getName().split("\\.")[0], fl.getName().split("\\.")[0]);
		    if (fl.isDirectory()) {
			ps.add(fl);
		    }
		    else {
			//System.out.print("loading " + fl.getAbsolutePath() + "...");
			//doc_names_.add(fl.getName());
			Document doc = dsarmdDP_.getDocument(fl.getAbsolutePath());
			ArrayList list = dsarmdDP_.parseDAList(doc,
							       "dsarmd",
							       false,
							       null);
			//all_utts_.addAll(list);
			tr_utts_.addAll(list);
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

    public void buildLocalTopicList(PhraseCheck phr_ch, XMLParse xp) {
	ArrayList utts = new ArrayList();
	ArrayList spks = new ArrayList();
	ArrayList comm_acts = new ArrayList();
	ArrayList resps_to = new ArrayList();
	ArrayList turn_nums = new ArrayList();
	lts_.clear();
	spks_.clear();
	nls_ = null;
	parts_.clear();
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt = utts_.get(i);
	    utts.add(utt.getContent());
	    /*
	    if (utt.getContent().split("[ ]+").length < 80 &&
		utt.getContent().split("[ ]+").length > 5) {
		System.out.println("parsing: " + utt.getContent());
		Util.parser_.parse(utt.getContent());
		//Util.parser_.printTree(Util.parser_.getTree());
		Util.parser_.showAllDependencies(Util.parser_.getDependencies(), utt.getTag());
	    }
	    */
	    String spk = utt.getSpeaker();
	    spks.add(spk.toLowerCase());
	    comm_acts.add(utt.getCommActType());
	    resps_to.add(utt.getRespTo());
	    turn_nums.add(utt.getTurn());
	    //if (spk.startsWith("ils")) continue;//only for test!!! remember to remove
	    if (!spks_.contains(spk.toLowerCase())) spks_.add(spk.toLowerCase());
	    Speaker part = parts_.get(spk.toLowerCase());
	    if (part == null) {
		part = new Speaker(spk.toLowerCase());
		part.setAllUtts(utts_); //1/26/2011
		parts_.put(spk.toLowerCase(), part);
	    }
	    part.addUtterance(utt);
	}
	Collections.sort(spks_);
	//comment it out for TkC
	if (!is_tskc_) {
	    FindVerb fv = new FindVerb();
	    //nls_ = new NounList(utts, spks, wn_, comm_acts, resps_to, turn_nums, phr_ch, xp);
	    nls_ = new NounList(xp, wn_, phr_ch, utts_);
            if(isEnglish_){
		    nls_.setEnglish(true);                
            }
            if(isUrdu_){
                nls_.setUrdu (true);
                nls_.setUrduPath (Settings.getValue (Settings.URDU_PATH));
            }
            if(isChinese_){
                nls_.setChinese (true);
//                nls_.setCnwn(CNWN);
            }
//	    if ((Settings.getValue (Settings.LANGUAGE)).equals("english") )
//		{
//		    isEnglish_ = true;
//		    nls_.setEnglish(true);
//                    StanfordPOSTagger.initialize();
//		}
//	    if ((Settings.getValue (Settings.LANGUAGE)).equals("urdu") )
//		{
//		    isUrdu_ = true;
//		    nls_.setUrdu (true);
//		    nls_.setUrduPath (Settings.getValue (Settings.URDU_PATH));
//		}
//            //m2w: chinese 5/17/11 1:39 PM
//            if ((Settings.getValue (Settings.LANGUAGE)).equals("chinese") )
//		{
//		    isChinese_ = true;
//		    nls_.setChinese (true);
//                    StanfordPOSTagger.initializeChinese();
//		}
	    nls_.createList(fv);
	    /*
	    if ((Settings.getValue (Settings.URDU)).equals("yes") )
		{
		    nls_.setUrdu (true);
		    nls_.setUrduPath (Settings.getValue (Settings.URDU_PATH));
		}
	    */
	    nls_.createList(fv);
	}
	//end comment
	//only for invlovement
	//if (is_inv_) {
	    nls_.sortIt();
	    nls_.countThP();
	    top10nouns_.clear();
	    setTop10nouns();
	    //}
	
	//end comment
	/*comment it out for TkC*/
	if (!is_tskc_) {
	    ArrayList nouns = nls_.getNouns();
	    for (int i = 0; i < nouns.size(); i++) {
		NounToken nt = (NounToken)nouns.get(i);
		String spk = nt.getSpeaker();
		LocalTopic lt = lts_.add(nt, spk);
		Speaker part = parts_.get(spk);
		if (lt == null || part == null) continue;
		if (nt.firstMention() ||
		    spk.equals(lt.getIntroducer())) {
		    part.addILT(lt);
		} else {
		    part.addCLT(lt);
		}
		part.addNoun(nt);
	    }
	    /*
	    System.out.println("size of LTs: " + lts_.size());
	    System.out.println("size of mentions: " + lts_.sizeOfMentions());
	    */
	}
	System.out.println("local topics: " );
	for (int i = 0; i < lts_.size(); i++) {
	    LocalTopic lt_ = (LocalTopic)lts_.get(i);
	    System.out.println("lt" + i + ": " + lt_);
	}
	for (int k = 0; k < utts_.size(); k++) {
	    Utterance utt_ = (Utterance)utts_.get(k);
	    //System.out.println(utt_.getTurn() + " || " + utt_.getSpeaker() + " || " + utt_.getTag() + " || " + utt_.getCommActType() 
	    //		       + " || " + utt_.getRespTo() + ": " + utt_.getTaggedContent());
	}
	//prxmlp_.setClaimCount(4 * parts_.size());
	/*end comment*/
    }
    
    public void buildALocalTopicList(PhraseCheck phr_ch, XMLParse xp) {
	ArrayList nouns = new ArrayList();
	lts_.clear();
	spks_.clear();
	nls_ = null;
	parts_.clear();
	for (int i = 0; i < utts_.size(); i++) {
	    Utterance utt = utts_.get(i);
	    String spk = utt.getSpeaker();
	    if (!spks_.contains(spk.toLowerCase())) spks_.add(spk.toLowerCase());
	    //if (spk.startsWith("ils")) continue;//only for test!!! remember to remove
	    Speaker part = parts_.get(spk.toLowerCase());
	    if (part == null) {
		part = new Speaker(spk.toLowerCase());
		part.setAllUtts(utts_); //1/26/2011
		parts_.put(spk.toLowerCase(), part);
	    }
	    part.addUtterance(utt);
	    String turnno = utt.getTurn();
	    //System.out.println("turnno: " + turnno);
	    String turnnos[] = turnno.split("\\.");
	    int tn = Integer.parseInt(turnnos[0]);
	    String tag = utt.getTag();
	    String pos = utt.getPOS();
	    String pos_ori = utt.getPOSORIGIN();
	    //System.out.println("pos: " + pos);
	    //System.out.println("pos_ori: " + pos_ori);
	    if (pos != null) {
		//process reference
		String[] list = pos.split(";");
		for (int j = 0; j < list.length; j++) {
		    if (list[j].trim().length() == 0) continue;
		    String[] noun_comp = list[j].split("[\\(\\)]");
		    if (noun_comp.length == 1) continue;
		    //System.out.println("word: " + noun_comp[0] + "  reference id: " + noun_comp[1]);
		    NounToken nt = new NounToken(noun_comp[0], tag, spk, tn, null, noun_comp[1].trim());
		    boolean found_pref = false;
		    for (int k = 0; k < nouns.size(); k++) {
			NounToken nt1 = (NounToken)nouns.get(k);
			if (nt1.getId() != null &&
			    nt1.getId().equals(noun_comp[1].trim())) {
			    nt1.addSubsequent(noun_comp[1].trim());
			    found_pref = true;
			    break;
			}
		    }
		    if (!found_pref) {
			//System.out.println("not found the reference");
			nt = new NounToken(noun_comp[0], tag, spk, tn, noun_comp[1].trim(), null);
		    }
		    nouns.add(nt);
		}
	    }
	    if (pos_ori != null) {
		//process reference
		String[] list = pos_ori.split(";");
		for (int j = 0; j < list.length; j++) {
		    if (list[j].trim().length() == 0) continue;
		    String[] noun_comp = list[j].split("[\\(\\)]");
		    //System.out.println("origin--- word: " + noun_comp[0] + "  id: " + noun_comp[1]);
		    NounToken nt = new NounToken(noun_comp[0], tag, spk, tn, noun_comp[1], null);
		    nouns.add(nt);
		}
	    }
	}
	Collections.sort(spks_);
	//only for invlovement
	//if (is_inv_) {
	    nls1_ = nouns;
	    sortNTs(nls1_);
	    top10nouns_.clear();
	    setATop10nouns();
	    //}
	//done for involvement
	for (int i = 0; i < nouns.size(); i++) {
	    NounToken nt = (NounToken)nouns.get(i);
	    String spk = nt.getSpeaker();
	    LocalTopic lt = lts_.addA(nt, spk.toLowerCase());
	    Speaker part = parts_.get(spk.toLowerCase());
	    if (lt == null || part == null) continue;
	    if (nt.firstAMention() ||
		spk.equals(lt.getIntroducer())) {
		part.addAILT(lt);
	    } else {
		part.addACLT(lt);
	    }
	    part.addNoun(nt);
	}
	//prxmlp_.setClaimCount(4 * parts_.size());
	/*
	System.out.println("size of LTs: " + lts_.size());
	System.out.println("size of mentions: " + lts_.sizeOfMentions());
	*/
    }

    public void sortNTs(ArrayList nouns) {
	for (int i = 0; i < nouns.size() - 1; i++) {
	    NounToken n1 = (NounToken)nouns.get(i);
	    for (int j = i + 1; j < nouns.size(); j++) {
		NounToken n2 = (NounToken)nouns.get(j);
		if (n1.sizeOfSubSM() < n2.sizeOfSubSM()) {
		    nouns.set(i, n2);
		    nouns.set(j, n1);
		    n1 = n2;
		}
	    }
	}
    }
    
    public void setTop10nouns() {
	ArrayList nouns = nls_.getNouns();
	ArrayList top10Ids = new ArrayList();
	for (int i = 0; i < 10; i++) {
	    NounToken noun = (NounToken)nouns.get(i);
	    Integer id = noun.getID();
	    top10Ids.add(id);
	}
	for (int i = 0; i < nouns.size(); i++) {
	    NounToken noun = (NounToken)nouns.get(i);
	    Integer noun_id = new Integer(noun.getID());
	    Integer noun_ref_id;
	    try {
		noun_ref_id = new Integer(noun.getRefInt());
	    }catch (NumberFormatException nfe) {
		nfe.printStackTrace();
		continue;
	    }
	    if (spks_.contains(noun.getWord().toLowerCase())) continue;
	    if (top10Ids.contains(noun_id) ||
		top10Ids.contains(noun_ref_id)) {
		top10nouns_.add(noun);
	    }
	}
	/*
	for (int i = 0; i < top10nouns_.size() && i < 10; i++) {
	    NounToken noun = (NounToken)top10nouns_.get(i);
	    if (noun.getRefInt() != -1) continue;
	    System.out.println(noun.toString());
	}
	*/
	//System.out.println("size of top10nouns: " + top10nouns_.size());
    }
    public String getTimeStamp ()
    {
	String to_return = "";
	java.util.Calendar cal = java.util.Calendar.getInstance ();
	to_return = to_return + cal.get (java.util.Calendar.YEAR) + "_" 
	    + cal.get (java.util.Calendar.MONTH) + "_" +
	    + cal.get (java.util.Calendar.DAY_OF_MONTH) + "_" + 
	    + cal.get (java.util.Calendar.HOUR_OF_DAY) + "_" + 
	    + cal.get (java.util.Calendar.MINUTE) + "_" + 
	    + cal.get (java.util.Calendar.SECOND) + "_";
	return to_return;
    }
    public void setATop10nouns() {
	ArrayList nouns = nls1_;
	ArrayList top10Ids = new ArrayList();
	int count = 0;
	for (int i = 0; i < nouns.size(); i++) {
	    NounToken noun = (NounToken)nouns.get(i);
	    String id = noun.getId();
	    if (id == null) {continue;}
	    top10Ids.add(id);
	    if (top10Ids.size() == 10) break;
	}
	for (int i = 0; i < nouns.size(); i++) {
	    NounToken noun = (NounToken)nouns.get(i);
	    String noun_id = noun.getId();
	    String noun_ref_id = noun.getRef();
	    if (top10Ids.contains(noun_id) ||
		top10Ids.contains(noun_ref_id)) {
		top10nouns_.add(noun);
	    }
	}
	//System.out.println("size of top10nouns: " + top10nouns_.size());
    }

    /**
     * @auther m2w: this method checks the config.txt file see which language was setup in it, then set the instance variables values, and initialize the pos taggers
     * @date 7/8/11 11:16 AM
     */
    private void setLanguageAndInitPosTaggers(){
        if ((Settings.getValue (Settings.LANGUAGE)).equals("english") )
		{
		    isEnglish_ = true;
//		    nls_.setEnglish(true);
                    StanfordPOSTagger.initialize();
		}
	    if ((Settings.getValue (Settings.LANGUAGE)).equals("urdu") )
		{
		    isUrdu_ = true;
//		    nls_.setUrdu (true);
//		    nls_.setUrduPath (Settings.getValue (Settings.URDU_PATH));
		}
            //m2w: chinese 5/17/11 1:39 PM
            if ((Settings.getValue (Settings.LANGUAGE)).equals("chinese") )
		{
		    isChinese_ = true;
//		    nls_.setChinese (true);
                    StanfordPOSTagger.initializeChinese();
		}
    }
    
//    private void initChineseWordnet(){
//        CNWN = new ChineseWordnet("mysql", "localhost", "3306", "atur");
//    }
    
    public void printReport() {
	prxmlp_.showSystemReport();
    }

    public void createReport() {
	prxmlp_.createReport();
    }

    /*******************************   Attributes  **************************/
    private TopicDisagreement tpd_ = null;
    private ExpressiveDisagreement exd_ = null;
    private TaskControl tkc_ = null;
    //private TopicControl tc_ = new TopicControl();
    private ArrayList<XMLParse> xmlps_ = new ArrayList<XMLParse>();
    private ArrayList phr_checks_ = new ArrayList();
    private Wordnet wn_ = new Wordnet();
    public static DOMParser parser_ = new DOMParser();
    public static DsarmdDP dsarmdDP_ = new DsarmdDP();
    private ArrayList docs_ = new ArrayList();
    private ArrayList<Utterance> utts_ = new ArrayList<Utterance>();
    private ArrayList docs_utts_ = new ArrayList();
    private ArrayList all_utts_ = new ArrayList();
    private ArrayList tr_utts_ = new ArrayList();
    private String path_ = null;
    private ArrayList da_acts_ = new ArrayList();
    private String docs_path_ = null;
    private String train_path_ = null;
    private NounList nls_ = null;
    private ArrayList nls1_ = null;
    private HashMap<String, Speaker> parts_ = new HashMap<String, Speaker>();
    private LocalTopics lts_ = new LocalTopics();
    private ArrayList top10nouns_ = new ArrayList();
    private ArrayList doc_names_ = new ArrayList();
    ArrayList spks_ = new ArrayList();
    private boolean isInv_ = false;
    ArrayList sts_ = null; //stop words
    private boolean is_inv_ = false;
    private boolean is_tskc_ = false;
    private ArrayList fls_ = new ArrayList();
    private PtsReportXMLParser prxmlp_ = null;
    public String wekaArffPath = Settings.getValue("wekaArffPath");
    public String wekaResultsPath = Settings.getValue("wekaResultsPath");
    public String wekaClassifierName = Settings.getValue("wekaClassifierName");
    public ArrayList mts_ = new ArrayList(); //meso-topics
    private GroupCohesion gch_ = new GroupCohesion();
    private Sociability sb_ = null;
    public static DocDialogueType ddt_ = null;
    private boolean isEnglish_ = false;
    private boolean isChinese_ = false;
    private boolean isUrdu_ = false;
//    private ChineseWordnet CNWN = null;


    private class QueryListener extends Thread
    {
        String QUERY = "";
        String DIR = "/home/samirashaikh/sshaikh/develop/NLTEST/scil0200/data/webservice/";
	String BACKUP = "/home/samirashaikh/sshaikh/develop/NLTEST/scil0200/data/backup/";
        String DIR2 = "/home/samirashaikh/sshaikh/develop/NLTEST/scil0200/output/";
        //String DIR = "/Users/samirashaikh/sshaikh/develop/NLTEST/scil0200/data/webservice/";
	//String BACKUP = "/Users/samirashaikh/sshaikh/develop/NLTEST/scil0200/data/backup/";
        //String DIR2 = "/Users/samirashaikh/sshaikh/develop/NLTEST/scil0200/output/";

        String QUERY_FILE = "webservice.txt";
        String RESULTS_FILE = "results.txt";

        boolean toRun = true;

        Assertions assertions;
        QueryListener (Assertions as) { this.assertions = as; }
        public void run ()
        {
            while (toRun)
                {
                    File qFile = new File (DIR, QUERY_FILE);
                    while (!qFile.exists ())
                        {
                            try
  
                                {
                                    System.err.println ("Sleeping..");
                                    sleep (7000);
                                }
                            catch (Exception ee)
                                {
                                    ee.printStackTrace ();
                                }
                        }

                    try
                        {

                            System.err.println ("Found file");
                            loadAndParse ();
                            makeAssertions ();


                            //first move the query file so we are absolutely sure for the thread                                                                                                                                                                                                                       
			    String cmd = "mv " + DIR + "/" + QUERY_FILE + " " + BACKUP + "/" + assertions.getTimeStamp () + QUERY_FILE;
                            Process moveQueryFile = null;
                            try
                                {
                                    moveQueryFile = Runtime.getRuntime ().exec (cmd);
                                    InputStreamReader myIStreamReader = new InputStreamReader (moveQueryFile.getInputStream ());
                                    BufferedReader bufferedReader = new BufferedReader (myIStreamReader);

                                    String line;
                                    while ((line = bufferedReader.readLine ()) != null)
                                        {
                                            System.err.println (line);
                                        }
                                    InputStreamReader myErrorStreamReader = new InputStreamReader (moveQueryFile.getErrorStream ());
                                    BufferedReader bufferedErrorReader = new BufferedReader (myErrorStreamReader);
                                    String eLine;
                                    while ((eLine = bufferedErrorReader.readLine ()) != null)
                                        {
                                            System.err.println (eLine);
                                        }

                                    if (moveQueryFile.waitFor () != 0)
                                        System.err.println ("ERROR" + moveQueryFile.exitValue ());
                                    else System.err.println ("removed query file");

                                    myIStreamReader.close ();
                                }
                            catch (IOException ioe )
                                {
				    ioe.printStackTrace ();
                                }
                            cmd = "cp " + DIR2 + "/webservice.txt " + DIR2 + "/results.txt";
                            Process renameResultsFile = null;
                            try
                                {
                                    renameResultsFile = Runtime.getRuntime ().exec (cmd);
                                    InputStreamReader myIStreamReader = new InputStreamReader (renameResultsFile.getInputStream ());
                                    BufferedReader bufferedReader = new BufferedReader (myIStreamReader);
                                    String line;
                                    while ( (line = bufferedReader.readLine ()) != null)
                                        {
                                            System.err.println (line);
                                        }
                                    InputStreamReader myErrorStreamReader = new InputStreamReader (renameResultsFile.getErrorStream ());
                                    BufferedReader bufferedErrorReader = new BufferedReader (myErrorStreamReader);
                                    String eLine;
                                    while ( (eLine = bufferedErrorReader.readLine ()) != null)
                                        {
                                            System.err.println (eLine);
                                        }
                                    if (renameResultsFile.waitFor () != 0)
                                        System.err.println ("ERROR" + renameResultsFile.exitValue ());
                                    else System.err.println ("copied file");
                                    myIStreamReader.close ();
                                    myErrorStreamReader.close ();
                                }
                            catch (Exception ee)
                                {
                                    ee.printStackTrace ();
                                }
                        }
                    catch (Exception ee)
                        {
                            ee.printStackTrace ();
                        }
                }
        }
    }
}
