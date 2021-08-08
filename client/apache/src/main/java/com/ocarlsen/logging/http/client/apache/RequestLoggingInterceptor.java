package com.ocarlsen.logging.http.client.apache;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// TODO: Finish
public class RequestLoggingInterceptor implements HttpRequestInterceptor {

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws IOException {

        final String method = request.getRequestLine().getMethod();
        System.out.println("method = " + method);

        final String uri = request.getRequestLine().getUri();
        System.out.println("uri = " + uri);

        final List<Header> headers = Arrays.asList(request.getAllHeaders());
        System.out.println("headers = " + headers);

        final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
        final String body = EntityUtils.toString(entity);
        System.out.println("body = " + body);
    }
}
