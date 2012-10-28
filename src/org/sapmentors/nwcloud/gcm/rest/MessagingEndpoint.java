package org.sapmentors.nwcloud.gcm.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.sapmentors.nwcloud.gcm.gcm.GoogleCloudMessagingClient;
import org.sapmentors.nwcloud.gcm.model.AndroidDevice;
import org.sapmentors.nwcloud.gcm.model.PushMessageExternal;
import org.sapmentors.nwcloud.gcm.model.PushMessageResponse;
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
     * POST a new object and store it in the persistency layer 
     * 
     * If the email already exist, the registration key will be updated.
     * (ie. doesn't currently support multiple android devices with the same email account)
     * 
     * Must be called with the HTTP POST method and accepts input in both JSON and XML format. 
     *  
     * Curl example: 
     * curl -i -X POST -H 'Content-Type: application/json'  -d '{"emailFrom":"dagfinn.parnas@gmail.com", "messageType":1, "message":"This is a push message", "emailTo":["dagfinn.parnas@gmail.com", "dagfinn.parnas@bouvet.no"]}' http://localhost:8080/nwcloud-androidgcm-backend/api/messaging 
     *  
     * @param feedEntry 
     * @return 
     */  
    @POST  
    @Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })  
    public PushMessageResponse sendGCMMessage(PushMessageExternal pushMessage) {  
          //The pushMessage is automatically populated based on the input. Yeah!  
          logger.info("Received message from " + pushMessage.getEmailFrom());  
          
          GoogleCloudMessagingClient gcmClient = new GoogleCloudMessagingClient(pushMessage);
          PushMessageResponse response = gcmClient.doSendMessage();
          
          return response;          
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
