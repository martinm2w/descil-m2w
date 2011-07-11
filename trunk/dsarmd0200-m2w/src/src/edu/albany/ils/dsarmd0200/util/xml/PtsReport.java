package edu.albany.ils.dsarmd0200.util.xml;

import java.util.ArrayList;
import java.util.HashMap;

/*
 *  Xin
 *  Pts report consists of 3 parts
 *  DataUnit: Defines the data unit is being analyzed, it consists of DuId and DuName
 *  DuParticipants: Defines the list of participant in the DU, each one has a ParticipantId and a ParticipantName
 *  LuCliamSet: Defines set of LU claims that are being made of the DU. This type has one attribute, ClaimCount,
 *  			which records the total number of claims made. For the ALB team, there are four types of claim:
 *  	TopicControlClaim
 *  	TaskControlClaim
 *  	ExpressiveDisagreementClaim
 *  	InvolvementClaim
 */
public interface PtsReport 
{
	// set the data unit
	void setDataUnit(String id, String name);
	// set the DuParticipants
	void setDuParticipants(ArrayList<String> arr);
	// must be called before start setting the claims
	void initClaim();
	// set topic control claim
    void setTopicControl(HashMap<String,ArrayList> topicMap);
	// set task control claim
	void setTaskControl(HashMap<String,ArrayList> taskMap);
	// set expressive disagreement claim
	void setExpressiveDisagreementClaim(HashMap<String,ArrayList> EDCMap);
	// set involvement claim
	void setInvolvementClaim(HashMap<String,ArrayList> ICMap);
	// create the system report, must be called after all settings
	void createReport();
	// parse the XML report format, print out all contents
	void showSystemReport();
}
