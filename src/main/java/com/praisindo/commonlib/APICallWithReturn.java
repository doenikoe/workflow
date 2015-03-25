package com.praisindo.commonlib;

import org.camunda.bpm.engine.impl.util.json.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Untuk memanggil web service IMS yang mengembalikan value untuk diproses lebih lanjut
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
			throw new Exception(body);
		}
		
		return result;
	}
			
}
