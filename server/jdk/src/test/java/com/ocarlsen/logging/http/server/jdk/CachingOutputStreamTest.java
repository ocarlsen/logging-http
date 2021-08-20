package com.ocarlsen.logging.http.server.jdk;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

public class CachingOutputStreamTest {

    private static final String DATA = "qrstuv";

    private CachingOutputStream cachingOutputStream;
    private OutputStream outputStream;

    @Before
    public void construct() {
        outputStream = mock(OutputStream.class);
        cachingOutputStream = new CachingOutputStream(outputStream);
    }

    @After
    public void close() throws IOException {
        cachingOutputStream.close();
    }

    @After
    public void noMoreInteractions() throws IOException {
        verify(outputStream).close();
        verifyNoMoreInteractions(outputStream);
    }

    @Test
    public void write_flush() throws IOException {
        IOUtils.write(DATA, cachingOutputStream, UTF_8);
        cachingOutputStream.flush();

        assertThat(cachingOutputStream.getBytes(), is(DATA.getBytes(UTF_8)));

        verify(outputStream).flush();
        verify(outputStream, times(DATA.length())).write(anyInt());
    }
}