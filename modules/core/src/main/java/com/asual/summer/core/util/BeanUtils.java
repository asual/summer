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

package com.asual.summer.core.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.OrderComparator;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class BeanUtils implements BeanFactoryPostProcessor {
	
	@Inject
	private static ConfigurableListableBeanFactory beanFactory;
	
	public static <T> T getBeanOfType(Class<T> clazz) {
		return getBeanOfType(beanFactory, clazz);
	}
	
	public static <T> T getBeanOfType(ConfigurableListableBeanFactory beanFactory, Class<T> clazz) {
		Map<String, T> beans = beanFactory.getBeansOfType(clazz);
		ArrayList<T> values = new ArrayList<T>(beans.values());
		if (values.size() != 0) {
			OrderComparator.sort(values);
			return values.get(0);
		}
		return null;
	}
	
	public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
		return beanFactory.getBeansOfType(clazz);
	}

	public static String[] getBeanNames(Class<?> clazz) {
		return beanFactory.getBeanNamesForType(clazz);
	}
	
	public static Object getBean(String name) {
		return beanFactory.getBean(name);
	}
	
	public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		return beanFactory.getBeansWithAnnotation(annotationType);
	}
	
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		BeanUtils.beanFactory = beanFactory;
	}
	
}
