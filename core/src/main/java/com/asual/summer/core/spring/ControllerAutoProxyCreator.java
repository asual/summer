package com.asual.summer.core.spring;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import com.asual.summer.core.ErrorResolver;
import com.asual.summer.core.util.RequestUtils;

@Named
public class ControllerAutoProxyCreator extends AbstractAutoProxyCreator {

	private static final long serialVersionUID = 1L;
	private static final ValidationMethodInterceptor interceptor = new ValidationMethodInterceptor();
	
	@Override
	protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource targetSource) {
		if (isControllerType(beanClass)) {
			return new Object[] { interceptor };
		}
		return DO_NOT_PROXY;
	}
	
	private boolean isControllerType(Class<?> beanClass) {
		return (Controller.class.isAssignableFrom(beanClass) ||
				AnnotationUtils.findAnnotation(beanClass, Controller.class) != null);
	}	
	
	private static class ValidationMethodInterceptor implements MethodInterceptor {
		
		public Object invoke(MethodInvocation invocation) throws Throwable {
			String form = (String) RequestUtils.getParameter("_form");
			if (RequestUtils.isValidation() && form != null && RequestUtils.getAttribute(ErrorResolver.ERRORS_ATTRIBUTE) != null) {
				RequestUtils.setAttribute(ErrorResolver.ERRORS_ATTRIBUTE, new HashMap<String, Map<String, Object>>());
				return new ModelAndView(new InternalResourceView(form));
			} else {
				return invocation.proceed();
			}
		}
	}

}