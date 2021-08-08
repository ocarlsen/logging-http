package com.ocarlsen.logging.http.server.javaee;

import com.ocarlsen.logging.http.server.javaee.CachingHttpServletResponse;
import com.ocarlsen.logging.http.server.javaee.CachingServletOutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CachingHttpServletResponseTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void getCachedContent_noBody() throws IOException {

        // Prepare mocks
        final ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        Mockito.when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);

        // Given
        final CachingHttpServletResponse response = new CachingHttpServletResponse(httpServletResponse);
        response.flushBuffer();

        // When
        final byte[] cachedContent = response.getCachedContent();

        // Then
        final String dataOut = new String(cachedContent, UTF_8);
        assertThat(dataOut, is(""));

        // Verify mocks
        verifyNoMoreInteractions(httpServletResponse, servletOutputStream);
    }

    @Test
    public void getOutputStream() throws IOException {

        // Prepare mocks
        final ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        Mockito.when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);

        // Given
        final CachingHttpServletResponse response = new CachingHttpServletResponse(httpServletResponse);

        // When
        final String dataIn = "abcdefg";
        final ServletOutputStream outputStream = response.getOutputStream();
        IOUtils.write(dataIn, outputStream, UTF_8);
        response.flushBuffer();

        // Then
        assertThat(outputStream, is(instanceOf(CachingServletOutputStream.class)));
        final byte[] cachedContent = response.getCachedContent();
        final String dataOut = new String(cachedContent, UTF_8);
        assertThat(dataOut, is(dataIn));

        // Verify mocks
        verify(httpServletResponse).getOutputStream();
        verify(servletOutputStream, times(dataIn.length())).write(anyInt());
        verify(servletOutputStream).flush();
        verifyNoMoreInteractions(httpServletResponse, servletOutputStream);
    }

    @Test
    public void getOutputStream_thenGetWriter() throws IOException {

        // Prepare mocks
        final ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        Mockito.when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);

        // Given
        final CachingHttpServletResponse response = new CachingHttpServletResponse(httpServletResponse);
        response.getOutputStream();

        // Then
        exception.expect(IllegalStateException.class);
        exception.expectMessage("getOutputStream() has already been called on this response.");

        // When
        response.getWriter();

        // Verify mocks
        verify(httpServletResponse).getOutputStream();
        verifyNoMoreInteractions(httpServletResponse, servletOutputStream);
    }

    @Test
    public void getWriter_noEncoding() throws IOException {

        // Prepare mocks
        final OutputStream outputStream = mock(OutputStream.class);
        final ServletOutputStream servletOutputStream = new CachingServletOutputStream(outputStream);
        final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);
        when(httpServletResponse.getCharacterEncoding()).thenReturn(null);

        // Given
        final CachingHttpServletResponse response = new CachingHttpServletResponse(httpServletResponse);

        // When
        final PrintWriter printWriter = response.getWriter();
        final String dataIn = "qrstuvwxyz";
        printWriter.print(dataIn);
        response.flushBuffer();

        // Then
        final String dataOut = new String(response.getCachedContent());
        assertThat(dataOut, is(dataIn));

        // Verify mocks
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).getCharacterEncoding();
        verify(outputStream, times(dataIn.length())).write(anyInt());
        verify(outputStream).flush();
        verifyNoMoreInteractions(httpServletResponse, outputStream);
    }

    @Test
    public void getWriter_withEncoding() throws IOException {

        // Prepare mocks
        final OutputStream outputStream = mock(OutputStream.class);
        final ServletOutputStream servletOutputStream = new CachingServletOutputStream(outputStream);
        final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);
        when(httpServletResponse.getCharacterEncoding()).thenReturn("UTF-8");

        // Given
        final CachingHttpServletResponse response = new CachingHttpServletResponse(httpServletResponse);

        // When
        final PrintWriter printWriter = response.getWriter();
        final String dataIn = "eeffgghh";
        printWriter.print(dataIn);
        response.flushBuffer();

        // Then
        final String dataOut = new String(response.getCachedContent());
        assertThat(dataOut, is(dataIn));

        // Verify mocks
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).getCharacterEncoding();
        verify(outputStream, times(dataIn.length())).write(anyInt());
        verify(outputStream).flush();
        verifyNoMoreInteractions(httpServletResponse, outputStream);
    }

    @Test
    public void getWriter_thenGetOutputStream() throws IOException {

        // Prepare mocks
        final OutputStream outputStream = mock(OutputStream.class);
        final ServletOutputStream servletOutputStream = new CachingServletOutputStream(outputStream);
        final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);

        // Given
        final CachingHttpServletResponse response = new CachingHttpServletResponse(httpServletResponse);
        response.getWriter();

        // Then
        exception.expect(IllegalStateException.class);
        exception.expectMessage("getWriter() has already been called on this response.");

        // When
        response.getOutputStream();

        // Verify mocks
        verify(httpServletResponse).getWriter();
        verify(httpServletResponse).getOutputStream();
        verifyNoMoreInteractions(httpServletResponse, outputStream);

    }
}