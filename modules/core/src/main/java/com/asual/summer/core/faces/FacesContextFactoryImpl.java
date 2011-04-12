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

package com.asual.summer.core.faces;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.ExternalContextFactory;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesContextFactoryImpl extends FacesContextFactory {

	private ExceptionHandlerFactory exceptionHandlerFactory;
	private ExternalContextFactory externalContextFactory;
	
	public FacesContextFactoryImpl() {
		exceptionHandlerFactory = (ExceptionHandlerFactory)
			  FactoryFinder.getFactory(FactoryFinder.EXCEPTION_HANDLER_FACTORY);
		externalContextFactory = (ExternalContextFactory)
			  FactoryFinder.getFactory(FactoryFinder.EXTERNAL_CONTEXT_FACTORY);
	}

	public FacesContext getFacesContext(Object sc, Object request, 
			Object response, Lifecycle lifecycle) throws FacesException {
		
		FacesContext ctx = new FacesContextImpl(
				externalContextFactory.getExternalContext(sc, request, response), lifecycle);
		ctx.setExceptionHandler(exceptionHandlerFactory.getExceptionHandler());
		return ctx;
	}

}