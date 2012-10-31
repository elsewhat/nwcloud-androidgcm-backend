package org.sapmentors.nwcloud.gcm.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.sapmentors.nwcloud.gcm.gcm.GoogleCloudMessagingClient;
import org.sapmentors.nwcloud.gcm.model.PersistenceClient;
import org.sapmentors.nwcloud.gcm.model.PushMessageExternal;
import org.sapmentors.nwcloud.gcm.model.PushMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/messaging")
public class MessagingEndpoint {
	private final static Logger logger = LoggerFactory.getLogger(AndroidDeviceEndpoint.class);
	
	//Ask jersey to populate this parameter for one of the REST methods
	@Context
	UriInfo uriInfo;
	
	PersistenceClient persistenceClient;
	
	/**
	 * Constructor must have no parameters (Jersey)
	 * It will initialize the datasource we are using (JPA)
	 */
	public MessagingEndpoint(){
		logger.debug("Constructor of MessagingEndpoint called");
		
		persistenceClient = new PersistenceClient();
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


}
