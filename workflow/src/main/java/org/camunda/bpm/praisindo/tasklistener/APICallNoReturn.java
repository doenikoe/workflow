package org.camunda.bpm.praisindo.tasklistener;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.praisindo.commonlib.UIParams;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Untuk memanggil web service IMS yang tidak mengembalikan value untuk diproses lebih lanjut
 * 
 * @author M.DONI
 */
public class APICallNoReturn implements TaskListener{
	
	public void notify(DelegateTask task) {
		
	}
}
