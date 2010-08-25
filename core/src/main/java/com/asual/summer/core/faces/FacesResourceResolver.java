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

import javax.faces.view.facelets.ResourceResolver;

public class FacesResourceResolver extends ResourceResolver {
    
	private static String ENCODING = "UTF-8";
	
    private byte[] readTextUrl(URL source, String encoding) throws IOException {
        byte[] result;
        try {
            URLConnection urlc = source.openConnection();
            StringBuffer sb = new StringBuffer(1024);
            InputStream input = urlc.getInputStream();
            UnicodeReader reader = new UnicodeReader(input, encoding);
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
        return result;
    }
    
	public URL resolveUrl(String path) {
    	URL url = getClass().getClassLoader().getResource(path.replaceAll("^/", ""));
    	try {
			return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), new FacesStreamHandler(readTextUrl(url, ENCODING)));
		} catch (Exception e) {
			return url;
		}
    }

    
    class FacesStreamHandler extends URLStreamHandler {
    	
    	private byte[] src;
    	
    	public FacesStreamHandler(byte[] src) {
    		this.src = src;
    	}
    	
        protected URLConnection openConnection(URL u) throws IOException {
        	
            return new URLConnection(u) {
            	
                public void connect() throws IOException {
                
                }
                
                public InputStream getInputStream() throws IOException {
                	return new ByteArrayInputStream(src);
                }
                
            };
        }
    }
    
}