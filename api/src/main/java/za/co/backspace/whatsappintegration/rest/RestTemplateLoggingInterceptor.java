package za.co.backspace.whatsappintegration.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String requestBody = new String(body, StandardCharsets.UTF_8);
        logRequest(request, requestBody);
        ClientHttpResponse response = execution.execute(request, body);
        byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());
        logResponse(request.getURI().toString(), request.getMethod().toString(), response, responseBody);
        return new BufferingClientHttpResponseWrapper(response, responseBody);
    }

    static class BufferingClientHttpResponseWrapper implements ClientHttpResponse {
        private final ClientHttpResponse response;
        private final byte[] body;

        BufferingClientHttpResponseWrapper(ClientHttpResponse response, byte[] body) {
            this.response = response;
            this.body = body;
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body); // allows multiple reads
        }

        // delegate others
        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode(); // now it's HttpStatusCode
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return response.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }

    private void logRequest(HttpRequest request, String body) throws IOException {
        log.info("⬅️ Request: {} {} {} {}", request.getMethod(), request.getURI().toString(), request.getHeaders(),
                body);
    }

    private void logResponse(String url, String method, ClientHttpResponse response, byte[] body) throws IOException {
        log.info("⬅️ Response: {} {} {} {} {} {}", method, url, response.getStatusCode(), response.getStatusText(),
                response.getHeaders(),
                new String(body, StandardCharsets.UTF_8));
    }
}
