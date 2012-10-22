package org.sapmentors.nwcloud.gcm.model;

import java.util.Date;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name = "T_ANDROIDDEVICE")
@NamedQueries({
	@NamedQuery(name = "AllEntries", query = "select d from AndroidDevice d"),
	@NamedQuery(name = "EntryByEmail", query = "select d from AndroidDevice d WHERE d.email=:email")
})
public class AndroidDevice {
	public static final String QUERY_ALL_ENTRIES= "AllEntries";
	public static final String QUERY_BY_EMAIL= "EntryByEmail";
	public static final String QUERY_BY_EMAIL_PARAM= "email";
	
	@Id
	@Basic
	private String registrationKey;
	

	@Basic
	private String email;

	@Temporal(TemporalType.TIMESTAMP)
	@Basic
	private Date timeCreated;
	
	public AndroidDevice(){
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
}
