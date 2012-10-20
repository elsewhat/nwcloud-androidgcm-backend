package org.sapmentors.nwcloud.gcm.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * Required ref example 2.7
 * http://jersey.java.net/nonav/documentation/latest/jax-rs.html
 * 
 * @author dagfinn.parnas
 *
 */
public class JAXRSApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = new HashSet<Class<?>>();
		set.add(AndroidDeviceEndpoint.class);
		return set;
	}

	
}
