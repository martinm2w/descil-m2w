/*
 * This class is to return the tagging results from weka's output prediction text
 */

package edu.albany.ils.dsarmd0200.cuetag.weka;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author gjiao
 */
public class TaggingResults {

    String tagType = "";
    String TAGGING_RESULT = ""; // weka result
    ArrayList<Utterance> utts_ = new ArrayList<Utterance>();

    HashSet<Integer> errorInst = new HashSet<Integer>();
    HashMap original = new HashMap(); // key: inst; value: original tag
    HashMap error = new HashMap(); // key: inst; value: incorrectly tagged tag

    HashMap<Integer, String> tag_alias = new HashMap<Integer, String>();

    public TaggingResults(String resultPath, ArrayList utts, String tagType){

        init();
        this.TAGGING_RESULT = resultPath;
        this.utts_ = utts;
        this.tagType = tagType;
    }

    private void init(){
        this.utts_.clear();
        this.error.clear();
        this.original.clear();
        this.errorInst.clear();
        this.tag_alias.clear();
    }

    public ArrayList getTaggingResults(){
        
        setTagAlias(TAGGING_RESULT);
//        System.err.println("Guohua debug: "+Arrays.toString(this.tag_alias.keySet().toArray()));

        ArrayList<Utterance> new_utts = utts_;
        BufferedReader br;
        String tempStr = "";
        int n = 0;
        try {
            br = new BufferedReader(new FileReader(TAGGING_RESULT));
            tempStr = br.readLine();
            while(!tempStr.contains("inst#     actual  predicted error prediction ()")){
                tempStr = br.readLine();
            }
            tempStr = br.readLine();
            while(!tempStr.equals("")){
                if(tempStr.contains("+")){ // incorrectly tagged utterance
                    //System.out.println("Err: " + tempStr);
                    String[] errorInfo = tempStr.trim().split("\\s+");
                    int inst = Integer.parseInt(errorInfo[0]);
                    String originalTag = errorInfo[1];
                    String errorTag = errorInfo[2];
                    errorInst.add(inst);
                    original.put(inst, originalTag);
                    error.put(inst, errorTag);
                    n++;
                }
                tempStr = br.readLine();
            }
            //System.out.println("Number of incorrectly tagged utterance: " + n);

            br.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        // start writing results
        
	//System.out.println("tag type: " + tagType);
        Object[] errInst = errorInst.toArray();
        for(int i = 0; i < errInst.length; i++){
            int inst = Integer.parseInt(errInst[i].toString()); // inst#
            String err_tagInfo = error.get(inst).toString();
            String err_tag_num = err_tagInfo.split(":")[0];
            int err_tag_no = Integer.parseInt(err_tag_num);
            String err_tag = tag_alias.get(err_tag_no).toString();

//            Utterance utterance = utts_.get(inst-1);
            if(tagType.equals("dialog_act3")){
//                utterance.setTag(err_tag);
                new_utts.get(inst-1).setTag3(err_tag);
            }
            else if(tagType.equals("dialog_act15")){
                new_utts.get(inst-1).setTag15(err_tag);
            }
            else if(tagType.equals("comm_act")){
//                utterance.setCommActType(err_tag);
                new_utts.get(inst-1).setCommActType(err_tag);
            }
//            utts_.remove(inst-1);
//            utts_.add(inst-1, utterance);
        }
	//System.out.println("new_utts: " + new_utts);
        return new_utts;
    }


    private void setTagAlias(String file){
        BufferedReader br;
        String tempStr = "";
        String[] tempStr_array;
        int alias = 1;
        try {
            br = new BufferedReader(new FileReader(file));
            while((tempStr = br.readLine()) != null){
                if(tempStr.contains(" = ")){
                    tempStr_array = tempStr.split(" = ");
                    tag_alias.put(alias, tempStr_array[tempStr_array.length-1].trim());
                    alias++;
                }
            }
            br.close();
        } catch (IOException e){
            e.printStackTrace();
        }        
    }

    public void writeTaggingXml(String file){
        BufferedWriter bw;

        try {
            bw = new BufferedWriter(new FileWriter(file));

            utts_ = getTaggingResults();
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            bw.write("<Dialogue date=\"2010-06-10\" >\n");

            for(int i = 0; i < utts_.size(); i++){
                Utterance utterance = (Utterance)utts_.get(i);
                String speaker = utterance.getSpeaker();
                if(speaker == null)
                    speaker = "";
                String comm_act_type = utterance.getCommActType();
                if(comm_act_type == null)
                    comm_act_type = "";
                String dialog_act = utterance.getTag();
                if(dialog_act == null)
                    dialog_act = "";
                String content = utterance.getContent();
                if(content == null)
                    content = "";
                String topic = utterance.getTopic();
                if(topic == null)
                    topic = "";
                String polarity = utterance.getPolarity();
                if(polarity == null)
                    polarity = "";
                String pos = utterance.getPOS();
                if(pos == null)
                    pos = "";
                String pos_origin = utterance.getPOSORIGIN();
                if(pos_origin == null)
                    pos_origin = "";
                String link_to = utterance.getRespTo();
                if(link_to == null)
                    link_to = "";
                String turnNo = utterance.getTurn();
                if(turnNo == null)
                    turnNo = "";
                                
                bw.write("<turn speaker=\"" + speaker + "\" dialog_act=\"" +
                        dialog_act + "\" comm_act_type=\"" + comm_act_type +
                        "\" link_to=\"" + link_to + "\" pos=\"" + pos +
                        "\" pos_origin=\"" + pos_origin + "\" topic=\"" +
                        topic + "\" polarity=\"" + polarity + "\" turn_no=\"" +
                        turnNo + "\">" + content + "</turn>\n");

            }
            bw.write("</Dialogue>");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
