package com.ocarlsen.logging.http;

import com.sun.net.httpserver.Headers;
import org.apache.http.Header;
import org.mockito.ArgumentMatcher;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

public class HeaderArgumentMatchers {

    /**
     * Matches the format of {@code Map.Entry<String, List<String>>}.
     * This is convenient because it is default of {@link Headers}.
     */
    @SuppressWarnings("unused")
    private static final String HEADER_PATTERN = "%s=[%s]";

    public static ArgumentMatcher<String> matchesHeader(final String headerName, final String headerValue) {
        return argument -> matchesNameValuePair(argument, headerName, List.of(headerValue));
    }

    public static ArgumentMatcher<String> matchesHttpHeaders(final HttpHeaders httpHeaders) {
        return argument -> {

            // Convert Map.Entry to name/value pair and match.
            for (final Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
                final String headerName = entry.getKey();
                final List<String> headerValues = entry.getValue();
                final boolean matches = matchesNameValuePair(argument, headerName, headerValues);
                if (!matches) {
                    return false;
                }
            }
            return true;
        };
    }

    public static ArgumentMatcher<String> matchesHeaderArray(final Header... headers) {
        return argument -> {

            // Convert to name/value pair and match.
            for (final Header header : headers) {
                final String headerName = header.getName();
                final String headerValue = header.getValue();
                final boolean matches = matchesNameValuePair(argument, headerName, List.of(headerValue));
                if (!matches) {
                    return false;
                }
            }
            return true;
        };
    }

    public static ArgumentMatcher<String> matchesHeaders(final Headers headers) {
        return argument -> {

            // Convert Map.Entry to name/value pair and match.
            for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
                final String headerName = entry.getKey();
                final List<String> headerValues = entry.getValue();
                final boolean matches = matchesNameValuePair(argument, headerName, headerValues);
                if (!matches) {
                    return false;
                }
            }
            return true;
        };
    }

    private static boolean matchesNameValuePair(final String argument, final String headerName, final List<String> headerValues) {

        // Check for case-sensitive header value match first.
        final String headerValueExpression = '=' + headerValues.toString();
        final int headerValueIndex = argument.indexOf(headerValueExpression);
        if (headerValueIndex < 0) {
            return false;
        }

        // Now check case-insensitive header name match.
        final int headerNameLength = headerName.length();
        return argument.regionMatches(true, headerValueIndex - headerNameLength,
                headerName, 0, headerNameLength);
    }
}
