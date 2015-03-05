package org.camunda.bpm.praisindo.compliance;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.praisindo.commonlib.UIParams;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ComplianceCheckDelegate implements JavaDelegate{
	
	//private final static Logger LOGGER = Logger.getLogger("OMS");	
	private String serviceName = "/AllocationAndOrder.svc/PTP_PRT_POST_OrderAndAllocation_ValidateAll";		//Jika "field injection" ada maka property ini akan di overwrite dg value dari field injection

	public void execute(DelegateExecution execution) throws Exception {		
		String warningCode = "";
		String warningMessage = "";
		UIParams param = new UIParams();
		String complianceWs = param.getOmsWs(); //Seharusnya ws address untuk compliance akan selalu sama di semua modul OMS, untuk sementara disamakan dengan omsws
		
		//Check apakah ada field injection serviceName
		String injectedServiceName = (String) execution.getVariable("serviceName");
		if(injectedServiceName != null && !injectedServiceName.isEmpty()){
			//field injection di set oleh user, gunakan value ini sebagai service name
			this.serviceName = injectedServiceName;
		}
		
		String JSONData = (String) execution.getVariable("jsonData");		
		
		try{			
			Client client = Client.create();
			
			WebResource webResource = client.resource(complianceWs + this.serviceName);
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, JSONData);
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
							//invalidCheckingDetected += resultArray.getJSONObject(i-1).getString("Message").toString() + "|";							
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
					execution.setVariable("errorMessagePushed", warningMessage);					
					execution.setVariable("warningCode", warningCode);					
					System.out.print(execution.getVariable("warningCode"));
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
