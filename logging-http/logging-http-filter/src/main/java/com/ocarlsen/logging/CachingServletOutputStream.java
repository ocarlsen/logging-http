package com.ocarlsen.logging;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Basically forwards {@link #write(int)}s to the delegate {@link OutputStream} and copies
 * the data to the internal {@link ByteArrayOutputStream}.
 */
public class CachingServletOutputStream extends ServletOutputStream {

    private final OutputStream outputStream;
    private final ByteArrayOutputStream cachedOutput;

    public CachingServletOutputStream(final OutputStream outputStream) {
        this.outputStream = outputStream;
        this.cachedOutput = new ByteArrayOutputStream();
    }

    @Override
    public void write(final int b) throws IOException {
        outputStream.write(b);
        cachedOutput.write(b);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
        cachedOutput.flush();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(final WriteListener readListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        cachedOutput.close();
    }

    public byte[] getBytes() {
        return cachedOutput.toByteArray();
    }

}
