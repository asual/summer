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

package com.asual.summer.xml;

import java.io.IOException;

import javax.inject.Named;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.fasterxml.jackson.xml.XmlMapper;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class XmlUtils {
	
	private static XmlMapper mapper = new XmlMapper();
	
	public static String encode(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(obj);
	}
	
	public static <T> T decode(String str, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(str, clazz);
	}
	
}
