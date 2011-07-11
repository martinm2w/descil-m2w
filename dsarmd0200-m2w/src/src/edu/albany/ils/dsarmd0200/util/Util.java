package edu.albany.ils.dsarmd0200.util;

import edu.albany.ils.dsarmd0200.util.xml.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import java.io.*;
import java.util.*;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash.*;

/**
 * this file is used to tools process, such as stop words filter.
 * @author Ting Liu
 */

public class Util {
    public Util() {
    }

    /** get the model of utterance based on its length
     */
    public static String getModel(String utt) {
	String[] utts = utt.split("[\\s]+");
	if (utts.length == 1) {
	    return "1";
	}
	else if (utts.length > 1 &&
		 utts.length < 5) {
	    return "2";
	}
	else if (utts.length > 4) {
	    return "3";
	}
	return null;
    }

    /** remove all other charactors except word, number, ', and space*/
    public static String filterIt(String text) {
	if(text.indexOf("{F ")>=0) {
	    text = text.replaceAll("\\{F[\\s][^}]+[}]", "");
	}
	if(text.indexOf("{D ")>=0) {
	    text = text.replaceAll("\\{D", "");
	}
	if(text.indexOf("{C")>=0) {
	    text = text.replaceAll("\\{C ", "");
	}
	if(text.indexOf("{A")>=0) {
	    text = text.replaceAll("\\{A ", "");
	}
	if(text.indexOf("{E")>=0) {
	    text = text.replaceAll("\\{E ", "");
	}
	if(text.indexOf("-")>=0) {
	    text = text.replaceAll("[-]+", " ");
	}
	if(text.indexOf("<")>=0 && text.indexOf(">")>=0) {
	    text = text.replaceAll("[<][^>]+[>]", "");
	}
	text = text.toLowerCase().trim();
	text = text.replaceAll("'cause", "because");
	text = text.replaceAll("'bout", "about");
	text = text.replaceAll("dont", "don't");
	text = text.replaceAll("lets", "let's");
	/*
	parser_.parse(text);
	parser_.printTree(parser_.getTree());
	*/
	text = urlReplace(text, urlPattern);
	text = text.replaceAll("[\\?]+", " QUESTIONMARKKK ");
	text = text.replaceAll(" dont ", " don't ");
	text = text.replaceAll("$a $m", "am");
	text = text.replaceAll("$p $m", "pm");
	text = text.replaceAll("[^\\w']+", " ").toLowerCase().trim();
	text = text.replaceAll("\\s+", " ");
	return text;
    }

    public static void writeToFile(String fn, HashMap confs) {
	try {
	    ObjectOutputStream ops = new ObjectOutputStream(new FileOutputStream(new File(fn)));
	    ops.writeObject(confs);
	    ops.flush();
	    ops.close();
	}catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }

