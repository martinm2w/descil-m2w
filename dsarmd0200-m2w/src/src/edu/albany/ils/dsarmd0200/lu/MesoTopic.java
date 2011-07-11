package edu.albany.ils.dsarmd0200.lu;

import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
//import java.io.*;
import java.util.*;

/**
 *
 * @author m2w.Ruobo
 * @Date 2011-2-15
 * @version 1.0
 *
 */
public class MesoTopic {

//	====================================Attributes==============================================
//    public static final int VERB_THRESHOLD = 15;
    
    public final int THRESHOLD_FILE_SIZE_LARGE = 10;//m2w: changed to 5. 4/13/11 12:45 PM
    public final int THRESHOLD_FILE_SIZE_SMALL = 5;//m2w: changed to 5. 4/13/11 12:45 PM
    public int THRESHOLD; // m2w: created 2 new fields 4/13/11 1:05 PM
    private ArrayList<ArrayList<String>> meso_topics_ = new ArrayList<ArrayList<String>>();
    private ArrayList utts_ = null;
    public static final int SHORT_LIST_THRESH = 3;// m2w: removing after parsing, if list length shorter than... then remove 4/21/11 3:06 PM

    //	====================================cal method=============================================
    /**
     * m2w: This method is to calculate the Mesotopic of a certain input document. It is called from the Assertions Class.
     *
     * @param filename
     * @param utts_
     * @param xp XMLParse
     * @param check PhraseCheck
     * @param wn Wordnet
     */
    public void calMesoTopic(String filename, ArrayList utts_, XMLParse xp, PhraseCheck check, Wordnet wn, NounList nl){

//        ArrayList<String> topicAList = new ArrayList<String>();// where i save the whole list
        //m2w : added the threshold of small or large files. 4/13/11 1:07 PM
        this.utts_ = utts_;
        if(this.utts_.size() > 200){
            THRESHOLD = THRESHOLD_FILE_SIZE_LARGE;
        }else{
            THRESHOLD = THRESHOLD_FILE_SIZE_SMALL;
        }
        
        //FindVerb fv = new FindVerb();
        //NounList nl = new NounList(xp, wn, check, utts_);
        //nl.createList(fv);
        ArrayList <AChain> a = nl.getChains();

        this.buildMesoTopic(a, xp);
    }

//	===================================util methods=============================================
    /**
     * m2w: This method is used in calMesotopic method. It is to build the mesotopic list by adding tempList which get from the AChain Obj.
     *
     * @param a ArrayList <AChain>
     * @param xp XMLParse
     */
    private void buildMesoTopic(ArrayList <AChain> a, XMLParse xp){
    	//build Blist
    	for (int i = 0; i < a.size(); i++){
		    AChain temp = a.get(i);
		    ArrayList<String> tempList = temp.getChain(a, xp);
		    tempList = this.removeBRep(tempList);
//                    System.out.println("thresh: " + THRESHOLD);
		    //check the mesotopic threshold
		    if ((tempList.size()-1) >= THRESHOLD){
//                        System.out.println("size: " + tempList.size());
		    	getMeso_topics_().add(tempList);
		    }

		}
        meso_topics_ = MesoTopic.sortTopicList(meso_topics_);
        

    }

    /**
     * m2w: This method is to remove turn repeats from tempList
     *
     * @param tempList  ArrayList<String>
     * @return tempList ArrayList<String>
     */
    private ArrayList<String> removeBRep(ArrayList<String> tempList)
	{
	    try {
		if (tempList == null) {
		    return tempList;
		}
		if(tempList.size()==1){
		    //arr.remove(0);
		    return tempList;
		}
		for (int i = 1; i < tempList.size(); i++) {
		    for (int j = 1; j < i; j++) {
			if (Integer.parseInt(tempList.get(j)) == (Integer.parseInt(tempList.get(i)))) {
				tempList.remove(i);
			    i--;
			    break;
			}
		    }
		}
		return tempList;

	    } catch (Exception ex) {
            return null;
	    }
	}

    /**
     * m2w: This method is to clear the MesoTopic obj ( the meso_topics_ arraylist).
     */
    public void clear(){
        this.meso_topics_.clear();
    }

