package com.ocarlsen.logging.http.client.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.inOrder;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = RequestLoggingInterceptorSpringBootIT.Config.class)
public class RequestLoggingInterceptorSpringBootIT {

    private static final String CONTROLLER_URI = "/request_logging_test";
    private static final String BODY = "body";
    private static final String ID = "id";

    @SuppressWarnings("unused")
    @LocalServerPort
    private int serverPort;

    @SuppressWarnings("unused")
    @Autowired
    private MyController myController;

    @SuppressWarnings("unused")
    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void contextLoads() {
        assertNotNull(myController);
    }

    @Test
    public void localServerPort() {
        assertThat(serverPort, is(greaterThan(10000)));
    }

    @Test
    public void restTemplate() {
        assertNotNull(restTemplate);
    }

    @Test
    public void intercept() {

        final int requestId = 1234;
        final UriComponents requestUri = UriComponentsBuilder
                .fromUriString(createUrlWithPort(CONTROLLER_URI))
                .queryParam(ID, requestId)
                .build();
        final String requestBody = "Hello!";
        final HttpMethod requestMethod = HttpMethod.POST;

        // AbstractHttpMessageConverter #addDefaultHeaders will add Content-Type and Content-Length if we don't.
        // Although the HttpURLConnection #isRestrictedHeader method will prevent Content-Length from being sent over the wire,
        // this is client-side logging so add it to list of expected headers.
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(singletonList(APPLICATION_JSON));
        requestHeaders.setContentType(TEXT_PLAIN);
        requestHeaders.setContentLength(requestBody.length());

        final HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        final ResponseEntity<String> responseEntity = restTemplate.exchange(requestUri.toUri(), requestMethod,
                requestEntity, String.class);

        final HttpStatus responseStatus = HttpStatus.OK;
        final String responseBody = myController.echo(requestBody, requestId);
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(APPLICATION_JSON_UTF8);
        responseHeaders.setContentLength(responseBody.length());

        final HttpStatus actualStatus = responseEntity.getStatusCode();
        assertThat(actualStatus, is(responseStatus));

        final String actualBody = responseEntity.getBody();
        assertThat(actualBody, is(responseBody));

        final HttpHeaders actualHeaders = responseEntity.getHeaders();
        for (final Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            assertThat(actualHeaders, hasEntry(entry.getKey(), entry.getValue()));
        }

        final Logger mockLogger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        final InOrder inOrder = inOrder(mockLogger);
        inOrder.verify(mockLogger).debug("Method  : {}", requestMethod);
        inOrder.verify(mockLogger).debug("URI:    : {}", requestUri.toUri());
        inOrder.verify(mockLogger).debug("Headers : {}", requestHeaders);
        inOrder.verify(mockLogger).debug("Body    : [{}]", requestBody);
        inOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("SameParameterValue")
    // TODO: Factor out, this is duplicated
    private String createUrlWithPort(final String path) {
        return "http://localhost:" + serverPort + path;
    }

    @Controller
    static class MyController {

        @SuppressWarnings("unused")
        @PostMapping(
                value = CONTROLLER_URI,
                consumes = TEXT_PLAIN_VALUE,
                produces = APPLICATION_JSON_VALUE)
        @ResponseBody  // Without this, we get 404
        public String echo(@RequestBody final String body,
                           @RequestParam(ID) final Integer id) {
            return String.format("{\"%s\":\"%s\", \"%s\": %d}", BODY, body, ID, id);
        }
    }

    @SuppressWarnings("unused")
    @Configuration
    @EnableAutoConfiguration
    static class Config {

        @Bean
        public MyController myController() {
            return new MyController();
        }

        @Bean
        public RestTemplate restTemplate() {
            final RestTemplate restTemplate = new RestTemplate();

            // Make sure buffering enabled.
            final ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
            assertThat(requestFactory, is(instanceOf(SimpleClientHttpRequestFactory.class)));
            final SimpleClientHttpRequestFactory simpleRequestFactory = (SimpleClientHttpRequestFactory) requestFactory;
            simpleRequestFactory.setBufferRequestBody(true);    // Be explicit.

            // Add interceptor under test
            restTemplate.getInterceptors().add(new RequestLoggingInterceptor());

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