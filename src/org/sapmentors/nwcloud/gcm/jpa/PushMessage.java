package org.sapmentors.nwcloud.gcm.jpa;

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
public class PushMessage {
	private String emailFrom; 
	private String[] emailTo; 
	private int messageType; 
	private String message;
	
	public String getEmailFrom() {
		return emailFrom;
	}
	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}
	public String[] getEmailTo() {
		return emailTo;
	}
	public void setEmailTo(String[] emailTo) {
		this.emailTo = emailTo;
	}
	public int getMessageType() {
		return messageType;
	}
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}	
}