    /**
     * m2w: this method takes in a mesotopic object and removes the turns with gaps greater than 10, it's used in the task focus MSM calculation.
     * @param meso_topics
     * @return ArrayList<ArrayList<String>> mesotopic
     * @time 2011-3-13 13:24:29
     */
  public static ArrayList<ArrayList<String>> rmTurnsGapGrTen (ArrayList<ArrayList<String>> meso_topics){

        ArrayList<ArrayList<String>> mt = meso_topics;

        //parse through the list, in each sub list, delete those turns with gaps greater than 10 (compare with -1 and +1 turns)
        for (int i = 0; i < mt.size(); i++){//list
            for(int j = 1; j < mt.get(i).size(); j++){//sublist

                int tempCurTurn = Integer.parseInt(mt.get(i).get(j));

                //if j = 1 , compare with +1's turn no. , if > 10 ,then delete
                if(j == 1 && mt.get(i).size() > 2){//1st one
                    int tempNextTurn = Integer.parseInt(mt.get(i).get(j+1));
                    if ((tempNextTurn - tempCurTurn) > 10){//if > 10
                        mt.get(i).remove(j);
                        j = j-1; // * need to test
                        continue;
                    }

                }else if(j > 1 && j < mt.get(i).size() -1 ){//mid

                    int tempNextTurn = Integer.parseInt(mt.get(i).get(j+1));
                    int tempPrevTurn = Integer.parseInt(mt.get(i).get(j-1));
                    if((tempCurTurn - tempPrevTurn) > 10 && (tempNextTurn - tempCurTurn) > 10){//if > 10
                        mt.get(i).remove(j);
                        j = j-1;
                        continue;
                    }

                }else if(j == mt.get(i).size() -1 && mt.get(i).size() > 2 ){//last
                    int tempPrevTurn = Integer.parseInt(mt.get(i).get(j-1));
                    if((tempCurTurn - tempPrevTurn) > 10){//if > 10
                        mt.get(i).remove(j);
                    }
                }//ends elseif
            }//ends sublist
        }//ends list
        return mt;
    }//ends method
    //* needs testing

    /**
     * m2w: this method sorts the list and reserve the top ten sublists
     * @param meso_topics
     * @return ArrayList<ArrayList<String>> meso topic
     * @time 2011-3-13 14:00:56
     */
    public static ArrayList<ArrayList<String>> getTopTenlist (ArrayList<ArrayList<String>> meso_topics){

        ArrayList<ArrayList<String>> mt = meso_topics;
       
        //reserve the top 10, delete others
        if(mt.size() > 11){//because 1st one is the topic , 1- 10 is the turn numbers

            while(mt.size() > 11){
                mt.remove(11);
            }
        }
        return mt;
    }

    /**
     * m2w: search same turns and if matches over 60%, merged them
     * @param meso_topics
     * @return ArrayList<ArrayList<String>> meso_topics_
     * @time 3/14/11 1:56 PM
     * 
     */
    public static ArrayList<ArrayList<String>> mergeSameTopic (ArrayList<ArrayList<String>> meso_topics){

        ArrayList<ArrayList<String>> mt = meso_topics;
        int count = 0;
        for(int i = 0; i < mt.size(); i++){//list from 1st
            for(int i2 = 1; i2 < mt.size(); i2 ++){//same list from 2nd
                for(int j = 1; j < mt.get(i).size(); j++){//sublist
                    String tempTurnNo = mt.get(i).get(j);
                    boolean hasIt = mt.get(i2).contains(tempTurnNo);
                    if(hasIt){
                      count ++;
                    }//ends if
                }//ends sublist
                //after the sublist is done searching, calculate the simularity, see if it needs to merge
                int compareSize = mt.get(i2).size();
                double percentage = (double)count / (double)compareSize;
                if(percentage > 0.6){//if it exceeds 60%, then merge
                    mt.set(i, MesoTopic.mergeTwoSubLists(mt.get(i), mt.get(i2)));
                }
                count = 0;
            }//ends list to compare with
        }//ends 1st list
        
        return mt;
    }

