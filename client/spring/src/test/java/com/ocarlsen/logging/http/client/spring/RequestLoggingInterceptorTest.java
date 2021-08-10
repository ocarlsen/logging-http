package com.ocarlsen.logging.http.client.spring;

import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class RequestLoggingInterceptorTest {

    @Theory
    @Test
    public void intercept(final HttpMethod method) throws IOException, URISyntaxException {

        // Given
        final URI uri = new URI("http://www.ocarlsen.com/path?abc=def");
        final String bodyText = "hello";
        final byte[] body = bodyText.getBytes(UTF_8);
        final RequestLoggingInterceptor interceptor = new RequestLoggingInterceptor();

        // Prepare mocks.
        final HttpRequest request = mock(HttpRequest.class);
        final HttpHeaders headers = mock(HttpHeaders.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(headers);
        final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(request, body)).thenReturn(response);

        // When
        final ClientHttpResponse actualResponse = interceptor.intercept(request, body, execution);

        // Then
        assertThat(actualResponse, is(sameInstance(response)));
        final Logger mockLogger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        verify(mockLogger).debug("Method  : {}", method);
        verify(mockLogger).debug("URL:    : {}", uri);
        verify(mockLogger).debug("Headers : {}", headers);
        verify(mockLogger).debug("Body    : [{}]", bodyText);
        verifyNoMoreInteractions(mockLogger);
        reset(mockLogger);

        // Verify mocks.
        verify(request).getMethod();
        verify(request).getURI();
        verify(request).getHeaders();
        verify(execution).execute(request, body);
        verifyNoMoreInteractions(request, headers, execution, response);
    }

    @Theory
    @Test
    public void intercept_exception(final HttpMethod method) throws IOException, URISyntaxException {

        // Given
        final URI uri = new URI("http://www.ocarlsen.com/trail?ghi=jkl");
        final String bodyText = "hello";
        final byte[] body = bodyText.getBytes(UTF_8);
        final RequestLoggingInterceptor interceptor = new RequestLoggingInterceptor();

        // Prepare mocks.
        final HttpRequest request = mock(HttpRequest.class);
        final HttpHeaders headers = mock(HttpHeaders.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(headers);
        final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        final IOException ioException = new IOException();
        when(execution.execute(request, body)).thenThrow(ioException);

        // When
        try {
            interceptor.intercept(request, body, execution);
        } catch (final IOException e) {
            assertThat(e, is(sameInstance(ioException)));
        }

        // Then
        final Logger mockLogger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        verify(mockLogger).debug("Method  : {}", method);
        verify(mockLogger).debug("URL:    : {}", uri);
        verify(mockLogger).debug("Headers : {}", headers);
        verify(mockLogger).debug("Body    : [{}]", bodyText);
        reset(mockLogger);

        // Verify mocks.
        verify(request).getMethod();
        verify(request).getURI();
        verify(request).getHeaders();
        verify(execution).execute(request, body);
        verifyNoMoreInteractions(request, headers, execution);
    }
}