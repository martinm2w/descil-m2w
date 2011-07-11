package edu.albany.ils.dsarmd0200.util.xml;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

//import org.apache.commons.*;
import org.apache.xmlbeans.XmlException;
//import org.apache.xmlbeans.XmlObject;
//import org.apache.xmlbeans.XmlOptions;

import gov.iarpa.scil.reportSchema.DataUnit;
import gov.iarpa.scil.reportSchema.DuParticipant;
import gov.iarpa.scil.reportSchema.LuClaimSet;
import gov.iarpa.scil.reportSchema.LucStatement;
//import gov.iarpa.scil.reportSchema.LucStatement;
import gov.iarpa.scil.reportSchema.ParticipantList;
import gov.iarpa.scil.reportSchema.SystemReportDocument;
import gov.iarpa.scil.reportSchema.LuClaimSet.ExpressiveDisagreementClaim;
import gov.iarpa.scil.reportSchema.LuClaimSet.InvolvementClaim;
import gov.iarpa.scil.reportSchema.LuClaimSet.TaskControlClaim;
import gov.iarpa.scil.reportSchema.LuClaimSet.TopicControlClaim;
import gov.iarpa.scil.reportSchema.SystemReportDocument.SystemReport;
import gov.iarpa.scil.reportSchema.SystemReportDocument.SystemReport.PerformerTeamId.Enum;

//This class parses the pts report XML file
public class PtsReportXMLParser implements PtsReport
{
	private String filename = "";
	private SystemReportDocument sys = null;
	private SystemReport sysdoc = null;
	private DataUnit du = null;
	private ParticipantList pl = null;
    private LuClaimSet lc = null;
    private Enum team;
    private HashMap<String, String> userNms = new HashMap<String, String>();
	
	public PtsReportXMLParser(String filename)
	{
		super();
		this.filename = filename;
		sys = SystemReportDocument.Factory.newInstance();
		sysdoc = sys.addNewSystemReport();
		sysdoc.setPerformerTeamId(team);
		
	}
	
	public void setDataUnit(String id, String name)
	{
		du = sysdoc.addNewDuDescription();
		du.setDuId(id);
		du.setDuName(name);
		//du.setPerformerTeamId("ALB");
	}
	
	public void setDuParticipants(ArrayList<String> arr)
	{
		if(arr.size() != 0)
		{
			pl = sysdoc.addNewDuParticipantList();
			BigInteger par_num = new BigInteger(String.valueOf(arr.size()));
			pl.setParticipantCount(par_num);
			DuParticipant[] participants = new DuParticipant[arr.size()];
			for(int index=0; index<participants.length; index++)
			{
			    participants[index] = pl.addNewParticipant();
			    //String id_name = arr.get(index);
			    //String id = id_name.substring(0, id_name.indexOf("_"));
			    //String name = id_name.substring(id_name.indexOf("_") + 1);
			    String name = arr.get(index);
			    participants[index].setParticipantId((new Integer(index)).toString());
			    if (name.startsWith("ils") ||
				!userNms.containsKey(name)) { participants[index].setParticipantName(name); }
			    else {participants[index].setParticipantName(userNms.get(name));}
			    //participants[index].setParticipantId(id);
			    //participants[index].setParticipantName(name);
			}
			pl.setParticipantArray(participants);
		}
	}
	
	public void initClaim()
	{
		lc = sysdoc.addNewLuClaimSet();
		lc.setClaimCount(new BigInteger("4"));
		userNms.put("jenniferb", "JR");
		userNms.put("lisa", "LI");
		userNms.put("eugenia", "EA");
		userNms.put("jessicad", "JD");
		userNms.put("kerri", "KI");
		userNms.put("laura", "LA");
		userNms.put("jenifer", "JR");
		userNms.put("johnw", "JW");
		userNms.put("jessicaf", "JF");
		userNms.put("nicole", "NE");
		userNms.put("jessamyn", "JN");
		userNms.put("jorge1", "JE");
		userNms.put("tony", "TY");
		userNms.put("ashley", "AY");
		userNms.put("lisa", "LI");
		userNms.put("danielle", "DE");
		userNms.put("chris", "CS");
		userNms.put("jamie", "JE");
		userNms.put("jennifer", "JR");
		userNms.put("jenny", "JY");
		userNms.put("jianhua", "JA");
		userNms.put("kara", "KA");
		userNms.put("ken", "KN");
		userNms.put("lance", "LE");
	}

