package com.ocarlsen.logging.http.client.apache;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_ENCODING;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public abstract class AbstractLoggingInterceptorTestManual {

    @Test
    public void process_get() throws IOException {
        final HttpGet httpGet = new HttpGet("https://www.tutorialspoint.com/");  // Returns content with gzip encoding

        httpGet.setHeader(new BasicHeader("sample-header", "My first header"));
        httpGet.setHeader(new BasicHeader("demo-header", "My second header"));
        httpGet.setHeader(new BasicHeader("test-header", "My third header"));

        process(httpGet);
    }

    @Test
    public void process_post() throws IOException {
        final HttpPost httpPost = new HttpPost("https://reqbin.com/echo/post/json");    // Real API server

        final String bodyIn = "{\n" +
                "    \"Id\": 78912,\n" +
                "    \"Customer\": \"Jason Sweet\",\n" +
                "    \"Quantity\": 1,\n" +
                "    \"Price\": 18.00\n" +
                "}";
        final HttpEntity requestEntity = EntityBuilder.create()
                                                      .setText(bodyIn)
                                                      .build();
        httpPost.setEntity(requestEntity);

        httpPost.setHeader(ACCEPT, APPLICATION_JSON.getMimeType());
        httpPost.setHeader(AUTHORIZATION, "Bearer abc");
        httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType());

        process(httpPost);
    }

    @Test
    public void process_post_gzip() throws IOException {
        final HttpPost httpPost = new HttpPost("https://reqbin.com/echo/post/json");    // Real API server

        final String bodyIn = "{\n" +
                "    \"Id\": 78912,\n" +
                "    \"Customer\": \"Jason Sweet\",\n" +
                "    \"Quantity\": 1,\n" +
                "    \"Price\": 18.00\n" +
                "}";
        final HttpEntity requestEntity = EntityBuilder.create()
                                                      .setText(bodyIn)
                                                      .gzipCompress()
                                                      .build();
        httpPost.setEntity(requestEntity);

        httpPost.setHeader(ACCEPT, APPLICATION_JSON.getMimeType());
        httpPost.setHeader(AUTHORIZATION, "Bearer abc");
        httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType());
        httpPost.setHeader(CONTENT_ENCODING, "gzip");

        process(httpPost);
    }

    protected void process(final HttpRequestBase httpRequest) throws IOException {
        try (final CloseableHttpClient httpclient = buildClient()) {
            final HttpResponse response = httpclient.execute(httpRequest);

            final StatusLine statusLine = response.getStatusLine();
            System.out.println(statusLine);

            final Header[] allHeaders = response.getAllHeaders();
            Arrays.stream(allHeaders).forEach(System.out::println);
            System.out.println();

            final HttpEntity entity = response.getEntity();
            final String body = EntityUtils.toString(entity);
            System.out.println(body);
        }
    }

    protected abstract CloseableHttpClient buildClient();
}
