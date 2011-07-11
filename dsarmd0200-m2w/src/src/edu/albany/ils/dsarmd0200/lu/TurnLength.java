package edu.albany.ils.dsarmd0200.lu;

/**
 * @Author: Ken
 * @Date: 01/08/2010
 * This class is used to calculate the average turn length 
 * of the participants
 */

import java.io.*;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.util.*;

public class TurnLength {
    public TurnLength(Document doc) {
    }

    
    public void calculateTLs() {
	
    }

    public double getTL(String usr_id) {
	return 0;
    }

    public double getMedianTL() {
	return 0;
    }

    public int getTLRank(String usr_id) {
	return 0;
    }

    private HashMap tls_ = new HashMap(); //key is usr_id, value is average turn length
    private Document doc_ = null;
}
