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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;

import com.asual.summer.core.faces.FacesResourceProcessor;

/**
 * @goal resources
 * @phase process-resources
 * @requiresProject
 * @requiresDependencyResolution runtime
 * 
 * @author Rostislav Hristov
 * 
 */
public class BundleResourcesMojo extends AbstractMojo {

	/**
	 * @parameter expression="${project.resources}"
	 * @readonly
	 */
	private List<Resource> resources;

	/**
	 * @parameter expression="${project.build.outputDirectory}"
	 * @readonly
	 */
	private File outputDirectory;

	/**
	 * @parameter default-value="UTF-8"
	 */
	private String encoding;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		try {
			
			for (Resource resource : resources) {
				
				File targetDirectory = outputDirectory;
				
				if (resource.getTargetPath() != null) {
					targetDirectory = new File(outputDirectory, resource.getTargetPath());
				}
				
				DirectoryScanner scanner = new DirectoryScanner();
				scanner.setBasedir(new File(resource.getDirectory()));
				scanner.setIncludes(new String[] { "**/*.html" });
				scanner.scan();
				
				for (String name : scanner.getIncludedFiles()) {
					
					File file = new File(targetDirectory + "/" + name);
					InputStream in = null;
					OutputStream out = null;
					byte[] bytes = null;
					
					try {
						in = new FileInputStream(file);
						bytes = FacesResourceProcessor.execute(file.toURI().toURL(), new FileInputStream(file), encoding);
					} finally {
						IOUtil.close(in);
					}
					
					if (bytes != null) {
						try {
							out = new FileOutputStream(file);
							out.write(bytes);
						} finally {
							IOUtil.close(out);
						}
					}
				}
			}
			
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
	
}
