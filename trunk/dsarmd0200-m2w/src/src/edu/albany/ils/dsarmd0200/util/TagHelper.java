package edu.albany.ils.dsarmd0200.util;

import java.net.*;
import java.io.*;

public class TagHelper{
    //added for TagClient functionality on the serverside pzongo
    Socket socket = null;
    PrintWriter out = null;
    BufferedReader in = null;
    //BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    String fromServer;
    String fromUser;
    final String host="localhost";  
    final int port=4325;  //should get value from XDOCPreferences 
    //int LENGTH_LIMIT=25;  //length restriction has been removed from DATagger

    public TagHelper(){
	init();
    }


    public void init(){
	listenSocket();  //added by pzongo
    }


    //pzongo added to store chat utterances 02/10/09

    public void listenSocket(){   // Create socket connection
	try{
	    socket = new Socket(this.host, this.port);
	    out = new PrintWriter(socket.getOutputStream(), true);
	    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	} catch (UnknownHostException e) {
	    System.out.println("Unknown host: localhost");
		System.exit(1);
	} catch  (IOException e) {
	    System.out.println("No I/O");
	    System.exit(1);
	}
    }
    
    
    public String process(String str) {
	String taggedStr = null;
	//pzPrint(str);
	str=str.trim();
	try {	
	    //out.println(str);
	    pzPrint(str);
	    //do{
	    //	fromServer=in.readLine();
	    //	
	    //}while(fromServer.equals("sd###")&& !str.equals(""));
	    //taggedStr=fromServer;
	    //System.out.println("client side: " + fromServer);
	    String [] myUtterance = str.replaceAll("[^a-zA-Z_0-9'\\s]", "").split("\\s+");

	    //assign 'll' tag to utterances longer than LENGTH_LIMIT characters
	    //because DATagger won't do it for you  ,DATagger now has no length limit
	    //if(myUtterance.length >LENGTH_LIMIT){
	    //	return "ll###"+str;  
	    //}
	    out.println(str);
	    if((fromServer = in.readLine()) != null) {
		taggedStr = fromServer;
		System.out.println("client side: " + fromServer);
	    }
	    else
		System.out.println("foo");

	    /*  old process loop, resulted in empty messages
	    if((fromServer = in.readLine()) != null) {
		taggedStr = fromServer;
		System.out.println("client side: " + fromServer);
	    }
	    */
	    //while ((fromServer = in.readLine()) != null) {
	    //System.out.println("client side: " + fromServer);
	    //break;
	    //if(fromServer.equals("unknown:exit")) break;
	    //if(!fromServer.equals("tag server waits for connection...")) 
	    //System.out.println("Server: " + fromServer);    
	    //fromUser = stdIn.readLine();
	    //if (fromUser != null) {
	    //System.out.println("Client: " + fromUser);
	    //out.println(fromUser);
	    //}
	    //}
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return taggedStr;
    }

    public void pzPrint(String str){
	System.out.println("pz "+str);
    }
}
