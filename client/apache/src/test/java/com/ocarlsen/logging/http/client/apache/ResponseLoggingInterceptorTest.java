package com.ocarlsen.logging.http.client.apache;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class ResponseLoggingInterceptorTest {

    @DataPoints
    public static final int[] STATUS_CODES = {200, 301, 404, 500};

    private final ResponseLoggingInterceptor responseInterceptor = new ResponseLoggingInterceptor();


    @Theory
    @Test
    public void process(final int statusCode) throws Exception {

        // Given
        final String body = "hello";

        // Prepare mocks
        final StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(statusCode);
        final Header header1 = mock(Header.class);
        final Header header2 = mock(Header.class);
        final Header[] headers = {header1, header2};
        final byte[] bodyBytes = body.getBytes(UTF_8);
        final HttpEntity entity = new ByteArrayEntity(bodyBytes);
        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getAllHeaders()).thenReturn(headers);
        when(httpResponse.getEntity()).thenReturn(entity);
        final HttpContext httpContext = mock(HttpContext.class);

        // When
        responseInterceptor.process(httpResponse, httpContext);

        // Then
        final Logger mockLogger = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);
        verify(mockLogger).debug("Status  : {}", statusCode);
        verify(mockLogger).debug("Headers : {}", List.of(header1, header2));
        verify(mockLogger).debug("Body    : [{}]", body);
        verifyNoMoreInteractions(mockLogger);
        reset(mockLogger);

        // Verify mocks
        verify(httpResponse).getStatusLine();
        verify(statusLine).getStatusCode();
        verify(httpResponse).getAllHeaders();
        verify(httpResponse).getEntity();
        verifyNoMoreInteractions(statusLine, header1, header2, httpResponse, httpContext);
    }
}