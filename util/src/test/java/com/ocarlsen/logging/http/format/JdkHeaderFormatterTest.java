package com.ocarlsen.logging.http.format;

import com.sun.net.httpserver.Headers;

public class JdkHeaderFormatterTest extends BaseHeaderFormatterTest<Headers> {

    @Override
    protected Headers buildHeaders() {
        final Headers headers = new Headers();
        headers.add("headerName1", "headerValue1.1");  // Will normalize keys!
        headers.add("headerName1", "headerValue1.2");  // Will normalize keys!
        headers.add("headerName2", "headerValue2.1");  // Will normalize keys!
        return headers;
    }

    @Override
    protected HeaderFormatter<Headers> buildHeaderFormatter() {
        return JdkHeaderFormatter.INSTANCE;
    }

    @Override
    protected String getExpectedValue() {
        return "{Headername1:\"headerValue1.1\", \"headerValue1.2\", Headername2:\"headerValue2.1\"}";
    }

}