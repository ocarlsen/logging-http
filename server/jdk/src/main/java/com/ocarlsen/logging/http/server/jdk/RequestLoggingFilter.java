package com.ocarlsen.logging.http.server.jdk;

import com.ocarlsen.logging.LogLevel;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsServer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.apache.http.HttpHeaders.CONTENT_ENCODING;

public class RequestLoggingFilter extends Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    private LogLevel logLevel = LogLevel.DEBUG;

    @Override
    public void doFilter(final HttpExchange exchange, final Chain chain) throws IOException {

        InputStream requestBody = exchange.getRequestBody();

        // TODO: Unit test
        // Handle content encoding
        final Headers headers = exchange.getRequestHeaders();
        final List<String> contentEncoding = headers.get(CONTENT_ENCODING);
        if (contentEncoding != null) {
            // TODO: Generalize as needed, e.g. what about "gz"?
            if (contentEncoding.contains("gzip")) {
                requestBody = new GZIPInputStream(requestBody);
            } else {
                throw new UnsupportedOperationException("TODO: Support content-encoding for: " + contentEncoding);
            }
        }

        logLevel.log(LOGGER, "Method  : {}", exchange.getRequestMethod());

        final URI requestURI = buildUri(exchange);
        logLevel.log(LOGGER, "URL     : {}", requestURI);

        // TODO: Figure out how to defer string creation as it's expensive and logger may not use it.
        final String headersFormatted = formatHeaders(headers);
        logLevel.log(LOGGER, "Headers : {}", headersFormatted);

        final String bodyText = IOUtils.toString(requestBody, UTF_8);
        logLevel.log(LOGGER, "Body    : [{}]", bodyText);

        // InputStream is exhausted so restore it here.
        // TODO: restore GZIP
        if (requestBody.markSupported()) {
            requestBody.reset();
        } else {
            requestBody = new ByteArrayInputStream(bodyText.getBytes(UTF_8));
            exchange.setStreams(requestBody, exchange.getResponseBody());
        }

        chain.doFilter(exchange);
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

    private String formatHeaders(final Headers headers) {
        return '{' + headers.entrySet()
                            .stream()
                            .map(e -> e.getKey() + "=" + e.getValue())
                            .collect(joining(", ")) + '}';
    }

    private static URI buildUri(final HttpExchange httpExchange) {
        try {
            final HttpContext httpContext = httpExchange.getHttpContext();
            final String scheme = httpContext.getServer() instanceof HttpsServer ? "https" : "http";
            final URI requestURI = httpExchange.getRequestURI();
            final String path = requestURI.getPath();
            final String queryParams = requestURI.getQuery();
            final InetSocketAddress addr = httpExchange.getLocalAddress();
            final String hostName = addr.getHostName();
            final int port = addr.getPort();
            return new URI(scheme, null, hostName, port, path, queryParams, null);
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