    public void setClaimCount(int count) {
	if (lc != null) {
	    lc.setClaimCount(new BigInteger((new Integer(count)).toString()));
	}else {
	    System.err.println("lc is null, please init first!!!!");
	}
    }
	
	public void setTopicControl(HashMap<String,ArrayList> topicMap)
	{
		if(lc != null && topicMap.keySet().size() > 0)
		{
			TopicControlClaim[] tocc = new TopicControlClaim[topicMap.keySet().size()];
			String[] keys = topicMap.keySet().toArray(new String[0]);
			for(int index=0; index<tocc.length; index++)
			{
				tocc[index] = lc.addNewTopicControlClaim();
				String key = keys[index];
				String id = key.substring(0, key.indexOf("_"));
				String name = key.substring(key.indexOf("_") + 1);
				ArrayList tocc_arr = topicMap.get(key);
				int score = ((Integer)tocc_arr.get(0)).intValue();
				String tocc_evidence = (String)tocc_arr.get(1);
				tocc[index].setClaimId(id);
				DuParticipant participants_topic = tocc[index].addNewParticipant1();
				participants_topic.setParticipantId(id);
				if (name.startsWith("ils")) { participants_topic.setParticipantName(name); }
				else {
				    if (userNms.get(name) != null) {
					participants_topic.setParticipantName(userNms.get(name));
					tocc_evidence = tocc_evidence.replaceAll(name, userNms.get(name));
				    }else {
					participants_topic.setParticipantName(name);
				    }
				}
				tocc[index].setParticipant1(participants_topic);
				tocc[index].setLuValue(new BigInteger(String.valueOf(score)));
				LucStatement tocc_ls = LucStatement.Factory.newInstance();
				tocc_ls.addEvidence(tocc_evidence);
				tocc[index].setLucEvidence(tocc_ls);
			}
			lc.setTopicControlClaimArray(tocc);
		}
		else
			System.err.println("lc is null, please call initClaim() first");
	}
	
	public void setTaskControl(HashMap<String,ArrayList> taskMap)
	{
		if(lc != null && taskMap.keySet().size() > 0)
		{
			TaskControlClaim[] tacc = new TaskControlClaim[taskMap.keySet().size()];
			String[] keys = taskMap.keySet().toArray(new String[0]);
			for(int index=0; index<tacc.length; index++)
			{
				tacc[index] = lc.addNewTaskControlClaim();
				String key = keys[index];
				String id = key.substring(0, key.indexOf("_"));
				String name = key.substring(key.indexOf("_") + 1);
				ArrayList tacc_arr = taskMap.get(key);
				int score = ((Integer)(tacc_arr.get(0))).intValue();
				String tacc_evidence = (String)tacc_arr.get(1);
				tacc[index].setClaimId(id);
				DuParticipant participants_task = tacc[index].addNewParticipant1();
				participants_task.setParticipantId(id);
				if (name.startsWith("ils")) { participants_task.setParticipantName(name); }
				else {
				    if (userNms.get(name) != null) {
					participants_task.setParticipantName(userNms.get(name));
					tacc_evidence = tacc_evidence.replaceAll(name, userNms.get(name));
				    }else {
					participants_task.setParticipantName(name);
				    }
				}
				tacc[index].setParticipant1(participants_task);
				tacc[index].setLuValue(new BigInteger(String.valueOf(score)));
				LucStatement tacc_ls = LucStatement.Factory.newInstance();
				tacc_ls.addEvidence(tacc_evidence);
				tacc[index].setLucEvidence(tacc_ls);
			}
			lc.setTaskControlClaimArray(tacc);
		}
		else
			System.err.println("lc is null, please call initClaim() first");
	}
	
