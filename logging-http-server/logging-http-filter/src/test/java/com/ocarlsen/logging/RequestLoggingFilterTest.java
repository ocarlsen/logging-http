package com.ocarlsen.logging;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static com.ocarlsen.logging.LogLevel.DEBUG;
import static com.ocarlsen.logging.LogLevel.INFO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

public class RequestLoggingFilterTest {

    private final RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter();
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final ServletResponse response = mock(ServletResponse.class);
    private final HttpServlet servlet = mock(HttpServlet.class);
    private final FilterChain chain = new MockFilterChain(servlet, requestLoggingFilter);  // Very useful!

    @After
    public void verifyNoMoreAndReset() {
        final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
        verifyNoMoreInteractions(request, response, servlet, logger);
        reset(request, response, servlet, logger);
    }

    @SuppressWarnings("UnnecessaryToStringCall")
    @Test
    public void doFilter() throws ServletException, IOException {

        // Default level is DEBUG.
        assertThat(requestLoggingFilter.getLogLevel(), CoreMatchers.is(DEBUG));

        // Given
        final String method = "POST";
        final StringBuffer requestUrl = new StringBuffer("http://localhost:8000/a/b/c");
        final String queryString = "d=e";
        final String requestBody = "Hello!";
        final ServletInputStream inputStream = new ByteArrayServletInputStream(requestBody, StandardCharsets.UTF_8);

        // Prepare mocks.
        when(request.getInputStream()).thenReturn(inputStream);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURL()).thenReturn(requestUrl);
        when(request.getQueryString()).thenReturn(queryString);
        doNothing().when(servlet).service(isA(ByteArrayHttpServletRequest.class), eq(response));

        final String headerName = "accept";
        final List<String> headerNames = List.of(headerName);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(headerNames));

        final List<String> headerValues = List.of(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE);
        when(request.getHeaders(headerName)).thenReturn(Collections.enumeration(headerValues));

        // When
        chain.doFilter(request, response);

        // Then
        final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
        verify(logger).debug("Method  : {}", method);
        verify(logger).debug("URL     : {}", requestUrl.toString());
        verify(logger).debug("Headers : {}", '[' + headerName + '=' + headerValues + ']');
        verify(logger).debug("Body    : [{}]", requestBody);

        // Verify mocks
        verify(servlet).service(isA(ByteArrayHttpServletRequest.class), eq(response));
        verify(request).getInputStream();
        verify(request).getMethod();
        verify(request).getRequestURL();
        verify(request).getQueryString();
        verify(request).getHeaderNames();
        verify(request).getHeaders(headerName);
    }

    @SuppressWarnings("UnnecessaryToStringCall")
    @Test
    public void doFilter_info() throws ServletException, IOException {

        requestLoggingFilter.setLogLevel(INFO);
        assertThat(requestLoggingFilter.getLogLevel(), CoreMatchers.is(INFO));

        // Given
        final String method = "POST";
        final StringBuffer requestUrl = new StringBuffer("http://localhost:8000/a/b/c");
        final String queryString = "d=e";
        final String requestBody = "Hello!";
        final ServletInputStream inputStream = new ByteArrayServletInputStream(requestBody, StandardCharsets.UTF_8);

        // Prepare mocks.
        when(request.getInputStream()).thenReturn(inputStream);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURL()).thenReturn(requestUrl);
        when(request.getQueryString()).thenReturn(queryString);
        doNothing().when(servlet).service(isA(ByteArrayHttpServletRequest.class), eq(response));

        final String headerName = "accept";
        final List<String> headerNames = List.of(headerName);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(headerNames));

        final List<String> headerValues = List.of(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE);
        when(request.getHeaders(headerName)).thenReturn(Collections.enumeration(headerValues));

        // When
        chain.doFilter(request, response);

        // Then
        final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
        verify(logger).info("Method  : {}", method);
        verify(logger).info("URL     : {}", requestUrl.toString());
        verify(logger).info("Headers : {}", '[' + headerName + '=' + headerValues + ']');
        verify(logger).info("Body    : [{}]", requestBody);

        // Verify mocks
        verify(servlet).service(isA(ByteArrayHttpServletRequest.class), eq(response));
        verify(request).getInputStream();
        verify(request).getMethod();
        verify(request).getRequestURL();
        verify(request).getQueryString();
        verify(request).getHeaderNames();
        verify(request).getHeaders(headerName);
    }

    @Test
    public void init() {
        final FilterConfig filterConfig = mock(FilterConfig.class);

        requestLoggingFilter.init(filterConfig);

        final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
        verify(logger).debug("Starting {} (logLevel={})", "RequestLoggingFilter", DEBUG);

        verifyNoInteractions(filterConfig);
    }

    @Test
    public void destroy() {
        requestLoggingFilter.destroy();

        final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
        verify(logger).debug("Stopping {} (logLevel={})", "RequestLoggingFilter", DEBUG);
    }
}
