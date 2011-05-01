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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;

import com.asual.summer.core.util.ResourceUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesStreamHandler extends URLStreamHandler {
	
	private URL resource;
	private byte[] bytes;
	private long lastModified = -1;
	
	public FacesStreamHandler(URL resource) {
		this.resource = resource;
	}
	
	protected URLConnection openConnection(final URL u) throws IOException {
		
		final String encoding = (String) ResourceUtils.getProperty("app.encoding");
		
		return new URLConnection(u) {
			
			public void connect() throws IOException {
			}
			
			public InputStream getInputStream() throws IOException {
				if (bytes == null || (FacesContext.getCurrentInstance().isProjectStage(ProjectStage.Development) && lastModified < getLastModified())) {
					try {
						URLConnection urlc = resource.openConnection();
						InputStream in = urlc.getInputStream();
						try {
							bytes = FacesResourceProcessor.execute(u, in, encoding);
						} finally {
							in.close();
						}
					} catch (IOException e) {
						throw e;
					}
					lastModified = getLastModified();
				}
				
				return new ByteArrayInputStream(bytes);
			}
			
			public long getLastModified() {
				try {
					return resource.openConnection().getLastModified();
				} catch (IOException e) {
					return -1;
				}
			}
		};
	}
}
