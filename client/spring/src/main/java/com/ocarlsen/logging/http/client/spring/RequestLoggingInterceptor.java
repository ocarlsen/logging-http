package com.ocarlsen.logging.http.client.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        return execution.execute(request, body);
    }

    @SuppressWarnings({"UnnecessaryToStringCall", "ConstantConditions"})
    private void logRequest(final HttpRequest request, final byte[] body) {
        LOGGER.debug("Method  : {}", request.getMethod().name());
        LOGGER.debug("URL     : {}", request.getURI().toString());
        final String headerFormatted = formatHeaders(request);
        LOGGER.debug("Headers : {}", headerFormatted);
        LOGGER.debug("Body    : [{}]", new String(body, UTF_8));
    }

    private String formatHeaders(final HttpRequest request) {
        return request.getHeaders().toString();
    }
}
