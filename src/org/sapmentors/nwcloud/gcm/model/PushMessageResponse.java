package org.sapmentors.nwcloud.gcm.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the PushMessage in external state
 * This represents the JSON/XML payload that must be provided
 * to the REST API
 *  
 * @author dagfinn.parnas
 *
 */
@XmlRootElement
public class PushMessageResponse {
	protected String emailFrom; 
	protected String[] emailSentTo; 
	protected String[] emailFailed; 
	protected int messageType;
	
	public String getEmailFrom() {
		return emailFrom;
	}
	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}
	public String[] getEmailSentTo() {
		return emailSentTo;
	}
	public void setEmailSentTo(String[] emailSentTo) {
		this.emailSentTo = emailSentTo;
	}
	public String[] getEmailFailed() {
		return emailFailed;
	}
	public void setEmailFailed(String[] emailFailed) {
		this.emailFailed = emailFailed;
	}
	public int getMessageType() {
		return messageType;
	}
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	} 
	
	
}
