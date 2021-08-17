package com.ocarlsen.logging.http.client.apache;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class RequestLoggingInterceptorTestManual extends AbstractLoggingInterceptorTestManual {

    @Override
    protected CloseableHttpClient buildClient() {
        final HttpRequestInterceptor requestInterceptor = new RequestLoggingInterceptor();
        return HttpClients.custom().addInterceptorFirst(requestInterceptor).build();
    }
}