package com.ocarlsen.logging.http.client.apache;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class ResponseLoggingInterceptorTestManual extends AbstractLoggingInterceptorTestManual {

    @Override
    protected CloseableHttpClient buildClient() {
        final HttpResponseInterceptor responseInterceptor = new ResponseLoggingInterceptor();
        return HttpClients.custom().addInterceptorFirst(responseInterceptor).build();
    }
}