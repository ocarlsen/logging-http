package com.ocarlsen.logging.http;

import com.sun.net.httpserver.Headers;
import org.apache.http.Header;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class HeaderMatchers {

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
                        MatcherAssert.assertThat(actualHttpHeaders.containsKey(headerName), is(true));
                        final List<String> headerValues = httpHeaders.get(headerName);
                        MatcherAssert.assertThat(headerValues, is(notNullValue()));
                        final List<String> actualHeaderValues = actualHttpHeaders.get(headerName);
                        MatcherAssert.assertThat(actualHeaderValues, contains(headerValues.toArray()));
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("HttpHeaders to match ")
                           .appendValue(httpHeaders);

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
                        assertThat(actualHeaders.containsKey(headerName), is(true));
                        final List<String> actualHeaderValues = actualHeaders.get(headerName);
                        final String headerValue = header.getValue();
                        assertThat(actualHeaderValues.get(0), is(headerValue));
                    }

                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Headers to match ")
                           .appendValue(Arrays.asList(headers));

            }
        };
    }
}
