/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author Laura G.H. Jiao
 */
public class SwbdDialogParser {


    public ArrayList parseDAList(String filename) {
        ArrayList list = new ArrayList();
        BufferedReader br;
        String tempStr = "";
        try {
            br = new BufferedReader(new FileReader(filename));
            while((tempStr = br.readLine()) != null){
                if(tempStr.contains(":")){
                    list.add(parseDA(tempStr));
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Utterance parseDA(String line){
        Utterance da = new Utterance();
        String[] lineData = line.split(":");
        String speaker = lineData[0].trim();
        String dialog_act = lineData[1].trim();
        String content = lineData[2].trim();
        da.setSpeaker(speaker);
        da.setTag(dialog_act);
        da.setContent(content);
        return da;
    }

}
