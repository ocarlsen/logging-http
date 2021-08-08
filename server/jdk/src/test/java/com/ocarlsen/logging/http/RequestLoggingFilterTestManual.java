package com.ocarlsen.logging.http;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static java.nio.charset.StandardCharsets.UTF_8;


public class RequestLoggingFilterTestManual {

    @Test
    public void doFilter() throws IOException {

        // TODO: Random port
        final int port = 8000;
        final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        final String path = "/applications/myapp";
        final HttpContext context = server.createContext(path, new MyHandler());

        context.getFilters().add(new LoggingRequestFilter());

        server.setExecutor(null); // creates a default executor
        server.start();


        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {

            //Creating a request object
            final String uri = String.format("http://localhost:%d%s", port, path);
            final HttpGet httpGet = new HttpGet(uri);

            //Executing the request
            final HttpResponse response = httpclient.execute(httpGet);

            System.out.println(response.getStatusLine());

            final HttpEntity entity = response.getEntity();
            final String body = EntityUtils.toString(entity);
            System.out.println("body = " + body);


            server.stop(1);
        }
    }

    private static class MyHandler implements HttpHandler {

        @Override
        public void handle(final HttpExchange t) throws IOException {
            final InputStream is = t.getRequestBody();
            read(is); // .. read the request body

            final String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void read(final InputStream is) throws IOException {
            final String body = IOUtils.toString(is, UTF_8);
            System.out.println("body = " + body);
        }
    }

}