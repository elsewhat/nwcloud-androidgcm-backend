package org.sapmentors.nwcloud.gcm.model;

import java.util.Date;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * JPA entity representing a mobile device
 * 
 * 
 * @author dagfinn.parnas
 *
 */
@XmlRootElement
@Entity
@Table(name = "T_MOBILEDEVICE")
@NamedQueries({
	@NamedQuery(name = "AllEntries", query = "select d from MobileDevice d"),
	@NamedQuery(name = "EntryByEmail", query = "select d from MobileDevice d WHERE d.email=:email")
})
public class MobileDevice {
	public static final String QUERY_ALL_ENTRIES= "AllEntries";
	public static final String QUERY_BY_EMAIL= "EntryByEmail";
	public static final String QUERY_BY_EMAIL_PARAM= "email";
	
	public static final String ANDROID_MOBILE_PLATFORM="Android";
	public static final String IOS_MOBILE_PLATFORM="iOS";
	
	@Id
	@Basic
	private String registrationKey;
	
	@Basic
	private String email;

	@Temporal(TemporalType.TIMESTAMP)
	@Basic
	private Date timeCreated;
	
	@Basic 
	private String mobilePlatform;
	
	public MobileDevice(){
	}

	
	public String getRegistrationKey() {
		return registrationKey;
	}

	public void setRegistrationKey(String registrationKey) {
		this.registrationKey = registrationKey;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}
	
	
	public String getMobilePlatform() {
		return mobilePlatform;
	}

	public void setMobilePlatform(String mobilePlatform) {
		this.mobilePlatform = mobilePlatform;
	}
	
	/**
	 * Is this an android device
	 * 
	 * @return
	 */
	public boolean isAndroidDevice(){
		if(ANDROID_MOBILE_PLATFORM.equalsIgnoreCase(mobilePlatform)){
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * Is this an iOS/Apple device
	 * (note Apple Push Notification service not yet implemented)
	 * @return
	 */
	public boolean isIOS(){
		if(IOS_MOBILE_PLATFORM.equalsIgnoreCase(mobilePlatform)){
			return true;
		}else {
			return false;
		}
	}
}
