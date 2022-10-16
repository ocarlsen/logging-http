package com.ocarlsen.logging.http.format;

import java.util.Iterator;
import java.util.Map;

import static com.ocarlsen.logging.http.HeaderArgumentMatchers.buildHeaderValueExpression;

public enum StringValuedMapHeaderFormatter implements HeaderFormatter<Map<String, String>> {
    INSTANCE;

    @Override
    public String format(final Map<String, String> headers) {
        final StringBuilder buf = new StringBuilder("{");
        for (Iterator<String> i = headers.keySet().iterator(); i.hasNext(); ) {
            final String key = i.next();
            buf.append(key).append(':');
            String value = headers.get(key);
            buf.append(buildHeaderValueExpression(value));

            if (i.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append('}');
        return buf.toString();
    }
}
