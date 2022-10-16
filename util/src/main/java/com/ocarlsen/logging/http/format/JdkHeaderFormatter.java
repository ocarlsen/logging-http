package com.ocarlsen.logging.http.format;

import com.sun.net.httpserver.Headers;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public enum JdkHeaderFormatter implements HeaderFormatter<Headers> {
    INSTANCE;

    @Override
    public String format(final Headers headers) {
        final StringBuilder buf = new StringBuilder("{");
        for (Iterator<String> i = headers.keySet().iterator(); i.hasNext(); ) {
            final String key = i.next();
            buf.append(key).append(':');
            List<String> values = headers.get(key);
            buf.append(buildHeaderValueExpression(values));

            if (i.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append('}');
        return buf.toString();
    }

    private String buildHeaderValueExpression(final List<String> headerValues) {
        // Copied from HttpHeaders#formatHeaders.
        return (headerValues.size() == 1 ?
                "\"" + headerValues.get(0) + "\"" :
                headerValues.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));

    }
}
