package com.ocarlsen.logging.http;

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
import java.util.ArrayList;
import java.util.Collection;
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
        logLevel.log(LOGGER, "Headers : {}", getHeaders(response));
        logLevel.log(LOGGER, "Body    : [{}]", new String(((CachingHttpServletResponse) response).getCachedContent()));
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

    private String getHeaders(final HttpServletResponse response) {
        final List<String> headers = new ArrayList<>();
        final Collection<String> headerNames = response.getHeaderNames();
        if (headerNames != null) {
            headerNames.forEach(h -> headers.add(h + "=" + getHeaderValues(response, h)));
        }
        return headers.toString();
    }

    private String getHeaderValues(final HttpServletResponse response, final String headerName) {
        final Collection<String> headerValues = response.getHeaders(headerName);
        return List.copyOf(headerValues).toString();
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean isEnabled(final String property) {
        final String value = System.getProperty(property, "true");
        return Boolean.parseBoolean(value);
    }

}
