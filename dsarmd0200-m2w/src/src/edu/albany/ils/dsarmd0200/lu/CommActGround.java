package edu.albany.ils.dsarmd0200.lu;

/**
 * @file: CommActGround.java
 * @author: pzongo
 * This file is used to create the Communicative Action Ground
 */

import edu.albany.ils.dsarmd0200.util.Queries;
import edu.albany.ils.dsarmd0200.util.Ngram;
import edu.albany.ils.dsarmd0200.util.Ngrams;
import edu.albany.ils.dsarmd0200.util.TurnBundle;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.util.StanfordPOSTagger;
import java.util.ArrayList;
import java.util.Collections;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class CommActGround{
    private static PrintWriter pw = null;//new PrintWriter(System.out,true);
    private static final int SECONDS_PER_HOUR =3600;
    private static final int SECONDS_PER_MINUTE =60;
    private static final int HOURS_PER_DAY =12;
    private static final String CONTINUATION_OF="continuation-of";
    private static final String ADDRESSED_TO="addressed-to";
    private static final String RESPONSE_TO="response-to";
    private static final String taggerRun="Switchboard 1";  //dialog act tagger run
    private static final String START="<START>";
    private static final String END="<END>";
    private static final int MAX_LEN=4;  //maximum ngram length
    private static final String CONTINUATION_OF_FILE="contNgrams.ser";
    private static final String ADDRESSED_TO_FILE="addrNgrams.ser";
    private static final String RESPONSE_TO_FILE="respNgrams.ser";


    private static int TEST_DISC=2;


    public static void main(String[] args){
	//Queries.initDatabase();
        try {
            pw = new PrintWriter(new FileWriter("makeThisFile.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //generateGround();
        //statsTest();
        
	//System.out.println("wtPow\tPercent\tPrecision(R)\tRecall(R)\tPrecision(A)\tRecall(A)\tPrecision(C)\tRecall(C)");

	//System.out.println("discID\twtPow\tPercent\tPrecision(R)\tRecall(R)\tPrecision(A)\tRecall(A)");
	ArrayList<Integer> discIDs=Queries.getAnnotatedDiscussions();
        for (int i = 0; i < discIDs.size(); i++) {
            System.out.print(discIDs.get(i) + "\t");
            TEST_DISC = discIDs.get(i);
            compareZipfs();
        }

	//testZipf();
    }

//converts a string into a sequence of part of speech tags
    public static String textToPosString(String inputText) {
        String taggedStr,posString="";
        String[] segString; //stores an ordered set of word,pos pairs
                            //i.e. {"The/DT","cat/NN","ran/VBD","up/RP","the/DT","stairs/NNS"}
        String[] piece;// stores a word,pos pair i.e {"cat","NN"}
        try {
            taggedStr = StanfordPOSTagger.mt.tagString(inputText);
        } catch (Exception e) {
            taggedStr = "";
        }
        segString = taggedStr.split(" +");

        for (int i = 0; i < segString.length; i++) {
            piece = segString[i].split("/");
            if (piece.length == 2) {
                posString += segString[i]+" ";//piece[1] + " ";
            }else{
                posString += "XX ";
            }
        }

        return posString;
    }



    private static void testZipf(){
	String commAct=CONTINUATION_OF;//rs.getString("c.comm_act");
	String utt = "one two three three five";//rs.getString("Content");
	String trimmedUtt = replacePunc(utt).trim().toLowerCase();
	ArrayList<Ngram> ngramList=new ArrayList<Ngram>();
	Ngram myGram=new Ngram(); 
	String [] dummyUtt = trimmedUtt.split(" +");  //split on one or more spaces

	String [] segmentedUtt = new String[dummyUtt.length+2];
	segmentedUtt[0]=START;
	for(int i=0;i<dummyUtt.length;i++){
	    segmentedUtt[i+1]=dummyUtt[i];
	}
	segmentedUtt[dummyUtt.length+1]=END;
	//process each word

	for(int i=0;i<MAX_LEN;i++){
	    for(int j=0;j<segmentedUtt.length-i;j++){
		myGram=new Ngram(); 
		for(int k=0;k<i+1;k++){
		    myGram.addGram(segmentedUtt[j+k]);
		}
		ngramList.add(myGram);	
	    }
	}

	//add i
	//add i-1 and i
	//add i-2, i-1, and i
	//add i-3,i-2,i-1, and i

	for(int i=0;i<ngramList.size();i++){
	    Ngram curGram = ngramList.get(i);
	    ArrayList<String> ngramText=curGram.getNgram();
	    for(int j=0;j<ngramText.size();j++){
		System.out.print(ngramText.get(j)+",");
	    }
	    System.out.println();
	}

	
	//if (commAct.equals(CONTINUATION_OF)) {
	//    contGrams.add(myGram);  //add this ngram if it doesnt exist
	//} else if (commAct.equals(ADDRESSED_TO)) {
	//    addrGrams.add(myGram);  //add this ngram if it doesnt exist
	//}


    }

    public static Ngrams contGrams = new Ngrams();
    public static Ngrams respGrams = new Ngrams();
    public static Ngrams addrGrams = new Ngrams();
    public static void training(ArrayList tr_utts_) {
	contGrams = new Ngrams();
	respGrams = new Ngrams();
	addrGrams = new Ngrams();
        String commAct, linkTo;
        String utt;
        ObjectOutputStream out;
        ObjectInputStream in;
        //System.out.println(sqlQuery);

        try {
            //update the Ngrams files if the appropriate ones don't exist
	    for (int j = 0; j < tr_utts_.size(); j++)  {
		Utterance utt_ = (Utterance)tr_utts_.get(j);
		commAct = utt_.getCommActType();
		
		utt = utt_.getUtterance();
		utt = textToPosString(utt);
		ArrayList<Ngram> ngramList = utteranceToNgramArray(utt);
		
		//System.out.println(commAct);
		if (commAct == null) {
		    //do nothing
		} else if (commAct.equals(RESPONSE_TO)) {
		    for (int i = 0; i < ngramList.size(); i++) {
			//Collections.sort(respGrams.toArrayList(),Collections.reverseOrder());
			respGrams.addNgram(ngramList.get(i));  //add all ngrams if they don't exist
		    }
		} else if (commAct.equals(ADDRESSED_TO)) {
		    for (int i = 0; i < ngramList.size(); i++) {
			//Collections.sort(addrGrams.toArrayList(),Collections.reverseOrder());
			addrGrams.addNgram(ngramList.get(i));  //add all ngrams if they don't exist
		    }
		} else if (commAct.equals(CONTINUATION_OF)) {
		    for (int i = 0; i < ngramList.size(); i++) {
			//Collections.sort(addrGrams.toArrayList(),Collections.reverseOrder());
			contGrams.addNgram(ngramList.get(i));  //add all ngrams if they don't exist
		    }
		}
	    }
	    rankGrams(respGrams, false);
	    rankGrams(addrGrams, false);
	    rankGrams(contGrams, false);
	    out = new ObjectOutputStream(new FileOutputStream(RESPONSE_TO_FILE));
	    out.writeObject(respGrams);
	    out.close();
	    out = new ObjectOutputStream(new FileOutputStream(ADDRESSED_TO_FILE));
	    out.writeObject(addrGrams);
	    out.close();
	    out = new ObjectOutputStream(new FileOutputStream(CONTINUATION_OF_FILE));
	    out.writeObject(contGrams);
	    out.close();
	} catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String tagIt(Utterance utt_) {
	String utt = utt_.getUtterance();
	String cleanUtt = utt.trim().toLowerCase();
	int respIndex, addrIndex, contIndex;
	double respValence = 1;  //value that indicates degree to which the statement is a response-to
	//lowest value is 1, highest value is not defined
	double contValence = 1;  //see respValence
	double addrValence = 1;  //see respValence
	String autoCommAct = "", autoLinkTo = "";  //automatic tagger's communicative action ancd link_to
	//the following 3 variables weight the communicative actions
	double RESP_WT = 1;//1;
	double ADDR_WT = 1;//32652.0/32020;
	double CONT_WT = 1;//32652.0/18189;
	Ngram respNgram, addrNgram, contNgram;  //Ngram to be inserted into an Ngrams object
	//Note: only 1 generic Ngram object is necessary
	double respFreq, addrFreq, contFreq;  //frequency of the associated Ngram
	//Note: onl 1 generic frequency is necessary
	ArrayList<Ngram> ngramList = utteranceToNgramArray(utt);  //contains every n-gram of the current utterance
	int cutoff = 1;
	int wtPow = 10;
	
	//inserts each Ngram of the utterance into the appropriate Ngrams object
	for (int i = 0; i < ngramList.size(); i++) {
	    respIndex = respGrams.getIndex(ngramList.get(i));
	    addrIndex = addrGrams.getIndex(ngramList.get(i));
	    contIndex = contGrams.getIndex(ngramList.get(i));
	    if (respIndex < 0 && addrIndex < 0 && contIndex < 0) {
		continue;
	    } else if (respIndex < 0 && addrIndex < 0) {
		contNgram = contGrams.getNgram(contIndex);
		contFreq = contNgram.getCount() * CONT_WT;
		contValence *= contFreq * Math.pow(contNgram.getSize(), wtPow);
	    } else if (respIndex < 0 && contIndex < 0) {
		addrNgram = addrGrams.getNgram(addrIndex);
		addrFreq = addrNgram.getCount() * ADDR_WT;
		addrValence *= addrFreq * Math.pow(addrNgram.getSize(), wtPow);
	    } else if (addrIndex < 0 && contIndex < 0) {
		respNgram = respGrams.getNgram(respIndex);
		respFreq = respNgram.getCount() * RESP_WT;
		respValence *= respFreq * Math.pow(respNgram.getSize(), wtPow);
	    } else if (respIndex < 0) {
		contNgram = contGrams.getNgram(contIndex);
		addrNgram = addrGrams.getNgram(addrIndex);
		
		contFreq = contNgram.getCount() * CONT_WT;
		addrFreq = addrNgram.getCount() * ADDR_WT;
		
		contValence *= contFreq * Math.pow(contNgram.getSize(), wtPow);
		addrValence *= addrFreq * Math.pow(addrNgram.getSize(), wtPow);
	    } else if (contIndex < 0) {
		respNgram = respGrams.getNgram(respIndex);
		addrNgram = addrGrams.getNgram(addrIndex);
		
		respFreq = respNgram.getCount() * RESP_WT;
		addrFreq = addrNgram.getCount() * ADDR_WT;
		
		respValence *= respFreq * Math.pow(respNgram.getSize(), wtPow);
		addrValence *= addrFreq * Math.pow(addrNgram.getSize(), wtPow);
	    } else if (addrIndex < 0) {
		contNgram = contGrams.getNgram(contIndex);
		respNgram = respGrams.getNgram(respIndex);
		
		contFreq = contNgram.getCount() * CONT_WT;
		respFreq = respNgram.getCount() * RESP_WT;
		
		contValence *= contFreq * Math.pow(contNgram.getSize(), wtPow);
		respValence *= respFreq * Math.pow(respNgram.getSize(), wtPow);
	    } else {
		contNgram = contGrams.getNgram(contIndex);
		respNgram = respGrams.getNgram(respIndex);
		addrNgram = addrGrams.getNgram(addrIndex);
		
		contFreq = contNgram.getCount() * CONT_WT;
		respFreq = respNgram.getCount() * RESP_WT;
		addrFreq = addrNgram.getCount() * ADDR_WT;
		
		contValence *= contFreq * Math.pow(contNgram.getSize(), wtPow);
		respValence *= respFreq * Math.pow(respNgram.getSize(), wtPow);
		addrValence *= addrFreq * Math.pow(addrNgram.getSize(), wtPow);
	    }
	}
	if ((contValence / respValence) > cutoff && (contValence / addrValence) > cutoff) {
	    autoCommAct = CONTINUATION_OF;
	} else if ((respValence / contValence) > cutoff && (respValence / addrValence) > cutoff) {
	    autoCommAct = RESPONSE_TO;
	} else if ((addrValence / respValence) > cutoff && (addrValence / contValence) > cutoff) {
	    autoCommAct = ADDRESSED_TO;
	    
	} else {
	    autoCommAct = RESPONSE_TO;
	}
	return autoCommAct;

    }

    private static void compareZipfs() {
        String sqlQuery;
        ResultSet rs;
        Ngrams contGrams = new Ngrams();
        Ngrams respGrams = new Ngrams();
        Ngrams addrGrams = new Ngrams();
        String commAct, linkTo;
        String utt;
        ObjectOutputStream out;
        ObjectInputStream in;
        //System.out.println(sqlQuery);

        try {
            //update the Ngrams files if the appropriate ones don't exist
            if (true) {//if (!respFile.exists() || !addrFile.exists() || !contFile.exists()) {

                sqlQuery = "SELECT DISTINCT Content,c.comm_act FROM Utterance" +
                        " INNER JOIN Annotations AS a ON Utterance.ID=a.UtteranceID" +
                        " LEFT JOIN CommLinkGround AS c ON Utterance.ID=c.UtteranceID" +
                        " WHERE NOT c.comm_act IS NULL AND DiscID <>" + TEST_DISC + ";";
                rs = Queries.getResult(sqlQuery);
                //process a single utterance
                while (rs.next()) {
                    //System.out.println(count++);
                    commAct = rs.getString("c.comm_act");
                    utt = rs.getString("Content");
                    utt = textToPosString(utt);
                    ArrayList<Ngram> ngramList = utteranceToNgramArray(utt);

                    //System.out.println(commAct);
                    if (commAct == null) {
                        //do nothing
                    } else if (commAct.equals(RESPONSE_TO)) {
                        for (int i = 0; i < ngramList.size(); i++) {
                            //Collections.sort(respGrams.toArrayList(),Collections.reverseOrder());
                            respGrams.addNgram(ngramList.get(i));  //add all ngrams if they don't exist
                        }
                    } else if (commAct.equals(ADDRESSED_TO)) {
                        for (int i = 0; i < ngramList.size(); i++) {
                            //Collections.sort(addrGrams.toArrayList(),Collections.reverseOrder());
                            addrGrams.addNgram(ngramList.get(i));  //add all ngrams if they don't exist
                        }
                    } else if (commAct.equals(CONTINUATION_OF)) {
                        for (int i = 0; i < ngramList.size(); i++) {
                            //Collections.sort(addrGrams.toArrayList(),Collections.reverseOrder());
                            contGrams.addNgram(ngramList.get(i));  //add all ngrams if they don't exist
                        }
                    }
                }
                rankGrams(respGrams, false);
                rankGrams(addrGrams, false);
                rankGrams(contGrams, false);
                out = new ObjectOutputStream(new FileOutputStream(RESPONSE_TO_FILE));
                out.writeObject(respGrams);
                out.close();
                out = new ObjectOutputStream(new FileOutputStream(ADDRESSED_TO_FILE));
                out.writeObject(addrGrams);
                out.close();
                out = new ObjectOutputStream(new FileOutputStream(CONTINUATION_OF_FILE));
                out.writeObject(contGrams);
                out.close();
                rs.close();
            } else {
                //load Ngrams objects into memory if the proper ones exist
                in = new ObjectInputStream(new FileInputStream(RESPONSE_TO_FILE));
                respGrams = (Ngrams) in.readObject();
                in.close();
                in = new ObjectInputStream(new FileInputStream(ADDRESSED_TO_FILE));
                addrGrams = (Ngrams) in.readObject();
                in.close();
                in = new ObjectInputStream(new FileInputStream(CONTINUATION_OF_FILE));
                contGrams = (Ngrams) in.readObject();
                in.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //at this point the Ngram arrays are correctly set up(ranked and counted) and ordered
        try {


            int WT_MIN = 0;//
            int WT_INC = 2;//
            int WT_MAX = 0;//largest exponential weight for n-gram length
            int CUTOFF_MIN = 1; //
            int CUTOFF_INC = 100; //
            int CUTOFF_MAX = 1; //largest cutoff
            String lastAddressSpeaker = "";// last person to utter an address-to
            String lastAddressTurn = "";// turn of last person to utter an address-to
            String lastTurn;
            String curSpeaker = "";
            ArrayList<String> speakerNames = Queries.getSpeakerNames(TEST_DISC);
            for (int cutoff = CUTOFF_MIN; cutoff <= CUTOFF_MAX; cutoff += CUTOFF_INC) {
                System.out.println("cutoff,max==" + cutoff + "," + CUTOFF_MAX);

                try {
                    pw = new PrintWriter(new FileWriter("actAndLink.txt"));
                    pw.println("Disc=" + Queries.getDiscussionDate(TEST_DISC) + "\n" + "wtPow\tPercent\tPrecision(R)\tRecall(R)\tPrecision(A)\tRecall(A)\tPrecision(C)\tRecall(C)");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (int wtPow = WT_MIN; wtPow <= WT_MAX; wtPow += WT_INC) {
                    System.out.println("\twtPow,max==" + wtPow + "," + WT_MAX);
                    pw.print(wtPow + "\t");

                    sqlQuery = "SELECT UtteranceID,Content,Name,TurnNo FROM Utterance" +
                            " INNER JOIN CommLinkGround ON UtteranceID=Utterance.ID" +
                            " INNER JOIN Speaker ON SpeakerID=Speaker.ID" +
                            " WHERE DiscID=" + TEST_DISC + ";";

                    rs = Queries.getResult(sqlQuery);

                    while (rs.next()) {
                        utt = rs.getString("Content");
                        String cleanUtt = utt.trim().toLowerCase();
                        int uttID = rs.getInt("UtteranceID");
                        curSpeaker = rs.getString("Name");
                        String turnNum = rs.getString("TurnNo");
                        int respIndex, addrIndex, contIndex;
                        double respValence = 1;  //value that indicates degree to which the statement is a response-to
                        //lowest value is 1, highest value is not defined
                        double contValence = 1;  //see respValence
                        double addrValence = 1;  //see respValence
                        String autoCommAct = "", autoLinkTo = "";  //automatic tagger's communicative action ancd link_to
                        //the following 3 variables weight the communicative actions
                        double RESP_WT = 1;//1;
                        double ADDR_WT = 1;//32652.0/32020;
                        double CONT_WT = 1;//32652.0/18189;
                        Ngram respNgram, addrNgram, contNgram;  //Ngram to be inserted into an Ngrams object
                        //Note: only 1 generic Ngram object is necessary
                        double respFreq, addrFreq, contFreq;  //frequency of the associated Ngram
                        //Note: onl 1 generic frequency is necessary
                        ArrayList<Ngram> ngramList = utteranceToNgramArray(utt);  //contains every n-gram of the current utterance

                        //inserts each Ngram of the utterance into the appropriate Ngrams object
                        for (int i = 0; i < ngramList.size(); i++) {
                            respIndex = respGrams.getIndex(ngramList.get(i));
                            addrIndex = addrGrams.getIndex(ngramList.get(i));
                            contIndex = contGrams.getIndex(ngramList.get(i));
                            if (respIndex < 0 && addrIndex < 0 && contIndex < 0) {
                                continue;
                            } else if (respIndex < 0 && addrIndex < 0) {
                                contNgram = contGrams.getNgram(contIndex);
                                contFreq = contNgram.getCount() * CONT_WT;
                                contValence *= contFreq * Math.pow(contNgram.getSize(), wtPow);
                            } else if (respIndex < 0 && contIndex < 0) {
                                addrNgram = addrGrams.getNgram(addrIndex);
                                addrFreq = addrNgram.getCount() * ADDR_WT;
                                addrValence *= addrFreq * Math.pow(addrNgram.getSize(), wtPow);
                            } else if (addrIndex < 0 && contIndex < 0) {
                                respNgram = respGrams.getNgram(respIndex);
                                respFreq = respNgram.getCount() * RESP_WT;
                                respValence *= respFreq * Math.pow(respNgram.getSize(), wtPow);
                            } else if (respIndex < 0) {
                                contNgram = contGrams.getNgram(contIndex);
                                addrNgram = addrGrams.getNgram(addrIndex);

                                contFreq = contNgram.getCount() * CONT_WT;
                                addrFreq = addrNgram.getCount() * ADDR_WT;

                                contValence *= contFreq * Math.pow(contNgram.getSize(), wtPow);
                                addrValence *= addrFreq * Math.pow(addrNgram.getSize(), wtPow);
                            } else if (contIndex < 0) {
                                respNgram = respGrams.getNgram(respIndex);
                                addrNgram = addrGrams.getNgram(addrIndex);

                                respFreq = respNgram.getCount() * RESP_WT;
                                addrFreq = addrNgram.getCount() * ADDR_WT;

                                respValence *= respFreq * Math.pow(respNgram.getSize(), wtPow);
                                addrValence *= addrFreq * Math.pow(addrNgram.getSize(), wtPow);
                            } else if (addrIndex < 0) {
                                contNgram = contGrams.getNgram(contIndex);
                                respNgram = respGrams.getNgram(respIndex);

                                contFreq = contNgram.getCount() * CONT_WT;
                                respFreq = respNgram.getCount() * RESP_WT;

                                contValence *= contFreq * Math.pow(contNgram.getSize(), wtPow);
                                respValence *= respFreq * Math.pow(respNgram.getSize(), wtPow);
                            } else {
                                contNgram = contGrams.getNgram(contIndex);
                                respNgram = respGrams.getNgram(respIndex);
                                addrNgram = addrGrams.getNgram(addrIndex);

                                contFreq = contNgram.getCount() * CONT_WT;
                                respFreq = respNgram.getCount() * RESP_WT;
                                addrFreq = addrNgram.getCount() * ADDR_WT;

                                contValence *= contFreq * Math.pow(contNgram.getSize(), wtPow);
                                respValence *= respFreq * Math.pow(respNgram.getSize(), wtPow);
                                addrValence *= addrFreq * Math.pow(addrNgram.getSize(), wtPow);
                            }
                        }
                        if ((contValence / respValence) > cutoff && (contValence / addrValence) > cutoff) {
                            autoCommAct = CONTINUATION_OF;
                        } else if ((respValence / contValence) > cutoff && (respValence / addrValence) > cutoff) {
                            autoCommAct = RESPONSE_TO;
                        } else if ((addrValence / respValence) > cutoff && (addrValence / contValence) > cutoff) {
                            autoCommAct = ADDRESSED_TO;

                        } else {
                            autoCommAct = RESPONSE_TO;
                        }




                        if (autoCommAct.equals(CONTINUATION_OF)) {
                            //self referential, usually utterer's last turn, 
                            //note:data shows it's usually more recent(timewise) than a response-to

                            autoLinkTo = curSpeaker;
                            lastTurn = getLastTurn(curSpeaker, turnNum, TEST_DISC);
                            if (!lastTurn.equals("")) {
                                autoLinkTo += ":" + lastTurn;
                            }
                        } else if (autoCommAct.equals(RESPONSE_TO)) {
                            //refers to another, we assume it's after an addressed to was sent
                            autoLinkTo = lastAddressSpeaker;
                            if (!lastAddressTurn.equals("")) {
                                autoLinkTo += ":" + lastAddressTurn;
                            }
                        } else if (autoCommAct.equals(ADDRESSED_TO)) {
                            //doesn't require a turn number, only a recipient, may be all users
                            int refCount = 0;  //amount of speakers that were referenced in the utterance
                            lastAddressSpeaker = curSpeaker;
                            lastAddressTurn = turnNum;
                            autoLinkTo = "all users";
                            //cheat and guess by speaker name occuring in utterance
                            for (int i = 0; i < speakerNames.size(); i++) {
                                if (cleanUtt.matches(".*" + speakerNames.get(i) + ".*")) {
                                    refCount++;
                                    autoLinkTo = speakerNames.get(i);
                                }
                            }
                            if (refCount != 1) {
                                autoLinkTo = "all users";
                            }
                        }

                        sqlQuery = "UPDATE CommLinkGround" +
                                " SET auto_comm_act='" + autoCommAct + "'," +
                                " auto_link_to='" + autoLinkTo + "'" +
                                " WHERE UtteranceID=" + uttID + ";";
                        Queries.runQuery(sqlQuery);
                    }
                    //System.out.print(wtPow+"\t");
                    System.out.print(CONTINUATION_OF + "\t");
                    autoEvalLinks(CONTINUATION_OF);
                    System.out.print(RESPONSE_TO + "\t");
                    autoEvalLinks(RESPONSE_TO);
                    System.out.print(ADDRESSED_TO + "\t");
                    autoEvalLinks(ADDRESSED_TO);
                    //System.out.println();
                    autoEval();
                    pw.flush();
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    //retrieves the last turn number that the user had
    private static String getLastTurn(String speaker, String beginTurnNum, int discID) {
        ResultSet rs;
        String myTurn,prevTurn="";
        String sqlQuery = "SELECT UtteranceID,Content,Name,TurnNo FROM Utterance" +
                " INNER JOIN CommLinkGround ON UtteranceID=Utterance.ID" +
                " INNER JOIN Speaker ON SpeakerID=Speaker.ID" +
                " WHERE DiscID=" + discID + " AND Name='" + speaker + "' ORDER BY TurnNo ASC;";

        try{
            rs=Queries.getResult(sqlQuery);
            while(rs.next()){
                myTurn=rs.getString("TurnNo");
                if(myTurn.equals(beginTurnNum)){
                    return prevTurn;
                }
                prevTurn=myTurn;
            }

        }catch(SQLException sqle){sqle.printStackTrace();}
        return "";
    }


    private static void autoEvalLinks(String commAct){
        double precision=0,recall=0;
	int correct=0;
        int total=0;
        ResultSet rs;
	String sqlQuery;
	try{
	    sqlQuery="SELECT Name,comm_act,link_to,auto_comm_act,auto_link_to"+
                    " FROM CommLinkGround INNER JOIN Utterance ON Utterance.ID=UtteranceID"+
                    " INNER JOIN Speaker ON SpeakerID=Speaker.ID"+
                    " WHERE comm_act='"+commAct+"' AND auto_comm_act='"+commAct+"'"+
                    " AND link_to=auto_link_to AND DiscID="+TEST_DISC+";";
	    rs=Queries.getResult(sqlQuery);
	    rs.last();
	    correct=rs.getRow();
	    
	    sqlQuery="SELECT Name,comm_act,link_to,auto_comm_act,auto_link_to"+
                    " FROM CommLinkGround INNER JOIN Utterance ON Utterance.ID=UtteranceID"+
                    " INNER JOIN Speaker ON SpeakerID=Speaker.ID"+
                    " WHERE comm_act='"+commAct+"' AND auto_comm_act='"+commAct+"'"+
                    " AND DiscID="+TEST_DISC+";";
	    rs=Queries.getResult(sqlQuery);
	    rs.last();
	    total=rs.getRow();

	}catch(SQLException sqle){sqle.printStackTrace();}
	System.out.println("correct/total="+correct+"/"+total+"="+round(100.*correct/total,2)+"%\t");
        
    }

    private static void autoEval(){
	double total=0,matches=0;
	String sqlQuery="SELECT c.* FROM CommLinkGround AS c"+
	    " INNER JOIN Utterance AS u ON u.ID=c.UtteranceID"+
	    " WHERE DiscID="+TEST_DISC+" AND NOT comm_act='' AND comm_act=auto_comm_act;";
	ResultSet rs;
	try{
	    rs=Queries.getResult(sqlQuery);
	    rs.last();
	    matches=rs.getRow();
	    
	    sqlQuery="SELECT c.* FROM CommLinkGround AS c"+
		" INNER JOIN Utterance AS u ON u.ID=c.UtteranceID"+
		" WHERE DiscID="+TEST_DISC+" AND NOT comm_act='';";
	    rs=Queries.getResult(sqlQuery);
	    rs.last();
	    total=rs.getRow();
	}catch(SQLException sqle){sqle.printStackTrace();}
	pw.print(round(100*matches/total,2)+"%\t");
	precisionAndRecall(RESPONSE_TO);
	precisionAndRecall(ADDRESSED_TO);
	precisionAndRecall(CONTINUATION_OF);
	pw.println();
    }

    private static void precisionAndRecall(String commAct){
	double precision=0,recall=0;
	int correct=0;
	int labeled=0;
	int actual=0;
	ResultSet rs;
	String sqlQuery;
	try{
	    sqlQuery="SELECT c.* FROM CommLinkGround AS c"+
		" INNER JOIN Utterance AS u ON u.ID=c.UtteranceID"+
		" WHERE DiscID="+TEST_DISC+" AND NOT comm_act=''"+
		" AND comm_act='"+commAct+
		"' AND auto_comm_act='"+commAct+"';";
	    rs=Queries.getResult(sqlQuery);
	    rs.last();
	    correct=rs.getRow();
	    
	    sqlQuery="SELECT c.* FROM CommLinkGround AS c"+
		" INNER JOIN Utterance AS u ON u.ID=c.UtteranceID"+
		" WHERE DiscID="+TEST_DISC+" AND NOT comm_act=''"+
		" AND auto_comm_act='"+commAct+"';";
	    rs=Queries.getResult(sqlQuery);
	    rs.last();
	    labeled=rs.getRow();

	    sqlQuery="SELECT c.* FROM CommLinkGround AS c"+
		" INNER JOIN Utterance AS u ON u.ID=c.UtteranceID"+
		" WHERE DiscID="+TEST_DISC+" AND NOT comm_act=''"+
		" AND comm_act='"+commAct+"';";
	    rs=Queries.getResult(sqlQuery);
	    rs.last();
	    actual=rs.getRow();

	    precision=1.0*correct/labeled;
	    recall=1.0*correct/actual;
	}catch(SQLException sqle){sqle.printStackTrace();}
	pw.print(round(100*precision,2)+"%\t");
	pw.print(round(100*recall,2)+"%\t");


    }    

    private static ArrayList<Ngram> utteranceToNgramArray(String utt){
	String trimmedUtt = replacePunc(utt).trim().toLowerCase();
	ArrayList<Ngram> ngramList=new ArrayList<Ngram>();
	Ngram myGram;
	String [] dummyUtt = trimmedUtt.split(" +");  //split on one or more spaces
	String [] segmentedUtt = new String[dummyUtt.length+2];  //utterance w/ <START> and <END> tags
	segmentedUtt[0]=START;
	for(int i=0;i<dummyUtt.length;i++){
	    segmentedUtt[i+1]=dummyUtt[i];
	}
	segmentedUtt[dummyUtt.length+1]=END;
	//process each word
	
	for(int i=0;i<MAX_LEN;i++){
	    for(int j=0;j<segmentedUtt.length-i;j++){
		myGram=new Ngram(); 
		for(int k=0;k<i+1;k++){
		    myGram.addGram(segmentedUtt[j+k]);
		}
		ngramList.add(myGram);	
	    }
	}
	return ngramList;
    } 

    private static Ngrams rankGrams(Ngrams ngramList, boolean isUnique){
	int rank=1;
	int sameRankCount=0;
	Collections.sort(ngramList.toArrayList(),Collections.reverseOrder());
	for(int i=0;i<ngramList.getSize();i++){
	    if(i==0){
	    }else if(ngramList.getNgram(i).getCount()==
		     ngramList.getNgram(i-1).getCount()){
		if(!isUnique)
		    sameRankCount++;
	    }else{
		sameRankCount=0;
	    }
	    ngramList.getNgram(i).setRank(rank-sameRankCount);
	    rank++;
	}
	return ngramList;
	
    }

    private static void generateGround(){
	ArrayList<Integer> discIDs = Queries.getAnnotatedDiscussions();
	ResultSet rs;
	ArrayList<TurnBundle> turns;

	for(int i=0;i<discIDs.size();i++){
	     turns=new ArrayList<TurnBundle>();
	    String sqlQuery = "SELECT SpeakerID,UtteranceID,TimeStmp,TurnNo,Content,auto_dialog_act\n"+
		" FROM Utterance INNER JOIN DATagger ON Utterance.ID=UtteranceID\n" +
		" WHERE DiscID=" + discIDs.get(i) + " AND run='"+taggerRun+"';";
	    rs=Queries.getResult(sqlQuery);
	    pw.println("Discussion(ID)="+Queries.getDiscussionDate(discIDs.get(i))+"("+discIDs.get(i)+")");
	    try{
		while(rs.next()){
		    TurnBundle bundle=new TurnBundle();
		    bundle.setSpeakerID(rs.getInt("SpeakerID"));
		    bundle.setUtteranceID(rs.getInt("UtteranceID"));
		    bundle.setTime(rs.getString("TimeStmp"));
		    bundle.setTurn(rs.getString("TurnNo"));
		    bundle.setUtt(rs.getString("Content"));
                    bundle.setAutoDialogAct("auto_dialog_act");
		    bundle.setDiscID(discIDs.get(i));
		    turns.add(bundle);
		    //pw.println(bundle.getTime());
		}
		
	    }catch(SQLException sqle){sqle.printStackTrace();}
	    
	    turns=automateCommAct(turns,discIDs.get(i));
            evaluateAutoComm(turns);
	    pw.println("\n");
	}
	
	//String time1="3:00:00 PM";
	//String time2="2:59:59 PM";
	//pw.println(timeDiff(time1,time2));
    }

    private static void statsTest() {
        ArrayList<Integer> discIDs = Queries.getAnnotatedDiscussions();
        ResultSet rs;
        ArrayList<TurnBundle> turns= new ArrayList<TurnBundle>();
        ArrayList<Integer> annotatorIDs;
        String sqlAct ="comm_act";
	ArrayList<Integer> continuationOfTimeOffsets=new ArrayList<Integer>();
	int continuationOfSum=0;

	ArrayList<Integer> responseToTimeOffsets=new ArrayList<Integer>();
	int responseToSum=0;

	int initialTurnCapacity=700;

        for (int i = 0; i < discIDs.size(); i++) {
	    pw.println("disc=="+discIDs.get(i));
            turns = new ArrayList<TurnBundle>();
	    turns.ensureCapacity(initialTurnCapacity);
            annotatorIDs = Queries.getAnnotatorIDs(discIDs.get(i));
            String sqlQuery = "SELECT SpeakerID,TimeStmp,TurnNo,Content,a1.link_to,a2.link_to,a3.link_to,"+
                        "a1."+sqlAct+",a2."+sqlAct+",a3."+sqlAct+" FROM Utterance" +
                        " LEFT JOIN (SELECT UtteranceID,link_to,comm_act FROM Annotations WHERE AnnotatorID="+annotatorIDs.get(0)+")"+
                        " AS a1 ON Utterance.ID=a1.UtteranceID"+
                        " LEFT JOIN (SELECT UtteranceID,link_to,comm_act FROM Annotations WHERE AnnotatorID="+annotatorIDs.get(1)+")" +
                        " AS a2 ON Utterance.ID=a2.UtteranceID" +
                        " LEFT JOIN (SELECT UtteranceID,link_to,comm_act FROM Annotations WHERE AnnotatorID="+annotatorIDs.get(2)+")" +
                        " AS a3 ON Utterance.ID=a3.UtteranceID WHERE DiscID="+discIDs.get(i)+";";

            rs=Queries.getResult(sqlQuery);
	    try{
		while(rs.next()){
		    TurnBundle bundle=new TurnBundle();
		    bundle.setSpeakerID(rs.getInt("SpeakerID"));
		    bundle.setTime(rs.getString("TimeStmp"));
		    bundle.setTurn(rs.getString("TurnNo"));
		    bundle.setUtt(rs.getString("Content"));
                    bundle.setDiscID(discIDs.get(i));
                    bundle.addCommLink(rs.getString("a1."+sqlAct),rs.getString("a1.link_to"));
                    bundle.addCommLink(rs.getString("a2."+sqlAct),rs.getString("a2.link_to"));
                    bundle.addCommLink(rs.getString("a3."+sqlAct),rs.getString("a3.link_to"));


		    turns.add(bundle);

		    //pw.println(bundle.getTime());
		}
		
	    }catch(SQLException sqle){sqle.printStackTrace();}

	    for(int j=0;j<turns.size();j++){
		//int annot1=0;int annot2=1;int annot3=2;
		int annotSize=3;
                //get rest of annotators

		//do similar process for response to
		int commIndex=0;int linkIndex=1;
		TurnBundle turn=turns.get(j);
		TurnBundle linkedTurn;
                int timeOffset=-99;

                ArrayList<ArrayList<String>> commLinks = turn.getCommLinks();
                for (int annotIndex = 0; annotIndex < annotSize; annotIndex++) {
                    if (commLinks == null || commLinks.get(annotIndex) == null ||
                            commLinks.get(annotIndex).get(commIndex) == null) {
                        continue;
                    } else if (commLinks.get(annotIndex).get(commIndex).equals(CONTINUATION_OF)) {
                        linkedTurn = findTurnFromLinkTo(turns, commLinks.get(annotIndex).get(linkIndex));
                        if (linkedTurn == null) {
                            continue;
                        }
                        timeOffset = timeDiff(turn.getTime(), linkedTurn.getTime());
                        continuationOfTimeOffsets.add(timeOffset);
                        continuationOfSum += timeOffset;
                    }else if (commLinks.get(annotIndex).get(commIndex).equals(RESPONSE_TO)) {
                        linkedTurn = findTurnFromLinkTo(turns, commLinks.get(annotIndex).get(linkIndex));
                        if (linkedTurn == null) {
                            continue;
                        }
                        timeOffset = timeDiff(turn.getTime(), linkedTurn.getTime());

                        responseToTimeOffsets.add(timeOffset);
                        responseToSum += timeOffset;
                    }

                }
                

            }


        }

	//System.out.println(continuationOfSum/continuationOfTimeOffsets.size());
/*
        pw.println("cont------------------------------");
        for(int i=0;i<continuationOfTimeOffsets.size();i++){
            pw.println(continuationOfTimeOffsets.get(i));
        }
	pw.println();
	//System.out.println(responseToSum/responseToTimeOffsets.size());
        pw.println("resp------------------------------");
        for(int i=0;i<responseToTimeOffsets.size();i++){
            pw.println(responseToTimeOffsets.get(i));
        }
*/






    }

    private static void evaluateAutoComm(ArrayList<TurnBundle> turns){
        int count=0;
        for(int i=0;i<turns.size();i++){
            TurnBundle curTurn=turns.get(i);
            String autoCommAct,autoLinkTo;
            String groundCommAct="",groundLinkTo="";
            ResultSet rs;
            String sqlQuery="SELECT comm_act,link_to FROM CommLinkGround"+
                    " INNER JOIN Utterance ON Utterance.ID=UtteranceID"+
                    " WHERE TurnNo='"+curTurn.getTurn()+
                    "' AND DiscID="+curTurn.getDiscID()+";";
            ArrayList<String> autoCommLink=curTurn.getAutoCommLink();
            if(autoCommLink.size()!=2){
                continue;
            }

            autoCommAct = autoCommLink.get(0);
            autoLinkTo = autoCommLink.get(1);
            rs = Queries.getResult(sqlQuery);
            try {
                while (rs.next()) {  //may cost  overhead in the unlikely situation that there are multiple results
                    groundCommAct = rs.getString("comm_act");
                    groundLinkTo = rs.getString("link_to");
                    //System.out.println(groundCommAct);
                }
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            if(autoCommAct.equals(groundCommAct)){
                count++;
            }

        }
        System.out.println(turns.get(0).getDiscID());
        double num=100.0*count/turns.size();
        System.out.println(count+"/"+turns.size()+"=="+round(num,2)+"%");
    }

    /**
     * rounds val to places amount of precision
     */
    public static double round(double val, int places){
        double result=val;
        double multiplier=Math.pow(10, places);
        result*=multiplier;
        result=Math.round(result);
        result/=multiplier;
        return result;

    }


    private static String findPreviousLink(ArrayList<TurnBundle> turns,int turnsIndex, int speakerID){
	String output="";
	for(int i=turnsIndex-1;i>=0;i--){
	    if(turns.get(i).getSpeakerID()==speakerID){
		output=Queries.getSpeakerName(speakerID)+":"+turns.get(i).getTurn();
		break;
	    }
	}
	return output;
    }


    private static TurnBundle findTurnFromLinkTo(ArrayList<TurnBundle> turns, String linkTo){
	TurnBundle curTurnBundle;
	String [] pair = linkTo.split(":");
        if(pair.length!=2){
	    System.err.println("no link to");
            return null;
        }
	String speaker = pair[0];
	String turnNo = pair[1];
	int turnVal;
	try{
	    turnVal= Integer.parseInt(turnNo);
	}catch(NumberFormatException nfe){
	    turnVal=1;
	}
	for(int i=turnVal-1;i<turns.size();i++){
            pw.println(i);
	    curTurnBundle=turns.get(i);
	    if(Queries.getSpeakerName(curTurnBundle.getSpeakerID()).equals(speaker) &&
	       curTurnBundle.getTurn().equals(turnNo)){
		pw.print(turnVal-i+" ");
		return curTurnBundle;
	    }
	    
	}
	System.err.println("turn not found");
	return null;
    }

    private static ArrayList<TurnBundle> automateCommAct(ArrayList<TurnBundle> turns,int discID){
        double resp=1,cont=1,addr=1;

	String greeterName="";//name of the last greeter
	String greetTurn="";
	int greetThreshold=15;//amount
	int greetDelay=greetThreshold+1;  //amount of turns since last greeting
	for(int i=0;i<turns.size(); i++) {
            TurnBundle curTurn=turns.get(i);
            int speakerID=curTurn.getSpeakerID();
	    ArrayList<String> speakerNames=Queries.getSpeakerNames(discID);
            String speaker=Queries.getSpeakerName(speakerID);
            String timeStamp=curTurn.getTime();
            String turnNo=curTurn.getTurn();
            String utt=curTurn.getUtt().trim();

	    
            pw.print(turnNo + "\t");
	    pw.print(speaker+"\t");
            pw.print(utt+"\t");
	    String commAct="";
            String linkTo="";
            //SELECT Name,Content,comm_act,link_to FROM Utterance INNER JOIN Annotations ON Utterance.ID=UtteranceID INNER JOIN Annotator ON Annotator.ID=AnnotatorID INNER JOIN Speaker ON SpeakerID=Speaker.ID WHERE (Content LIKE '%*' OR Content LIKE '*%') AND NOT Content LIKE '%*%*%';

	    //correct-misspelling
            if((utt.matches(".*\\*") || utt.matches("\\*.*")) && !utt.matches(".*\\*.*\\*.*")){
		commAct=CONTINUATION_OF;
		linkTo=findPreviousLink(turns,i,speakerID);
	    }
	    else if(utt.matches("\\*.*\\*")){
		commAct=ADDRESSED_TO;
	    }
	    //greeting
            else if (utt.toLowerCase().matches("hello.*") || utt.toLowerCase().matches("hey.*") ||
                    utt.toLowerCase().matches("hi .*") || utt.toLowerCase().matches("howdy.*") ||
                    utt.toLowerCase().matches("what's up.*") || utt.toLowerCase().matches("whats up.*") ||
                    utt.toLowerCase().matches("good morning*") || utt.toLowerCase().matches("good evening*")) {
                if (greetDelay >= greetThreshold) {
                    greetDelay = 0;
		    commAct=ADDRESSED_TO;
		    linkTo="all users";
		    for(int j=0;j<speakerNames.size();j++){
			if(utt.toLowerCase().matches(".*"+speakerNames.get(j)+".*"))
			    linkTo=speakerNames.get(j);
		    }
		    greeterName=speaker;
		    greetTurn=turnNo;
		}else{
		    commAct=RESPONSE_TO;
		    linkTo=greeterName+":"+greetTurn;
		    for(int j=0;j<speakerNames.size();j++){
			if(utt.toLowerCase().matches(".*"+speakerNames.get(j)+".*"))
			    linkTo=findPreviousLink(turns,i,Queries.getSpeakerID(speakerNames.get(j)));
	            }
                    if(linkTo.split(":")[0].equals(speaker))
                        linkTo="all users";
                }
            }
            //questions
            else if (utt.endsWith("?")) {
                commAct = ADDRESSED_TO;
            } else {
                for (int j = 0; j < speakerNames.size(); j++) {
                    if (replacePunc(utt).matches(".* "+speakerNames.get(j)+".*") ||
			replacePunc(utt).matches(speakerNames.get(j)+" .*") ||
			replacePunc(utt).matches(speakerNames.get(j))) {
                        linkTo=speakerNames.get(j).toLowerCase();
                        commAct=ADDRESSED_TO;
                    }
                }
                if(linkTo.equals(""))
                    commAct = RESPONSE_TO;
            }

            greetDelay++;
	    pw.print(commAct+"\t");
	    pw.print(linkTo+"\t");
	    pw.println();
            turns.get(i).setAutoCommLink(commAct, linkTo);
            String sqlQuery = "UPDATE CommLinkGround" +
                    " SET auto_comm_act='"+commAct+"', auto_link_to='"+linkTo+"'"+
                    " WHERE UtteranceID="+turns.get(i).getUtteranceID()+";";
	    if(utt.toLowerCase().matches("ken and lance.*")){
		System.out.println(commAct);
		System.out.println(linkTo);
	    }
	    //System.out.println(sqlQuery);
            Queries.runQuery(sqlQuery);
	    
	    
	}
        return turns;
    }

    private static String replacePunc(String input){
        String output=input.replace("`", " ");
        output=output.replace("~", " ");
        output=output.replace("!", " ");
        output=output.replace("@", " ");
        output=output.replace("#", " ");
        output=output.replace("$", " ");
        output=output.replace("%", " ");
        output=output.replace("^", " ");
        output=output.replace("&", " ");
        output=output.replace("*", " ");
        output=output.replace("(", " ");
        output=output.replace(")", " ");
        output=output.replace("-", " ");
        output=output.replace("_", " ");
        output=output.replace("=", " ");
        output=output.replace("+", " ");
        output=output.replace("[", " ");
        output=output.replace("]", " ");
        output=output.replace("{", " ");
        output=output.replace("}", " ");
        output=output.replace("|", " ");
        output=output.replace("\\", " ");
        output=output.replace(":", " ");
        output=output.replace(";", " ");
        output=output.replace("'", " ");
        output=output.replace("\"", " ");
        output=output.replace("<", " ");
        //output=output.replace(",", " ");
        output=output.replace(">", " ");
        output=output.replace(".", " ");
        //output=output.replace("?", " ");
	output=output.replace("?", " ? ");

	

        output = output.replace("/", " ");
        return output;
    }

    //returns the difference between time1 and time2 in seconds
    //dependent upon times falling on the same day
    private static int timeDiff(String time1, String time2){
	//e.g 2:25:36 PM
	String hour1,hour2;
	String min1,min2;
	String sec1,sec2;
	String ampm1,ampm2;

	int colonSeg=3;
	int suffixSeg=2;

	int absTime1=0;
	int absTime2=0;

	String [] timeSet1=time1.split(":");
	if(timeSet1.length!=colonSeg){
	    System.err.println("uh oh");
	    return 0;
	}
	String [] end1 = timeSet1[2].split(" ");
	if(end1.length!=suffixSeg){
	    System.err.println("uh oh");
	    return 0;
	}

	String [] timeSet2=time2.split(":");
	if(timeSet2.length!=colonSeg){
	    System.err.println("uh oh");
	    return 0;
	}

	String [] end2 = timeSet2[2].split(" ");
	if(end2.length!=suffixSeg){
	    System.err.println("uh oh");
	    return 0;
	}

	hour1=timeSet1[0];
	min1=timeSet1[1];
	sec1=end1[0];
	ampm1=end1[1];

	hour2= timeSet2[0];
	min2= timeSet2[1];
	sec2= end2[0];
	ampm2= end2[1];

	//pw.print(hour1+":"+min1+":"+sec1+" "+ampm1);
	//pw.print(" -- ");
	//pw.println(hour2+":"+min2+":"+sec2+" "+ampm2);

	absTime1+=Integer.valueOf(sec1);
	absTime1+=(SECONDS_PER_MINUTE*Integer.valueOf(min1));
	absTime1+=(SECONDS_PER_HOUR*Integer.valueOf(hour1));
	if(ampm1.equals("PM"))
	    absTime1+=(SECONDS_PER_HOUR*HOURS_PER_DAY);
	else if(!ampm1.equals("AM")){
	    System.err.println("uh oh");
	    return 0;
	}

	absTime2+=Integer.valueOf(sec2);
	absTime2+=(SECONDS_PER_MINUTE*Integer.valueOf(min2));
	absTime2+=(SECONDS_PER_HOUR*Integer.valueOf(hour2));
	if(ampm2.equals("PM"))
	    absTime2+=(SECONDS_PER_HOUR*HOURS_PER_DAY);
	else if(!ampm2.equals("AM")){
	    System.err.println("uh oh");
	    return 0;
	}

	return absTime1-absTime2;


    }
    
}
