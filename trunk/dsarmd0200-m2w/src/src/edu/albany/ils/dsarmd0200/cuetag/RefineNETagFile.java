package edu.albany.ils.dsarmd0200.cuetag;

import java.io.*;
import java.util.regex.*;

public class RefineNETagFile {
	
	String readPath = "/home/sshaikh/TAG_Project/TagTraining/swbd_202k_42tags_noplus_NETag";
	String writePath = "/home/sshaikh/TAG_Project/TagTraining/swbd_202k_42tags_noplus_NETag_Refined";
	
	public void refine() {
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(new File(readPath)));
			PrintWriter pw = new PrintWriter(new FileWriter(new File(writePath)));
			String regExp = "[<][A-Z]+[\\s]TYPE[=][\"][A-Z_]+[\"][>][^/]+[<][/][A-Z]+[>]";
			//String regExp = "[\\<][\\/][\\w]+[\\>]";
			Pattern pt = Pattern.compile(regExp);
			Matcher mt;
			String matchStr;
			String typeStr;
			int startIndex;
			int endIndex;
			
			while((line = br.readLine())!=null) {				
				mt = pt.matcher(line);
				//System.out.println("line");
				while(mt.find()) {
					//System.out.println("find something!");
					matchStr = mt.group();
					startIndex = matchStr.indexOf("TYPE=\"") + 6;
					endIndex = matchStr.indexOf("\">");
					typeStr = matchStr.substring(startIndex, endIndex);
					//System.out.println("the matched string is: " + matchStr);
					//System.out.println("the type is: " + typeStr);
					line = line.replace(matchStr, typeStr.toLowerCase());
				}
				pw.println(line);
				
			}
			
			br.close();
			pw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	public static void main(String[] args) {
		RefineNETagFile rf = new RefineNETagFile();
		rf.refine();
		
	}


}
