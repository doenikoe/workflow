package org.camunda.bpm.praisindo.commonlib;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;

public class SendEmailNotification implements JavaDelegate{
	private final static Logger LOGGER = Logger.getLogger(SendEmailNotification.class.getName());
	private UIParams param = new UIParams();

	public void execute(DelegateExecution execution) throws Exception {
		String assignee = (String) execution.getVariable("assignee");
	    String taskId = (String) execution.getVariable("taskID");
	    String taskName = (String) execution.getVariable("taskName");	    	    	    	    

	    if (assignee != null) {	      
	      // Get User Profile from User Management
	      IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();
	      User user = identityService.createUserQuery().userId(assignee).singleResult();

	      if (user != null) {	        
	        // Get Email Address from User Profile
	        String recipient = user.getEmail();
	        
	        if (recipient != null && !recipient.isEmpty()) {
	        	Session session = param.getMailSession();
	        	
	        	try {
	                // Create a default MimeMessage object.
	                Message message = new MimeMessage(session);

	                // Set From: header field of the header.
	                message.setFrom(new InternetAddress("praisindotesting@gmail.com", "noreply@praisindo.com"));
	                
	                // Create the message part
	                BodyPart messageBodyPart = new MimeBodyPart();
	                
	                // Create a multipar message
	                Multipart multipart = new MimeMultipart();
	                
	                if(execution.hasVariable("CPEmail")){
	                	String CPEmail = (String) execution.getVariable("CPEmail");
	            	    String CPName = (String) execution.getVariable("CPName");
	            	    String CPartyName = (String) execution.getVariable("CPartyName");
	            	    String filename = (String) execution.getVariable("sourceFile");
	            	    
		                // Set To: header field of the header.
		                message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(CPEmail));
	            	    
	                	// Set Subject: header field
		                message.setSubject("(OMS) Trading Information");
			                	
		                // Now set the actual message
		                messageBodyPart.setText("Dear "+CPName+","
		                		+"\nPlease note the following trading for "+ CPartyName +" on the attachment.\n\nThanks & Regards");			                
	
		                // Set text message part
		                multipart.addBodyPart(messageBodyPart);
	
		                // Part two is attachment
		                messageBodyPart = new MimeBodyPart();		                
		                DataSource source = new FileDataSource(filename);	                
		                messageBodyPart.setDataHandler(new DataHandler(source));
		                messageBodyPart.setFileName(source.getName());
		                multipart.addBodyPart(messageBodyPart);
	                }else{
	                	 message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
	                	 message.setSubject("(OMS) New Task Assigned: " + taskName);
	                	 messageBodyPart.setText("Dear "+assignee+","
			                		+"\nYou have assigned to a new task.\nPlease complete this task http://localhost:8080/camunda/app/tasklist/default/#/?task="+taskId+".\n\nThanks & Regards");
			             multipart.addBodyPart(messageBodyPart);
	                }

	                // Send the complete message parts
	                message.setContent(multipart);

	                // Send message
	                Transport.send(message);

	                LOGGER.info("Email successfully sent to user.");
	         
	             } catch (MessagingException e) {
	            	LOGGER.log(Level.WARNING, "Could not send the email", e);
	                throw new RuntimeException(e);
	             }	        	

	        } else {
	          LOGGER.warning("Not sending email to user " + assignee + "', user has no email address.");
	        }

	      } else {
	        LOGGER.warning("Not sending email to user " + assignee + "', user is not enrolled with identity service.");
	      }
		
	    }
	}

}
