package com.ocarlsen.logging.http.format;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.ocarlsen.logging.http.HeaderArgumentMatchers.buildHeaderValueExpression;

public enum ArrayHeaderFormatter implements HeaderFormatter<Header[]> {
    INSTANCE;

    @Override
    public String format(final Header[] headers) {
        final LinkedHashMap<String, List<String>> headerMap = new LinkedHashMap<>();
        for (final Header header : headers) {
            final String headerName = header.getName();
            final String headerValue = header.getValue();
            final List<String> headerValues = List.of(headerValue);
            final List<String> currentValues = headerMap.computeIfAbsent(headerName, key -> new ArrayList<>());
            currentValues.addAll(headerValues);
        }
        return buildHeaderValueExpression(headerMap);
    }
}
