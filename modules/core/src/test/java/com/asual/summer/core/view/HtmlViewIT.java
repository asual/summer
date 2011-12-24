package com.asual.summer.core.view;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class HtmlViewIT {
	
	@Test
	public void testPage() throws IOException, InterruptedException {
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet("http://localhost:9090/");
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		String content = EntityUtils.toString(entity);
		assertEquals(
				"<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"    <head>\n" + 
				"        <meta charset=\"UTF-8\">\n" + 
				"        <title>\n" + 
				"            Summer - The HTML5 Library for Java and Scala\n" + 
				"        </title>\n" + 
				"        <link href=\"/css/summer-core.css\" rel=\"stylesheet\">\n" + 
				"        <script src=\"/js/summer-core.js\"></script>\n" + 
				"        <script>\n" + 
				"            function getWelcomeKey() {\n" + 
				"                return \"Welcome Key\";\n" + 
				"            }\n" + 
				"        </script>\n" + 
				"        <script>\n" + 
				"            $.summer = {\n" + 
				"                contextPath: ''\n" + 
				"            };\n" + 
				"        </script>\n" + 
				"    </head>\n" + 
				"    <body>\n" + 
				"        <h1>\n"+ 
				"            <a href=\"http://www.asual.com/summer/?module=core&amp;test=true\">Summer - The HTML5 Library for Java and Scala</a>\n" + 
				"        </h1>\n" + 
				"        <p>\n" + 
				"            © 2010-2011 Asual DZZD\n" + 
				"        </p>\n" + 
				"    </body>\n" + 
				"</html>", content.replaceAll(System.getProperty("line.separator"), "\n"));
	}
	
}