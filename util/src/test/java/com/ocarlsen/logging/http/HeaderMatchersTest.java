package com.ocarlsen.logging.http;

import com.sun.net.httpserver.Headers;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static com.ocarlsen.logging.http.HeaderMatchers.containsHeader;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HeaderMatchersTest {

    @Test
    public void containsHeader_matches_true() {

        // Given
        final String headerName = "Content-Type";
        final String headerValue = "application/json";

        // When
        final Matcher<Header> matcher = containsHeader(headerName, headerValue);
        final Header headerToMatch = new BasicHeader("content-type", headerValue);     // Case-insensitive header name match works!
        final boolean match = matcher.matches(headerToMatch);

        // Then
        assertThat(match, is(true));
    }

    @Test
    public void containsHeader_matches_false() {

        // Given
        final String headerName = "Content-Type";
        final String headerValue = "APPLICATION/JSON";

        // When
        final Matcher<Header> matcher = containsHeader(headerName, headerValue);
        final Header headerToMatch = new BasicHeader(headerName, "application/json");     // Case-insensitive header value match does not.
        final boolean match = matcher.matches(headerToMatch);

        // Then
        assertThat(match, is(false));
    }

    @Test
    public void containsHeader_describeTo() {

        // Given
        final String headerName = "Content-Type";
        final String headerValue = "application/json";

        // When
        final Description description = new StringDescription();
        final Matcher<Header> matcher = containsHeader(headerName, headerValue);
        matcher.describeTo(description);

        // Then
        assertThat(description.toString(), is("a header with name \"Content-Type\" and value \"application/json\""));
    }

    @Test
    public void containsHttpHeaders_match_true() {

        // Given
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-TEST", "Test Value");
        httpHeaders.addAll("Accept", List.of("application/json", "text/plain"));

        // When
        final Matcher<HttpHeaders> matcher = HeaderMatchers.containsHttpHeaders(httpHeaders);
        final HttpHeaders headersToMatch = new HttpHeaders();
        headersToMatch.add("x-test", "Test Value");     // Case-insensitive header name match works!
        headersToMatch.addAll("accept", List.of("application/json", "text/plain"));
        final boolean match = matcher.matches(headersToMatch);

        // Then
        assertThat(match, is(true));
    }

    @Test
    public void containsHttpHeaders_match_false() {

        // Given
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Test", "TEST VALUE");
        httpHeaders.addAll("Accept", List.of("application/json", "text/plain"));

        // When
        final Matcher<HttpHeaders> matcher = HeaderMatchers.containsHttpHeaders(httpHeaders);
        final HttpHeaders headersToMatch = new HttpHeaders();
        headersToMatch.add("X-Test", "test value");     // Case-insensitive header value match does not.
        headersToMatch.addAll("accept", List.of("application/json", "text/plain"));
        final boolean match = matcher.matches(headersToMatch);

        // Then
        assertThat(match, is(false));
    }

    @Test
    public void containsHttpHeaders_describeTo() {

        // Given
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Test", "Test Value");
        httpHeaders.addAll("Accept", List.of("application/json", "text/plain"));

        // When
        final Description description = new StringDescription();
        final Matcher<HttpHeaders> matcher = HeaderMatchers.containsHttpHeaders(httpHeaders);
        matcher.describeTo(description);

        // Then
        final String actual = description.toString();
        assertThat(actual, is("HttpHeaders to match [X-Test:\"Test Value\", Accept:\"application/json\", \"text/plain\"]"));
    }

    @Test
    public void containsHeaders_match_true() {

        // Given
        final Header header1 = new BasicHeader("X-TEST", "Test Value");
        final Header header2 = new BasicHeader("Accept", "application/json, text/plain");
        final Header[] headers = new Header[]{header1, header2};

        // When
        final Matcher<Headers> matcher = HeaderMatchers.containsHeaders(headers);
        final Headers headersToMatch = new Headers();
        headersToMatch.add("x-test", "Test Value");     // Case-insensitive header name match works!
        headersToMatch.add("accept", "application/json, text/plain");
        final boolean match = matcher.matches(headersToMatch);

        // Then
        assertThat(match, is(true));
    }

    @Test
    public void containsHeaders_match_false() {

        // Given
        final Header header1 = new BasicHeader("X-Test", "TEST VALUE");
        final Header header2 = new BasicHeader("Accept", "application/json, text/plain");
        final Header[] headers = new Header[]{header1, header2};

        // When
        final Matcher<Headers> matcher = HeaderMatchers.containsHeaders(headers);
        final Headers headersToMatch = new Headers();
        headersToMatch.add("X-Test", "test value");     // Case-insensitive header value match does not.
        headersToMatch.add("accept", "application/json, text/plain");
        final boolean match = matcher.matches(headersToMatch);

        // Then
        assertThat(match, is(false));
    }

    @Test
    public void containsHeaders_describeTo() {

        // Given
        final Header header1 = new BasicHeader("X-Test", "Test Value");
        final Header header2 = new BasicHeader("Accept", "application/json, text/plain");
        final Header[] headers = new Header[]{header1, header2};

        // When
        final Description description = new StringDescription();
        final Matcher<Headers> matcher = HeaderMatchers.containsHeaders(headers);
        matcher.describeTo(description);

        // Then
        assertThat(description.toString(), is("Headers to match {X-Test:\"Test Value\", Accept:\"application/json\", \"text/plain\"}"));
    }
}