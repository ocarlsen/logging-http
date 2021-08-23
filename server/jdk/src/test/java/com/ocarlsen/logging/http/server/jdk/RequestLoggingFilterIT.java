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
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
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
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
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


public class RequestLoggingFilterIT {

    private static final String PATH = "/request_logging_test";
    private static final String BODY = "body";
    private static final String ID = "id";

    private static EchoHandler echoHandler;
    private static HttpServer httpServer;
    private static HttpContext context;
    private static int port;

    @BeforeClass
    public static void startServer() throws IOException {
        echoHandler = new EchoHandler();

        // Random port
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        context = httpServer.createContext(PATH, echoHandler);

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
    public void doFilter_get() throws IOException {

        context.getFilters().add(new RequestLoggingFilter());

        // Given
        final int requestId = 1234;
        final String uri = format("http://localhost:%d%s?%s=%d", port, PATH, ID, requestId);
        final HttpGet httpGet = new HttpGet(uri);
        final Header header1 = new BasicHeader("X-Test", "testvalue");
        final Header header2 = new BasicHeader("Accept", "application/json, text/plain");
        final Header[] requestHeaders = {header1, header2};
        httpGet.setHeaders(requestHeaders);

        final int expectedResponseStatus = 200;
        final String expectedResponseBody = echoHandler.echo("", requestId);

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {

            // When
            final HttpResponse response = httpclient.execute(httpGet);

            // Then
            final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
            verify(logger).debug("Method  : {}", httpGet.getMethod());
            verify(logger).debug("URL     : {}", uri);
            verify(logger).debug(eq("Headers : {}"), argThat(containsHeaders(requestHeaders)));
            verify(logger).debug("Body    : [{}]", "");
            reset(logger);

            // Confidence checks
            final Headers actualRequestHeaders = echoHandler.getRequestHeaders();
            assertThat(actualRequestHeaders, containsHeadersMatching(requestHeaders));

            final int responseStatus = response.getStatusLine().getStatusCode();
            assertThat(responseStatus, is(expectedResponseStatus));

            final HttpEntity responseEntity = response.getEntity();
            final String responseBody = EntityUtils.toString(responseEntity);
            assertThat(responseBody, is(expectedResponseBody));

            final List<Header> actualResponseHeaders = asList(response.getAllHeaders());
            assertThat(actualResponseHeaders, hasItem(aHeaderMatching("content-length", String.valueOf(responseBody.length()))));
        }
    }

    @Test
    public void doFilter_post() throws IOException {

        context.getFilters().add(new RequestLoggingFilter());

        // Given
        final int requestId = 1234;
        final String uri = format("http://localhost:%d%s?%s=%d", port, PATH, ID, requestId);
        final HttpPost httpPost = new HttpPost(uri);
        final Header header1 = new BasicHeader("X-Test", "testvalue");
        final Header header2 = new BasicHeader("Accept", "application/json, text/plain");
        final Header[] requestHeaders = {header1, header2};
        httpPost.setHeaders(requestHeaders);
        final String requestBodyText = "hello";
        final HttpEntity requestEntity = EntityBuilder.create()
                                                      .setText(requestBodyText)
                                                      .build();
        httpPost.setEntity(requestEntity);

        final int expectedResponseStatus = 200;
        final String expectedResponseBody = echoHandler.echo(requestBodyText, requestId);

        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // When
            final HttpResponse response = httpClient.execute(httpPost);

            // Then
            final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
            verify(logger).debug("Method  : {}", httpPost.getMethod());
            verify(logger).debug("URL     : {}", uri);
            verify(logger).debug(eq("Headers : {}"), argThat(containsHeaders(requestHeaders)));
            verify(logger).debug("Body    : [{}]", requestBodyText);
            reset(logger);

            // Confidence checks
            final Headers actualRequestHeaders = echoHandler.getRequestHeaders();
            assertThat(actualRequestHeaders, containsHeadersMatching(requestHeaders));

            final int responseStatus = response.getStatusLine().getStatusCode();
            assertThat(responseStatus, is(expectedResponseStatus));

            final HttpEntity responseEntity = response.getEntity();
            final String responseBody = EntityUtils.toString(responseEntity);
            assertThat(responseBody, is(expectedResponseBody));

            final List<Header> actualResponseHeaders = asList(response.getAllHeaders());
            assertThat(actualResponseHeaders, hasItem(aHeaderMatching("content-length", String.valueOf(responseBody.length()))));
        }
    }

