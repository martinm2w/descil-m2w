package edu.albany.ils.dsarmd0200.util;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.io.Serializable;

/**
 *
 * @author pzongo
 */
public class Ngrams implements Serializable{
    private ArrayList<Ngram> ngramList=new ArrayList<Ngram>();

    public void addNgram(Ngram subj){
        boolean result=false;
        for(int i=0;i<ngramList.size();i++){
            if(subj.equals(ngramList.get(i))){
		ngramList.get(i).incCount();
                return;
            }        
        }
        ngramList.add(subj);  //count should be one	
    }
    public Ngram getNgram(int index){
	return ngramList.get(index);  //should error check to ensure it is in the right bounds
    }

    public ArrayList<Ngram> toArrayList(){
	return ngramList;
    }
    //returns the index of subj in this Ngrams instance
    //otherwise it returns -1
    public int getIndex(Ngram subj){
        for(int i=0;i<ngramList.size();i++){
            if(subj.equals(ngramList.get(i))){
		ngramList.get(i).incCount();
                return i;
            }
        }
	return -1;
    }

    public boolean contains(Ngram subj){
        boolean result=false;
        for(int i=0;i<ngramList.size();i++){
            if(subj.equals(ngramList.get(i))){
                result=true;
                break;
            }
        
        }
        return result;
    }

    public int getSize(){ //inefficient, i should consider speeding this up
        return ngramList.size();
    }


}
