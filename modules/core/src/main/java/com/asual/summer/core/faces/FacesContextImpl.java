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

import javax.el.ELContext;
import javax.faces.context.ExternalContext;
import javax.faces.lifecycle.Lifecycle;

import com.sun.faces.el.ELContextImpl;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesContextImpl extends com.sun.faces.context.FacesContextImpl {

	public FacesContextImpl(ExternalContext ec, Lifecycle lifecycle) {
		super(ec, lifecycle);
	}
	
    public ELContext getELContext() {
    	ELContextImpl elContext = (ELContextImpl) super.getELContext();
        elContext.setFunctionMapper(new FacesFunctionMapper());
        return elContext;
    }

}