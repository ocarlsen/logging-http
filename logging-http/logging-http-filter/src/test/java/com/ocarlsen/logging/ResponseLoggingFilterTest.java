package com.ocarlsen.logging;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.ocarlsen.logging.LogLevel.DEBUG;
import static com.ocarlsen.logging.LogLevel.INFO;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

public class ResponseLoggingFilterTest {

    private final ResponseLoggingFilter responseLoggingFilter = new ResponseLoggingFilter();
    private final ServletRequest request = mock(ServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final HttpServlet servlet = mock(HttpServlet.class);
    private final ServletOutputStream outputStream = mock(ServletOutputStream.class);
    private final FilterChain chain = new MockFilterChain(servlet, responseLoggingFilter);  // Very useful!

    @After
    public void verifyNoMoreAndReset() {
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verifyNoMoreInteractions(request, response, servlet, outputStream, logger);
        reset(request, response, servlet, outputStream, logger);
    }

    @Test
    public void doFilter_getOutputStream() throws ServletException, IOException {

        // Default level is DEBUG.
        assertThat(responseLoggingFilter.getLogLevel(), is(DEBUG));

        // Given
        final String responseBody = "Hello!";
        final int responseStatus = 200;

        // Prepare mocks.
        doAnswer(invocation -> {
            final CachingHttpServletResponse responseArg = invocation.getArgument(1);
            responseArg.getOutputStream().print(responseBody);
            responseArg.flushBuffer();
            return null;
        }).when(servlet).service(eq(request), isA(CachingHttpServletResponse.class));
        when(response.getOutputStream()).thenReturn(outputStream);
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getStatus()).thenReturn(responseStatus);

        final String headerName = "accept";
        final Collection<String> headerNames = List.of(headerName);
        when(response.getHeaderNames()).thenReturn(headerNames);

        final Collection<String> headerValues = List.of(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE);
        when(response.getHeaders(headerName)).thenReturn(headerValues);

        // When
        chain.doFilter(request, response);

        // Then
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verify(logger).debug("Status  : {}", responseStatus);
        verify(logger).debug("Headers : {}", '[' + headerName + '=' + headerValues + ']');
        verify(logger).debug("Body    : [{}]", responseBody);

        // Verify mocks
        verify(servlet).service(eq(request), isA(CachingHttpServletResponse.class));
        verify(response).getOutputStream();
        verify(outputStream, times(responseBody.length())).write(anyInt());
        verify(outputStream).flush();
        verify(response).getStatus();
        verify(response).getHeaderNames();
        verify(response).getHeaders(headerName);
    }

    @Test
    public void doFilter_getOutputStream_info() throws ServletException, IOException {

        responseLoggingFilter.setLogLevel(INFO);
        assertThat(responseLoggingFilter.getLogLevel(), is(INFO));

        // Given
        final String responseBody = "Hello!";
        final int responseStatus = 200;

        // Prepare mocks.
        doAnswer(invocation -> {
            final CachingHttpServletResponse responseArg = invocation.getArgument(1);
            responseArg.getOutputStream().print(responseBody);
            responseArg.flushBuffer();
            return null;
        }).when(servlet).service(eq(request), isA(CachingHttpServletResponse.class));
        when(response.getOutputStream()).thenReturn(outputStream);
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getStatus()).thenReturn(responseStatus);

        final String headerName = "accept";
        final Collection<String> headerNames = List.of(headerName);
        when(response.getHeaderNames()).thenReturn(headerNames);

        final Collection<String> headerValues = List.of(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE);
        when(response.getHeaders(headerName)).thenReturn(headerValues);

        // When
        chain.doFilter(request, response);

        // Then
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verify(logger).info("Status  : {}", responseStatus);
        verify(logger).info("Headers : {}", '[' + headerName + '=' + headerValues + ']');
        verify(logger).info("Body    : [{}]", responseBody);

        // Verify mocks
        verify(servlet).service(eq(request), isA(CachingHttpServletResponse.class));
        verify(response).getOutputStream();
        verify(outputStream, times(responseBody.length())).write(anyInt());
        verify(outputStream).flush();
        verify(response).getStatus();
        verify(response).getHeaderNames();
        verify(response).getHeaders(headerName);
    }

    @Test
    public void doFilter_getWriter() throws ServletException, IOException {

        // Default level is DEBUG.
        assertThat(responseLoggingFilter.getLogLevel(), is(DEBUG));

        // Given
        final String responseBody = "Goodbye!";
        final int responseStatus = 200;

        // Prepare mocks.
        doAnswer(invocation -> {
            final CachingHttpServletResponse responseArg = invocation.getArgument(1);
            responseArg.getWriter().println(responseBody);
            return null;
        }).when(servlet).service(eq(request), isA(CachingHttpServletResponse.class));
        when(response.getOutputStream()).thenReturn(outputStream);
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getStatus()).thenReturn(responseStatus);

        final String headerName = "accept";
        final Collection<String> headerNames = List.of(headerName);
        when(response.getHeaderNames()).thenReturn(headerNames);

        final Collection<String> headerValues = List.of(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE);
        when(response.getHeaders(headerName)).thenReturn(headerValues);

        // When
        chain.doFilter(request, response);

        // Then
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verify(logger).debug("Status  : {}", responseStatus);
        verify(logger).debug("Headers : {}", '[' + headerName + '=' + headerValues + ']');
        verify(logger).debug("Body    : [{}]", responseBody + '\n');

        // Verify mocks
        verify(servlet).service(eq(request), isA(CachingHttpServletResponse.class));
        verify(response).getOutputStream();
        verify(response).getCharacterEncoding();
        verify(outputStream, times(responseBody.length() + 1)).write(anyInt());  // +1 for newline
        verify(outputStream).flush();
        verify(response).getStatus();
        verify(response).getHeaderNames();
        verify(response).getHeaders(headerName);
    }

    @Test
    public void doFilter_noBody() throws ServletException, IOException {

        // Default level is DEBUG.
        assertThat(responseLoggingFilter.getLogLevel(), is(DEBUG));

        // Given
        final int responseStatus = 200;

        // Prepare mocks.
        doNothing().when(servlet).service(eq(request), isA(CachingHttpServletResponse.class));
        when(response.getStatus()).thenReturn(responseStatus);

        final String headerName = "accept";
        final Collection<String> headerNames = List.of(headerName);
        when(response.getHeaderNames()).thenReturn(headerNames);

        final Collection<String> headerValues = List.of(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE);
        when(response.getHeaders(headerName)).thenReturn(headerValues);

        // When
        chain.doFilter(request, response);

        // Then
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verify(logger).debug("Status  : {}", responseStatus);
        verify(logger).debug("Headers : {}", '[' + headerName + '=' + headerValues + ']');
        verify(logger).debug("Body    : [{}]", "");

        // Verify mocks
        verify(servlet).service(eq(request), isA(CachingHttpServletResponse.class));
        verify(response).getStatus();
        verify(response).getHeaderNames();
        verify(response).getHeaders(headerName);
    }

    @Test
    public void init() {
        final FilterConfig filterConfig = mock(FilterConfig.class);

        responseLoggingFilter.init(filterConfig);

        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verify(logger).debug("Starting {} (logLevel={})", "ResponseLoggingFilter", DEBUG);

        verifyNoInteractions(filterConfig);
    }

    @Test
    public void destroy() {
        responseLoggingFilter.destroy();

        final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
        verify(logger).debug("Stopping {} (logLevel={})", "ResponseLoggingFilter", DEBUG);
    }
}
