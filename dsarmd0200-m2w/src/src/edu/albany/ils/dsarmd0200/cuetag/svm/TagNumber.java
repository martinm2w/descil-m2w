/*
 * This class is used to parse between tags and their Integer numbers
 * The Integer tag numbers are required in the SVM input formats
 */

package edu.albany.ils.dsarmd0200.cuetag.svm;

/**
 *
 * @author Laura G.H. Jiao
 */
public class TagNumber {


    public int tagNumberCommAct(String tag) {

        if(tag.toLowerCase().trim().equals("addressed-to"))
            return 1;
        else if(tag.toLowerCase().trim().equals("response-to"))
            return 2;
        else if(tag.toLowerCase().trim().equals("continuation-of"))
            return 3;
        else
            return 4;
    }

    public String tagNumberCommAct(int tagNum) {

        if(tagNum == 1)
            return "addressed-to";
        else if(tagNum == 2)
            return "response-to";
        else if(tagNum == 3)
            return "continuation-of";
        else
            return "unknown";
    }

    public int tagNumberPolarity(String tag) {
        
        if(tag.toLowerCase().trim().equals("positive"))
            return 1;
        else if(tag.toLowerCase().trim().equals("negative"))
            return 2;
        else if(tag.toLowerCase().trim().equals("neutral")
                || tag == null
                || tag.trim().equals(""))
            return 3;
        else
            return 4; // unknow tag
    }

    public String tagNumerPolarity(int tagNum) {

        if(tagNum == 1)
            return "positive";
        else if(tagNum == 2)
            return "negative";
        else if(tagNum == 3)
            return "neutral";
        else
            return "unknown";
    }

    public int tagNumberDialogAct(String tag, int tagNum){

        tag = tag.toLowerCase();
        int value = 0;
        if(tagNum == 15){
            if(tag.toLowerCase().contains("explanation") ||
                    tag.toLowerCase().contains("assertion-opinion"))
                value = 1;
            else if(tag.toLowerCase().contains("offer-commit"))
                value = 2;
            else if(tag.toLowerCase().contains("acknowledge"))
                value = 3;
            else if(tag.toLowerCase().contains("signal-non-understanding"))
                value = 4;
            else if(tag.toLowerCase().contains("response-answer") ||
                    tag.toLowerCase().contains("positive-answer"))
                value = 5;
            else if(tag.toLowerCase().contains("response-non-answer") ||
                    tag.toLowerCase().contains("negative-answer"))
                value = 6;
            else if(tag.toLowerCase().contains("agree-accept") ||
                    tag.toLowerCase().contains("qualified-agree"))
                value = 7;
            else if(tag.toLowerCase().contains("disagree-reject") ||
                    tag.toLowerCase().contains("qualified-disagree"))
                value = 8;
            else if(tag.toLowerCase().contains("information-request"))
                value = 9;
            else if(tag.toLowerCase().contains("confirmation-request"))
                value = 10;
            else if(tag.toLowerCase().contains("action-directive") ||
                    tag.toLowerCase().contains("open-question-option"))
                value = 11;
            else if(tag.toLowerCase().contains("conventional-opening"))
                value = 12;
            else if(tag.toLowerCase().contains("conventional-closing"))
                value = 13;
            else if(tag.toLowerCase().contains("communication-management") ||
                    tag.toLowerCase().contains("other-conventional-phrase"))
                value = 14;
            else if(tag.toLowerCase().contains("correct-mispelling") ||
                    tag.contains("correct-misspelling"))
                value = 15;
            else // unknown-label
                value = 16;
        }

        else if(tagNum == 33){
            if(tag.equals("task:agree-accept"))
               value = 1;
            else if(tag.equals("comm-mgmt:conventional-opening"))
               value = 2;
            else if(tag.equals("prcs-mgmt:information-request"))
               value = 3;
            else if(tag.equals("prcs-mgmt:assertion-opinion"))
               value = 4;
            else if(tag.equals("comm-mgmt:assertion-opinion"))
               value = 5;
            else if(tag.equals("prcs-mgmt:response-answer"))
               value = 6;
            else if(tag.equals("task:assertion-opinion"))
               value = 7;
            else if(tag.equals("comm-mgmt:acknowledge"))
               value = 8;
            else if(tag.equals("task:information-request"))
               value = 9;
            else if(tag.equals("task:acknowledge"))
               value = 10;
            else if(tag.equals("task:disagree-reject"))
               value = 11;
            else if(tag.equals("task:response-non-answer"))
               value = 12;
            else if(tag.equals("task:response-answer"))
               value = 13;
            else if(tag.equals("comm-mgmt:other-conventional-phrase"))
               value = 14;
            else if(tag.equals("task:action-directive"))
               value = 15;
            else if(tag.equals("task:confirmation-request"))
               value = 16;
            else if(tag.equals("comm-mgmt:correct-misspelling"))
               value = 17;
            else if(tag.equals("comm-mgmt:information-request"))
               value = 18;
            else if(tag.equals("comm-mgmt:response-answer"))
               value = 19;
            else if(tag.equals("task:signal-non-understanding"))
               value = 20;
            else if(tag.equals("prcs-mgmt:agree-accept"))
               value = 21;
            else if(tag.equals("comm-mgmt:signal-non-understanding"))
               value = 22;
            else if(tag.equals("comm-mgmt:agree-accept"))
               value = 23;
            else if(tag.equals("comm-mgmt:conventional-closing"))
               value = 24;
            else if(tag.equals("comm-mgmt:confirmation-request"))
               value = 25;
            else if(tag.equals("comm-mgmt:disagree-reject"))
               value = 26;
            else if(tag.equals("prcs-mgmt:response-non-answer"))
               value = 27;
            else if(tag.equals("comm-mgmt:response-non-answer"))
               value = 28;
            else if(tag.equals("prcs-mgmt:action-directive"))
               value = 29;
            else if(tag.equals("prcs-mgmt:acknowledge"))
               value = 30;
            else if(tag.equals("comm-mgmt:action-directive"))
               value = 31;
            else if(tag.equals("prcs-mgmt:confirmation-request"))
               value = 32;
            else if(tag.equals("prcs-mgmt:disagree-reject"))
               value = 33;
            else
               value = 34;
        }

        else if(tagNum == 3){
            if(tag.contains("task"))
                value = 1;
            else if(tag.contains("prcs-mgmt") ||
                    tag.contains("task-mgmt"))
                value = 2;
            else if(tag.contains("comm-mgmt"))
                value = 3;
            else
                value = 4;
        }


        if(value == 0){
            System.err.println("Tag number parse failed !");
        }
        return value;
    }