	public void setExpressiveDisagreementClaim(HashMap<String,ArrayList> EDCMap)
	{
		if(lc != null && EDCMap.keySet().size() > 0)
		{
			ExpressiveDisagreementClaim[] edc = new ExpressiveDisagreementClaim[EDCMap.keySet().size()];
			String[] keys = EDCMap.keySet().toArray(new String[0]);
			for(int index=0; index<edc.length; index++)
			{
				edc[index] = lc.addNewExpressiveDisagreementClaim();
				String key = keys[index];
				String id = key.substring(0, key.indexOf("_"));
				String name = key.substring(key.indexOf("_") + 1);
				ArrayList EDC_arr = EDCMap.get(key); 
				int score = ((Integer)(EDC_arr.get(0))).intValue();
				String EDC_evidence = (String)EDC_arr.get(1);
				edc[index].setClaimId(id);
				DuParticipant participants_edc = edc[index].addNewParticipant1();
				participants_edc.setParticipantId(id);
				if (name.startsWith("ils") ||
				    !userNms.containsKey(name)) { participants_edc.setParticipantName(name); }
				else {
				    participants_edc.setParticipantName(userNms.get(name));
				    EDC_evidence = EDC_evidence.replaceAll(name, userNms.get(name));
				}
				edc[index].setParticipant1(participants_edc);
				edc[index].setLuValue(new BigInteger(String.valueOf(score)));
				LucStatement EDC_ls = LucStatement.Factory.newInstance();
				EDC_ls.addEvidence(EDC_evidence);
				edc[index].setLucEvidence(EDC_ls);
			}
			lc.setExpressiveDisagreementClaimArray(edc);
		}
		else
			System.err.println("lc is null, please call initClaim() first");
	}
	
	public void setInvolvementClaim(HashMap<String,ArrayList> ICMap)
	{
		if(lc != null && ICMap.keySet().size() > 0)
		{
			InvolvementClaim[] ic = new InvolvementClaim[ICMap.keySet().size()];
			String[] keys = ICMap.keySet().toArray(new String[0]);
			for(int index=0; index<ic.length; index++)
			{
				ic[index] = lc.addNewInvolvementClaim();
				String key = keys[index];
				String id = key.substring(0, key.indexOf("_"));
				String name = key.substring(key.indexOf("_") + 1);
				ArrayList ic_arr = ICMap.get(key);
				int score = ((Integer)ic_arr.get(0)).intValue();
				String ic_evidence = (String)ic_arr.get(1);
				ic[index].setClaimId(id);
				DuParticipant participants_ic = ic[index].addNewParticipant1();
				participants_ic.setParticipantId(id);
				if (name.startsWith("ils") ||
				    !userNms.containsKey(name)) { participants_ic.setParticipantName(name); }
				else {
				    participants_ic.setParticipantName(userNms.get(name));
				    ic_evidence = ic_evidence.replaceAll(name, userNms.get(name));
				}
				ic[index].setParticipant1(participants_ic);
				ic[index].setLuValue(new BigInteger(String.valueOf(score)));
				LucStatement ic_ls = LucStatement.Factory.newInstance();
				ic_ls.addEvidence(ic_evidence);
				ic[index].setLucEvidence(ic_ls);
			}
			lc.setInvolvementClaimArray(ic);
		}
		else
			System.err.println("lc is null, please call initClaim() first");
	}
	