    /**
     * m2w : this class is to extract the merge method from the mergeSameTopic method. to make it simpler. merge the two sublists, and use the topic of the longer one, and sort the topic
     * @time 3/14/11 2:35 PM
     * @param merge_to
     * @param merge_from
     * @return
     */
    public static ArrayList<String> mergeTwoSubLists (ArrayList<String> merge_to, ArrayList<String> merge_from){

        ArrayList<String> l1 = merge_to;
        ArrayList<String> l2 = merge_from;

        for(int i = 1; i < l1.size(); i++){//l1
            for(int j = 1; j < l2.size(); j++){//l2

                if(l1.contains(l2.get(j))){
                    continue;
                }else{
                    l1.add(l2.get(j));
                }//ends if
            }//ends l2
        }//ends l1

        ArrayList<String> sortList = new ArrayList<String>();
        sortList = (ArrayList<String>)l1.clone();
        sortList.remove(0);
//        * if it needs to sort? yes
        Collections.sort(sortList, new Comparator(){
            @Override
                public int compare(Object o1, Object o2) {
                Integer turnNo1 = Integer.parseInt((String)o1);
                Integer turnNo2 = Integer.parseInt((String)o2);
                return turnNo1.compareTo(turnNo2);}
        });
        ArrayList<String> tempList = new ArrayList<String>();
        tempList.add(l1.get(0));
        tempList.addAll(sortList);

        return tempList;
        
    }

    /**
     * m2w : this method is extract to utilize sorting the topic list, don't need to copy the Collections sort every where
     * @param meso_topics
     * @return
     * @time 3/15/11 11:26 AM
     *
     */
    public static ArrayList<ArrayList<String>> sortTopicList (ArrayList<ArrayList<String>> meso_topics){
        
        ArrayList<ArrayList<String>> mt = meso_topics;
        ArrayList<ArrayList<String>> return_list = new ArrayList<ArrayList<String>>();
        //m2w : added sort when building the list. 3/14/11 1:46 PM
        Collections.sort(mt, new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
            Integer size1 = ((ArrayList<String>)o1).size();
            Integer size2 = ((ArrayList<String>)o2).size();
            return size2.compareTo(size1);
            }
        });
        
        //adding sort after all things. 4/21/11 2:27 PM
        for(int i = 0; i < mt.size(); i ++ ){//looping lists
            ArrayList<String> orgList = mt.get(i);
            ArrayList<String> sortList = (ArrayList<String>)mt.get(i).clone();
            sortList.remove(0);
            
            Collections.sort(sortList, new Comparator(){
            @Override
                public int compare(Object o1, Object o2) {
                Integer turnNo1 = Integer.parseInt((String)o1);
                Integer turnNo2 = Integer.parseInt((String)o2);
                return turnNo1.compareTo(turnNo2);}
            });
            ArrayList<String> tempList = new ArrayList<String>();
            tempList.add(orgList.get(0));
            tempList.addAll(sortList);
            return_list.add(tempList);
        }//ends sorting turns
        return return_list;
    }


    public static ArrayList<ArrayList<String>> removeShortLists (ArrayList<ArrayList<String>> meso_topics){
        ArrayList<ArrayList<String>> mt = meso_topics;
        for(int i = 0; i < mt.size(); i++){
            if((mt.get(i).size() - 1) < SHORT_LIST_THRESH){
                mt.remove(i);
            }
        }
        return mt;
    }

//=========================================setters & getters==============================================
    /**
     * @return the meso_topics_
     */
    public ArrayList<ArrayList<String>> getMeso_topics_() {
        return meso_topics_;
    }

    public ArrayList getUtts ()
    {
	return this.utts_;
    }

