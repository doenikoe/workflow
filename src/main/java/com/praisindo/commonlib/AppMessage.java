package com.praisindo.commonlib;

import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.praisindo.commonlib.UIParams;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class AppMessage{
	
	private String nodeService = "/notif";
	
	public void notification(String recipient, String message) {				
		JSONObject jsonPush = new JSONObject();
		
		UIParams param = new UIParams();
		String nodeUrl = param.getNodeJs();
		//Push error message to socket
		try{
			Client client = Client.create();
			WebResource webResource = client.resource(nodeUrl + nodeService);
			
			jsonPush.put("requestto", recipient);
			jsonPush.put("channel", "applicationMessage");
			jsonPush.put("message", message);			
			
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
