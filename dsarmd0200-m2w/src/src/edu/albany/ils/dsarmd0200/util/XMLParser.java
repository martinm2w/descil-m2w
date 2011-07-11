package edu.albany.ils.dsarmd0200.util;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser 
{

	private static String date = "";
	private static ArrayList<String> speakers = new ArrayList<String>();
	private static ArrayList<String> end_times = new ArrayList<String>();
	private static ArrayList<String> turn_nos = new ArrayList<String>();
	private static ArrayList<String> dialog_acts = new ArrayList<String>();
	private static ArrayList<String> comm_act_types = new ArrayList<String>();
	private static ArrayList<String> is_new_topics = new ArrayList<String>();
	private static ArrayList<String> topics = new ArrayList<String>();
	private static ArrayList<String> topic_colors = new ArrayList<String>();
	private static ArrayList<String> focuses = new ArrayList<String>();
	private static ArrayList<String> link_tos = new ArrayList<String>();
	private static ArrayList<String> is_new_focuses = new ArrayList<String>();
	private static ArrayList<String> modes = new ArrayList<String>();
	
	//for marked
	//private static ArrayList<String> breakpoints = new ArrayList<String>();
	
	public static ArrayList<String> parse(String filename)
	{
		ArrayList<String> utterances = new ArrayList<String>();
		try {		
				DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
				DocumentBuilder parser1 = factory1.newDocumentBuilder();
				Document document1 = parser1.parse(filename);
				
				Element dialogue1 = document1.getDocumentElement();
				NodeList turns1 = dialogue1.getElementsByTagName("turn");
				NodeList root = document1.getElementsByTagName("Dialogue");
				Element rootvalue = (Element)root.item(0);
				
				//get date attribute
				date = rootvalue.getAttribute("date");
				//outFile.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
				//outFile.println("<Dialogue date=\""+date+"\">");
				
				//get all attributes for each row, save into arraylists
				for(int i=0;i<turns1.getLength();i++)
				{
					Element el1 = (Element)turns1.item(i);
					
					//retrieve all attributes and save
					//dirty staff
					String speaker = el1.getAttribute("speaker");
					if(speaker==null)
					{
						speaker = " ";
					}
					String end_time = el1.getAttribute("end_time");
					if(end_time==null)
					{
						end_time = " ";
					}
					String turn_no = el1.getAttribute("turn_no");
					if(turn_no == null)
					{
						turn_no = " ";
					}
					String dialog_act = el1.getAttribute("dialog_act");
					if(dialog_act == null)
					{
						dialog_act = " ";
					}
					String comm_act_type = el1.getAttribute("comm_act_type");
					if(comm_act_type == null)
					{
						comm_act_type = " ";
					}
					String link_to = el1.getAttribute("link_to");
					if(link_to == null)
					{
						link_to = " ";
					}
					String is_new_topic = el1.getAttribute("is_new_topic");
					if(is_new_topic == null)
					{
						is_new_topic = " ";
					}
					String topic = el1.getAttribute("topic");
					if(topic == null)
					{
						topic = " ";
					}
					String topic_color = el1.getAttribute("topic_color");
					if(topic_color == null)
					{
						topic_color = " ";
					}
					String is_new_focus =el1.getAttribute("is_new_focus");
					if(is_new_focus == null)
					{
						is_new_focus = " ";
					}
					String focus = el1.getAttribute("focus");
					if(focus == null)
					{
						focus = " ";
					}
					String mode = el1.getAttribute("mode");
					if(mode == null)
					{
						mode = " ";
					}
					
					//for marked
					//String breakpoint = el1.getAttribute("breakpoint");
					//if(breakpoint == null)
					//{
					//	breakpoint = " ";
					//}
					//breakpoints.add(breakpoint);
					
					
					
					speakers.add(speaker);
					end_times.add(end_time);
					turn_nos.add(turn_no);
					dialog_acts.add(dialog_act);
					comm_act_types.add(comm_act_type);
					link_tos.add(link_to);
					is_new_topics.add(is_new_topic);
					topics.add(topic);
					topic_colors.add(topic_color);
					is_new_focuses.add(is_new_focus);
					focuses.add(focus);
					modes.add(mode);
					
					String chatcontent;

		    		//get utterances here
		    		Node firstNode = el1.getChildNodes().item(0);
		    		if(firstNode!=null)
		    		{
		    			chatcontent = firstNode.getNodeValue();
		    			if(chatcontent=="")
		    			{
		    				chatcontent = " ";
		    			}
		    			utterances.add(chatcontent);
		    		}
		    		else
		    		{
		    			utterances.add(" ");
		    		}
			} 
		}
		  catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(utterances.size());
		return utterances;
	}

	//********** Start of getters ************
	public static String getDate() {
		return date;
	}


	public static ArrayList<String> getSpeakers() {
		return speakers;
	}


	public static ArrayList<String> getEnd_times() {
		return end_times;
	}


	public static ArrayList<String> getTurn_nos() {
		return turn_nos;
	}


	public static ArrayList<String> getDialog_acts() {
		return dialog_acts;
	}


	public static ArrayList<String> getComm_act_types() {
		return comm_act_types;
	}


	public static ArrayList<String> getIs_new_topics() {
		return is_new_topics;
	}


	public static ArrayList<String> getTopics() {
		return topics;
	}


	public static ArrayList<String> getTopic_colors() {
		return topic_colors;
	}


	public static ArrayList<String> getFocuses() {
		return focuses;
	}


	public static ArrayList<String> getLink_tos() {
		return link_tos;
	}


	public static ArrayList<String> getIs_new_focuses() {
		return is_new_focuses;
	}

	public static ArrayList<String> getModes() {
		return modes;
	}
	
	//for marked
	//public static ArrayList<String> getBreakpoints()
	//{
	//	return breakpoints;
	//}
}
