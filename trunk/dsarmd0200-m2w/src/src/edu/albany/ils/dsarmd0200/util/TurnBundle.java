package edu.albany.ils.dsarmd0200.util;
/**
 * @file: TurnBundle
 * @author: pzongo
 * This file is used to represent a row of queries needed by the
 * comm act ground
 */

import java.util.ArrayList;

public class TurnBundle{
    private String timeStamp="";
    private String turn="";
    private String utt="";
    private String autoDialogAct="";
    private int speakerID=0;
    private int utteranceID=0;
    private int discID=0;
    private ArrayList<ArrayList<String>> commLinks=new ArrayList<ArrayList<String>>();
    private ArrayList<String> autoCommLink=new ArrayList<String>();

    public TurnBundle(){
    }


    public String getTime(){
	return timeStamp;
    }
    public void setTime(String timeStampIn){
	timeStamp=timeStampIn;
    }

    public String getTurn(){
	return turn;
    }
    public void setTurn(String turnIn){
	turn=turnIn;
    }

    public String getUtt(){
	return utt;
    }
    public void setUtt(String uttIn){
	utt=uttIn;
    }

    public int getSpeakerID(){
	return speakerID;
    }
    public void setSpeakerID(int speakerIdIn){
	speakerID=speakerIdIn;
    }
    public int getUtteranceID() {
        return utteranceID;
    }

    public void setUtteranceID(int utteranceIdIn) {
        utteranceID = utteranceIdIn;
    }

    public int getDiscID(){
	return discID;
    }
    public void setDiscID(int discIdIn){
	discID=discIdIn;
    }

    public ArrayList<ArrayList<String>> getCommLinks(){
	return commLinks;
    }
    public void addCommLink(String comm,String link){
        ArrayList<String> input = new ArrayList<String>();
        input.add(comm);
        input.add(link);
	commLinks.add(input);
    }
    public ArrayList<String> getAutoCommLink(){
	return autoCommLink;
    }
    public void setAutoCommLink(String comm,String link){
	autoCommLink.add(comm);
	autoCommLink.add(link);
    }

    public void setAutoDialogAct(String autoDialogActIn){
        autoDialogAct=autoDialogActIn;
    }

    public String getAutoDialogAct(){
        return autoDialogAct;

    }


}
