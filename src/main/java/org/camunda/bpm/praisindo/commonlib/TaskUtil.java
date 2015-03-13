package org.camunda.bpm.praisindo.commonlib;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class TaskUtil {
	private final static Logger LOGGER = Logger.getLogger(UIParams.class.getName());
	
	private UIParams param = new UIParams();
	private DB db = param.db;
	private DBCollection collection;
	
		
	/**
	 * Insert task baru ke mongoDB
	 */
	public void insertTask(JSONObject json){
		try{			
			collection = db.getCollection("task");
			DBObject obj = (DBObject) JSON.parse(json.toString());
			collection.insert(obj);			
			LOGGER.info("New task has been inserted.");
		}catch(Exception e){
			LOGGER.warning("Could not insert new task, "+e);
		}		
	}
	
	/**
	 * Update task di mongoDB
	 */
	public void updateTask(String taskID, BasicDBObject document){
		try{
			collection = db.getCollection("task");
			BasicDBObject searchQuery = new BasicDBObject().append("taskID", taskID);															 									 
			collection.update(searchQuery, document);
			LOGGER.info("Task has been completed.");
		}catch(Exception e){
			LOGGER.warning("Could not update the task, "+e);
		}
	}
}
