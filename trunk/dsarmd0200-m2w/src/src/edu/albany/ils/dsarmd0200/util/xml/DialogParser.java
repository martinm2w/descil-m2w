package edu.albany.ils.dsarmd0200.util.xml;

import edu.albany.ils.dsarmd0200.evaltag.*;
import java.util.*;
import java.io.*;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Pars dialog in xml format
 * @author TL
 * 
 * */
public abstract class DialogParser {
    public static String DialogActStr = "turn"; 
    public static DOMParser parser = new DOMParser();

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

    /**
     * Return the list of dialog acts by parsing a file.
     * @param filename the String name of the file
     * Modified by TL 11/08/07
     * */
    public ArrayList parseDAList(String filename,
				 String dl_act_str,
				 String doc_type) {
	ArrayList list = new ArrayList();
	try {
	    //get the Document from the parser
	    parser.parse(filename);
	} catch(SAXException saxe) {
	    saxe.printStackTrace();
	} catch(IOException ioe) {
	    ioe.printStackTrace();
	}
	
	return parseDAList(parser.getDocument(), doc_type, false, dl_act_str);
    }
    
    /**
     * Return the list of dialog acts by parsing a file.
     * @param document in xml document structure
     * Added by TL 11/08/07
     * */
    public ArrayList parseDAList(Document document,
				 String corpus_type,
				 boolean clustered,
				 String dl_act_str) {
	
	Node parent = document;
	return parseDAList(parent, corpus_type, clustered, dl_act_str);
    }

    public ArrayList parseDAList(Node document,
				 String corpus_type,
				 boolean clustered,
				 String dl_act_str) {
	if (dl_act_str != null) {
	    DialogActStr = dl_act_str;
	}
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
	    if(node.getNodeName().equals(DialogActStr)) {
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
		if(node.getNodeName().equals(DialogActStr)) {
		    if (corpus_type.equals("amities-swbd") ||
			corpus_type.equals("amities") ||
			corpus_type.equals("companion-swbd") ||
			corpus_type.equals("dsarmd")) {
			//System.out.println("parsing node: " + node);
			list.add(parseDA(node));
		    }
		    else {
			list.add(parseDA(node, corpus_type, clustered));
		    }
		}
		else {
		    list.addAll(parseDAList(node, corpus_type, clustered, dl_act_str));
		}
	    }
	    return list;
	}
	else {
	    for(int i = 0; i < nodeList.getLength(); i++) {
		Node node = nodeList.item(i);
		list.addAll(parseDAList(node, corpus_type, clustered, dl_act_str));
	    }
	}
	return list;
    }
    
    /**
     * Parse the Node to get a DialogAct.
     * @param node the Node in dom representing the dialog act
     * */
    public abstract DialogAct parseDA(Node node);
    
    /**
     * Parse the Node to get a DialogAct. Use the original tag of the dialogue
     * @param node the Node in dom representing the dialog act
     * @param clustered whether use clustered tag or original tag
     *         --- true - use clustered tag
     *         --- false - use original tag
     * Added by TL
     * */
    public abstract DialogAct parseDA(Node node,
				      String corpus_type,
				      boolean clustered);
}
