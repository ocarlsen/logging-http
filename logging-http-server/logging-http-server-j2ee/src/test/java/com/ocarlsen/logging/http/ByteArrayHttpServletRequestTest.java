package com.ocarlsen.logging.http;

import com.ocarlsen.logging.http.ByteArrayHttpServletRequest;
import com.ocarlsen.logging.http.ByteArrayServletInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ByteArrayHttpServletRequestTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void getInputStream() throws IOException {

        // Prepare mocks
        final String dataIn = "ghijklmnop";
        final ServletInputStream servletInputStream = new ByteArrayServletInputStream(dataIn, UTF_8);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);

        // Given
        final ByteArrayHttpServletRequest request = new ByteArrayHttpServletRequest(httpServletRequest);

        // When
        final ServletInputStream inputStream = request.getInputStream();

        // Then
        assertThat(inputStream, is(instanceOf(ByteArrayServletInputStream.class)));
        final String dataOut = IOUtils.toString(inputStream, UTF_8);
        assertThat(dataOut, is(dataIn));

        // Verify mocks
        verify(httpServletRequest).getInputStream();
        verifyNoMoreInteractions(httpServletRequest);
    }

    @Test
    public void getInputStream_thenGetReader() throws IOException {

        // Prepare mocks
        final String dataIn = "ghijklmnop";
        final ServletInputStream servletInputStream = new ByteArrayServletInputStream(dataIn, UTF_8);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);

        // Given
        final ByteArrayHttpServletRequest request = new ByteArrayHttpServletRequest(httpServletRequest);
        request.getInputStream();

        // Then
        exception.expect(IllegalStateException.class);
        exception.expectMessage("getInputStream() has already been called on this request.");

        // When
        request.getReader();

        // Verify mocks
        verify(httpServletRequest).getInputStream();
        verify(httpServletRequest).getReader();
        verifyNoMoreInteractions(httpServletRequest);
    }

    @Test
    public void getReader_noEncoding() throws IOException {

        // Prepare mocks
        final String dataIn = "qrstuvwxyz";
        final ServletInputStream servletInputStream = new ByteArrayServletInputStream(dataIn, UTF_8);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
        when(httpServletRequest.getCharacterEncoding()).thenReturn(null);

        // Given
        final ByteArrayHttpServletRequest request = new ByteArrayHttpServletRequest(httpServletRequest);

        // When
        final BufferedReader bufferedReader = request.getReader();

        // Then
        final String dataOut = IOUtils.toString(bufferedReader);
        assertThat(dataOut, is(dataIn));

        // Verify mocks
        verify(httpServletRequest).getInputStream();
        verify(httpServletRequest).getCharacterEncoding();
        verifyNoMoreInteractions(httpServletRequest);
    }

    @Test
    public void getReader_withEncoding() throws IOException {

        // Prepare mocks
        final String dataIn = "eeffgghh";
        final ServletInputStream servletInputStream = new ByteArrayServletInputStream(dataIn, UTF_8);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
        when(httpServletRequest.getCharacterEncoding()).thenReturn("UTF-8");

        // Given
        final ByteArrayHttpServletRequest request = new ByteArrayHttpServletRequest(httpServletRequest);

        // When
        final BufferedReader bufferedReader = request.getReader();

        // Then
        final String dataOut = IOUtils.toString(bufferedReader);
        assertThat(dataOut, is(dataIn));

        // Verify mocks
        verify(httpServletRequest).getInputStream();
        verify(httpServletRequest).getCharacterEncoding();
        verifyNoMoreInteractions(httpServletRequest);
    }

    @Test
    public void getReader_thenGetInputStream() throws IOException {

        // Prepare mocks
        final String dataIn = "qrstuvwxyz";
        final ServletInputStream servletInputStream = new ByteArrayServletInputStream(dataIn, UTF_8);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);

        // Given
        final ByteArrayHttpServletRequest request = new ByteArrayHttpServletRequest(httpServletRequest);
        request.getReader();

        // Then
        exception.expect(IllegalStateException.class);
        exception.expectMessage("getReader() has already been called on this request.");

        // When
        request.getInputStream();

        // Verify mocks
        verify(httpServletRequest).getReader();
        verify(httpServletRequest).getInputStream();
        verifyNoMoreInteractions(httpServletRequest);
    }

    @Test
    public void getCachedContent() throws IOException {

        // Prepare mocks
        final String dataIn = "aabbccdd";
        final ServletInputStream servletInputStream = new ByteArrayServletInputStream(dataIn, UTF_8);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);

        // Given
        final ByteArrayHttpServletRequest request = new ByteArrayHttpServletRequest(httpServletRequest);

        // When
        final byte[] cachedContent = request.getCachedContent();

        // Then
        final String dataOut = new String(cachedContent, UTF_8);
        assertThat(dataOut, is(dataIn));

        // Verify mocks
        verify(httpServletRequest).getInputStream();
        verifyNoMoreInteractions(httpServletRequest);
    }
}