    public static boolean filterIt(String name_,
				   NounToken nt) {
		if ((name_.equals("carol") ||
		     name_.equals("ashley") ||
		     name_.equals("jessicad")) && 
		    (nt.getWord().equals("thing") ||
		     nt.getWord().equals("who") ||
		     nt.getWord().equals("what") ||
		     nt.getWord().equals("one")  ||
		     nt.getWord().equals("city") ||
		     nt.getWord().equals("many") ||
		     nt.getWord().equals("one") ||
		     nt.getWord().equals("these") ||
		     nt.getWord().equals("sun") ||
		     nt.getWord().equals("way") ||
		     nt.getWord().equals("man") ||
		     nt.getWord().equals("everyone") ||
		     nt.getWord().equals("u") ||
		     nt.getWord().equals("wheel") ||
		     nt.getWord().equals("person") ||
		     nt.getWord().equals("road") ||
		     nt.getWord().equals("roads") ||
		     nt.getWord().equals("body") ||
		     nt.getWord().equals("number") ||
		     nt.getWord().equals("ease") ||
		     nt.getWord().equals("hour") ||
		     nt.getWord().equals("time") ||
		     nt.getWord().equals("times") ||
		     nt.getWord().equals("minutes") ||
		     nt.getWord().equals("minute") ||
		     nt.getWord().equals("bye") ||
		     nt.getWord().equals("those") ||
		     nt.getWord().equals("couple") ||
		     nt.getWord().equals("lots") ||
		     nt.getWord().equals("world") ||
		     nt.getWord().equals("reason") ||
		     nt.getWord().equals("factor") ||
		     nt.getWord().equals("event") ||
		     nt.getWord().equals("topic") ||
		     nt.getWord().equals("option") ||
		     nt.getWord().equals("pair") ||
		     nt.getWord().equals("water") ||
		     nt.getWord().equals("sheet") ||
		     nt.getWord().equals("places") ||
		     nt.getWord().equals("fan") ||
		     nt.getWord().equals("streets") ||
		     nt.getWord().equals("setup") ||
		     nt.getWord().equals("can") ||
		     nt.getWord().equals("guess") ||
		     nt.getWord().equals("line") ||
		     nt.getWord().equals("workers") ||
		     nt.getWord().equals("future") ||
		     nt.getWord().equals("lot") ||
		     nt.getWord().equals("hm") ||
		     nt.getWord().equals("both") ||
		     nt.getWord().equals("fact") ||
		     nt.getWord().equals("words") ||
		     nt.getWord().equals("news") ||
		     nt.getWord().equals("r u") ||
		     nt.getWord().equals("hope") ||
		     nt.getWord().equals("part") ||
		     nt.getWord().equals("need") ||
		     nt.getWord().equals("n") ||
		     nt.getWord().equals("years") ||
		     nt.getWord().equals("whoever") ||
		     nt.getWord().equals("set") ||
		     nt.getWord().equals("trust") ||
		     nt.getWord().equals("hope") ||
		     nt.getWord().equals("day") ||
		     nt.getWord().equals("our") ||
		     nt.getWord().equals("self") ||
		     nt.getWord().equals("others") ||
		     nt.getWord().equals("ps") ||
		     nt.getWord().equals("your") ||
		     nt.getWord().equals("u guys") ||
		     nt.getWord().equals("hopes") ||
		     nt.getWord().equals("plans") ||
		     nt.getWord().equals("tonight") ||
		     nt.getWord().equals("week") ||
		     nt.getWord().equals("room") ||
		     nt.getWord().equals("story") ||
		     nt.getWord().equals("place") ||
		     nt.getWord().equals("things people") ||
		     nt.getWord().equals("choice") ||
		     nt.getWord().equals("bet") ||
		     nt.getWord().equals("presence") ||
		     nt.getWord().equals("point") ||
		     nt.getWord().equals("word") ||
		     nt.getWord().equals("head") ||
		     nt.getWord().equals("humans") ||
		     nt.getWord().equals("turn") ||
		     nt.getWord().equals("risk") ||
		     nt.getWord().equals("amount") ||
		     nt.getWord().equals("nonsense") ||
		     nt.getWord().equals("connection") ||
		     nt.getWord().equals("bottom") ||
		     nt.getWord().equals("idea lauren") ||
		     nt.getWord().equals("point nicole") ||
		     nt.getWord().equals("talks") ||
		     nt.getWord().equals("sessions") ||
		     nt.getWord().equals("ideas") ||
		     nt.getWord().equals("look") ||
		     nt.getWord().equals("look nicole") ||
		     nt.getWord().equals("faces") ||
		     nt.getWord().equals("move") ||
		     nt.getWord().equals("hall") ||
		     nt.getWord().equals("message") ||
		     nt.getWord().equals("name n") ||
		     nt.getWord().equals("names") ||
		     nt.getWord().equals("piece o cake") ||
		     nt.getWord().equals("thanks personnel") ||
		     nt.getWord().equals("n john") ||
		     nt.getWord().equals("john n") ||
		     nt.getWord().equals("dream"))) {
		    return true;
		}
		return false;
    }

    public static Object loadFile(String fn) {
	File pcs = new File(fn);
	try {
	    ObjectInputStream ops = new ObjectInputStream(new FileInputStream(pcs));
	    Object lfc = ops.readObject();
	    ops.close();
	    return lfc;
	}catch (IOException ioe) {
	    ioe.printStackTrace();
	}catch (ClassNotFoundException cnfe) {
	    cnfe.printStackTrace();
	}
	return null;
    }

    public static ArrayList loadStWds(String fn) {
	ArrayList sts = new ArrayList();
	try {
	    String a_line = null;
	    BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
	    while ((a_line = br.readLine()) != null) {
		sts.add(a_line.trim());
	    }
	}catch (IOException ioe) {
	    ioe.printStackTrace();
	    return null;
	}
	return sts;
    }

    public static double[] genQTThrs(double[] pls) {
	double[] qt_thrs = new double[6];
	qt_thrs[0] = pl_.evaluate(pls, 80);
	qt_thrs[1] = pl_.evaluate(pls, 60);
	qt_thrs[2] = pl_.evaluate(pls, 40);
	qt_thrs[3] = pl_.evaluate(pls, 20);
	qt_thrs[4] = pl_.evaluate(pls, 50);
	qt_thrs[5] = sd_.evaluate (pls, qt_thrs[4]);
	/*
	System.out.print("qt_thrs: ");
	for (int i = 0; i < qt_thrs.length; i++) {
	    System.out.print(" " + qt_thrs[i]);
	}
	System.out.println();
	*/
	return qt_thrs;
    }

