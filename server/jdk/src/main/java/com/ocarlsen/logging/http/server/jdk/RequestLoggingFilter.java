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
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.CONTENT_ENCODING;

// TODO: Unit test
public class RequestLoggingFilter extends Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    private LogLevel logLevel = LogLevel.DEBUG;

    @Override
    public void doFilter(final HttpExchange exchange, final Chain chain) throws IOException {

        InputStream requestBody = exchange.getRequestBody();

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
        logLevel.log(LOGGER, "Headers : {}", exchange.getRequestHeaders());
        final String bodyText = IOUtils.toString(requestBody, UTF_8);
        logLevel.log(LOGGER, "Body    : [{}]", bodyText);

        // InputStream is exhausted so restore it here.
        requestBody = new ByteArrayInputStream(bodyText.getBytes(UTF_8));
        exchange.setStreams(requestBody, exchange.getResponseBody());

        chain.doFilter(exchange);
    }

    // TODO: Test
    @Override
    public String description() {
        return this.getClass().getSimpleName();
    }

    // TODO: Test
    public LogLevel getLogLevel() {
        return logLevel;
    }

    // TODO: Test
    public void setLogLevel(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    private static URI buildUri(final HttpExchange httpExchange) {
        try {
            final HttpContext httpContext = httpExchange.getHttpContext();
            final String path = httpContext.getPath();
            final String scheme = httpContext.getServer() instanceof HttpsServer ? "https" : "http";
            final URI requestURI = httpExchange.getRequestURI();
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
