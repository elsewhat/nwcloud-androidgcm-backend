package org.sapmentors.nwcloud.gcm.jersey;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.sapmentors.nwcloud.gcm.model.AndroidDevice;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {
    private final JAXBContext context;
    private final Set<Class> types;
    private Class[] ctypes = { AndroidDevice.class}; //your pojo class
   
    public JAXBContextResolver() throws Exception {
        this.types = new HashSet(Arrays.asList(ctypes));
        this.context = new JSONJAXBContext(JSONConfiguration.natural().build(),
                ctypes); //json configuration
    }

    @Override
    public JAXBContext getContext(Class<?> objectType) {
        return (types.contains(objectType)) ? context : null;
    }
}
