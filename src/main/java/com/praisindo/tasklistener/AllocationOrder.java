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
		JSONArray variables = new JSONArray();
		
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
				variables.put(new JSONObject("{_ordinal:1, name:TOrderID, dataType:String, value:"+obj.get("TOrderID")+", editable:false, hidden:false}"));
				variables.put(new JSONObject("{_ordinal:2, name:DealType, dataType:String, value:"+obj.get("DealType")+", editable:false, hidden:false}"));
				variables.put(new JSONObject("{_ordinal:3, name:InstrumentCode, dataType:String, value:"+obj.get("InstrumentCode")+", editable:false, hidden:false}"));
				variables.put(new JSONObject("{_ordinal:4, name:OrderType, dataType:String, value:"+obj.get("OrderType")+", editable:false, hidden:false}"));
				variables.put(new JSONObject("{_ordinal:5, name:OrderPeriod, dataType:String, value:"+obj.get("OrderPeriod")+", editable:false, hidden:false}"));
				variables.put(new JSONObject("{_ordinal:6, name:Date, dataType:Date, value:\""+obj.get("Date")+"\", editable:false, hidden:false}"));			
				variables.put(new JSONObject("{_ordinal:7, name:Price, dataType:float, value:"+obj.get("Price")+", editable:false, hidden:false}"));
				variables.put(new JSONObject("{_ordinal:8, name:Unit, dataType:float, value:"+obj.get("Units")+", editable:false, hidden:false}"));
				variables.put(new JSONObject("{_ordinal:9, name:Status, dataType:String, value:"+obj.get("Status")+", editable:false, hidden:false}"));
				variables.put(new JSONObject("{_ordinal:10, name:Remarks, dataType:String, value:\""+obj.get("Remarks")+"\", editable:false, hidden:false}"));																									
			}										
			
			task.setVariableLocal("Records",variables.toString());
			
			JSONArray jsonButton = new JSONArray();
			jsonButton.put(new JSONObject("{name:Approve, value:true}"));
			jsonButton.put(new JSONObject("{name:Unapprove, value:false}"));
			task.setVariableLocal("Button", jsonButton.toString());
			
			JSONArray jsonForm = new JSONArray();
			jsonForm.put(new JSONObject("{name:IsApproved, dataType:boolean}"));			
			task.setVariableLocal("FormData", jsonForm.toString());
			
			orderList.remove(i);			
			task.setVariable("OrderList", orderList);		
			JSONObject jsonPush = new JSONObject();
			
			JSONArray jsonOrder = new JSONArray();
			jsonOrder.put(new JSONObject("{TOrderID:"+TOrderID+"}"));			
			task.setVariableLocal("TOrderID",jsonOrder.toString());
			
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
