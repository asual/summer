package com.asual.summer.sample.domain;

import java.io.IOException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.springframework.web.multipart.MultipartFile;

public class Image implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

    private String value;
    private String contentType;
    private byte[] bytes;
    
    public Image(MultipartFile file) throws IOException {
    	value = file.getOriginalFilename();
    	contentType = file.getContentType();
    	bytes = file.getBytes();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBytes() { 
        return bytes; 
    } 

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
    
    public boolean equals(Object o) { 
        if (!(o instanceof Image)) { 
            return false;
        }
        return new EqualsBuilder().append(bytes, ((Image) o).getBytes()).isEquals();
    }
    
}
