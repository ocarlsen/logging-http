package com.ocarlsen.logging.http.client.apache;

import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ResponseLoggingInterceptorTestManual extends AbstractLoggingInterceptorTestManual {

    @Override
    protected CloseableHttpClient buildClient() {
        final HttpResponseInterceptor responseInterceptor = new ResponseLoggingInterceptor();
        return HttpClients.custom().addInterceptorFirst(responseInterceptor).build();
    }
}