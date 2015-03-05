package org.camunda.bpm.praisindo.oms;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.praisindo.commonlib.AppMessage;
import org.camunda.bpm.praisindo.commonlib.TaskUtil;
import org.camunda.bpm.praisindo.commonlib.UIParams;

import com.mongodb.BasicDBObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class AllocationAndOrder implements JavaDelegate {
	//private final static Logger LOGGER = Logger.getLogger(AllocationAndOrder.class.getName());
	
	public void execute(DelegateExecution execution) throws Exception {
		String user = (String) execution.getVariable("assignTo");
		String serviceName = (String) execution.getVariable("svcAddress");
		String serviceMethod = (String) execution.getVariable("svcMethod");
		String JSONData = (String) execution.getVariable("jsonData");
		String taskID = (String) execution.getVariable("taskID");
		
		UIParams param = new UIParams();
		String complianceWs = param.getOmsWs();				
		
		AppMessage errorNotif = new AppMessage();
		
		TaskUtil taskUtil = new TaskUtil();
		
		//invoke web services	
	    try{
	    	Client client = Client.create();
	    	ClientResponse response = null;
	    	
	    	WebResource webResource = client.resource(complianceWs + serviceName);
	    	if(serviceMethod.equals("post")){
	    		response = webResource.type("application/json").post(ClientResponse.class, JSONData);
	    	}else if (serviceMethod.equals("put")){
	    		response = webResource.type("application/json").put(ClientResponse.class, JSONData);
	    	}else{
	    		errorNotif.execute(user, "Cannot recognize the method.");
	    		throw new Exception("Cannot recognize the method.");
	    	}
	    	
			if (response.getStatus() == 200) {
				String wsResult = response.getEntity(String.class);
				JSONObject wsResponse = new JSONObject(wsResult);		
				String errorCode = wsResponse.getString("ErrorCode");
				Boolean isSuccess = wsResponse.getBoolean("IsSuccess");
				String message = wsResponse.getString("Message");				
				
				String solutionCode = "";				
				if(wsResponse.has("SolutionCode")){
					solutionCode = wsResponse.getString("SolutionCode");
					if(!isSuccess){
						errorNotif.execute(user, message);
						throw new Exception("Err("+errorCode+") "+message+", "+solutionCode);
					}
				}
				
				String result = "";				
				if(wsResponse.has("Result")){
					result = wsResponse.getString("Result");
					if(isSuccess){
						execution.setVariable("Result", result);						
					}else{
						errorNotif.execute(user, message);
						throw new Exception("Err("+errorCode+") "+message+", "+solutionCode);
					}
				}
				
				//Update task status jadi completed
				BasicDBObject newDocument = new BasicDBObject();
				newDocument.append("$set", new BasicDBObject().append("isCompleted", true));
				taskUtil.updateTask(taskID, newDocument);
				
			//Kalo berhasil invoke ws tapi statusnya nggak 200	
			}else{
				String message = response.getResponseStatus().toString();
				errorNotif.execute(user, message);
				throw new Exception("Err("+response.getStatus()+") "+message);
			}
		//Kalo gagal invoke ws	
	    }catch(Exception e){	    	
	    	errorNotif.execute(user, e.getMessage());
	    	throw new Exception("Err("+e.hashCode()+") " + e.getMessage());
	    }		    	
	  }
}