package com.asual.summer.core.spring;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.PropertyAccessException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DefaultBindingErrorProcessor;
import org.springframework.validation.Errors;

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
