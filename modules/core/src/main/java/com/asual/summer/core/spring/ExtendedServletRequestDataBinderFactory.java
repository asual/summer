package com.asual.summer.core.spring;

import java.util.List;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;

public class ExtendedServletRequestDataBinderFactory extends ServletRequestDataBinderFactory{

	public ExtendedServletRequestDataBinderFactory(
			List<InvocableHandlerMethod> binderMethods,
			WebBindingInitializer initializer) {
		super(binderMethods, initializer);
	}
	
	@Override
	protected ServletRequestDataBinder createBinderInstance(Object target, String objectName, NativeWebRequest request) {
		return new ExtendedServletRequestDataBinder(target, objectName);
	}

}
