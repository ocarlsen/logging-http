package com.ocarlsen.logging.http.client.apache;

import com.ocarlsen.logging.http.GzipContentEnablingEntity;
import com.ocarlsen.logging.http.format.ArrayHeaderFormatter;
import com.ocarlsen.logging.http.format.HeaderFormatter;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.lang.invoke.MethodHandles.lookup;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RequestLoggingInterceptor implements HttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    private final HeaderFormatter<Header[]> headerFormatter = ArrayHeaderFormatter.INSTANCE;

    @Override
    public void process(final HttpRequest httpRequest, final HttpContext httpContext) throws IOException {
        logRequest(httpRequest);
    }

    private void logRequest(final HttpRequest request) throws IOException {
        LOGGER.debug("Method  : {}", request.getRequestLine().getMethod());
        LOGGER.debug("URL     : {}", request.getRequestLine().getUri());

        final String headersFormatted = headerFormatter.format(request.getAllHeaders());
        LOGGER.debug("Headers : {}", headersFormatted);

        final String body;
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();

            // TODO: Generalize this, replicate for other clients
            // TODO: Test
            // Handle content encoding
            final Header contentEncodingHeader = entity.getContentEncoding();
            if (contentEncodingHeader != null) {
                final HeaderElement[] encodings = contentEncodingHeader.getElements();
                for (final HeaderElement encoding : encodings) {
                    if (encoding.getName().equalsIgnoreCase("gzip")) {

                        // In case of GzipCompressingEntity, need to wrap so GzipDecompressingEntity can read content.
                        if (entity instanceof GzipCompressingEntity) {
                            entity = new GzipContentEnablingEntity((GzipCompressingEntity) entity);
                        }

                        entity = new GzipDecompressingEntity(entity);
                        break;
                    }
                }
            }

            body = EntityUtils.toString(entity, UTF_8);

            // If entity has been consumed, we need to restore.
            if (!entity.isRepeatable()) {
                final HttpEntity repeatableEntity = EntityBuilder.create()
                                                                 .setText(body)
                                                                 // TODO: restore GZIP
                                                                 .build();
                ((HttpEntityEnclosingRequest) request).setEntity(repeatableEntity);
            }
        } else {
            body = "";
        }
        LOGGER.debug("Body    : [{}]", body);
    }
}
