package com.asual.summer.core.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.SmartView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;

import com.asual.summer.core.ResponseViews;
import com.asual.summer.core.util.BeanUtils;
import com.asual.summer.core.view.AbstractResponseView;

public class AnnotationHandlerMethodReturnValueHandler implements
		HandlerMethodReturnValueHandler {

	@Autowired
	private ViewResolverConfiguration viewResolverConfiguration;

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		Class<?> paramType = returnType.getParameterType();
		return ModelAndView.class.isAssignableFrom(paramType) || void.class.equals(paramType) || ModelMap.class.isAssignableFrom(paramType) || View.class.isAssignableFrom(paramType);
	}

	@Override
	public void handleReturnValue(Object returnValue,
			MethodParameter returnType, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest) throws Exception {

		/*
		 * if(mav.isReference()) { String viewName = mav.getViewName();
		 * mavContainer.setViewName(viewName); if (viewName != null &&
		 * viewName.startsWith("redirect:")) {
		 * mavContainer.setRedirectModelScenario(true); } }
		 */
		View view = null;
		if (returnValue != null) {
			if (returnValue instanceof ModelAndView) {
				ModelAndView mav = (ModelAndView) returnValue;
				mavContainer.addAllAttributes(mav.getModelMap());
				view = mav.getView();
				if(view == null){
					mavContainer.setViewName(mav.getViewName());
				}
			} else if (returnValue instanceof ModelMap) {
				mavContainer.addAllAttributes((ModelMap) returnValue);
			} else if (returnValue instanceof View){
				view = (View) returnValue;
			}
		}

		// summer logic
		if(view != null){
			mavContainer.setView(view);
			ResponseViews viewAnn = AnnotationUtils.findAnnotation(
					returnType.getMethod(), ResponseViews.class);
			if (view instanceof SmartView) {
				if (((SmartView) view).isRedirectView()) {
					mavContainer.setRedirectModelScenario(true);
				}
			}
			else if (viewAnn != null){
				Class<? extends AbstractResponseView>[] values = viewAnn.value();
				List<AbstractView> views = new ArrayList<AbstractView>();
				boolean explicit = viewAnn.explicit();

				if (values.length != 0) {
					for (Class<? extends AbstractResponseView> value : values) {
						views.addAll(BeanUtils.getBeansOfType(value).values());
					}
				}

				view = (AbstractResponseView) viewResolverConfiguration.handleViews(views, webRequest);
				if (explicit) {
					view = views.get(0);
				}
			}
			
		}
		else{
			ResponseViews viewAnn = AnnotationUtils.findAnnotation(
					returnType.getMethod(), ResponseViews.class);
			if (viewAnn != null){
				Class<? extends AbstractResponseView>[] values = viewAnn.value();
				List<AbstractView> views = new ArrayList<AbstractView>();
				boolean explicit = viewAnn.explicit();

				if (values.length != 0) {
					for (Class<? extends AbstractResponseView> value : values) {
						views.addAll(BeanUtils.getBeansOfType(value).values());
					}
				}

				view = (AbstractResponseView) viewResolverConfiguration.handleViews(views, webRequest);
				if (explicit) {
					view = views.get(0);
				}
				if(view != null){
					mavContainer.setView(view);
				}
			}
		}
		
		

	}

}
