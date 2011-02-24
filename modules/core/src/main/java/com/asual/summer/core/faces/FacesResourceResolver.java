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

package com.asual.summer.core.faces;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.view.facelets.ResourceResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asual.summer.core.util.ResourceUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesResourceResolver extends ResourceResolver {
	
	private final Log logger = LogFactory.getLog(getClass());
	private Map<String, URL> resources = new HashMap<String, URL>();
	
	public URL resolveUrl(String path) {
		if (!resources.containsKey(path)) {
			URL url = ResourceUtils.getClasspathResource("/".equals(path) ? "META-INF/" : path.replaceAll("^/", ""));
			if (url != null) {
				try {
					url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), new FacesStreamHandler(url));
				} catch (AccessControlException e) {
				} catch (NoClassDefFoundError e) {
				} catch (MalformedURLException e) {
					throw new FacesException(e);
				}
			} else {
				logger.warn("The requested resource [" + path + "] cannot be resolved.");
			}
			resources.put(path, url);
		}
		return resources.get(path);
	}
	
}