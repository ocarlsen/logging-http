package com.ocarlsen.logging.http;

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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ResponseLoggingInterceptorTest.Config.class})
public class ResponseLoggingInterceptorTest {

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

        final UriComponents requestUri = UriComponentsBuilder.fromUriString("/logging_test").build();
        final String requestBody = "Hello!";
        final HttpMethod requestMethod = HttpMethod.GET;

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

        mockServer.expect(requestTo(requestUri.toUriString()))
                  .andExpect(header(ACCEPT, APPLICATION_JSON_VALUE))
                  .andExpect(header(CONTENT_TYPE, TEXT_PLAIN_VALUE))
                  .andExpect(header(CONTENT_LENGTH, Integer.toString(requestBody.length())))
                  .andExpect(method(requestMethod))
                  .andRespond(withStatus(responseStatus).headers(responseHeaders).body(responseBody));

        final HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        final ResponseEntity<String> responseEntity = restTemplate.exchange(requestUri.toUri(), requestMethod,
                requestEntity, String.class);

        final HttpStatus actualStatus = responseEntity.getStatusCode();
        assertThat(actualStatus, is(responseStatus));

        final String actualBody = responseEntity.getBody();
        assertThat(actualBody, is(responseBody));

        final HttpHeaders actualHeaders = responseEntity.getHeaders();
        assertThat(actualHeaders, is(responseHeaders));

        mockServer.verify();

        final Logger mockLogger = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);
        final InOrder inOrder = inOrder(mockLogger);
        inOrder.verify(mockLogger).debug("Status  : {}", responseStatus);
        inOrder.verify(mockLogger).debug("Headers : {}", responseHeaders);
        inOrder.verify(mockLogger).debug("Body    : [{}]", responseBody);
        inOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    static class Config {

        @Bean
        RestTemplate restTemplate() {
            final RestTemplate restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add(new ResponseLoggingInterceptor());

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