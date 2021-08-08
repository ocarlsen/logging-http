package com.ocarlsen.logging.http.client.apache;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// TODO: Finish
public class ResponseLoggingInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(final HttpResponse response, final HttpContext context) throws IOException {

        final int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("statusCode = " + statusCode);

        final List<Header> headers = Arrays.asList(response.getAllHeaders());
        System.out.println("headers = " + headers);

        HttpEntity entity = response.getEntity();

        // Handle gzip encoding
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
        System.out.println("body = " + body);

        // Replace entity for downstream consumers.
        final HttpEntity newEntity = new StringEntity(body, ContentType.get(entity));
        response.setEntity(newEntity);
    }

}
