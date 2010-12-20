/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.ajax;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.faces.component.visit.PartialVisitContext;
import com.sun.faces.facelets.compiler.UIInstructions;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class AjaxVisitContext extends PartialVisitContext {
    
    private NullWriter nullWriter = new NullWriter();
    private Collection<String> unvisitedClientIds;
    
    public AjaxVisitContext(Collection<String> clientIds, Set<VisitHint> hints) {
        super(FacesContext.getCurrentInstance(), clientIds, hints);
        unvisitedClientIds = new HashSet<String>(clientIds);
    }

    public VisitResult invokeVisitCallback(UIComponent component, 
            VisitCallback callback) {
        
        if (component instanceof UIInstructions) {
            try {
                FacesContext context = FacesContext.getCurrentInstance();
                ResponseWriter writer = context.getResponseWriter();
                context.setResponseWriter(nullWriter);
                component.encodeBegin(context);
                context.setResponseWriter(writer);
            } catch (IOException e) {}
        }
        
        String clientId = component.getClientId();
        
        if (!getIdsToVisit().contains(clientId)) {
            clientId = null;
            return VisitResult.ACCEPT;
        }
        
        VisitResult result = callback.visit(this, component);
        unvisitedClientIds.remove(clientId);
        
        if (unvisitedClientIds.isEmpty()) {
            return VisitResult.COMPLETE;
        }
        
        return result;
    }
    
    public Collection<String> getSubtreeIdsToVisit(UIComponent component) {
        return getIdsToVisit();     
    }

    private static class NullWriter extends ResponseWriter {

        public String getContentType() {
            return null;
        }
        
        public String getCharacterEncoding() {
            return null;
        }
        
        public void flush() throws IOException {}
        
        public void startDocument() throws IOException {}
        
        public void endDocument() throws IOException {}
        
        public void startElement(String name, UIComponent component)
            throws IOException {
        }
        
        public void endElement(String name) throws IOException {}
        
        public void writeAttribute(String name, Object value, String property)
            throws IOException {
        }
        
        public void writeURIAttribute(String name, Object value, String property)
            throws IOException {
        }
        
        public void writeComment(Object comment) throws IOException {}
        
        public void writeText(Object text, String property) throws IOException {}
        
        public void writeText(char[] text, int off, int len) throws IOException {}
        
        public ResponseWriter cloneWithWriter(Writer writer) {
            return null;
        }
        
        public void write(char[] cbuf, int off, int len) throws IOException {}
        
        public void close() throws IOException {}

    }    
}