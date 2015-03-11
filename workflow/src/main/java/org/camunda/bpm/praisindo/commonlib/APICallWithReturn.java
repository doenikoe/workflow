package org.camunda.bpm.praisindo.commonlib;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Untuk memanggil web service IMS yang tidak mengembalikan value untuk diproses lebih lanjut
 * 
 * @author M.DONI
 */
public class APICallWithReturn{
	
	public String request(String address, String method, String data) throws Exception{
		String result = "";	
		Client client = Client.create();
		
		WebResource webResource = client.resource(address);
		ClientResponse response = webResource.type("application/json").method(method, ClientResponse.class, data);
		
		if (response.getStatus() == 200) {
			String wsResult = response.getEntity(String.class);
			JSONObject wsResponse = new JSONObject(wsResult);		
			String errorCode = wsResponse.getString("ErrorCode");
			Boolean isSuccess = wsResponse.getBoolean("IsSuccess");
			String message = wsResponse.getString("Message");
			if(wsResponse.has("Result"))
				result = wsResponse.getString("Result");						
		}else{
			String body = response.getEntity(String.class);
			throw new Exception("Err("+response.getStatus()+") "+body);
		}
		
		return result;
	}
			
}
