# logging-http

[![Maven Central](https://img.shields.io/maven-central/v/com.ocarlsen.logging.http/logging-http.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.ocarlsen.logging.http%22%20AND%20a:%22logging-http%22)
[![Build](https://github.com/ocarlsen/logging-http/actions/workflows/build.yml/badge.svg)](https://github.com/ocarlsen/logging-http/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ocarlsen_logging-http&metric=alert_status)](https://sonarcloud.io/dashboard?id=ocarlsen_logging-http)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=ocarlsen_logging-http&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ocarlsen_logging-http)

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
        <version>1.0.2-SNAPSHOT</version>
    </dependency>

#### Gradle

    compile 'com.ocarlsen.logging.http:client-apache:1.0.2-SNAPSHOT

### spring

Logging `org.springframework.http.clientClientHttpRequestInterceptor`s for request and response.

#### Maven

    <dependency>
        <groupId>com.ocarlsen.logging.http</groupId>
        <artifactId>client-spring</artifactId>
        <version>1.0.2-SNAPSHOT</version>
    </dependency>

#### Gradle

    compile 'com.ocarlsen.logging.http:client-spring:1.0.2-SNAPSHOT


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
        <version>1.0.2-SNAPSHOT</version>
    </dependency>

#### Gradle

    compile 'com.ocarlsen.logging.http:server-javaee:1.0.2-SNAPSHOT



### jdk

Logging `com.sun.net.httpserver.Filter`s for request and response.

#### Maven

    <dependency>
        <groupId>com.ocarlsen.logging.http</groupId>
        <artifactId>server-jdk</artifactId>
        <version>1.0.2-SNAPSHOT</version>
    </dependency>

#### Gradle

    compile 'com.ocarlsen.logging.http:server-jdk:1.0.2-SNAPSHOT


## util

Utilities for project.
