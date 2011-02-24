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

package com.asual.summer.bundle;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.Descriptor;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.FragmentDescriptor;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.eclipse.jetty.xml.XmlParser.Node;

/**
 * @goal descriptor
 * @phase package
 * @requiresProject
 * @requiresDependencyResolution runtime
 * 
 * @author Rostislav Hristov
 * 
 */
public class BundleDescriptorMojo extends AbstractMojo {	

	/**
	 * @parameter default-value="${project.artifacts}"
	 * @readonly
	 */
	private Collection<Artifact> artifacts;

	/**
	 * @parameter expression="${basedir}"
	 * @readonly
	 */
	private File basedir;

	/**
	 * @parameter expression="${project.build.directory}"
	 * @readonly
	 */
	private File buildDirectory;

	/**
	 * @parameter expression="${project.build.finalName}"
	 * @readonly
	 */
	private String finalName;
	
	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException {

		try {
			
			File webXml = new File(buildDirectory, finalName + "/WEB-INF/web.xml");
			FileUtils.copyFile(new File(basedir, "src/main/webapp/WEB-INF/web.xml"), webXml);
			
			Configuration[] configurations = new Configuration[] { new WebXmlConfiguration(), new FragmentConfiguration() };
			WebAppContext context = new WebAppContext();
			context.setDefaultsDescriptor(null);
			context.setDescriptor(webXml.getAbsolutePath());
			context.setConfigurations(configurations);
			
			for (Artifact artifact : artifacts) {
				JarInputStream in = new JarInputStream(new FileInputStream(artifact.getFile()));
				while (true) {
					ZipEntry entry = in.getNextEntry();
					if (entry != null) {
						if ("META-INF/web-fragment.xml".equals(entry.getName())) {
							Resource fragment = Resource.newResource("file:" + artifact.getFile().getAbsolutePath());
							context.getMetaData().addFragment(fragment, 
									Resource.newResource("jar:" + fragment.getURL() + "!/META-INF/web-fragment.xml"));
							context.getMetaData().addWebInfJar(fragment);
						}
					} else {
						break;
					}
				}
				in.close();
			}
			
			for (int i = 0; i < configurations.length; i++) {
				configurations[i].preConfigure(context);
			}
			
			for (int i = 0; i < configurations.length; i++) {
				configurations[i].configure(context);
			}
			
			for (int i = 0; i < configurations.length; i++) {
				configurations[i].postConfigure(context);
			}
			
			Descriptor descriptor = context.getMetaData().getWebXml();
			Node root = descriptor.getRoot();
			
			List<Object> nodes = new ArrayList<Object>();
			List<FragmentDescriptor> fragmentDescriptors = context.getMetaData().getOrderedFragments();
			for (FragmentDescriptor fd : fragmentDescriptors) {
				for (int i = 0; i < fd.getRoot().size(); i++) {
					Object el = fd.getRoot().get(i);
					if (el instanceof Node && ((Node) el).getTag().matches("^name|ordering$")) {
						continue;
					}
					nodes.add(el);
				}
			}
			root.addAll(nodes);

			BufferedWriter writer = new BufferedWriter(new FileWriter(webXml));
			writer.write(root.toString());
			writer.close();
			
			File warFile = new File(buildDirectory, finalName + ".war");
			File bundleFile = new File(buildDirectory, "bundle.war");
			
			JarInputStream in = new JarInputStream(new FileInputStream(warFile));
			JarOutputStream out = new JarOutputStream(new FileOutputStream(bundleFile));
			
			while (true) {
				ZipEntry entry = in.getNextEntry();
				if (entry != null) {
					if ("WEB-INF/web.xml".equals(entry.getName())) {
						BufferedInputStream bis = new BufferedInputStream(new FileInputStream(webXml));
						putEntry(out, bis, new ZipEntry(entry.getName()));
						bis.close();
					} else {
						putEntry(out, in, entry);
					}
				} else {
					break;
				}
			}
			
			in.close();
			out.close();
			
			warFile.delete();
			FileUtils.moveFile(bundleFile, warFile);
			
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
	
	private void putEntry(JarOutputStream out, InputStream in, ZipEntry entry) throws IOException {
		out.putNextEntry(entry);
		IOUtil.copy(in, out);
		out.closeEntry();
	}
	
}
