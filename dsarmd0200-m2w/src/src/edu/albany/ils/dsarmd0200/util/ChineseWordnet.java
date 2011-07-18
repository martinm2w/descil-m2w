/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.albany.ils.dsarmd0200.util;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * m2w: this class contains the chinese wordnet util methods.
 * @author ruobo
 * @date Jun 29, 2011
 */
public class ChineseWordnet {
//	====================================Attributes=============================================
    public ChineseWordnetJDBC cjdbc;
//    public static Connection conn;
    //improving: 
    public static HashMap<String, ArrayList<String>> synMap, hyperMap, hypoMap;
    public static int testSyn = 0;
    public static int testSynjdbc = 0;
//      ===================================init & const============================================
    public ChineseWordnet(String dbms, String serverName, String portNumber, String dbName){
        super();
        cjdbc = new ChineseWordnetJDBC(dbms, serverName, portNumber, dbName);
        synMap = new HashMap<String, ArrayList<String>>();
        hyperMap = new HashMap<String, ArrayList<String>>();
        hypoMap = new HashMap<String, ArrayList<String>>();
//        try{
//            conn = cjdbc.getConnection("root", "root");
//        }catch(SQLException e){e.printStackTrace();}
    }
    
//	====================================public method============================================
    //testing 
    public static void main(String[] args){
        ArrayList<String> resultSet = new ArrayList<String>();
        ChineseWordnet CNWN = new ChineseWordnet("mysql", "localhost", "3306", "atur");
//        CNWN.initialize("mysql", "localhost", "3306", "atur");
        try{
            resultSet = CNWN.getChineseSynlist("嘲笑");
//            resultSet = CNWN.getChineseHyperlist("嘲笑");
//            resultSet = CNWN.getChineseHypolist("嘲笑");
//            ChineseWordnet.closeConn();
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        for(String a : resultSet)
            System.out.print("[" + a + "]");
    }
    
//    public static void closeConn() throws SQLException{
////        conn.close();
//    }
//    public static void main(String[] agrs){
//        ChineseWordnetJDBC cjdbc = new ChineseWordnetJDBC("mysql", "localhost", "3306", "atur");
//        
//    }
    /**
     * m2w: trying to setup jdbc parameters
     * @param dbms
     * @param serverName
     * @param portNumber
     * @param dbName 
     * @date 6/29/11 12:58 PM
     */
//    public void initialize(String dbms, String serverName, String portNumber, String dbName){
//        cjdbc = new ChineseWordnetJDBC(dbms, serverName, portNumber, dbName);
////        try{
////            Connection conn = cjdbc.getConnection("root", "root");
////            conn.close();
////            System.out.println("CN-WN jdbc testing ... fine");
////        }catch(Exception e){
////            e.printStackTrace();
////        }
//    }
    
    /**
     * m2w: the public interface, returns an arraylist of strings of all synons.
     * added the improvemnt which is the implementation of the hashmaps .7/14/11 10:37 AM
     * @param word: the word we want to search.
     * @return this word's synlist.
     * @throws SQLException 
     * @date 7/14/11 10:37 AM
     */
    public ArrayList<String> getChineseSynlist(String word) throws SQLException{
//        System.out.println("syn: " + ++testSyn);
        ArrayList<String> synset = new ArrayList<String>();
        if(!word.equals("")){
            if(this.isTheWordInTheMap(word, synMap)){
                return synMap.get(word);
            }else{
                Connection conn = cjdbc.getConnection("root", "root");
                synset = this.getChSynset(word, conn);//synset not parsed
                conn.close();
                synset = this.parseSynset(synset);
//                System.out.println("synjdbc operation: " + ++testSynjdbc);
            }
            this.addToWordMap(word, synset, synMap);
//            System.out.println(synset);
        }
        return synset;
    }
    
    /**
     * m2w the public interfaces, returns an arraylist of strings of all hypers.
     * @param word
     * @return
     * @throws SQLException 
     */
    public ArrayList<String> getChineseHyperlist(String word) throws SQLException{
        ArrayList<String> hyperList = new ArrayList<String>();
        if(!word.equals("")){
            if(this.isTheWordInTheMap(word, hyperMap)){
                return hyperMap.get(word);
            }else{
                Connection conn = cjdbc.getConnection("root", "root");
                hyperList = this.getCNHyper(word, conn);
                conn.close();
            }
            this.addToWordMap(word, hyperList, hyperMap);
        }
        return hyperList;
    }
    
    public ArrayList<String> getChineseHypolist(String word) throws SQLException{ 
        ArrayList<String> hypoList = new ArrayList<String>();
        if(!word.equals("")){
            if(this.isTheWordInTheMap(word, hypoMap)){
                return hypoMap.get(word);
            }else{
                Connection conn = cjdbc.getConnection("root", "root");
                hypoList = this.getCNHypo(word, conn);
                conn.close();
            }
            this.addToWordMap(word, hypoList, hypoMap);
        }
        return hypoList;
    }
    
    public boolean isChineseSyn(String word1, String word2){
        boolean isSyn = false;
        if(this.isCnSyn(word1, word2)){
            isSyn = true;
        }
//        else if(this.isCnSyn(word2, word1)){
//            isSyn = true;
//        }else{
//            isSyn = false;
//        }
        return isSyn;
    }
    
//    public void closeConn(){
//        try{
//            conn.close();
//        }catch(SQLException e){e.printStackTrace();}
//    }
//	===================================private methods============================================
    
    /**
     *  m2w: this method is used to get the synsets of the chinese word.
     * 1. input chinese chars , get synset_id chiese, wn_chinese
     * 2. get english word's e_syn_id from wn_map
     * 3. look up the synset from wn_synset, 
     * 4. return the synset
     * 
     * @param word
     * @return the list of synsets
     * @date 6/22/11 1:14 PM
     */
    private ArrayList<String> getChSynset(String word, Connection conn) throws SQLException{
        ArrayList<String> synset = new ArrayList<String>();
        //1. get synset_id chiese, wn_chinese
//        String sql = "SELECT synset_id, w_num, chinese FROM wn_chinese WHERE chinese = ?";
        String sql1 = "SELECT * FROM wn_chinese WHERE chinese LIKE BINARY ? ";
        PreparedStatement prest = conn.prepareStatement(sql1);
        prest.setString(1, word); //counted from 1
        ResultSet rs = prest.executeQuery();
//        System.out.println("query1 done..."); // result columns are counted from 1 
        ArrayList<ArrayList<String>> list1 = new ArrayList<ArrayList<String>>();
        while (rs.next()){
          ArrayList<String> tempList = new ArrayList<String>();
          String synset_id = rs.getString(1);
          String w_num = rs.getString(2);
          String chinese = rs.getString(3) ;
          tempList.add(synset_id);tempList.add(w_num);tempList.add(chinese);
          list1.add(tempList);
//          System.out.println(synset_id + "\t" + "- " + w_num + "-\t" + chinese);
         }
//        System.out.println("list 1 is: " + list1);//ends step 1 
        
        
        //2 . looking for the english number for the chinese words
        ArrayList<ArrayList<String>> list2 = new ArrayList<ArrayList<String>>();
        for(ArrayList<String> templist : list1){
            String sql2 = "SELECT * FROM wn_map WHERE c_syn_id LIKE " + templist.get(0) + " AND c_syn_num LIKE " +  templist.get(1) ;
            ResultSet rs2 = prest.executeQuery(sql2);
//            System.out.println("query2 done..."); 
            while (rs2.next()){
              ArrayList<String> tempList = new ArrayList<String>();
              String s1 = rs2.getString(1);
              String s2 = rs2.getString(2);
              String s3 = rs2.getString(3);
              String s4 = rs2.getString(4);
              tempList.add(s1);tempList.add(s2);tempList.add(s3);tempList.add(s4);
              list2.add(tempList);
//              System.out.println(s1 + "\t" + "- " + s2 + "-\t" + s3 + "\t" + s4);
            }
        }
//        System.out.println("list 2 is: " + list2);//ends step 2 for loop

        //3. get the synset from wn_synset
        ArrayList<ArrayList<String>> list3 = new ArrayList<ArrayList<String>>();
        for(ArrayList<String> templist : list2){
            String sql3 = "SELECT * FROM wn_synset WHERE synset_id LIKE " + templist.get(0) + " AND w_num LIKE " +  templist.get(1) ;
//            System.out.println(sql3);
            ResultSet rs3 = prest.executeQuery(sql3);
//            System.out.println("query3 done..."); 
            while (rs3.next()){
              ArrayList<String> tempList = new ArrayList<String>();
              String s1 = rs3.getString(1);
              String s2 = rs3.getString(2);
              String s3 = rs3.getString(3);
              String s4 = rs3.getString(4);
              String s5 = rs3.getString(5);
              String s6 = rs3.getString(6);
              String s7 = rs3.getString(7);
              String s8 = rs3.getString(8);
              tempList.add(s1);tempList.add(s2);tempList.add(s3);tempList.add(s4);tempList.add(s5);tempList.add(s6);tempList.add(s7);tempList.add(s8);
              list3.add(tempList);
//              System.out.println(s1 + "\t" + "- " + s2 + "-\t" + s3 + "\t" + s4);
            }
        }
//        System.out.println("list 3 is: " + list3);
        //adding synsets from list3 to synset
        for(ArrayList<String> tempList : list3){
            synset.add(tempList.get(7));
        }
        return synset;
        
    }
    
    /**
     * m2w: parse the synset into arraylist of strings of synons.
     * @param input
     * @return 
     */
    private ArrayList<String> parseSynset(ArrayList<String> input){
        ArrayList<String> synset = new ArrayList<String>();
        HashSet tempSet = new HashSet();
            if(!input.isEmpty()){
//                System.out.println("in");
                for(String subSynset : input){
                    if(subSynset.contains("n")){
//                        System.out.println("has N " + subSynset);
                        Pattern p = Pattern.compile("n.[\\w+.]* [a-zA-Z]?[^a-zA-Z]+");// can fit "n.vi.vt. U出价,提议,意图,报价"
                        Matcher m = p.matcher(subSynset);
    //                    Scanner s = new Scanner(subSynset).useDelimiter("n. [^a-z]");
                        while(m.find()){
                            String temp = m.group();
                            String words = temp.split(" ")[1];
                            String[] senses = words.split(",", -1);
                            for(String s : senses){
                                if(s.contains(";")){
                                    String[] senseSplit = s.split(";");
                                    for(String ss : senseSplit){
                                        if(!tempSet.contains(ss)){
                                            tempSet.add(ss);
                                        }
                                    }
                                }else{
                                    if(!tempSet.contains(s)){
                                        tempSet.add(s);
                                    }
                                }
                            }//adding to tempset
                        }//ends while find
                    }
                }
            }
       synset.addAll(tempSet);
//        }catch(UnsupportedEncodingException e){e.printStackTrace();}
        return synset;
    }
    
    
    private ArrayList<String> getCNHyper(String word, Connection conn) throws SQLException{
        ArrayList<String> synset = new ArrayList<String>();
        //1. get synset_id chiese, wn_chinese
        String sql1 = "SELECT * FROM wn_chinese WHERE chinese LIKE BINARY ? ";
        PreparedStatement prest = conn.prepareStatement(sql1);
        prest.setString(1, word); //counted from 1
        ResultSet rs = prest.executeQuery();
//        System.out.println("query1 done..."); // result columns are counted from 1 
        ArrayList<ArrayList<String>> list1 = new ArrayList<ArrayList<String>>();
        while (rs.next()){
          ArrayList<String> tempList = new ArrayList<String>();
          String synset_id = rs.getString(1);
          String w_num = rs.getString(2);
          String chinese = rs.getString(3) ;
          tempList.add(synset_id);tempList.add(w_num);tempList.add(chinese);
          list1.add(tempList);
//          System.out.println(synset_id + "\t" + "- " + w_num + "-\t" + chinese);
         }
//        System.out.println("list 1 is: " + list1);//ends step 1 
        
        
        //2 . looking for the english number for the chinese words
        ArrayList<ArrayList<String>> list2 = new ArrayList<ArrayList<String>>();
        for(ArrayList<String> templist : list1){
            String sql2 = "SELECT * FROM wn_map WHERE c_syn_id LIKE " + templist.get(0) + " AND c_syn_num LIKE " +  templist.get(1) ;
            ResultSet rs2 = prest.executeQuery(sql2);
//            System.out.println("query2 done..."); 
            while (rs2.next()){
              ArrayList<String> tempList = new ArrayList<String>();
              String s1 = rs2.getString(1);
              String s2 = rs2.getString(2);
              String s3 = rs2.getString(3);
              String s4 = rs2.getString(4);
              tempList.add(s1);tempList.add(s2);tempList.add(s3);tempList.add(s4);
              list2.add(tempList);
//              System.out.println(s1 + "\t" + "- " + s2 + "-\t" + s3 + "\t" + s4);
            }
        }
//        System.out.println("list 2 is: " + list2);//ends step 2 for loop

        //3. get hyper from hypers
        ArrayList<ArrayList<String>> list3 = new ArrayList<ArrayList<String>>();
        for(ArrayList<String> templist : list2){
            String sql3 = "SELECT * FROM wn_hypernym WHERE synset_id_1 LIKE " + templist.get(0);
//            System.out.println(sql3);
            ResultSet rs3 = prest.executeQuery(sql3);
//            System.out.println("query3 done..."); 
            while (rs3.next()){
              ArrayList<String> tempList = new ArrayList<String>();
              String s1 = rs3.getString(1);
              String s2 = rs3.getString(2);
              tempList.add(s1);tempList.add(s2);
              list3.add(tempList);
//              System.out.println(s1 + "\t" + "- " + s2 + "-\t" + s3 + "\t" + s4);
            }
        }
//        System.out.println("list 3 is: " + list3);
        
        //4.convert english ids to chinese ones using wn_map
        ArrayList<ArrayList<String>> list4 = new ArrayList<ArrayList<String>>();
        for(ArrayList<String> templist : list3){
            String sql4 = "SELECT * FROM wn_map WHERE e_syn_id LIKE " + templist.get(1);
//            System.out.println(sql3);
            ResultSet rs4 = prest.executeQuery(sql4);
//            System.out.println("query3 done..."); 
            while (rs4.next()){
              ArrayList<String> tempList = new ArrayList<String>();
              String s1 = rs4.getString(1);
              String s2 = rs4.getString(2);
              String s3 = rs4.getString(3);
              String s4 = rs4.getString(4);
              tempList.add(s1);tempList.add(s2);tempList.add(s3);tempList.add(s4);
              list4.add(tempList);
//              System.out.println(s1 + "\t" + "- " + s2 + "-\t" + s3 + "\t" + s4);
            }
        }
//        System.out.println("list 4 is: " + list4);
        
        
        //5.convert english word to chinese and build the set
        ArrayList<ArrayList<String>> list5 = new ArrayList<ArrayList<String>>();
        for(ArrayList<String> templist : list4){
            String sql5 = "SELECT * FROM wn_chinese WHERE synset_id LIKE " + templist.get(2);
//            System.out.println(sql3);
            ResultSet rs5 = prest.executeQuery(sql5);
//            System.out.println("query3 done..."); 
            while (rs5.next()){
              ArrayList<String> tempList = new ArrayList<String>();
              String s1 = rs5.getString(1);
              String s2 = rs5.getString(2);
              String s3 = rs5.getString(3);
              tempList.add(s1);tempList.add(s2);tempList.add(s3);
              list5.add(tempList);
//              System.out.println(s1 + "\t" + "- " + s2 + "-\t" + s3 + "\t" + s4);
            }
        }
//        System.out.println("list 5 is: " + list5);
        
        
        
        
        //6. return
        //adding synsets from list5 to synset
        HashSet tempSet = new HashSet();
        for(ArrayList<String> tempList : list5){
            String tempWord = tempList.get(2);
            if(!tempSet.contains(tempWord)){
                tempSet.add(tempWord);
            }
        }
        
        synset.addAll(tempSet);
        
        return synset;
        
    }
    
    
    
        private ArrayList<String> getCNHypo(String word, Connection conn) throws SQLException{
        ArrayList<String> synset = new ArrayList<String>();
        //1. get synset_id chiese, wn_chinese
        String sql1 = "SELECT * FROM wn_chinese WHERE chinese LIKE BINARY ? ";
        PreparedStatement prest = conn.prepareStatement(sql1);
        prest.setString(1, word); //counted from 1
        ResultSet rs = prest.executeQuery();
//        System.out.println("query1 done..."); // result columns are counted from 1 
        ArrayList<ArrayList<String>> list1 = new ArrayList<ArrayList<String>>();
        while (rs.next()){
          ArrayList<String> tempList = new ArrayList<String>();
          String synset_id = rs.getString(1);
          String w_num = rs.getString(2);
          String chinese = rs.getString(3) ;
          tempList.add(synset_id);tempList.add(w_num);tempList.add(chinese);
          list1.add(tempList);
//          System.out.println(synset_id + "\t" + "- " + w_num + "-\t" + chinese);
         }
//        System.out.println("list 1 is: " + list1);//ends step 1 
        
        
        //2 . looking for the english number for the chinese words
        ArrayList<ArrayList<String>> list2 = new ArrayList<ArrayList<String>>();
        for(ArrayList<String> templist : list1){
            String sql2 = "SELECT * FROM wn_map WHERE c_syn_id LIKE " + templist.get(0) + " AND c_syn_num LIKE " +  templist.get(1) ;
            ResultSet rs2 = prest.executeQuery(sql2);
//            System.out.println("query2 done..."); 
            while (rs2.next()){
              ArrayList<String> tempList = new ArrayList<String>();
              String s1 = rs2.getString(1);
              String s2 = rs2.getString(2);
              String s3 = rs2.getString(3);
              String s4 = rs2.getString(4);
              tempList.add(s1);tempList.add(s2);tempList.add(s3);tempList.add(s4);
              list2.add(tempList);
//              System.out.println(s1 + "\t" + "- " + s2 + "-\t" + s3 + "\t" + s4);
            }
        }
//        System.out.println("list 2 is: " + list2);//ends step 2 for loop

        //3. get hyper from hypers
        ArrayList<ArrayList<String>> list3 = new ArrayList<ArrayList<String>>();
        for(ArrayList<String> templist : list2){
            String sql3 = "SELECT * FROM wn_hyponym WHERE synset_id_1 LIKE " + templist.get(0);
//            System.out.println(sql3);
            ResultSet rs3 = prest.executeQuery(sql3);
//            System.out.println("query3 done..."); 
            while (rs3.next()){
              ArrayList<String> tempList = new ArrayList<String>();
              String s1 = rs3.getString(1);
              String s2 = rs3.getString(2);
              tempList.add(s1);tempList.add(s2);
              list3.add(tempList);
//              System.out.println(s1 + "\t" + "- " + s2 + "-\t" + s3 + "\t" + s4);
            }
        }
//        System.out.println("list 3 is: " + list3);
        
        //4.convert english ids to chinese ones using wn_map
        ArrayList<ArrayList<String>> list4 = new ArrayList<ArrayList<String>>();
        for(ArrayList<String> templist : list3){
            String sql4 = "SELECT * FROM wn_map WHERE e_syn_id LIKE " + templist.get(1);
//            System.out.println(sql3);
            ResultSet rs4 = prest.executeQuery(sql4);
//            System.out.println("query3 done..."); 
            while (rs4.next()){
              ArrayList<String> tempList = new ArrayList<String>();
              String s1 = rs4.getString(1);
              String s2 = rs4.getString(2);
              String s3 = rs4.getString(3);
              String s4 = rs4.getString(4);
              tempList.add(s1);tempList.add(s2);tempList.add(s3);tempList.add(s4);
              list4.add(tempList);
//              System.out.println(s1 + "\t" + "- " + s2 + "-\t" + s3 + "\t" + s4);
            }
        }
//        System.out.println("list 4 is: " + list4);
        
        
        //5.convert english word to chinese and build the set
        ArrayList<ArrayList<String>> list5 = new ArrayList<ArrayList<String>>();
        for(ArrayList<String> templist : list4){
            String sql5 = "SELECT * FROM wn_chinese WHERE synset_id LIKE " + templist.get(2);
//            System.out.println(sql3);
            ResultSet rs5 = prest.executeQuery(sql5);
//            System.out.println("query3 done..."); 
            while (rs5.next()){
              ArrayList<String> tempList = new ArrayList<String>();
              String s1 = rs5.getString(1);
              String s2 = rs5.getString(2);
              String s3 = rs5.getString(3);
              tempList.add(s1);tempList.add(s2);tempList.add(s3);
              list5.add(tempList);
//              System.out.println(s1 + "\t" + "- " + s2 + "-\t" + s3 + "\t" + s4);
            }
        }
//        System.out.println("list 5 is: " + list5);
        
        
        
        
        //6. return
        //adding synsets from list5 to synset
        HashSet tempSet = new HashSet();
        for(ArrayList<String> tempList : list5){
            String tempWord = tempList.get(2);
            if(!tempSet.contains(tempWord)){
                tempSet.add(tempWord);
            }
        }
        
        synset.addAll(tempSet);
        
        return synset;
        
    }
        /**
         * m2w: util method for isChineseSynonym
         * @param word1
         * @param word2
         * @return 
         */
        private boolean isCnSyn(String word1, String word2){
            boolean isSyn = false;
            List<String> synList = new ArrayList<String>();
//            try{
//                synList = this.getChineseSynlist(word1);
            if(!synMap.isEmpty())
                synList = synMap.get(word1);
//            }catch(SQLException e){e.printStackTrace();}
            if(!synList.isEmpty()){
                for(String s : synList){
                    if(s.contains(word2)){
                        isSyn = true;
//                        System.out.println("match: " + word1 + "-" + word2);
                    }
                    
                }
            }
            return isSyn;
        }
        
        /**
         * m2w: see if the word is already in the map
         * @param word
         * @return boolean value: inMap
         * @date 7/14/11 10:06 AM
         */
        private boolean isTheWordInTheMap(String word, HashMap<String, ArrayList<String>> map){
            boolean inMap = false;
//            System.out.println("word: >" + word + "<");
            if(word.equals("") && 
                map.containsKey(word)){
                inMap = true;
            }
            return inMap;
        }
        
        /**
         * m2w: util method for putting new word and synlist into the map, just for making the project look neat.
         * @param word
         * @param synlist 
         */
        private void addToWordMap(String word, ArrayList<String> list, HashMap<String, ArrayList<String>> map){
            if(!map.containsKey(word)){
                map.put(word, list);
            }
        }
//      =================================setters & getters=========================================
}
