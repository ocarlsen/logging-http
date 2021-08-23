package com.ocarlsen.logging.http.client.spring;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
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
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
    private EchoController echoController;

    @SuppressWarnings("unused")
    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void contextLoads() {
        assertNotNull(echoController);
    }

    @Test
    public void localServerPort() {
        assertThat(serverPort, is(greaterThan(10000)));
    }

    @Test
    public void restTemplate() {
        assertNotNull(restTemplate);
    }

    @SuppressWarnings("UnnecessaryToStringCall")
    @Test
    public void intercept() {

        // Given
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

        final HttpStatus expectedResponseStatus = HttpStatus.OK;
        final String expectedResponseBody = echoController.echo(requestBody, null, requestId);
        final HttpHeaders expectedResponseHeaders = new HttpHeaders();
        expectedResponseHeaders.setContentType(APPLICATION_JSON_UTF8);
        expectedResponseHeaders.setContentLength(expectedResponseBody.length());

        // When
        final HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        final ResponseEntity<String> responseEntity = restTemplate.exchange(requestUri.toUri(), requestMethod,
                requestEntity, String.class);

        // Then
        final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        final InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).debug("Method  : {}", requestMethod.name());
        inOrder.verify(logger).debug("URL     : {}", requestUri.toUri().toString());
        inOrder.verify(logger).debug(eq("Headers : {}"), argThat(containsHeaders(requestHeaders)));
        inOrder.verify(logger).debug("Body    : [{}]", requestBody);
        inOrder.verifyNoMoreInteractions();

        // Confidence checks
        final HttpHeaders actualRequestHeaders = echoController.getRequestHeaders();
        assertThat(actualRequestHeaders, containsHttpHeaders(requestHeaders));

        final HttpStatus responseStatus = responseEntity.getStatusCode();
        assertThat(responseStatus, is(expectedResponseStatus));

        final String responseBody = responseEntity.getBody();
        assertThat(responseBody, is(expectedResponseBody));

        final HttpHeaders responseHeaders = responseEntity.getHeaders();
        assertThat(responseHeaders, containsHttpHeaders(expectedResponseHeaders));
    }

    // TODO: Factor out, this is duplicated
    @SuppressWarnings("SameParameterValue")
    private static Matcher<HttpHeaders> containsHttpHeaders(final HttpHeaders httpHeaders) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(final Object actual) {
                if (actual instanceof HttpHeaders) {
                    final HttpHeaders actualHttpHeaders = (HttpHeaders) actual;

                    for (final Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
                        final String headerName = entry.getKey();
                        assertThat(actualHttpHeaders.containsKey(headerName), is(true));
                        final List<String> headerValues = httpHeaders.get(headerName);
                        assertThat(headerValues, is(notNullValue()));
                        final List<String> actualHeaderValues = actualHttpHeaders.get(headerName);
                        assertThat(actualHeaderValues, contains(headerValues.toArray()));
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("HttpHeaders to match ")
                           .appendValue(httpHeaders);

            }
        };
    }

    // TODO: Factor out, this is duplicated.
    private ArgumentMatcher<String> containsHeaders(final HttpHeaders headers) {
        return argument -> {

            // Convert Map.Entry to string and search ignoring case.
            for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
                final String headerPair = entry.toString();
                final boolean matches = containsStringIgnoringCase(headerPair).matches(argument);
                if (!matches) {
                    return false;
                }
            }
            return true;
        };
    }

    @SuppressWarnings("SameParameterValue")
    // TODO: Factor out, this is duplicated
    private String createUrlWithPort(final String path) {
        return "http://localhost:" + serverPort + path;
    }

    // TODO: Factor out, this is duplicated
    @Controller
    static class EchoController {

        private HttpHeaders requestHeaders;

        @SuppressWarnings("unused")
        @PostMapping(
                value = CONTROLLER_URI,
                consumes = TEXT_PLAIN_VALUE,
                produces = APPLICATION_JSON_VALUE)
        @ResponseBody  // Without this, we get 404
        public String echo(@RequestBody final String body,
                           @RequestHeader final HttpHeaders headers,
                           @RequestParam(ID) final Integer id) {

            // Keep for confidence check later.
            this.requestHeaders = headers;

            return format("{\"%s\":\"%s\", \"%s\": %d}", BODY, body, ID, id);
        }

        public HttpHeaders getRequestHeaders() {
            return requestHeaders;
        }
    }

    @SuppressWarnings("unused")
    @Configuration
    @EnableAutoConfiguration
    static class Config {

        @Bean
        EchoController echoController() {
            return new EchoController();
        }

        @Bean
        RequestLoggingInterceptor loggingInterceptor() {
            return new RequestLoggingInterceptor();
        }

        @Bean
        RestTemplate restTemplate(final ClientHttpRequestInterceptor interceptor) {
            final RestTemplate restTemplate = new RestTemplate();

            // Make sure buffering enabled.
            final ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
            final SimpleClientHttpRequestFactory simpleRequestFactory = (SimpleClientHttpRequestFactory) requestFactory;
            simpleRequestFactory.setBufferRequestBody(true);    // Be explicit.

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