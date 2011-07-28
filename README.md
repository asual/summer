# Summer - The HTML5 Library for Java and Scala

Summer is the next generation presentation layer library for Spring MVC. It provides the world's best HTML5 template 
engine and takes advantage of many Spring features like MVC annotations and data binding, content negotiation, 
REST support, conversion and various others. In addition it supports a number of Java EE 6 specifications including 
Servlet 3.0, JPA 2.0, Bean Validation and EL 2.2.

Summer enables the creation of true web modules that package every required static resource. It also makes AJAX 
really simple by providing partial page rendering capabilities. Support for HTML5 WebSockets is available through the 
popular Atmosphere framework.

## Quickstart

You can create and run a blank Summer project by using the following commands:

    mvn archetype:generate -DarchetypeCatalog=http://www.asual.com/maven/content/groups/public/archetype-catalog.xml
    mvn eclipse:eclipse -DdownloadSources=true
    mvn jetty:run
    
## Specify the dependencies manually

In order to use Summer you need the following records to your Maven 3 POM file:

    <repositories>
        <repository>
            <id>com.asual.repositories.releases</id>
            <name>Asual Releases Repository</name>
            <url>http://www.asual.com/maven/content/groups/public</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>com.asual.summer</groupId>
            <artifactId>summer-core</artifactId>
            <version>1.0.0.M3</version>
        </dependency>
    </dependencies>

## Changes

### 07/28/2011 - 1.0.0.M3

* New LessPack module for resource optimization and less.js support.
* New Beauty module for enahnced look and feel of form components.
* New Bundle plugin for Servlet 2.5 compatibility.
* New OneJar plugin for packaging applications as executable JAR files.
* New AppEngine sample.
* Improved response rendering.
* Support for boolean attributes.
* Various minor improvements.

### 02/18/2011 - 1.0.0.M2

* New pure Scala sample.
* Improved AJAX support.
* AJAX powered form validation.
* Support for parameterized HTML templates.
* Support for Commons FileUpload Streaming API.
* Support for XML resource bundles.
* Switched to JavaEE 6 annotations.
* Separate JSON and XML view modules.
* XStream replaced by the FasterXML Databind package.
* Updated jQuery and Atmosphere versions.

### 10/01/2010 - 1.0.0.M1

Initial release.
