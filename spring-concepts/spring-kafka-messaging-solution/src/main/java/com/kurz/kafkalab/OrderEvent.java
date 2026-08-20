package com.kurz.kafkalab;

public record OrderEvent(String orderId, String status) {
}
