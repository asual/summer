package com.asual.summer.core.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.accept.ParameterContentNegotiationStrategy;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.accept.ServletPathExtensionContentNegotiationStrategy;
import org.springframework.web.context.ServletContextAware;

import com.asual.summer.core.util.BeanUtils;
import com.asual.summer.core.view.AbstractResponseView;

public class ExtendedContentNegotiationManagerFactoryBean 
implements FactoryBean<ContentNegotiationManager>, ServletContextAware, InitializingBean {
	
	private boolean favorPathExtension = true;
	private boolean favorParameter = false;
	private boolean ignoreAcceptHeader = false;
	private String parameterName = "format";
	private MediaType defaultContentType = MediaType.TEXT_HTML;
	
	private Map<String, MediaType> mediaTypes = new HashMap<String, MediaType>();
	
	private ServletContext servletContext;
	
	private ContentNegotiationManager contentNegotiationManager;
	
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, AbstractResponseView> responseViews = BeanUtils.getBeansOfType(AbstractResponseView.class);
		for(AbstractResponseView responseView : responseViews.values()){
			mediaTypes.put(responseView.getExtension(), MediaType.parseMediaType(responseView.getContentType()));
		}
		mediaTypes.put("html", defaultContentType);
		
		List<ContentNegotiationStrategy> strategies = new ArrayList<ContentNegotiationStrategy>();
		if (this.favorPathExtension) {
			PathExtensionContentNegotiationStrategy strategy;
			if (this.servletContext != null) {
				strategy = new ServletPathExtensionContentNegotiationStrategy(this.servletContext, this.mediaTypes);
			} else {
				strategy = new PathExtensionContentNegotiationStrategy(this.mediaTypes);
			}
			strategies.add(strategy);
		}

		if (this.favorParameter) {
			ParameterContentNegotiationStrategy strategy = new ParameterContentNegotiationStrategy(this.mediaTypes);
			strategy.setParameterName(this.parameterName);
			strategies.add(strategy);
		}

		if (!this.ignoreAcceptHeader) {
			strategies.add(new HeaderContentNegotiationStrategy());
		}

		if (this.defaultContentType != null) {
			strategies.add(new FixedContentNegotiationStrategy(this.defaultContentType));
		}
		this.contentNegotiationManager = new ContentNegotiationManager(strategies);
		
	}

	@Override
	public ContentNegotiationManager getObject() throws Exception {
		return contentNegotiationManager;
	}

	@Override
	public Class<?> getObjectType() {
		return ContentNegotiationManager.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
