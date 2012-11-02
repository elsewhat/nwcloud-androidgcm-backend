package org.sapmentors.nwcloud.gcm.model;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.sapmentors.nwcloud.gcm.rest.MobileDeviceEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceClient {
	final Logger logger = LoggerFactory.getLogger(MobileDeviceEndpoint.class);
	
	//Used for reading/writing to JPA persistence
	private static EntityManagerFactory entityMangerFactory;
	
	public PersistenceClient (){
		initPersistencyLayer();
	}
	
	public EntityManager getEntityManager(){
		return entityMangerFactory.createEntityManager();
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
