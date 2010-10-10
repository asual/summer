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

package com.asual.summer.core.spring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class StreamFileItem implements FileItem {

	private static final long serialVersionUID = 1L;

	private String fieldName;
	private String contentType;
	private boolean formField;
	private String name;
	private ByteArrayOutputStream outputStream;
	
	public StreamFileItem(String fieldName, String contentType, boolean formField, String fileName, long maxSize) {
		this.fieldName = fieldName;
		this.contentType = contentType;
		this.formField = formField;
		this.name = fileName;
		this.outputStream = (maxSize > 0) ? new ByteArrayOutputStream((int) maxSize) : new ByteArrayOutputStream();
	}
	
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(get());
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isInMemory() {
		return true;
	}
	
	public long getSize() {
		return get().length;
	}
	
	public byte[] get() {
		return outputStream.toByteArray();
	}
	
	public String getString(String encoding) throws UnsupportedEncodingException {
		return new String(get(), encoding);
	}
	
	public String getString() {
		return new String(get());
	}
	
	public void write(File file) throws Exception {
	}
	
	public void delete() {
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public boolean isFormField() {
		return formField;
	}
	
	public void setFormField(boolean formField) {
		this.formField = formField;
	}
	
	public OutputStream getOutputStream() throws IOException {
		return outputStream;
	}
	
}