package edu.albany.ils.dsarmd0200.util;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.*;
import java.util.ArrayList;
/**
 *
 * @author pzongo
 */
public class Queries {
    //private static Statement s;
    private static boolean init=false;
    private static Connection con;
    private static final String MAY_5TH_DISCUSSION="2009-05-05";

    public static void initDatabase(){
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String database = "jdbc:mysql://localhost/HumanTerrain";
            con = DriverManager.getConnection( database ,"root","");
            init=true;

        }catch(Exception e){e.printStackTrace();}
    }

    public static ResultSet getResult(String sqlQuery){
        ResultSet rs=null;

        if (!init) {initDatabase();}
	try{
            Statement s = con.createStatement();
            s.execute(sqlQuery);

            rs = s.getResultSet();

        }catch(java.sql.SQLException e){e.printStackTrace();}
	return rs;
    }
    
    public static void runQuery(String sqlQuery) {
        if (!init) {initDatabase();}        //initDatabase();
        try {
            Statement s = con.createStatement();
            s.execute(sqlQuery);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }



    public static ArrayList<String> getSpeakerNames(int discID){
        ResultSet rs=null;
        if (!init) {initDatabase();}
	ArrayList<String> speakerNames=new ArrayList<String>();
	String sqlQuery ="SELECT DISTINCT Name FROM Utterance"+
	    " INNER JOIN Speaker ON Speaker.ID=SpeakerID WHERE DiscID="+discID+";";
	
	try{
            Statement s = con.createStatement();
	    s.execute(sqlQuery);
	    rs = s.getResultSet();
	    while(rs.next()){
		speakerNames.add(rs.getString("Name"));
	    }
	    rs.close();
	}catch(SQLException sqle){sqle.printStackTrace();}

	return speakerNames;
    }

public static String getSpeakerName(int speakerID){
        ResultSet rs=null;
        if (!init) {initDatabase();}

    String sqlQuery = "SELECT Name FROM Speaker WHERE ID="+speakerID+";";
        //System.out.println(sqlQuery);
    try{
        Statement s = con.createStatement();
        s.execute(sqlQuery);

        rs = s.getResultSet();

        while (rs.next()){
          	//System.out.println("foo"+rs.getString(1));
		//System.out.println("bar"+rs.getString(2));
            return rs.getString(1);

        }
	rs.close();
    }catch(java.sql.SQLException e){e.printStackTrace();}
    return "";


}
public static int getSpeakerID(String speaker){
        ResultSet rs=null;
        if (!init) {initDatabase();}

    String sqlQuery = "SELECT ID FROM Speaker WHERE Name='"+speaker+"';";
        //System.out.println(sqlQuery);
    try{
        Statement s = con.createStatement();
        s.execute(sqlQuery);

        rs = s.getResultSet();

        while (rs.next()){
          	//System.out.println("foo"+rs.getString(1));
		//System.out.println("bar"+rs.getString(2));
            return rs.getInt(1);

        }
	rs.close();
    }catch(java.sql.SQLException e){e.printStackTrace();}
    return 0;


}

public static ArrayList<Integer> getUtterancesFromDiscussons(int annotatorID,int discID){
        ResultSet rs=null;
        if (!init) {initDatabase();}
    ArrayList<Integer> uttList = new ArrayList<Integer>();

    String sqlQuery = "SELECT UtteranceID FROM Annotations INNER JOIN"+
	" Utterance ON UtteranceID=Utterance.ID WHERE AnnotatorID="+annotatorID+
	" AND DiscID="+discID+";";
        //System.out.println(sqlQuery); "
    try{
        Statement s = con.createStatement();
        s.execute(sqlQuery);

        rs = s.getResultSet();

        while (rs.next()){
          	//System.out.println("foo"+rs.getString(1));
		//System.out.println("bar"+rs.getString(2));
            uttList.add(rs.getInt(1));

        }
	rs.close();
    }catch(java.sql.SQLException e){e.printStackTrace();}
    return uttList;
}


/**
     * returns all non expert annonator IDs in an arraylist
     * @return
     */
    public static ArrayList<Integer> getAnnotatorIDs() {
        ResultSet rs=null;
        if (!init) {initDatabase();}

        ArrayList<Integer> annotatorIDs = new ArrayList<Integer>();
        for(int i = 0; i<annotatorIDs.size();i++){
            System.out.println(annotatorIDs.get(i));
        }
        String sqlQuery = "SELECT ID FROM Annotator;";
        try {
            Statement s = con.createStatement();
            s.execute(sqlQuery);
            rs = s.getResultSet();
            while (rs.next()) {
                annotatorIDs.add(rs.getInt(1));
            }
	    rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return annotatorIDs;

    }
    /**
     * returns all non expert annonator IDs for a given discussion in an arraylist
     * @return
     */
    public static ArrayList<Integer> getAnnotatorIDs(int discID) {
        ResultSet rs=null;
        if (!init) {initDatabase();}

        ArrayList<Integer> annotatorIDs = new ArrayList<Integer>();
        for(int i = 0; i<annotatorIDs.size();i++){
            System.out.println(annotatorIDs.get(i));
        }
        String sqlQuery = 
	    "SELECT DISTINCT AnnotatorID FROM Annotations "+
	    " INNER JOIN Utterance ON UtteranceID=Utterance.ID"+
	    " WHERE  DiscID="+discID+";";
        try {
            Statement s = con.createStatement();
            s.execute(sqlQuery);
            rs = s.getResultSet();
            while (rs.next()) {
                annotatorIDs.add(rs.getInt(1));
            }
	    rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return annotatorIDs;

    }


    /**
     * returns all non expert annonator IDs in an arraylist
     * @return
     */
    public static ArrayList<Integer> getExpertAnnotatorIDs() {
        ResultSet rs=null;
        if (!init) {initDatabase();}

        ArrayList<Integer> annotatorIDs = new ArrayList<Integer>();
        for (int i = 0; i < annotatorIDs.size(); i++) {
            System.out.println(annotatorIDs.get(i));
        }
        String sqlQuery = "SELECT ID FROM ExpertAnnotator;";
        try {
            Statement s = con.createStatement();
            s.execute(sqlQuery);
            rs = s.getResultSet();
            while (rs.next()) {
                annotatorIDs.add(rs.getInt(1));
            }
	    rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return annotatorIDs;

    }


    public static int getDiscussionID(String date){
        ResultSet rs=null;

        if (!init) {initDatabase();}


        String sqlQuery = "SELECT ID FROM Discussion WHERE Date='"+date+"';";
        try{
            Statement s = con.createStatement();
            s.execute(sqlQuery);

            rs = s.getResultSet();

            while (rs.next()){
                return rs.getInt(1);
            }
	    rs.close();
        }catch(java.sql.SQLException e){e.printStackTrace();}
        return 0;
    }

    public static String getDiscussionDate(int discID){
        ResultSet rs=null;

        if (!init) {initDatabase();}


        String sqlQuery = "SELECT Date FROM Discussion WHERE ID='"+discID+"';";
        try{
            Statement s = con.createStatement();
            s.execute(sqlQuery);

            rs = s.getResultSet();

            while (rs.next()){
                return rs.getString(1);
            }
	    rs.close();
        }catch(java.sql.SQLException e){e.printStackTrace();}
        return "";
    }
 public static String getAnnotatorName(int annotatorID,boolean isExpert){
        ResultSet rs=null;
        if (!init) {initDatabase();}

        String sqlQuery = "SELECT AName FROM ";
        if(isExpert)
                sqlQuery+="Expert";
        sqlQuery+="Annotator WHERE ID="+annotatorID+";";



        try{
            Statement s = con.createStatement();
            s.execute(sqlQuery);

            rs = s.getResultSet();

            while (rs.next()){
                return rs.getString(1);
            }
	    rs.close();
        }catch(java.sql.SQLException e){e.printStackTrace();}
        return "";

 }

 public static ArrayList<String> getTopicAndFocusFromAnnotations(int uttID, int annotatorID){
        ResultSet rs=null;
        if (!init) {initDatabase();}
        ArrayList<String> topicAndFocus = new ArrayList<String>();
        String topic,focus;

        String sqlQuery = "SELECT topic,focus FROM Annotations"+
                " WHERE UtteranceID="+uttID+" AND AnnotatorID="+annotatorID+";";



        try{
            Statement s = con.createStatement();
            s.execute(sqlQuery);

            rs = s.getResultSet();

            while (rs.next()){
                topic = rs.getString(1);
                focus = rs.getString(2);
                topicAndFocus.add(topic);
                topicAndFocus.add(focus);
            }
	    rs.close();
        }catch(java.sql.SQLException e){e.printStackTrace();}

        return topicAndFocus;

 }

  public static String getTopicFromAnnotations(int uttID, int annotatorID){
        ResultSet rs=null;
        if (!init) {initDatabase();}
        String topic = "";

        String sqlQuery = "SELECT topic FROM Annotations"+
                " WHERE UtteranceID="+uttID+" AND AnnotatorID="+annotatorID+";";



        try{
            Statement s = con.createStatement();
            s.execute(sqlQuery);

            rs = s.getResultSet();

            while (rs.next()){
                topic = rs.getString(1);
            }
	    rs.close();
        }catch(java.sql.SQLException e){e.printStackTrace();}
        return topic;

 }

 public static String getFocusFromAnnotations(int uttID, int annotatorID){
        ResultSet rs=null;
        if (!init) {initDatabase();}
        String focus = "";

        String sqlQuery = "SELECT focus FROM Annotations"+
                " WHERE UtteranceID="+uttID+" AND AnnotatorID="+annotatorID+";";

        try{
            Statement s = con.createStatement();
            s.execute(sqlQuery);

            rs = s.getResultSet();

            while (rs.next()){
                focus = rs.getString(1);
            }
	    rs.close();
        }catch(java.sql.SQLException e){e.printStackTrace();}
        return focus;

 }


     public static ArrayList<Integer> getAnnotatedDiscussions() {
        ResultSet rs=null;
        if (!init) {initDatabase();}

        ArrayList<Integer> discussionArray = new ArrayList<Integer>();
        String sqlQuery = "SELECT DISTINCT DiscID FROM Utterance INNER JOIN" +
                " Annotations ON Utterance.ID=UtteranceID WHERE DiscID <>  10 AND"+
                " DiscID <> 8 ORDER BY DiscID;";


        try {
            Statement s = con.createStatement();
            s.execute(sqlQuery);
            rs = s.getResultSet();
            while (rs.next()) {
                if(!Queries.getDiscussionDate(rs.getInt(1)).equals(MAY_5TH_DISCUSSION))
                    discussionArray.add(rs.getInt(1));
            }
	    rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*sqlQuery = "SELECT DISTINCT DiscID FROM Utterance INNER JOIN" +
                " ExpertAnnotations ON Utterance.ID=UtteranceID ORDER BY DiscID;";
        try {
            s.execute(sqlQuery);
            rs = s.getResultSet();
            while (rs.next()) {
                if (!discussionArray.contains(rs.getInt(1))) {
                    discussionArray.add(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(discussionArray);*/
        return discussionArray;
    }



}
