package com.asual.summer.core.resource.reload;

import com.asual.summer.core.resource.PropertyResource;
import com.asual.summer.core.util.BeanUtils;

public class ReloadingPropertyResource extends PropertyResource{
	
	private ReloadingStrategy reloadingStrategy;
	
	public ReloadingPropertyResource(){
		reloadingStrategy = new FileChangedReloadingStrategy();
		reloadingStrategy.setPropertyResource(this);
	}
	
	@Override
	public Object getProperty(String key){
		if(reloadingStrategy != null && reloadingStrategy.reloadingRequired()){
			performReload();
		}
		return super.getProperty(key);
	}

	/**
	 * @return the reloadingStrategy
	 */
	public ReloadingStrategy getReloadingStrategy() {
		return reloadingStrategy;
	}

	/**
	 * @param reloadingStrategy the reloadingStrategy to set
	 */
	public void setReloadingStrategy(ReloadingStrategy reloadingStrategy) {
		this.reloadingStrategy = reloadingStrategy;
		this.reloadingStrategy.setPropertyResource(this);
	}
	
	private void performReload(){
		synchronized (reloadingStrategy) {
			super.reloadPropertyPlaceholderConfigurer();
			super.postProcessBeanFactory(BeanUtils.getBeanFactory());
			reloadingStrategy.reloadingPerformed();
		}
	}

}
