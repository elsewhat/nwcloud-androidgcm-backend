package org.sapmentors.nwcloud.gcm.rest;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
            	  
            	  //We must commit the first transaction here
	        	  try { 
	                  entityManager.getTransaction().commit(); 
	              }catch(PersistenceException e){//most likely primary key violation
	            	  logger.warn("Unable to remove existing devices. Will continue, but may fail",e);
	              }
	        	  
	        	  //create new transaction
	        	  entityManager = persistenceClient.getEntityManager();  
	              entityManager.getTransaction().begin();       	  
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
    
              
              //return the url to the resource created
              try {  
            	  String email = URLEncoder.encode(androidDevice.getEmail(),"UTF-8");
            	  URI createdURI = new URI(uriInfo.getAbsolutePath()+""+ email);  
            	  return Response.created(createdURI).build();  
              } catch (URISyntaxException e) {  
            	  logger.warn("Unable to create correct URI for newly created feed " + androidDevice, e);  
            	  //fallback is to include the input path (which will be lacking the id of the new object)  
            	  return Response.created(uriInfo.getAbsolutePath()).build();  
              } catch (UnsupportedEncodingException e) {
            	  logger.warn("Unable to create correct URI for newly created feed " + androidDevice, e);  
            	  //fallback is to include the input path (which will be lacking the id of the new object)  
            	  return Response.created(uriInfo.getAbsolutePath()).build();  
			}   
    } 
    
    
    /** 
     * POST a new object and store it in the persistency layer 
     * 
     * If the email already exist, the registration key will be updated.
     * (ie. doesn't currently support multiple android devices with the same email account)
     * 
     * Must be called with the HTTP POST method and accepts input in both JSON and XML format. 
     *  
     * Curl example (deletes for dagfinn.parnas@gmail.com):  
     * curl  -i -X DELETE -H "Accept: application/json" http://localhost:8080/nwcloud-androidgcm-backend/api/androiddevice/dagfinn.parnas%40gmail.com
     * @param feedEntry 
     * @return 
     */  
    @Path("/{Email}")
    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response  deleteDevice(@PathParam("Email") String email) {
        logger.info("Attempting to delete device based on email {0}", email);
        //Start persistence transaction
        EntityManager entityManager = persistenceClient.getEntityManager();  
        entityManager.getTransaction().begin();  
        
        boolean doDelete=false;
        //If we already have a device registered for the email, delete it
        List<AndroidDevice> resultList = entityManager.createNamedQuery(AndroidDevice.QUERY_BY_EMAIL,  
        		AndroidDevice.class).setParameter(AndroidDevice.QUERY_BY_EMAIL_PARAM, email).getResultList();  
        
        if(resultList!=null && resultList.size()>0){
      	  logger.info("Found existing devices registered for " + email + " Deleting");
      	  for (Iterator<AndroidDevice> iterator = resultList.iterator(); iterator.hasNext();) {
				AndroidDevice existingAndroidDevice = (AndroidDevice) iterator.next();
				entityManager.remove(existingAndroidDevice);
				doDelete=true;
			}
        }
        
        entityManager.getTransaction().commit();
        if(doDelete){
        	//returning HTTP 200 OK
        	return Response.ok("Devices for "+ email + " deleted").build();
        }else {
        	logger.info("Found no devices registered for " + email);
        	//returning HTTP 204 No Content
        	return Response.noContent().entity("Found no devices registered for " + email).build();
        }
        	
   }
        
	
}
