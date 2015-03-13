package org.camunda.bpm.praisindo.commonlib;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.camunda.bpm.praisindo.commonlib.UIParams;

public class ValidationMessage implements JavaDelegate{
	
	private String nodeService = "/notif";
	
	public void execute(DelegateExecution execution) throws Exception {
		String message = (String) execution.getVariable("message");
		String creator = (String) execution.getVariable("sysCreatedBy");
	
		JSONObject jsonPush = new JSONObject();
		
		UIParams param = new UIParams();
		String nodeUrl = param.getNodeJs();
		//Push error message to socket
		try{
			Client client = Client.create();
			WebResource webResource = client.resource(nodeUrl + nodeService);
			
			jsonPush.put("requestto", creator);
			jsonPush.put("channel", "validationError");
			jsonPush.put("message", message);			
			
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, jsonPush.toString());
			if (response.getStatus() != 200) {
				String body = response.getEntity(String.class);
				throw new Exception("Err("+response.getStatus()+") "+body);
			}
		}catch(Exception e){
			throw new Exception("Err("+e.hashCode()+") " + e.getMessage());
		}
	}
}
