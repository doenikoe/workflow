package org.camunda.bpm.praisindo.oms;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.praisindo.commonlib.APICallWithReturn;
import org.camunda.bpm.praisindo.commonlib.UIParams;

public class AllocationApproval implements JavaDelegate{
	private String serviceName = "/AllocationAndOrder.svc/PTP_PRT_POST_OrderAndAllocation_Approve";
	private String serviceMethod = "POST";
	private APICallWithReturn call = new APICallWithReturn();	
	private UIParams param = new UIParams();	
	
	public void execute(DelegateExecution execution) throws Exception {
		//Check apakah ada field injection serviceName
		String injectedServiceName = (String) execution.getVariable("serviceName");
		if(injectedServiceName != null && !injectedServiceName.isEmpty()){
			//field injection di set oleh user, gunakan value ini sebagai service name
			this.serviceName = injectedServiceName;
		}
		//Check apakah ada field injection serviceMethod
		String injectedServiceMethod = (String) execution.getVariable("serviceMethod");
		if(injectedServiceMethod != null && !injectedServiceMethod.isEmpty()){
			//field injection di set oleh user, gunakan value ini sebagai service method
			this.serviceMethod = injectedServiceMethod;
		}
		
		String address = param.getOmsWs() + serviceName;	
		String data = (String) execution.getVariable("orderID");				
		
		call.request(address, serviceMethod, data);				
	}

}
