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
		bytes = FacesResourceProcessor.execute(url, url.openConnection().getInputStream(), encoding);
		
		assertEquals(
				"<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"	\n" + 
				"	<title>${messages.page.welcome}</title>\n" + 
				"	\n" + 
				"	<script type=\"text/expression\">\n" + 
				"		function getWelcomeKey() {\n" + 
				"			return 'page'.concat('.' + 'welcome');\n" + 
				"		}\n" + 
				"	</script>\n" + 
				"	\n" + 
				"	<h1><a href=\"http://www.asual.com/summer/?module=core&amp;test=true\">${resourceUtils.getMessage(getWelcomeKey())}</a></h1>\n" + 
				"	\n" + 
				"	<p>&#169; 2010-2011 Asual DZZD</p>\n" + 
				"	\n" + 
				"</html>", new String(bytes, encoding));
	}

}
