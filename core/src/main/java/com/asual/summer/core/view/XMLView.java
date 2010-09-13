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

package com.asual.summer.core.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.asual.summer.core.util.StringUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.TreeMapConverter;
import com.thoughtworks.xstream.converters.collections.TreeSetConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Component("xml")
public class XMLView extends AbstractResponseView {

    private static final String DEFAULT_CONTENT_TYPE = "application/xml";
    private static final String DEFAULT_EXTENSION = "xml";

    public XMLView() {
        super();
        setContentType(DEFAULT_CONTENT_TYPE);
        setExtension(DEFAULT_EXTENSION);
    }
	
    protected void renderMergedOutputModel(Map<String, Object> model,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
    	XStream xstream = new XStream() {
		    protected MapperWrapper wrapMapper(MapperWrapper next) {
		        return new PersistentCollectionPackageStrippingMapper(next);
		    }
		};
		xstream.registerConverter(new PersistentCollectionConverter(xstream.getMapper()));
		
		String encoding = StringUtils.getEncoding();
		byte[] bytes = ("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n" + xstream.toXML(filterModel(model))).getBytes(encoding);
		
		response.setContentLength(bytes.length);
        response.setContentType(getContentType());
        response.setCharacterEncoding(StringUtils.getEncoding());
		response.getOutputStream().write(bytes);
	}
	
    @SuppressWarnings("rawtypes")
    private static class PersistentCollectionPackageStrippingMapper extends MapperWrapper {
    	
        public PersistentCollectionPackageStrippingMapper(Mapper wrapped) {
            super(wrapped);
        }
        
        public String serializedClass(Class type) {
        	return StringUtils.toCamelCase(replaceClass(type).getSimpleName());
        }
        
        private Class replaceClass(Class type) {
        	if (PersistentCollectionConverter.isPersistenCollection(type)) {
    			if (SortedSet.class.isAssignableFrom(type)) {
    				type = TreeSet.class;
    			} else if (SortedMap.class.isAssignableFrom(type)) {
    				type = TreeMap.class;
    			} else if (Map.class.isAssignableFrom(type)) {
    				type = HashMap.class;
    			} else if (Set.class.isAssignableFrom(type)) {
    				type = HashSet.class;
    			} else if (List.class.isAssignableFrom(type)) {
    				type = ArrayList.class;
    			}
    		}
        	return type;
        }
    }
    
	@SuppressWarnings("rawtypes")
    private static class PersistentCollectionConverter implements Converter {

        private final Mapper mapper;
            
    	public PersistentCollectionConverter(Mapper mapper) {
            this.mapper = mapper;
    	}
        
		public boolean canConvert(Class type) {
			return isPersistenCollection(type);
	    }

		public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
			
			Converter converter = null;
			
			if (source instanceof TreeSet) {
				converter = new TreeSetConverter(mapper);
			} else if (source instanceof TreeMap) {
				converter = new TreeMapConverter(mapper);
			} else if (source instanceof Map) {
				converter = new CollectionConverter(mapper);
			} else if (source instanceof List) {
				converter = new CollectionConverter(mapper);
			}
			
			if (converter != null) {
				converter.marshal(source, writer, context);
			}
		}
		
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
			return null;
		}

        @SuppressWarnings("unchecked")
		private static boolean isPersistenCollection(Class type) {
			try {
				Class persistentCollection = Class.forName("org.hibernate.collection.PersistentCollection");
	    		return persistentCollection.isAssignableFrom(type);
			} catch (ClassNotFoundException e) {
				return false;
			}
        }    	
    }

}