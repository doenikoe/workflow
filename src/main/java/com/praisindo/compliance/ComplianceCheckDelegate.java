package com.praisindo.compliance;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.praisindo.commonlib.UIParams;
import com.praisindo.executionlistener.ComplianceDataIsolation;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ComplianceCheckDelegate implements JavaDelegate{
		
	private String default_serviceName = "/AllocationAndOrder.svc/PTP_PRT_POST_OrderAndAllocation_ValidateAll";
	private Expression serviceName;
	private Expression validationData;

	public void execute(DelegateExecution execution) throws Exception {		
		String warningCode = "";
		String warningMessage = "";
		UIParams param = new UIParams();
		String complianceWs = param.getOmsWs();
		ComplianceDataIsolation isolation = new ComplianceDataIsolation();				
		
		//Check apakah ada field injection serviceName, jika ada maka value default_serviceName akan di override		
		if(serviceName != null){
			//field injection di set oleh user, gunakan value ini sebagai service name
			String injectedServiceName = serviceName.getValue(execution).toString();
			this.default_serviceName = injectedServiceName;
		}		
		
		String dataRequest = "";
		//Check apakah ada field injection validationData
		if(validationData != null){										
			JSONArray jsonData = new JSONArray(execution.getVariable("jsonData").toString());			
			String key = validationData.getValue(execution).toString();								
			dataRequest = isolation.getValidationData(jsonData, key).toString();	
		}else{
			dataRequest = execution.getVariable("validationData").toString();
		}
		
		try{			
			Client client = Client.create();
			
			WebResource webResource = client.resource(complianceWs + this.default_serviceName);
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, dataRequest);
			if (response.getStatus() == 200) {
				String wsResult = response.getEntity(String.class);
				JSONObject wsResponse = new JSONObject(wsResult);		
				String errorCode = wsResponse.getString("ErrorCode");
				Boolean isSuccess = wsResponse.getBoolean("IsSuccess");
				String message = wsResponse.getString("Message");
				String result = wsResponse.getString("Result");
				if(isSuccess == true){
					JSONArray resultArray = new JSONArray(result);									
					for(int i=0;i<resultArray.length();i++){												
						Boolean isValid = resultArray.getJSONObject(i).getBoolean("IsValid");
						//Invalid checking detected by compliance
						if(!isValid){														
							warningCode = resultArray.getJSONObject(i).getString("WarningCode").toString();
							warningMessage = resultArray.getJSONObject(i).getString("Message").toString();							
						}
					}
				}else{					
					throw new Exception("Err("+errorCode+") "+message);
				}
				
				if(warningMessage == ""){
					execution.setVariable("complianceChecking", true);	
				}else{
					execution.setVariable("complianceChecking", false);	
					execution.setVariable("message", warningMessage);					
					execution.setVariable("warningCode", warningCode);										
				}
				
			}else{
				String body = response.getEntity(String.class);
				throw new Exception("Err("+response.getStatus()+") "+body);
			}												
			
		}catch(Exception e){
			//LOGGER.info(e.getMessage());
			throw new Exception("Err("+e.hashCode()+") " + e.getMessage());			
		}
	  }	
}
