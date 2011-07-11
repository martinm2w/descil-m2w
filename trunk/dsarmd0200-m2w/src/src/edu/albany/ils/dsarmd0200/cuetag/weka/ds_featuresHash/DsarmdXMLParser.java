/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import java.util.ArrayList;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;

/**
 *
 * @author Laura G.H. Jiao
 */
public class DsarmdXMLParser {

    private final static String COMM_ACT_TYPE = "comm_act_type";
    private final static String DIALOG_ACT = "dialog_act";
    private final static String LINK_TO = "link_to";
    private final static String SPEAKER = "speaker";
    private final static String TOPIC = "topic";
    private final static String TURN_NO = "turn_no";


    /**
     * XML parser
     * @param xdml file path.
     * @return parsed document of the parsed xdml file.
     */
    public static Document DOMParser(String url){
        try{
            DOMParser parser = new DOMParser();
            parser.parse(url);
            Document document = parser.getDocument();
            return document;
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            return null;
        }
    }

    public static ArrayList<Utterance> getUttsArrayList(Document doc){

        ArrayList<Utterance> list = new ArrayList<Utterance>();

        NodeList nList = doc.getElementsByTagName("turn");
        for(int i = 0; i < nList.getLength(); i++){
            Node node = nList.item(i);
            String text = node.getTextContent().trim(); // content
            if(text.equals(null) || text.equals("")){
                text = "";
            }
            NamedNodeMap attrs = node.getAttributes();

            Attr attrib_comm_act_type = (Attr)attrs.getNamedItem(COMM_ACT_TYPE);
            String comm_act_type = attrib_comm_act_type.getValue();
            Attr attrib_da = (Attr)attrs.getNamedItem(DIALOG_ACT);
            String dialog_act = attrib_da.getValue();
            Attr attrib_link_to = (Attr)attrs.getNamedItem(LINK_TO);
            String link_to = attrib_link_to.getValue();
            Attr attrib_speaker = (Attr)attrs.getNamedItem(SPEAKER);
            String speaker = attrib_speaker.getValue();
            Attr attrib_topic = (Attr)attrs.getNamedItem(TOPIC);
            String topic = attrib_topic.getValue();
            Attr attrib_turnNo = (Attr)attrs.getNamedItem(TURN_NO);
            String turnNo = attrib_turnNo.getValue();

            Utterance utterance = new Utterance();

            if(comm_act_type != null){
                utterance.setCommActType(comm_act_type);
            }
            if(dialog_act != null){
                utterance.setTag(dialog_act);
            }
            if(link_to != null){
                utterance.setRespTo(link_to);
            }
            if(speaker != null){
                utterance.setSpeaker(speaker);
            }
            if(topic != null){
                utterance.setTopic(topic);
            }
            if(turnNo != null){
                utterance.setTurn(turnNo);
            }
            if(text != null){
                utterance.setContent(text);
            }

            list.add(utterance);
        }

        return list;
    }

}
