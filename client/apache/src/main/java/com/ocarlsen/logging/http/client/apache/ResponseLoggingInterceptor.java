package com.ocarlsen.logging.http.client.apache;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.invoke.MethodHandles.lookup;

public class ResponseLoggingInterceptor implements HttpResponseInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(lookup().lookupClass());

    @Override
    public void process(final HttpResponse httpResponse, final HttpContext httpContext) throws IOException {
        logResponse(httpResponse);
    }

    // TODO: Response/Request logging formatter
    private void logResponse(final HttpResponse response) throws IOException {
        LOGGER.debug("Status  : {}", response.getStatusLine().getStatusCode());
        LOGGER.debug("Headers : {}", Arrays.asList(response.getAllHeaders()));

        HttpEntity entity = response.getEntity();

        // Handle gzip encoding
        // TODO: Generalize this to other clients
        // TODO: Test this
        final Header contentEncodingHeader = entity.getContentEncoding();
        if (contentEncodingHeader != null) {
            final HeaderElement[] encodings = contentEncodingHeader.getElements();
            for (final HeaderElement encoding : encodings) {
                if (encoding.getName().equalsIgnoreCase("gzip")) {
                    entity = new GzipDecompressingEntity(entity);
                    break;
                }
            }
        }

        final String body = EntityUtils.toString(entity);
        LOGGER.debug("Body    : [{}]", body);
    }

}
