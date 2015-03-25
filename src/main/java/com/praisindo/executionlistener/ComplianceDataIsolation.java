package com.praisindo.executionlistener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

public class ComplianceDataIsolation implements ExecutionListener {

	public void notify(DelegateExecution execution) throws Exception {
		JSONArray jsonData = new JSONArray(execution.getVariable("jsonData").toString());
		String validationData = this.getValidationData(jsonData, "TAllocation").toString();
		
		execution.setVariable("validationData", validationData);
		execution.setVariable("sysCreatedBy", (jsonData.getJSONObject(0)).get("sysCreatedBy"));
	}
	
	/*
	 * Memisahkan data untuk validasi
	 */
	public JSONArray getValidationData(JSONArray json, String key){										
		JSONArray someJson = new JSONArray();
		for(int i=0;i<json.length();i++){
			JSONObject obj = new JSONObject(json.get(i).toString());						
			JSONArray arrayTemp = obj.getJSONArray(key);
			for(int j=0;j<arrayTemp.length();j++){
				obj = new JSONObject(arrayTemp.get(j).toString());
				someJson.put(obj);
			}			
		}		
		return someJson;		
	}

}
