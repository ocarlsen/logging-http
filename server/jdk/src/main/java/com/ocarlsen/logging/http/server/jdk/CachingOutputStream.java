package com.ocarlsen.logging.http.server.jdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Basically forwards {@link #write(int)}s to the delegate {@link OutputStream} and copies
 * the data to the internal {@link ByteArrayOutputStream}.
 */
public class CachingOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final ByteArrayOutputStream cachedOutput;

    public CachingOutputStream(final OutputStream outputStream) {
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
    public void close() throws IOException {
        outputStream.close();
        cachedOutput.close();
    }

    public byte[] getBytes() {
        return cachedOutput.toByteArray();
    }
}
