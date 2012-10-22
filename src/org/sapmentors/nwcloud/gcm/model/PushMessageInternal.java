package org.sapmentors.nwcloud.gcm.model;

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
public class PushMessageInternal extends PushMessage {
	private AndroidDevice deviceFrom; 
	private List<AndroidDevice> devicesTo;
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
	
}
