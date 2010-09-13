package com.asual.summer.core.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author Rostislav Georgiev
 *
 */
public class ResourceEditorRegistrarFactoryBean implements FactoryBean<ResourceEditorRegistrar>,ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public ResourceEditorRegistrar getObject() throws Exception {
        return new ResourceEditorRegistrar(applicationContext);
    }

    @Override
    public Class<?> getObjectType() {
        return ResourceEditorRegistrar.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
