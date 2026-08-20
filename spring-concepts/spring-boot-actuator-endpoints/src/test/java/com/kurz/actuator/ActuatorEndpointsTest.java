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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ActuatorEndpointsTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("/actuator/health is exposed by default and reports an UP aggregate with component details")
    void healthEndpointReportsAggregateStatus() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
        assertThat(response.getBody()).contains("\"components\"");
    }

    @Test
    @DisplayName("/actuator/info is opted into exposure and reports the configured app name")
    void infoEndpointIsExposedAndPopulated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/info", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Kurz Actuator Lab");
    }

    @Test
    @DisplayName("/actuator/beans is opted into exposure")
    void beansEndpointIsExposed() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/beans", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("/actuator/loggers was never opted into exposure and stays unreachable")
    void loggersEndpointStaysUnexposed() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/loggers", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("/actuator/health/readiness reflects the custom readiness group, including diskSpace")
    void readinessGroupIncludesDiskSpace() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health/readiness", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
        assertThat(response.getBody()).contains("diskSpace");
    }
}
