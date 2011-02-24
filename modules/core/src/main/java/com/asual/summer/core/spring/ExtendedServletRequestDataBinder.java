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

package com.asual.summer.core.spring;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.asual.summer.core.util.RequestUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class ExtendedServletRequestDataBinder extends ServletRequestDataBinder {

	public ExtendedServletRequestDataBinder(Object target, String objectName) {
		super(target, objectName);
	}
	
	public BindingResult getBindingResult() {
		BindingResult result = super.getInternalBindingResult();
		if (RequestUtils.isValidation() && !result.hasErrors()) {
			result.addError(new ObjectError("validation", "success"));
		}
		return result;
	}

}
