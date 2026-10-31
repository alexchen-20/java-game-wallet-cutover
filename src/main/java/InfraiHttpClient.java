import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiHttpClient {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String apiKey;
    private final URI baseUrl;

    public InfraiHttpClient(LayeredConfig config) {
        this.apiKey = config.apiKey();
        this.baseUrl = config.baseUrl();
    }

    public String request(String method, String path, String jsonBody) {
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder(baseUrl.resolve(path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");
                HttpRequest.BodyPublisher body = jsonBody == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(jsonBody);
                HttpResponse<String> response = http.send(
                    builder.method(method, body).build(), HttpResponse.BodyHandlers.ofString());
                String envelope = response.body();
                boolean ok = envelope.contains("\"ok\":true");
                if (response.statusCode() == 429 && attempt < 3) {
                    sleep(retryDelay(response, attempt));
                    continue;
                }
                if (!ok) {
                    throw new InfraiException("Infrai rejected the request: " + envelope);
                }
                if (response.statusCode() >= 500) {
                    throw new InfraiException("Infrai transport status " + response.statusCode());
                }
                return envelope;
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InfraiException("Infrai transport failed", e);
            }
        }
        throw new InfraiException("Infrai retry budget exhausted");
    }

    private long retryDelay(HttpResponse<String> response, int attempt) {
        try { return Long.parseLong(response.headers().firstValue("Retry-After").orElse("0")) * 1000L; }
        catch (NumberFormatException ignored) { return (long) Math.pow(2, attempt) * 250L; }
    }

    private void sleep(long millis) {
        try { Thread.sleep(Math.min(millis, 5000L)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new InfraiException("Retry interrupted", e); }
    }

    public static final class InfraiException extends RuntimeException {
        public InfraiException(String message) { super(message); }
        public InfraiException(String message, Throwable cause) { super(message, cause); }
    }
}
