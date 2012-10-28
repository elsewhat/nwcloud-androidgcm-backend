package org.sapmentors.nwcloud.gcm.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.sapmentors.nwcloud.gcm.model.AndroidDevice;
import org.sapmentors.nwcloud.gcm.model.PersistenceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST endpoint definition using JAX-RS (Jersey)
 * 
 * This endpoint is used to register the Android device that can receive push messages
 * from this backend. 
 * 
 * It is typically called from the Android app after it has registering with 
 * Google Cloud Messaging and received a registration key.
 * 
 * @author dagfinn.parnas
 *
 */
@Path("/androiddevice")
public class AndroidDeviceEndpoint {
	final Logger logger = LoggerFactory.getLogger(AndroidDeviceEndpoint.class);
	
	//Ask jersey to populate this parameter for one of the REST methods
	@Context
	UriInfo uriInfo;

	private PersistenceClient persistenceClient;
	

	
	/**
	 * Constructor must have no parameters (Jersey)
	 * It will initialize the datasource we are using (JPA)
	 */
	public AndroidDeviceEndpoint(){
		logger.debug("Constructor of DeviceInfoEndpoint called");
		
		persistenceClient = new PersistenceClient();
	}
	
	/**
	 * Get all android devices that have registered to Google Cloud Messaging for our app
	 * Should not normally be exposed to external clients
	 * (we use it to populate a list of people you can send a GCM push message to) 
	 * 
	 * Private data (ie. registration key) is removed from the objects
	 * 
	 * Curl example (return all feeds in json format):
	 * $ curl  -i -H "Accept: application/json" http://localhost:8080/nwcloud-androidgcm-backend/api/androiddevice/
	 */
	@GET
	@Produces( { MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
	public List<AndroidDevice> getAndroidDevices() {
		logger.info("getAndroidDevices method called");
		
		
		EntityManager entityManager = persistenceClient.getEntityManager();  
        List<AndroidDevice> resultList = entityManager.createNamedQuery(AndroidDevice.QUERY_ALL_ENTRIES,  
        		AndroidDevice.class).getResultList();  
        
        if(resultList!=null){
	        //remove sensitive data for response
	        for (Iterator<AndroidDevice> iterator = resultList.iterator(); iterator.hasNext();) {
				AndroidDevice androidDevice = (AndroidDevice) iterator.next();
				androidDevice.setRegistrationKey(null);		
			}
        }
		
        String message = (resultList==null)? "getAndroidDevices returning null": "getAndroidDevices returning " + resultList.size() + " entries";  
        logger.info(message);  
        
        //avoid returning content null. Instead it should return {}
        if(resultList==null){
        	resultList=new ArrayList<AndroidDevice>();
        }
        
        return resultList;
	}
	
	
    /** 
     * POST a new object and store it in the persistency layer 
     * 
     * If the email already exist, the registration key will be updated.
     * (ie. doesn't currently support multiple android devices with the same email account)
     * 
     * Must be called with the HTTP POST method and accepts input in both JSON and XML format. 
     *  
     * Curl example (creates new feed): 
     * $ curl -i -X POST -H 'Content-Type: application/json'  -d '{"registrationKey":"thisisnotagregistrationkey","email":"dagfinn.parnas@bouvet.no"}' http://localhost:8080/nwcloud-androidgcm-backend/api/androiddevice/ 
     *  
     * @param feedEntry 
     * @return 
     */  
    @POST  
    @Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })  
    public Response addDevice(AndroidDevice androidDevice) {  
              //The feedEntry is automatically populated based on the input. Yeah!  
              logger.info("Storing android device for "+androidDevice.getEmail());  

              //Start persistence transaction
              EntityManager entityManager = persistenceClient.getEntityManager();  
              entityManager.getTransaction().begin();  
              
              
              //If we already have a device registered for the email, delete it
              List<AndroidDevice> resultList = entityManager.createNamedQuery(AndroidDevice.QUERY_BY_EMAIL,  
              		AndroidDevice.class).setParameter(AndroidDevice.QUERY_BY_EMAIL_PARAM, androidDevice.getEmail()).getResultList();  
              if(resultList!=null && resultList.size()>0){
            	  logger.info("Found existing devices registered for " + androidDevice.getEmail() + " Deleting them before adding new");
            	  for (Iterator<AndroidDevice> iterator = resultList.iterator(); iterator.hasNext();) {
					AndroidDevice existingAndroidDevice = (AndroidDevice) iterator.next();
					entityManager.remove(existingAndroidDevice);
				}
              }
              
                
              //add timestamp
              androidDevice.setTimeCreated(new Date());
              //persist the entry    
              try { 
            	  entityManager.persist(androidDevice);
            	//Commit transaction
                  entityManager.getTransaction().commit(); 
              }catch(PersistenceException e){//most likely primary key violation
            	  logger.warn("Primary key of entity already exist",e);
            	  return Response.status(400).entity("Registrationkey already exist (or other DB error)").build();
              }
    
              
               


              //The HTTP response should include the URL to the newly generated new entry.  
              //Probably exist a better way of doing this, but it works  
              //TODO: Do we need to return the URI ? Is it useful for the client?
              try {  
                        URI createdURI = new URI(uriInfo.getAbsolutePath()+""+ androidDevice.getRegistrationKey());  
                        return Response.created(createdURI).build();  
              } catch (URISyntaxException e) {  
                        logger.warn("Unable to create correct URI for newly created feed " + androidDevice, e);  
                        //fallback is to include the input path (which will be lacking the id of the new object)  
                        return Response.created(uriInfo.getAbsolutePath()).build();  
              }   
    } 
    
    //TODO: Implement a delete method
    
	
}
