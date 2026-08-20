import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@DisplayName("IngredientClient")
class IngredientClientTest {

    private static HttpServer server;
    private static int port;
    private static final AtomicInteger flakyAttempts = new AtomicInteger();

    private IngredientClient client;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/ingredients", IngredientClientTest::handle);
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @BeforeEach
    void setUp() {
        flakyAttempts.set(0);
        client = new IngredientClient(WebClient.create("http://localhost:" + port));
    }

    private static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/ingredients")) {
            respondJson(exchange, 200, """
                    [
                      {"id":"FLTO","name":"Flour Tortilla","type":"WRAP"},
                      {"id":"CHED","name":"Cheddar","type":"CHEESE"}
                    ]""");
            return;
        }

        String id = path.substring("/ingredients/".length());
        switch (id) {
            case "FLTO" -> respondJson(exchange, 200,
                    "{\"id\":\"FLTO\",\"name\":\"Flour Tortilla\",\"type\":\"WRAP\"}");
            case "FLAKY" -> {
                if (flakyAttempts.incrementAndGet() < 3) {
                    respondJson(exchange, 503, "{\"error\":\"unavailable\"}");
                } else {
                    respondJson(exchange, 200, "{\"id\":\"FLAKY\",\"name\":\"Jalapeno\",\"type\":\"VEGGIE\"}");
                }
            }
            case "BROKEN" -> respondJson(exchange, 503, "{\"error\":\"unavailable\"}");
            case "SLOW" -> {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                respondJson(exchange, 200, "{\"id\":\"SLOW\",\"name\":\"Slow Cheese\",\"type\":\"DAIRY\"}");
            }
            default -> respondJson(exchange, 404, "{\"error\":\"not found\"}");
        }
    }

    private static void respondJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    @Test
    @DisplayName("getIngredient decodes a successful response into an Ingredient")
    void shouldFetchSingleIngredient() {
        StepVerifier.create(client.getIngredient("FLTO"))
                .expectNext(new Ingredient("FLTO", "Flour Tortilla", "WRAP"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getIngredients streams every ingredient as a Flux")
    void shouldFetchAllIngredients() {
        StepVerifier.create(client.getIngredients())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("a 404 terminates the publisher with a WebClientResponseException, not a thrown exception at the call site")
    void shouldSignalErrorForNon2xx() {
        StepVerifier.create(client.getIngredient("MISSING"))
                .expectErrorMatches(e -> e instanceof WebClientResponseException wcre
                        && wcre.getStatusCode().value() == 404)
                .verify();
    }

    @Test
    @DisplayName("getIngredientResilient retries a transient failure and eventually succeeds")
    void shouldRetryTransientFailureAndSucceed() {
        StepVerifier.create(client.getIngredientResilient("FLAKY"))
                .expectNextMatches(i -> i.id().equals("FLAKY"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getIngredientResilient falls back once every retry attempt is exhausted")
    void shouldFallbackWhenRetriesExhausted() {
        StepVerifier.create(client.getIngredientResilient("BROKEN"))
                .expectNextMatches(i -> i.name().equals("UNKNOWN"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getIngredientWithTimeout falls back instead of waiting for a slow upstream (bonus)")
    void shouldFallbackOnTimeout() {
        StepVerifier.create(client.getIngredientWithTimeout("SLOW", Duration.ofMillis(100)))
                .expectNextMatches(i -> i.name().equals("UNKNOWN"))
                .verifyComplete();
    }
}
