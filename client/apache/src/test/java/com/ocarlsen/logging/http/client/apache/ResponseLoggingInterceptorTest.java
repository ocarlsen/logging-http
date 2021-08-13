package com.ocarlsen.logging.http.client.apache;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TEMPORARY_REDIRECT;

@RunWith(Theories.class)
public class ResponseLoggingInterceptorTest {

    @DataPoints
    public static final HttpStatus[] HTTP_STATUSES = {OK, TEMPORARY_REDIRECT, NOT_FOUND, INTERNAL_SERVER_ERROR};

    private final ResponseLoggingInterceptor responseInterceptor = new ResponseLoggingInterceptor();


    @Theory
    @Test
    public void process_gzip(final HttpStatus httpStatus) throws Exception {

        // Given
        final String bodyIn = "hello";
        final int statusCode = httpStatus.value();
        final String reasonPhrase = httpStatus.getReasonPhrase();

        // Prepare mocks
        final StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(statusLine.getReasonPhrase()).thenReturn(reasonPhrase);
        final Header header1 = mock(Header.class);
        final Header header2 = mock(Header.class);
        final Header[] headers = {header1, header2};
        final HttpEntity entityIn = EntityBuilder.create()
                                                 .setText(bodyIn)
                                                 .gzipCompress() // Automatically sets content encoding header
                                                 .build();
        final HttpResponse httpResponse = new BasicHttpResponse(statusLine);
        httpResponse.setHeaders(headers);
        httpResponse.setEntity(entityIn);
        final HttpContext httpContext = mock(HttpContext.class);

        // When
        responseInterceptor.process(httpResponse, httpContext);

        // Then
        final Logger mockLogger = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);
        verify(mockLogger).debug("Status  : {}", statusCode);
        verify(mockLogger).debug("Headers : {}", List.of(header1, header2));
        verify(mockLogger).debug("Body    : [{}]", bodyIn);
        verifyNoMoreInteractions(mockLogger);
        reset(mockLogger);

        // Entity not replaced.
        HttpEntity entityOut = httpResponse.getEntity();
        assertThat(entityOut, is(sameInstance(entityIn)));

        // Decompress.
        entityOut = new GzipDecompressingEntity(
                new GzipContentEnablingEntity(
                        (GzipCompressingEntity) entityOut));

        // Entity not consumed.
        final String bodyOut = EntityUtils.toString(entityOut, UTF_8);
        assertThat(bodyOut, is(bodyIn));

        // Verify mocks
        verify(statusLine).getProtocolVersion();
        verify(statusLine, times(2)).getStatusCode();
        verify(statusLine).getReasonPhrase();
        verifyNoMoreInteractions(statusLine, header1, header2, httpContext);
    }

    @Theory
    @Test
    public void process_withEntity_repeatable(final HttpStatus httpStatus) throws Exception {

        // Given
        final String bodyIn = "hello";
        final int statusCode = httpStatus.value();
        final String reasonPhrase = httpStatus.getReasonPhrase();

        // Prepare mocks
        final StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(statusLine.getReasonPhrase()).thenReturn(reasonPhrase);
        final Header header1 = mock(Header.class);
        final Header header2 = mock(Header.class);
        final Header[] headers = {header1, header2};
        final HttpEntity entityIn = EntityBuilder.create()
                                                 .setText(bodyIn)
                                                 .build();
        final HttpResponse httpResponse = new BasicHttpResponse(statusLine);
        httpResponse.setHeaders(headers);
        httpResponse.setEntity(entityIn);
        final HttpContext httpContext = mock(HttpContext.class);

        // When
        responseInterceptor.process(httpResponse, httpContext);

        // Then
        final Logger mockLogger = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);
        verify(mockLogger).debug("Status  : {}", statusCode);
        verify(mockLogger).debug("Headers : {}", List.of(header1, header2));
        verify(mockLogger).debug("Body    : [{}]", bodyIn);
        verifyNoMoreInteractions(mockLogger);
        reset(mockLogger);

        // Entity not replaced.
        final HttpEntity entityOut = httpResponse.getEntity();
        assertThat(entityOut, is(sameInstance(entityIn)));

        // Entity not consumed.
        final String bodyOut = EntityUtils.toString(entityOut, UTF_8);
        assertThat(bodyOut, is(bodyIn));

        // Verify mocks
        verify(statusLine).getProtocolVersion();
        verify(statusLine, times(2)).getStatusCode();
        verify(statusLine).getReasonPhrase();
        verifyNoMoreInteractions(statusLine, header1, header2, httpContext);
    }

    @Theory
    @Test
    public void process_withEntity_notRepeatable(final HttpStatus httpStatus) throws Exception {

        // Given
        final String bodyIn = "hello";
        final int statusCode = httpStatus.value();
        final String reasonPhrase = httpStatus.getReasonPhrase();

        // Prepare mocks
        final StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(statusLine.getReasonPhrase()).thenReturn(reasonPhrase);
        final Header header1 = mock(Header.class);
        final Header header2 = mock(Header.class);
        final Header[] headers = {header1, header2};
        final HttpEntity entityIn = EntityBuilder.create()
                                                 .setStream(IOUtils.toInputStream(bodyIn, UTF_8))
                                                 .build();
        final HttpResponse httpResponse = new BasicHttpResponse(statusLine);
        httpResponse.setHeaders(headers);
        httpResponse.setEntity(entityIn);
        final HttpContext httpContext = mock(HttpContext.class);

        // When
        responseInterceptor.process(httpResponse, httpContext);

        // Then
        final Logger mockLogger = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);
        verify(mockLogger).debug("Status  : {}", statusCode);
        verify(mockLogger).debug("Headers : {}", List.of(header1, header2));
        verify(mockLogger).debug("Body    : [{}]", bodyIn);
        verifyNoMoreInteractions(mockLogger);
        reset(mockLogger);

        // Entity is replaced.
        final HttpEntity entityOut = httpResponse.getEntity();
        assertThat(entityOut, is(not(sameInstance(entityIn))));
        assertThat(entityOut.isRepeatable(), is(true));

        // Entity not consumed.
        final String bodyOut = EntityUtils.toString(entityOut, UTF_8);
        assertThat(bodyOut, is(bodyIn));

        // Verify mocks
        verify(statusLine).getProtocolVersion();
        verify(statusLine, times(2)).getStatusCode();
        verify(statusLine).getReasonPhrase();
        verifyNoMoreInteractions(statusLine, header1, header2, httpContext);
    }
}