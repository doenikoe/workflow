package org.camunda.bpm.praisindo.oms;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.praisindo.commonlib.APICallWithReturn;
import org.camunda.bpm.praisindo.commonlib.TaskUtil;
import org.camunda.bpm.praisindo.commonlib.UIParams;

import com.mongodb.BasicDBObject;

public class OrderExecution implements JavaDelegate{
	private String default_serviceName = "/AllocationAndOrder.svc/PTP_PRT_PUT_Execution_Insert";
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
		
		call.request(address, default_serviceMethod, data);
		
		//Update task status jadi completed
		String taskID = (String) execution.getVariable("taskID");
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.append("$set", new BasicDBObject().append("isCompleted", true));
		taskUtil.updateTask(taskID, newDocument);
	}
}