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

package com.asual.summer.core.faces;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.view.facelets.ResourceResolver;

public class FacesResourceResolver extends ResourceResolver {
    
	private Map<String, URL> resources = new HashMap<String, URL>();
	
	public URL resolveUrl(String path) {
    	try {
        	URL url = getClass().getClassLoader().getResource(path.replaceAll("^/", ""));
			if (!resources.containsKey(path) || (resources.containsKey(path) && 
					resources.get(path).openConnection().getLastModified() != url.openConnection().getLastModified()) ) {
		    	resources.put(path, new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), new FacesStreamHandler(url)));
			}
			return resources.get(path);
        } catch (IOException e) {
            throw new FacesException(e);
        }
    }
        
    private class FacesStreamHandler extends URLStreamHandler {
    	
    	private URL resource;
    	private InputStream inputStream;
    	private long lastModified;
    	
    	public FacesStreamHandler(URL resource) {
    		this.resource = resource;
    	}
    	
        protected URLConnection openConnection(URL u) throws IOException {
        	
            return new URLConnection(u) {
            	
                public void connect() throws IOException {
                }
                
                public InputStream getInputStream() throws IOException {
                	
                    if (inputStream == null || (inputStream != null && lastModified < getLastModified())) {
                		
	                    byte[] result;
	                    try {
	                        URLConnection urlc = resource.openConnection();
	                        StringBuffer sb = new StringBuffer(1024);
	                        InputStream input = urlc.getInputStream();
	                        UnicodeReader reader = new UnicodeReader(input, "UTF-8");
	                        try {
	                            char[] cbuf = new char[32];
	                            int r;
	                            while ((r = reader.read(cbuf, 0, 32)) != -1) {
	                                sb.append(cbuf, 0, r);
	                            }
	                            result = sb.toString().getBytes(reader.getEncoding());
	                        } finally {
	                            reader.close();
	                            input.close();
	                        }
	                    } catch (IOException e) {
	                        throw e;
	                    }
	                    
	                    inputStream = new ByteArrayInputStream(result);
	                    lastModified = getLastModified();
                	}
                	
                    return inputStream;
                }
                
                public long getLastModified() {
					try {
						return resource.openConnection().getLastModified();
					} catch (IOException e) {
						return 0;
					}
                }
                
            };
        }
    }

}