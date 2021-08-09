package com.ocarlsen.logging.http.client.apache;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.RequestLine;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class RequestLoggingInterceptorTest {

    @DataPoints
    public static final String[] METHODS = {"GET", "POST", "PUT", "DELETE"};

    private final RequestLoggingInterceptor requestInterceptor = new RequestLoggingInterceptor();

    @Theory
    @Test
    public void process(final String method) throws Exception {

        // Given
        final String uri = "https://www.ocarlsen.com/path?query=search";
        final String body = "hello";

        // Prepare mocks
        final RequestLine requestLine = mock(RequestLine.class);
        when(requestLine.getMethod()).thenReturn(method);
        when(requestLine.getUri()).thenReturn(uri);
        final Header header1 = mock(Header.class);
        final Header header2 = mock(Header.class);
        final Header[] headers = {header1, header2};
        final byte[] bodyBytes = body.getBytes(UTF_8);
        final HttpEntity entity = new ByteArrayEntity(bodyBytes);
        final HttpEntityEnclosingRequest httpRequest = mock(HttpEntityEnclosingRequest.class);
        when(httpRequest.getRequestLine()).thenReturn(requestLine);
        when(httpRequest.getAllHeaders()).thenReturn(headers);
        when(httpRequest.getEntity()).thenReturn(entity);
        final HttpContext httpContext = mock(HttpContext.class);

        // When
        requestInterceptor.process(httpRequest, httpContext);

        // Then
        final Logger mockLogger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        verify(mockLogger).debug("Method  : {}", method);
        verify(mockLogger).debug("URI:    : {}", uri);
        verify(mockLogger).debug("Headers : {}", List.of(header1, header2));
        verify(mockLogger).debug("Body    : [{}]", body);
        verifyNoMoreInteractions(mockLogger);
        reset(mockLogger);

        // Verify mocks
        verify(httpRequest, times(2)).getRequestLine();
        verify(requestLine).getMethod();
        verify(requestLine).getUri();
        verify(httpRequest).getAllHeaders();
        verify(httpRequest).getEntity();
        verifyNoMoreInteractions(requestLine, header1, header2, httpRequest, httpContext);
    }
}