    @Test
    public void doFilter_post_gzip() throws IOException {

        context.getFilters().add(new RequestLoggingFilter());

        // Given
        final int requestId = 1234;
        final String uri = format("http://localhost:%d%s?%s=%d", port, PATH, ID, requestId);
        final HttpPost httpPost = new HttpPost(uri);
        final Header header1 = new BasicHeader("X-Test", "testvalue");
        final Header header2 = new BasicHeader("Accept", "application/json, text/plain");
        final Header[] requestHeaders = {header1, header2};
        httpPost.setHeaders(requestHeaders);
        final String requestBodyText = "hello";
        final HttpEntity requestEntity = EntityBuilder.create()
                                                      .setText(requestBodyText)
                                                      .gzipCompress() // Automatically sets content encoding header
                                                      .build();
        httpPost.setEntity(requestEntity);

        final int expectedResponseStatus = 200;
        final String expectedResponseBody = echoHandler.echo(requestBodyText, requestId);

        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // When
            final HttpResponse response = httpClient.execute(httpPost);

            // Then
            final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
            verify(logger).debug("Method  : {}", httpPost.getMethod());
            verify(logger).debug("URL     : {}", uri);
            verify(logger).debug(eq("Headers : {}"), argThat(containsHeaders(requestHeaders)));
            verify(logger).debug("Body    : [{}]", requestBodyText);
            reset(logger);

            // Confidence checks
            final Headers actualRequestHeaders = echoHandler.getRequestHeaders();
            assertThat(actualRequestHeaders, containsHeadersMatching(requestHeaders));

            final int responseStatus = response.getStatusLine().getStatusCode();
            assertThat(responseStatus, is(expectedResponseStatus));

            final HttpEntity responseEntity = response.getEntity();
            final String responseBody = EntityUtils.toString(responseEntity);
            assertThat(responseBody, is(expectedResponseBody));

            final List<Header> actualResponseHeaders = asList(response.getAllHeaders());
            assertThat(actualResponseHeaders, hasItem(aHeaderMatching("content-length", String.valueOf(responseBody.length()))));
        }
    }

    // TODO: Factor out, this is duplicated
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

    // TODO: Factor out, this is duplicated
    private static Matcher<Headers> containsHeadersMatching(final Header[] headers) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(final Object actual) {
                if (actual instanceof Headers) {
                    final Headers actualHeaders = (Headers) actual;

                    for (final Header header : headers) {
                        final String headerName = header.getName();
                        assertThat(actualHeaders.containsKey(headerName), is(true));
                        final List<String> actualHeaderValues = actualHeaders.get(headerName);
                        final String headerValue = header.getValue();
                        assertThat(actualHeaderValues.get(0), is(headerValue));
                    }

                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Headers to match ")
                           .appendValue(Arrays.asList(headers));

            }
        };
    }

    // TODO: Factor out, this is duplicated.
    private ArgumentMatcher<String> containsHeaders(final Header[] headers) {
        return argument -> {

            // Convert to string and search ignoring case.
            for (final Header header : headers) {
                final String headerName = header.getName();
                final String headerValue = header.getValue();
                final String headerPair = format("%s=[%s]", headerName, headerValue);
                final boolean matches = containsStringIgnoringCase(headerPair).matches(argument);
                if (!matches) {
                    return false;
                }
            }
            return true;
        };
    }

    private static class EchoHandler implements HttpHandler {

        private Headers requestHeaders;

        @Override
        public void handle(final HttpExchange httpExchange) throws IOException {

            // Keep for confidence check later.
            this.requestHeaders = httpExchange.getRequestHeaders();

            final InputStream requestBody = httpExchange.getRequestBody();
            final String requestBodyText = IOUtils.toString(requestBody, UTF_8);
            final URI uri = httpExchange.getRequestURI();
            final List<NameValuePair> params = URLEncodedUtils.parse(uri, UTF_8);
            final int id = extractId(params);
            final String responseBodyText = echo(requestBodyText, id);

            // Will set content-length header.
            httpExchange.sendResponseHeaders(200, responseBodyText.length());

            final OutputStream responseBody = httpExchange.getResponseBody();
            responseBody.write(responseBodyText.getBytes(UTF_8));
        }

        public String echo(final String requestBodyText, final int id) {
            return format("{\"%s\":\"%s\", \"%s\":%d}", BODY, requestBodyText, ID, id);
        }

        public Headers getRequestHeaders() {
            return requestHeaders;
        }

        private int extractId(final List<NameValuePair> params) {
            final NameValuePair param = params.get(0);  // Should only be one
            if (param.getName().equals(ID)) {
                return Integer.parseInt(param.getValue());
            }
            throw new IllegalStateException("id not found");
        }
    }

}