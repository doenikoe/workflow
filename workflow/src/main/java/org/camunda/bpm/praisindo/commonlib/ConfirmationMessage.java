package org.camunda.bpm.praisindo.commonlib;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.camunda.bpm.praisindo.commonlib.UIParams;

public class ConfirmationMessage implements TaskListener{
	
	private String nodeService = "/notif";
	
	public void notify(DelegateTask task) {
		String errorMessagePushed = (String) task.getVariable("errorMessagePushed");
		String creator = (String) task.getVariable("createdBy");
		String taskID = (String) task.getId();
	
		JSONObject jsonPush = new JSONObject();
		
		UIParams param = new UIParams();
		String nodeUrl = param.getNodeJs();
		//Push error message to socket
		try{
			Client client = Client.create();
			WebResource webResource = client.resource(nodeUrl + nodeService);
			
			jsonPush.put("requestto", creator);
			jsonPush.put("channel", "confirmationMessage");
			jsonPush.put("message", errorMessagePushed);
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
