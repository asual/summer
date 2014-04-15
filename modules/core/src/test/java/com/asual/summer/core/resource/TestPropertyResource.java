package com.asual.summer.core.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.asual.summer.core.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring-property-resource.xml" })
public class TestPropertyResource {
	
	@Test
	public void testOrder(){
		assertEquals(10, ((Integer) ResourceUtils.getProperty("test")).intValue());
	}

}