	public void createReport()
	{
		File xmlFile = new File(filename);
		try {
			if(sys != null)				
			{sys.save(xmlFile);}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		    System.out.println("filename: " + filename);
			e.printStackTrace();
		}
	}
	// simply print out the XML file content
	public void showSystemReport()
	{
		try {
			File xmlFile = new File(filename);
			SystemReport doc = SystemReportDocument.Factory.parse(xmlFile).getSystemReport();
			
			// get PerformerTeamId
			// Enum performerTeamId = doc.getPerformerTeamId();
			// System.out.println("Performer Team ID: " + performerTeamId);
			
			// get DuDescription
			DataUnit dataUnits = doc.getDuDescription();
			System.out.println("Data Unit Description:");
			System.out.println("DuId: " + dataUnits.getDuId());
			System.out.println("DuName: " + dataUnits.getDuName());
			
			// get DuParticipantistList
			DuParticipant[] participants = doc.getDuParticipantList().getParticipantArray();
			System.out.println("DU Participant List:");
			for(int i=0; i<participants.length; i++)
			{
				System.out.println("Participant ID: " + participants[i].getParticipantId());
				System.out.println("Participant Name: " + participants[i].getParticipantName());
			}
			BigInteger participantCount = doc.getDuParticipantList().getParticipantCount();
			System.out.println("Total number of participants: " + participantCount);
			
			// get LuClaimSet
			LuClaimSet set = doc.getLuClaimSet();
			System.out.println("Lu Claim Set:");
			// Claim Count
			System.out.println("Claim Count: " + set.getClaimCount());
			
			// get TopicControlClaim
			TopicControlClaim[] tocc = set.getTopicControlClaimArray();
			System.out.println("Topic Control Claim:");
			for(int i=0; i<tocc.length; i++)
			{
				System.out.println("Claim ID:" + tocc[i].getClaimId());
				System.out.println("Participant ID:" + tocc[i].getParticipant1().getParticipantId());
				System.out.println("Participant Name:" + tocc[i].getParticipant1().getParticipantName());
				System.out.println("LU Value:" + tocc[i].getLuValue());
				System.out.println("LUC Evidence:" + tocc[i].getLucEvidence());
				System.out.println();
			}
			System.out.println("Topic Control Ends:");
			System.out.println();
			
			// get TaskControlClaim
			TaskControlClaim[] tacc = set.getTaskControlClaimArray();
			System.out.println("Task Control Claim:");
			for(int i=0; i<tacc.length; i++)
			{
				System.out.println("Claim ID:" + tacc[i].getClaimId());
				System.out.println("Participant ID:" + tacc[i].getParticipant1().getParticipantId());
				System.out.println("Participant Name:" + tacc[i].getParticipant1().getParticipantName());
				System.out.println("LU Value:" + tacc[i].getLuValue());
				System.out.println("LUC Evidence:" + tacc[i].getLucEvidence());	
				System.out.println();
			}
			System.out.println("Task Control Ends");
			System.out.println();
			
			// get ExpressiveDisagreementClaim
			ExpressiveDisagreementClaim[] edc = set.getExpressiveDisagreementClaimArray();
			System.out.println("Expressive Disagreement Claim:");
			for(int i=0; i<edc.length; i++)
			{
				System.out.println("Claim ID:" + edc[i].getClaimId());
				System.out.println("Participant ID:" + edc[i].getParticipant1().getParticipantId());
				System.out.println("Participant Name:" + edc[i].getParticipant1().getParticipantName());
				System.out.println("LU Value:" + edc[i].getLuValue());
				System.out.println("LUC Evidence:" + edc[i].getLucEvidence());
				System.out.println();
			}	
			System.out.println("Expressive Disagreement Ends");
			System.out.println();
			
			// get InvolvementClaim
			InvolvementClaim[] ic = set.getInvolvementClaimArray();
			System.out.println("Involvement Claim:");
			for(int i=0; i<ic.length; i++)
			{
				System.out.println("Claim ID:" + ic[i].getClaimId());
				System.out.println("Participant ID:" + ic[i].getParticipant1().getParticipantId());
				System.out.println("Participant Name:" + ic[i].getParticipant1().getParticipantName());
				System.out.println("LU Value:" + ic[i].getLuValue());
				System.out.println("LUC Evidence:" + ic[i].getLucEvidence());
				System.out.println();
			}	
			System.out.println("Involvement Claim Ends");
			System.out.println();
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	// create a system report
//	public void createSystemReport()
//	{
//		SystemReportDocument sys = SystemReportDocument.Factory.newInstance();
//		SystemReport sysdoc = sys.addNewSystemReport();
		// add DuDescription
//		DataUnit du = sysdoc.addNewDuDescription();
		// Example to set Du Id and name
//		du.setDuId("DuID");
//		du.setDuName("DuName");
		
		// add DuParticipants
//		ParticipantList pl = sysdoc.addNewDuParticipantList();
		// Here's an example of setting a 3 participants list
//		BigInteger three = new BigInteger("3");
//		pl.setParticipantCount(three);
//		DuParticipant[] participants = new DuParticipant[3];
//		participants[0] = pl.addNewParticipant();
//		participants[0].setParticipantId("1");
//		participants[0].setParticipantName("Ken");
//		participants[1] = pl.addNewParticipant();
//		participants[1].setParticipantId("2");
//		participants[1].setParticipantName("Kerri");
//		participants[2] = pl.addNewParticipant();
//		participants[2].setParticipantId("3");
//		participants[2].setParticipantName("John");
//		pl.setParticipantArray(participants);
		
		// add Claim set
//		LuClaimSet lc = sysdoc.addNewLuClaimSet();
		// Example of setting LuClaimSet
		// add claim count
//		lc.setClaimCount(three);
			
		// set topic control
//		TopicControlClaim[] tocc = new TopicControlClaim[3];
//		tocc[0] = lc.addNewTopicControlClaim();
//		tocc[0].setClaimId("Topic Control Claim ID 1");	
//		DuParticipant participants_topic_1 = tocc[0].addNewParticipant1();
//		participants_topic_1.setParticipantId("1");
//		participants_topic_1.setParticipantName("Ken");
//		tocc[0].setParticipant1(participants_topic_1);
//		tocc[0].setLuValue(new BigInteger("1"));
//		LucStatement ls = tocc[0].addNewLucEvidence();
//		ls.set(arg0)
//		tocc[0].setLucEvidence((LucStatement)("good"));
		/*
		tocc[1] = lc.addNewTopicControlClaim();
		tocc[1].setClaimId("Topic Control Claim ID 2");
		DuParticipant participants_topic_2 = tocc[1].addNewParticipant1();
		participants_topic_2.setParticipantId("2");
		participants_topic_2.setParticipantName("Kerri");
		tocc[1].setParticipant1(participants_topic_2);
		tocc[1].setLuValue(new BigInteger("2"));
		
		tocc[2] = lc.addNewTopicControlClaim();
		tocc[2].setClaimId("Topic Control Claim ID 3");
		DuParticipant participants_topic_3 = tocc[2].addNewParticipant1();
		participants_topic_3.setParticipantId("3");
		participants_topic_3.setParticipantName("Jessica");
		tocc[2].setParticipant1(participants_topic_3);
		tocc[2].setLuValue(new BigInteger("3"));
		*/
//		lc.setTopicControlClaimArray(tocc);
		
		// set Task Control Claim	
		/*
		TaskControlClaim[] tacc = new TaskControlClaim[3];
		tacc[0] = lc.addNewTaskControlClaim();
		tacc[0].setClaimId("Task Control Claim ID 1");	
		DuParticipant participants_task_1 = tacc[0].addNewParticipant1();
		participants_task_1.setParticipantId("1");
		participants_task_1.setParticipantName("Ken");
		tacc[0].setParticipant1(participants_task_1);
		tacc[0].setLuValue(new BigInteger("1"));
		
		tacc[1] = lc.addNewTaskControlClaim();
		tacc[1].setClaimId("Task Control Claim ID 2");	
		DuParticipant participants_task_2 = tacc[1].addNewParticipant1();
		participants_task_2.setParticipantId("2");
		participants_task_2.setParticipantName("Kerri");
		tacc[1].setParticipant1(participants_task_2);
		tacc[1].setLuValue(new BigInteger("2"));
		
		tacc[2] = lc.addNewTaskControlClaim();
		tacc[2].setClaimId("Task Control Claim ID 3");
		DuParticipant participants_task_3 = tacc[2].addNewParticipant1();
		participants_task_3.setParticipantId("3");
		participants_task_3.setParticipantName("Jessica");
		tacc[2].setParticipant1(participants_task_3);
		tacc[2].setLuValue(new BigInteger("3"));
		
		lc.setTaskControlClaimArray(tacc);
		*/
		
		// set Expressive Disagreement Claim
		/*
		ExpressiveDisagreementClaim[] edc = new ExpressiveDisagreementClaim[3];
		edc[0] = lc.addNewExpressiveDisagreementClaim();
		edc[0].setClaimId("Expressive Disagreement Claim ID 1");	
		DuParticipant participants_exp_1 = edc[0].addNewParticipant1();
		participants_exp_1.setParticipantId("1");
		participants_exp_1.setParticipantName("Ken");
		edc[0].setParticipant1(participants_exp_1);
		edc[0].setLuValue(new BigInteger("1"));
		
		edc[1] = lc.addNewExpressiveDisagreementClaim();
		edc[1].setClaimId("Expressive Disagreement Claim ID 2");
		DuParticipant participants_exp_2 = edc[1].addNewParticipant1();
		participants_exp_2.setParticipantId("2");
		participants_exp_2.setParticipantName("Kerri");
		edc[1].setParticipant1(participants_exp_2);
		edc[1].setLuValue(new BigInteger("2"));
		
		edc[2] = lc.addNewExpressiveDisagreementClaim();
		edc[2].setClaimId("Expressive Disagreement Claim ID 3");
		DuParticipant participants_exp_3 = edc[2].addNewParticipant1();
		participants_exp_3.setParticipantId("3");
		participants_exp_3.setParticipantName("Jessica");
		edc[2].setParticipant1(participants_exp_3);
		edc[2].setLuValue(new BigInteger("3"));
		
		lc.setExpressiveDisagreementClaimArray(edc);
		*/
		
		// set Involvement Claim	
		/*
		InvolvementClaim[] ic = new InvolvementClaim[3];
		ic[0] = lc.addNewInvolvementClaim();
		ic[0].setClaimId("Involvement Claim ID 1");	
		DuParticipant participants_inv_1 = ic[0].addNewParticipant1();
		participants_inv_1.setParticipantId("1");
		participants_inv_1.setParticipantName("Ken");
		ic[0].setParticipant1(participants_inv_1);
		ic[0].setLuValue(new BigInteger("1"));
		
		ic[1] = lc.addNewInvolvementClaim();
		ic[1].setClaimId("Involvement Claim ID 2");
		DuParticipant participants_inv_2 = ic[1].addNewParticipant1();
		participants_inv_2.setParticipantId("2");
		participants_inv_2.setParticipantName("Kerri");
		ic[1].setParticipant1(participants_inv_2);
		ic[1].setLuValue(new BigInteger("2"));
		
		ic[2] = lc.addNewInvolvementClaim();
		ic[2].setClaimId("Involvement Claim ID 3");
		DuParticipant participants_inv_3 = ic[2].addNewParticipant1();
		participants_inv_3.setParticipantId("3");
		participants_inv_3.setParticipantName("Jessica");
		ic[2].setParticipant1(participants_inv_3);
		ic[2].setLuValue(new BigInteger("3"));
		
		lc.setInvolvementClaimArray(ic);
		
		File xmlFile = new File(filename);
		try {
			sys.save(xmlFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
//	}
	
	/*
	public static void main(String args[])
	{		
		String filename = "D:\\xml\\example.xml";
		PtsReportXMLParser parser = new PtsReportXMLParser(filename);
		parser.createSystemReport();
		parser.showSystemReport();
	}
*/	
}

