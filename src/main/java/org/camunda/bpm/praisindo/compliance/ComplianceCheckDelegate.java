package org.camunda.bpm.praisindo.compliance;

import java.util.HashMap;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.praisindo.commonlib.UIParams;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ComplianceCheckDelegate implements JavaDelegate{
	
	//private final static Logger LOGGER = Logger.getLogger("OMS");	
	private String default_serviceName = "/AllocationAndOrder.svc/PTP_PRT_POST_OrderAndAllocation_ValidateAll";		//Jika "field injection" ada maka property ini akan di overwrite dg value dari field injection
	private Expression serviceName;
	private Expression validationData;

	public void execute(DelegateExecution execution) throws Exception {		
		String warningCode = "";
		String warningMessage = "";
		UIParams param = new UIParams();
		//Seharusnya ws address untuk compliance akan selalu sama di semua modul OMS, untuk sementara disamakan dengan omsws
		String complianceWs = param.getOmsWs();
		
		//Check apakah ada field injection serviceName		
		if(serviceName != null){
			//field injection di set oleh user, gunakan value ini sebagai service name
			String injectedServiceName = serviceName.getValue(execution).toString();
			this.default_serviceName = injectedServiceName;
		}		
						
		String complianceData = validationData.getValue(execution).toString();
		
		String[] strArrayValidation = complianceData.split(",");
		if(strArrayValidation.length==1){
			complianceData = (String) execution.getVariable(complianceData);
		}else{
			JSONObject jsonValidation = new JSONObject();			
			for(int i=0;i<strArrayValidation.length;i++){
				String key = strArrayValidation[i].toString();
				String value = (String) execution.getVariable(key);							
				jsonValidation.put(key, value);							
			}
			complianceData = jsonValidation.toString();
		}			
		
		try{			
			Client client = Client.create();
			
			WebResource webResource = client.resource(complianceWs + this.default_serviceName);
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, complianceData);
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
					execution.setVariable("message", warningMessage);					
					execution.setVariable("warningCode", warningCode);										
				}
				
			}else{
				String body = response.getEntity(String.class);
				throw new Exception("Err("+response.getStatus()+") "+body);
			}									
			
			//data bundle for next process -> save allocation			
			JSONObject jsonAllocation = new JSONObject();
			jsonAllocation.put("Date",(String) execution.getVariable("Date"));
			jsonAllocation.put("DealType",(String) execution.getVariable("DealType"));
			jsonAllocation.put("InstrumentCode",(String) execution.getVariable("InstrumentCode"));
			jsonAllocation.put("InvClass",(String) execution.getVariable("InvClass"));
			jsonAllocation.put("MFValueType",(String) execution.getVariable("MFValueType"));
			jsonAllocation.put("OrderNo",(String) execution.getVariable("OrderNo"));
			jsonAllocation.put("OrderPeriod",(String) execution.getVariable("OrderPeriod"));
			jsonAllocation.put("OrderType",(String) execution.getVariable("OrderType"));
			jsonAllocation.put("Price",(String) execution.getVariable("Price"));
			jsonAllocation.put("Remarks",(String) execution.getVariable("Remarks"));
			jsonAllocation.put("TAllocation",(String) execution.getVariable("TAllocation"));
			jsonAllocation.put("TOrderID",(String) execution.getVariable("TOrderID"));
			jsonAllocation.put("Units",(String) execution.getVariable("Units"));
			jsonAllocation.put("sysCreatedBy",(String) execution.getVariable("sysCreatedBy"));
			jsonAllocation.put("sysModifiedBy",(String) execution.getVariable("sysModifiedBy"));
			execution.setVariable("allocationData", jsonAllocation.toString());
			
		}catch(Exception e){
			//LOGGER.info(e.getMessage());
			throw new Exception("Err("+e.hashCode()+") " + e.getMessage());			
		}
	  }
}
