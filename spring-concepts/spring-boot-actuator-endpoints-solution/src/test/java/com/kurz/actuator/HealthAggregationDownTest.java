package com.kurz.actuator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

// Forces the built-in diskSpace health indicator DOWN by setting its free-space threshold far
// above any real disk's free space — no custom HealthIndicator needed to see how one DOWN
// component drags the whole aggregate status down.
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.health.diskspace.threshold=1000000GB")
@AutoConfigureTestRestTemplate
class HealthAggregationDownTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("a DOWN diskSpace component drags the aggregate /actuator/health status to DOWN")
    void downComponentPropagatesToAggregateStatus() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).contains("\"status\":\"DOWN\"");
    }
}
