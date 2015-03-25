package com.praisindo.commonlib;

import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class UIParams {
	
	private MongoClient mongoClient;
	protected DB db;
	private DBCollection collection;
	private String host = "192.168.69.26";//chivas
	private int port = 27017;
	
	private String dbName = "ims";
	private String collectionName = "params";	
	
	private String omsWs = null;	
	private String nodeJs = null;
	public  String camunda = "http://localhost:8080/engine-rest";
	
	//setting email pengirim
	private String MAIL_HOST = "smtp.gmail.com";
	private String MAIL_USER = "praisindotesting@gmail.com";
	private String MAIL_PWD = "praisindo2015";
	
	@SuppressWarnings("deprecation")
	public UIParams(){
		//Set connection to mongodb
		mongoClient = new MongoClient(host, port);
		db = mongoClient.getDB(dbName);
		collection = db.getCollection(collectionName);
	}	
	
	/** 
	 * @return String omsws address
	 */
	public String getOmsWs(){
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("varName", "omsws_root");
		DBCursor cursor = this.collection.find(searchQuery);
		while (cursor.hasNext()){
			DBObject o = cursor.next();			
		    this.omsWs = o.get("varValue").toString();		    
		}
		
		return this.omsWs;
	}
	
	/**
	 * Mendapatkan url dari nodejs
	 * @return
	 */
	public String getNodeJs(){
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("varName", "nodeUrl");
		DBCursor cursor = this.collection.find(searchQuery);
		while (cursor.hasNext()){
			DBObject o = cursor.next();			
		    this.nodeJs = o.get("varValue").toString();		    
		}
		
		return this.nodeJs;
	}
	
	/*
	 * Session Email pengirim notifikasi
	 */
	public Session getMailSession(){
		Properties props = new Properties();
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.host", MAIL_HOST);
	    props.put("mail.smtp.port", "25");
	    
	    //Get the Session object.
	    Session session = Session.getInstance(props,
	       new javax.mail.Authenticator() {
	          protected PasswordAuthentication getPasswordAuthentication() {
	             return new PasswordAuthentication(MAIL_USER, MAIL_PWD);
	          }
	       });
	    
	    return session;	    
	}
}
