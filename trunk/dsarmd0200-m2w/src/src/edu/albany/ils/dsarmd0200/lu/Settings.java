package edu.albany.ils.dsarmd0200.lu;
//Author: Ken Stahl
//**Settings class holds variables contained in the configuration file **//

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
Comments for config file
#lines beginning with # are comments, format is key='value'
#can define variables using $, must be defined before using
#values are parsed based on ' and '' puts ' in the value
#please make sure to match opening and closing quotes
 **/

public abstract class Settings {
	
    //location of config file
    private static String location = "/home/ruobo/develop/scil0200/config.txt";
    public static final String EMOTES = "emotes";
    public static final String MALES = "malenames";
    public static final String FEMALES = "femalenames";
    public static final String PW = "pw";
    public static final String NW = "nw";
    public static final String POLARITY_CUES = "polarity_cues";
    public static final String POSI_CONFS = "posi_confs";
    public static final String NEG_CONFS = "neg_confs";
    public static final String AFIRST_WORDS = "afirst_words";
    public static final String ACFIRST_WORDS = "acfirst_words";
    public static final String AAFIRST_WORDS = "aafirst_words";
    public static final String AACFIRST_WORDS = "aacfirst_words";
    public static final String STOP_WORDS = "stop_words";
    public static final String PROCESS_TYPE = "process_type";
    public static final String REPORT = "report";
    public static final String TRAINING = "training";
    public static final String YES = "yes";
    public static final String NO = "no";
    public static final String WEB_SERVICE = "web_service";
//    public static final String URDU = "urdu";
    public static final String URDU_PATH = "urduTagPath";
    private static HashMap <String, String> settings;
    private static HashMap <String, String> variables;
    private static boolean initialized = false;
    //m2w: chinese . 5/17/11 1:43 PM
//    public static final String CHINESE = "chinese";
//    public static final String CHINESE_PATH = "chineseTagPath";
    public static final String LANGUAGE = "language";
    public static final String POS_ENGLISH_MODEL = "posEnglishModel";
    public static final String POS_CHINESE_MODEL = "posChineseModel";
    public static final String CHINESE_SEG_DATA_DIR = "ChineseSegDataDir";


    
    public static void initialize(){
		settings = new HashMap();
		variables = new HashMap();
		File settingsFile = new File(location);
		try{
			BufferedReader br = new BufferedReader(new FileReader(settingsFile));
			while (br.ready()){
				String lineOfData = br.readLine();
				parseSettingsLine(lineOfData);
			}
			initialized = true;
		} catch (Exception e){
		}
	}
	
	private static void parseSettingsLine(String param){
	    //System.out.println("processing: " + param);
		if (param.length() > 0){
			if (param.charAt(0) != '#' && param.charAt(0) != '$'){
				String setKey = ""; String setVal = "";
				int i;
				for (i = 0; i < param.length() && param.charAt(i) != '='; i++){
					if (!Character.isWhitespace(param.charAt(i)))
						setKey += param.charAt(i);
				}	setVal = resolveValue(param.substring(i + 1));
				//System.out.println("setKey + setVal: " + setKey + " ====== " + setVal);
				settings.put(setKey, setVal);
			} else if (param.charAt(0) == '$'){
				String varKey = ""; String varVal = "";
				int i;
				for (i = 1; (i < param.length()) && param.charAt(i) != '='; i++){
					if (!Character.isWhitespace(param.charAt(i)))
							varKey += param.charAt(i);
				}   varVal = resolveValue(param.substring(i + 1));
				variables.put(varKey, varVal);
			}
		}
	}
	
	private static String resolveValue(String param){
		String retval = "";
		String p = param.trim();
		ArrayList <String> tokens = new ArrayList();
		int i = 0; char quote = '\'';
		while (i < p.length()){
			String token = "";
			if (p.charAt(i) == quote){
				i++;
				if (i < p.length() && p.charAt(i) == quote){
					token += quote; i++;
				}
				  //ok got the first case of 2 single quotes out of the way
				while (i < p.length() && p.charAt(i) != quote){
					token += p.charAt(i);
					i++;
					if (i < p.length() && p.charAt(i) == quote){
						i++;
						if (i < p.length() && p.charAt(i) == quote){
							token += quote;
							i++;
						} else i--;
						//double quote found, so add a single quote and continue parsing
					}
				} i++;
			} else if (p.charAt(i) == '$'){
				i++;
				while (i < p.length() && !Character.isWhitespace(p.charAt(i))){
					token += p.charAt(i);
					i++;
				}
				if (!variables.containsKey(token))
					System.err.println("Error in the configuration file.");
				token = variables.get(token);
			}
			while (i < p.length() && Character.isWhitespace(p.charAt(i))) i++;
			while (i < p.length() && p.charAt(i) != '$' && p.charAt(i) != quote)
				i++;
			if (token.length() > 0){
				tokens.add(token);
			}
		}
		for (i = 0; i < tokens.size(); i++)
			retval += tokens.get(i);
		return retval;
	}
	
	public static String getValue(String key){
		if (!initialized)
			initialize();
		String retval = "";
		if (settings.containsKey(key)){
			retval = settings.get(key);
		}
		return retval;
	}
    public static void setValue(String key, String val){
	if (!initialized)
	    initialize();
	if (settings.containsKey(key)){
	    settings.put(key, val);
	}
    }
}
