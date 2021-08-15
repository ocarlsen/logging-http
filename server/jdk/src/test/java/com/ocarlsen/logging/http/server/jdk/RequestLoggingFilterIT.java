package com.ocarlsen.logging.http.server.jdk;

import com.ocarlsen.logging.http.client.apache.GzipContentEnablingEntity;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;


public class RequestLoggingFilterIT {

    public static final String PATH = "/testapp";

    @Test
    public void doFilter_get() throws IOException, URISyntaxException {

        // TODO: Move server logic to BeforeClass, AfterClass
        // Random port
        final HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        final String responseBodyText = "goodbye";
        final StringResponseHandler httpHandler = new StringResponseHandler(responseBodyText);
        final HttpContext context = httpServer.createContext(PATH, httpHandler);

        final InetSocketAddress address = httpServer.getAddress();
        final int port = address.getPort();

        context.getFilters().add(new RequestLoggingFilter());

        httpServer.setExecutor(null); // creates a default executor
        httpServer.start();


        // Given
        final String uri = String.format("http://localhost:%d%s?abc=def", port, PATH);
        final HttpGet httpGet = new HttpGet(uri);
        final Header header = new BasicHeader("X-Test", "testvalue");
        final Header[] headers = {header};
        httpGet.setHeaders(headers);

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {

            // When
            final HttpResponse response = httpclient.execute(httpGet);

            // Then
            assertThat(httpHandler.getRequestBodyText(), is(""));

            final StatusLine statusLine = response.getStatusLine();
            assertThat(statusLine.getStatusCode(), is(200));

            // Make sure response not consumed by filter.
            final HttpEntity entity = response.getEntity();
            final String entityText = EntityUtils.toString(entity);
            assertThat(entityText, is(responseBodyText));

            // TODO: Asserts
            final Header[] responseHeaders = response.getAllHeaders();
        }

        // TODO: Move server logic to BeforeClass, AfterClass
        httpServer.stop(1);

        // Verify
        final Logger mockLogger = LoggerFactory.getLogger(RequestLoggingFilter.class);
        verify(mockLogger).debug("Method  : {}", httpGet.getMethod());
        verify(mockLogger).debug("URL     : {}", new URI(uri));
        verify(mockLogger).debug(eq("Headers : {}"), argThat(containsHeaders(headers)));
        verify(mockLogger).debug("Body    : [{}]", "");
        reset(mockLogger);
    }

    @Test
    public void doFilter_post_gzip() throws IOException, URISyntaxException {

        // Random port
        final HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        final String responseBodyText = "goodbye";
        final StringResponseHandler httpHandler = new StringResponseHandler(responseBodyText);
        final HttpContext context = httpServer.createContext(PATH, httpHandler);

        final InetSocketAddress address = httpServer.getAddress();
        final int port = address.getPort();

        context.getFilters().add(new RequestLoggingFilter());

        httpServer.setExecutor(null); // creates a default executor
        httpServer.start();


        // Given
        final String uri = String.format("http://localhost:%d%s", port, PATH);
        final HttpPost httpPost = new HttpPost(uri);
        final Header header = new BasicHeader("X-Test", "testvalue");
        final Header[] headers = {header};
        httpPost.setHeaders(headers);
        final String requestBodyText = "hello";

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {


            // GZip request
            final HttpEntity requestEntity = EntityBuilder.create()
                                                          .setText(requestBodyText)
                                                          .gzipCompress() // Automatically sets content encoding header
                                                          .build();
            httpPost.setEntity(requestEntity);

            // When
            final HttpResponse response = httpclient.execute(httpPost);

            // Then
            assertThat(httpHandler.getRequestBodyText(), is(requestBodyText));

            final StatusLine statusLine = response.getStatusLine();
            assertThat(statusLine.getStatusCode(), is(200));

            // Make sure request not consumed by filter.
            HttpEntity entityOut = new GzipDecompressingEntity(
                    new GzipContentEnablingEntity(
                            (GzipCompressingEntity) requestEntity));
            final String actualRequestBody = EntityUtils.toString(entityOut);
            assertThat(actualRequestBody, is(requestBodyText));

            // Make sure response not consumed by filter.
            final HttpEntity responseEntity = response.getEntity();
            final String actualResponseText = EntityUtils.toString(responseEntity);
            assertThat(actualResponseText, is(responseBodyText));

            // TODO: Asserts
            final Header[] responseHeaders = response.getAllHeaders();
        }

        httpServer.stop(1);

        // Verify
        final Logger mockLogger = LoggerFactory.getLogger(RequestLoggingFilter.class);
        verify(mockLogger).debug("Method  : {}", httpPost.getMethod());
        verify(mockLogger).debug("URL     : {}", new URI(uri));
        verify(mockLogger).debug(eq("Headers : {}"), argThat(containsHeaders(headers)));
        verify(mockLogger).debug("Body    : [{}]", requestBodyText);
        reset(mockLogger);
    }

    // TODO: doFilter_post_gzip

    // TODO: Factor out, this is duplicated.
    private ArgumentMatcher<Headers> containsHeaders(final Header[] headers) {
        return argument -> {

            for (final Header header : headers) {
                final String headerName = header.getName();
                if (argument.containsKey(headerName)) {
                    final List<String> allValues = argument.get(headerName);
                    final String headerValue = header.getValue();
                    if (!allValues.contains(headerValue)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        };
    }

    private static class StringResponseHandler implements HttpHandler {

        private final String responseBodyText;

        private String requestBodyText;

        private StringResponseHandler(final String responseBodyText) {
            this.responseBodyText = responseBodyText;
        }

        @Override
        public void handle(final HttpExchange httpExchange) throws IOException {
            final InputStream requestBody = httpExchange.getRequestBody();
            this.requestBodyText = IOUtils.toString(requestBody, UTF_8);

            httpExchange.sendResponseHeaders(200, responseBodyText.length());

            final OutputStream responseBody = httpExchange.getResponseBody();
            responseBody.write(responseBodyText.getBytes(UTF_8));
            responseBody.close();
        }

        public String getRequestBodyText() {
            return requestBodyText;
        }
    }

}