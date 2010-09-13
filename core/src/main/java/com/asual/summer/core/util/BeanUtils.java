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

package com.asual.summer.core.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Component
public class BeanUtils implements BeanFactoryPostProcessor {
	
	@Autowired
	private static ConfigurableListableBeanFactory beanFactory;
	
	public static <T> T getBeanOfType(Class<T> clazz) {
		Map<String, T> beans = getBeansOfType(clazz);
		Iterator<Entry<String, T>> iterator = beans.entrySet().iterator();
		if (iterator.hasNext()) {
			return iterator.next().getValue();
		}
		return null;
	}
	
	public static <T> T getBeanOfType(ConfigurableListableBeanFactory beanFactory, Class<T> clazz) {
		Map<String, T> beans = beanFactory.getBeansOfType(clazz);
		Iterator<Entry<String, T>> iterator = beans.entrySet().iterator();
		if (iterator.hasNext()) {
			return iterator.next().getValue();
		}
		return null;
	}
	
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
    	return beanFactory.getBeansOfType(clazz);
    }
    
    public static <T> T getBeansOfType(ConfigurableListableBeanFactory beanFactory, Class<T> clazz) {
        Map<String, T> beans = beanFactory.getBeansOfType(clazz);
        Iterator<Entry<String, T>> iterator = beans.entrySet().iterator();
        if (iterator.hasNext()) {
            return iterator.next().getValue();
        }
        return null;
    }

    public static String[] getBeanNames(Class<?> clazz) {
    	return beanFactory.getBeanNamesForType(clazz);
    }
    
    public static Object getBean(String name) {
        return beanFactory.getBean(name);
    }

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		BeanUtils.beanFactory = beanFactory;
	}
}
