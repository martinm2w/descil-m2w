package edu.albany.ils.dsarmd0200.lu;


/**
 * @file: DialogueActType.java
 * @author: Ting Liu
 * This file is used to create the DialogueAct type of the current utterance
 */

import edu.albany.ils.dsarmd0200.cuetag.weka.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash.*;
import java.util.*;


public class DialogueActType extends Opinion{
    
    public DialogueActType(ArrayList all_utts_,
			   ArrayList tr_utts_,
			   ArrayList<Utterance> utts_) {
	super();
	this.all_utts_ = all_utts_;
	this.tr_utts_ = tr_utts_;
	this.utts_ = utts_;
	tagType_ = Settings.getValue("tagType");
	//System.out.println("go into processFeatures...");
	if (tagType_.equals("da15")) {
	    processFeatures();
	    prepareForTraining();
	}else {
	    prepareForTraining3();
	}
    }

    /*******************************get information**************************/


    /*******************************set information**************************/
    public void prepareForTraining() {
        /* generate training arff file */
        ArrayList tagList = ca.getTagList();
	trainingArff = wekaArffPath + "training.arff";
	testingArff = wekaArffPath + "testing.arff";
        ArffGenerator ag = new ArffGenerator("training.arff", trainingArff, "testing.arff",
					     testingArff, tr_utts_, utts_, tagList, featuresMap_, allFeatures_);
        ag.writeTrainingArff();
        
        /* generate testing arff file */
        Boolean nonB = false;
        if(nonBlank.equals("nonBlank"))
            nonB = true;
        ag.writeTestingArff(nonB);

	//da_arff_gen_ = new DialogActArffGen(all_utts_, tr_utts_, utts_);
	//da_arff_gen_.generateArffFile(trainingArff, testingArff);
    }
    public void prepareForTraining3() {
	trainingArff = wekaArffPath + "training3.arff";
	testingArff = wekaArffPath + "testing3.arff";

	da_arff_gen_ = new DialogActArffGen(all_utts_, tr_utts_, utts_);
	da_arff_gen_.generateArffFile(trainingArff, testingArff);
    }

    public void tagIt3() {
	String trainingArffBatchFiltered = wekaArffPath + "training3_batch.arff";
	String testingArffBatchFiltered = wekaArffPath + "testing3_batch.arff";
	String wekaResults = wekaResultsPath + "wekaClassifierName3.result";
	weka_classify3_ = new edu.albany.ils.dsarmd0200.cuetag.weka.WekaClassify(trainingArff, testingArff,
										 trainingArffBatchFiltered, testingArffBatchFiltered, wekaResults, wekaClassifierName);
	weka_classify3_.classify();
	results_ = new TaggingResults(wekaResults, utts_, "dialog_act" + Settings.getValue("tagNum"));
	utts_ = results_.getTaggingResults();
	//System.out.println("utts: " + utts_);
	//results_.writeTaggingXml("/home/gjiao/Desktop/Sep1/session49_DialogAct.xml");
    }

    public void tagIt() {
	String trainingArffBatchFiltered = wekaArffPath + "training_batch.arff";
	String testingArffBatchFiltered = wekaArffPath + "testing_batch.arff";
	String wekaResults = wekaResultsPath + "wekaClassifierName.result";
	weka_classify_ = new edu.albany.ils.dsarmd0200.cuetag.weka/*.ds_featuresHash*/.WekaClassify(trainingArff, testingArff,
					  trainingArffBatchFiltered, testingArffBatchFiltered, wekaResults, wekaClassifierName);
	weka_classify_.classify();
	results_ = new TaggingResults(wekaResults, utts_, "dialog_act" + Settings.getValue("tagNum"));
	utts_ = results_.getTaggingResults();
	//System.out.println("utts: " + utts_);
	//results_.writeTaggingXml("/home/gjiao/Desktop/Sep1/session49_DialogAct.xml");
    }

