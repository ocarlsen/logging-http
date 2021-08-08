package com.ocarlsen.logging.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;

// TODO: Finish
public class LoggingRequestInterceptorTestManual {

    @Test
    public void process() throws Exception {

        //Creating an HttpRequestInterceptor
        final HttpRequestInterceptor requestInterceptor = new LoggingRequestInterceptor();

        //Creating a CloseableHttpClient object
        final CloseableHttpClient httpclient = HttpClients.custom().addInterceptorFirst(requestInterceptor).build();

        //Creating a request object
        //final HttpGet httpget = new HttpGet("https://www.tutorialspoint.com/");
        final HttpPost httpPost = new HttpPost("https://www.tutorialspoint.com/");

        final String body = "abc";
        final HttpEntity requestEntity = new ByteArrayEntity(body.getBytes(UTF_8));
        httpPost.setEntity(requestEntity);

        //Setting the header to it
        httpPost.setHeader(new BasicHeader("sample-header", "My first header"));
        httpPost.setHeader(new BasicHeader("demo-header", "My second header"));
        httpPost.setHeader(new BasicHeader("test-header", "My third header"));
        // httpget.setHeader(new BasicHeader("sample-header", "My first header"));
        // httpget.setHeader(new BasicHeader("demo-header", "My second header"));
        // httpget.setHeader(new BasicHeader("test-header", "My third header"));

        //Executing the request
        final HttpResponse response = httpclient.execute(httpPost);

        //Printing the status line
        System.out.println(response.getStatusLine());
    }
}