# logging-http

TODO: Add badges

A library for logging HTTP requests and responses on server and client.

There are different modules for client and server, and modules shared by both:

## client

Client logging libraries.

### apache

Logging `org.apache.httpHttpRequestInterceptor`s for request and response.

#### Maven

    <dependency>
        <groupId>com.ocarlsen.logging.http</groupId>
        <artifactId>client-apache</artifactId>
        <version>1.0.1-SNAPSHOT</version>
    </dependency>

#### Gradle

    compile 'com.ocarlsen.logging.http:client-apache:1.0.1-SNAPSHOT

### spring

Logging `org.springframework.http.clientClientHttpRequestInterceptor`s for request and response.

#### Maven

    <dependency>
        <groupId>com.ocarlsen.logging.http</groupId>
        <artifactId>client-spring</artifactId>
        <version>1.0.1-SNAPSHOT</version>
    </dependency>

#### Gradle

    compile 'com.ocarlsen.logging.http:client-spring:1.0.1-SNAPSHOT


## parent

The parent POM.

## report

Aggregator project for reports, e.g. JaCoCo.

## server

Server logging libraries.

### javaee

Logging `javax.servlet.Filter`s for request and response.

#### Maven

    <dependency>
        <groupId>com.ocarlsen.logging.http</groupId>
        <artifactId>server-javaee</artifactId>
        <version>1.0.1-SNAPSHOT</version>
    </dependency>

#### Gradle

    compile 'com.ocarlsen.logging.http:server-javaee:1.0.1-SNAPSHOT



### jdk

Logging `com.sun.net.httpserver.Filter`s for request and response.

#### Maven

    <dependency>
        <groupId>com.ocarlsen.logging.http</groupId>
        <artifactId>server-jdk</artifactId>
        <version>1.0.1-SNAPSHOT</version>
    </dependency>

#### Gradle

    compile 'com.ocarlsen.logging.http:server-jdk:1.0.1-SNAPSHOT


## util

Utilities for project.
