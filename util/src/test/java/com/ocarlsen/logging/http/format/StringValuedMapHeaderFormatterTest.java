package com.ocarlsen.logging.http.format;

import java.util.Map;
import java.util.TreeMap;

public class StringValuedMapHeaderFormatterTest extends BaseHeaderFormatterTest<Map<String, String>> {

    @Override
    protected Map<String, String> buildHeaders() {
        final Map<String, String> headers = new TreeMap<String, String>();
        headers.put("headerName1", "headerValue1.1, headerValue1.2");
        headers.put("headerName2", "headerValue2.1");
        return headers;
    }

    @Override
    protected HeaderFormatter<Map<String, String>> buildHeaderFormatter() {
        return StringValuedMapHeaderFormatter.INSTANCE;
    }
}