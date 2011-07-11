/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author Laura G.H. Jiao
 */
public class DsarmdDialogParser {

    public static DOMParser parser = new DOMParser();

    public final String UTTERANCE_STR = "turn";
    public final String COMM_ACT_TYPE = "comm_act_type";
    public final String DIALOG_ACT = "dialog_act";
    public final String LINK_TO = "link_to";
    public final String SPEAKER = "speaker";
    public final String TOPIC = "topic";
    public final String TURN_NO = "turn_no";

    public Document getDocument(String fn) {
	//parseDAList(fn);
	try{
	    parser.parse(fn);
	} catch(SAXException saxe) {
	    saxe.printStackTrace();
	} catch(IOException ioe) {
	    ioe.printStackTrace();
	}
	return parser.getDocument();
    }

    public ArrayList parseDAList(String filename) {
	ArrayList list = new ArrayList();
	try {
	    //get the Document from the parser
	    parser.parse(filename);
	} catch(SAXException saxe) {
	    saxe.printStackTrace();
	} catch(IOException ioe) {
	    ioe.printStackTrace();
	}

	return parseDAList(parser.getDocument());
    }

    public ArrayList parseDAList(Document document){
        Node parent = document;
        return parseDAList(parent);
    }

    public ArrayList parseDAList(Node document){
        ArrayList list = new ArrayList();
	NodeList nodeList = null;
	Node parent = document;
	boolean da = false;
	nodeList = parent.getChildNodes();
	if(nodeList.getLength() == 0) {
	    //if have no children, break the loop
	    return list;
	}
        //check the name of the node
	for(int i = 0; i < nodeList.getLength(); i++) {
	    Node node = nodeList.item(i);
	    //System.out.println(node.getNodeName());
	    if(node.getNodeName().equals(this.UTTERANCE_STR)) {
		//if the node is for dialog act, return
		da = true;
		break;
	    }
	}
        if(da) {
	    //read the dialog act one by one from the node list
	    //create the DialogAct for each of them and add them to the list
	    for(int i = 0; i < nodeList.getLength(); i++) {
		Node node = nodeList.item(i);
		if(node.getNodeName().equals(this.UTTERANCE_STR)) {		    
			//System.out.println("parsing node: " + node);
			list.add(parseDA(node));		    
		}
		else {
		    list.addAll(parseDAList(node));
		}
	    }
	    return list;
	}
	else {
	    for(int i = 0; i < nodeList.getLength(); i++) {
		Node node = nodeList.item(i);
		list.addAll(parseDAList(node));
	    }
	}
	return list;
        
    }

    public Utterance parseDA(Node node) {
	//create a new dialog act
	Utterance da = new Utterance();
	//get the attibutes
	NamedNodeMap map = node.getAttributes();
	Node sub_node = map.getNamedItem(this.DIALOG_ACT);
	if (sub_node != null) {
	    /*
	    System.out.println("=====================");
	    System.out.println("node name: " + sub_node.getNodeName());
	    System.out.println("node local name: " + sub_node.getLocalName());
	    System.out.println("node value: " + sub_node.getNodeValue());
	    */
	    da.setTag(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(this.COMM_ACT_TYPE);
	if (sub_node != null) {
	    da.setCommActType(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(this.TOPIC);
	if (sub_node != null) {
	    da.setTopic(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(this.SPEAKER);
	if (sub_node != null) {
	    da.setSpeaker(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(this.LINK_TO);
	if (sub_node != null) {
	    da.setRespTo(sub_node.getNodeValue());
	}
	sub_node = map.getNamedItem(this.TURN_NO);
	if (sub_node != null) {
	    da.setRespTo(sub_node.getNodeValue());
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
	da.setContent(da_cont.toString());
	//System.out.println("node content is: " + da_cont.toString());
	return da;
    }

}
