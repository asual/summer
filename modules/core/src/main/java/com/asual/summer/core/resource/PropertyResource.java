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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

/**
 * 
 * @author Rostislav Hristov
 * @author Rostislav Georgiev
 *
 */
public class PropertyResource extends AbstractResource implements BeanFactoryPostProcessor {

	private ExtendedPropertyPlaceholderConfigurer eppc;
	private final Log logger = LogFactory.getLog(getClass());
	private Resource[] resources;
	
	public PropertyResource() {
		setOrder(Ordered.HIGHEST_PRECEDENCE);
		eppc = ExtendedPropertyPlaceholderConfigurer.get(this);
	}

	public Object getProperty(String key) {
		return eppc.getProperty(key);
	}
	
	/*public void setProperties(Properties properties) {
		eppc.setProperties(properties);
	}*/

	/*
	public void setPropertiesArray(Properties[] propertiesArray) {
		eppc.setPropertiesArray(propertiesArray);
	}*/
	
	public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
		eppc.setIgnoreResourceNotFound(ignoreResourceNotFound);
	}
	
	public void setLocation(String location) {
		ResourceEditor editor = (ResourceEditor) BeanUtils.findEditorByConvention(Resource.class);
		this.resources = new Resource[] {locationStringToResource(location, editor)};
	}
	
	public void setLocations(String[] locations) {
		
		ResourceEditor editor = (ResourceEditor) BeanUtils.findEditorByConvention(Resource.class);
		Resource[] resources = new Resource[locations.length];
		for (int i = 0; i < locations.length; i++) {
			resources[i] = locationStringToResource(locations[i], editor);
		}
		this.resources = resources;
	}
	
	public void setLocations(List<String> locations){
		setLocations(locations.toArray(new String[locations.size()]));
	}
	
	private Resource locationStringToResource(String location, ResourceEditor editor){
		editor.setAsText(location);
		return (Resource) editor.getValue();
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
		
		this.resources = fileResources.toArray(new Resource[fileResources.size()]);
	}
	
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		eppc.postProcessBeanFactory(beanFactory);
	}
	
	protected void reloadPropertyPlaceholderConfigurer(){
		eppc.updateLocations();
	}

	/**
	 * @return the locations
	 */
	public Resource[] getResources() {
		return resources;
	}

	

}
