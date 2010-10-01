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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import com.asual.summer.core.resource.MessageResource;
import com.asual.summer.core.resource.PropertyResource;
import com.asual.summer.core.resource.ScriptResource;
import com.asual.summer.core.resource.StylesheetResource;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Component
public class ResourceUtils {

	private static List<MessageResource> messageResources = null;
	private static List<PropertyResource> propertyResources = null;
	private static List<StylesheetResource> stylesheetResources = null;
	private static List<ScriptResource> scriptResources = null;
	
	public static String getMessage(String key) {
    	return ResourceUtils.getMessage(key, new Object[] {}, LocaleContextHolder.getLocale());
    }
    
    public static String getMessage(String key, Locale locale) {
    	return ResourceUtils.getMessage(key, new Object[] {}, locale);
    }

	public static String getMessage(String key, Object[] args) {
		return getMessage(key, args, LocaleContextHolder.getLocale());
	}
    
	public static String getMessage(String key, Object[] args, Locale locale) {
	
		if (messageResources == null) {
			messageResources = getResources(MessageResource.class);
		}
		
		for (MessageResource bean : messageResources) {
	        try {
	    		return bean.getMessage(key, args, locale);
	        } catch (NoSuchMessageException e) {
			}
		}    	
		
	    return "{" + key + "}";
	}

	public static Object getProperty(String key) {
		
		if (propertyResources == null) {
			propertyResources = getResources(PropertyResource.class);
		}
		
		Object property = null;

		for (PropertyResource bean : propertyResources) {
			property = bean.getProperty(key);
			if (property != null) {
				break;
			}
		}
		
		if (property != null) {
	        if (property instanceof String) {
	            return ObjectUtils.convert((String) property);
	        } else {
	            return property;
	        }
		}
		
	    return "{" + key + "}";
	}
	
	public static List<ScriptResource> getScripts() {
		if (scriptResources == null) {
			scriptResources = getResources(ScriptResource.class);
		}
		return scriptResources;
	}

	public static List<StylesheetResource> getStylesheets() {
		if (stylesheetResources == null) {
			stylesheetResources = getResources(StylesheetResource.class);
		}
		return stylesheetResources;
	}
    
    public static String getManifestAttribute(String key) {
        try {
        	ServletContext servletContext = RequestUtils.getRequest().getSession().getServletContext();
            Manifest mf = new Manifest();
			mf.read(new FileInputStream(servletContext.getResource("/META-INF/MANIFEST.MF").getFile()));
	        return mf.getMainAttributes().getValue(key);
		} catch (Exception e) {
			return "";
		}
    }
    
    public static boolean exists(String name) {
    	return ResourceUtils.class.getClassLoader().getResource(name) != null;
    }

	@SuppressWarnings("unchecked")
	static <T> List<T> getResources(Class<T> clazz) {
		
		String[] names = BeanUtils.getBeanNames(clazz);
		List<T> resources = new ArrayList<T>();
	
		for (String name : names) {
			resources.add((T) BeanUtils.getBean(name));
		}
		
		OrderComparator.sort(resources);
		return resources;
	}
	
}