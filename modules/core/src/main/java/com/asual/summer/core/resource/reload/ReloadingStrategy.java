package com.asual.summer.core.resource.reload;

import com.asual.summer.core.resource.PropertyResource;


public interface ReloadingStrategy {
	
	/**
	 * Set a reference to the PropertyResource to be monitored.
	 * @param configuration
	 */
	public void setPropertyResource(PropertyResource configuration);
	
    /**
     * Initialize the strategy.
     */
    void init();

    /**
     * Tell if the evaluation of the strategy requires to reload the configuration.
     *
     * @return a flag whether a reload should be performed
     */
    boolean reloadingRequired();

    /**
     * Notify the strategy that the file has been reloaded.
     */
    void reloadingPerformed();
}
