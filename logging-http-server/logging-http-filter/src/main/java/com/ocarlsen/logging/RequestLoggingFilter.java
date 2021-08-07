package com.ocarlsen.logging;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;

public class RequestLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    private LogLevel logLevel = LogLevel.DEBUG;

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        request = new ByteArrayHttpServletRequest(request);

        final StringBuffer url = request.getRequestURL();
        final String query = request.getQueryString();
        if ((query != null) && (!query.isBlank())) {
            url.append('?').append(query);
        }

        logLevel.log(LOGGER, "Method  : {}", request.getMethod());
        logLevel.log(LOGGER, "URL     : {}", url.toString());
        logLevel.log(LOGGER, "Headers : {}", getHeaders(request));
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

    private String getHeaders(final HttpServletRequest request) {
        final List<String> headers = new ArrayList<>();
        final Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            Collections.list(headerNames).forEach(h -> headers.add(h + '=' + getHeaderValues(request, h)));
        }
        return headers.toString();
    }

    private String getHeaderValues(final HttpServletRequest request, final String headerName) {
        final Enumeration<String> headerValues = request.getHeaders(headerName);
        return Collections.list(headerValues).toString();
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean isEnabled(final String property) {
        final String value = System.getProperty(property, "true");
        return Boolean.parseBoolean(value);
    }

}
