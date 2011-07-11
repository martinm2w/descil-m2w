package edu.albany.ils.dsarmd0200.lu;


/**
 * @file: CommunicationType.java
 * @author: Ting Liu
 * This file is used to create the Communication type of the current utterance
 */

import edu.albany.ils.dsarmd0200.cuetag.weka.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.util.*;
import java.util.*;


public class CommunicationType extends Opinion{
    
    public CommunicationType(ArrayList all_utts_,
			     ArrayList tr_utts_,
			     ArrayList<Utterance> utts_,
			     HashMap parts,
			     Wordnet wn) {
	super();
	this.all_utts_ = all_utts_;
	this.tr_utts_ = tr_utts_;
	this.utts_ = utts_;
	parts_ = parts;
	wn_ = wn;
	prepareForTraining();
    }

    /*******************************get information**************************/


    /*******************************set information**************************/
    public void prepareForTraining() {
	com_arff_gen_ = new CommActArffGen(all_utts_, tr_utts_, utts_, parts_, wn_);
	trainingArff = wekaArffPath + com_arff_gen_.foldName + "_training.arff";
	testingArff = wekaArffPath + com_arff_gen_.foldName + "_testing.arff";
	com_arff_gen_.generateArffFile(trainingArff, testingArff);
    }

    public void tagIt() {
	String trainingArffBatchFiltered = wekaArffPath + com_arff_gen_.foldName + "_training_batch.arff";
	String testingArffBatchFiltered = wekaArffPath + com_arff_gen_.foldName + "_testing_batch.arff";
	String wekaResults = wekaResultsPath + com_arff_gen_.foldName + "_wekaClassifierName.result";
	weka_classify_ = new WekaClassify(trainingArff, testingArff,
					  trainingArffBatchFiltered, testingArffBatchFiltered, wekaResults, wekaClassifierName);
	weka_classify_.classify();
	results_ = new TaggingResults(wekaResults, utts_, "comm_act");
	utts_ = results_.getTaggingResults();
	//results_.writeTaggingXml("/home/gjiao/Desktop/Sep1/session49_CommAct.xml");
    }

    /*******************************   Attributes  **************************/
    private ArrayList<Utterance> utts_ = null;
    private ArrayList all_utts_ = null;
    private ArrayList tr_utts_ = null;
    private String wekaArffPath = Settings.getValue("wekaArffPath");
    private String wekaResultsPath = Settings.getValue("wekaResultsPath");
    private String wekaClassifierName = Settings.getValue("wekaClassifierName");
    private CommActArffGen com_arff_gen_ = null;
    private String trainingArff = null;
    private String testingArff = null;
    private WekaClassify weka_classify_ = null;
    private TaggingResults results_ = null;
    private HashMap parts_ = null;
    private Wordnet wn_ = null;
}
