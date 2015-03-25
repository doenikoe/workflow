package com.praisindo.tasklistener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.praisindo.commonlib.APICallWithReturn;
import com.praisindo.commonlib.TaskUtil;
import com.praisindo.commonlib.UIParams;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class AllocationOrder implements TaskListener{
	private UIParams param = new UIParams();
	private String default_serviceName = "/AllocationAndOrder.svc/PTP_PRT_GET_OrderAndAllocation_GetByOrderID/";
	private APICallWithReturn call = new APICallWithReturn();	
	private TaskRefresh taskPush = new TaskRefresh();
	private TaskUtil taskUtil = new TaskUtil();

	public void notify(DelegateTask task){
		JSONObject variables = new JSONObject();
		
		List<String> orderList = new ArrayList<String>();
		orderList = (List<String>) task.getVariable("OrderList");
		
		int i = orderList.size()-1;						
		String TOrderID = orderList.get(i);																	
		String address = param.getOmsWs() + default_serviceName + TOrderID;
										
		try{
			String result = call.request(address, "GET", null);				
			JSONArray resultArray = new JSONArray(result);
			
			for(int j=0; j<resultArray.length(); j++){
				JSONObject obj = new JSONObject(resultArray.get(j).toString());					
				variables.put("TOrderID", obj.get("TOrderID"));
				variables.put("DealType", obj.get("DealType"));
				variables.put("InstrumentCode", obj.get("InstrumentCode"));
				variables.put("OrderType", obj.get("OrderType"));
				variables.put("OrderPeriod", obj.get("OrderPeriod"));
				variables.put("Date", obj.get("Date"));
				variables.put("Price", obj.get("Price"));
				variables.put("Status", obj.get("Status"));
				variables.put("Remarks", obj.get("Remarks"));
									
				task.setVariableLocal("LocalVariables",variables.toString());
				task.setVariableLocal("IsApproved", "i-"+false);
				task.setVariableLocal("Button", "Approve|Unapprove");									
			}				
																							
			orderList.remove(i);			
			task.setVariable("OrderList", orderList);		
			JSONObject jsonPush = new JSONObject();
			
			//socket push
			try{
				taskPush.notify(task);				
				jsonPush = taskPush.getDataBundle();
			}catch(Exception e){
				throw new Exception(e.getMessage());
			}
						
			//mongodb push add data variable
			try{
				BasicDBObject newDocument = new BasicDBObject();
				newDocument.append("$set", new BasicDBObject().append("variables", variables.toString()));
				taskUtil.updateTask(task.getId(), newDocument);
			}catch(Exception e){
				throw new Exception(e.getMessage());
			}
																	
		}catch(Exception e){
			try {
				throw new Exception(e.getMessage());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
}
