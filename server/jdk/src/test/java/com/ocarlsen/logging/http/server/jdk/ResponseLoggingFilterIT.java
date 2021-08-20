package com.ocarlsen.logging.http.server.jdk;

import com.sun.net.httpserver.Filter;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class ResponseLoggingFilterIT {

    public static final String PATH = "/testapp";


    private static StringResponseHandler httpHandler;
    private static HttpServer httpServer;
    private static HttpContext context;
    private static int port;

    @BeforeClass
    public static void startServer() throws IOException {
        httpHandler = new StringResponseHandler();

        // Random port
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        context = httpServer.createContext(PATH, httpHandler);

        final InetSocketAddress address = httpServer.getAddress();
        port = address.getPort();

        httpServer.setExecutor(null); // creates a default executor
        httpServer.start();
    }

    @AfterClass
    public static void stopServer() {
        httpServer.removeContext(PATH);
        httpServer.stop(1);
    }

    @After
    public void removeAllFilters() {
        context.getFilters().retainAll(Collections.<Filter>emptyList());
    }

    @Test
    public void doFilter_get() throws IOException, InterruptedException {

        context.getFilters().add(new ResponseLoggingFilter());

        // Given
        final String uri = String.format("http://localhost:%d%s?abc=def", port, PATH);
        final HttpGet httpGet = new HttpGet(uri);
        final Header header1 = new BasicHeader("X-Test", "testvalue");
        final Header header2 = new BasicHeader("Accept", "application/json, text/plain");
        final Header[] headers = {header1, header2};
        httpGet.setHeaders(headers);
        final String responseBodyText = "goodbye";
        httpHandler.setResponseBodyText(responseBodyText);

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {

            // When
            final HttpResponse response = httpclient.execute(httpGet);

            // Client may return before server filter chain is done executing.
            Thread.sleep(1000);

            // Confidence checks
            assertThat(httpHandler.getRequestBodyText(), is(""));

            final Headers actualRequestHeaders = httpHandler.getRequestHeaders();
            for (final Header header : headers) {
                final String headerName = header.getName();
                assertThat(actualRequestHeaders.containsKey(headerName), is(true));
                final List<String> actualHeaderValues = actualRequestHeaders.get(headerName);
                final String headerValue = header.getValue();
                assertThat(actualHeaderValues.get(0), is(headerValue));
            }

            final StatusLine statusLine = response.getStatusLine();
            assertThat(statusLine.getStatusCode(), is(200));

            final List<Header> actualResponseHeaders = asList(response.getAllHeaders());
            assertThat(actualResponseHeaders, hasItem(aHeaderMatching("content-length", String.valueOf(responseBodyText.length()))));

            // Make sure response not consumed by filter.
            final HttpEntity entity = response.getEntity();
            final String entityText = EntityUtils.toString(entity);
            assertThat(entityText, is(responseBodyText));
        }

        // Then
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verify(logger).debug("Status  : {}", 200);
        verify(logger).debug(eq("Headers : {}"), argThat(containsHeader("content-length", String.valueOf(responseBodyText.length()))));
        verify(logger).debug("Body    : [{}]", responseBodyText);
        reset(logger);
    }

    @Test
    public void doFilter_post() throws IOException, InterruptedException {

        context.getFilters().add(new ResponseLoggingFilter());

        // Given
        final String uri = String.format("http://localhost:%d%s?abc=def", port, PATH);
        final HttpPost httpPost = new HttpPost(uri);
        final Header header1 = new BasicHeader("X-Test", "testvalue");
        final Header header2 = new BasicHeader("Accept", "application/json, text/plain");
        final Header[] headers = {header1, header2};
        httpPost.setHeaders(headers);
        final String requestBodyText = "hello";
        final HttpEntity requestEntity = EntityBuilder.create()
                                                      .setText(requestBodyText)
                                                      .build();
        httpPost.setEntity(requestEntity);
        final String responseBodyText = "goodbye";
        httpHandler.setResponseBodyText(responseBodyText);

        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // When
            final HttpResponse response = httpClient.execute(httpPost);

            // Client may return before server filter chain is done executing.
            Thread.sleep(1000);

            // Confidence checks
            assertThat(httpHandler.getRequestBodyText(), is(requestBodyText));

            final Headers actualRequestHeaders = httpHandler.getRequestHeaders();
            for (final Header header : headers) {
                final String headerName = header.getName();
                assertThat(actualRequestHeaders.containsKey(headerName), is(true));
                final List<String> actualHeaderValues = actualRequestHeaders.get(headerName);
                final String headerValue = header.getValue();
                assertThat(actualHeaderValues.get(0), is(headerValue));
            }

            // Make sure request not consumed by filter.
            final String actualRequestBody = EntityUtils.toString(requestEntity);
            assertThat(actualRequestBody, is(requestBodyText));

            final StatusLine statusLine = response.getStatusLine();
            assertThat(statusLine.getStatusCode(), is(200));

            final List<Header> actualResponseHeaders = asList(response.getAllHeaders());
            assertThat(actualResponseHeaders, hasItem(aHeaderMatching("content-length", String.valueOf(responseBodyText.length()))));

            // Make sure response not consumed by filter.
            final HttpEntity responseEntity = response.getEntity();
            final String actualResponseText = EntityUtils.toString(responseEntity);
            assertThat(actualResponseText, is(responseBodyText));
        }

        // Then
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verify(logger).debug("Status  : {}", 200);
        verify(logger).debug(eq("Headers : {}"), argThat(containsHeader("content-length", String.valueOf(responseBodyText.length()))));
        verify(logger).debug("Body    : [{}]", responseBodyText);
        reset(logger);
    }

    @SuppressWarnings("SameParameterValue")
    private static Matcher<Header> aHeaderMatching(final String headerName, final String headerValue) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(final Object actual) {
                if (actual instanceof Header) {
                    final Header header = (Header) actual;
                    return (header.getName().equalsIgnoreCase(headerName) &&
                            header.getValue().equals(headerValue));
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("a header with name ")
                           .appendValue(headerName)
                           .appendText(" and value ")
                           .appendValue(headerValue);

            }
        };
    }

    // TODO: Factor out, this is duplicated.
    @SuppressWarnings("SameParameterValue")
    private ArgumentMatcher<String> containsHeader(final String headerName, final String headerValue) {
        return argument -> {

            // TODO: Figure out why runs twice
            // Convert to string and search ignoring case.
            final String headerPair = String.format("%s=[%s]", headerName, headerValue);
            return containsStringIgnoringCase(headerPair).matches(argument);
        };
    }

    private static class StringResponseHandler implements HttpHandler {

        private Headers requestHeaders;
        private String requestBodyText;
        // TODO: responseHeaders
        private String responseBodyText;

        @Override
        public void handle(final HttpExchange httpExchange) throws IOException {
            this.requestHeaders = httpExchange.getRequestHeaders();
            final InputStream requestBody = httpExchange.getRequestBody();
            this.requestBodyText = IOUtils.toString(requestBody, UTF_8);

            // Will set content-length header.
            httpExchange.sendResponseHeaders(200, responseBodyText.length());

            final OutputStream responseBody = httpExchange.getResponseBody();
            responseBody.write(responseBodyText.getBytes(UTF_8));
        }

        public Headers getRequestHeaders() {
            return requestHeaders;
        }

        public String getRequestBodyText() {
            return requestBodyText;
        }

        public void setResponseBodyText(final String responseBodyText) {
            this.responseBodyText = responseBodyText;
        }
    }

}