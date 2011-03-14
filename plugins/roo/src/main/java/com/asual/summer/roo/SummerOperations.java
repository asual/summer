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

import java.util.Set;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public interface SummerOperations extends ProjectOperations {

	boolean isCreateProjectAvailable();

	boolean isNewControllerAvailable();
	
	void createProject(JavaPackage topLevelPackage, String projectName, Integer majorJavaVersion);
	
	void createAutomaticController(JavaType controller, JavaType entity, Set<String> disallowedOperations, String path);	
	
	void generateAll(JavaPackage javaPackage);
	
	String getProjectRoot();

}