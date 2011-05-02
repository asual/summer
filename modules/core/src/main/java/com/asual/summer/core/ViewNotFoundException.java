/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.core;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asual.summer.core.util.RequestUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class ViewNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private final Log logger = LogFactory.getLog(getClass());
	private boolean logged = false;
	
	public ViewNotFoundException() {
		super(RequestUtils.getUrl());
	}
	
	public ViewNotFoundException(String message) {
		super(message);
	}
	
	public ViewNotFoundException(String message, Throwable e) {
		super(message, e);
	}

	public ViewNotFoundException(Throwable e) {
		super(e);
	}

    public void printStackTrace(PrintStream s) {
    	if (!logged) {
    		logger.error(getMessage());
    		logged = true;
    	}
    }
    
    public void printStackTrace(PrintWriter s) {
    	if (!logged) {
    		logger.error(getMessage());
    		logged = true;
    	}
    }
    
    public StackTraceElement[] getStackTrace() {
    	return new StackTraceElement[0];
    }
    
}
