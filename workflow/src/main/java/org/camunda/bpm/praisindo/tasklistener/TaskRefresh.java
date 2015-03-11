package org.camunda.bpm.praisindo.tasklistener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.text.DateFormatter;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.praisindo.commonlib.AppMessage;
import org.camunda.bpm.praisindo.commonlib.TaskUtil;
import org.camunda.bpm.praisindo.commonlib.UIParams;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class TaskRefresh implements TaskListener{	
	private String nodeService = "/notif";
	
	public void notify(DelegateTask task) {
		String taskID = task.getId();
		String taskName = task.getName();		
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String created = format.format(task.getCreateTime());		
		String assignee = task.getAssignee();
		String processDefinitionId = task.getProcessDefinitionId();				
		
		UIParams param = new UIParams();			
		String nodeUrl = param.getNodeJs();
		
		JSONObject jsonPush = new JSONObject();
		
		AppMessage errorNotif = new AppMessage();
		
		TaskUtil taskUtil = new TaskUtil();
		
		//Get process definition information
		try{
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
				
				//isi json object
				jsonPush.put("taskID", taskID);
				jsonPush.put("taskName", taskName);
				jsonPush.put("created", created);
				jsonPush.put("assignee", assignee);
				jsonPush.put("processDefinitionName", processDefinitionName);
				jsonPush.put("channel", "taskRefresh");																						
				
				//push json object to socket
				try{
					Client nodeClient = Client.create();					
					WebResource nodeResource = nodeClient.resource(nodeUrl + nodeService);
					ClientResponse nodeResponse = nodeResource.type("application/json").post(ClientResponse.class, jsonPush.toString());					
					if (nodeResponse.getStatus() == 200) {
						//insert to mongodb	
						jsonPush.put("isCompleted", false);
						taskUtil.insertTask(jsonPush);
						jsonPush.remove("isCompleted");											
					}else{ 					
						String body = nodeResponse.getEntity(String.class);
						throw new Exception("Err("+nodeResponse.getStatus()+") "+body);
					}
				}catch(Exception e){
					throw new Exception("Err("+e.hashCode()+") " + e.getMessage());
				}
									
			}else {
				String message = response.getResponseStatus().toString();
				errorNotif.execute(assignee, message);
				throw new Exception("Err("+response.getStatus()+") "+message);
			}		
		}catch(Exception e){
			try {
				errorNotif.execute(assignee, e.getMessage());
				throw new Exception("Err("+e.hashCode()+") " + e.getMessage());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
