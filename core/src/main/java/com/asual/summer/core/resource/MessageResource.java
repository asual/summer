package com.asual.summer.core.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

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

public class MessageResource extends AbstractResource {

	private ResourceBundleMessageSource rbms;
    private final Log logger = LogFactory.getLog(getClass());
	
	public MessageResource() {
		setOrder(Ordered.HIGHEST_PRECEDENCE);
		rbms = new ResourceBundleMessageSource();
	}

	public String getMessage(String code, Object args[], Locale locale) {
		return rbms.getMessage(code, args, locale);
	}
	
	public void setLocations(String[] locations) {
		setWildcardLocations(locations);
	}
    
	public void setWildcardLocations(String[] locations) {
		
		List<String> resources = new ArrayList<String>();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		for (String location : locations) {
			try {
				Resource[] wildcard = resolver.getResources(location + "*.properties");
				if (wildcard != null && wildcard.length > 0) {
					for (Resource resource : wildcard) {
						if ("jar".equals(resource.getURL().getProtocol()))  {
							resources.add(resource.getURL().getFile().split("\\!/")[1]);
						} else {
							resources.add(0, resource.getURL().getFile().split("/classes/")[1]);
						}
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		List<String> basenames = new ArrayList<String>();
		for (String resource : resources) {
			String basename = resource.replaceAll("/", ".").replaceAll("(_\\w\\w){0,3}\\.properties", "");
			if (!basenames.contains(basename)) {
				basenames.add(basename);
			}
		}
		
		rbms.setBasenames(basenames.toArray(new String[basenames.size()]));
	}
	
}