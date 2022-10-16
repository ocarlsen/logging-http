package com.ocarlsen.logging.http.client.apache;

import com.ocarlsen.logging.http.GzipContentEnablingEntity;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.ocarlsen.logging.http.HeaderArgumentMatchers.buildHeaderValueExpression2;
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

        final String headersFormatted = formatHeaders(response.getAllHeaders());
        LOGGER.debug("Headers : {}", headersFormatted);

        HttpEntity entity = response.getEntity();

        // TODO: Generalize this, replicate for other clients
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

        final String body = EntityUtils.toString(entity);
        LOGGER.debug("Body    : [{}]", body);

        // If entity has been consumed, we need to restore.
        if (!entity.isRepeatable()) {
            final HttpEntity repeatableEntity = EntityBuilder.create()
                                                             .setText(body)
                                                             // TODO: restore GZIP
                                                             .build();
            response.setEntity(repeatableEntity);
        }
    }

    // TODO: Factor out, this is duplicated
    private String formatHeaders(final Header[] headers) {
        final LinkedHashMap<String, List<String>> headerMap = new LinkedHashMap<>();
        for (final Header header : headers) {
            final String headerName = header.getName();
            final String headerValue = header.getValue();

            // Parse if contains comma, otherwise just wrap in List.
/*
        if (headerValue.lastIndexOf(',') > 0) {
            String[] headerValues = headerValue.split("\\s*,\\s*");
            return Arrays.stream(headerValues)
                         .collect(Collectors.toList());
        } else {
            return List.of(headerValue);
        }
*/
            final List<String> headerValues = List.of(headerValue);
            final List<String> currentValues = headerMap.computeIfAbsent(headerName, key -> new ArrayList<>());
            currentValues.addAll(headerValues);
        }
        return buildHeaderValueExpression2(headerMap);
    }

}
