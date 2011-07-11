package edu.albany.ils.dsarmd0200.util.xml;

import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.util.*;
import java.util.*;
import java.io.*;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Pars dialog in companioin xml data format
 * @author TL
 * @date 09/10/08
 **/
public class DsarmdDP extends DialogParser {

    //constants
    public static final String DialogActStr = "turn";
    public static final String Tag_Name = "dialog_act";
    public static final String Polarity = "polarity";
    public static final String Turn_no = "turn_no";
    public static final String Link_to = "link_to";
    public static final String Speaker = "speaker";
    public static final String Topic = "topic";
    public static final String POS = "pos";
    public static final String POS_ORI = "pos_origin";
    public static final String Comm_Act_Type = "comm_act_type";

    /**
     * Parse the Node to get a DialogAct.
     * @param node the Node in dom representing the dialog act
     * */
    public DialogAct parseDA(Node node) {
	//create a new dialog act
	Utterance da = new Utterance();
	//get the attibutes
	NamedNodeMap map = node.getAttributes();
	String tag = new String();
	Node sub_node = map.getNamedItem(Tag_Name);
	if (sub_node != null) {
	    /*
	    System.out.println("=====================");
	    System.out.println("node name: " + sub_node.getNodeName());
	    System.out.println("node local name: " + sub_node.getLocalName());
	    System.out.println("node value: " + sub_node.getNodeValue());
	    */
	    tag = sub_node.getNodeValue();
	    da.setTag(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(Comm_Act_Type);
	if (sub_node != null) {
	    tag = sub_node.getNodeValue();
	    da.setCommActType(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(Polarity);
	if (sub_node != null) {
	    tag = sub_node.getNodeValue();
	    da.setPolarity(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(POS);
	if (sub_node != null) {
	    tag = sub_node.getNodeValue();
	    da.setPOS(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(POS_ORI);
	if (sub_node != null) {
	    tag = sub_node.getNodeValue();
	    da.setPOSORIGIN(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(Topic);
	if (sub_node != null) {
	    tag = sub_node.getNodeValue();
	    da.setTopic(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(Speaker);
	if (sub_node != null) {
	    tag = sub_node.getNodeValue();
	    da.setSpeaker(sub_node.getNodeValue());
	}
	
	sub_node = map.getNamedItem(Link_to);
	if (sub_node != null) {
	    tag = sub_node.getNodeValue();
	    da.setRespTo(sub_node.getNodeValue());
	}
	
	sub_node = map.getNamedItem(Turn_no);
	if (sub_node != null) {
	    tag = sub_node.getNodeValue();
	    da.setTurn(sub_node.getNodeValue());
	}
	
	/*
	Node tmpNode = null;
	//check the basic forward looking function
	tmpNode = map.getNamedItem(Tag_Name);
	if(tmpNode != null) {
	}
	*/
	StringBuffer da_cont = new StringBuffer();
	NodeList list = node.getChildNodes();
	String content = null;
	for(int i = 0; i < list.getLength(); i++) {
	    if((content = list.item(i).getTextContent()) != null) {
		da_cont.append(content);
	    }
	}
	//set the utterance of the dialog act
	//da.setContent(Util.filterIt(da_cont.toString()));
	da.setContent(da_cont.toString()); //03/23/11 TL
	da.setSource(node);
	//System.out.println("node content is: " + da_cont.toString());
	return da;
    }
    
    /**
     * Parse the Node to get a DialogAct. Use the original tag of the dialogue
     * @param node the Node in dom representing the dialog act
     * @param clustered whether use clustered tag or original tag not used in companion data now
     *         --- true - use clustered tag
     *         --- false - use original tag
     * 
     * */
    public DialogAct parseDA(Node node,
			     String corpus_type, //not used in companion data
			     boolean clustered) {
	//create a new dialog act
	Utterance da = new Utterance();
	//get the attibutes
	NamedNodeMap map = node.getAttributes();
	String tag = new String();
	StringBuffer da_cont = new StringBuffer();
	//get the utterance of the dialog act
	NodeList list = node.getChildNodes();
	String content = null;
	for(int i = 0; i < list.getLength(); i++) {
	    if((content = list.item(i).getTextContent()) != null) {
		da_cont.append(content);
	    }
	}
	//set the utterance of the dialog act
	da.setContent(da_cont.toString());
	da.setSource(node);
	//System.out.println("node content is: " + da_cont.toString());
	return da;
    }
}
