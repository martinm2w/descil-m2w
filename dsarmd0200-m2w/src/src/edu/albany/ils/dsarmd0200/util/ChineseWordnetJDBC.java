/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.albany.ils.dsarmd0200.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * m2w: this class is the util class for chinese wordnet jdbc
 * @author ruobo
 * @date Jun 29, 2011
 */
public class ChineseWordnetJDBC {
    //	====================================Attributes=============================================
     String dbms;
     String serverName;
     String portNumber;
     String dbName;
//      ===================================init & const============================================
    public ChineseWordnetJDBC(String dbms, String serverName, String portNumber, String dbName) {
        super();
        this.dbms = dbms;
        this.serverName = serverName;
        this.portNumber = portNumber;
        this.dbName = dbName;
    }
//	====================================main method============================================

//	===================================util methods============================================
        /**
     * m2w: this method is used to get the connection from the chinese wordnet database
     * @param username
     * @param password
     * @return
     * @throws SQLException
     * @date 6/22/11 1:08 PM
     */
    public Connection getConnection(String username, String password) throws SQLException{
        Connection conn = null;
        
        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);
//        System.out.println("testing props: " + connectionProps);
        
        if(this.dbms.equals("mysql")){
//            Class.forName("com.mysql.jdbc.Driver").newInstance(); //don't actually need
            conn = DriverManager.getConnection("jdbc:" + this.dbms + "://" + this.serverName + ":" + this.portNumber + "/" + this.dbName +"?useUnicode=true&amp;characterEncoding=UTF-8", connectionProps);
        }
//        System.out.println("Connected to database");
        return conn;
    }
//      =================================setters & getters=========================================
}
