package com.ocarlsen.logging.http.server.jdk;

import com.ocarlsen.logging.LogLevel;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.lang.invoke.MethodHandles.lookup;

public class RequestLoggingFilter extends Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    private LogLevel logLevel = LogLevel.DEBUG;

    @Override
    public void doFilter(final HttpExchange exchange, final Chain chain) throws IOException {
        logLevel.log(LOGGER, "Method  : {}", exchange.getRequestMethod());
        logLevel.log(LOGGER, "URL     : {}", exchange.getRequestURI());
        logLevel.log(LOGGER, "Headers : {}", exchange.getRequestHeaders());
        logLevel.log(LOGGER, "Body    : [{}]", exchange.getRequestBody());

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
}
