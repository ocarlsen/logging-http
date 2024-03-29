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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;

public class ResponseLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    private LogLevel logLevel = LogLevel.DEBUG;

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) servletResponse;

        response = new CachingHttpServletResponse(response);

        chain.doFilter(servletRequest, response);

        logLevel.log(LOGGER, "Status  : {}", response.getStatus());

        final String headersFormatted = formatHeaders(response);
        logLevel.log(LOGGER, "Headers : {}", headersFormatted);

        final String body = new String(((CachingHttpServletResponse) response).getCachedContent());
        logLevel.log(LOGGER, "Body    : [{}]", body);
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

    private String formatHeaders(final HttpServletResponse response) {
        final LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
        final Collection<String> headerNames = response.getHeaderNames();
        if (headerNames != null) {
            headerNames.forEach(h -> headers.put(h, getHeaderValues(response, h)));
        }
        return headers.toString();
    }

    private List<String> getHeaderValues(final HttpServletResponse response, final String headerName) {
        final Collection<String> headerValues = response.getHeaders(headerName);
        return List.copyOf(headerValues);
    }

}
