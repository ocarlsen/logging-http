package com.ocarlsen.logging.http;

import com.sun.net.httpserver.Headers;
import org.apache.http.Header;
import org.mockito.ArgumentMatcher;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                final List<String> headerValues = parseHeaderValues(headerValue);
                final boolean matches = matchesNameValuePair(argument, headerName, headerValues);
                if (!matches) {
                    return false;
                }
            }
            return true;
        };
    }

    public static String buildHeaderValueExpression(final List<String> headerValues) {
        // Copied from HttpHeaders#formatHeaders.
        return (headerValues.size() == 1 ?
                "\"" + headerValues.get(0) + "\"" :
                headerValues.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
    }

    public static String buildHeaderValueExpression(final Map<String, String> headerMap) {
        final StringBuilder buf = new StringBuilder("{");
        for (Iterator<String> i = headerMap.keySet().iterator(); i.hasNext(); ) {
            final String key = i.next();
            buf.append(key).append(':');
            String value = headerMap.get(key);
            List<String> values = parseHeaderValues(value);
            buf.append(buildHeaderValueExpression(values));

            if (i.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append('}');
        return buf.toString();
    }

    public static <T> String buildHeaderValueExpression2(final Map<String, List<String>> headerMap) {
        final StringBuilder buf = new StringBuilder("{");
        for (Iterator<String> i = headerMap.keySet().iterator(); i.hasNext(); ) {
            final String key = i.next();
            buf.append(key).append(':');
            List<String> values = headerMap.get(key);
            buf.append(buildHeaderValueExpression(values));

            if (i.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append('}');
        return buf.toString();
    }

    public static List<String> parseHeaderValues(final String headerValue) {
        // Parse if contains comma, otherwise just wrap in List.
        if (headerValue.lastIndexOf(',') > 0) {
            String[] headerValues = headerValue.split("\\s*,\\s*");
            return Arrays.stream(headerValues)
                         .collect(Collectors.toList());
        } else {
            return List.of(headerValue);
        }
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
        final String headerValueExpression = ":" + buildHeaderValueExpression(headerValues);
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