    public String tagNumberDialogAct(int tag, int tagNum){

        String value = "";
        if(tagNum == 12){

        /* for 12 tags */
        if(tag == 1)
            value = "assertion-opinion";
        else if(tag == 2)
            value = "offer-commit";
        else if(tag == 3)
            value = "acknowledge";
        else if(tag == 4)
            value = "signal-non-understanding";
        else if(tag == 5)
            value = "IRCR";
        else if(tag == 6)
            value = "open-question-option";
        else if(tag == 7)
            value = "action-directive";
        else if(tag == 8)
            value = "conversation-norms";
        else if(tag == 9)
            value = "positive-answer";
        else if(tag == 10)
            value = "negative-answer";
        else if(tag == 11)
            value = "AAQA";
        else if(tag == 12)
            value = "DRQD";
        else
            value = "unknown";
            
        }

        else if(tagNum == 15){

            /* for 15 tags */
            if(tag == 1)
                value = "assertion-opinion";
            else if(tag == 2)
                value = "offer-commit";
            else if(tag == 3)
                value = "acknowledge";
            else if(tag == 4)
                value = "signal-non-understanding";
            else if(tag == 5)
                value = "response-answer";
            else if(tag == 6)
                value = "response-non-answer";
            else if(tag == 7)
                value = "agree-accept";
            else if(tag == 8)
                value = "disagree-reject";
            else if(tag == 9)
                value = "information-request";
            else if(tag == 10)
                value = "confirmation-request";
            else if(tag == 11)
                value = "action-directive";
            else if(tag == 12)
                value = "conventional-opening";
            else if(tag == 13)
                value = "conventional-closing";
            else if(tag == 14)
                value = "other-conventional-phrase";
            else if(tag == 15)
                value = "correct-misspelling";
            else
                value = "unknown";
        }

        else if(tagNum == 43){

            /* for swbd corpus */
            if(tag == 1)
                value = "sd";
            else if(tag == 2)
                value = "b";
            else if(tag == 3)
                value = "sv";
            else if(tag == 4)
                value = "aa";
            else if(tag == 5)
                value = "percentage-";
            else if(tag == 6)
                value = "ba";
            else if(tag == 7)
                value = "qy";
            else if(tag == 8)
                value = "x";
            else if(tag == 9)
                value = "ny";
            else if(tag == 10)
                value = "fc";
            else if(tag == 11)
                value = "percentage";
            else if(tag == 12)
                value = "qy";
            else if(tag == 13)
                value = "nn";
            else if(tag == 14)
                value = "bk";
            else if(tag == 15)
                value = "h";
            else if(tag == 16)
                value = "qy^d";
            else if(tag == 17)
                value = "bc";
            else if(tag == 18)
                value = "bh";
            else if(tag == 19)
                value = "^q";
            else if(tag == 20)
                value = "bf";
            else if(tag == 21)
                value = "na";
            else if(tag == 22)
                value = "ad";
            else if(tag == 23)
                value = "^2";
            else if(tag == 24)
                value = "b^m";
            else if(tag == 25)
                value = "qo";
            else if(tag == 26)
                value = "qh";
            else if(tag == 27)
                value = "^h";
            else if(tag == 28)
                value = "ar";
            else if(tag == 29)
                value = "ng";
            else if(tag == 30)
                value = "br";
            else if(tag == 31)
                value = "no";
            else if(tag == 32)
                value = "fp";
            else if(tag == 33)
                value = "qrr";
            else if(tag == 34)
                value = "nd";
            else if(tag == 35)
                value = "t3";
            else if(tag == 36)
                value = "cc";
            else if(tag == 37)
                value = "t1";
            else if(tag == 38)
                value = "bd";
            else if(tag == 39)
                value = "aap/am";
            else if(tag == 40)
                value = "^g";
            else if(tag == 41)
                value = "qw^d";
            else if(tag == 42)
                value = "fa";
            else if(tag == 43)
                value = "ft";
            else
                value = "unknown";
        }

        if(value.equals("")){
            System.err.println("Tag string parse failed !");
        }
        return value;
    }

