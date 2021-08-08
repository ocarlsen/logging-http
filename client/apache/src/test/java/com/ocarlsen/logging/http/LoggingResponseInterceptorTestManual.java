package com.ocarlsen.logging.http;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;

// TODO: Finish
public class LoggingResponseInterceptorTestManual {

    @Test
    public void process() throws IOException {
        final HttpResponseInterceptor responseInterceptor = new LoggingResponseInterceptor();

        //Creating a CloseableHttpClient object
        try (final CloseableHttpClient httpclient = HttpClients.custom().addInterceptorFirst(responseInterceptor).build()) {

            //Creating a request object
            final HttpGet httpGet = new HttpGet("https://www.tutorialspoint.com/");

            //Executing the request
            final HttpResponse response = httpclient.execute(httpGet);

            System.out.println(response.getStatusLine());

            HttpEntity entity = response.getEntity();
            final Header contentEncodingHeader = entity.getContentEncoding();
            if (contentEncodingHeader != null) {
                final HeaderElement[] encodings = contentEncodingHeader.getElements();
                for (final HeaderElement encoding : encodings) {
                    if (encoding.getName().equalsIgnoreCase("gzip")) {
                        entity = new GzipDecompressingEntity(entity);
                        break;
                    }
                }
            }
            final String body = EntityUtils.toString(entity);
            System.out.println("body = " + body);
        }
    }
}