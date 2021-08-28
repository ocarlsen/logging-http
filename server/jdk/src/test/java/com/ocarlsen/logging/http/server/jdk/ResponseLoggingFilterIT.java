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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static com.ocarlsen.logging.http.HeaderArgumentMatchers.matchesHeader;
import static com.ocarlsen.logging.http.HeaderMatchers.containsHeader;
import static com.ocarlsen.logging.http.HeaderMatchers.containsHeaders;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class ResponseLoggingFilterIT {

    private static final String PATH = "/response_logging_test";
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
    public void doFilter_get() throws IOException, InterruptedException {

        context.getFilters().add(new ResponseLoggingFilter());

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

            // Client may return before server filter chain is done executing.
            Thread.sleep(1000);

            // Then
            final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
            verify(logger).debug("Status  : {}", expectedResponseStatus);
            verify(logger).debug(eq("Headers : {}"), argThat(matchesHeader(CONTENT_LENGTH,
                    String.valueOf(expectedResponseBody.length()))));
            verify(logger).debug("Body    : [{}]", expectedResponseBody);
            reset(logger);

            // Confidence checks
            final Headers actualRequestHeaders = echoHandler.getRequestHeaders();
            assertThat(actualRequestHeaders, containsHeaders(requestHeaders));

            final int responseStatus = response.getStatusLine().getStatusCode();
            assertThat(responseStatus, is(expectedResponseStatus));

            final HttpEntity responseEntity = response.getEntity();
            final String responseBody = EntityUtils.toString(responseEntity);
            assertThat(responseBody, is(expectedResponseBody));

            final List<Header> actualResponseHeaders = asList(response.getAllHeaders());
            assertThat(actualResponseHeaders, hasItem(containsHeader(CONTENT_LENGTH,
                    String.valueOf(responseBody.length()))));
        }
    }

    @Test
    public void doFilter_post() throws IOException, InterruptedException {

        context.getFilters().add(new ResponseLoggingFilter());

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

            // Client may return before server filter chain is done executing.
            Thread.sleep(1000);

            // Then
            final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
            verify(logger).debug("Status  : {}", expectedResponseStatus);
            verify(logger).debug(eq("Headers : {}"), argThat(matchesHeader(CONTENT_LENGTH,
                    String.valueOf(expectedResponseBody.length()))));
            verify(logger).debug("Body    : [{}]", expectedResponseBody);
            reset(logger);

            // Confidence checks
            final Headers actualRequestHeaders = echoHandler.getRequestHeaders();
            assertThat(actualRequestHeaders, containsHeaders(requestHeaders));

            final int responseStatus = response.getStatusLine().getStatusCode();
            assertThat(responseStatus, is(expectedResponseStatus));

            final HttpEntity responseEntity = response.getEntity();
            final String responseBody = EntityUtils.toString(responseEntity);
            assertThat(responseBody, is(expectedResponseBody));

            final List<Header> actualResponseHeaders = asList(response.getAllHeaders());
            assertThat(actualResponseHeaders, hasItem(containsHeader(CONTENT_LENGTH,
                    String.valueOf(responseBody.length()))));
        }
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