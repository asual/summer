# Summer - An HTML5 Presentation Layer Library for Java

Summer is an HTML5 presentation layer library for modern Java applications. It enforces a 
set of best practices that will let you produce neat applications with great look and feel. 
Summer streamlines the development process while preserving the complete control over the 
application. It's designed for modularity and extensibility.

## Start a Project

You can create and run a blank Summer project by using the following commands:

    mvn archetype:generate -DarchetypeCatalog=http://www.asual.com/maven/content/groups/public/archetype-catalog.xml
    cd sample
    mvn eclipse:eclipse -DdownloadSources=true
    mvn jetty:run
    
## Add the dependencies manually

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