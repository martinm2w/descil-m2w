package edu.albany.ils.dsarmd0200.evaltag;

import java.util.*;
import java.io.*;
//import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * The dialog act for companion data.
 * @author TL
 * @date 09/11/08
 * */
public class Utterance implements DialogAct {
    
    //data
    private String content = null;
    private String tagged_content = null;
    private String tag = "";    
    private String mtag = "";    
    private String polarity = null;
    private String topic = null;
    private String turn = null;
    private String pos_ = null;
    private String pos_origin_ = null;
    private String resp_to = null;
    private String speaker = null;
    private String comm_act_type = "";
    private String time = null;
    
    private String sys_polarity = null;
    private String sys_tag = null;
    private String sys_resp_to = null;
    private String sys_comm_act_type = null;
    private String sys_topic = null;
    
    private Node source_ = null;

    //m2w: added instance variable of type Utterance containing the .1 and .0 turns.
    private ArrayList<Utterance> sub_turns = new ArrayList<Utterance>();


    /************************get information***********************************/
    /**
     * Get the content.
     * */
    public String getContent() {
	return content;
    }
    
    public String getUtterance() {
	return content;
    }

    public String getTaggedContent() {
	return tagged_content;
    }
    
    /**
     * Get the tag.
     * */
    public String getTag() {
	return tag;
    }

    public String getMTag() {
	return mtag;
    }
    
    public String getSpeaker() {
	return speaker;
    }
    
    public String getPolarity() {
	return polarity;
    }

    public String getTopic() {
	return topic;
    }

    public String getPOS() {
	return pos_;
    }

    public String getPOSORIGIN() {
	return pos_origin_;
    }

    public String getTurn() {
	return turn;
    }

    public String getRespTo() {
	return resp_to;
    }
    
    public String getCommActType() {
	return comm_act_type;
    }
    
    public String getSysCommActType() {
	return sys_comm_act_type;
    }
    
    public String getSysTag() {
	return sys_tag;
    }
    
    public String getSysPolarity() {
	return sys_polarity;
    }

    public String getSysRespTo() {
	return sys_resp_to;
    }
    
    public String getSysTopic() {
	return sys_topic;
    }

    /**
     * get the source node
     */
    public Node getSource() {
	return source_;
    }


    public String getStandardTag() {
	return null;
    }
    
    public String getStandardTag(String _tag) {
	return null;
    }

    public boolean isStandardTag(String tag) {
	return false;
    }


    /*****************************set information*****************************/
    /**
     * set source node
     */
    public void setSource(Node node) {
	source_ = node;
    }
    
    /**
     * Set the tag.
     * */
    public void setTag(String tag) {
	if (tag.indexOf(":") != -1) {
	    if (tag.endsWith(":")) {
		//System.out.println("tag: " + tag);
		this.tag = "unknown";
		return;
	    }
	    String[] tags = tag.split(":");
	    this.tag = tags[1];
	    this.mtag = tags[0];
	} else {
	    this.tag = tag;
	}
    }

    // Added by Laura, 9-1-2010
    public void setTag3(String tag3){
	this.mtag = tag3;
	/*
        String[] tags = this.tag.split(":");
        String tag15 = tags[1];
        this.tag = tag3 + ":" + tag15;
	*/
    }

    public void setTag15(String tag15){
	/*
        String[] tags = this.tag.split(":");
        String tag3 = tags[0];
	*/
        this.tag = tag15;
    }
    

    public void setPOS(String pos) {
	this.pos_ = pos;
    }

    public void setPOSORIGIN(String pos_origin) {
	this.pos_origin_ = pos_origin;
    }

    public void setSpeaker(String speaker) {
	this.speaker = speaker.toLowerCase();//add toLowerCase 1/20/2011
    }

    public void setPolarity(String polarity) {
	this.polarity = polarity;
    }

    
    public void setTurn(String turn) {
	this.turn = turn.split("\\.")[0];
    }

    public void setRespTo(String resp_to) {
	this.resp_to = resp_to;
    }

    public void setCommActType(String comm_act_type) {
	this.comm_act_type = comm_act_type;
    }

    public void setSysCommActType(String sys_comm_act_type) {
	this.sys_comm_act_type = sys_comm_act_type;
    }

    public void setSysTag(String tag) {
	this.sys_tag = tag;
    }

    public void setSysPolarity(String polarity) {
	this.sys_polarity = polarity;
    }

    public void setSysRespTo(String resp_to) {
	this.sys_resp_to = resp_to;
    }

    /**
     * Set the content.
     * */
    public void setContent(String content) {
	this.content = content;
    }

    public void setTaggedContent(String content) {
	this.tagged_content = content;
    }

    public void setUtterance(String content) {
	this.content = content;
    }
    
    public void setTopic (String topic) {
	this.topic = topic;
    }

    public void setTime (String time) {
	this.time = time;
    }

    public void setSysTopic(String sys_topic) {
	this.sys_topic = topic;
    }

    /**
     * set tag maps
     */
    public static void setTagMaps() {
    }

    public String toString() {
	if (topic != null) {
	    StringBuffer output = new StringBuffer();
	    output.append("speaker: " + speaker + "\n");
	    output.append("content: " + content + "\n");
	    output.append("tag: " + tag + "\n");
	    output.append("topic: " + topic + "\n");
	    output.append("mtag: " + mtag + "\n");
	    output.append("communication act: " + comm_act_type + "\n");
	    output.append("polarity: " + polarity + "\n");
	    output.append("turn: " + turn + "\n");
	    output.append("response to: " + resp_to + "\n");
	    output.append("extracted tag: " + sys_tag + "\n");
	    output.append("extracted polarity: " + sys_polarity + "\n");
	    output.append("extracted response to: " + sys_resp_to + "\n");
	    return output.toString();
	}
	return null;
    }

    /**
     * @return the sub_turns
     */
    public ArrayList<Utterance> getSub_turns() {
        return sub_turns;
    }

    /**
     * @param sub_turns the sub_turns to set
     */
    public void setSub_turns(ArrayList<Utterance> sub_turns) {
        this.sub_turns = sub_turns;
    }
    
}
