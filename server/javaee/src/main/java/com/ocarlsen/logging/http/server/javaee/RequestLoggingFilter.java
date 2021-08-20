package com.ocarlsen.logging.http.server.javaee;

import com.ocarlsen.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;

public class RequestLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    private LogLevel logLevel = LogLevel.DEBUG;

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        // TODO: Check if markSupported before wrapping. See RequestLoggingFilter from 'jdk' package.
        request = new ByteArrayHttpServletRequest(request);

        final StringBuffer url = request.getRequestURL();
        final String query = request.getQueryString();
        if ((query != null) && (!query.isBlank())) {
            url.append('?').append(query);
        }

        logLevel.log(LOGGER, "Method  : {}", request.getMethod());
        logLevel.log(LOGGER, "URL     : {}", url.toString());

        final String headerFormatted = formatHeaders(request);
        logLevel.log(LOGGER, "Headers : {}", headerFormatted);

        final String body = new String(((ByteArrayHttpServletRequest) request).getCachedContent());
        logLevel.log(LOGGER, "Body    : [{}]", body);

        chain.doFilter(request, servletResponse);
    }

    @Override
    public void init(final FilterConfig config) {
        logLevel.log(LOGGER, "Starting {} (logLevel={})", getClass().getSimpleName(), logLevel);
    }

    @Override
    public void destroy() {
        logLevel.log(LOGGER, "Stopping {} (logLevel={})", getClass().getSimpleName(), logLevel);
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    private String formatHeaders(final HttpServletRequest request) {
        final LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
        final Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            Collections.list(headerNames).forEach(h -> headers.put(h, getHeaderValues(request, h)));
        }
        return headers.toString();
    }

    private List<String> getHeaderValues(final HttpServletRequest request, final String headerName) {
        final Enumeration<String> headerValues = request.getHeaders(headerName);
        return Collections.list(headerValues);
    }

}
