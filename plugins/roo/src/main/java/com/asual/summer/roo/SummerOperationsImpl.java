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

package com.asual.summer.roo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.WebMvcOperations;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.AbstractProjectOperations;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectType;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.WebXmlUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Component
@Service
public class SummerOperationsImpl extends AbstractProjectOperations implements SummerOperations {
	
	private Logger logger = Logger.getLogger(getClass().getName());
	
	@Reference 
	private FileManager fileManager;
	
	@Reference 
	private PathResolver pathResolver;
	
	@Reference 
	private MetadataDependencyRegistry dependencyRegistry;
	
	@Reference 
	private TypeLocationService typeLocationService;
	
	@Reference 
	private TypeManagementService typeManagementService;	
	
	public boolean isCreateProjectAvailable() {
		return !isProjectAvailable();
	}
	
	public String getProjectRoot() {
		return pathResolver.getRoot(Path.ROOT);
	}
	
	public void createProject(JavaPackage topLevelPackage, String projectName,
			Integer majorJavaVersion) {

		Assert.isTrue(isCreateProjectAvailable(), "Project creation is unavailable at this time");
		Assert.notNull(topLevelPackage, "Top level package required");

		if (majorJavaVersion == null || (majorJavaVersion < 6 || majorJavaVersion > 7)) {
			String ver = System.getProperty("java.version");
			if (ver.indexOf("1.7.") > -1) {
				majorJavaVersion = 7;
			} else {
				majorJavaVersion = 6;
			}
		}

		if (projectName == null) {
			String packageName = topLevelPackage.getFullyQualifiedPackageName();
			int lastIndex = packageName.lastIndexOf(".");
			if (lastIndex == -1) {
				projectName = packageName;
			} else {
				projectName = packageName.substring(lastIndex + 1);
			}
		}

		Document pom;
		try {
			pom = XmlUtils.getDocumentBuilder().parse(getClass().getResourceAsStream("/META-INF/pom-template.xml"));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		Element root = pom.getDocumentElement();
		
		XmlUtils.findRequiredElement("/project/artifactId", root).setTextContent(projectName);
		XmlUtils.findRequiredElement("/project/groupId", root).setTextContent(topLevelPackage.getFullyQualifiedPackageName());
		XmlUtils.findRequiredElement("/project/name", root).setTextContent(projectName);

		List<Element> versionElements = XmlUtils.findElements("//*[.='JAVA_VERSION']", root);
		for (Element versionElement : versionElements) {
			versionElement.setTextContent("1." + majorJavaVersion);
		}

		MutableFile pomMutableFile = fileManager.createFile(pathResolver.getIdentifier(Path.ROOT, "pom.xml"));
		XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);

		fileManager.scan();
		
		ProjectMetadata projectMetadata = (ProjectMetadata) metadataService.get(ProjectMetadata.getProjectIdentifier());
		Assert.notNull(projectMetadata, "Project metadata required");
		
		InputStream templateInputStream = getClass().getResourceAsStream("/META-INF/applicationContext-template.xml");
		Document context;
		try {
			context = XmlUtils.getDocumentBuilder().parse(templateInputStream);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		
		Element contextRoot = context.getDocumentElement();
		XmlUtils.findFirstElementByName("context:component-scan", contextRoot).setAttribute("base-package", 
				projectMetadata.getTopLevelPackage().getFullyQualifiedPackageName());

		PathResolver pathResolver = projectMetadata.getPathResolver();
		MutableFile mutableFile = fileManager.createFile(pathResolver.getIdentifier(Path.SPRING_CONFIG_ROOT, "applicationContext.xml"));
		XmlUtils.writeXml(mutableFile.getOutputStream(), context);

		fileManager.scan();
		
		try {
			FileCopyUtils.copy(getClass().getResourceAsStream("/META-INF/jetty-env-template.xml"), 
					fileManager.createFile(pathResolver.getIdentifier(Path.SRC_TEST_RESOURCES, "jetty-env.xml")).getOutputStream());
		} catch (IOException e) {
			logger.warning("Unable to install Jetty JNDI configuration.");
		}
	}

	@Override
	public void generateAll(JavaPackage javaPackage) {
		
		Set<ClassOrInterfaceTypeDetails> cids = typeLocationService.findClassesOrInterfaceDetailsWithAnnotation(new JavaType(RooEntity.class.getName()));
		
		for (ClassOrInterfaceTypeDetails cid : cids) {
			if (Modifier.isAbstract(cid.getModifier())) {
				continue;
			}
			
			JavaType javaType = cid.getName();
			Path path = PhysicalTypeIdentifier.getPath(cid.getDeclaredByMetadataId());
			EntityMetadata entityMetadata = (EntityMetadata) metadataService.get(EntityMetadata.createIdentifier(javaType, path));
			
			if (entityMetadata == null || (!entityMetadata.isValid())) {
				continue;
			}
			
			String downstreamWebScaffoldMetadataId = WebScaffoldMetadata.createIdentifier(javaType, path);
			if (dependencyRegistry.getDownstream(entityMetadata.getId()).contains(downstreamWebScaffoldMetadataId)) {
				continue;
			}
			
			JavaType controller = new JavaType(javaPackage.getFullyQualifiedPackageName() + "." + javaType.getSimpleTypeName() + "Controller");
			createAutomaticController(controller, javaType, new HashSet<String>(), entityMetadata.getPlural().toLowerCase());
		}
	}

	@Override
	public boolean isNewControllerAvailable() {
		return isProjectAvailable();
	}

	@Override
	public void createAutomaticController(JavaType controller, JavaType entity,
			Set<String> disallowedOperations, String path) {

		Assert.notNull(controller, "Controller Java Type required");
		Assert.notNull(entity, "Entity Java Type required");
		Assert.notNull(disallowedOperations, "Set of disallowed operations required");
		Assert.hasText(path, "Controller base path required");
		Assert.isTrue(isProjectAvailable(), "Project metadata required");
		Assert.isTrue(fileManager.exists(pathResolver.getIdentifier(Path.SPRING_CONFIG_ROOT, "applicationContext.xml")), "Application context does not exist");

		String resourceIdentifier = typeLocationService.getPhysicalLocationCanonicalPath(controller, Path.SRC_MAIN_JAVA);

		if (fileManager.exists(resourceIdentifier)) {
			return;
		}
			
		List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

		// Create annotation @RooWebScaffold(path = "/test", formBackingObject = MyObject.class)
		List<AnnotationAttributeValue<?>> rooWebScaffoldAttributes = new ArrayList<AnnotationAttributeValue<?>>();
		rooWebScaffoldAttributes.add(new StringAttributeValue(new JavaSymbolName("path"), path));
		rooWebScaffoldAttributes.add(new ClassAttributeValue(new JavaSymbolName("formBackingObject"), entity));
		for (String operation : disallowedOperations) {
			rooWebScaffoldAttributes.add(new BooleanAttributeValue(new JavaSymbolName(operation), false));
		}
		annotations.add(new AnnotationMetadataBuilder(new JavaType(RooWebScaffold.class.getName()), rooWebScaffoldAttributes));

		// Create annotation @RequestMapping("/myobject/**")
		List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
		requestMappingAttributes.add(new StringAttributeValue(new JavaSymbolName("value"), "/" + path));
		annotations.add(new AnnotationMetadataBuilder(new JavaType("org.springframework.web.bind.annotation.RequestMapping"), requestMappingAttributes));

		// Create annotation @Controller
		List<AnnotationAttributeValue<?>> controllerAttributes = new ArrayList<AnnotationAttributeValue<?>>();
		annotations.add(new AnnotationMetadataBuilder(new JavaType("org.springframework.stereotype.Controller"), controllerAttributes));

		String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(controller, getPathResolver().getPath(resourceIdentifier));
		ClassOrInterfaceTypeDetailsBuilder typeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(declaredByMetadataId, Modifier.PUBLIC, controller, PhysicalTypeCategory.CLASS);
		typeDetailsBuilder.setAnnotations(annotations);

		typeManagementService.generateClassFile(typeDetailsBuilder.build());
		
		try {
			FileCopyUtils.copy(getClass().getResourceAsStream("/META-INF/webmvc-config-template.xml"), 
					fileManager.createFile(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, "WEB-INF/spring/webmvc-config.xml")).getOutputStream());
		} catch (IOException e) {
			logger.warning("Unable to install Jetty JNDI configuration.");
		}
		
		PathResolver pathResolver = getPathResolver();
		
		if (fileManager.exists(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, "WEB-INF/web.xml"))) {
			return;
		}

