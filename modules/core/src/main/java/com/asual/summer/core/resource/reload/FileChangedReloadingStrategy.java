package com.asual.summer.core.resource.reload;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

import com.asual.summer.core.resource.PropertyResource;
import com.asual.summer.core.util.ResourceUtils;

public class FileChangedReloadingStrategy implements ReloadingStrategy {

	private final Log logger = LogFactory.getLog(getClass());
	/** Constant for the jar URL protocol. */
	private static final String JAR_PROTOCOL = "jar";

	/** Constant for the default refresh delay. */
	private static final int DEFAULT_REFRESH_DELAY = 5000;

	/** Stores a reference to the resource to be monitored. */
	protected PropertyResource resource;

	/** The last time the configuration file was modified. */
	protected long lastModified;

	/** The last time the file was checked for changes. */
	protected long lastChecked = -1;

	/** The minimum delay in milliseconds between checks. */
	protected long refreshDelay = DEFAULT_REFRESH_DELAY;

	/** A flag whether a reload is required. */
	private boolean reloading = false;
	
	private File[] files;

	public void setPropertyResource(PropertyResource configuration) {
		this.resource = configuration;
	}

	public void init() {
		updateLastModified();
	}

	public boolean reloadingRequired() {
		synchronized (this) {
			if (!reloading) {
				if(lastChecked > 0){
					long now = System.currentTimeMillis();
	
					if (now > lastChecked + refreshDelay) {
						lastChecked = now;
						if (hasChanged()) {
							reloading = true;
						}
					}
				}
				else{
					lastChecked = System.currentTimeMillis();
				}
			}

			return reloading;
		}
	}

	public void reloadingPerformed() {
		synchronized (this) {
			updateLastModified();
			logger.info("Reloaded properties files.");
		}
	}

	/**
	 * Return the minimal time in milliseconds between two reloadings.
	 * 
	 * @return the refresh delay (in milliseconds)
	 */
	public long getRefreshDelay() {
		return refreshDelay;
	}

	/**
	 * Set the minimal time between two reloadings.
	 * 
	 * @param refreshDelay
	 *            refresh delay in milliseconds
	 */
	public void setRefreshDelay(long refreshDelay) {
		this.refreshDelay = refreshDelay;
	}

	/**
	 * Update the last modified time.
	 */
	protected void updateLastModified() {
		for (File file : getFiles()) {
			if (file != null) {
				long fModified = file.lastModified();
				if (fModified > lastModified) {
					lastModified = fModified;
				}
			}
		}
		reloading = false;
	}

	/**
	 * Check if the configuration has changed since the last time it was loaded.
	 * 
	 * @return a flag whether the configuration has changed
	 */
	protected boolean hasChanged() {
		boolean hasChanged = false;
		for (File file : getFiles()) {
			if (file == null || !file.exists()) {
				continue;
			} else if (file.lastModified() > lastModified) {
				hasChanged = true;
				break;
			}
		}

		return hasChanged;
	}

	/**
	 * Returns the file that is monitored by this strategy. Note that the return
	 * value can be <b>null </b> under some circumstances.
	 * 
	 * @return the monitored file
	 */
	protected File[] getFiles() {
		if (files == null){
			List<File> filesList = new ArrayList<File>(resource.getResources().length);
			for (int i = 0; i < resource.getResources().length; i++) {
				Resource location = resource.getResources()[i];
				try {
					filesList.add(fileFromURL(location.getURL()));
				} catch (IOException e) {
					logger.warn("Missing file or invalid URL: " + location);
				}
			}
			files = filesList.toArray(new File[filesList.size()]);
		}
		return files;
	}

	/**
	 * Helper method for transforming a URL into a file object. This method
	 * handles file: and jar: URLs.
	 * 
	 * @param url
	 *            the URL to be converted
	 * @return the resulting file or <b>null </b>
	 */
	private File fileFromURL(URL url) {
		if (JAR_PROTOCOL.equals(url.getProtocol())) {
			String path = url.getPath();
			try {
				return ResourceUtils.fileFromURL(new URL(path.substring(0,
						path.indexOf('!'))));
			} catch (MalformedURLException mex) {
				return null;
			}
		} else {
			return ResourceUtils.fileFromURL(url);
		}
	}

}
