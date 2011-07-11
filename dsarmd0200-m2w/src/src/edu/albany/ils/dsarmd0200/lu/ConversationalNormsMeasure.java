
package edu.albany.ils.dsarmd0200.lu;

// author Laura G.H. Jiao

import edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash.DsarmdDATag;
import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.util.xml.DsarmdDP;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Document;

public class ConversationalNormsMeasure {

    private ArrayList<Utterance> utts = new ArrayList<Utterance>();

    // key: Turn no; value: Dialog Act
    private HashMap<String, String> turnNo_DA = new HashMap<String, String>();
    // Key: Dialog Act; value: List of turns
    private HashMap<String, ArrayList<Utterance>> CNM_utts = new HashMap<String, ArrayList<Utterance>>();
    // key: Dialog Act; value: List of response turns to this da tag
    private HashMap<String, ArrayList<Utterance>> CNM_responses = new HashMap<String, ArrayList<Utterance>>();

    DsarmdDP dp = new DsarmdDP();
    
    
    public ConversationalNormsMeasure(ArrayList<Utterance> utts){
        this.utts = utts;
        loadMaps();
    }

    public ConversationalNormsMeasure(String filePath){
        File fl = new File(filePath);
        Document doc = dp.getDocument(fl.getAbsolutePath());
        ArrayList<Utterance> list = dp.parseDAList(doc, "dsarmd", false, null);
        this.utts = list;
        loadMaps();
    }

    /*
     * Save tag freq, responses freq
     */
    private void loadMaps(){
        for(Utterance utt : utts){
            String daTag = utt.getTag().toLowerCase();
            daTag = daTag.replace("--", "");
            String commAct = utt.getCommActType();
            String turnNo = utt.getTurn();

            /* load turnNo_DA map */
            turnNo_DA.put(turnNo, daTag);

            /* load CNM_utts map */
            if(CNM_utts.containsKey(daTag)){
                ArrayList<Utterance> tmp = CNM_utts.get(daTag);
                tmp.add(utt);
                CNM_utts.put(daTag, tmp);
            }
            else{
                ArrayList<Utterance> tmp = new ArrayList<Utterance>();
                tmp.add(utt);
                CNM_utts.put(daTag, tmp);
            }

            /* load CNM_responses map */
            if(commAct.toLowerCase().trim().equals("response-to")){
                String linkToStr = utt.getRespTo();
                if(linkToStr != null){
                    String[] linkToStrArray = linkToStr.split(":");
                    if(linkToStrArray.length > 1){
                        String linkToTurnNo = linkToStrArray[1];
                        String responseToDaTag = turnNo_DA.get(linkToTurnNo);
                        if(CNM_responses.containsKey(responseToDaTag)){
                            ArrayList<Utterance> tmp = CNM_responses.get(responseToDaTag);
                            tmp.add(utt);
                            CNM_responses.put(responseToDaTag, tmp);
                        }
                        else {
                            ArrayList<Utterance> tmp = new ArrayList<Utterance>();
                            tmp.add(utt);
                            CNM_responses.put(responseToDaTag, tmp);
                        }
                    }
                }
            }

        }
    }

    
    /*
     * Calculate CNM for this corpus
     */
    public double getCNM(){
        double result = 0.0;

        int irNum = 0, crNum = 0, adNum = 0, ocNum = 0;
        int irRes = 0, crRes = 0, adRes = 0, ocRes = 0;
        /* tag occurances */
        if(CNM_utts.containsKey(DsarmdDATag.IR)){
            irNum = CNM_utts.get(DsarmdDATag.IR).size();
        }
        if(CNM_utts.containsKey(DsarmdDATag.CR)){
            crNum = CNM_utts.get(DsarmdDATag.CR).size();
        }
        if(CNM_utts.containsKey(DsarmdDATag.AD)){
            adNum = CNM_utts.get(DsarmdDATag.AD).size();
        }
        if(CNM_utts.containsKey(DsarmdDATag.OC)){
            ocNum = CNM_utts.get(DsarmdDATag.OC).size();
        }
        /* tag responses occurances */
        if(CNM_responses.containsKey(DsarmdDATag.IR)){
            irRes = CNM_responses.get(DsarmdDATag.IR).size();
        }
        if(CNM_responses.containsKey(DsarmdDATag.CR)){
            crRes = CNM_responses.get(DsarmdDATag.CR).size();
        }
        if(CNM_responses.containsKey(DsarmdDATag.AD)){
            adRes = CNM_responses.get(DsarmdDATag.AD).size();
        }
        if(CNM_responses.containsKey(DsarmdDATag.OC)){
            ocRes = CNM_responses.get(DsarmdDATag.OC).size();
        }

        int daTagNum = irNum + crNum + adNum + ocNum;
        int responsesDaTagNum = irRes + crRes + adRes + ocRes;
//System.out.println("total: " + daTagNum + "     responses: " + responsesDaTagNum);
        result = (double)responsesDaTagNum/(double)daTagNum;

        return result;
    }

    /*
     * Calculate CNM for this tag, in this corpus
     */
    public double getCNMMeasures(String daTag){

        double result = 0.0;

        if(CNM_utts.containsKey(daTag) && CNM_responses.containsKey(daTag)){

            int daTagNum = CNM_utts.get(daTag).size();

            int responsesDaTagNum = CNM_responses.get(daTag).size();
//System.out.println("total: " + daTagNum + "     responses: " + responsesDaTagNum);
            result = (double)responsesDaTagNum/(double)daTagNum;

            return result;

        }        
        return result;        
    }
    

    public HashMap<String, String> getTurnNo_DA(){
        return turnNo_DA;
    }

    public HashMap<String, ArrayList<Utterance>> getCNM_utts(){
        return CNM_utts;
    }

    public HashMap<String, ArrayList<Utterance>> getCNM_responses(){
        return CNM_responses;
    }


    /* illustration purpose only*/
    public static void main(String[] args){
        String docs_path = "/home/jgh/Desktop/Human_Terrain_Studies/lauren_annotated/"
            + "GroundTruth/ground_truth/with_new_annotated/";
        ConversationalNormsMeasure CNM = new ConversationalNormsMeasure(docs_path + "Cheney_gt.xml");
        System.out.println("Laura debug: size of utts = " + CNM.utts.size());

        
        System.out.println("Laura debug: size of CNM_turnNoDA = " + CNM.getTurnNo_DA().size());
        System.out.println("Laura debug: size of CNM_utts = " + CNM.getCNM_utts().size());
        System.out.println("Laura debug: size of CNM_responses = " + CNM.getCNM_responses().size());

        System.out.println("CNM for 'information-request' = " + CNM.getCNMMeasures(DsarmdDATag.IR));
        System.out.println("CNM for 'conformation-request' = " + CNM.getCNMMeasures(DsarmdDATag.CR));
        System.out.println("CNM for 'offer-commit' = " + CNM.getCNMMeasures(DsarmdDATag.OC));
        System.out.println("CNM for 'action-directive' = " + CNM.getCNMMeasures(DsarmdDATag.AD));


        System.out.println("CNM = " + CNM.getCNM());
    }

}
