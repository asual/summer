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

package com.asual.summer.core.spring;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.PropertyAccessException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DefaultBindingErrorProcessor;
import org.springframework.validation.Errors;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class ExtendedBindingErrorProcessor extends DefaultBindingErrorProcessor {

	public void processPropertyAccessException(PropertyAccessException ex, BindingResult bindingResult) {
		super.processPropertyAccessException(ex, bindingResult);
		Object[] arguments = bindingResult.getAllErrors().get(bindingResult.getErrorCount() - 1).getArguments();
		arguments[1] = ExceptionUtils.getRootCause(ex).getMessage();
	}

	protected Object[] getArgumentsForBindError(String objectName, String field) {
		String[] codes = new String[] {objectName + Errors.NESTED_PATH_SEPARATOR + field, field};
		return new Object[] {new DefaultMessageSourceResolvable(codes, field), null};
	}
	
}
