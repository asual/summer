package com.asual.summer.core.spring;

import java.util.LinkedList;
import java.util.List;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;

public class ExtendedRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter{
	
	private HandlerMethodReturnValueHandler customHandlerMethodReturnValueHandler;
	
	protected ServletRequestDataBinderFactory createDataBinderFactory(List<InvocableHandlerMethod> binderMethods)
	throws Exception {
		return new ExtendedServletRequestDataBinderFactory(binderMethods, getWebBindingInitializer());
	}
	
	public ExtendedRequestMappingHandlerAdapter(){
		super();
		getMessageConverters().add(new MappingJacksonHttpMessageConverter());
	}
	
	public void setCustomHandlerMethodReturnValueHandler(HandlerMethodReturnValueHandler handler){
		this.customHandlerMethodReturnValueHandler = handler;
		
	}
	
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		List<HandlerMethodReturnValueHandler> handlers = new LinkedList<HandlerMethodReturnValueHandler>();
		handlers.add(customHandlerMethodReturnValueHandler);
		if(super.getReturnValueHandlers() != null){
			handlers.addAll(super.getReturnValueHandlers().getHandlers());
		}
		super.setReturnValueHandlers(handlers);
	}

}
