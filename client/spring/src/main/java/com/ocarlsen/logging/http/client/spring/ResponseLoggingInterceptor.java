package com.ocarlsen.logging.http.client.spring;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static java.lang.invoke.MethodHandles.lookup;

public class ResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {

        final ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    /**
     * Requires that the {@link ClientHttpRequestFactory} returns a {@link BufferingClientHttpRequestFactory}.
     * Otherwise the response {@link InputStream} returned by {@link ClientHttpResponse#getBody()} will be consumed before
     * reaching the client.
     */
    private void logResponse(final ClientHttpResponse response) throws IOException {
        LOGGER.debug("Status  : {}", response.getStatusCode().value());
        final String headersFormatted = formatHeaders(response);
        LOGGER.debug("Headers : {}", headersFormatted);

        final String body = IOUtils.toString(response.getBody(), StandardCharsets.UTF_8);
        LOGGER.debug("Body    : [{}]", body);
    }

    private String formatHeaders(final ClientHttpResponse response) {
        return response.getHeaders().toString();
    }
}
