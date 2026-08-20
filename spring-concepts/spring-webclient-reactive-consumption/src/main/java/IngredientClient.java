import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * A minimal reactive client for a downstream "ingredients" HTTP API.
 * Every method returns a Mono/Flux describing the request -- none of them
 * perform I/O until the caller subscribes.
 */
public class IngredientClient {

    private final WebClient webClient;

    public IngredientClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Fetches a single ingredient by id. Nothing is sent over the network
     * until the returned Mono is subscribed to.
     */
    public Mono<Ingredient> getIngredient(String id) {
        // TODO-00: Build the request with webClient.get().uri("/ingredients/{id}", id),
        // call .retrieve(), and decode the body into a Mono<Ingredient> with
        // .bodyToMono(Ingredient.class).

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Streams every ingredient. Like getIngredient(), building this Flux
     * performs no I/O -- the request only fires on subscription.
     */
    public Flux<Ingredient> getIngredients() {
        // TODO-01: Same shape as getIngredient(), but GET "/ingredients" and
        // decode with .bodyToFlux(Ingredient.class) instead.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Like getIngredient(), but resilient to transient failures: retries a
     * few times with a short backoff, and falls back to an "UNKNOWN"
     * ingredient if every attempt still fails.
     */
    public Mono<Ingredient> getIngredientResilient(String id) {
        // TODO-02: Start from getIngredient(id), then chain:
        //   - .retryWhen(Retry.backoff(3, Duration.ofMillis(50))) to retry a
        //     transient failure (e.g. a 503) with exponential backoff.
        //   - .onErrorResume(e -> Mono.just(new Ingredient(id, "UNKNOWN", "UNKNOWN")))
        //     to recover once every retry attempt is exhausted.
        // This is the "timeouts/retries are Reactor operators, not client
        // settings" idea from the article: nothing here is WebClient-specific.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Like getIngredient(), but bounded by a timeout: falls back to an
     * "UNKNOWN" ingredient instead of waiting indefinitely for a slow
     * upstream response.
     */
    public Mono<Ingredient> getIngredientWithTimeout(String id, Duration timeout) {
        // TODO-03 (optional): Start from getIngredient(id), apply
        // .timeout(timeout), then .onErrorResume(...) to fall back the same
        // way getIngredientResilient() does.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
