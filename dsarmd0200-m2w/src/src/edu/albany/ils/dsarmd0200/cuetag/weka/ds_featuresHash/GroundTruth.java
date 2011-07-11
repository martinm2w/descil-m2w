/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import java.io.File;
import java.util.ArrayList;
import org.w3c.dom.Document;

/**
 *
 * @author Laura G.H. Jiao
 */
public class GroundTruth {

    private static ArrayList<File> dataFile = new ArrayList<File>(); // same chat files, different annotations
    private static ArrayList<ArrayList<Utterance>> data_UTTS_list = new ArrayList<ArrayList<Utterance>>(); // List of ArrayLists, size = num of data files
    private static int utteranceNum = 0; // number of utterances
    private static ArrayList<Utterance> groundTruth_UTTS = new ArrayList<Utterance>(); // ground truth


    public GroundTruth(String fileFolderPath){

        File folder = new File(fileFolderPath);
        File[] files = folder.listFiles();
        for(int i = 0; i < files.length; i++){
            File file = files[i];
            dataFile.add(file);
        }        
    }

    public GroundTruth(String[] filePath){

        for(int i = 0; i < filePath.length; i++){
            File file = new File(filePath[i]);
            dataFile.add(file);
        }        
    }

    public static void main(String[] args){

        String fileFolderPath = "/home/jgh/Desktop/Human_Terrain_Studies/lauren_annotated/GroundTruth/Mar11_GroupB_GT/";
        init();
        GroundTruth gc = new GroundTruth(fileFolderPath);
        gc.setDataUttsList(dataFile);
        gc.initGroundTruthUttsList();
        gc.setGroundTruthUttsList(15, "da15");
    }

    private static void init(){
        dataFile.clear();
        data_UTTS_list.clear();
        groundTruth_UTTS.clear();        
    }
   
   
    private void setDataUttsList(ArrayList dataFile){

        for(int i = 0; i < dataFile.size(); i++){
            File file = (File)dataFile.get(i);
//            DsarmdDialogParser ddp = new DsarmdDialogParser();
//            Document doc = ddp.getDocument(file.getAbsolutePath());
//            ArrayList<Utterance> list = ddp.parseDAList(doc);
            String docURL = file.getAbsolutePath();
            Document doc = DsarmdXMLParser.DOMParser(docURL);
            ArrayList<Utterance> list = DsarmdXMLParser.getUttsArrayList(doc);
//            System.out.println("Size of dataFile: " + dataFile.get(i).toString() + " = " + list.size());

            for(int j = 0; j < list.size(); j++){
                Utterance utterance = (Utterance)list.get(j);                

                String turnNo = utterance.getTurn();
                double turnNoD = Double.parseDouble(turnNo);
                int truncatedTurnNo = (int)(turnNoD);
                if(turnNoD == (double)truncatedTurnNo){ // 16.0 == 16
                    list.get(j).setTurn(String.valueOf(truncatedTurnNo));
                }
                if(turnNoD > (double)truncatedTurnNo){ // eg. if 16.1 > 16.0

//                    System.out.println("list " + i + " turnNo = " + turnNo + " node " + j);
                    String content = utterance.getContent();
                    Utterance prev_utterance = (Utterance)list.get(j-1);
                    String prev_content = prev_utterance.getContent();
                    prev_content = prev_content +" " + content;
                    list.get(j-1).setContent(prev_content);
                    String tag15 = utterance.getTag().toLowerCase().trim();
                    String prev_tag15 = prev_utterance.getTag().toLowerCase().trim();
                    if(tag15.equals("assertion-opinion") ^ prev_tag15.equals("assertion-opinion")){ // if one of them is AS, set it to the other one
                        if(prev_tag15.equals("assertion-opinion")){
                            list.get(j-1).setTag15(tag15);
                        }
                    }
                    list.get(j-1).setTurn(String.valueOf(truncatedTurnNo));
                    list.remove(j);
//                    System.out.println("Remove node: list " + i + ", node " + j);
                    j = j-1;
                }

            }


//            System.out.println("************* Laura debug: Size of modified list " + i + "= " + list.size());
//            for(int k = 0; k < list.size(); k++){
//                Utterance utt = (Utterance)list.get(k);
//                System.out.println("aaaaaaaaaa " + utt.getTurnNo());
//            }
            data_UTTS_list.add(list);
        }        
    }

 
    private void initGroundTruthUttsList(){
        int min = 0;
        int max = 0;
        for(int i = 0; i < data_UTTS_list.size(); i++){
            int current = data_UTTS_list.get(i).size();
            int min_ = data_UTTS_list.get(min).size();
            int max_ = min_;
            if(min_ >= current){
                min = i;
            }
            if(max_ <= current){
                max = i;
            }            
        }
        ArrayList utts = data_UTTS_list.get(min); // get a sample data file
        utteranceNum = data_UTTS_list.get(max).size();
        for(int i = 0; i < utts.size(); i++){
            Utterance utterance = (Utterance)utts.get(i);
            groundTruth_UTTS.add(utterance);
        }
    }

    private void setGroundTruthUttsList(int tagNum, String tagType){
        for(int i = 0; i < utteranceNum; i++){
            int[] countingArray = new int[tagNum+1];
            for(int j = 0; j < data_UTTS_list.size(); j++){
                ArrayList utts = data_UTTS_list.get(j);
                Utterance utterance = (Utterance) utts.get(i);
                String tag = "";
                if(tagType.equals("da15")){
                    tag = utterance.getTag().toLowerCase().trim();
                }
                else if(tagType.equals("da3")){
                    tag = utterance.getMTag().toLowerCase().trim();
                }
                else {
                    System.err.println("unrecognized tag type");
                    return;
                }
                int tagAlias = DsarmdDATag.getTagAliasNum(tag, tagNum);
                countingArray[tagAlias-1]++;
            }
            /* get the max */
            int AS_alias = DsarmdDATag.getTagAliasNum("assertion-opinion", 15);
            int max = 0;
            for(int k = 0; k < countingArray.length; k++){
                if(countingArray[k] > countingArray[max]){
                    max = k;
                }
            }
            if(max == (AS_alias-1)){ // if Ground truth is assertion-opinion, check if there is other tag in the annotation
                for(int l = 0; l < countingArray.length; l++){
                    if(countingArray[l] > 0 && (l != max)){
                        max = l; // set it to other tag
                    }
                }
            }
            String groundTruthTag = DsarmdDATag.getTagName(max+1, tagNum);
            if(tagType.equals("da15")){
                groundTruth_UTTS.get(i).setTag15(groundTruthTag);
            }
            else if(tagType.equals("da3")){
                groundTruth_UTTS.get(i).setTag3(groundTruthTag);
            }
            else {
                System.err.println("unrecognized tag type");
                return;
            }
        }
        
        
    }

    public ArrayList getDataFile(){
        return dataFile;
    }

    public ArrayList getUTTSList(){
        return data_UTTS_list;
    }

    public ArrayList getGroundTruthUTTSList(){
        return groundTruth_UTTS;
    }

}
