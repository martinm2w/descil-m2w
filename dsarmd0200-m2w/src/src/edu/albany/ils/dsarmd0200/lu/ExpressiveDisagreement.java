package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ting Liu
 * @Date: 01/08/2010
 * This class is used to judge which particants in a 
 * small discussion group are most expressively desagreed
 * to each other
 */

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;


public class ExpressiveDisagreement {

    public ExpressiveDisagreement(ArrayList utts) {
	utts_ = utts;
    }
    /*********************************get attributes**********************/

    /*********************************set attributes**********************/
    public void calDRI() {
	DisagreeRejectIndex dri = new DisagreeRejectIndex(utts_);
	dri.calDRI();
    }

    public void calCEDI(PtsReportXMLParser prxmlp, ArrayList spks,
			HashMap<String, Speaker> parts) {
	CumulativeEDisagreementIndex cedi = new CumulativeEDisagreementIndex(utts_);
	cedi.calCEDI(prxmlp, spks, parts);
    }
    /*******************************   Attributes  **************************/
    HashMap<String, Speaker> spks_ = null; //the speaker who has this control
    ArrayList utts_ = null;
}
