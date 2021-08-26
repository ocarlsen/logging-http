package com.ocarlsen.logging.http.client.spring;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static com.ocarlsen.logging.http.HeaderArgumentMatchers.containsHttpHeadersIgnoringCase;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ResponseLoggingInterceptorMockServerIT.Config.class})
public class ResponseLoggingInterceptorMockServerIT {

    @SuppressWarnings("unused")
    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer
                .bindTo(restTemplate)
                .bufferContent()  // So response not consumed by interceptor
                .build();
    }

    @Test
    public void intercept() {

        // Given
        final UriComponents requestUri = UriComponentsBuilder.fromUriString("/logging_test?abc=def").build();
        final String requestBody = "Hello!";
        final HttpMethod requestMethod = HttpMethod.POST;

        // AbstractHttpMessageConverter#addDefaultHeaders will add Content-Type and Content-Length if we don't.
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(singletonList(APPLICATION_JSON));
        requestHeaders.setContentType(TEXT_PLAIN);
        requestHeaders.setContentLength(requestBody.length());

        final HttpStatus responseStatus = HttpStatus.ACCEPTED;
        final String responseBody = "{\"message\":\"Goodbye!\"}";
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setAccept(singletonList(TEXT_PLAIN));
        responseHeaders.setContentType(APPLICATION_JSON);
        responseHeaders.setContentLength(responseBody.length());

        // Prepare mock
        mockServer.expect(requestTo(requestUri.toUriString()))
                  .andExpect(header(ACCEPT, APPLICATION_JSON_VALUE))
                  .andExpect(header(CONTENT_TYPE, TEXT_PLAIN_VALUE))
                  .andExpect(header(CONTENT_LENGTH, Integer.toString(requestBody.length())))
                  .andExpect(method(requestMethod))
                  .andExpect(content().string(requestBody))
                  .andRespond(withStatus(responseStatus).headers(responseHeaders).body(responseBody));

        // When
        final HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        final ResponseEntity<String> responseEntity = restTemplate.exchange(requestUri.toUri(), requestMethod,
                requestEntity, String.class);

        // Then
        final Logger logger = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);
        final InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).debug("Status  : {}", responseStatus.value());
        inOrder.verify(logger).debug(eq("Headers : {}"), argThat(containsHttpHeadersIgnoringCase(responseHeaders)));
        inOrder.verify(logger).debug("Body    : [{}]", responseBody);
        inOrder.verifyNoMoreInteractions();

        // Confidence checks
        final HttpStatus actualStatus = responseEntity.getStatusCode();
        assertThat(actualStatus, is(responseStatus));

        final String actualResponseBody = responseEntity.getBody();
        assertThat(actualResponseBody, is(responseBody));

        final HttpHeaders actualHeaders = responseEntity.getHeaders();
        assertThat(actualHeaders, is(responseHeaders));

        // Verify mock
        mockServer.verify();
    }

    @SuppressWarnings("unused")
    static class Config {


        @Bean
        ResponseLoggingInterceptor loggingInterceptor() {
            return new ResponseLoggingInterceptor();
        }

        @Bean
        RestTemplate restTemplate(final ClientHttpRequestInterceptor interceptor) {
            final RestTemplate restTemplate = new RestTemplate();

            // Add interceptor under test
            restTemplate.getInterceptors().add(interceptor);

            // Disable annoying "Accept-Charset" header, interferes with test.
            for (final HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
                if (converter instanceof StringHttpMessageConverter) {
                    ((StringHttpMessageConverter) converter).setWriteAcceptCharset(false);
                }
            }

            return restTemplate;
        }
    }
}