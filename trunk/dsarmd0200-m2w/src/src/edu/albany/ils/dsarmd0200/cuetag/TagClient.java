package edu.albany.ils.dsarmd0200.cuetag;

import java.io.*;
import java.net.*;

public class TagClient {
	
    Socket socket = null;
    PrintWriter out = null;
    BufferedReader in = null;
    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    String fromServer;
    String fromUser;
    String host;
    int port;

    public TagClient(String str, int num){
	this.host = str;
	this.port = num;
        listenSocket();
    }

	public void listenSocket(){
   // Create socket connection
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
	  try {	
		  out.println(str);
		  if((fromServer = in.readLine()) != null) {
		      taggedStr = fromServer;
			  System.out.println("client side: " + fromServer);
		  }
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
	
	protected void finalize(){
//		Clean up 
		try{
	      out.close();
		  in.close();
	      stdIn.close();
          socket.close();
		} catch (IOException e) {
		   System.out.println("Could not close.");
	       System.exit(-1);
	    }
	}
    /*
	public static void main(String[] args) {
		TagClient tagClient = new TagClient("localhost", 4325);
		//	tagClient.listenSocket();
		tagClient.process("hello, how are you?");
		tagClient.process("do you have some interests on this topic");
		tagClient.process("I hope you can copy these materials");
		tagClient.finalize();
		System.out.println("the client side is over!");
	}
    */
}
