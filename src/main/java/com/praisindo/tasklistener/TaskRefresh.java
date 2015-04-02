package com.praisindo.tasklistener;

import java.text.SimpleDateFormat;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import com.praisindo.commonlib.AppMessage;
import com.praisindo.commonlib.TaskUtil;
import com.praisindo.commonlib.UIParams;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class TaskRefresh implements TaskListener{	
	private String nodeService = "/notif";
	private UIParams param = new UIParams();
	private AppMessage appMessage = new AppMessage();
	private TaskUtil taskUtil = new TaskUtil();
	private JSONObject bundle = new JSONObject();
	
	public void notify(DelegateTask task) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		String taskID = task.getId();
		String taskName = task.getName();				
		String created = format.format(task.getCreateTime());		
		String assignee = task.getAssignee();
		String processDefinitionId = task.getProcessDefinitionId();							
		String nodeUrl = param.getNodeJs();
		
		JSONObject jsonPush = new JSONObject();								
														
		try{			
			//invoke rest-engine camunda untuk mengambil informasi task 
			Client client = Client.create();
			WebResource webResource = client.resource(param.camunda + "/process-definition/" + processDefinitionId);
			ClientResponse response = webResource.get(ClientResponse.class);									
			if(response.getStatus() == 200){
				
				String restResult = response.getEntity(String.class);
				JSONObject restResultObject = new JSONObject(restResult);		
				String processDefinitionName = restResultObject.getString("name");

				//untuk keperluan di service task
				task.setVariable("taskID", taskID);
				task.setVariable("taskName", taskName);
				task.setVariable("assignee", assignee);
				//

				jsonPush.put("taskID", taskID);
				jsonPush.put("taskName", taskName);
				jsonPush.put("created", created);
				jsonPush.put("assignee", assignee);
				jsonPush.put("processDefinitionName", processDefinitionName);				
				jsonPush.put("channel", "taskRefresh");																									
				
				//push json object to socket			
				Client nodeClient = Client.create();					
				WebResource nodeResource = nodeClient.resource(nodeUrl + nodeService);
				ClientResponse nodeResponse = nodeResource.type("application/json").post(ClientResponse.class, jsonPush.toString());					
				if (nodeResponse.getStatus() == 200) {
					//insert to mongoDB
					jsonPush.put("variables", new JSONArray("[]"));
					jsonPush.put("isCompleted", false);
					taskUtil.insertTask(jsonPush);
					//
				}else{ 					
					String body = nodeResponse.getEntity(String.class);
					throw new Exception("Err("+nodeResponse.getStatus()+") "+body);
				}
				//									
			}else {
				String message = response.getResponseStatus().toString();
				appMessage.notification(assignee, message);
				throw new Exception("Err("+response.getStatus()+") "+message);
			}		
		}catch(Exception e){			
			try {
				appMessage.notification(assignee, e.getMessage());
				throw new Exception("Err("+e.hashCode()+") " + e.getMessage());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void setDataBundle(JSONObject json){
		this.bundle = json;
	}
	
	public JSONObject getDataBundle(){
		return this.bundle;
	}
	
}
