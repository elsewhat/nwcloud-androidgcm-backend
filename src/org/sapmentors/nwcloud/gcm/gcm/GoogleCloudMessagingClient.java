package org.sapmentors.nwcloud.gcm.gcm;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.sapmentors.nwcloud.gcm.model.MobileDevice;
import org.sapmentors.nwcloud.gcm.model.PersistenceClient;
import org.sapmentors.nwcloud.gcm.model.PushMessageExternal;
import org.sapmentors.nwcloud.gcm.model.PushMessageInternal;
import org.sapmentors.nwcloud.gcm.model.PushMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

public class GoogleCloudMessagingClient {
	private final static Logger logger = LoggerFactory.getLogger(GoogleCloudMessagingClient.class);
	//The server's key from the Google GCM API project
	private static final String GCM_API_KEY = "AIzaSyBWxYKSdl1NEHHDFxqE64ZUA8ibJNIwKww";
	
	private PushMessageInternal pushMessage; 
	private PersistenceClient persistenceClient;
	
	
	public GoogleCloudMessagingClient(PushMessageExternal pushMessageExt) {
		super();
		persistenceClient = new PersistenceClient();
		this.pushMessage = expandPushMessage(pushMessageExt);
	}
	
	
	/**
	 * Expand the push message received via REST interface.
	 * Does this by using the persisted information about the devices
	 * 
	 */
	public PushMessageInternal expandPushMessage (PushMessageExternal pushMessageExt) throws NoResultException{
		EntityManager entityManager = persistenceClient.getEntityManager();
		
		pushMessage= PushMessageInternal.createFromPushMessage(pushMessageExt);
		
		
		//expand sender
		try {
			 MobileDevice deviceFrom = entityManager.createNamedQuery(MobileDevice.QUERY_BY_EMAIL,  
		        		MobileDevice.class).setParameter(MobileDevice.QUERY_BY_EMAIL_PARAM, pushMessage.getEmailFrom()).getSingleResult();
			 pushMessage.setDeviceFrom(deviceFrom);
		}catch(NoResultException exception){
			throw new NoResultException("Sender's android device has not been registered for GCM sending in nwcloud backend");
		}
		
		//expand recipients
		String emailTo[] = pushMessageExt.getEmailTo();
		if(emailTo==null || emailTo.length==0){
			throw new NoResultException("No recipients defined (emailTo)");
		}
		
		for (int i = 0; i < emailTo.length; i++) {
			String email = emailTo[i];
			
			
			try {
				 MobileDevice deviceTo = entityManager.createNamedQuery(MobileDevice.QUERY_BY_EMAIL,  
			        		MobileDevice.class).setParameter(MobileDevice.QUERY_BY_EMAIL_PARAM, email).getSingleResult();
				 pushMessage.addDeviceTo(deviceTo);
				 logger.debug(email + " found in database");
			}catch(NoResultException exception){
				logger.info(email + " not found in database. Will not be in recipient list for this message");
				//this sender not found
				pushMessage.addEmailFailed(email);		
			}
		}
		
		return pushMessage;	
	}

	/**
	 * Sends the message using the Sender object to the registered device.
	 * 
	 * @param message
	 *            the message to be sent in the GCM ping to the device.
	 * @param sender
	 *            the Sender object to be used for ping,
	 * @param deviceInfo
	 *            the registration id of the device.
	 * @return Result the result of the ping.
	 */
	public PushMessageResponse doSendMessage()  {
		Sender sender = new Sender(GCM_API_KEY);
		
		String message = pushMessage.getMessage();
		// Trim message if needed.
		if (message.length() > 1000) {
			message = message.substring(0, 1000) + "[...]";
		}

		Message msg = new Message.Builder().addData("message", message).build();
		
		List<MobileDevice> listRecipients = pushMessage.getDevicesTo();
		
		//logger.debug("Sending message to " +listRecipients.size() + " recipients");
		for (Iterator<MobileDevice> iterator = listRecipients.iterator(); iterator.hasNext();) {
			MobileDevice androidDevice = (MobileDevice) iterator.next();
			//Send the message
			try {
				logger.debug("Sending message to " + androidDevice.getEmail());
				Result result = sender.send(msg, androidDevice.getRegistrationKey(), 5);
				//update device based on response from GCM
				handleMessageResponse(androidDevice, result);
			}catch (IOException e){
				logger.warn("Failed to send message to " + androidDevice.getEmail(),e);
				pushMessage.addEmailFailed(androidDevice.getEmail());
			}	
		}
		
		//response which can be returned to client through REST
		return pushMessage.createPushMessageResponse();
	}


	/**
	 * Response from GCM may cause stored values for devices to be updated or deleted
	 * 
	 * @param androidDevice
	 * @param result Returned from sender.send
	 */
	private void handleMessageResponse(MobileDevice androidDevice,
			Result result) {
		EntityManager entityManager = persistenceClient.getEntityManager();
		
		if (result.getMessageId() != null) {
			String canonicalRegId = result.getCanonicalRegistrationId();
			if (canonicalRegId != null) {
				//Update Registration ID
				logger.info("Will update registration id based on getCanonicalRegistrationId. New value:"+canonicalRegId );
				  
	            entityManager.getTransaction().begin();  
	            
	            androidDevice.setRegistrationKey(canonicalRegId);
	            androidDevice.setTimeCreated(new Date());

                entityManager.getTransaction().commit();
			}
		} else {
			String error = result.getErrorCodeName();
			if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
				logger.info("Will remove device from database since it according to GCM is not registered. Device email:"+androidDevice.getEmail() );
				//Remove entity
	            entityManager.getTransaction().begin();  
	            
	            entityManager.remove(androidDevice);
	            
	            entityManager.getTransaction().commit();
			}
		}
		
	}
	
	
	
}
