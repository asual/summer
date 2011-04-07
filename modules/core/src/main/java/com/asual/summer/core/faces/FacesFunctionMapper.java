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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;

import javax.el.FunctionMapper;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesFunctionMapper extends FunctionMapper {
	
	private final Log logger = LogFactory.getLog(getClass());

	private static HashMap<String, JavaFileObject> classes = new HashMap<String, JavaFileObject>();
	
	private static ClassLoader classLoader = new ClassLoader() {
		
	    protected Class<?> findClass(String name) throws ClassNotFoundException {
	        if (classes.containsKey(name)) {
	            byte[] bytes = ((MemoryJavaFileObject) classes.get(name)).baos.toByteArray();
	            return defineClass(name, bytes, 0, bytes.length);
	        }
	        return Class.forName(name, true, getClass().getClassLoader());
	    }
	};
	
	private class JavaObjectFromString extends SimpleJavaFileObject {
		
	    private String contents = null;
	    
	    public JavaObjectFromString(String className, String contents) throws Exception{
	        super(new URI(className + Kind.SOURCE.extension), Kind.SOURCE);
	        this.contents = contents;
	    }
	    
	    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
	        return contents;
	    }
	}
	
	private class MemoryJavaFileObject extends SimpleJavaFileObject {

	    ByteArrayOutputStream baos;

	    MemoryJavaFileObject(String name, Kind kind) {
	        super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension), kind);
	    }

	    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException, IllegalStateException, UnsupportedOperationException {
	        throw new UnsupportedOperationException();
	    }

	    public InputStream openInputStream() throws IOException, IllegalStateException, UnsupportedOperationException {
	        return new ByteArrayInputStream(baos.toByteArray());
	    }

	    public OutputStream openOutputStream() throws IOException, IllegalStateException, UnsupportedOperationException {
	        return baos = new ByteArrayOutputStream();
	    }
	}
	
	private class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
		
	    MemoryFileManager (JavaCompiler compiler){
	        super(compiler.getStandardFileManager(null,null,null));
	    }
	    
	    public JavaFileObject getJavaFileForOutput(Location location, String name, Kind kind, FileObject sibling) throws java.io.IOException {
	        JavaFileObject jfo = new MemoryJavaFileObject(name, kind);
	        classes.put(name, jfo);
	        return jfo;
	    }
	}
	
	public Method resolveFunction(String namespace, final String fn) {
		
		try {
			
			String className = fn + "Script";
			
			if (!classes.containsKey(className)) {
				
				StringBuilder out = new StringBuilder();
				out.append("public class " + className + " {");
				out.append("	private static String fn = \"" + fn + "\";");
				out.append("	public static Object call(Object... args) throws Exception {");
				out.append("		return Class.forName(\"com.asual.summer.core.util.ScriptUtils\")");
				out.append("			.getDeclaredMethod(\"call\", String.class, Object[].class).invoke(null, fn, args);");
				out.append("	}");
				out.append("}");
				
				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				JavaFileManager jfm = new MemoryFileManager(compiler);
				JavaFileObject file = new JavaObjectFromString(className, out.toString());
				compiler.getTask(null, jfm, null, null, null, Arrays.asList(file)).call();				
			}
			
	    	return Class.forName(className, true, classLoader).getDeclaredMethod("call", Object[].class);
		    
		} catch (Exception e) {
			
			logger.error(e.getMessage(), e);
		}
		
		return null;
	}
	
}
