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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
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
public class MessageResource extends AbstractResource {

	private ReloadableResourceBundleMessageSource rbms;
    private final Log logger = LogFactory.getLog(getClass());
	
	public MessageResource() {
		setOrder(Ordered.HIGHEST_PRECEDENCE);
		rbms = new ReloadableResourceBundleMessageSource();
	}

	public String getMessage(String code, Object args[], Locale locale) {
		return rbms.getMessage(code, args, locale);
	}
	
	public void setLocations(String[] locations) {
		setWildcardLocations(locations);
	}
    
	public void setWildcardLocations(String[] locations) {
		
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		List<String> basenames = new ArrayList<String>();
		for (String location : locations) {
			try {
				Resource[] wildcard = (Resource[]) ArrayUtils.addAll(
						resolver.getResources(location + "*.properties"), 
						resolver.getResources(location + "*.xml"));
				if (wildcard != null && wildcard.length > 0) {
					for (Resource resource : wildcard) {
						URL url = resource.getURL();
						boolean isJar = ResourceUtils.isJarURL(url);
						String basename = "classpath:" + url.getFile()
							.split(isJar ? ResourceUtils.JAR_URL_SEPARATOR : "/classes/")[1]
							.replaceAll("(_\\w+){0,3}\\.(properties|xml)", "");
						if (!basenames.contains(basename)) {
							if (isJar) {
								basenames.add(basename);
							} else {
								basenames.add(0, basename);
							}
						}
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		rbms.clearCache();
		rbms.setBasenames(basenames.toArray(new String[basenames.size()]));
	}
	
}