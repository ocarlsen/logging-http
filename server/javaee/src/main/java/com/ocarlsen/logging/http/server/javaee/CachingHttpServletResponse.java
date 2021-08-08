package com.ocarlsen.logging.http.server.javaee;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class CachingHttpServletResponse extends HttpServletResponseWrapper {

    private ServletOutputStream outputStream;
    private PrintWriter writer;
    private CachingServletOutputStream cachingServletOutputStream;

    public CachingHttpServletResponse(final HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called on this response.");
        }

        if (outputStream == null) {
            outputStream = super.getOutputStream();
            cachingServletOutputStream = new CachingServletOutputStream(outputStream);
        }

        return cachingServletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called on this response.");
        }

        if (writer == null) {
            final ServletOutputStream outputStream = super.getOutputStream();
            cachingServletOutputStream = new CachingServletOutputStream(outputStream);

            final OutputStreamWriter outputStreamWriter = getOutputStreamWriter(cachingServletOutputStream);
            writer = new PrintWriter(outputStreamWriter, true);
        }

        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        } else if (cachingServletOutputStream != null) {
            cachingServletOutputStream.flush();
        }
    }

    public byte[] getCachedContent() {
        if (cachingServletOutputStream != null) {
            return cachingServletOutputStream.getBytes();
        } else {
            // Prevent NPEs.
            return new byte[0];
        }
    }


    private OutputStreamWriter getOutputStreamWriter(final CachingServletOutputStream outputStream) throws UnsupportedEncodingException {
        final String characterEncoding = super.getCharacterEncoding();
        if (characterEncoding != null) {
            return new OutputStreamWriter(outputStream, characterEncoding);
        } else {
            return new OutputStreamWriter(outputStream);
        }
    }

}