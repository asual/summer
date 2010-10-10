# Summer - The HTML5 Library for Java

Summer is the next generation presentation layer library for Spring MVC. It provides the world's best HTML5 template 
engine and takes advantage of many Spring 3 features like content negotiation, REST support, resource handling and 
various others. In addition out of the box it supports a number of Java EE 6 specifications like Servlet 3.0, JPA 2, 
Bean Validation and EL 2.2.

Summer enables the creation of true web modules that package every required static resource. It also makes AJAX 
really simple by providing partial page rendering capabilities. Support for HTML5 WebSockets is available through the 
popular Atmosphere framework.

## Quickstart

You can create and run a blank Summer project by using the following commands:

    mvn archetype:generate -DarchetypeCatalog=http://www.asual.com/maven/content/groups/public/archetype-catalog.xml
    mvn eclipse:eclipse -DdownloadSources=true
    mvn jetty:run
    
## Specify the dependencies manually

In order to use Summer you need the following records to your Maven 2 POM file:

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
            <version>1.0.0.M1</version>
        </dependency>
    </dependencies>

## Changes
    
### 10/10/2010 - 1.0.0.M2

- Support for Commons FileUpload Streaming API.
- Support for XML Resource Bundles.
- Switched to JavaEE 6 Annotations.
- Updated Atmosphere Version.
- Improved Sample.

### 10/01/2010 - 1.0.0.M1

Initial release.