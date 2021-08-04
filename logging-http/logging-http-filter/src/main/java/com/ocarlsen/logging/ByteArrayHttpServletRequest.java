package com.ocarlsen.logging;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * An {@link HttpServletRequest} backed by a {@link ByteArrayServletInputStream}.
 */
public class ByteArrayHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedContent;

    private BufferedReader reader;
    private ByteArrayServletInputStream byteArrayServletInputStream;

    public ByteArrayHttpServletRequest(final HttpServletRequest request) throws IOException {
        super(request);
        final InputStream requestInputStream = request.getInputStream();
        this.cachedContent = IOUtils.toByteArray(requestInputStream);
    }

    @Override
    public ServletInputStream getInputStream() {
        if (reader != null) {
            throw new IllegalStateException("getReader() has already been called on this request.");
        }

        if (byteArrayServletInputStream == null) {
            byteArrayServletInputStream = new ByteArrayServletInputStream(this.cachedContent);
        }

        return byteArrayServletInputStream;
    }

    @Override
    public BufferedReader getReader() throws UnsupportedEncodingException {
        if (byteArrayServletInputStream != null) {
            throw new IllegalStateException("getInputStream() has already been called on this request.");
        }

        if (reader == null) {
            final ByteArrayServletInputStream inputStream = new ByteArrayServletInputStream(this.cachedContent);

            final InputStreamReader inputStreamReader = getInputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
        }

        return reader;
    }

    public byte[] getCachedContent() {
        return cachedContent;
    }

    private InputStreamReader getInputStreamReader(final ByteArrayServletInputStream inputStream) throws UnsupportedEncodingException {
        final String characterEncoding = super.getCharacterEncoding();
        if (characterEncoding != null) {
            return new InputStreamReader(inputStream, characterEncoding);
        } else {
            return new InputStreamReader(inputStream);
        }
    }
}