    public void processFeatures() {
	ca = new CorpusAnalysisPriority(all_utts_, utts_, tr_utts_);
	ca.runAnalysis(tagType_);
	/*	
        ca.setFeatureMap(tagType_, "acknowledge", 3, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "offer-commit", 3, 1000, 0.325, 2); // 1
        ca.setFeatureMap(tagType_, "action-directive", 3, 1000, 0.4, 2); // 3
        ca.setFeatureMap(tagType_, "disagree-reject", 3, 1000, 0.325, 2); // 4
        ca.setFeatureMap(tagType_, "agree-accept", 3, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "conventional-opening", 3, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "conventional-closing", 3, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "confirmation-request", 3, 1000, 0.4, 2); //
        ca.setFeatureMap(tagType_, "information-request", 3, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "correct-misspelling", 3, 1000, 0.325, 2); // 2
        ca.setFeatureMap(tagType_, "response-non-answer", 3, 1000, 0.35, 2);
        ca.setFeatureMap(tagType_, "response-answer", 3, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "other-conventional-phrase", 3, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "signal-non-understanding", 3, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "assertion-opinion", 3, 1000, 0.325, 2); // 10
	*/
        ca.setFeatureMap(tagType_, "acknowledge", 5, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "offer-commit", 3, 1000, 0.325, 2); // 1
        ca.setFeatureMap(tagType_, "action-directive", 3, 1000, 0.4, 2); // 3
        ca.setFeatureMap(tagType_, "disagree-reject", 4, 1000, 0.325, 2); // 4
        ca.setFeatureMap(tagType_, "agree-accept", 6, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "conventional-opening", 4, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "conventional-closing", 4, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "confirmation-request", 3, 1000, 0.4, 2); //
        ca.setFeatureMap(tagType_, "information-request", 7, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "correct-misspelling", 3, 1000, 0.325, 2); // 2
        ca.setFeatureMap(tagType_, "response-non-answer", 3, 1000, 0.35, 2);
        ca.setFeatureMap(tagType_, "response-answer", 4, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "other-conventional-phrase", 4, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "signal-non-understanding", 2, 1000, 0.325, 2);
        ca.setFeatureMap(tagType_, "assertion-opinion", 10, 1000, 0.325, 2); // 10
	/*
	ca.setFeatureMap(tagType_, "acknowledge", 1, 1000, 0, 1);
        ca.setFeatureMap(tagType_, "offer-commit", 1, 1000, 0, 1); // 1
        ca.setFeatureMap(tagType_, "action-directive", 1, 1000, 0, 1); // 3
        ca.setFeatureMap(tagType_, "disagree-reject", 1, 1000, 0, 1); // 4
        ca.setFeatureMap(tagType_, "agree-accept", 1, 1000, 0, 1);
        ca.setFeatureMap(tagType_, "conventional-opening", 1, 1000, 0, 1);
        ca.setFeatureMap(tagType_, "conventional-closing", 1, 1000, 0, 1);
        ca.setFeatureMap(tagType_, "confirmation-request", 1, 1000, 0, 1); //
        ca.setFeatureMap(tagType_, "information-request", 1, 1000, 0, 1);
        ca.setFeatureMap(tagType_, "correct-misspelling", 1, 1000, 0, 1); // 2
        ca.setFeatureMap(tagType_, "response-non-answer", 1, 1000, 0, 1);
        ca.setFeatureMap(tagType_, "response-answer", 1, 1000, 0, 1);
        ca.setFeatureMap(tagType_, "other-conventional-phrase", 1, 1000, 0, 1);
        ca.setFeatureMap(tagType_, "signal-non-understanding", 1, 1000, 0, 1);
        ca.setFeatureMap(tagType_, "assertion-opinion", 1, 1000, 0, 1); // 10
	*/
	featuresMap_ = ca.getFeaturesMap();
	allFeatures_ = ca.getAllFeatures();    
    }

    /*******************************   Attributes  **************************/
    private ArrayList<Utterance> utts_ = null;
    private ArrayList<Utterance> all_utts_ = null;
    private ArrayList<Utterance> tr_utts_ = null;
    private String wekaArffPath = Settings.getValue("wekaArffPath");
    private String wekaResultsPath = Settings.getValue("wekaResultsPath");
    private String wekaClassifierName = Settings.getValue("wekaClassifierName");
    private DialogActArffGen da_arff_gen_ = null;
    private String trainingArff = null;
    private String testingArff = null;
    private edu.albany.ils.dsarmd0200.cuetag.weka/*.ds_featuresHash*/.WekaClassify weka_classify_ = null;
    private edu.albany.ils.dsarmd0200.cuetag.weka.WekaClassify weka_classify3_ = null;
    private TaggingResults results_ = null;
    private HashMap parts_ = null;
    private String tagType_ = null;
    private HashMap<String, ArrayList> featuresMap_ = null;
    private ArrayList<String> allFeatures_ = null;
    private CorpusAnalysisPriority ca = null;
    final static String nonBlank = Settings.getValue("nonBlank");
}
