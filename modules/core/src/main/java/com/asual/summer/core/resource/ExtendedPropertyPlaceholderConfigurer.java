package com.asual.summer.core.resource;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.Resource;

import com.asual.summer.core.util.StringUtils;

public class ExtendedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	
	private static ExtendedPropertyPlaceholderConfigurer singleton = new ExtendedPropertyPlaceholderConfigurer();
	
	public static ExtendedPropertyPlaceholderConfigurer get(PropertyResource propertyResource){
		singleton.addPropertyResource(propertyResource);
		return singleton;
	}
	
	private ExtendedPropertyPlaceholderConfigurer(){}

	private Properties properties = new Properties();
	
	private List<PropertyResource> propertyResources = new LinkedList<PropertyResource>();
	
	private String stringArraySeparator;
	
	private final static String ARRAY_SEPARATOR_KEY = "app.stringArraySeparator";
	
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties properties) throws BeansException {
		this.properties = properties;
		super.processProperties(beanFactoryToProcess, properties);
	}
	
	private void addPropertyResource(PropertyResource propertyResource){
		propertyResources.add(propertyResource);
	}

	public Object getProperty(String key) {
		
		String value = super.resolvePlaceholder(key, properties, PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
		if (value != null) {
			if (stringArraySeparator == null) {
				stringArraySeparator = (String) super.resolvePlaceholder(ARRAY_SEPARATOR_KEY, properties, PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
			}
			if (stringArraySeparator != null && value.indexOf(stringArraySeparator) != -1 && !value.equals(stringArraySeparator)) {
				String[] arr = value.split(stringArraySeparator);
				for (int i = 0; i < arr.length; i++) {
					arr[i] = StringUtils.decorate(arr[i].trim(),System.getProperties());
				}
				return arr;
			} else {
				return StringUtils.decorate(value.trim(),System.getProperties());
			}
		}
		return null;
	}
	
	public void updateLocations(){
		Collections.sort(propertyResources, Collections.reverseOrder(OrderComparator.INSTANCE));
		List<Resource> locations = new LinkedList<Resource>();
		for(PropertyResource propertyResource : propertyResources){
			for(Resource location : propertyResource.getResources()){
				locations.add(location);
			}
		}
		
		setLocations(locations.toArray(new Resource[locations.size()]));
	}
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		
		try {
			updateLocations();
			
			Properties mergedProps = mergeProperties();

			// Convert the merged properties, if necessary.
			convertProperties(mergedProps);

			// Let the subclass process the properties.
			processProperties(beanFactory, mergedProps);
		}
		catch (IOException ex) {
			throw new BeanInitializationException("Could not load properties", ex);
		}
	}
	
}