    public double[] genSDThrs(double[] pls) {
	double[] qt_thrs = new double[5];
	double mean = pl_.evaluate(pls, 50);
	double sd = sd_.evaluate (pls, mean);
	//System.out.println("mean: " + mean + " sd: " + sd);
	qt_thrs[0] = mean + 1.5 * sd;
	//qt_thrs[0] = mean + 1 * sd;
	qt_thrs[1] = mean + 0.5 * sd;
	qt_thrs[2] = mean - 0.5 * sd;
	qt_thrs[3] = mean - 1.5 * sd;
	qt_thrs[4] = sd;
	//qt_thrs[3] = mean - 1 * sd;
	System.out.print("mean_sd_thrs: ");
	for (int i = 0; i < qt_thrs.length; i++) {
	    System.out.print(" " + qt_thrs[i]);
	}
	return qt_thrs;
    }
    public static double genMean(double[] pls) {
	double mean = pl_.evaluate(pls, 50);
	return mean;
    }

    public static double genSD(double[] pls) {
	//added 04/06/2011
	double mean = pl_.evaluate(pls, 50);
	double sd = sd_.evaluate (pls, mean);
	//System.out.println("mean: " + mean + " sd: " + sd);
	return sd;
    }

    public static ArrayList<String> uttToWds(String utt) {
	//by Xin, for caculating similarities between utterances
	ArrayList<String> wordArray = new ArrayList<String>();
	StringTokenizer st = new StringTokenizer(utt);
	while(st.hasMoreTokens())
	    {
		String word = st.nextToken();
		word = word.toLowerCase();
		if(!wordArray.contains(word) &&
		   !sws_.isStopWord(word)) {
		    wordArray.add(word);
		}
	    }
	return wordArray;
    }

    public static double compareUtts(String utt1, String utt2) {
	ArrayList<String> utt1_wds = uttToWds(utt1);
	ArrayList<String> utt2_wds = uttToWds(utt2);
	return compareUtts(utt1_wds, utt2_wds);
    }

    private static double compareUtts(ArrayList<String> compared_wordArray, ArrayList<String> wordArray) {
	int hit = 0;
	double f = 0;
	for(int j=0;j<wordArray.size();j++)
	    {
		for(int k=0;k<compared_wordArray.size();k++)
		    {
			if(compared_wordArray.get(k).equalsIgnoreCase(wordArray.get(j)))
			    {
				hit++;
			    }
		    }
		
	    }
	
	if(compared_wordArray.size()==0 || wordArray.size()==0)
	    {
		
		f = 0;
	    }
	else
	    {				
		f =(((double)hit/(float)compared_wordArray.size()) + ((double)hit/(float)wordArray.size()))/2;
	    }
	return f;
    }


    public static String urlReplace(String str, String pattern){
        String result = "";
        if(str != null && pattern != null){
            String[] array = ExtractRegex.testSplit(str, pattern);
            if(array != null){
//                System.err.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                for(String i : array){
                    str = str.replace(i, " hyperlink ");
                }
            }
        }
        // by Laura, Jan 3, 2011
        if(str.toLowerCase().contains("youtubelink")){
            str = str.toLowerCase().replace("youtubelink", "hyperlink");
        }
        return str;
    }

    public static void main(String[] args) {
	try {
	    File session = new File(args[0]);
	    BufferedReader br = new BufferedReader(new FileReader(session));
	    StringBuffer fc = new StringBuffer();
	    String a_line = null;
	    while ((a_line = br.readLine()) != null) {
		if (a_line.trim().length() > 0) {
		    if (!a_line.endsWith("</turn>")) {
			System.out.println("find a line no end of turn sign");
			a_line += "</turn>";
		    }
		    fc.append(a_line + "\n");
		}
	    }
	    br.close();
	    BufferedWriter bw = new BufferedWriter(new FileWriter (session));
	    bw.write(fc.toString());
	    bw.close();
	}catch (Exception e) {
	    e.printStackTrace();
	}
    }
    

    public static StopWords sws_ = new StopWords();
    private static Percentile pl_ = new Percentile();
    private static StandardDeviation sd_ = new StandardDeviation();
    private final static String urlPattern = "http://[A-Za-z0-9]+.[A-Za-z0-9]+[/=?%-&_~`@[/]/':+!]*([^\"\"])*$";
    //public static StanfordParser parser_ = new StanfordParser();
}
