package com.ocarlsen.logging.http.server.jdk;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.ocarlsen.logging.LogLevel.DEBUG;
import static com.ocarlsen.logging.LogLevel.INFO;
import static com.ocarlsen.logging.http.HeaderArgumentMatchers.buildHeaderValueExpression;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class RequestLoggingFilterTest {

    private final RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter();
    private final HttpExchange httpExchange = mock(HttpExchange.class);
    private final HttpContext httpContext = mock(HttpContext.class);
    private final HttpServer httpServer = mock(HttpsServer.class);
    private final InetSocketAddress localAddr = mock(InetSocketAddress.class);
    private final Filter.Chain chain = mock(Filter.Chain.class);

    @After
    public void verifyNoMoreAndReset() {
        final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
        verifyNoMoreInteractions(httpExchange, httpContext, httpServer, localAddr, chain, logger);
        reset(httpExchange, httpContext, httpServer, localAddr, chain, logger);
    }

    @Test
    public void description() {
        assertThat(requestLoggingFilter.description(), is("RequestLoggingFilter"));
    }

    @SuppressWarnings("UnnecessaryToStringCall")
    @Test
    public void doFilter_debug_markSupported() throws IOException, URISyntaxException {

        // Default level is DEBUG.
        assertThat(requestLoggingFilter.getLogLevel(), CoreMatchers.is(DEBUG));

        // Given
        final String headerName = "Accept";
        final List<String> headerValues = List.of("text/plain", "application/json");
        final Headers headers = new Headers();
        headers.put(headerName, headerValues);
        final String requestBody = "Hello!";
        final InputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(UTF_8));
        final String method = "POST";
        final String path = "/a/b/c";
        final String queryString = "d=e";
        final URI uri = new URI(path + '?' + queryString);
        final String hostName = "localhost";
        final int port = 8080;

        // Prepare mocks.
        when(httpExchange.getRequestHeaders()).thenReturn(headers);
        when(httpExchange.getRequestBody()).thenReturn(inputStream);
        when(httpExchange.getRequestMethod()).thenReturn(method);
        when(httpExchange.getHttpContext()).thenReturn(httpContext);
        when(httpContext.getServer()).thenReturn(httpServer);
        when(httpExchange.getRequestURI()).thenReturn(uri);
        when(httpExchange.getLocalAddress()).thenReturn(localAddr);
        when(localAddr.getHostName()).thenReturn(hostName);
        when(localAddr.getPort()).thenReturn(port);
        doNothing().when(chain).doFilter(httpExchange);

        // When
        requestLoggingFilter.doFilter(httpExchange, chain);

        // Then
        final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
        verify(logger).debug("Method  : {}", method);
        verify(logger).debug("URL     : {}", new URI("https", null, hostName, port, path, queryString, null).toString());
        verify(logger).debug("Headers : {}", '{' + headerName + ':' + buildHeaderValueExpression(headerValues) + '}');
        verify(logger).debug("Body    : [{}]", requestBody);

        // Make sure request not consumed by filter.
        final String actualRequestBody = IOUtils.toString(inputStream, UTF_8);
        assertThat(actualRequestBody, is(requestBody));

        // Verify mocks
        verify(httpExchange).getRequestHeaders();
        verify(httpExchange).getRequestBody();
        verify(httpExchange).getRequestMethod();
        verify(httpExchange).getHttpContext();
        verify(httpContext).getServer();
        verify(httpExchange).getRequestURI();
        verify(httpExchange).getLocalAddress();
        verify(localAddr).getHostName();
        verify(localAddr).getPort();
        verify(chain).doFilter(httpExchange);
    }

    @SuppressWarnings("UnnecessaryToStringCall")
    @Test
    public void doFilter_info_markNotSupported() throws IOException, URISyntaxException {

        requestLoggingFilter.setLogLevel(INFO);
        assertThat(requestLoggingFilter.getLogLevel(), CoreMatchers.is(INFO));

        // Given
        final String headerName = "Accept";
        final List<String> headerValues = List.of("text/plain", "application/json");
        final Headers headers = new Headers();
        headers.put(headerName, headerValues);
        final String requestBody = "Hello!";
        final InputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(UTF_8)) {

            @Override
            public boolean markSupported() {
                return false;  // For test case
            }
        };
        final String method = "POST";
        final String path = "/a/b/c";
        final String queryString = "d=e";
        final URI uri = new URI(path + '?' + queryString);
        final String hostName = "localhost";
        final int port = 8080;

        // Prepare mocks.
        when(httpExchange.getRequestHeaders()).thenReturn(headers);
        when(httpExchange.getRequestBody()).thenReturn(inputStream);
        when(httpExchange.getRequestMethod()).thenReturn(method);
        when(httpExchange.getHttpContext()).thenReturn(httpContext);
        when(httpContext.getServer()).thenReturn(httpServer);
        when(httpExchange.getRequestURI()).thenReturn(uri);
        when(httpExchange.getLocalAddress()).thenReturn(localAddr);
        when(localAddr.getHostName()).thenReturn(hostName);
        when(localAddr.getPort()).thenReturn(port);
        doNothing().when(chain).doFilter(httpExchange);
        final OutputStream responseBody = mock(OutputStream.class);
        when(httpExchange.getResponseBody()).thenReturn(responseBody);
        doNothing().when(httpExchange).setStreams(isA(ByteArrayInputStream.class), eq(responseBody));

        // When
        requestLoggingFilter.doFilter(httpExchange, chain);

        // Then
        final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
        verify(logger).info("Method  : {}", method);
        verify(logger).info("URL     : {}", new URI("https", null, hostName, port, path, queryString, null).toString());
        verify(logger).info("Headers : {}", '{' + headerName + ':' + buildHeaderValueExpression(headerValues) + '}');   // TODO: Fix!!!
        verify(logger).info("Body    : [{}]", requestBody);

        // Make sure request not consumed by filter.
        final ArgumentCaptor<ByteArrayInputStream> requestBodyCaptor = ArgumentCaptor.forClass(ByteArrayInputStream.class);
        verify(httpExchange).setStreams(requestBodyCaptor.capture(), eq(responseBody));
        final InputStream newInputStream = requestBodyCaptor.getValue();
        assertThat(newInputStream, is(instanceOf(ByteArrayInputStream.class)));
        final String newRequestBody = IOUtils.toString(newInputStream, UTF_8);
        assertThat(newRequestBody, is(requestBody));

        // Verify mocks
        verify(httpExchange).getRequestHeaders();
        verify(httpExchange).getRequestBody();
        verify(httpExchange).getRequestMethod();
        verify(httpExchange).getHttpContext();
        verify(httpContext).getServer();
        verify(httpExchange).getRequestURI();
        verify(httpExchange).getLocalAddress();
        verify(localAddr).getHostName();
        verify(localAddr).getPort();
        verify(chain).doFilter(httpExchange);
        verify(httpExchange).getResponseBody();
        verifyNoInteractions(responseBody);
    }
}
