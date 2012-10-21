package org.sapmentors.nwcloud.gcm.rest;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.sapmentors.nwcloud.gcm.jpa.AndroidDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

@Path("/messaging")
public class MessagingEndpoint {
	private final static Logger logger = LoggerFactory.getLogger(AndroidDeviceEndpoint.class);
	
	//Ask jersey to populate this parameter for one of the REST methods
	@Context
	UriInfo uriInfo;
	
	//Used for reading/writing to JPA persistence
	private static EntityManagerFactory entityMangerFactory;
	
	/**
	 * Constructor must have no parameters (Jersey)
	 * It will initialize the datasource we are using (JPA)
	 */
	public MessagingEndpoint(){
		logger.debug("Constructor of MessagingEndpoint called");
		
		initPersistencyLayer();
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
	private static Result doSendMessageOverGCM(String message, Sender sender,
			AndroidDevice deviceInfo) throws IOException {
		// Trim message if needed.
		if (message.length() > 1000) {
			message = message.substring(0, 1000) + "[...]";
		}

		Message msg = new Message.Builder().addData("message", message).build();
		
		//send the message via gcm-server
		Result result = sender.send(msg, deviceInfo.getRegistrationKey(), 5);

		//Update android device information based on result
		if (result.getMessageId() != null) {
			String canonicalRegId = result.getCanonicalRegistrationId();
			if (canonicalRegId != null) {
				//Update Registration ID
				logger.info("Will update registration id based on getCanonicalRegistrationId. New value:"+canonicalRegId );
				
				EntityManager entityManager = entityMangerFactory.createEntityManager();  
	            entityManager.getTransaction().begin();  
	            
	            deviceInfo.setRegistrationKey(canonicalRegId);
	            deviceInfo.setTimeCreated(new Date());

                entityManager.getTransaction().commit();
			}
		} else {
			String error = result.getErrorCodeName();
			if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
				logger.info("Will remove device from database since it according to GCM is not registered. Device email:"+deviceInfo.getEmail() );
				//Remove entity
				EntityManager entityManager = entityMangerFactory.createEntityManager();  
	            entityManager.getTransaction().begin();  
	            
	            entityManager.remove(deviceInfo);
	            
	            entityManager.getTransaction().commit();
			}
		}

		return result;
	}

	/**
	 * Initialize the persistency layer (JPA)
	 * 
	 * @throws Exception
	 */
	private void initPersistencyLayer() {

		try {
			logger.debug("Setting up persistency layer for AndroidDeviceEndpoint");
			InitialContext ctx = new InitialContext();
			DataSource dataSource = (DataSource) ctx
					.lookup("java:comp/env/jdbc/DefaultDB");
			Map<String, DataSource> properties = new HashMap<String, DataSource>();
			properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE,
					dataSource);

			// IMPORTANT! The first parameter must match your JPA Model name in
			// persistence.xml
			entityMangerFactory = Persistence.createEntityManagerFactory(
					"nwcloud-androidgcm-backend", properties);
		} catch (NamingException e) {
			// TODO: Handle exception better
			logger.error("FATAL: Could not intialize database", e);
			throw new RuntimeException(e);
		}

	}

}
