package com.asual.summer.sample.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.asual.summer.sample.domain.Status;

@Component
public class StringToStatusConvertor implements Converter<String, Status> {
    
    public Status convert(String source) {
		try {
	    	return Status.find(source);
		} catch (Exception e) {
			return null;
		}
    }

}