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

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class ResourceOrderProcessor implements BeanFactoryPostProcessor {

	private Map<Object, Integer> resources;
	
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		AbstractResource resource = null;
		for (Object key : resources.keySet()) {
			if (key instanceof AbstractResource) {
				resource = (AbstractResource) key;
			} else if (key instanceof String) {
				resource = beanFactory.getBean((String) key, AbstractResource.class);
			}
			resource.setOrder(resources.get(key));
		}
	}
	
	public void setResources(Map<Object, Integer> resources) {
		this.resources = resources;
	}

}