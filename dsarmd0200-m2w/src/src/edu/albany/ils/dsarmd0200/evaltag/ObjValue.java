package edu.albany.ils.dsarmd0200.evaltag;

public class ObjValue {
  
	private String tag;
	private int frequency;
	private double prediction;
	
	private int useFrequency;
	private int correctFrequency;
	private int wrongFrequency;
        private double correctPercentage;
        private double wrongPercentage;
	private String ngram;
	
	public ObjValue(String gram ,String tagStr, int fre, double pred) {
		this.tag = tagStr;
		this.frequency = fre;
		this.prediction = pred;
		
		this.useFrequency = 0;
		this.correctFrequency = 0;
		this.wrongFrequency = 0;
		this.ngram = gram;
	}
	
	public ObjValue() {
		
	}
	
	public String toString() {
		return (this.ngram + " | " + this.tag + " | " + this.frequency + " | " + Math.rint(this.prediction * 1000)/1000 + " | " + 
				this.correctFrequency + " | " + this.wrongFrequency + " | " + this.useFrequency);
	}
	
	public String toTotalString() {
		return (this.ngram + ":" + this.tag + ":" + Math.rint(this.prediction * 1000)/1000  + ":" + this.frequency + 
				":" + (double)this.useFrequency/10 + ":" + Math.rint(getCorrectPercentage() * 1000)/1000 + ":" +
				Math.rint(getWrongPercentage() * 1000)/1000);
	}
	
	
	public void setTag(String tagStr) {
		this.tag = tagStr;
	}
	
	public void setFrequency(int fre) {
		this.frequency = fre;
	}
	
	public void setPrediction(double pre) {
		this.prediction = pre;
	}
	
	public void setUseFrequency(int fre) {
		this.useFrequency = fre;
	}
	
	public void increaseUseFrequency() {
		this.useFrequency++;
	}
	
	public void setCorrectFrequency(int fre) {
		this.correctFrequency = fre;
	}
	public void increaseCorrectFrequency() {
		this.correctFrequency++;
	}
	
	public void setWrongFrequency(int fre) {
		this.wrongFrequency = fre;
	}
	
	public void increaseWrongFrequency() {
		this.wrongFrequency++;
	}
	
	public String getTag() {
		return this.tag;
	}
	
	public int getFrequency() {
		return this.frequency;
	}
	
	public double getPrediction() {
		return this.prediction;
	}
	
	public int getUseFrequency(){
		return this.useFrequency;
	}
	
	public int getCorrectFrequency() {
		return this.correctFrequency;
	}

        public double getCorrectPercentage() {
	    if(this.useFrequency !=0) return (double)correctFrequency/(double)useFrequency;
            else return 0.0;
        }
	
	public int getWrongFrequency() {
		return this.wrongFrequency;
	}

        public double getWrongPercentage(){
        	if(this.useFrequency!=0) return (double)wrongFrequency/(double)useFrequency;
                else return 0.0;
        }
	
	public String getNGram() {
		return this.ngram;
	}
	
}
