package com.ocarlsen.logging.http;

import com.sun.net.httpserver.Headers;
import org.apache.http.Header;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;

public class HeaderMatchers {

    // TODO: Add/start matcher class for Apache, Spring, JDK headers.
    public static Matcher<Header> containsHeader(final String headerName, final String headerValue) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(final Object actual) {
                if (actual instanceof Header) {
                    final Header header = (Header) actual;
                    return (header.getName().equalsIgnoreCase(headerName) &&
                            header.getValue().equals(headerValue));
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("a header with name ")
                           .appendValue(headerName)
                           .appendText(" and value ")
                           .appendValue(headerValue);
            }
        };
    }

    public static Matcher<HttpHeaders> containsHttpHeaders(final HttpHeaders httpHeaders) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(final Object actual) {
                if (actual instanceof HttpHeaders) {
                    final HttpHeaders actualHttpHeaders = (HttpHeaders) actual;

                    for (final Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
                        final String headerName = entry.getKey();
                        if (!actualHttpHeaders.containsKey(headerName)) {
                            return false;
                        }

                        final List<String> headerValues = httpHeaders.get(headerName);
                        if (headerValues == null) {
                            return false;
                        }

                        final List<String> actualHeaderValues = actualHttpHeaders.get(headerName);
                        return contains(headerValues.toArray()).matches(actualHeaderValues);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("HttpHeaders to match ")
                           .appendText(httpHeaders.toString());
            }
        };
    }

    public static Matcher<Headers> containsHeaders(final Header... headers) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(final Object actual) {
                if (actual instanceof Headers) {
                    final Headers actualHeaders = (Headers) actual;

                    for (final Header header : headers) {
                        final String headerName = header.getName();
                        if (!actualHeaders.containsKey(headerName)) {
                            return false;
                        }

                        final List<String> actualHeaderValues = actualHeaders.get(headerName);
                        if ((actualHeaderValues == null) || (actualHeaderValues.isEmpty())) {
                            return false;
                        }

                        final String headerValue = header.getValue();
                        return (actualHeaderValues.get(0).equals(headerValue));
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                final Map<String, String> collect = Arrays.stream(headers).collect(
                        LinkedHashMap::new,
                        (map, header) -> map.put(header.getName(), List.of(header.getValue()).toString()),
                        Map::putAll);
                description.appendText("Headers to match ")
                           .appendText(collect.toString());
            }
        };
    }
}
