package io.jfatura.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestClient;

/**
 * {@code tests/helpers/mock-fetch.ts} karşılığı: GİB'e giden tüm istekleri
 * yakalar ve sıradaki kuyruğa alınmış JSON yanıtını döndürür.
 *
 * <pre>
 * gib.once("{\"data\":[]}");          // mockFetchOnce
 * gib.sequence(a, b, c);              // mockFetchSequence
 * GibHttpMock.Call call = gib.call(0);// getFetchCall()
 * </pre>
 */
public final class GibHttpMock implements ClientHttpRequestFactory {

    public record Call(int index, URI uri, String method, HttpHeaders headers, String rawBody) {

        /** GIB komutu — dispatch çağrılarında bulunur. */
        public String cmd() {
            return formParam(rawBody, "cmd");
        }

        public String callid() {
            return formParam(rawBody, "callid");
        }

        public String pageName() {
            return formParam(rawBody, "pageName");
        }

        public String token() {
            return formParam(rawBody, "token");
        }

        /** Login/logout komutu — assos-login çağrılarında bulunur. */
        public String assoscmd() {
            return formParam(rawBody, "assoscmd");
        }

        /** Çözümlenmiş {@code jp} JSON payload'ı. */
        public Map<String, Object> jp() {
            String raw = formParam(rawBody, "jp");
            if (raw == null || raw.isEmpty()) {
                return Map.of();
            }
            try {
                String decoded = java.net.URLDecoder.decode(raw, StandardCharsets.UTF_8);
                return new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(
                                decoded, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } catch (IOException e) {
                throw new IllegalStateException("jp payload ayrıştırılamadı: " + raw, e);
            }
        }
    }

    private final List<String> queuedResponses = new ArrayList<>();
    private final List<Call> calls = new ArrayList<>();

    public static GibHttpMock bindTo(RestClient.Builder builder) {
        GibHttpMock mock = new GibHttpMock();
        builder.requestFactory(mock);
        return mock;
    }

    public void once(String jsonResponse) {
        queuedResponses.add(jsonResponse);
    }

    public void sequence(String... jsonResponses) {
        for (String body : jsonResponses) {
            once(body);
        }
    }

    public Call call(int index) {
        return calls.get(index);
    }

    public int count() {
        return calls.size();
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return new CapturingRequest(calls.size(), uri, httpMethod);
    }

    private static String formParam(String body, String name) {
        for (String pair : body.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq < 0 ? pair : pair.substring(0, eq);
            if (key.equals(name)) {
                return eq < 0 ? "" : pair.substring(eq + 1);
            }
        }
        return null;
    }

    private final class CapturingRequest implements ClientHttpRequest {

        private final int index;
        private final URI uri;
        private final HttpMethod method;
        private final HttpHeaders headers = new HttpHeaders();
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();

        private CapturingRequest(int index, URI uri, HttpMethod method) {
            this.index = index;
            this.uri = uri;
            this.method = method;
        }

        @Override
        public HttpMethod getMethod() {
            return method;
        }

        @Override
        public URI getURI() {
            return uri;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return new java.util.HashMap<>();
        }

        @Override
        public OutputStream getBody() {
            return body;
        }

        @Override
        public ClientHttpResponse execute() throws IOException {
            String rawBody = body.toString(StandardCharsets.UTF_8);
            calls.add(new Call(index, uri, method.name(), new HttpHeaders(headers), rawBody));
            if (queuedResponses.isEmpty()) {
                throw new AssertionError(
                        "Kuyrukta yanıt yok ama GİB çağrısı yapıldı (#" + calls.size() + ": " + rawBody + ")");
            }
            String next = queuedResponses.remove(0);
            return new MockClientHttpResponse(next.getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
        }
    }
}
