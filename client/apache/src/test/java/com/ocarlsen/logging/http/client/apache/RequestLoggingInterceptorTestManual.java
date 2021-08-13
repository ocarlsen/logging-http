package com.ocarlsen.logging.http.client.apache;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RequestLoggingInterceptorTestManual extends AbstractLoggingInterceptorTestManual {

    @Override
    protected CloseableHttpClient buildClient() {
        final HttpRequestInterceptor requestInterceptor = new RequestLoggingInterceptor();
        return HttpClients.custom().addInterceptorFirst(requestInterceptor).build();
    }
}