		InputStream templateInputStream = getClass().getResourceAsStream("/META-INF/web-template.xml");
		Document webXml;
		try {
			webXml = XmlUtils.getDocumentBuilder().parse(templateInputStream);
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}

		String projectName = getProjectMetadata().getProjectName();
		WebXmlUtils.setDisplayName(projectName, webXml, null);
		WebXmlUtils.setDescription("Roo generated " + projectName + " application", webXml, null);

		WebXmlUtils.addContextParam(new WebXmlUtils.WebXmlParam("defaultHtmlEscape", "true"), webXml, "Enable escaping of form submission contents");
		if (fileManager.exists(pathResolver.getIdentifier(Path.SRC_MAIN_RESOURCES, "META-INF/persistence.xml"))) {
			WebXmlUtils.addFilter(WebMvcOperations.OPEN_ENTITYMANAGER_IN_VIEW_FILTER_NAME, "org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter", "/*", webXml, null);
		}
		WebXmlUtils.setSessionTimeout(new Integer(10), webXml, null);
		
		writeToDiskIfNecessary(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, "WEB-INF/web.xml"), webXml);
		try {
			templateInputStream.close();
		} catch (IOException ignore) {
		}
		fileManager.scan();
		
		updateProjectType(ProjectType.WAR);
	}
	
	private boolean writeToDiskIfNecessary(String fileName, Document proposed) {
		
		// Build a string representation of the JSP
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		XmlUtils.writeXml(XmlUtils.createIndentingTransformer(), byteArrayOutputStream, proposed);
		String xmlContent = byteArrayOutputStream.toString();
		try {
			byteArrayOutputStream.close();
		} catch (IOException ignore) {}

		// If mutableFile becomes non-null, it means we need to use it to write out the contents of jspContent to the file
		MutableFile mutableFile = null;
		if (fileManager.exists(fileName)) {
			// First verify if the file has even changed
			File f = new File(fileName);
			String existing = null;
			try {
				existing = FileCopyUtils.copyToString(new FileReader(f));
			} catch (IOException ignoreAndJustOverwriteIt) {
			}

			if (!xmlContent.equals(existing)) {
				mutableFile = fileManager.updateFile(fileName);
			}
		} else {
			mutableFile = fileManager.createFile(fileName);
			Assert.notNull(mutableFile, "Could not create XML file '" + fileName + "'");
		}

		if (mutableFile != null) {
			try {
				// We need to write the file out (it's a new file, or the existing file has different contents)
				FileCopyUtils.copy(xmlContent, new OutputStreamWriter(mutableFile.getOutputStream()));
				// Return and indicate we wrote out the file
				return true;
			} catch (IOException ioe) {
				throw new IllegalStateException("Could not output '" + mutableFile.getCanonicalPath() + "'", ioe);
			}
		}

		// A file existed, but it contained the same content, so we return false
		return false;
	}	
}