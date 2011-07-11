package edu.albany.ils.dsarmd0200.util;

import java.util.Properties;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.io.*;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.albany.ils.dsarmd0200.lu.*;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;

/**
 *
 * Author: Ken Stahl
 * just an interface for Stanford POS tagger, a place to keep the initialization
 * 
 * @update: 5/26/11 2:06 PM - - m2w : added chinese tagging.
 * 
 *
 */
public abstract class StanfordPOSTagger{
	
	public static MaxentTagger mt;
        static CRFClassifier classifier;
         // for chinese tagging 5/26/11 2:12 PM
        
	public static void initialize()
	{
//            System.out.print("init english");
	try {
	    mt = new MaxentTagger(Settings.getValue(Settings.POS_ENGLISH_MODEL));
		//System.out.println("===========================");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	
	public static String tagString(String param){
		String retval = "";
		try{
			
			retval = mt.tagString(param);
		} catch (Exception e) {
			System.err.println("Error tagging " + param);
			e.printStackTrace();
		}
		return retval;
	}
        
        //m2w: chinese initiallizer. 5/18/11 12:47 PM
        public static void initializeChinese()
	{
//            System.out.println("initializing chinese");
	try {
             
                    mt = new MaxentTagger(Settings.getValue(Settings.POS_CHINESE_MODEL));
                   
                    
                    Properties props = new Properties();
                    props.setProperty("sighanCorporaDict", Settings.getValue(Settings.CHINESE_SEG_DATA_DIR));
                    // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
                    // props.setProperty("normTableEncoding", "UTF-8");
                    // below is needed because CTBSegDocumentIteratorFactory accesses it
                    props.setProperty("serDictionary", Settings.getValue(Settings.CHINESE_SEG_DATA_DIR) + "/dict-chris6.ser.gz");
                //    props.setProperty("testFile", "/home/-ruobo/Desktop/1");
                    props.setProperty("inputEncoding", "UTF-8");
                    props.setProperty("sighanPostProcessing", "true");

                    classifier = new CRFClassifier(props);
                    classifier.loadClassifierNoExceptions(Settings.getValue(Settings.CHINESE_SEG_DATA_DIR) + "/ctb.gz", props);
                    // flags must be re-set after data is loaded
                    classifier.flags.setProperties(props);

                    
                    
            //classifier ready to use.
            
		//System.out.println("===========================");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
        
        public static String tagChineseString(String param){
		String forTagging = "";
                String retval = "";
                    
//                System.err.println("string input : " + param);
                List segList = classifier.segmentString(param);
                for(Object a : segList){
                    forTagging += (String)a + " ";
                }
//                 System.out.println("seged: :" + forTagging);
                
                try{
                    retval = mt.tagString(forTagging);
                        
		} catch (Exception e) {
			System.err.println("Error tagging " + param);
			e.printStackTrace();
		}
		return retval;
	}
        
        
        
        
}
