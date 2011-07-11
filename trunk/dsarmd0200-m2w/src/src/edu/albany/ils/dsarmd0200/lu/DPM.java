package  edu.albany.ils.dsarmd0200.lu;

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import edu.albany.ils.dsarmd0200.evaltag.*;


public class DPM
{
    
    private HashMap speakers = null;  //list of speakers
    private double DPM = 0;

    DPM (File inFile)
    {
	//no need to have code here now 
	//since merged with Assertion Maker
    }

    DPM (HashMap speakerMap)
    {
	speakers = speakerMap;
	//call to calculate DPM
	//calDPM ();
    }

    public void calDPM ()
    {
	TreeMap topic_cMap = new TreeMap ();
	TreeMap task_cMap = new TreeMap ();
	TreeMap disMap = new TreeMap ();
	TreeMap invMap = new TreeMap ();
	if (speakers != null)
	    {
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
			double inv = info.getInvolvement ();
			
			
			topic_cMap.put (new Double (topic_c), sp);
			task_cMap.put (new Double (task_c), sp);
			disMap.put (new Double (dis), sp);
			invMap.put (new Double (inv), sp);
			
			System.err.println (name + " " + sp.getTC ().getTL ());
		    }
		//calculate high/medium/low
		calHML (topic_cMap, false);
		calHML (task_cMap, false);
		calHML (disMap, false); 
		calHML (invMap, true);
		
		//now calculate DPM
		DPM = 0;
		keySet = speakers.keySet ();
		iter = keySet.iterator ();
		while (iter.hasNext ())
		    DPM = DPM + ((Speaker) (speakers.get (iter.next ()))).getDPM ();
		  
		DPM = DPM/speakers.size ();
		
		//System.out.println ("*********GC - DPM**************");
		System.out.println ("@DPM:" + DPM);
 
	    }
    }

    public double getDPM() { return DPM; }

    //calculate high/medium/low for each speaker on LUs
    public void calHML (TreeMap map, boolean isInv)
    {

	double HIGH = 1;
	double MEDIUM = 0.5;
	double LOW = 0;
	if (isInv)
	    {
		HIGH = 0.8;
		MEDIUM = 0.4;
	    }

	Double first = (Double) map.lastKey ();
	Speaker sp_first = (Speaker) map.get (first);

	//set 0 for lowest speaker
	Double last = (Double) map.firstKey ();
	Speaker sp_last = (Speaker) map.get (last);

	if (last.doubleValue () == first.doubleValue() )
	    //means all have same value
	    {
		Collection values_c = map.values ();
		ArrayList values = new ArrayList (values_c);
		if (last.doubleValue () == 0) //maybe found no disagreement or task control
		    {
			//in this case set every body's scores to 0
			for (int i = 0; i < values.size (); i++)
			    ((Speaker) values.get (i)).setDPM (LOW);
		    }
		else //last value is *not* zero, yet is same as first value
		    {
			//here set everyones score to 1
			for (int i = 0; i < values.size (); i++)
			    ((Speaker) values.get (i)).setDPM (HIGH); 
		    }
	    }
	else //there is variation
	    {
		sp_first.setDPM (1);
		sp_last.setDPM (0);
		//find the second highest person
		map.remove (first);
		map.remove (last);

		if (map.size () > 1)
		    {
			Double second = (Double) map.lastKey ();
			Speaker sp_second = (Speaker) map.get (second);
			
			//how much difference is there
			double diff = first.doubleValue () - second.doubleValue ();
			if (diff < 0.20) //close by 20%, so still high
 			    sp_second.setDPM (HIGH);
			else //middle
			    sp_second.setDPM (MEDIUM);

			map.remove (second);

			//for the rest, set score to middle
			if (map.size () >= 1)
			    {
				Collection values_c = map.values ();
				ArrayList values =  new ArrayList (values_c);
				for (int i = 0; i < values.size (); i++)
				    ((Speaker) values.get (i)).setDPM (MEDIUM);
			    }
		    }
		else if (map.size () == 1) //if only one left 
		    {
			Double middle = (Double) map.lastKey ();
			Speaker sp_middle = (Speaker) map.get (middle);
			sp_middle.setDPM (MEDIUM);
		    }
		
	    }
    }
    
    
    public static void main (String args [])
    {
	//legacy main function
	//is not executed now
	File inFile = new File (args [0]);
	DPM dpm = new DPM (inFile);
	
	
    }
}
