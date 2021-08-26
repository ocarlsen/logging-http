package com.ocarlsen.logging.http;

import com.sun.net.httpserver.Headers;
import org.apache.http.Header;
import org.mockito.ArgumentMatcher;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;

public class HeaderArgumentMatchers {

    public static ArgumentMatcher<String> containsHeaderIgnoringCase(final String headerName, final String headerValue) {
        return argument -> {

            // Convert to string and search ignoring case.
            final String headerPair = format("%s=[%s]", headerName, headerValue);
            return containsStringIgnoringCase(headerPair).matches(argument);
        };
    }

    public static ArgumentMatcher<String> containsHttpHeadersIgnoringCase(final HttpHeaders httpHeaders) {
        return argument -> {

            // Convert Map.Entry to string and search ignoring case.
            for (final Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
                final String headerPair = entry.toString();
                final boolean matches = containsStringIgnoringCase(headerPair).matches(argument);
                if (!matches) {
                    return false;
                }
            }
            return true;
        };
    }

    public static ArgumentMatcher<String> containsHeadersIgnoringCase(final Header... headers) {
        return argument -> {

            // Convert to string and search ignoring case.
            for (final Header header : headers) {
                final String headerName = header.getName();
                final String headerValue = header.getValue();
                final String headerPair = String.format("%s=[%s]", headerName, headerValue);
                final boolean matches = containsStringIgnoringCase(headerPair).matches(argument);
                if (!matches) {
                    return false;
                }
            }
            return true;
        };
    }

    public static ArgumentMatcher<String> containsHeadersIgnoringCase(final Headers headers) {
        return argument -> {

            // Convert Map.Entry to string and search ignoring case.
            for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
                final String headerPair = entry.toString();
                final boolean matches = containsStringIgnoringCase(headerPair).matches(argument);
                if (!matches) {
                    return false;
                }
            }
            return true;
        };
    }
}
