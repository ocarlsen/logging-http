package com.ocarlsen.logging.http;

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

        try {
            logRequest(request, body);
            return execution.execute(request, body);
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    private void logRequest(final HttpRequest request, final byte[] body) {
        LOGGER.debug("Method  : {}", request.getMethod());
        LOGGER.debug("URI:    : {}", request.getURI());
        LOGGER.debug("Headers : {}", request.getHeaders());
        LOGGER.debug("Body    : [{}]", new String(body, UTF_8));
    }
}
