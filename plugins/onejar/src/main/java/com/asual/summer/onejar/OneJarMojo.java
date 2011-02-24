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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;

/**
 * @goal onejar
 * @phase package
 * @requiresProject
 * @requiresDependencyResolution runtime
 * 
 * @author Rostislav Hristov
 *
 */
public class OneJarMojo extends AbstractMojo {	
	
	/**
	 * @parameter default-value="${localRepository}"
	 * @readonly
	 */
	private ArtifactRepository localRepository;

	/**
	 * @parameter default-value="${project.pluginArtifacts}"
	 * @readonly
	 */
	private Collection<Artifact> pluginArtifacts;
	
	/**
	 * @parameter default-value="${project.remoteArtifactRepositories}"
	 * @readonly
	 */
	private List<ArtifactRepository> remoteArtifactRepositories;	

	/**
	 * @parameter expression="${project.build.directory}"
	 * @readonly
	 */
	private File buildDirectory;

	/**
	 * @parameter expression="${project.build.finalName}.war"
	 * @readonly
	 */
	private String warName;

	/**
	 * @parameter expression="${project.build.finalName}-onejar.jar"
	 * @readonly
	 */
	private String jarName;

	/**
	 * @parameter expression="${project}"
	 * @readonly
	 */
	private MavenProject project;
	
	/**
	 * @component
	 * @readonly
	 */
	private MavenProjectHelper projectHelper;
	
	/**
	 * @component
	 * @readonly
	 */
	private MavenProjectBuilder mavenProjectBuilder;

	/**
	 * @component
	 * @readonly
	 */
	private ArtifactFactory artifactFactory;   
	
	private static String JAR_CLASSPATH = "lib/";
	private static String JAR_MAIN_CLASS = "com.simontuffs.onejar.Boot";
	private static String ONE_JAR_DIST = "one-jar-boot-0.96.jar";
	private static String ONE_JAR_MAIN_CLASS = "com.asual.summer.onejar.OneJarServer";
	
	public void execute() throws MojoExecutionException {

		JarOutputStream out = null;
		JarInputStream in = null;

		try {
			
			File jarFile = new File(buildDirectory, jarName);
			File warFile = new File(buildDirectory, warName);
			
			Manifest manifest = new Manifest(new ByteArrayInputStream("Manifest-Version: 1.0\n".getBytes("UTF-8")));
			manifest.getMainAttributes().putValue("Main-Class", JAR_MAIN_CLASS);
			manifest.getMainAttributes().putValue("One-Jar-Main-Class", ONE_JAR_MAIN_CLASS);
			
			out = new JarOutputStream(new FileOutputStream(jarFile, false), manifest);
			in = new JarInputStream(getClass().getClassLoader().getResourceAsStream(ONE_JAR_DIST));

			putEntry(out, new FileInputStream(warFile), new ZipEntry(JAR_CLASSPATH + warFile.getName()));
			
			for (Artifact artifact : pluginArtifacts) {
				if (artifact.getArtifactId().equalsIgnoreCase("summer-onejar")) {
					artifact.updateVersion(artifact.getVersion(), localRepository);
					putEntry(out, new FileInputStream(artifact.getFile()), new ZipEntry(JAR_CLASSPATH + artifact.getFile().getName()));
					MavenProject project = mavenProjectBuilder.buildFromRepository(artifact, remoteArtifactRepositories, localRepository);
					@SuppressWarnings("unchecked")
					List<Dependency> dependencies = project.getDependencies();
					for (Dependency dependency : dependencies) {
						if (!"provided".equals(dependency.getScope())) {
							Artifact dependencyArtifact = artifactFactory.createArtifact(
									dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), 
									dependency.getScope(), dependency.getType());
							dependencyArtifact.updateVersion(dependencyArtifact.getVersion(), localRepository);
							putEntry(out, new FileInputStream(dependencyArtifact.getFile()), new ZipEntry(JAR_CLASSPATH + dependencyArtifact.getFile().getName()));
						}
					}
				}
			}
			
			while (true) {
				ZipEntry entry = in.getNextEntry();
				if (entry != null) {
					putEntry(out, in, entry);
				} else {
					break;
				}
			}
			
			projectHelper.attachArtifact(project, "jar", jarFile);
			
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
			throw new MojoExecutionException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}
	}

	private void putEntry(JarOutputStream out, InputStream in, ZipEntry entry) throws IOException {
		out.putNextEntry(entry);
		IOUtils.copy(in, out);
		out.closeEntry();
	}

}
