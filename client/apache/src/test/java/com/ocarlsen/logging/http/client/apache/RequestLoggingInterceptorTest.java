package com.ocarlsen.logging.http.client.apache;

import com.ocarlsen.logging.http.GzipContentEnablingEntity;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(Theories.class)
public class RequestLoggingInterceptorTest {

    @DataPoints
    public static final String[] METHODS = {"GET", "POST", "PUT", "DELETE"};

    private final RequestLoggingInterceptor requestInterceptor = new RequestLoggingInterceptor();

    @Theory
    @Test
    public void process_noEntity(final String method) throws Exception {

        // Given
        final String uri = "https://www.ocarlsen.com/path?query=search";

        // Prepare mocks
        final Header header1 = mock(Header.class);
        final Header header2 = mock(Header.class);
        final Header[] headers = {header1, header2};
        final BasicHttpRequest httpRequest = new BasicHttpRequest(method, uri);
        httpRequest.setHeaders(headers);
        final HttpContext httpContext = mock(HttpContext.class);

        // When
        requestInterceptor.process(httpRequest, httpContext);

        // Then
        final Logger mockLogger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        verify(mockLogger).debug("Method  : {}", method);
        verify(mockLogger).debug("URL:    : {}", uri);
        verify(mockLogger).debug("Headers : {}", List.of(header1, header2));
        verify(mockLogger).debug("Body    : [{}]", "");
        verifyNoMoreInteractions(mockLogger);
        reset(mockLogger);

        // Verify mocks
        verifyNoMoreInteractions(header1, header2, httpContext);
    }

    @Theory
    @Test
    public void process_withEntity_gzip(final String method) throws Exception {

        // Given
        final String uri = "https://www.ocarlsen.com/path?query=search";
        final String bodyIn = "hello";

        // Prepare mocks
        final Header header1 = mock(Header.class);
        final Header header2 = mock(Header.class);
        final Header[] headers = {header1, header2};
        final HttpEntity entityIn = EntityBuilder.create()
                                                 .setText(bodyIn)
                                                 .gzipCompress() // Automatically sets content encoding header
                                                 .build();
        final HttpEntityEnclosingRequest httpRequest = new BasicHttpEntityEnclosingRequest(method, uri);
        httpRequest.setHeaders(headers);
        httpRequest.setEntity(entityIn);
        final HttpContext httpContext = mock(HttpContext.class);

        // When
        requestInterceptor.process(httpRequest, httpContext);

        // Then
        final Logger mockLogger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        verify(mockLogger).debug("Method  : {}", method);
        verify(mockLogger).debug("URL:    : {}", uri);
        verify(mockLogger).debug("Headers : {}", List.of(header1, header2));
        verify(mockLogger).debug("Body    : [{}]", bodyIn);
        verifyNoMoreInteractions(mockLogger);
        reset(mockLogger);

        // Entity not replaced.
        HttpEntity entityOut = httpRequest.getEntity();
        assertThat(entityOut, is(sameInstance(entityIn)));

        // Decompress.
        entityOut = new GzipDecompressingEntity(
                new GzipContentEnablingEntity(
                        (GzipCompressingEntity) entityOut));

        // Entity not consumed.
        final String bodyOut = EntityUtils.toString(entityOut, UTF_8);
        assertThat(bodyOut, is(bodyIn));

        // Verify mocks
        verifyNoMoreInteractions(header1, header2, httpContext);
    }

    @Theory
    @Test
    public void process_withEntity_repeatable(final String method) throws Exception {

        // Given
        final String uri = "https://www.ocarlsen.com/path?query=search";
        final String bodyIn = "hello";

        // Prepare mocks
        final Header header1 = mock(Header.class);
        final Header header2 = mock(Header.class);
        final Header[] headers = {header1, header2};
        final HttpEntity entityIn = EntityBuilder.create()
                                                 .setText(bodyIn)
                                                 .build();
        final HttpEntityEnclosingRequest httpRequest = new BasicHttpEntityEnclosingRequest(method, uri);
        httpRequest.setHeaders(headers);
        httpRequest.setEntity(entityIn);
        final HttpContext httpContext = mock(HttpContext.class);

        // When
        requestInterceptor.process(httpRequest, httpContext);

        // Then
        final Logger mockLogger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        verify(mockLogger).debug("Method  : {}", method);
        verify(mockLogger).debug("URL:    : {}", uri);
        verify(mockLogger).debug("Headers : {}", List.of(header1, header2));
        verify(mockLogger).debug("Body    : [{}]", bodyIn);
        verifyNoMoreInteractions(mockLogger);
        reset(mockLogger);

        // Entity not replaced.
        final HttpEntity entityOut = httpRequest.getEntity();
        assertThat(entityOut, is(sameInstance(entityIn)));

        // Entity not consumed.
        final String bodyOut = EntityUtils.toString(entityOut, UTF_8);
        assertThat(bodyOut, is(bodyIn));

        // Verify mocks
        verifyNoMoreInteractions(header1, header2, httpContext);
    }

    @Theory
    @Test
    public void process_withEntity_notRepeatable(final String method) throws Exception {

        // Given
        final String uri = "https://www.ocarlsen.com/path?query=search";
        final String bodyIn = "hello";

        // Prepare mocks
        final Header header1 = mock(Header.class);
        final Header header2 = mock(Header.class);
        final Header[] headers = {header1, header2};
        final HttpEntity entityIn = EntityBuilder.create()
                                                 .setStream(IOUtils.toInputStream(bodyIn, UTF_8))
                                                 .build();
        final HttpEntityEnclosingRequest httpRequest = new BasicHttpEntityEnclosingRequest(method, uri);
        httpRequest.setHeaders(headers);
        httpRequest.setEntity(entityIn);
        final HttpContext httpContext = mock(HttpContext.class);

        // When
        requestInterceptor.process(httpRequest, httpContext);

        // Then
        final Logger mockLogger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        verify(mockLogger).debug("Method  : {}", method);
        verify(mockLogger).debug("URL:    : {}", uri);
        verify(mockLogger).debug("Headers : {}", List.of(header1, header2));
        verify(mockLogger).debug("Body    : [{}]", bodyIn);
        verifyNoMoreInteractions(mockLogger);
        reset(mockLogger);

        // Entity is replaced.
        final HttpEntity entityOut = httpRequest.getEntity();
        assertThat(entityOut, is(not(sameInstance(entityIn))));
        assertThat(entityOut.isRepeatable(), is(true));

        // Entity not consumed.
        final String bodyOut = EntityUtils.toString(entityOut, UTF_8);
        assertThat(bodyOut, is(bodyIn));

        // Verify mocks
        verifyNoMoreInteractions(header1, header2, httpContext);
    }
}