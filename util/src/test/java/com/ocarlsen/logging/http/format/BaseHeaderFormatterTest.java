package com.ocarlsen.logging.http.format;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class BaseHeaderFormatterTest<T> {

    @Test
    public void format() {
        final T headers = buildHeaders();

        final HeaderFormatter<T> headerFormatter = buildHeaderFormatter();
        String formattedHeaders = headerFormatter.format(headers);  // TODO: Make the order predictable.

        final String expectedValue = getExpectedValue();
        assertThat(formattedHeaders, is(expectedValue));
    }

    protected String getExpectedValue() {
        return "{headerName1:\"headerValue1.1, headerValue1.2\", headerName2:\"headerValue2.1\"}";
    }

    protected abstract T buildHeaders();

    protected abstract HeaderFormatter<T> buildHeaderFormatter();
}
