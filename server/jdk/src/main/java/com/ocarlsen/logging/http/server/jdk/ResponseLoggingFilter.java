package com.ocarlsen.logging.http.server.jdk;

import com.ocarlsen.logging.LogLevel;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class ResponseLoggingFilter extends Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    private LogLevel logLevel = LogLevel.DEBUG;

    @Override
    public void doFilter(final HttpExchange exchange, final Chain chain) throws IOException {

        // Wrap
        final OutputStream outputStream = exchange.getResponseBody();
        final CachingOutputStream cachingOutputStream = new CachingOutputStream(outputStream);

        exchange.setStreams(exchange.getRequestBody(), cachingOutputStream);

        chain.doFilter(exchange);

        final int responseCode = exchange.getResponseCode();
        logLevel.log(LOGGER, "Status  : {}", responseCode);

        final String headersFormatted = formatHeaders(exchange.getResponseHeaders());
        logLevel.log(LOGGER, "Headers : {}", headersFormatted);

        final String body = new String(cachingOutputStream.getBytes(), UTF_8);
        logLevel.log(LOGGER, "Body    : [{}]", body);
    }

    @Override
    public String description() {
        return this.getClass().getSimpleName();
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    // TODO: Factor out, this is duplicated
    private String formatHeaders(final Headers headers) {
        return '{' + headers.entrySet()
                            .stream()
                            .map(e -> e.getKey() + "=" + e.getValue())
                            .collect(joining(", ")) + '}';
    }
}