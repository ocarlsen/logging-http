package com.ocarlsen.logging.http.format;

import com.sun.net.httpserver.Headers;

import java.util.Iterator;
import java.util.List;

import static com.ocarlsen.logging.http.HeaderArgumentMatchers.buildHeaderValueExpression;

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
}