//===========================================old methods====================================================
//    //adding to list //build Alist
//    private void addToList(ArrayList<String> topicAList, ArrayList <AChain> a, XMLParse xp){
//    	for (int i = 0; i < a.size(); i++){
//		    AChain temp = a.get(i);
//		    ArrayList<String> tempList = temp.getChain(a, xp);
//		    topicAList.add("chain " + i + " starts:" + tempList.get(0));
//		    for (int j = 1; j < tempList.size(); j++){
//		    	topicAList.add(tempList.get(j));
//		    }
//		    topicAList.add("chain " + i + " ends");
//		}
//
//    }
//
//    //reading from A list
//    private void readFromList(ArrayList<String> topicAList){
//    	String line = "";
//		String flag = "end";
//	    String autoTopic = null;
//	    ArrayList<Integer> arr = new ArrayList<Integer>();
//
//    	for (int ri = 0; ri < topicAList.size(); ri++){
//			line = topicAList.get(ri);
//			System.err.println(line);
//			if(line.indexOf("chain")!=-1)
//			    {
//				if(flag.equals("end"))
//				    {
////					chainCount++;
//					flag = "starts";
//					autoTopic = line.substring(line.lastIndexOf(":")).substring(1).trim();
//					continue;
//				    }
//
//				if(flag.equals("starts"))
//				    {
//					flag = "end";
//					arr = removeRep(arr);
//					buildList(arr,autoTopic/*,chainCount,topics*/);
//				    }
//			    }
//			else
//			    {
//				    int member = Integer.parseInt(line);
//				    arr.add(member);//arr is the array of all evolved turn numbers of the meso-topic
//				}
//			}
//    }
//
//    //find the spk, build the list
//    private void buildList(ArrayList<Integer> arr, String autoTopic/*, int chainCount, ArrayList<String> topics*/)
//	{
//	    //System.out.println("in build map: " + arr.size());
//	    if(arr.size()>=THRESHOLD)
//		{
//		    //chains++;
//		    //printoutChain(arr,autoTopic,chainCount,topics);
//		    Utterance fst = (Utterance)utts_.get(arr.get(0));//get first utt
//		    String spk = fst.getSpeaker();//get first spk
//		    ArrayList<Object> mst = new ArrayList<Object>();
//		    mst.add(spk);
//		    mst.add(autoTopic);
////		    mst.add(arr.size())
//		    mst.add(arr);
////		    ArrayList msts = mst;
////		    meso_topics_.add(mst);
////		    	(ArrayList)meso_topics_.get(spk);
////		    if (msts == null) {
////			msts = new ArrayList();
////			meso_topics_.put(spk, msts);
////		    }
////		    msts.add(mst); // msts has first autotopic and arrsize?
//		    for(int i=1;i<arr.size();i++)
//			{
//			    //map.put(arr.get(i), (map.get(arr.get(i)) + " " + autoTopic).trim());
//		    	if (utts_.size() > arr.get(i)){
//				    Utterance utt = (Utterance)utts_.get(arr.get(i));
//				    utt.setTopic(autoTopic);
//		    	}
//			}
//		}
//	    //System.out.println("Now chain size: "+chains);
//	}
//
//
//
//	//for Alist
//    private ArrayList<Integer> removeRep(ArrayList<Integer> arr)
//	{
//	    try {
//		if (arr == null) {
//		    return arr;
//		}
//		if(arr.size()==1){
//		    //arr.remove(0);
//		    return arr;
//		}
//		for (int i = 0; i < arr.size(); i++) {
//		    for (int j = 0; j < i; j++) {
//			if (arr.get(j).equals(arr.get(i))) {
//			    arr.remove(i);
//			    i--;
//			    break;
//			}
//		    }
//		}
//		// arr.remove(0);
//		// System.out.println(array);
//		return arr;
//
//	    } catch (Exception ex) {
//            return null;
//	    }
//	}
//  public void callMesoTopic(String filename, ArrayList utts_, XMLParse xp, PhraseCheck check, Wordnet wn)
//	{
//	    this.utts_ = utts_;
//	    int chains=0;
//	    BufferedReader br;
//	    //BufferedReader inchain;
//	    //BufferedWriter outchain;
//	    String autoTopic = null;
//	    //automated topic map
//	    HashMap<Integer,String> map = new HashMap<Integer,String>();
//	    ArrayList<Utterance> utts = new ArrayList<Utterance>();
//
//	    //Wordnet wn = new Wordnet();
//	    FindVerb fv = new FindVerb();
//	    //PhraseCheck check = new PhraseCheck(filename,utts_);
//	    //XMLParse xp = new XMLParse(filename,utts_);
//	    NounList nl = new NounList(xp, wn, check);
//	    nl.createList(fv);
//	    //ListCompare lc = new ListCompare(nl, xp/*,utts*/);
//	    //lc.printAnnotatedUniqueTopics();
//	    //System.out.println(lc.uniqueTopicCount() + " unique topics found.");
//	    //CorefEvaluation cre = new CorefEvaluation(lc);
//	    //lc.printAutoChain();
//	    //lc.tryComparison();
//	    ArrayList <AChain> a = nl.getChains();
//	    //System.out.println("The size of automated chain list is " + a.size());
//
//	    try {
//		File file = new File("/home/ruobo/develop/dsarmd0100/NLTEST/DSARMD0100/tmp/MesoChain");
//		//BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
//		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
//		for (int i = 0; i < a.size(); i++){
//		    AChain temp = a.get(i);
//		    bw.write("chain " + i + " starts: ");
//		    //bw.newLine();
//		    // HERE IS AN ERROR PRINTING CHAIN NUMBER TO THE FILEUtterance utt = (Utterance)utts_.get(arr.get(i));
//		    temp.printOut(a,xp,bw);//m2w: AChain.printOut prints out the list of turn numbers of the topic chain.
//		    bw.write("chain " + i + " ends");
//		    bw.newLine();
//		}
//		bw.close();
//	    } catch (FileNotFoundException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	    } catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	    }
//
//	    //XMLParse parser = new XMLParse(filename,utts_);
//	    ArrayList<String> topics = new ArrayList<String>();
//	    ArrayList<Integer> arr = new ArrayList<Integer>();
//
//	    try {
//		br = new BufferedReader(new InputStreamReader(new FileInputStream("/home/ruobo/develop/dsarmd0100/NLTEST/DSARMD0100/tmp/MesoChain")));
//		String line = "";
//		String flag = "end";
//		int chainCount = 0;
//
//		while((line = br.readLine())!=null)
//		    {
//			if(line.indexOf("chain")!=-1)
//			    {
//				if(flag.equals("end"))
//				    {
//					chainCount++;
//					flag = "starts";
////					arr = new ArrayList<Integer>();
//					//System.out.println(line);Utterance utt = (Utterance)utts_.get(arr.get(i));
//					autoTopic = line.substring(line.lastIndexOf(":")).substring(1).trim();
//					//System.out.println("Chain " + chainCount + ": " +autoTopic);
//					continue;
//				    }
//
//				if(flag.equals("starts"))
//				    {
//					flag = "end";
//					arr = removeRep(arr);
//					buildMap(arr,autoTopic/*,chainCount,topics*/);
//					//calculate(autoTopic,arr,topics);
//					//topicMap = dealwithit(arr,utterances,topicMap);
//				    }
//			    }
//			else
//			    {
//				try {
//				    int member = Integer.parseInt(line);
//				    //System.out.println("Chain " + chainCount + ": " +member);
//				    arr.add(member);//arr is the array of all evolved turn numbers of the meso-topic
//				} catch (NumberFormatException e) {
//				    // TODO Auto-generated catch block
//				    e.printStackTrace();
//				}			    }
//		    }
//		br.close();
////		ArrayList keys = new ArrayList(meso_topics_.keySet());
//		/*
//		for (int i = 0; i < keys.size(); i++) {
//		    System.out.println("$$$$$$$$$$$$$$$$$$$Meso Topics introduced by " + keys.get(i));
//		    ArrayList msts = (ArrayList)meso_topics_.get(keys.get(i));
//		    for (int j = 0; j < msts.size(); j++) {
//			ArrayList mst = (ArrayList)msts.get(j);
//			System.out.println("topic: " + mst.get(0) + "    mentioned(including introduction): " + mst.get(1));
//		    }
//		}
//		*/
//		//inchain.close();
//		//outchain.close();
//	    } catch (FileNotFoundException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	    } catch (NumberFormatException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	    } catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	    }
//	}

}
