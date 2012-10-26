package org.sapmentors.nwcloud.gcm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the PushMessage in its internal state
 * Objects should be populated from the JPA persistency layer,
 * so that they can easily be updated.
 * 
 * May also store information about the status of the message
 * 
 * @author dagfinn.parnas
 *
 */
public class PushMessageInternal extends PushMessageExternal {
	protected AndroidDevice deviceFrom; 
	protected List<AndroidDevice> devicesTo;
	protected List<String> emailsFailed; 
	
	public List<String> getEmailsFailed() {
		return emailsFailed;
	}
	public void setEmailsFailed(List<String> emailsFailed) {
		this.emailsFailed = emailsFailed;
	}
	
	public void addEmailFailed(String email){
		if(emailsFailed==null){
			emailsFailed = new ArrayList<String>(10);
		}
		emailsFailed.add(email);
	}
	
	public AndroidDevice getDeviceFrom() {
		return deviceFrom;
	}
	public void setDeviceFrom(AndroidDevice deviceFrom) {
		this.deviceFrom = deviceFrom;
	}
	public List<AndroidDevice> getDevicesTo() {
		return devicesTo;
	}
	public void setDevicesTo(List<AndroidDevice> devicesTo) {
		this.devicesTo = devicesTo;
	} 
	
	public void addDeviceTo(AndroidDevice deviceTo){
		if(devicesTo==null){
			devicesTo = new ArrayList<AndroidDevice>(10);
		}
		devicesTo.add(deviceTo);
	}
	
	public static PushMessageInternal createFromPushMessage (PushMessageExternal pushMessageExt){
		PushMessageInternal pushMessage = new PushMessageInternal();
		pushMessage.setEmailFrom(pushMessageExt.getEmailFrom());
		pushMessage.setEmailTo(pushMessageExt.getEmailTo());
		pushMessage.setMessageType(pushMessageExt.getMessageType());
		pushMessage.setMessage(pushMessageExt.getMessage());
		
		return pushMessage;
	}
	
	public PushMessageResponse createPushMessageResponse(){
		PushMessageResponse pushMessageResponse = new PushMessageResponse();
		pushMessageResponse.setEmailFrom(emailFrom);
		pushMessageResponse.setMessageType(messageType);
		
		//TODO: Populate the list of failed and ok recipients
		
		return pushMessageResponse;
	}
	
}
