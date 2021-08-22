package com.ocarlsen.logging.http.client.spring;

import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class ResponseLoggingInterceptorTest {

    @Theory
    @Test
    public void intercept(final HttpStatus statusCode) throws IOException {

        // Given
        final String requestBodyText = "hello";
        final byte[] requestBody = requestBodyText.getBytes(UTF_8);
        final ResponseLoggingInterceptor interceptor = new ResponseLoggingInterceptor();

        // Prepare mocks.
        final HttpRequest request = mock(HttpRequest.class);
        final ClientHttpResponse response = mock(ClientHttpResponse.class);
        final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(request, requestBody)).thenReturn(response);
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-Test", "testvalue");
        headers.addAll("Accept", List.of("application/json", "text/plain"));
        when(response.getStatusCode()).thenReturn(statusCode);
        when(response.getHeaders()).thenReturn(headers);
        final String responseBodyText = "goodbye";
        final InputStream responseBody = new ByteArrayInputStream(responseBodyText.getBytes(UTF_8));
        when(response.getBody()).thenReturn(responseBody);

        // When
        final ClientHttpResponse actualResponse = interceptor.intercept(request, requestBody, execution);

        // Then
        assertThat(actualResponse, is(sameInstance(response)));
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);
        verify(logger).debug("Status  : {}", statusCode.value());
        verify(logger).debug(eq("Headers : {}"), argThat(containsHeaders(headers)));
        verify(logger).debug("Body    : [{}]", responseBodyText);
        reset(logger);

        // Verify mocks.
        verify(response).getStatusCode();
        verify(response).getHeaders();
        verify(response).getBody();
        verify(execution).execute(request, requestBody);
        verifyNoMoreInteractions(request, execution, response);
    }

    @Theory
    @Test
    public void intercept_exception() throws IOException {

        // Given
        final String requestBodyText = "hello";
        final byte[] requestBody = requestBodyText.getBytes(UTF_8);
        final ResponseLoggingInterceptor interceptor = new ResponseLoggingInterceptor();

        // Prepare mocks.
        final HttpRequest request = mock(HttpRequest.class);
        final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        final IOException ioException = new IOException();
        when(execution.execute(request, requestBody)).thenThrow(ioException);

        // When
        try {
            interceptor.intercept(request, requestBody, execution);
        } catch (final IOException e) {
            assertThat(e, is(sameInstance(ioException)));
        }

        // Then
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);
        verifyNoMoreInteractions(logger);
        reset(logger);

        // Verify mocks.
        verify(execution).execute(request, requestBody);
        verifyNoMoreInteractions(request, execution);
    }

    // TODO: Factor out, this is duplicated.
    private ArgumentMatcher<String> containsHeaders(final HttpHeaders headers) {
        return argument -> {

            // Convert Map.Entry to string and search ignoring case.
            for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
                final String headerPair = entry.toString();
                final boolean matches = containsStringIgnoringCase(headerPair).matches(argument);
                if (!matches) {
                    return false;
                }
            }
            return true;
        };
    }
}