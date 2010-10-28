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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Manifest;

import javax.inject.Named;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.OrderComparator;

import com.asual.summer.core.resource.MessageResource;
import com.asual.summer.core.resource.PropertyResource;
import com.asual.summer.core.resource.ScriptResource;
import com.asual.summer.core.resource.StylesheetResource;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class ResourceUtils {
	
	private static Map<String, String> attributes = new HashMap<String, String>();
	
	private static List<MessageResource> messageResources = null;
	private static List<PropertyResource> propertyResources = null;
	private static List<StylesheetResource> stylesheetResources = null;
	private static List<ScriptResource> scriptResources = null;
	
	public static String getMessage(String key) {
    	return getMessage(key, new Object[] {}, LocaleContextHolder.getLocale());
    }
    
    public static String getMessage(String key, Locale locale) {
    	return getMessage(key, new Object[] {}, locale);
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
		
	    return null;
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
		
	    return null;
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
        	if (!attributes.containsKey(key)) {
            	String path = "META-INF/MANIFEST.MF";
                Manifest mf = new Manifest();
                URL resource = RequestUtils.getServletContext().getResource("/" + path);
                if (resource == null) {
                	resource = getClasspathResources(path, false).get(0);
                }
    			mf.read(new FileInputStream(resource.getFile()));
    			attributes.put(key, mf.getMainAttributes().getValue(key));
        	}
        	return attributes.get(key);
		} catch (Exception e) {
			return "";
		}
    }
    
    public static boolean exists(String name) {
    	return ResourceUtils.class.getClassLoader().getResource(name) != null;
    }
    
    public static List<URL> getClasspathResources(String name, boolean jarURL) {
    	List<URL> list = new ArrayList<URL>();
    	try {
	    	Enumeration<URL> resources = RequestUtils.class.getClassLoader().getResources(name);
	    	while(resources.hasMoreElements()) {
	    		URL resource = resources.nextElement();
	    		if (org.springframework.util.ResourceUtils.isJarURL(resource) == jarURL) {
	    			list.add(resource);
	    		}
	    	}
    	} catch (Exception e) {
    	}
    	return list;
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