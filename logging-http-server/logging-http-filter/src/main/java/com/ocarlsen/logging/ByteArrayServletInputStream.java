package com.ocarlsen.logging;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Basically copies the input bytes into a {@link ByteArrayInputStream} that can be {@link #read()} multiple times.
 */
public class ByteArrayServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream cachedInput;

    public ByteArrayServletInputStream(final String s, final Charset charset) {
        this(s.getBytes(charset));
    }

    public ByteArrayServletInputStream(final byte[] bytes) {
        this.cachedInput = new ByteArrayInputStream(bytes);
    }

    @Override
    public int read() {
        return cachedInput.read();
    }

    @Override
    public boolean isFinished() {
        return cachedInput.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(final ReadListener readListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        cachedInput.close();
    }
}
