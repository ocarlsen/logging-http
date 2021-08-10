package com.ocarlsen.logging.http.client.apache;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RequestLoggingInterceptor implements HttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    @Override
    public void process(final HttpRequest httpRequest, final HttpContext httpContext) throws IOException {
        logRequest(httpRequest);
    }

    private void logRequest(final HttpRequest request) throws IOException {
        LOGGER.debug("Method  : {}", request.getRequestLine().getMethod());
        LOGGER.debug("URL:    : {}", request.getRequestLine().getUri());

        // TODO: Confirm standard header format is List.
        // TODO: Also make sure loggers all take Strings, not Headers objects or whatever.
        LOGGER.debug("Headers : {}", Arrays.asList(request.getAllHeaders()));

        final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
        final String body = EntityUtils.toString(entity, UTF_8);
        LOGGER.debug("Body    : [{}]", body);
    }
}
