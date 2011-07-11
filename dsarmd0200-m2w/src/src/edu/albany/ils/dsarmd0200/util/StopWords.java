package edu.albany.ils.dsarmd0200.util;
/**
 * this file is used to filter stop words
 * @author Ting Liu
 */

import java.util.*;
import java.io.*;

public class StopWords {
    public StopWords() {
	readStopWords(new File(stop_path));
    }

    public void readStopWords(File file){
        if( !file.exists() ) {
	    System.out.println("the stop-words file cannot be found!");
	    return ;
        }
        try{
	    String line;        
	    int fl = (int)file.length();
	    char[] f_chs = new char [fl];
            BufferedReader in = new BufferedReader ( new FileReader (file));
	    in.read(f_chs);
	    String f_str = new String(f_chs);
	    in = new BufferedReader(new StringReader(f_str));
            while ((line = in.readLine()) != null) {
		stop_words_.add(line.trim());
            }
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void filterIt(ArrayList words) {
	for (int i = 0; i < words.size(); i++) {
	    String word = ((String)words.get(i)).toLowerCase();
	    if (stop_words_.contains(word)) {
		words.remove(i);
		i--;
	    }
	}
    }
    
    public boolean isStopWord(String word) {
	return stop_words_.contains(word);
    }
    

    public final String stop_path="/home/ruobo/develop/scil0200/conf/stop-words"; //which question words + verbs and more are removed, except pronoun and be
    //    public final String stop_path="/projects/NL5/NL/NLCOMMON/bin/DATag-stop-words_backup";
    //public final String stop_path="/projects/NL5/NL/NLCOMMON/bin/DATag-stop-words"; //which question words are removed
    //    public final String stop_path="/projects/NL5/NL/NLCOMMON/bin/DATag-stop-words_new"; //which question words + verbs are removed
    private HashSet stop_words_ = new HashSet();
}
