package com.ocarlsen.logging;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.ReadListener;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ByteArrayServletInputStreamTest {

    private static final String DATA = "abcdefg";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private ByteArrayServletInputStream byteArrayServletInputStream;

    @Before
    public void construct() {
        byteArrayServletInputStream = new ByteArrayServletInputStream(DATA, UTF_8);
    }

    @After
    public void close() throws IOException {
        byteArrayServletInputStream.close();
    }

    @Test
    public void read_isFinished() throws IOException {
        assertThat(byteArrayServletInputStream.isFinished(), is(false));

        final String data = IOUtils.toString(byteArrayServletInputStream, UTF_8);
        assertThat(data, is(DATA));

        assertThat(byteArrayServletInputStream.isFinished(), is(true));
    }

    @Test
    public void isReady() {
        assertThat(byteArrayServletInputStream.isReady(), is(true));
    }

    @Test
    public void setReadListener() {
        exception.expect(UnsupportedOperationException.class);
        final ReadListener ignored = null;
        byteArrayServletInputStream.setReadListener(ignored);
    }
}