package com.ocarlsen.logging.http.format;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StringValuedMapHeaderFormatterTest {

    @Test
    public void format() {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("headerName1", "headerValue1.1, headerValue1.2");
        //headers.put("headerName2", "headerValue2.1");

        String formattedHeaders = StringValuedMapHeaderFormatter.INSTANCE.format(headers);

        // TODO: Make the order predictable.
        // assertThat(formattedHeaders, is("{headerName1:\"headerValue1.1, headerValue1.2\", Headername2:\"headerValue2.1\"}"));
        assertThat(formattedHeaders, is("{headerName1:\"headerValue1.1, headerValue1.2\"}"));
    }
}