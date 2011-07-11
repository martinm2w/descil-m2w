package edu.albany.ils.dsarmd0200.cuetag;

import java.io.*;


/*
 *  this class is used to parse the da source file to produce the NE file which will further be processed with BBN
 *  Identifinder to tag out name entities, and then further be processed by RefineNETagFile class to replace specific 
 *  word with the general Name Entity Classifer's name
 *  Author: Ting Liu
 * */

public class FileProcessor {
	
    public static void processFile() {
	String readPath = "/home/ting/develop/deer/data/swbd_202k_42tags_noplus.xml";
	String writePath = "/home/ting/develop/deer/data/swbd_202k_42tags_noplus.txt";
	BufferedReader br;
	PrintWriter pw;
	String line = null;
	String newLine = null;
	//String speaker = null;
	String text = null;
	String daTag = null;
	int text_length = 0;
	int startIndex = -1;
	int endIndex = -1;
	int dialogue_ID = 0;
	try{
	    br = new BufferedReader( new FileReader( new File(readPath)));
	    pw = new PrintWriter( new BufferedWriter( new FileWriter( new File(writePath))));
	    
	    while((line = br.readLine())!=null) {
		if(line.indexOf("<dialogue name")>=0) {
		    pw.println("Dialogue " + dialogue_ID++);
		} else if (line.indexOf("CDATA")>=0) {
		    startIndex = line.indexOf("da=") + 4;
		    endIndex = line.indexOf("id=") - 2;
		    daTag = line.substring(startIndex, endIndex);	
		    startIndex = line.indexOf("CDATA[") + 6;
		    endIndex = line.indexOf("]]></utt>");
		    newLine = cleanAndFormat(line, daTag, startIndex, endIndex, text_length);
		    //	     		    newLine = model_number + ":" + daTag + ":" + "<text>" + text +"</text>";
		    //the reason to add<text>/</text> tag here is to make BBN Identifinder to be able to parse the 
		    //produced file
		    pw.println(newLine);
		} 
	    }
	    pw.close();
	} catch (IOException e) {
	    System.out.println(e + "read file error!");
	}
	
	
    }
    
    public static String cleanAndFormat(String line,
					String daTag,
					int startIndex,
					int endIndex,
					int text_length) {
	int model_number = 0;
	String newLine = null;
	String text = line.substring(startIndex, endIndex);
	if(text.indexOf("{F ")>=0) {
	    text = text.replaceAll("\\{F[\\s][^}]+[}]", "");
	}
	if(text.indexOf("{D ")>=0) {
	    text = text.replaceAll("\\{D", "");
	}
	if(text.indexOf("{C")>=0) {
	    text = text.replaceAll("\\{C ", "");
	}
	if(text.indexOf("{A")>=0) {
	    text = text.replaceAll("\\{A ", "");
	}
	if(text.indexOf("{E")>=0) {
	    text = text.replaceAll("\\{E ", "");
	}
	if(text.indexOf("-")>=0) {
	    text = text.replaceAll("-", "");
	}
	if(text.indexOf("<")>=0 && text.indexOf(">")>=0) {
	    text = text.replaceAll("[<][^>]+[>]", "");
	}
	text = text.replaceAll("$a $m", "am");
	text = text.replaceAll("$p $m", "pm");
	text = text.replaceAll("[^\\w^']+", " ").toLowerCase().trim();
	//	text = text.replaceAll("[^\\w^'^\\?]+", " ").toLowerCase().trim();
	text = text.replaceAll("'cause", "because");
	text = text.replaceAll("'bout", "about");
	text = text.replaceAll("\\s+", " ");
	if(text != null && !text.equals("")) {
	    text_length = text.split(" ").length;
	    if(text_length == 1) model_number = 1;
	    else if (text_length >=2 && text_length <=4) model_number = 2;
	    else model_number = 3;					
	    newLine = model_number + ":" + daTag + ":" + "<start> " + text +" <finish>";
	}
	return newLine;
    }
    
    
    public static String clean(String line) {
	int model_number = 0;
	String newLine = null;
	String text = line;
	if(text.indexOf("{F ")>=0) {
	    text = text.replaceAll("\\{F[\\s][^}]+[}]", "");
	}
	if(text.indexOf("{D ")>=0) {
	    text = text.replaceAll("\\{D", "");
	}
	if(text.indexOf("{C")>=0) {
	    text = text.replaceAll("\\{C ", "");
	}
	if(text.indexOf("{A")>=0) {
	    text = text.replaceAll("\\{A ", "");
	}
	if(text.indexOf("{E")>=0) {
	    text = text.replaceAll("\\{E ", "");
	}
	if(text.indexOf("-")>=0) {
	    text = text.replaceAll("-", "");
	}
	if(text.indexOf("<")>=0 && text.indexOf(">")>=0) {
	    text = text.replaceAll("[<][^>]+[>]", "");
	}
	text = text.replaceAll("$a $m", "am");
	text = text.replaceAll("$p $m", "pm");
	text = text.replaceAll("[^\\w^']+", " ").toLowerCase().trim();
	//	text = text.replaceAll("[^\\w^'^\\?]+", " ").toLowerCase().trim();
	text = text.replaceAll("'cause", "because");
	text = text.replaceAll("'bout", "about");
	text = text.replaceAll("\\s+", " ");
	return text;
    }
    
    
    public static void main(String[] args) {
	processFile();
	System.out.println("program ends!");
    }

}
