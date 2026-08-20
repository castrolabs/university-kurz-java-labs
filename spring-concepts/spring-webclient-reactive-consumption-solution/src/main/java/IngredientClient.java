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

    public Mono<Ingredient> getIngredient(String id) {
        return webClient.get()
                .uri("/ingredients/{id}", id)
                .retrieve()
                .bodyToMono(Ingredient.class);
    }

    public Flux<Ingredient> getIngredients() {
        return webClient.get()
                .uri("/ingredients")
                .retrieve()
                .bodyToFlux(Ingredient.class);
    }

    public Mono<Ingredient> getIngredientResilient(String id) {
        return getIngredient(id)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(50)))
                .onErrorResume(e -> Mono.just(unknown(id)));
    }

    public Mono<Ingredient> getIngredientWithTimeout(String id, Duration timeout) {
        return getIngredient(id)
                .timeout(timeout)
                .onErrorResume(e -> Mono.just(unknown(id)));
    }

    private static Ingredient unknown(String id) {
        return new Ingredient(id, "UNKNOWN", "UNKNOWN");
    }
}
