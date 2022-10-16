package com.ocarlsen.logging.http.format;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArrayHeaderFormatterTest {

    @Test
    public void format() {
        final Header header1 = new BasicHeader("headerName1", "headerValue1.1, headerValue1.2");
        final Header header2 = new BasicHeader("headerName2", "headerValue2.1");
        final Header[] headers = new Header[]{header1, header2};

        String formattedHeaders = ArrayHeaderFormatter.INSTANCE.format(headers);

        assertThat(formattedHeaders, is("{headerName1:\"headerValue1.1, headerValue1.2\", headerName2:\"headerValue2.1\"}"));
    }
}