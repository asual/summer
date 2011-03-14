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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Component
@Service
public class SummerCommands implements CommandMarker {
	
	private Logger logger = Logger.getLogger(getClass().getName());
	
	@Reference 
	private SummerOperations operations;
	
	@Reference 
	private MetadataService metadataService;

	@CliAvailabilityIndicator("summer project")
	public boolean isCreateProjectAvailable() {
		return operations.isCreateProjectAvailable();
	}

	@CliCommand(value = "summer project", help = "Creates a new Maven project")
	public void createProject(
		@CliOption(key = { "", "topLevelPackage" }, mandatory = true, optionContext = "update", help = "The uppermost package name (this becomes the <groupId> in Maven and also the '~' value when using Roo's shell)") JavaPackage topLevelPackage, 
		@CliOption(key = "projectName", mandatory = false, help = "The name of the project (last segment of package name used as default)") String projectName, 
		@CliOption(key = "java", mandatory = false, help = "Forces a particular major version of Java to be used (will be auto-detected if unspecified; specify 5 or 6 or 7 only)") Integer majorJavaVersion) {
		operations.createProject(topLevelPackage, projectName, majorJavaVersion);
	}	
	
	@CliAvailabilityIndicator({ "summer controller scaffold", "summer controller all" }) 
	public boolean isNewControllerAvailable() {
		return operations.isNewControllerAvailable();
	}

	@CliCommand(value = "summer controller all", help = "Scaffold a controller for all entities without an existing controller") 
	public void generateAll(
		@CliOption(key = "package", mandatory = true, optionContext = "update", help = "The package in which new controllers will be placed") JavaPackage javaPackage) {
		
		ProjectMetadata projectMetadata = (ProjectMetadata) metadataService.get(ProjectMetadata.getProjectIdentifier());
		Assert.notNull(projectMetadata, "Could not obtain ProjectMetadata");
		if (!javaPackage.getFullyQualifiedPackageName().startsWith(projectMetadata.getTopLevelPackage().getFullyQualifiedPackageName())) {
			logger.warning("Your controller was created outside of the project's top level package and is therefore not included in the preconfigured component scanning. Please adjust your component scanning manually in webmvc-config.xml");
		}
		operations.generateAll(javaPackage);
	}

	@CliCommand(value = "summer controller scaffold", help = "Create a new scaffold Controller (ie where we maintain CRUD automatically)") 
	public void newController(
		@CliOption(key = { "class", "" }, mandatory = true, help = "The path and name of the controller object to be created") JavaType controller, 
		@CliOption(key = "entity", mandatory = false, optionContext = "update,project", unspecifiedDefaultValue = "*", help = "The name of the entity object which the controller exposes to the web tier") JavaType entity, 
		@CliOption(key = "path", mandatory = false, help = "The base path under which the controller listens for RESTful requests (defaults to the simple name of the form backing object)") String path, 
		@CliOption(key = "disallowedOperations", mandatory = false, help = "A comma separated list of operations (only create, update, delete allowed) that should not be generated in the controller") String disallowedOperations) {

		EntityMetadata entityMetadata = (EntityMetadata) metadataService.get(EntityMetadata.createIdentifier(entity, Path.SRC_MAIN_JAVA));
		if (entityMetadata == null) {
			logger.warning("The specified entity can not be resolved to an Entity type in your project");
			return;
		}
		
		if (controller.getSimpleTypeName().equalsIgnoreCase(entity.getSimpleTypeName())) {
			logger.warning("Controller class name needs to be different from the class name of the form backing object (suggestion: '" + entity.getSimpleTypeName() + "Controller')");
			return;
		}

		Set<String> disallowedOperationSet = new HashSet<String>();
		if (!"".equals(disallowedOperations)) {
			for (String operation : StringUtils.commaDelimitedListToSet(disallowedOperations)) {
				if (!("create".equals(operation) || "update".equals(operation) || "delete".equals(operation))) {
					logger.warning("-disallowedOperations options can only contain 'create', 'update', 'delete': -disallowedOperations update,delete");
					return;
				}
				disallowedOperationSet.add(operation.toLowerCase());
			}
		}

		if (path == null || path.length() == 0) {
			PluralMetadata pluralMetadata = (PluralMetadata) metadataService.get(PluralMetadata.createIdentifier(entity, Path.SRC_MAIN_JAVA));
			Assert.notNull(pluralMetadata, "Could not determine plural for '" + entity.getSimpleTypeName() + "'");
			path = pluralMetadata.getPlural().toLowerCase();
		} else if (path.startsWith("/")) {
			path = path.substring(1);
		}

		operations.createAutomaticController(controller, entity, disallowedOperationSet, path);
	}
	
}