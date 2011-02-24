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

package com.asual.summer.onejar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class OneJarServer {
	
	public static void main(String[] args) throws Exception {
		Server server = new Server(Integer.valueOf(System.getProperty("server.port", "8080")));
		server.setHandler(new WebAppContext(getCurrentWarFile(), System.getProperty("server.contextPath", "/")));
		try {
			server.start();
		} catch (Exception e) {
			server.stop();
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static String getCurrentWarFile() throws IOException {
		JarFile jarFile = new JarFile(System.getProperty("java.class.path"));
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			String name = entries.nextElement().getName();
			if (name.endsWith(".war")) {
				File war = new File(new File(System.getProperty("java.io.tmpdir")), "summer-onejar-" + System.currentTimeMillis() + ".war");
				InputStream input = jarFile.getInputStream(new ZipEntry(name));
				FileOutputStream output = new FileOutputStream(war);
				IOUtils.copy(input, output);
				IOUtils.closeQuietly(input);
				IOUtils.closeQuietly(output);
				war.deleteOnExit();
				return war.getAbsolutePath();
			}
		}		
		return null;
	}
}