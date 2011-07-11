package edu.albany.ils.dsarmd0200.evaltag;

/**
 * The basic interface represents a dialog act.
 * @author chen
 * */
public interface DialogAct {
	/**
	 * Get the utterance of the dialog act.
	 * */
    public String getUtterance();
	
	/**
	 * Get the tag of the utterance.
	 * */
	public String getTag();
	
	/**
	 * Set the utterance of the dialog act.
	 * @param utterance the utterance String
	 * */
    public void setUtterance(String utterance);
    public void setContent(String content);
	
	/**
	 * Set the tag of the utterance.
	 * @param tag the tag of the utterance as a String
	 * */
	public void setTag(String tag);
	
    
	/**
	 * Get the standard tag of the utterance
	 * according to some standard tag set.
	 * */
	public String getStandardTag();
	
	public String getStandardTag(String _tag);
    
	public boolean isStandardTag(String tag);
	
}
