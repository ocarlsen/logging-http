package com.ocarlsen.logging.http;

import com.sun.net.httpserver.Headers;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static com.ocarlsen.logging.http.HeaderArgumentMatchers.matchesHeader;
import static com.ocarlsen.logging.http.HeaderArgumentMatchers.matchesHeaderArray;
import static com.ocarlsen.logging.http.HeaderArgumentMatchers.matchesHeaders;
import static com.ocarlsen.logging.http.HeaderArgumentMatchers.matchesHttpHeaders;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HeaderArgumentMatchersTest {

    @Test
    public void matchesHeader_matches_true() {

        // Given
        final String headerName = "Content-Type";
        final String headerValue = "application/json";

        // When
        final ArgumentMatcher<String> matcher = matchesHeader(headerName, headerValue);
        final String headerToMatch = "{content-type=[application/json]";     // Case-insensitive header name match works!
        final boolean match = matcher.matches(headerToMatch);

        // Then
        assertThat(match, is(true));
    }

    @Test
    public void matchesHeader_matches_false() {

        // Given
        final String headerName = "Content-Type";
        final String headerValue = "APPLICATION/JSON";

        // When
        final ArgumentMatcher<String> matcher = matchesHeader(headerName, headerValue);
        final String headerToMatch = "{Content-Type=[application/json]";     // Case-insensitive header value match does not.
        final boolean match = matcher.matches(headerToMatch);

        // Then
        assertThat(match, is(false));
    }

    @Test
    public void matchesHttpHeaders_matches_true() {

        // Given
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-TEST", "Test Value");
        httpHeaders.addAll("Accept", List.of("application/json", "text/plain"));

        // When
        final ArgumentMatcher<String> matcher = matchesHttpHeaders(httpHeaders);
        final String headerStringToMatch = "{accept=[application/json, text/plain]," +
                "x-test=[Test Value]}";     // Case-insensitive header name match works!
        final boolean match = matcher.matches(headerStringToMatch);

        // Then
        assertThat(match, is(true));
    }

    @Test
    public void matchesHttpHeaders_matches_false() {

        // Given
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Test", "TEST VALUE");
        httpHeaders.addAll("Accept", List.of("application/json", "text/plain"));

        // When
        final ArgumentMatcher<String> matcher = matchesHttpHeaders(httpHeaders);
        final String headerStringToMatch = "{Accept=[application/json, text/plain]," +
                "X-Test=[test value]}";     // Case-insensitive header value match does not.
        final boolean match = matcher.matches(headerStringToMatch);

        // Then
        assertThat(match, is(false));
    }

    @Test
    public void matchesHeaderArray_matches_true() {

        // Given
        final Header header1 = new BasicHeader("X-TEST", "Test Value");
        final Header header2 = new BasicHeader("Accept", "application/json, text/plain");
        final Header[] headers = new Header[]{header1, header2};

        // When
        final ArgumentMatcher<String> matcher = matchesHeaderArray(headers);
        final String headerStringToMatch = "{accept=[application/json, text/plain]," +
                "x-test=[Test Value]}";     // Case-insensitive header name match works!
        final boolean match = matcher.matches(headerStringToMatch);

        // Then
        assertThat(match, is(true));
    }

    @Test
    public void matchesHeaderArray_matches_false() {

        // Given
        final Header header1 = new BasicHeader("X-TEST", "Test Value");
        final Header header2 = new BasicHeader("Accept", "application/json, text/plain");
        final Header[] headers = new Header[]{header1, header2};

        // When
        final ArgumentMatcher<String> matcher = matchesHeaderArray(headers);
        final String headerStringToMatch = "{Accept=[application/json, text/plain]," +
                "X-Test=[test value]}";     // Case-insensitive header value match does not.
        final boolean match = matcher.matches(headerStringToMatch);

        // Then
        assertThat(match, is(false));
    }

    @Test
    public void matchesHeaders_matches_true() {

        // Given
        final Headers headers = new Headers();
        headers.add("X-TEST", "Test Value");
        headers.put("Accept", List.of("application/json", "text/plain"));

        // When
        final ArgumentMatcher<String> matcher = matchesHeaders(headers);
        final String headerStringToMatch = "{accept=[application/json, text/plain]," +
                "x-test=[Test Value]}";     // Case-insensitive header name match works!
        final boolean match = matcher.matches(headerStringToMatch);

        // Then
        assertThat(match, is(true));
    }

    @Test
    public void matchesHeaders_matches_false() {

        // Given
        final Headers headers = new Headers();
        headers.add("X-Test", "TEST VALUE");
        headers.put("Accept", List.of("application/json", "text/plain"));

        // When
        final ArgumentMatcher<String> matcher = matchesHeaders(headers);
        final String headerStringToMatch = "{Accept=[application/json, text/plain]," +
                "X-Test=[test value]}";     // Case-insensitive header value match does not.
        final boolean match = matcher.matches(headerStringToMatch);

        // Then
        assertThat(match, is(false));
    }
}