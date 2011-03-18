/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Collections;
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

/**
 * 
 * @author Rostislav Hristov
 * @author Rostislav Georgiev
 *
 */
public class PropertyResource extends LocationResource implements BeanFactoryPostProcessor {

	private ExtendedPropertyPlaceholderConfigurer eppc;
	private final Log logger = LogFactory.getLog(getClass());
	
	public PropertyResource() {
		setOrder(Ordered.HIGHEST_PRECEDENCE);
		eppc = new ExtendedPropertyPlaceholderConfigurer();
		eppc.setOrder(getOrder());
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
		
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		List<Resource[]> resourceLocations = new ArrayList<Resource[]>();
		
		List<Resource> fileResources = new ArrayList<Resource>();
		List<Resource> jarResources = new ArrayList<Resource>();
		
		for (String location : locations) {
			try {
				Resource[] wildcard = resolver.getResources(location);
				if (wildcard != null && wildcard.length > 0) {
					resourceLocations.add(wildcard);
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		int i = 0;
		boolean entries = true;
		
		while(entries) {
			entries = false;
			for (Resource[] location : resourceLocations) {
				if (location.length > i) {
					try {
						boolean isJar = ResourceUtils.isJarURL(location[i].getURL());
						if (isJar) {
							jarResources.add(location[i]);
						} else {
							fileResources.add(location[i]);
						}
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
					entries = true;
				}
			}
			i++;
		}
		
		fileResources.addAll(jarResources);
		Collections.reverse(fileResources);
		
		eppc.setLocations(fileResources.toArray(new Resource[fileResources.size()]));
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
			String value = super.resolvePlaceholder(key, properties, PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
			if (value != null) {
				String separator = stringArraySeparator;
				if (separator == null) {
					separator = (String) super.resolvePlaceholder("app.stringArraySeparator", properties, PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
				}
				if (separator != null && value.indexOf(separator) != -1) {
					String[] arr = value.split(separator);
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
