package com.praisindo.commonlib;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.praisindo.commonlib.UIParams;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class UserConfirmation implements TaskListener{
	
	private String nodeService = "/notif";
	
	public void notify(DelegateTask task) {
		String message = (String) task.getVariable("message");
		String creator = (String) task.getVariable("sysCreatedBy");
		String taskID = (String) task.getId();
	
		JSONObject jsonPush = new JSONObject();
		
		UIParams param = new UIParams();
		String nodeUrl = param.getNodeJs();
		//Push error message to socket
		try{
			Client client = Client.create();
			WebResource webResource = client.resource(nodeUrl + nodeService);
			
			jsonPush.put("requestto", creator);
			jsonPush.put("channel", "userConfirmation");
			jsonPush.put("message", message);
			jsonPush.put("callBackUserTaskID", taskID);
			
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, jsonPush.toString());
			if (response.getStatus() != 200) {
				String body = response.getEntity(String.class);
				throw new Exception("Err("+response.getStatus()+") "+body);
			}
		}catch(Exception e){
			try {
				throw new Exception("Err("+e.hashCode()+") " + e.getMessage());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
}
