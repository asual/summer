package com.asual.summer.core.faces;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.asual.summer.core.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring.xml" })
public class FacesResourceProcessorTest {
	
	@Test
	public void testPage() throws FileNotFoundException, IOException {
		
		URL url; 
		byte[] bytes;
		String encoding = (String) ResourceUtils.getProperty("app.encoding");
		
		url = ResourceUtils.getClasspathResource("META-INF/pages/index.html");
		bytes = FacesResourceProcessor.execute(url, url.openConnection().getInputStream());
		
		assertEquals(
				"<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"	\n" + 
				"	<title>${messages.page.title}</title>\n" + 
				"	\n" + 
				"	<p>& &#38; &#169;</p>\n" + 
				"	\n" + 
				"</html>", new String(bytes, encoding));
		
	}

}
