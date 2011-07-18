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

package com.asual.summer.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.inject.Named;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class ObjectUtils {

	public static Object convert(String value) {
		if (Pattern.compile("true|false", Pattern.CASE_INSENSITIVE).matcher(value).matches()) {
			return Boolean.valueOf(value.toLowerCase());
		} else {
			try {
				return Integer.valueOf(value);
			} catch (NumberFormatException e) {
				return value;
			}
		}
	}
	
	public static int size(Object obj) {
		if (obj instanceof String) {
			return ((String) obj).length();			
		} else if (obj instanceof Collection) {
			return ((Collection<?>) obj).size();
		} else if (obj instanceof Map) {
			return ((Map<?, ?>) obj).size();
		} else if (obj.getClass().isArray()) {
			return ((Object[]) obj).length;
		}
		return 0;
	}
	
	public static byte[] compress(Object obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gz = new GZIPOutputStream(baos);
		ObjectOutputStream oos = new ObjectOutputStream(gz);
		oos.writeObject(obj);
		oos.close();
		return baos.toByteArray();
	}
	
	public static Object decompress(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		GZIPInputStream gz = new GZIPInputStream(bais);
		ObjectInputStream ois = new ObjectInputStream(gz);
		Object obj = ois.readObject();
		ois.close();
		return obj;
	}

}
