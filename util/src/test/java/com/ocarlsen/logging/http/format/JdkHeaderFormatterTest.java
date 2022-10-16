package com.ocarlsen.logging.http.format;

import com.sun.net.httpserver.Headers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JdkHeaderFormatterTest {

    @Test
    public void format() {
        final Headers headers = new Headers();
        headers.add("headerName1", "headerValue1.1");
        headers.add("headerName1", "headerValue1.2");
        headers.add("headerName2", "headerValue2.1");

        // TODO: Make the order predictable.
        String formattedHeaders = JdkHeaderFormatter.INSTANCE.format(headers);

        assertThat(formattedHeaders, is("{Headername1:\"headerValue1.1\", \"headerValue1.2\", Headername2:\"headerValue2.1\"}"));
    }
}