package com.ocarlsen.logging.http.server.jdk;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import static com.ocarlsen.logging.LogLevel.DEBUG;
import static com.ocarlsen.logging.LogLevel.INFO;
import static com.ocarlsen.logging.http.HeaderArgumentMatchers.matchesHeaders;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ResponseLoggingFilterTest {

    private final ResponseLoggingFilter responseLoggingFilter = new ResponseLoggingFilter();
    private final HttpExchange httpExchange = mock(HttpExchange.class);
    private final InputStream inputStream = mock(InputStream.class);
    private final Filter.Chain chain = mock(Filter.Chain.class);

    @After
    public void verifyNoMoreAndReset() {
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verifyNoMoreInteractions(httpExchange, inputStream, chain, logger);
        reset(httpExchange, inputStream, chain, logger);
    }

    @Test
    public void description() {
        assertThat(responseLoggingFilter.description(), is("ResponseLoggingFilter"));
    }

    @Test
    public void doFilter_debug() throws IOException {

        // Default level is DEBUG.
        assertThat(responseLoggingFilter.getLogLevel(), CoreMatchers.is(DEBUG));

        // Given
        final String responseBody = "Goodbye!";
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final int responseCode = 200;
        final String headerName = "Accept";
        final List<String> headerValues = List.of("text/plain", "application/json");
        final Headers headers = new Headers();
        headers.put(headerName, headerValues);

        // Prepare mocks.
        when(httpExchange.getResponseBody()).thenReturn(outputStream);
        when(httpExchange.getRequestBody()).thenReturn(inputStream);
        final ArgumentCaptor<CachingOutputStream> argumentCaptor = ArgumentCaptor.forClass(CachingOutputStream.class);
        doNothing().when(httpExchange).setStreams(eq(inputStream), argumentCaptor.capture());
        doAnswer(invocation -> {
            final CachingOutputStream cachingOutputStream = argumentCaptor.getValue();
            try (final OutputStreamWriter writer = new OutputStreamWriter(cachingOutputStream)) {
                writer.write(responseBody);
                writer.flush();
                return null;
            }
        }).when(chain).doFilter(httpExchange);
        when(httpExchange.getResponseCode()).thenReturn(responseCode);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // When
        responseLoggingFilter.doFilter(httpExchange, chain);

        // Then
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verify(logger).debug("Status  : {}", responseCode);
        verify(logger).debug(eq("Headers : {}"), argThat(matchesHeaders(headers)));
        verify(logger).debug("Body    : [{}]", responseBody);

        // Make sure response not consumed by filter.
        final String actualResponseBody = outputStream.toString(UTF_8);
        assertThat(actualResponseBody, is(responseBody));

        // Verify mocks
        verify(httpExchange).getResponseBody();
        verify(httpExchange).getRequestBody();
        verify(httpExchange).getResponseCode();
        verify(httpExchange).getResponseHeaders();
        verify(httpExchange).setStreams(eq(inputStream), isA(CachingOutputStream.class));
        verify(chain).doFilter(httpExchange);
    }

    @Test
    public void doFilter_info() throws IOException {

        responseLoggingFilter.setLogLevel(INFO);
        assertThat(responseLoggingFilter.getLogLevel(), CoreMatchers.is(INFO));

        // Given
        final String responseBody = "Goodbye!";
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final int responseCode = 200;
        final String headerName = "Accept";
        final List<String> headerValues = List.of("text/plain", "application/json");
        final Headers headers = new Headers();
        headers.put(headerName, headerValues);

        // Prepare mocks.
        when(httpExchange.getResponseBody()).thenReturn(outputStream);
        when(httpExchange.getRequestBody()).thenReturn(inputStream);
        final ArgumentCaptor<CachingOutputStream> argumentCaptor = ArgumentCaptor.forClass(CachingOutputStream.class);
        doNothing().when(httpExchange).setStreams(eq(inputStream), argumentCaptor.capture());
        doAnswer(invocation -> {
            final CachingOutputStream cachingOutputStream = argumentCaptor.getValue();
            try (final OutputStreamWriter writer = new OutputStreamWriter(cachingOutputStream)) {
                writer.write(responseBody);
                writer.flush();
                return null;
            }
        }).when(chain).doFilter(httpExchange);
        when(httpExchange.getResponseCode()).thenReturn(responseCode);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);

        // When
        responseLoggingFilter.doFilter(httpExchange, chain);

        // Then
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verify(logger).info("Status  : {}", responseCode);
        verify(logger).info(eq("Headers : {}"), argThat(matchesHeaders(headers)));
        verify(logger).info("Body    : [{}]", responseBody);

        // Make sure response not consumed by filter.
        final String actualResponseBody = outputStream.toString(UTF_8);
        assertThat(actualResponseBody, is(responseBody));

        // Verify mocks
        verify(httpExchange).getResponseBody();
        verify(httpExchange).getRequestBody();
        verify(httpExchange).getResponseCode();
        verify(httpExchange).getResponseHeaders();
        verify(httpExchange).setStreams(eq(inputStream), isA(CachingOutputStream.class));
        verify(chain).doFilter(httpExchange);
    }
}
