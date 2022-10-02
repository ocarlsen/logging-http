# logging-http

A library for logging HTTP requests and responses on server and client.

There are different modules for client and server, and modules shared by both:

## client

Client logging libraries.

### apache

Logging `org.apache.httpHttpRequestInterceptor`s for request and response.

### spring

Logging `org.springframework.http.clientClientHttpRequestInterceptor`s for request and response.

## parent

The parent POM.

## report

Aggregator project for reports, e.g. JaCoCo.

## server

Server logging libraries.

### javaee

Logging `javax.servlet.Filter`s for request and response.


### jdk

Logging `com.sun.net.httpserver.Filter`s for request and response.


## util

Utilities for project.
