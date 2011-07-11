/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

/**
 *
 * @author Laura G.H. Jiao
 */
public class DsarmdDATag {

    public static final String AS = "assertion-opinion";
    public static final String CO = "conventional-opening";
    public static final String CC = "conventional-closing";
    public static final String IR = "information-request";
    public static final String CR = "confirmation-request";
    public static final String RA = "response-answer";
    public static final String RN = "response-non-answer";
    public static final String OC = "offer-commit";
    public static final String AK = "acknowledge";
    public static final String AA = "agree-accept";
    public static final String DR = "disagree-reject";
    public static final String CM = "correct-misspelling";
    public static final String AD = "action-directive";
    public static final String OP = "other-conventional-phrase";
    public static final String SN = "signal-non-understanding";

    public static final String TASK = "task";
    public static final String COMM_MGMT = "comm_mgmt";
    public static final String PRCS_MGMT = "prcs_mgmt";

    
    public static int getTagAliasNum(String tag, int tagNum){
        if(tagNum == 15){
            if(tag.toLowerCase().contains(AS))
                return 1;
            else if(tag.toLowerCase().contains(CO))
                return 2;
            else if(tag.toLowerCase().contains(CC))
                return 3;
            else if(tag.toLowerCase().contains(IR))
                return 4;
            else if(tag.toLowerCase().contains(CR))
                return 5;
            else if(tag.toLowerCase().contains(RA))
                return 6;
            else if(tag.toLowerCase().contains(RN))
                return 7;
            else if(tag.toLowerCase().contains(OC))
                return 8;
            else if(tag.toLowerCase().contains(AK))
                return 9;
            else if(tag.toLowerCase().contains(AA))
                return 10;
            else if(tag.toLowerCase().contains(DR))
                return 11;
            else if(tag.toLowerCase().contains(CM) ||
                    tag.toLowerCase().contains("correct-mispelling"))
                return 12;
            else if(tag.toLowerCase().contains(AD))
                return 13;
            else if(tag.toLowerCase().contains(OP))
                return 14;
            else if(tag.toLowerCase().contains(SN))
                return 15;
            else
                return 16;
        }

        else if(tagNum == 3){
            if(tag.toLowerCase().contains(TASK))
                return 1;
            else if(tag.toLowerCase().contains(COMM_MGMT))
                return 2;
            else if(tag.toLowerCase().contains(PRCS_MGMT))
                return 3;
            else
                return 4;
        }

        else
            System.err.println("Error in tagNum specification !");
            return 0;
    }

    public static String getTagName(int alias, int tagNum){
        String name = "";
        if(tagNum == 15){
            if(alias == 1)
                name = AS;
            else if(alias == 2)
                name = CO;
            else if(alias == 3)
                name = CC;
            else if(alias == 4)
                name = IR;
            else if(alias == 5)
                name = CR;
            else if(alias == 6)
                name = RA;
            else if(alias == 7)
                name = RN;
            else if(alias == 8)
                name = OC;
            else if(alias == 9)
                name = AK;
            else if(alias == 10)
                name = AA;
            else if(alias == 11)
                name = DR;
            else if(alias == 12)
                name = CM;
            else if(alias == 13)
                name = AD;
            else if(alias == 14)
                name = OP;
            else if(alias == 15)
                name = SN;
            else
                name = "unknown";
        }
        else if(tagNum == 3){
            if(alias == 1)
                name = TASK;
            else if(alias == 2)
                name = COMM_MGMT;
            else if(alias == 3)
                name = PRCS_MGMT;
            else
                name = "unknown";
        }
        return name;
    }
}
