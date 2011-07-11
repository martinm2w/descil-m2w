package  edu.albany.ils.dsarmd0200.lu;

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import edu.albany.ils.dsarmd0200.evaltag.*;


public class PRM
{
    static int DIALOGUE_THRESHOLD = 150; //need to have at least 150 utterances in input dialogue to segment the dialogue
    static int MESOTOPIC_THRESHOLD = 3; //need to have at least 3 meso topics in input dialogue to segment the dialogue
    static int FILE_NUMBER = 0;
    static double TOPIC_C_WEIGHT = 1;
    static double TASK_C_WEIGHT = 0.9;
    static double AGR_WEIGHT = 0.7;
    static double DISAGR_WEIGHT = AGR_WEIGHT;
    static double INV_WEIGHT = 0.3;
    private double PRM = 0;

    private HashMap speakers = null;
    ArrayList mesotopics = null;

    PRM (File inFile)
    {
	//no need to have code here now 
	//since merged with Assertion Maker
    }

    PRM (HashMap sp, ArrayList mt)
    {
	speakers = sp;
	mesotopics = mt;
	//call to calculate PRM
	//calPRM ();
    }

    public void calPRM (int file_number)
    {
	FILE_NUMBER = file_number;
	calPRM ();
    }
    public double getPRM() { return PRM; }
    public void calPRM ()
    {
	//mesotopics
	MesoTopic mt = (MesoTopic) mesotopics.get (FILE_NUMBER);
	ArrayList mts = (ArrayList) mt.getMeso_topics_ ();
	
	int mts_size = mts.size ();
	int utts_size = mt.getUtts ().size ();

	if ((utts_size < DIALOGUE_THRESHOLD || mts_size < MESOTOPIC_THRESHOLD) && speakers != null)
	    {
		TreeMap topic_cMap = new TreeMap ();
		TreeMap task_cMap = new TreeMap ();
		TreeMap disMap = new TreeMap ();
		TreeMap agrMap = new TreeMap ();
		TreeMap invMap = new TreeMap ();
		
		Set keySet = speakers.keySet ();
		Iterator iter = keySet.iterator ();
		while (iter.hasNext ())
		    {
			String name = (String) iter.next ();
			if (name.startsWith ("ils")) continue;
			
			Speaker sp = (Speaker) speakers.get (name);			
			Leadership info = sp.getLeadershipInfo ();
			
			double topic_c = info.getTopicControl ();
			double task_c = info.getTaskControl ();
			double dis = info.getDisagreement ();
			double agr = sp.getATX ();
			double inv = info.getInvolvement ();
			
			topic_cMap.put (new Double (topic_c), sp);
			task_cMap.put (new Double (task_c), sp);
			disMap.put (new Double (dis), sp);
			agrMap.put (new Double (agr), sp);
			invMap.put (new Double (inv), sp);

			
		    }
		calDistance (topic_cMap, TOPIC_C_WEIGHT);
		calDistance (task_cMap, TASK_C_WEIGHT);
		calDistance (disMap, DISAGR_WEIGHT);
		calDistance (agrMap, AGR_WEIGHT);
		calDistance (invMap, INV_WEIGHT);

	    }
	PRM = 0;
	Set keySet = speakers.keySet ();
	Iterator iter = keySet.iterator ();
	while (iter.hasNext ())
	    PRM = PRM + ((Speaker) (speakers.get (iter.next ()))).getPRM ();
		  
	PRM = PRM/speakers.size ();
		
	//System.out.println ("*********GC - PRM**************");
	System.out.println ("@PRM:" + PRM);
    }

    public void calDistance (TreeMap map, double weight)
    {

	//do it for the highest speaker
	Double key = (Double) map.lastKey ();
	Speaker first = (Speaker) map.get (key);
	double last_value = key.doubleValue ();
       
	first.setPRM (weight);

	while (map.size () > 0)
	    {
		map.remove (key);
		if (map.size () == 0)
		    break;
		key = (Double) map.lastKey ();
		double value = key.doubleValue ();
		Speaker sp = (Speaker) map.get (key);
		if ((last_value - value) < 0.20)
		    sp.setPRM (weight);
		last_value = value;

	    }
    }
    
    public static void main (String args [])
    {
	//legacy main function
	//is not executed now
	File inFile = new File (args [0]);
	PRM prm = new PRM (inFile);
	
	
    }
}
