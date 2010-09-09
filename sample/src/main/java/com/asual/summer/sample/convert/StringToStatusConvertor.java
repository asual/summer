package com.asual.summer.sample.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.asual.summer.core.util.StringUtils;
import com.asual.summer.sample.domain.Status;

@Component
public class StringToStatusConvertor implements Converter<String, Status> {
    
    public Status convert(String source) {
    	if (StringUtils.isEmpty(source)) {
            return null;
    	}
        return Status.findStatus(Integer.parseInt(source));
    }

}