package com.ocarlsen.logging.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class allows another {@link HttpEntity} to read content from a {@link GzipCompressingEntity},
 * whose {@link GzipCompressingEntity#getContent()} method throws {@link UnsupportedOperationException}.
 */
public class GzipContentEnablingEntity extends HttpEntityWrapper {

    public GzipContentEnablingEntity(final GzipCompressingEntity gzipCompressingEntity) {
        super(gzipCompressingEntity);
    }

    @Override
    public InputStream getContent() throws IOException {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            this.writeTo(outputStream);
            final byte[] bytes = outputStream.toByteArray();
            return new ByteArrayInputStream(bytes);
        }
    }
}
