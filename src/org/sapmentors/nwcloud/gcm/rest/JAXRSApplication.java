package org.sapmentors.nwcloud.gcm.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

/**
 * Required in order to bind enpoints to JAX-RS (Jersey)
 * Ref xample 2.7
 * http://jersey.java.net/nonav/documentation/latest/jax-rs.html
 * 
 * @author dagfinn.parnas
 *
 */
public class JAXRSApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = new HashSet<Class<?>>();
		//Add all endpoints to this set
		set.add(AndroidDeviceEndpoint.class);
		//Add Providers
		set.add(JAXBContextResolver.class);
		return set;
	}	
}
