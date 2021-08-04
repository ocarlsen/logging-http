package com.ocarlsen.logging;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CachingServletOutputStreamTest {

    private static final String DATA = "jklmnop";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private CachingServletOutputStream cachingServletOutputStream;
    private OutputStream outputStream;

    @Before
    public void construct() {
        outputStream = mock(OutputStream.class);
        cachingServletOutputStream = new CachingServletOutputStream(outputStream);
    }

    @After
    public void close() throws IOException {
        cachingServletOutputStream.close();
    }

    @After
    public void noMoreInteractions() throws IOException {
        verify(outputStream).close();
        verifyNoMoreInteractions(outputStream);
    }

    @Test
    public void write_flush() throws IOException {
        IOUtils.write(DATA, cachingServletOutputStream, UTF_8);
        cachingServletOutputStream.flush();

        assertThat(cachingServletOutputStream.getBytes(), is(DATA.getBytes(UTF_8)));

        verify(outputStream).flush();
        verify(outputStream, times(DATA.length())).write(anyInt());
    }

    @Test
    public void isReady() {
        assertThat(cachingServletOutputStream.isReady(), is(true));
    }

    @Test
    public void setWtriteListener() {
        exception.expect(UnsupportedOperationException.class);
        final WriteListener ignored = null;
        cachingServletOutputStream.setWriteListener(ignored);
    }
}