    /* Added for lauren_annotated files */
    public String DialogAct(String tag, int tagNum){

        String value = "";
	//System.out.println("tag number: " + tagNum);
        if(tagNum == 15){
        if(tag.toLowerCase().contains("explanation") ||
                tag.toLowerCase().contains("assertion-opinion"))
            value = "assertion-opinion";
        else if(tag.toLowerCase().contains("offer-commit"))
            value = "offer-commit";
        else if(tag.toLowerCase().contains("acknowledge"))
            value = "acknowledge";
        else if(tag.toLowerCase().contains("signal-non-understanding"))
            value = "signal-non-understanding";
        else if(tag.toLowerCase().contains("response-answer") ||
                tag.toLowerCase().contains("positive-answer"))
            value = "response-answer";
        else if(tag.toLowerCase().contains("response-non-answer") ||
                tag.toLowerCase().contains("negative-answer"))
            value = "response-non-answer";
        else if(tag.toLowerCase().contains("agree-accept") ||
                tag.toLowerCase().contains("qualified-agree"))
            value = "agree-accept";
        else if(tag.toLowerCase().contains("disagree-reject") ||
                tag.toLowerCase().contains("qualified-disagree"))
            value = "disagree-reject";
        else if(tag.toLowerCase().contains("information-request"))
            value = "information-request";
        else if(tag.toLowerCase().contains("confirmation-request"))
            value = "confirmation-request";
        else if(tag.toLowerCase().contains("action-directive") ||
                tag.toLowerCase().contains("open-question-option"))
            value = "action-directive";
        else if(tag.toLowerCase().contains("conventional-opening"))
            value = "conventional-opening";
        else if(tag.toLowerCase().contains("conventional-closing"))
            value = "conventional-closing";
        else if(tag.toLowerCase().contains("communication-management") ||
                tag.toLowerCase().contains("other-conventional-phrase"))
            value = "other-conventional-phrase";
        else if(tag.toLowerCase().contains("correct-mispelling") ||
                tag.toLowerCase().contains("correct-misspelling"))
            value = "correct-misspelling";
        else // unknown-label
            value = "unknown";
        }

        else if(tagNum == 3){
            if(tag.toLowerCase().contains("task"))
                value = "task";
            else if(tag.toLowerCase().contains("prcs-mgmt") ||
                    tag.toLowerCase().contains("task-mgmt"))
                value = "prcs-mgmt";
            else if(tag.toLowerCase().contains("comm-mgmt"))
                value = "comm-mgmt";
            else
                value = "unknown";
        }

        if(value.equals("")){
            System.err.println("tag no parse failed !");
            System.exit(0);
        }

        return value;
        
    }

}
