/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.core.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

public class PropertyResource extends AbstractResource implements BeanFactoryPostProcessor {

    private ExtendedPropertyPlaceholderConfigurer eppc;
    private final Log logger = LogFactory.getLog(getClass());
    
	public PropertyResource() {
		setOrder(Ordered.HIGHEST_PRECEDENCE);
		eppc = new ExtendedPropertyPlaceholderConfigurer();
		eppc.setOrder(getOrder());
		eppc.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
	}

    public Object getProperty(String key) {
    	return eppc.getProperty(key);
    }
    
    public String getStringArraySeparator() {
        return eppc.getStringArraySeparator();
    }

    public void setStringArraySeparator(String stringArraySeparator) {
    	eppc.setStringArraySeparator(stringArraySeparator);
    }
	
	public void setLocations(String[] locations) {
		setWildcardLocations(locations);
	}
    
	public void setWildcardLocations(String[] locations) {
		
		List<Resource> resources = new ArrayList<Resource>();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		for (String location : locations) {
			try {
				Resource[] wildcard = resolver.getResources(location);
				if (wildcard != null && wildcard.length > 0) {
					for (Resource resource : wildcard) {
						if (ResourceUtils.isJarURL(resource.getURL()))  {
							resources.add(0, resource);
						} else {
							resources.add(resource);
						}
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		eppc.setLocations(resources.toArray(new Resource[resources.size()]));
	}

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		eppc.postProcessBeanFactory(beanFactory);
	}

	private class ExtendedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

		private Properties properties;
	    private String stringArraySeparator;
	    
		protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties properties) throws BeansException {
	        this.properties = properties;
	        super.processProperties(beanFactoryToProcess, properties);
	    }	

	    public Object getProperty(String key) {
	        String value = properties.getProperty(key);
	        if (value != null) {
	            if (stringArraySeparator != null && value.indexOf(stringArraySeparator) != -1) {
	                String[] arr = value.split(stringArraySeparator);
	                for (int i = 0; i < arr.length; i++) {
	                    arr[i] = arr[i].trim();
	                }
	                return arr;
	            } else {
	                return value.trim();
	            }
	        }
	        return null;
	    }
	    
	    public String getStringArraySeparator() {
	        return stringArraySeparator;
	    }

	    public void setStringArraySeparator(String stringArraySeparator) {
	        this.stringArraySeparator = stringArraySeparator;
	    }
	    
	}

}
