package edu.albany.ils.dsarmd0200.util;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.lang.StringBuilder;
import java.io.Serializable;

/**
 *
 * @author pzongo
 */
public class Ngram implements Comparable<Ngram>,Serializable{
    private ArrayList<String> ngram=new ArrayList<String>();
    private int count=1;
    private int rank=0;

    public void addGram(String gramIn){
        ngram.add(gramIn);
    }

    public ArrayList<String> getNgram(){
        return ngram;
    }
    public void setNgram(ArrayList<String> ngramIn){
        ngram=ngramIn;
    }
    public int getCount(){
        return count;
    }
    public void incCount(){
        count++;
    }
    public int getRank(){
        return rank;
    }
    public void setRank(int rankIn){
        rank=rankIn;
    }
    public boolean equals(Ngram subj){
        boolean result=true;
        for(int i=0;i<ngram.size();i++){
	    if(subj.getNgram().size()!=ngram.size())
		return false;
            if(!subj.getNgram().get(i).equals(ngram.get(i))){
                result=false;
                break;
            }
        
        }
        return result;

    }

    public int compareTo(Ngram subj){
	return count-subj.getCount();
    }

    //size of ngrams, ideally no greater than 4
    public int getSize(){ //inefficient, i should consider speeding this up
        return ngram.size();
    }

    public String toString(){
	StringBuilder result=new StringBuilder();
	for(int i=0;i<ngram.size()-1;i++){
	    result.append(ngram.get(i)+",");
	}

	result.append(ngram.get(ngram.size()-1));
	return result.toString();
    }

}
