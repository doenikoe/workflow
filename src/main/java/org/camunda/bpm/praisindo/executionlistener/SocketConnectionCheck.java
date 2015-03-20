package org.camunda.bpm.praisindo.executionlistener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.praisindo.commonlib.UIParams;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class SocketConnectionCheck implements ExecutionListener {			
	public void notify(DelegateExecution execution) throws Exception{				
		UIParams param = new UIParams();			
		String nodeUrl = param.getNodeJs();																																	

		try{
			Client nodeClient = Client.create();					
			WebResource nodeResource = nodeClient.resource(nodeUrl);
			ClientResponse nodeResponse = nodeResource.type("application/json").get(ClientResponse.class);					
			if (nodeResponse.getStatus() != 200) {													
				String body = nodeResponse.getEntity(String.class);
				throw new Exception("["+getClass().getName()+"] "+body);
			}
		}catch(Exception e){
			throw new Exception("["+getClass().getName()+"] "+ e.getMessage());
		}															
	}
}