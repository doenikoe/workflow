package com.praisindo.oms;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.praisindo.commonlib.APICallWithReturn;
import com.praisindo.commonlib.TaskUtil;
import com.praisindo.commonlib.UIParams;

public class SaveAllocation implements JavaDelegate{
	private String default_serviceName = "/AllocationAndOrder.svc/PTP_PRT_PUT_OrderAndAllocation_Insert/0";
	private String default_serviceMethod = "PUT";
	private APICallWithReturn call = new APICallWithReturn();	
	private UIParams param = new UIParams();
	private TaskUtil taskUtil = new TaskUtil();
	private Expression serviceName;
	private Expression serviceMethod;
	
	public void execute(DelegateExecution execution) throws Exception {
		//Check apakah ada field injection serviceName				
		if(serviceName != null){
			//field injection di set oleh user, gunakan value ini sebagai service name
			String injectedServiceName = serviceName.getValue(execution).toString();			
			this.default_serviceName = injectedServiceName;
		}
		//Check apakah ada field injection serviceMethod		
		if(serviceMethod != null){
			//field injection di set oleh user, gunakan value ini sebagai service method
			String injectedServiceMethod = serviceMethod.getValue(execution).toString();
			this.default_serviceMethod = injectedServiceMethod;
		}
		
		String address = param.getOmsWs() + default_serviceName;	
		String data = (String) execution.getVariable("jsonData");				
		
		String result = call.request(address, default_serviceMethod, data);
		JSONArray json = new JSONArray(result);		
		List<String> listResult = new ArrayList<String>();
		for(int i=0; i<json.length(); i++){
			JSONObject obj = new JSONObject(json.get(i).toString());
			listResult.add(obj.get("TOrderID").toString());
		}		
		execution.setVariable("OrderList", listResult);
		execution.setVariable("OrderQuantity", listResult.size());
		
		//Update task status jadi completed
		String taskID = (String) execution.getVariable("taskID");
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.append("$set", new BasicDBObject().append("isCompleted", true));
		taskUtil.updateTask(taskID, newDocument);			
		
	}

}
